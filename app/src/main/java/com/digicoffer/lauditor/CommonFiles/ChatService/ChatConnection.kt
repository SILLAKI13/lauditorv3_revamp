package com.digicoffer.lauditor.CommonFiles.ChatService

import android.annotation.SuppressLint
import android.app.Notification
import android.app.Notification.DEFAULT_SOUND
import android.app.Notification.DEFAULT_VIBRATE
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Context.RECEIVER_EXPORTED
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.Dashboard.DahboardModels.UnreadCountModel
import com.digicoffer.lauditor.LoginActivity.ViewModels.ConfirmationLogin
import com.digicoffer.lauditor.MainActivity
import com.digicoffer.lauditor.R
import org.jivesoftware.smack.ConnectionConfiguration
import org.jivesoftware.smack.ConnectionListener
import org.jivesoftware.smack.ReconnectionManager
import org.jivesoftware.smack.SmackException
import org.jivesoftware.smack.StanzaListener
import org.jivesoftware.smack.XMPPConnection
import org.jivesoftware.smack.XMPPException
import org.jivesoftware.smack.chat2.Chat
import org.jivesoftware.smack.chat2.ChatManager
import org.jivesoftware.smack.chat2.IncomingChatMessageListener
import org.jivesoftware.smack.packet.Message
import org.jivesoftware.smack.packet.Presence
import org.jivesoftware.smack.packet.Stanza
import org.jivesoftware.smack.tcp.XMPPTCPConnection
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration
import org.jivesoftware.smackx.sid.element.StanzaIdElement
import org.jxmpp.jid.EntityBareJid
import org.jxmpp.jid.impl.JidCreate
import org.jxmpp.stringprep.XmppStringprepException
import java.io.IOException
import java.security.SecureRandom
import java.util.ArrayList
import java.util.UUID
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLEngine

class ChatConnection : ConnectionListener {

    var context: Context
    private val mUsername: String
    private val mPassword: String?
    private val mServiceName: String
    var messageListAdapter: RecyclerView? = null
    private val unreadcountlist = ArrayList<UnreadCountModel>()

    enum class LoggedInState {
        LOGGED_IN, LOGGED_OUT
    }

    enum class ConnectionState {
        CONNECTED, AUTHENTICATED, CONNECTING, DISCONNECTING, DISCONNECTED
    }

    constructor(context: Context) {
        this.context = context
        Log.d(TAG, "RoosterConnection Constructor called.")
        mApplicationContext = context.applicationContext
        val jid = PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
            .getString("xmpp_jid", null)
        mPassword = PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
            .getString("xmpp_password", null)

        if (jid != null) {
            mUsername = jid + "@" + (Constants.XMPP_DOMAIN ?: "")
            mServiceName = Constants.XMPP_DOMAIN ?: ""
        } else {
            mUsername = ""
            mServiceName = ""
        }
    }

    fun createContext(): SSLContext? {
        return try {
            val sslContext = SSLContext.getInstance("TLSv1.2")
            sslContext.init(null, null, SecureRandom())
            sslContext
        } catch (e: Exception) {
            AndroidUtils.logMsg(e.message)
            null
        }
    }

    @Throws(IOException::class, XMPPException::class, SmackException::class)
    fun connect() {
        Log.d(TAG, "Connecting to server $mServiceName as $mUsername")

        if (mUsername.isEmpty() || mPassword.isNullOrEmpty()) {
            Log.e(
                TAG, "connect() aborted — xmpp_jid or xmpp_password is null/empty in SharedPreferences. " +
                        "Ensure saveXmppPreferences() in LoginActivity has run before connect() is called."
            )
            ChatConnectionService.sConnectionState = ConnectionState.DISCONNECTED
            return
        }

        try {
            val sslContext = createContext()
            val conf = XMPPTCPConnectionConfiguration.builder()
                .setXmppDomain(Constants.XMPP_DOMAIN)
                .setHost(Constants.XMPP_DOMAIN)
                .setSecurityMode(ConnectionConfiguration.SecurityMode.required)
                .setCustomSSLContext(sslContext)
                .setSendPresence(true)
                .setCompressionEnabled(true)
                .build()

            setupUiThreadBroadCastMessageReceiver()

            if (mConnection != null && (mConnection!!.isConnected || mConnection!!.isAuthenticated)) {
                Log.d(TAG, "connect() — mConnection already connected/authenticated, skipping reconnect.")
                ChatConnectionService.sConnectionState = ConnectionState.CONNECTED
                return
            }

            if (mConnection != null && !mConnection!!.isConnected) {
                Log.d(TAG, "connect() — mConnection exists but is disconnected. Resetting for fresh connect.")
                try {
                    mConnection!!.disconnect()
                } catch (ignored: Exception) {
                }
                mConnection = null
            }

            if (mConnection == null) {
                mConnection = XMPPTCPConnection(conf)
                mConnection!!.addConnectionListener(this)
                Log.d(TAG, "connect() — new XMPPTCPConnection created.")
            }

            ChatConnectionService.sConnectionState = ConnectionState.CONNECTING
            Log.d(TAG, "connect() — state set to CONNECTING")

            mConnection!!.connect()
            mConnection!!.login(mUsername, mPassword)

            Log.d(TAG, "connect() — login succeeded for $mUsername")

            val presence = Presence(Presence.Type.available)
            presence.status = "online"
            presence.mode = Presence.Mode.available

            PresenceManager.registerOn(mConnection!!)
            PresenceManager.ensureSubscriptions(mConnection, Constants.totalchatclientlist)

            val reconnectionManager = ReconnectionManager.getInstanceFor(mConnection)
            ReconnectionManager.setEnabledPerDefault(true)
            reconnectionManager.enableAutomaticReconnection()

            if (incomingListener != null) {
                ChatManager.getInstanceFor(mConnection).removeIncomingListener(incomingListener)
            }

            incomingListener = IncomingChatMessageListener { messageFrom, message, chat ->
                val from = message.from.toString()
                val contactJid = if (from.contains("/")) from.split("/")[0] else from

                Constants.fromjid = contactJid

                val slashSplit = from.split("/")
                val bareJid = slashSplit[0]
                val uniqueId = if (slashSplit.size > 1) slashSplit[1] else ""
                val username = bareJid.split("@")[0]

                if (TypingStateManager.handleIncomingMessage(message, username)) {
                    return@IncomingChatMessageListener
                }

                val finalId = if (uniqueId.isEmpty()) username else "${username}_$uniqueId"
                Constants.updatedFromIid = finalId

                var messageId: String? = null
                val sid = message.getExtension<StanzaIdElement>(StanzaIdElement::class.java)
                if (sid != null) {
                    messageId = sid.id
                }
                if (messageId == null) {
                    messageId = message.stanzaId
                }
                if (messageId == null) {
                    messageId = "local-" + System.currentTimeMillis()
                }

                Log.e("MESSAGE_ID", messageId)
                Constants.message_id = messageId
                Log.e("XMPP_MESSAGE_ID", "Received ID: " + Constants.message_id)

                val intent = Intent(ChatConnectionService.NEW_MESSAGE)
                intent.setPackage(mApplicationContext!!.packageName)
                intent.putExtra("message_id", Constants.message_id)
                intent.putExtra(ChatConnectionService.BUNDLE_FROM_JID, contactJid)
                intent.putExtra(ChatConnectionService.BUNDLE_MESSAGE_BODY, message.body)
                intent.putExtra(ChatConnectionService.BUNDLE_MESSAGE_SUBJECT, message.subject)
                mApplicationContext!!.sendBroadcast(intent)

                if (Constants.fromjid != Constants.Chat_id) {
                    ChatCountApi(context).callupdatecount()
                }

                show_notification(contactJid.split("@")[0], message.body, message.subject)
            }

            ChatManager.getInstanceFor(mConnection).addIncomingListener(incomingListener)

            mConnection!!.addAsyncStanzaListener({ stanza ->
                val msg = stanza as Message
                if (msg.from == null) return@addAsyncStanzaListener

                val from = msg.from.toString()
                val bareJid = if (from.contains("/")) from.split("/")[0] else from
                val guid = if (bareJid.contains("@")) bareJid.split("@")[0] else bareJid

                TypingStateManager.handleIncomingMessage(msg, guid)
            }, org.jivesoftware.smack.filter.StanzaTypeFilter(Message::class.java))

        } catch (e: Exception) {
            Log.e(TAG, "connect() FAILED — " + e.javaClass.simpleName + ": " + e.message, e)
            ChatConnectionService.sConnectionState = ConnectionState.DISCONNECTED
            Log.d(TAG, "connect() — state set to DISCONNECTED after exception")

            if (e is IOException) throw e
            if (e is XMPPException) throw e
            if (e is SmackException) throw e
            throw SmackException.NotConnectedException(e.message)
        }
    }

    fun disconnect() {
        Log.d(TAG, "Disconnecting from server $mServiceName")

        val prefs = PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
        prefs.edit().putBoolean("xmpp_logged_in", false).apply()

        try {
            if (mConnection != null) {
                if (incomingListener != null) {
                    ChatManager.getInstanceFor(mConnection).removeIncomingListener(incomingListener)
                    incomingListener = null
                }
                mConnection!!.disconnect()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting: " + e.message, e)
        }

        PresenceManager.clear()
        TypingStateManager.clear()

        mConnection = null

        if (uiThreadMessageReceiver != null) {
            try {
                mApplicationContext!!.unregisterReceiver(uiThreadMessageReceiver)
            } catch (ignored: Exception) {
            }
            uiThreadMessageReceiver = null
        }
    }

    override fun connected(connection: XMPPConnection?) {
        ChatConnectionService.sConnectionState = ConnectionState.CONNECTED
        Log.d(TAG, "connected() — TCP connected. State = CONNECTED")
    }

    override fun authenticated(connection: XMPPConnection?, resumed: Boolean) {
        ChatConnectionService.sConnectionState = ConnectionState.CONNECTED
        Log.d(TAG, "authenticated() — login complete. State = CONNECTED (resumed=$resumed)")
    }

    override fun connectionClosed() {
        ChatConnectionService.sConnectionState = ConnectionState.DISCONNECTED
        Log.d(TAG, "connectionClosed() — State = DISCONNECTED")
    }

    override fun connectionClosedOnError(e: java.lang.Exception?) {
        ChatConnectionService.sConnectionState = ConnectionState.DISCONNECTED
        Log.d(TAG, "connectionClosedOnError() — State = DISCONNECTED: " + e.toString())
    }

    fun isConnected(): Boolean {
        return mConnection != null && mConnection!!.isConnected
    }

    fun isAuthenticated(): Boolean {
        return mConnection != null && mConnection!!.isAuthenticated
    }

    companion object {
        private const val TAG = "RoosterConnection"

        @JvmField
        var instance: ChatConnection? = null

        @JvmField
        var mApplicationContext: Context? = null

        @JvmField
        var mConnection: XMPPTCPConnection? = null

        @JvmField
        var uiThreadMessageReceiver: BroadcastReceiver? = null

        @JvmField
        var incomingListener: IncomingChatMessageListener? = null

        @JvmField
        var mManager: NotificationManager? = null

        @JvmStatic
        @Synchronized
        fun getInstance(context: Context): ChatConnection {
            if (instance == null) {
                instance = ChatConnection(context)
            }
            return instance!!
        }

        @JvmStatic
        @Synchronized
        fun resetInstance() {
            if (instance != null) {
                instance!!.disconnect()
                instance = null
            }
        }

        @JvmStatic
        fun loginUser(context: Context, username: String?, password: String?) {
            if (instance == null) {
                instance = ChatConnection(context)
            }

            Thread {
                try {
                    val sslContext = instance!!.createContext()
                    val conf = XMPPTCPConnectionConfiguration.builder()
                        .setXmppDomain(Constants.XMPP_DOMAIN)
                        .setHost(Constants.XMPP_DOMAIN)
                        .setSecurityMode(ConnectionConfiguration.SecurityMode.required)
                        .setCustomSSLContext(sslContext)
                        .setSendPresence(true)
                        .setCompressionEnabled(true)
                        .build()

                    if (mConnection != null && !mConnection!!.isConnected) {
                        Log.d(TAG, "loginUser() — mConnection exists but disconnected. Resetting.")
                        try {
                            mConnection!!.disconnect()
                        } catch (ignored: Exception) {
                        }
                        mConnection = null
                    }

                    if (mConnection == null) {
                        mConnection = XMPPTCPConnection(conf)
                        mConnection!!.addConnectionListener(instance)
                    }

                    if (mConnection!!.isConnected) mConnection!!.disconnect()

                    ChatConnectionService.sConnectionState = ConnectionState.CONNECTING

                    mConnection!!.connect()
                    mConnection!!.login(username, password)

                    Log.d(TAG, "loginUser() — login succeeded for $username")

                    setupUiThreadBroadCastMessageReceiver()
                    val presence = Presence(Presence.Type.available)
                    presence.status = "online"
                    presence.mode = Presence.Mode.available

                    PresenceManager.registerOn(mConnection!!)
                    PresenceManager.ensureSubscriptions(mConnection, Constants.totalchatclientlist)

                    val reconnectionManager = ReconnectionManager.getInstanceFor(mConnection)
                    ReconnectionManager.setEnabledPerDefault(true)
                    reconnectionManager.enableAutomaticReconnection()

                    if (incomingListener != null) {
                        ChatManager.getInstanceFor(mConnection).removeIncomingListener(incomingListener)
                    }

                    incomingListener = IncomingChatMessageListener { messageFrom, message, chat ->
                        val from = message.from.toString()
                        val contactJid = if (from.contains("/")) from.split("/")[0] else from
                        Constants.fromjid = contactJid

                        val slashSplit = from.split("/")
                        val bareJid = slashSplit[0]
                        val uniqueId = if (slashSplit.size > 1) slashSplit[1] else ""
                        val username2 = bareJid.split("@")[0]

                        if (TypingStateManager.handleIncomingMessage(message, username2)) {
                            return@IncomingChatMessageListener
                        }

                        val finalId = if (uniqueId.isEmpty()) username2 else "${username2}_$uniqueId"
                        Constants.updatedFromIid = finalId

                        var messageId: String? = null
                        val sid = message.getExtension<StanzaIdElement>(StanzaIdElement::class.java)
                        if (sid != null) {
                            messageId = sid.id
                        }
                        if (messageId == null) {
                            messageId = message.stanzaId
                        }
                        if (messageId == null) {
                            messageId = "local-" + System.currentTimeMillis()
                        }

                        Log.e("MESSAGE_ID", messageId)
                        Constants.message_id = messageId
                        show_notification(contactJid.split("@")[0], message.body, message.subject)

                        val intent = Intent(ChatConnectionService.NEW_MESSAGE)
                        intent.setPackage(mApplicationContext!!.packageName)
                        intent.putExtra(ChatConnectionService.BUNDLE_FROM_JID, contactJid)
                        intent.putExtra(ChatConnectionService.BUNDLE_MESSAGE_BODY, message.body)
                        intent.putExtra(ChatConnectionService.BUNDLE_MESSAGE_SUBJECT, message.subject)
                        mApplicationContext!!.sendBroadcast(intent)

                        if (Constants.fromjid != Constants.Chat_id) {
                            ChatCountApi(context).callupdatecount()
                        }
                    }

                    ChatManager.getInstanceFor(mConnection).addIncomingListener(incomingListener)

                    mConnection!!.addAsyncStanzaListener({ stanza ->
                        val msg = stanza as Message
                        if (msg.from == null) return@addAsyncStanzaListener

                        val from2 = msg.from.toString()
                        val bareJid2 = if (from2.contains("/")) from2.split("/")[0] else from2
                        val guid2 = if (bareJid2.contains("@")) bareJid2.split("@")[0] else bareJid2

                        TypingStateManager.handleIncomingMessage(msg, guid2)
                    }, org.jivesoftware.smack.filter.StanzaTypeFilter(Message::class.java))

                } catch (e: Exception) {
                    Log.e(
                        TAG,
                        "loginUser() FAILED for " + username + " — " + e.javaClass.simpleName + ": " + e.message,
                        e
                    )
                    ChatConnectionService.sConnectionState = ConnectionState.DISCONNECTED
                }
            }.start()
        }

        @JvmStatic
        fun setupUiThreadBroadCastMessageReceiver() {
            if (uiThreadMessageReceiver != null) {
                try {
                    mApplicationContext!!.unregisterReceiver(uiThreadMessageReceiver)
                } catch (ignored: Exception) {
                }
                uiThreadMessageReceiver = null
            }

            uiThreadMessageReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (ChatConnectionService.SEND_MESSAGE == intent.action) {
                        sendMessage(
                            intent.getStringExtra(ChatConnectionService.BUNDLE_MESSAGE_BODY),
                            intent.getStringExtra(ChatConnectionService.BUNDLE_TO),
                            intent.getStringExtra(ChatConnectionService.BUNDLE_MESSAGE_SUBJECT)
                        )
                    }
                }
            }
            val filter = IntentFilter()
            filter.addAction(ChatConnectionService.SEND_MESSAGE)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mApplicationContext!!.registerReceiver(uiThreadMessageReceiver, filter, RECEIVER_EXPORTED)
            } else {
                ContextCompat.registerReceiver(
                    mApplicationContext!!,
                    uiThreadMessageReceiver!!,
                    filter,
                    ContextCompat.RECEIVER_NOT_EXPORTED
                )
            }
        }

        @JvmStatic
        private fun sendMessage(body: String?, toJid: String?, subject: String?) {
            if (toJid.isNullOrEmpty()) return
            try {
                val jid = JidCreate.entityBareFrom(toJid + "@" + (Constants.XMPP_DOMAIN ?: ""))
                val chat = ChatManager.getInstanceFor(mConnection).chatWith(jid)
                val message = Message(jid, Message.Type.chat)

                val stanzaId = UUID.randomUUID().toString()
                message.stanzaId = stanzaId
                message.body = body
                message.subject = subject

                message.addExtension(
                    org.jivesoftware.smack.packet.StandardExtensionElement
                        .builder("request", "urn:xmpp:receipts")
                        .build()
                )

                chat.send(message)
                Log.d(TAG, "sendMessage() — sent id=$stanzaId to=$toJid")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun sendDisplayedMarker(toJid: String?, lastMessageId: String?) {
            if (toJid.isNullOrEmpty() || lastMessageId.isNullOrEmpty()) {
                Log.d(TAG, "sendDisplayedMarker() — skipping, missing toJid or messageId")
                return
            }

            if (mConnection == null || !mConnection!!.isConnected || !mConnection!!.isAuthenticated) {
                Log.e(TAG, "sendDisplayedMarker() — not connected, cannot send read marker")
                return
            }

            try {
                val jid = JidCreate.entityBareFrom(toJid + "@" + (Constants.XMPP_DOMAIN ?: ""))
                val message = Message(jid, Message.Type.chat)
                message.addExtension(
                    org.jivesoftware.smack.packet.StandardExtensionElement
                        .builder("displayed", "urn:xmpp:chat-markers:0")
                        .addAttribute("id", lastMessageId)
                        .build()
                )

                mConnection!!.sendStanza(message)
                Log.d(TAG, "sendDisplayedMarker() — sent id=$lastMessageId to=$toJid")
            } catch (e: Exception) {
                Log.e(TAG, "sendDisplayedMarker() — failed: " + e.message)
            }
        }

        @JvmStatic
        fun sendChatState(toJid: String?, state: String?) {
            if (toJid.isNullOrEmpty() || state.isNullOrEmpty()) {
                return
            }

            if (mConnection == null || !mConnection!!.isConnected || !mConnection!!.isAuthenticated) {
                Log.d(TAG, "sendChatState() — not connected, skipping")
                return
            }

            try {
                val jid = JidCreate.entityBareFrom(toJid + "@" + (Constants.XMPP_DOMAIN ?: ""))
                val message = Message(jid, Message.Type.chat)
                message.addExtension(
                    org.jivesoftware.smack.packet.StandardExtensionElement
                        .builder(state, "http://jabber.org/protocol/chatstates")
                        .build()
                )

                mConnection!!.sendStanza(message)
                Log.d(TAG, "sendChatState() — sent $state to=$toJid")
            } catch (e: Exception) {
                Log.e(TAG, "sendChatState() — failed: " + e.message)
            }
        }

        @JvmStatic
        @SuppressLint("NotificationPermission")
        fun show_notification(contact: String?, message: String?, subject: String?) {
        }

        private fun getManager(): NotificationManager? {
            if (mManager == null) {
                mManager = mApplicationContext!!.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            }
            return mManager
        }

        @JvmStatic
        fun getAndroidChannelNotification(
            title: String,
            body: String,
            notification_type: String,
            notify_details: Array<String>
        ): Notification.Builder? {
            val penint: PendingIntent
            var pendingIntentConfirm: PendingIntent? = null
            var title_notification = body
            var body_notification = ""
            if (body.contains("\n")) {
                val msg = body.split("\n").toTypedArray()
                title_notification = msg[0]
                for (i in 1 until msg.size) {
                    body_notification += msg[i]
                }
            }
            if (notification_type == "B" || notification_type == "C" || notification_type == "P") {
                val intent = Intent(mApplicationContext, MainActivity::class.java)
                intent.putExtra("fragment", "Relationship")
                intent.putExtra("TYPE", notification_type)
                penint = PendingIntent.getActivity(
                    mApplicationContext,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            } else if (notification_type == "chat") {
                val intent = Intent(mApplicationContext, MainActivity::class.java)
                intent.putExtra("fragment", "CHAT")
                intent.putExtra("CONTACT_JID", notify_details[0])
                intent.putExtra("CONTACT_NAME", notify_details[1])
                body_notification = title_notification
                title_notification = notify_details[1]
                penint = PendingIntent.getActivity(
                    mApplicationContext,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                val intent = Intent(mApplicationContext, ConfirmationLogin::class.java)
                penint = PendingIntent.getActivity(
                    mApplicationContext,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val intentConfirm = Intent(mApplicationContext, ConfirmationLogin::class.java)
                intentConfirm.action = "CONFIRM"
                intentConfirm.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                pendingIntentConfirm = PendingIntent.getActivity(
                    mApplicationContext,
                    0,
                    intentConfirm,
                    PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notification = Notification.Builder(
                    mApplicationContext!!.applicationContext, "com.digisec.digicoffer"
                )
                    .setContentTitle(title_notification)
                    .setContentText(body_notification)
                    .setSmallIcon(R.mipmap.trans_logo)
                    .setAutoCancel(true)
                    .setStyle(
                        Notification.BigTextStyle()
                            .bigText(body_notification)
                    )
                    .setContentIntent(penint)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)

                notification.setContentIntent(penint).notification
                if (Build.VERSION.SDK_INT >= 21) {
                    notification.setDefaults(DEFAULT_SOUND or DEFAULT_VIBRATE)
                    notification.setPriority(Notification.PRIORITY_HIGH)
                }
                return notification
            }
            return null
        }

        @JvmStatic
        fun getNotificationOldMobile(
            title: String?,
            message: String,
            notification_type: String,
            notify_details: Array<String>
        ): NotificationCompat.Builder {
            val penint: PendingIntent
            var pendingIntentConfirm: PendingIntent? = null
            var title_notification = message
            val body_notification = ""
            if (message.contains("\n")) {
                val msg = message.split("\n").toTypedArray()
                title_notification = msg[0]
                for (i in 1 until msg.size) {
                    title_notification += msg[i]
                }
            }
            if (notification_type == "B" || notification_type == "C" || notification_type == "P") {
                val intent = Intent(mApplicationContext, MainActivity::class.java)
                intent.putExtra("fragment", "Relationship")
                intent.putExtra("TYPE", notification_type)
                penint = PendingIntent.getActivity(
                    mApplicationContext,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            } else if (notification_type == "chat") {
                val intent = Intent(mApplicationContext, MainActivity::class.java)
                intent.putExtra("fragment", "CHAT")
                intent.putExtra("EXTRA_CONTACT_JID", notify_details[0])
                intent.putExtra("EXTRA_CONTACT_NAME", notify_details[1])
                intent.putExtra("EXTRA_CONTACT_TYPE", "relationship")
                penint = PendingIntent.getActivity(
                    mApplicationContext,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                val intent = Intent(mApplicationContext, ConfirmationLogin::class.java)
                penint = PendingIntent.getActivity(
                    mApplicationContext,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val intentConfirm = Intent(mApplicationContext, ConfirmationLogin::class.java)
                intentConfirm.action = "CONFIRM"
                intentConfirm.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                pendingIntentConfirm = PendingIntent.getActivity(
                    mApplicationContext,
                    0,
                    intentConfirm,
                    PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }

            val notification = NotificationCompat.Builder(mApplicationContext!!)
                .setContentTitle(title_notification)
                .setContentText(body_notification)
                .setSmallIcon(R.mipmap.trans_logo)
                .setContentIntent(penint)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(body_notification)
                )
            if (Build.VERSION.SDK_INT >= 21) {
                notification.setDefaults(DEFAULT_SOUND or DEFAULT_VIBRATE)
                notification.setPriority(NotificationCompat.PRIORITY_HIGH)
            }
            return notification
        }

        @JvmStatic
        fun getUserStatus(toJid: String?) {
            mConnection!!.fromMode = XMPPConnection.FromMode.valueOf(toJid ?: "")
            mConnection!!.fromMode
        }

        const val CHAT_STATE_COMPOSING = "composing"
        const val CHAT_STATE_PAUSED = "paused"
        const val CHAT_STATE_GONE = "gone"
        const val COMPOSING_THROTTLE_MS = 3000L
    }
}

class PresencePacketListener(private val context: Context) : StanzaListener {
    override fun processStanza(packet: Stanza) {
        val presence = packet as Presence
        val presenceType = presence.type
        // Do sth with presence
    }
}
