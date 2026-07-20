package com.digicoffer.lauditor.Chat.Adapters

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.Chat.Model.ChildDO
import com.digicoffer.lauditor.Dashboard.DahboardModels.UnreadCountModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.CommonFiles.ChatService.ChatCountApi
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import org.json.JSONException
import java.util.ArrayList

class ChildAdapter(
    private var filteredList: ArrayList<ChildDO>,
    private val cContext: Context,
    private val context: EventListener,
    private val Mactivity: Activity
) : RecyclerView.Adapter<ChildAdapter.MyViewHolder>() {

    interface EventListener {
        fun Message(childDO: ChildDO?)
        @Throws(JSONException::class)
        fun view_users(uid: String?, name: String?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sub, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val childDO = filteredList[position]
        holder.tv_name.text = childDO.name
        Constants.Chat_id = ""
        var totalUnreadCount = 0
        for (unreadCount in Constants.unreadList) {
            val fromjid = unreadCount.fromjid
            val guid = childDO.guid
            if (fromjid != null && guid != null && fromjid.contains(guid)) {
                try {
                    totalUnreadCount += unreadCount.count.toInt()
                } catch (e: Exception) {
                }
            }
        }

        childDO.unread_count = totalUnreadCount.toString()
        val name = childDO.name
        if (!name.isNullOrEmpty()) {
            val personName = name.substring(0, 1)
            holder.person_icon.text = personName
        }
        holder.count_icon.background = cContext.getDrawable(R.drawable.red_circular)
        val unread = childDO.unread_count
        if (!unread.isNullOrEmpty()) {
            val unreadcount = try { unread.toInt() } catch (e: Exception) { 0 }
            Log.d("unread_count_value", "$unreadcount")
            if (unreadcount > 0) {
                holder.count_icon.text = childDO.unread_count
                holder.count_icon.visibility = View.VISIBLE
            } else {
                holder.count_icon.visibility = View.GONE
            }
        } else {
            holder.count_icon.visibility = View.GONE
        }

        holder.ll_users.background = cContext.getDrawable(R.drawable.rectangular_white_background)
        holder.tv_name.setTextColor(Color.BLACK)
        holder.ll_users.setOnClickListener {
            try {
                Log.d("Message", childDO.name ?: "")
                context.Message(childDO)
                holder.ll_users.background = cContext.getDrawable(R.drawable.radiobutton_centre_green_background)
                holder.tv_name.setTextColor(Color.WHITE)
                context.view_users(childDO.guid, childDO.name)
                ChatCountApi(cContext).callResetUnreadCount(childDO.guid ?: "")
                Constants.Chat_id = childDO.guid + Constants.VitacapeExtention
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv_name: TextView = itemView.findViewById(R.id.tv_name)
        val person_icon: TextView = itemView.findViewById(R.id.person_icon)
        val count_icon: TextView = itemView.findViewById(R.id.count_icon)
        val ll_users: LinearLayoutCompat = itemView.findViewById(R.id.ll_clients)
        val ll_clients: LinearLayoutCompat = itemView.findViewById(R.id.ll_clients)
    }
}
