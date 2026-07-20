package com.digicoffer.lauditor.Chat.Adapters

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
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.Chat.Model.ChildDO
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.CommonFiles.ChatService.ChatConnectionService
import com.digicoffer.lauditor.CommonFiles.ChatService.ChatCountApi
import com.digicoffer.lauditor.CommonFiles.ChatService.ConversationMetaApi
import com.digicoffer.lauditor.CommonFiles.ChatService.TypingStateManager
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import java.util.ArrayList
import java.util.Date

class MembersAdapter(
    private val members: ArrayList<ChildDO>,
    private val context: Context,
    private val listener: OnMemberClick
) : RecyclerView.Adapter<MembersAdapter.MemberVH>(), Filterable {

    interface OnMemberClick {
        fun onMemberClick(member: ChildDO?)
    }

    private val filteredList = ArrayList<ChildDO>(members)

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
        refreshConversationMeta()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberVH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.client_list, parent, false)
        return MemberVH(v)
    }

    override fun onBindViewHolder(holder: MemberVH, position: Int) {
        val member = filteredList[position]

        val name = member.name ?: ""
        holder.tv_name.text = name
        holder.person_icon.text = if (name.isNotEmpty()) name.substring(0, 1).uppercase() else ""

        val unread = member.unread_count
        if (unread != null && unread != "0") {
            holder.count_icon.visibility = View.VISIBLE
            holder.count_icon.text = unread
        } else {
            holder.count_icon.visibility = View.GONE
        }

        holder.plus_icon.visibility = View.GONE
        holder.ll_users.visibility = View.GONE

        holder.ll_client_card.setOnClickListener {
            listener.onMemberClick(member)
            member.unread_count = "0"
            holder.count_icon.visibility = View.GONE
            ChatCountApi(context).callResetUnreadCount(member.guid ?: "")
        }

        val lastMsg = member.lastMessage
        if (lastMsg != null && lastMsg.isNotEmpty()) {
            holder.ll_last_msg.visibility = View.VISIBLE

            if (TypingStateManager.isTyping(member.guid)) {
                holder.tv_last_msg.text = "typing..."
                holder.tv_last_msg.setTextColor(
                    holder.tv_last_msg.context.resources.getColor(R.color.blue)
                )
            } else {
                holder.tv_last_msg.text = lastMsg
                holder.tv_last_msg.setTextColor(
                    holder.tv_last_msg.context.resources.getColor(R.color.material_dynamic_neutral50)
                )
            }
            holder.tv_last_msg_time.text = member.lastMessageTime
        } else {
            holder.ll_last_msg.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val search = charSequence.toString().trim().lowercase()
                val tempList = ArrayList<ChildDO>()

                if (search.isEmpty()) {
                    tempList.addAll(members)
                } else {
                    for (row in members) {
                        val name = AndroidUtils.isNull(row.name).lowercase()
                        val firm = AndroidUtils.isNull(row.FirmName).lowercase()

                        if (name.contains(search) || firm.contains(search)) {
                            tempList.add(row)
                        }
                    }
                }

                val results = FilterResults()
                results.values = tempList
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList.clear()
                if (results?.values != null) {
                    filteredList.addAll(results.values as ArrayList<ChildDO>)
                }
                sortMembers()
            }
        }
    }

    private fun refreshConversationMeta() {
        val chatJids = ArrayList<String>()
        for (member in members) {
            chatJids.add(member.guid + Constants.VitacapeExtention)
        }

        ConversationMetaApi.fetch(context, chatJids, object : ConversationMetaApi.MetaCallback {
            override fun onMetaReady(metaMap: Map<String, ConversationMetaApi.ConversationMeta>) {
                for (member in members) {
                    val meta = metaMap[member.guid] ?: continue
                    member.lastMessage = meta.lastMessage
                    member.lastMessageTime = AndroidUtils.getChatTimeText(Date(meta.lastMessageTimestamp))
                    member.lastMessageTimestamp = meta.lastMessageTimestamp
                    member.unread_count = meta.unreadCount
                }
                sortMembers()
            }
        })
    }

    private fun registerNewMessageReceiver() {
        newMessageReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent == null) return
                val action = intent.action
                Log.d("MembersAdapterDebug", "onReceive fired, action=$action")
                if (action != ChatConnectionService.NEW_MESSAGE) return

                val fromJidRaw = intent.getStringExtra(ChatConnectionService.BUNDLE_FROM_JID)
                val body = intent.getStringExtra(ChatConnectionService.BUNDLE_MESSAGE_BODY)
                if (fromJidRaw == null) return

                val fromGuid = if (fromJidRaw.contains("@")) {
                    fromJidRaw.split("@")[0]
                } else {
                    fromJidRaw
                }

                Log.d("MembersAdapterDebug", "fromGuid=$fromGuid body=$body membersSize=${members.size}")

                var matched = false
                for (member in members) {
                    if (fromGuid == member.guid) {
                        matched = true
                        member.lastMessage = body
                        member.lastMessageTime = AndroidUtils.getChatTimeText(Date())
                        member.lastMessageTimestamp = System.currentTimeMillis()

                        val currentUnread = parseUnread(member.unread_count)
                        member.unread_count = (currentUnread + 1).toString()

                        sortMembers()
                        break
                    }
                }
                if (!matched) {
                    Log.d("MembersAdapterDebug", "No member matched fromGuid=$fromGuid")
                }
            }
        }

        val filter = IntentFilter(ChatConnectionService.NEW_MESSAGE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(newMessageReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            ContextCompat.registerReceiver(context, newMessageReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        }
        Log.d("MembersAdapterDebug", "newMessageReceiver registered, context=$context")
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

    private fun sortMembers() {
        filteredList.sortWith { m1, m2 ->
            val t1 = m1.lastMessageTimestamp
            val t2 = m2.lastMessageTimestamp

            if (t1 != t2) {
                t2.compareTo(t1)
            } else {
                val unread1 = parseUnread(m1.unread_count)
                val unread2 = parseUnread(m2.unread_count)

                if (unread1 != unread2) {
                    unread2.compareTo(unread1)
                } else {
                    val n1 = m1.name ?: ""
                    val n2 = m2.name ?: ""
                    n1.compareTo(n2, ignoreCase = true)
                }
            }
        }
        notifyDataSetChanged()
    }

    private fun parseUnread(unread: String?): Int {
        return try {
            unread?.toInt() ?: 0
        } catch (e: Exception) {
            0
        }
    }

    class MemberVH(v: View) : RecyclerView.ViewHolder(v) {
        val tv_name: TextView = v.findViewById(R.id.tv_name)
        val person_icon: TextView = v.findViewById(R.id.person_icon)
        val tv_last_msg: TextView = v.findViewById(R.id.tv_last_msg)
        val tv_last_msg_time: TextView = v.findViewById(R.id.tv_last_msg_time)
        val count_icon: TextView = v.findViewById(R.id.count_icon)
        val plus_icon: ImageView = v.findViewById(R.id.plus_icon)
        val ll_users: LinearLayoutCompat = v.findViewById(R.id.ll_users)
        val ll_client_card: LinearLayout = v.findViewById(R.id.ll_client_card)
        val ll_last_msg: RelativeLayout = v.findViewById(R.id.ll_last_msg)
    }
}
