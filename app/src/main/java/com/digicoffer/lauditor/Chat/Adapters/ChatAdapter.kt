package com.digicoffer.lauditor.Chat.Adapters

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.Chat.Model.ClientRelationshipsDo
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.CommonFiles.ChatService.ChatConnectionService
import com.digicoffer.lauditor.CommonFiles.ChatService.ChatCountApi
import com.digicoffer.lauditor.CommonFiles.ChatService.ConversationMetaApi
import com.digicoffer.lauditor.CommonFiles.ChatService.TypingStateManager
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList
import java.util.Date
import java.util.HashMap

class ChatAdapter(
    input: ArrayList<ClientRelationshipsDo>,
    private val listener: EventListener,
    private val context: Context,
    private val activity: FragmentActivity
) : RecyclerView.Adapter<ChatAdapter.MyViewHolder>(), Filterable, AsyncTaskCompleteListener {

    private val clientMap = HashMap<String, ClientRelationshipsDo>()
    private var list = ArrayList<ClientRelationshipsDo>()
    private var filteredList = ArrayList<ClientRelationshipsDo>()
    private val firmMap = HashMap<String, String>()
    private var progressDialog: Dialog? = null
    private var currentFirm: ClientRelationshipsDo? = null

    interface EventListener {
        @Throws(JSONException::class)
        fun view_users(uid: String?, name: String?, clientType: String?)
    }

    private val typingChangeListener = object : TypingStateManager.TypingChangeListener {
        override fun onTypingChanged(guid: String, isTyping: Boolean) {
            for (i in 0 until filteredList.size) {
                if (guid == filteredList[i].guid) {
                    notifyItemChanged(i)
                    break
                }
            }
        }
    }

    private var newMessageReceiver: BroadcastReceiver? = null

    init {
        TypingStateManager.addListener(typingChangeListener)
        registerNewMessageReceiver()

        for (row in input) {
            if ("entity".equals(row.clientType, ignoreCase = true)) {
                currentFirm = row
                callUsersApi(row)
                continue
            }
            if (!clientMap.containsKey(row.guid)) {
                row.guid?.let { clientMap.put(it, row) }
                list.add(row)
            }
        }
        filteredList = list
        refreshConversationMeta()
    }

    override fun onClick(view: View) {}

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        progressDialog?.let { AndroidUtils.dismiss_dialog(it) }

        if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                val result = JSONObject(httpResult.responseContent)
                if (result.getBoolean("error")) return

                val users = result.getJSONObject("data").getJSONArray("users")
                val firmName = firmMap[httpResult.requestType]

                var addedAny = false
                for (i in 0 until users.length()) {
                    val obj = users.getJSONObject(i)
                    val guid = obj.getString("guid")
                    if (!clientMap.containsKey(guid)) {
                        val user = ClientRelationshipsDo().apply {
                            this.guid = guid
                            this.name = obj.getString("name")
                            this.firmName = firmName
                            this.clientType = "consumer"
                        }
                        clientMap[guid] = user
                        list.add(user)
                        addedAny = true
                    }
                }

                filteredList = list

                if (addedAny) {
                    refreshConversationMeta()
                } else {
                    sortChats()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun callUsersApi(firm: ClientRelationshipsDo) {
        progressDialog = AndroidUtils.get_progress(activity)
        firm.rel_id?.let { relId ->
            firm.name?.let { name ->
                firmMap[relId] = name
            }
            try {
                WebServiceHelper.callHttpWebService(
                    this,
                    context,
                    WebServiceHelper.RestMethodType.GET,
                    "relationship/$relId/users/notify",
                    relId,
                    JSONObject().toString()
                )
            } catch (e: Exception) {
                progressDialog?.let { AndroidUtils.dismiss_dialog(it) }
            }
        }
    }

    private fun refreshConversationMeta() {
        val chatJids = ArrayList<String>()
        for (row in list) {
            chatJids.add(row.guid + Constants.VitacapeExtention)
        }

        ConversationMetaApi.fetch(context, chatJids, object : ConversationMetaApi.MetaCallback {
            override fun onMetaReady(metaMap: Map<String, ConversationMetaApi.ConversationMeta>) {
                for (row in list) {
                    val meta = metaMap[row.guid] ?: continue
                    row.lastMessage = meta.lastMessage
                    row.lastMessageTime = AndroidUtils.getChatTimeText(Date(meta.lastMessageTimestamp))
                    row.lastMessageTimestamp = meta.lastMessageTimestamp
                    row.unread_count = meta.unreadCount
                }
                sortChats()
            }
        })
    }

    private fun registerNewMessageReceiver() {
        newMessageReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent == null) return
                val action = intent.action
                Log.d("ChatAdapterDebug", "onReceive fired, action=$action")
                if (action != ChatConnectionService.NEW_MESSAGE) return

                val fromJidRaw = intent.getStringExtra(ChatConnectionService.BUNDLE_FROM_JID)
                val body = intent.getStringExtra(ChatConnectionService.BUNDLE_MESSAGE_BODY)
                if (fromJidRaw == null) return

                val fromGuid = if (fromJidRaw.contains("@")) {
                    fromJidRaw.split("@")[0]
                } else {
                    fromJidRaw
                }

                Log.d("ChatAdapterDebug", "fromGuid=$fromGuid body=$body listSize=${list.size}")

                var matched = false
                for (row in list) {
                    if (fromGuid == row.guid) {
                        matched = true
                        row.lastMessage = body
                        row.lastMessageTime = AndroidUtils.getChatTimeText(Date())
                        row.lastMessageTimestamp = System.currentTimeMillis()

                        val currentUnread = parseUnread(row.unread_count)
                        row.unread_count = (currentUnread + 1).toString()

                        sortChats()
                        break
                    }
                }
                if (!matched) {
                    Log.d("ChatAdapterDebug", "No row matched fromGuid=$fromGuid")
                }
            }
        }

        val filter = IntentFilter(ChatConnectionService.NEW_MESSAGE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(newMessageReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            ContextCompat.registerReceiver(context, newMessageReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        }
        Log.d("ChatAdapterDebug", "newMessageReceiver registered, context=$context")
    }

    fun unregister() {
        newMessageReceiver?.let {
            try {
                context.unregisterReceiver(it)
            } catch (ignored: Exception) {
            }
            newMessageReceiver = null
        }
        TypingStateManager.removeListener(typingChangeListener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.client_list, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val row = filteredList[position]

        val displayName = if (row.firmName == null) {
            row.name ?: ""
        } else {
            "${row.name} - ${row.firmName}"
        }

        holder.tv_name.text = displayName
        holder.person_icon.text = if (displayName.isNotEmpty()) displayName.substring(0, 1).uppercase() else ""
        holder.dot_icon.visibility = View.GONE

        val unread = row.unread_count
        if (unread != null && unread != "0") {
            holder.count_icon.visibility = View.VISIBLE
            holder.count_icon.text = unread
        } else {
            holder.count_icon.visibility = View.GONE
        }

        holder.ll_client_card.setOnClickListener {
            try {
                listener.view_users(row.guid, displayName, row.source)
                row.unread_count = "0"
                holder.count_icon.visibility = View.GONE
                ChatCountApi(context).callResetUnreadCount(row.guid ?: "")
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

        if (row.lastMessage != null && !row.lastMessage!!.isEmpty()) {
            holder.ll_last_msg.visibility = View.VISIBLE

            if (TypingStateManager.isTyping(row.guid)) {
                holder.tv_last_msg.text = "typing..."
                holder.tv_last_msg.setTextColor(
                    holder.tv_last_msg.context.resources.getColor(R.color.blue)
                )
            } else {
                holder.tv_last_msg.text = row.lastMessage
                holder.tv_last_msg.setTextColor(
                    holder.tv_last_msg.context.resources.getColor(R.color.material_dynamic_neutral50)
                )
            }
            holder.tv_last_msg_time.text = row.lastMessageTime
        } else {
            holder.ll_last_msg.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(cs: CharSequence?): FilterResults {
                val q = cs.toString().lowercase()
                val temp = ArrayList<ClientRelationshipsDo>()

                for (row in list) {
                    val name = if (row.firmName == null) {
                        row.name ?: ""
                    } else {
                        "${row.name} - ${row.firmName}"
                    }

                    if (name.lowercase().contains(q)) {
                        temp.add(row)
                    }
                }
                val fr = FilterResults()
                fr.values = temp
                return fr
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(cs: CharSequence?, fr: FilterResults?) {
                filteredList = fr?.values as ArrayList<ClientRelationshipsDo>
                sortChats()
            }
        }
    }

    private fun sortChats() {
        filteredList.sortWith { c1, c2 ->
            val t1 = c1.lastMessageTimestamp
            val t2 = c2.lastMessageTimestamp
            if (t1 != t2) {
                t2.compareTo(t1)
            } else {
                val u1 = parseUnread(c1.unread_count)
                val u2 = parseUnread(c2.unread_count)
                if (u1 != u2) {
                    u2.compareTo(u1)
                } else {
                    val n1 = c1.name ?: ""
                    val n2 = c2.name ?: ""
                    n1.compareTo(n2, ignoreCase = true)
                }
            }
        }
        notifyDataSetChanged()
    }

    private fun parseUnread(unread: String?): Int {
        return try {
            if (unread.isNullOrEmpty()) 0 else unread.toInt()
        } catch (e: Exception) {
            0
        }
    }

    class MyViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tv_name: TextView = v.findViewById(R.id.tv_name)
        val person_icon: TextView = v.findViewById(R.id.person_icon)
        val count_icon: TextView = v.findViewById(R.id.count_icon)
        val tv_last_msg: TextView = v.findViewById(R.id.tv_last_msg)
        val tv_last_msg_time: TextView = v.findViewById(R.id.tv_last_msg_time)
        val plus_icon: ImageView = v.findViewById(R.id.plus_icon)
        val dot_icon: ImageView = v.findViewById(R.id.dot_icon)
        val ll_client_card: LinearLayout = v.findViewById(R.id.ll_client_card)
        val ll_last_msg: RelativeLayout = v.findViewById(R.id.ll_last_msg)
        val ll_users: LinearLayoutCompat = v.findViewById(R.id.ll_users)
    }
}
