package com.digicoffer.lauditor.Documents.DocumentsListAdpater

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.Documents.Documents
import com.digicoffer.lauditor.Documents.Models.DocumentsModel
import com.digicoffer.lauditor.R
import java.util.ArrayList

class GroupsListAdapter(
    private var sharedList: ArrayList<DocumentsModel>,
    documents: Documents?,
    private val listener: OnCheckedChangeListener?
) : RecyclerView.Adapter<GroupsListAdapter.Viewholder>() {

    private var list_item: ArrayList<DocumentsModel> = sharedList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.select_team_members, parent, false)
        return Viewholder(itemView)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        val groupsModel = sharedList[position]
        holder.cb_documents.isChecked = groupsModel.isGroupChecked
        holder.cb_documents.tag = position
        holder.tv_tm_name.text = groupsModel.group_name

        // Setting click listener for the checkbox
        holder.cb_documents.setOnClickListener {
            val pos = holder.cb_documents.tag as Int
            if (sharedList[pos].isGroupChecked) {
                sharedList[pos].isGroupChecked = false
                holder.cb_documents.isChecked = false
            } else {
                sharedList[pos].isGroupChecked = true
                holder.cb_documents.isChecked = true
            }

            listener?.onCheckedChanged(sharedList[pos])
        }
    }

    fun getList_item(): ArrayList<DocumentsModel> {
        return sharedList
    }

    override fun getItemCount(): Int {
        return sharedList.size
    }

    inner class Viewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv_tm_name: TextView = itemView.findViewById(R.id.tv_tm_name)
        val list_line: View = itemView.findViewById(R.id.list_line)
        val select_tm_layout: LinearLayout = itemView.findViewById(R.id.select_tm_layout)
        val cb_documents: CheckBox = itemView.findViewById(R.id.chk_selected)

        init {
            list_line.visibility = View.VISIBLE
            select_tm_layout.setBackgroundResource(R.drawable.background_transparent)
        }
    }

    // Interface for checkbox change listener
    fun interface OnCheckedChangeListener {
        fun onCheckedChanged(documentsModel: DocumentsModel)
    }
}
