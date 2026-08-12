package com.digicoffer.lauditor.Members.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.Groups.Models.ViewGroupModel
import com.digicoffer.lauditor.Matter.ViewModels.ViewMatter
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import java.util.ArrayList

class GroupsAdapter : RecyclerView.Adapter<GroupsAdapter.ViewHolder>, Filterable {
    var groupsList = ArrayList<ViewGroupModel>()
    var listItems = ArrayList<ViewGroupModel>()
    var filtered_List = ArrayList<ViewGroupModel>()
    var relationshipsAdapter: Any? = null
    var viewMatter: ViewMatter? = null
    var clientRelationship: Any? = null

    constructor(groupsList: ArrayList<ViewGroupModel>) {
        this.groupsList = groupsList
        this.listItems = groupsList
        this.filtered_List = groupsList
    }

    constructor(groupsList: ArrayList<ViewGroupModel>, viewMatter: ViewMatter?) {
        this.groupsList = groupsList
        this.listItems = groupsList
        this.filtered_List = groupsList
        this.viewMatter = viewMatter
    }

    constructor(
        groupsList: ArrayList<ViewGroupModel>,
        relationshipsAdapter: Any?,
        clientRelationship: Any?
    ) {
        this.groupsList = groupsList
        this.listItems = groupsList
        this.filtered_List = groupsList
        this.relationshipsAdapter = relationshipsAdapter
        this.clientRelationship = clientRelationship
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.select_team_members, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val layoutParams = holder.itemView.layoutParams
        layoutParams.width = RecyclerView.LayoutParams.MATCH_PARENT
        holder.itemView.layoutParams = layoutParams
        
        groupsList = listItems
        val groupModel = filtered_List[position]
        holder.cb_team_members.isChecked = filtered_List[position].isChecked
        holder.cb_team_members.tag = position

        holder.cb_team_members.setOnClickListener {
            val adapterPos = holder.adapterPosition
            if (adapterPos != RecyclerView.NO_POSITION) {
                val group = filtered_List[adapterPos]
                val isChecked = group.isChecked

                if (false) {
                    // commented out relationshipsAdapter blocks
                } else if (viewMatter != null) {
                    if (Constants.isAlterPopup) {
                        group.isChecked = !isChecked
                        viewMatter?.loadNewGroups(groupsList)
                    } else {
                        if (isChecked) {
                            if (!group.isCan_delete) {
                                group.isChecked = true
                                viewMatter?.checkRemoveGroups(group.group_id, group.name, groupsList)
                                notifyItemChanged(adapterPos)
                            } else {
                                group.isChecked = false
                                viewMatter?.load_selected_groups(groupsList)
                            }
                        } else {
                            group.isChecked = true
                            viewMatter?.load_selected_groups(groupsList)
                        }
                    }
                } else {
                    group.isChecked = !isChecked
                    holder.cb_team_members.isChecked = !isChecked
                }
            }
        }

        if (groupModel.name != null) {
            holder.tv_tm_name.text = groupModel.name
        } else {
            holder.tv_tm_name.text = groupModel.group_name
        }
        holder.select_tm_layout.layoutParams = layoutParams
    }

    fun resetGroupTemporaryStates() {
        for (group in groupsList) {
            group.isChecked = group.isChecked
        }
        notifyDataSetChanged()
    }

    fun selectOrDeselectAll(isChecked: Boolean) {
        for (i in listItems.indices) {
            listItems[i].isChecked = isChecked
        }
        notifyDataSetChanged()
    }

    fun getList_item(): ArrayList<ViewGroupModel> {
        return groupsList
    }

    override fun getItemCount(): Int {
        return filtered_List.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val charString = charSequence?.toString() ?: ""
                val filteredList: MutableList<ViewGroupModel> = if (charString.isEmpty()) {
                    ArrayList(listItems)
                } else {
                    val temp = ArrayList<ViewGroupModel>()
                    for (row in listItems) {
                        val name = row.name
                        if (name != null && name.lowercase().contains(charString.lowercase())) {
                            temp.add(row)
                        }
                    }
                    temp
                }
                val filterResults = FilterResults()
                filterResults.count = filteredList.size
                filterResults.values = filteredList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults?) {
                groupsList = (filterResults?.values as? ArrayList<ViewGroupModel>) ?: ArrayList()
                filtered_List = groupsList
                notifyDataSetChanged()
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var cb_team_members: CheckBox = itemView.findViewById(R.id.chk_selected)
        var tv_tm_name: TextView = itemView.findViewById(R.id.tv_tm_name)
        var select_tm_layout: LinearLayout = itemView.findViewById(R.id.select_tm_layout)
    }
}
