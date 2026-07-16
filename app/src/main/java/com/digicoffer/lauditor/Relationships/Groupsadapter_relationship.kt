package com.digicoffer.lauditor.Relationships

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.Groups.Models.ViewGroupModel
import com.digicoffer.lauditor.R
import java.util.ArrayList
import java.util.Locale

class Groupsadapter_relationship(
    var groupsList: ArrayList<ViewGroupModel>,
    var clientRelationship: ClientRelationship?
) : RecyclerView.Adapter<Groupsadapter_relationship.ViewHolder>(), Filterable {

    var list_item: ArrayList<ViewGroupModel> = groupsList
    var filtered_list: ArrayList<ViewGroupModel> = groupsList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.select_team_members, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val layoutParams = holder.itemView.layoutParams
        layoutParams.width = RecyclerView.LayoutParams.MATCH_PARENT
        holder.itemView.layoutParams = layoutParams
        val groupModel = filtered_list[position]
        groupsList = list_item
        holder.cb_team_members.isChecked = filtered_list[position].isChecked
        holder.cb_team_members.tag = position

        holder.cb_team_members.setOnClickListener {
            val pos = holder.cb_team_members.tag as Int
            val newState = !filtered_list[pos].isChecked
            filtered_list[pos].isChecked = newState

            clientRelationship?.load_selected_groups(filtered_list)
            clientRelationship?.updateSelectAllState(areAllItemsSelected())
        }

        if (groupModel.name != null) {
            holder.tv_tm_name.text = groupModel.name
        } else {
            holder.tv_tm_name.text = groupModel.group_name
        }
        holder.select_tm_layout.layoutParams = layoutParams
    }

    fun selectOrDeselectAll(isChecked: Boolean) {
        for (i in 0 until list_item.size) {
            list_item[i].isChecked = isChecked
        }
        notifyDataSetChanged()
    }



    fun areAllItemsSelected(): Boolean {
        for (item in filtered_list) {
            if (!item.isChecked) {
                return false
            }
        }
        return true
    }

    override fun getItemCount(): Int {
        return filtered_list.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    groupsList = list_item
                } else {
                    val filteredList = ArrayList<ViewGroupModel>()
                    for (row in list_item) {
                        if (AndroidUtils.isNull(row.name).lowercase(Locale.getDefault())
                                .contains(charString.lowercase(Locale.getDefault()))
                        ) {
                            filteredList.add(row)
                        }
                    }
                    groupsList = filteredList
                }
                val filterResults = FilterResults()
                filterResults.count = groupsList.size
                filterResults.values = groupsList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                groupsList = filterResults.values as ArrayList<ViewGroupModel>
                filtered_list = groupsList
                notifyDataSetChanged()
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cb_team_members: CheckBox = itemView.findViewById(R.id.chk_selected)
        val tv_tm_name: TextView = itemView.findViewById(R.id.tv_tm_name)
        val select_tm_layout: LinearLayout = itemView.findViewById(R.id.select_tm_layout)
    }
}
