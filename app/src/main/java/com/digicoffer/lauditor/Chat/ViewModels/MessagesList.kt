package com.digicoffer.lauditor.Chat.ViewModels

import android.Manifest
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.Chat.Adapters.MessageListAdapter
import com.digicoffer.lauditor.Chat.Model.MessageDo
import com.digicoffer.lauditor.Chat.Model.User
import com.digicoffer.lauditor.MainActivity
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.CommonFiles.ChatService.ChatConnection
import com.digicoffer.lauditor.CommonFiles.ChatService.ChatConnectionService
import com.digicoffer.lauditor.CommonFiles.ChatService.PresenceManager
import com.digicoffer.lauditor.CommonFiles.ChatService.TypingStateManager
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import org.jivesoftware.smack.SmackException
import org.jivesoftware.smack.XMPPException
import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smackx.forward.packet.Forwarded
import org.jivesoftware.smackx.mam.MamManager
import org.jivesoftware.smackx.sid.element.StanzaIdElement
import org.jxmpp.jid.impl.JidCreate
import org.jxmpp.stringprep.XmppStringprepException
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Objects

class MessagesList(
    private var rv_messagesList: RecyclerView? = null
) : Fragment() {

    private var ib_back_button: ImageView? = null
    private var mainActivity: MainActivity? = null
    private var bt_sendMessage: ImageView? = null
    private var bt_receiveMessage: ImageView? = null
    private var mChatView: EditText? = null
    private var tv_contactName: TextView? = null
    private var person_icon: TextView? = null
    private var tv_chat: TextView? = null
    private var tv_presence_status: TextView? = null
    private var header_presence_dot: View? = null
    private var progress_dialog: Dialog? = null
    private var fb_chat: FloatingActionButton? = null
    private var ivSearch: ImageView? = null
    private var searchLayout: View? = null
    private var etSearchChat: TextInputEditText? = null
    private var iv_search_cancel: ImageView? = null
    private var iv_video_call: ImageView? = null

    val message_list = ArrayList<MessageDo>()
    val time_list = ArrayList<MessageDo>()
    var adapter: MessageListAdapter? = null

    private var contactJid = ""
    private var contact_name = ""
    private var contact_type = ""
    private var currentJid = ""
    private var person_name = ""
    private var lastSeenMessageId: String? = null

    private var lastComposingSentAt = 0L
    private var isCurrentlyComposing = false

    private val typingPauseHandler = Handler(Looper.getMainLooper())
    private val pausedRunnable = Runnable { sendPausedIfWasComposing() }

    private var mBroadcastReceiver: BroadcastReceiver? = null

    private var isReconnecting = false
    private var hasShownConnectionToast = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.activity_message_list, container, false)

        rv_messagesList = v.findViewById(R.id.reyclerview_message_list)
        tv_contactName = v.findViewById(R.id.tv_contactName)
        ivSearch = v.findViewById(R.id.iv_search)
        searchLayout = v.findViewById(R.id.search_layout)
        iv_search_cancel = v.findViewById(R.id.iv_search_cancel)
        etSearchChat = searchLayout?.findViewById(R.id.et_search_chat)
        iv_video_call = v.findViewById(R.id.iv_video_call)
        person_icon = v.findViewById(R.id.person_icon)
        header_presence_dot = v.findViewById(R.id.header_presence_dot)
        tv_presence_status = v.findViewById(R.id.tv_presence_status)
        tv_chat = v.findViewById(R.id.tv_chat)
        mChatView = v.findViewById(R.id.edittext_chatbox)
        bt_sendMessage = v.findViewById(R.id.button_chatbox_send)
        ib_back_button = v.findViewById(R.id.back_button)

        requireActivity().window?.setSoftInputMode(
            android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        tv_chat?.setText(R.string.chat)
        tv_chat?.visibility = View.GONE

        ivSearch?.setOnClickListener {
            etSearchChat?.setText("")
            if (searchLayout?.visibility == View.VISIBLE) {
                searchLayout?.visibility = View.GONE
            } else {
                searchLayout?.visibility = View.VISIBLE
                etSearchChat?.requestFocus()
            }
        }

        iv_search_cancel?.setOnClickListener {
            etSearchChat?.setText("")
            searchLayout?.visibility = View.GONE
        }

        iv_video_call?.setOnClickListener { view ->
            val perms = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            if (AndroidUtils.hasPermissions(perms, requireContext())) {
                AndroidUtils.loadWebView(requireContext(), requireActivity())
            } else {
                AndroidUtils.showAlertDialog(view, requireActivity())
            }
        }

        ib_back_button?.setOnClickListener { loadBackPressed() }

        mChatView?.let { chatView ->
            updateSendButtonState(chatView.text.toString())
            chatView.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
                override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
                override fun afterTextChanged(e: Editable?) {
                    val text = e?.toString() ?: ""
                    updateSendButtonState(text)

                    if (text.trim().isEmpty()) {
                        sendPausedIfWasComposing()
                        return
                    }

                    sendComposingThrottled()
                    schedulePausedAfterInactivity()
                }
            })
        }

        bt_sendMessage?.setOnClickListener {
            val text = mChatView?.text?.toString()?.trim() ?: ""
            if (text.isEmpty()) {
                AndroidUtils.showToast("Message field cannot be empty", context)
                return@setOnClickListener
            }

            if (ChatConnectionService.getState() == ChatConnection.ConnectionState.CONNECTED) {
                Log.d("MessagesList", "Sending message: $text")

                val intent = Intent(ChatConnectionService.SEND_MESSAGE).apply {
                    putExtra(ChatConnectionService.BUNDLE_MESSAGE_BODY, text)
                    putExtra(ChatConnectionService.BUNDLE_TO, contactJid)
                    val name = if (Constants.USER_ID == "admin") Constants.FIRM_NAME else Constants.NAME
                    val subject = "$name ##$currentJid## #N#$name#N#"
                    putExtra(ChatConnectionService.BUNDLE_MESSAGE_SUBJECT, subject)
                }
                activity?.sendBroadcast(intent)

                val chatMessage = MessageDo().apply {
                    message = text
                    viewType = Constants.chat_SENT
                    iscurrentchat = true
                    createdAt = AndroidUtils.getDateToString(
                        Calendar.getInstance().time, "MMM dd, yyyy hh:mm a"
                    )
                    sender = User().apply { nickname = "" }
                }

                if (!isMessageExist(chatMessage)) {
                    time_list.add(chatMessage)
                    adapter?.addMessage(chatMessage)
                    mChatView?.setText("")
                    rv_messagesList?.smoothScrollToPosition(message_list.size - 1)
                }
            } else {
                Toast.makeText(
                    activity,
                    "Client not connected. Message not sent! Please try after a few seconds.",
                    Toast.LENGTH_LONG
                ).show()
                AndroidUtils.reconnecXMPPServer(requireActivity())
            }
        }

        val bundle = arguments
        if (bundle != null) {
            contactJid = bundle.getString("EXTRA_CONTACT_JID") ?: ""
            contact_name = bundle.getString("EXTRA_CONTACT_NAME") ?: ""
            contact_type = bundle.getString("EXTRA_CONTACT_TYPE") ?: ""
        }

        val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        currentJid = pref.getString("xmpp_jid", null) ?: ""
        pref.edit().putString("CURRENTCHAT_JID", contactJid).apply()

        Log.d("IDS..", "contactJid=$contactJid currentJid=$currentJid")

        tv_contactName?.text = contact_name
        if (contact_name.isNotEmpty()) {
            person_name = contact_name.substring(0, 1)
            person_icon?.text = person_name
        }

        updatePresenceDot()
        PresenceManager.addListener(presenceChangeListener)
        TypingStateManager.addListener(chatTypingListener)

        if (contact_type.equals("appointment", ignoreCase = true)) {
            iv_video_call?.visibility = View.GONE
        } else {
            iv_video_call?.visibility = View.VISIBLE
        }

        rv_messagesList?.layoutManager = GridLayoutManager(context, 1)
        rv_messagesList?.clipChildren = false
        rv_messagesList?.clipToPadding = false
        adapter = MessageListAdapter(requireContext(), message_list, person_name, time_list)
        rv_messagesList?.adapter = adapter

        etSearchChat?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = s?.toString() ?: ""
                adapter?.filter?.filter(text)
                iv_search_cancel?.setImageDrawable(
                    if (text.isEmpty()) {
                        context?.getDrawable(R.drawable.search_groups)
                    } else {
                        context?.getDrawable(R.drawable.simple_cancel)
                    }
                )
            }
        })

        Handler(Looper.getMainLooper()).postDelayed({
            if (isAdded && !isDetached && activity != null) {
                ChatHistoryTask().execute()
            }
        }, 1500)

        return v
    }

    private fun updateSendButtonState(text: String) {
        val enabled = text.trim().isNotEmpty()
        bt_sendMessage?.alpha = if (enabled) 1.0f else 0.5f
        bt_sendMessage?.isEnabled = enabled
    }

    private fun loadBackPressed() {
        Constants.Chat_id = ""
        val frag = Chat()
        activity?.supportFragmentManager
            ?.beginTransaction()
            ?.replace(R.id.id_framelayout, frag)
            ?.addToBackStack(null)
            ?.commit()
    }

    private fun sendReadMarkerForLastSeenMessage() {
        val lastId = lastSeenMessageId
        if (lastId.isNullOrEmpty()) return
        ChatConnection.sendDisplayedMarker(contactJid, lastId)
    }

    private fun sendComposingThrottled() {
        val now = System.currentTimeMillis()
        if (now - lastComposingSentAt < COMPOSING_THROTTLE_MS && isCurrentlyComposing) {
            return
        }

        ChatConnection.sendChatState(contactJid, ChatConnection.CHAT_STATE_COMPOSING)
        lastComposingSentAt = now
        isCurrentlyComposing = true
    }

    private fun schedulePausedAfterInactivity() {
        typingPauseHandler.removeCallbacks(pausedRunnable)
        typingPauseHandler.postDelayed(pausedRunnable, 1000L)
    }

    private fun sendPausedIfWasComposing() {
        typingPauseHandler.removeCallbacks(pausedRunnable)
        if (!isCurrentlyComposing) return

        ChatConnection.sendChatState(contactJid, ChatConnection.CHAT_STATE_PAUSED)
        isCurrentlyComposing = false
    }

    private fun sendGone() {
        typingPauseHandler.removeCallbacks(pausedRunnable)
        ChatConnection.sendChatState(contactJid, ChatConnection.CHAT_STATE_GONE)
        isCurrentlyComposing = false
    }

    private fun updatePresenceDot() {
        val dot = header_presence_dot ?: return
        val jid = contactJid
        val isOnline = PresenceManager.isOnline(jid)

        dot.visibility = View.VISIBLE
        dot.setBackgroundResource(
            if (isOnline) R.drawable.green_circular else R.drawable.red_circular
        )

        tv_presence_status?.let {
            it.visibility = View.VISIBLE
            it.text = if (isOnline) "Online" else "Offline"
        }
    }

    private val presenceChangeListener = object : PresenceManager.PresenceChangeListener {
        override fun onPresenceChanged(guid: String, isOnline: Boolean) {
            if (guid == contactJid) {
                updatePresenceDot()
            }
        }
    }

    private val chatTypingListener = object : TypingStateManager.TypingChangeListener {
        override fun onTypingChanged(guid: String, isTyping: Boolean) {
            if (guid == contactJid) {
                adapter?.setTypingBubbleVisible(isTyping)
                if (isTyping) {
                    adapter?.let { ad ->
                        rv_messagesList?.smoothScrollToPosition(ad.itemCount - 1)
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        sendGone()
        mBroadcastReceiver?.let {
            activity?.unregisterReceiver(it)
        }
    }

    override fun onStart() {
        super.onStart()
        val contextApp = context?.applicationContext
        if (contextApp != null) {
            val pref = PreferenceManager.getDefaultSharedPreferences(contextApp)
            pref.edit().putString("CURRENTCHAT_JID", contactJid).apply()
        }

        if (ChatConnectionService.getState() != ChatConnection.ConnectionState.CONNECTED) {
            AndroidUtils.reconnecXMPPServer(requireActivity())
        }
    }

    override fun onResume() {
        super.onResume()

        mBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent?.action ?: return

                if (action == ChatConnectionService.NEW_MESSAGE) {
                    val from = (intent.getStringExtra(ChatConnectionService.BUNDLE_FROM_JID) ?: "")
                        .split("@")[0]
                    val body = intent.getStringExtra(ChatConnectionService.BUNDLE_MESSAGE_BODY)
                    val incomingMessageId = intent.getStringExtra("message_id")

                    if (from == contactJid) {
                        val chatMessage = MessageDo().apply {
                            message = body
                            viewType = Constants.chat_RECEIVE
                            iscurrentchat = true
                            createdAt = AndroidUtils.getDateToString(
                                Calendar.getInstance().time, "MMM dd, yyyy hh:mm a"
                            )
                            stanzaId = incomingMessageId
                            sender = User().apply { nickname = contact_name }
                        }

                        if (!isMessageExist(chatMessage)) {
                            time_list.add(chatMessage)
                            adapter?.addMessage(chatMessage)
                            adapter?.let { ad ->
                                rv_messagesList?.smoothScrollToPosition(ad.itemCount - 1)
                            }

                            if (!incomingMessageId.isNullOrEmpty()) {
                                lastSeenMessageId = incomingMessageId
                                sendReadMarkerForLastSeenMessage()
                            }
                        }
                    } else {
                        Log.d("MessagesList", "Message from other JID: $from")
                    }
                }
            }
        }

        mBroadcastReceiver?.let {
            val filter = IntentFilter(ChatConnectionService.NEW_MESSAGE)
            ContextCompat.registerReceiver(requireContext(), it, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        PresenceManager.removeListener(presenceChangeListener)
        TypingStateManager.removeListener(chatTypingListener)
        activity?.window?.setSoftInputMode(
            android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        )
    }

    fun load_chat_history(resp: String) {
        if (context == null) return
        rv_messagesList?.layoutManager = GridLayoutManager(context, 1)
        adapter?.notifyDataSetChanged()
        if (!message_list.isEmpty()) {
            rv_messagesList?.smoothScrollToPosition(message_list.size - 1)
        }
        sendReadMarkerForLastSeenMessage()
    }

    inner class ChatHistoryTask : AsyncTask<Void, Void, String>() {
        private val MAX_RECONNECT_ATTEMPTS = 2
        private var reconnectAttempts = 0

        override fun onPreExecute() {
            super.onPreExecute()
            ChatUnreadCountUpdateTask(currentJid, contactJid).execute("")
            progress_dialog = AndroidUtils.get_progress(activity)
        }

        override fun doInBackground(vararg voids: Void?): String? {
            val conn = ChatConnection.mConnection
            if (conn == null || !conn.isConnected || !conn.isAuthenticated) {
                Log.e("ChatHistoryTask", "Not connected — attempt $reconnectAttempts/$MAX_RECONNECT_ATTEMPTS")

                if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                    reconnectAttempts++
                    if (reconnectAttempts >= 2) {
                        showConnectionLostToast()
                    }
                    scheduleReconnectOnMainThread()
                } else {
                    Log.e("ChatHistoryTask", "Max reconnect attempts reached — giving up")
                    showToastOnMain("Could not connect to chat. Please try again.")
                }
                return null
            }

            try {
                val contactID = "$contactJid@${Constants.XMPP_DOMAIN}"
                val currentID = "$currentJid@${Constants.XMPP_DOMAIN}"

                val jid = JidCreate.entityBareFrom(contactID)
                val mamManager = MamManager.getInstanceFor(ChatConnection.mConnection)

                val mamQueryArgs = MamManager.MamQueryArgs.builder()
                    .limitResultsToJid(jid)
                    .setResultPageSizeTo(50)
                    .queryLastPage()
                    .build()

                val mamQuery = mamManager.queryArchive(mamQueryArgs)
                val forwardedList = mamQuery.page.forwarded

                if (forwardedList.isEmpty()) {
                    Log.d("ChatHistoryTask", "No messages in archive")
                    return ""
                }

                message_list.clear()
                time_list.clear()

                val outputFormat = "MMM dd, yyyy hh:mm a"
                val sdf = SimpleDateFormat(outputFormat, Locale.US)

                for (forwarded in forwardedList) {
                    val msg = forwarded.forwardedStanza
                    val stamp = forwarded.delayInformation.stamp

                    val body = msg.body
                    if (body.isNullOrEmpty()) continue

                    val from = if (msg.from != null) msg.from.toString().split("/")[0] else ""
                    val timeStamp = sdf.format(stamp)

                    val timeEntry = MessageDo().apply {
                        createdAt = timeStamp
                    }
                    time_list.add(timeEntry)

                    var stanzaId: String? = null
                    val sid = msg.getExtension(StanzaIdElement::class.java)
                    if (sid != null) {
                        stanzaId = sid.id
                    }
                    if (stanzaId == null) {
                        stanzaId = msg.stanzaId
                    }

                    val chatMessage = MessageDo().apply {
                        message = body
                        iscurrentchat = false
                        createdAt = timeStamp
                        this.stanzaId = stanzaId
                    }

                    val user = User()
                    if (from.equals(currentID, ignoreCase = true)) {
                        chatMessage.viewType = Constants.chat_SENT
                        user.nickname = Constants.NAME
                    } else {
                        chatMessage.viewType = Constants.chat_RECEIVE
                        user.nickname = contact_name
                    }
                    chatMessage.sender = user
                    message_list.add(chatMessage)

                    if (!stanzaId.isNullOrEmpty()) {
                        lastSeenMessageId = stanzaId
                    }

                    Log.d(
                        "ChatHistoryTask",
                        "[${chatMessage.viewType}] ${body.substring(0, Math.min(30, body.length))} @ $timeStamp"
                    )
                }

                dismissDialogOnMainThread()
            } catch (e: XmppStringprepException) {
                Log.e("ChatHistoryTask", "XMPP error: ${e.message}")
                handleRetry()
                return null
            } catch (e: XMPPException.XMPPErrorException) {
                Log.e("ChatHistoryTask", "XMPP error: ${e.message}")
                handleRetry()
                return null
            } catch (e: SmackException.NotConnectedException) {
                Log.e("ChatHistoryTask", "XMPP error: ${e.message}")
                handleRetry()
                return null
            } catch (e: SmackException.NoResponseException) {
                Log.e("ChatHistoryTask", "XMPP error: ${e.message}")
                handleRetry()
                return null
            } catch (e: SmackException.NotLoggedInException) {
                Log.e("ChatHistoryTask", "XMPP error: ${e.message}")
                handleRetry()
                return null
            } catch (e: InterruptedException) {
                Log.e("ChatHistoryTask", "XMPP error: ${e.message}")
                handleRetry()
                return null
            } catch (e: Exception) {
                Log.e("ChatHistoryTask", "Unexpected error: ${e.message}")
                return null
            }

            return ""
        }

        private fun handleRetry() {
            if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                reconnectAttempts++
                if (reconnectAttempts >= 2) {
                    showConnectionLostToast()
                }
                scheduleReconnectOnMainThread()
            } else {
                Log.e("ChatHistoryTask", "Max reconnect attempts reached — giving up")
                showToastOnMain("Could not connect to chat. Please try again.")
            }
        }

        override fun onPostExecute(response: String?) {
            progress_dialog?.let {
                if (it.isShowing) AndroidUtils.dismiss_dialog(it)
            }

            isReconnecting = false
            hasShownConnectionToast = false

            if (response != null) {
                load_chat_history("")
            }
        }
    }

    private fun dismissDialogOnMainThread() {
        if (activity == null || isDetached) return
        activity?.runOnUiThread {
            progress_dialog?.let {
                if (it.isShowing) AndroidUtils.dismiss_dialog(it)
            }
        }
    }

    private fun showConnectionLostToast() {
        if (hasShownConnectionToast) return
        hasShownConnectionToast = true
        showToastOnMain(getString(R.string.chat_cnt_lost))
    }

    private fun showToastOnMain(msg: String) {
        if (activity == null || isDetached) return
        activity?.runOnUiThread {
            Toast.makeText(activity, msg, Toast.LENGTH_LONG).show()
        }
    }

    private fun scheduleReconnectOnMainThread() {
        if (activity == null || isDetached) return

        if (isReconnecting) {
            Log.d("ChatReconnect", "Already reconnecting — skipping duplicate.")
            return
        }

        isReconnecting = true
        activity?.runOnUiThread { updateChatConnection() }
    }

    fun updateChatConnection() {
        val act = activity ?: return
        if (isDetached) return

        val prefs = PreferenceManager.getDefaultSharedPreferences(act)
        var uid = Constants.UID
        if (!Constants.ROLE.equals("admin", ignoreCase = true)) {
            uid = uid + "_" + Constants.USER_ID
        }
        prefs.edit()
            .putString("xmpp_jid", uid)
            .putString("xmpp_password", Constants.TOKEN)
            .putBoolean("xmpp_logged_in", true)
            .apply()

        if (mConnection == null) {
            mConnection = ChatConnection(act)
        }
        if (chatConnectionService == null) {
            chatConnectionService = ChatConnectionService()
        }

        JsonTask().execute(Constants.base_URL + "user/create/")

        try {
            val conn = ChatConnection.mConnection
            if (conn != null && !conn.isConnected) {
                object : AsyncTask<Void, Void, Boolean>() {
                    override fun doInBackground(vararg voids: Void?): Boolean {
                        return try {
                            ChatConnection.mConnection?.connect()
                            ChatConnection.mConnection?.login()
                            true
                        } catch (e: Exception) {
                            Log.e("ChatConnection", "Reconnect failed: " + e.message)
                            false
                        }
                    }

                    override fun onPostExecute(success: Boolean) {
                        isReconnecting = false
                        hasShownConnectionToast = false

                        if (success && isAdded && !isDetached && activity != null) {
                            Log.d("ChatConnection", "Reconnect OK — retrying ChatHistoryTask")
                            ChatHistoryTask().execute()
                        } else {
                            Log.e("ChatConnection", "Reconnect failed — not retrying")
                        }
                    }
                }.execute()
            } else {
                isReconnecting = false
                hasShownConnectionToast = false
            }
        } catch (e: Exception) {
            Log.e("ChatConnection", "Reconnect setup failed: " + e.message)
            isReconnecting = false
            hasShownConnectionToast = false
        }
    }

    inner class ChatUnreadCountUpdateTask(currentJID: String, JID: String) : AsyncTask<String, String, String>() {
        private var url = ""

        init {
            this.url = "https://${Constants.XMPP_DOMAIN}/unread-timestamp/$currentJID${File.separator}$JID"
            Log.d("GET UNREAD COUNT", this.url)
        }

        override fun doInBackground(vararg strings: String?): String {
            return requestUnreadCount(url)
        }

        override fun onPostExecute(response: String?) {}
    }

    private fun requestUnreadCount(url: String): String {
        var data = ""
        var conn: HttpURLConnection? = null
        try {
            conn = URL(url).openConnection() as HttpURLConnection
            conn.setRequestProperty("Authorization", "Bearer ${Constants.TOKEN}")
            conn.requestMethod = "GET"
            val code = conn.responseCode
            Log.d("REQUEST_COUNT", code.toString())
            if (code == 200) {
                val `in` = conn.inputStream
                val isr = InputStreamReader(`in`)
                var ch: Int
                val sb = StringBuilder()
                while (isr.read().also { ch = it } != -1) sb.append(ch.toChar())
                data = sb.toString()
            }
        } catch (e: Exception) {
            AndroidUtils.logMsg(e.message)
        } finally {
            conn?.disconnect()
        }
        return data
    }

    private inner class JsonTask : AsyncTask<String, String, String>() {
        override fun doInBackground(vararg params: String?): String {
            try {
                val conn = mConnection
                if (conn == null) {
                    Log.e("JsonTask", "mConnection is null — skipping XMPP login")
                    return ""
                }
                conn.connect()
            } catch (e: Exception) {
                Log.d("Chat Error", "Something went wrong while connecting: ${e.message}")
                e.fillInStackTrace()
                chatConnectionService?.stopSelf()
            }
            return ""
        }
    }

    private fun isMessageExist(chatMessage: MessageDo): Boolean {
        for (message in message_list) {
            if (message.message != null &&
                chatMessage.message != null &&
                message.message == chatMessage.message &&
                message.viewType != null &&
                message.viewType == chatMessage.viewType &&
                message.sender != null &&
                chatMessage.sender != null &&
                Objects.equals(message.sender?.nickname, chatMessage.sender?.nickname) &&
                Objects.equals(message.createdAt, chatMessage.createdAt)
            ) {
                return true
            }
        }
        return false
    }

    companion object {
        private var mConnection: ChatConnection? = null
        private var chatConnectionService: ChatConnectionService? = null
        private const val COMPOSING_THROTTLE_MS = 3000L
    }
}
