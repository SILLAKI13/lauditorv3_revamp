package com.digicoffer.lauditor.Chat.Adapters

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.Chat.Model.ChildDO
import com.digicoffer.lauditor.Chat.Model.ClientRelationshipsDo
import com.digicoffer.lauditor.Dashboard.DahboardModels.UnreadCountModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList

class TeamsAdapter(
    contactsList: ArrayList<ClientRelationshipsDo>,
    private val context: EventListener,
    private val frag_context: Context,
    private val activity: FragmentActivity
) : RecyclerView.Adapter<TeamsAdapter.MyViewHolder>(), Filterable, ChildAdapter.EventListener {

    var child_list = ArrayList<ChildDO>()
    private var isExpandable = false
    var tv_name_users: TextView? = null
    var progress_dialog: Dialog? = null
    var filtered_list = ArrayList<ClientRelationshipsDo>()
    var list_item = ArrayList<ClientRelationshipsDo>()

    interface EventListener {
        fun Message(childDO: ChildDO?)
        @Throws(JSONException::class)
        fun view_users(uid: String?, name: String?)
        fun Users(clientRelationshipsDo: ClientRelationshipsDo?, holder: MyViewHolder?)
    }

    init {
        this.list_item = contactsList
        this.filtered_list = contactsList
        this.isExpandable = false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.client_list, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val clientRelationshipsDo = filtered_list[position]
        Constants.Chat_id = ""
        var totalUnreadCount = 0
        for (unreadCount in Constants.unreadList) {
            if (unreadCount.fromjid == clientRelationshipsDo.guid) {
                try {
                    totalUnreadCount += unreadCount.count?.toIntOrNull() ?: 0
                } catch (e: Exception) {
                }
            }
        }

        clientRelationshipsDo.unread_count = totalUnreadCount.toString()
        val isExpandableLocal = clientRelationshipsDo.expanded

        Log.d("Position", "${clientRelationshipsDo.recyclerview_position}${clientRelationshipsDo.name}")

        holder.tv_name.text = clientRelationshipsDo.name
        val name = clientRelationshipsDo.name
        if (!name.isNullOrEmpty()) {
            val personName = name.substring(0, 1)
            holder.person_icon.text = personName
        }
        holder.ll_users.visibility = View.VISIBLE

        Log.d("Client_Type", "${clientRelationshipsDo.clientType}")

        val users = clientRelationshipsDo.Users
        if (users != null && users.length() > 0) {
            holder.plus_icon.visibility = View.VISIBLE
        } else {
            holder.plus_icon.visibility = View.GONE
        }

        holder.ll_users.visibility = if (isExpandableLocal) View.VISIBLE else View.GONE
        if (isExpandableLocal) {
            holder.plus_icon.setBackgroundResource(R.drawable.minus_icon_small_chat)
        } else {
            holder.plus_icon.setBackgroundResource(R.drawable.plus_icon_xl_chat)
        }

        holder.plus_icon.setOnClickListener {
            if (clientRelationshipsDo.expanded) {
                child_list.clear()
                clientRelationshipsDo.expanded = false
            } else {
                clientRelationshipsDo.expanded = true
                context.Users(clientRelationshipsDo, holder)
            }
            notifyItemChanged(holder.bindingAdapterPosition)
        }

        val unread = clientRelationshipsDo.unread_count
        if (!unread.isNullOrEmpty()) {
            val unreadcount = try { unread.toInt() } catch (e: Exception) { 0 }
            Log.d("unread_count_value", "$unreadcount")
            if (unreadcount > 0) {
                holder.dot_icon.visibility = View.VISIBLE
            } else {
                holder.dot_icon.visibility = View.GONE
            }
        } else {
            holder.dot_icon.visibility = View.GONE
        }

        try {
            child_list.clear()
            if (users != null) {
                for (i in 0 until users.length()) {
                    val childDO1 = ChildDO()
                    val jsonuser = users.getJSONObject(i)
                    childDO1.setGuid(jsonuser.getString("guid"))
                    childDO1.name = jsonuser.getString("name")
                    child_list.add(childDO1)
                }
            }

            for (client in child_list) {
                for (unreadCount in Constants.unreadList) {
                    if (unreadCount.fromjid == client.guid) {
                        client.unread_count = unreadCount.count
                        Log.d("teamunread_list", "${client.guid} count: ${client.unread_count}")
                        break
                    } else {
                        client.unread_count = ""
                    }
                }
            }
            loadChildList(holder)
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    private fun getUserInTeam(users: JSONArray, teamId: String, name: String, holder: MyViewHolder) {
        try {
            for (j in 0 until users.length()) {
                val childDO1 = ChildDO()
                val jsonuser = users.getJSONObject(j)
                childDO1.setGuid(jsonuser.getString("guid"))
                childDO1.name = jsonuser.getString("name")
                child_list.add(childDO1)
            }
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
        loadChildList(holder)
    }

    private fun loadChildList(holder: MyViewHolder) {
        val layoutManager = LinearLayoutManager(frag_context, LinearLayoutManager.VERTICAL, false)
        val childRecyclerViewAdapter = ChildAdapter(child_list, holder.rv_users.context, this, activity)
        holder.rv_users.adapter = childRecyclerViewAdapter
        holder.rv_users.layoutManager = layoutManager
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    filtered_list = list_item
                } else {
                    val filteredList = ArrayList<ClientRelationshipsDo>()
                    for (row in list_item) {
                        if (AndroidUtils.isNull(row.name).lowercase().contains(charString.lowercase()) ||
                            AndroidUtils.isNull(row.clientType).lowercase().contains(charString.lowercase()) ||
                            AndroidUtils.isNull(row.created).lowercase().contains(charString.lowercase()) ||
                            AndroidUtils.isNull(row.consent).lowercase().contains(charString.lowercase())
                        ) {
                            filteredList.add(row)
                        }
                    }
                    filtered_list = filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = filtered_list
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(cha: CharSequence?, filterResults: FilterResults?) {
                filtered_list = filterResults?.values as ArrayList<ClientRelationshipsDo>
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount(): Int {
        return filtered_list.size
    }

    override fun Message(childDO: ChildDO?) {
        context.Message(childDO)
    }

    override fun view_users(uid: String?, name: String?) {
        try {
            context.view_users(uid, name)
        } catch (e: JSONException) {
            e.fillInStackTrace()
        }
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv_name: TextView = itemView.findViewById(R.id.tv_name)
        val person_icon: TextView = itemView.findViewById(R.id.person_icon)
        val plus_icon: ImageView = itemView.findViewById(R.id.plus_icon)
        val dot_icon: ImageView = itemView.findViewById(R.id.dot_icon)
        val ll_users: LinearLayoutCompat = itemView.findViewById(R.id.ll_users)
        val rv_users: RecyclerView = itemView.findViewById(R.id.rv_users)
        val ll_client_card: LinearLayout = itemView.findViewById(R.id.ll_client_card)
    }
}
