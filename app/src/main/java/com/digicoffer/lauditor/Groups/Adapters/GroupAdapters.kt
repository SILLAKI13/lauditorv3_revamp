package com.digicoffer.lauditor.Groups.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.ItemClickListener
import com.digicoffer.lauditor.Groups.Groups
import com.digicoffer.lauditor.Groups.Models.GroupModel
import com.digicoffer.lauditor.R
import java.util.ArrayList

class GroupAdapters : RecyclerView.Adapter<GroupAdapters.ViewHolder>, Filterable {
    private var itemsArrayList: ArrayList<GroupModel>
    private var filtered_List: ArrayList<GroupModel>
    private val list_item: ArrayList<GroupModel>
    private val itemClickListener: ItemClickListener?
    private val mTag: String
    var selectedGroupId: String = ""
    private val group: Groups
    var btn_submit: Button? = null

    constructor(
        itemsArrayList: ArrayList<GroupModel>,
        Tag: String,
        itemClickListener: ItemClickListener?,
        context: Groups
    ) {
        this.itemsArrayList = itemsArrayList
        this.list_item = ArrayList(itemsArrayList)
        this.filtered_List = itemsArrayList
        this.group = context
        this.mTag = Tag
        this.itemClickListener = itemClickListener
    }

    constructor(
        itemsArrayList: ArrayList<GroupModel>,
        Tag: String,
        itemClickListener: ItemClickListener?,
        context: Groups,
        btn_submit: Button?
    ) {
        this.itemsArrayList = itemsArrayList
        this.list_item = ArrayList(itemsArrayList)
        this.filtered_List = itemsArrayList
        this.group = context
        this.mTag = Tag
        this.itemClickListener = itemClickListener
        this.btn_submit = btn_submit
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = if (mTag == "TM") {
            inflater.inflate(R.layout.select_team_members, parent, false)
        } else {
            inflater.inflate(R.layout.assign_group_head, parent, false)
        }
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val groupModel = filtered_List[position]
        if (mTag == "TM") {
            holder.cb_team_members?.isChecked = groupModel.isChecked
            holder.cb_team_members?.tag = position
            holder.cb_team_members?.isEnabled = true
            check_allselected()
            holder.cb_team_members?.setOnClickListener {
                val pos = holder.bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
                val isChecked = filtered_List[pos].isChecked
                filtered_List[pos].isChecked = !isChecked
                check_allselected()
                group.selectedtmlist(filtered_List)
            }
            holder.tv_tm_name?.text = groupModel.name
        } else {
            holder.rb_tv_name?.text = groupModel.name
            holder.rb_group_head?.setOnCheckedChangeListener(null)
            holder.rb_group_head?.isChecked = groupModel.id == selectedGroupId
            holder.rb_group_head?.tag = groupModel.id

            holder.rb_group_head?.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedGroupId = groupModel.id
                    itemClickListener?.onClick(groupModel.id)
                    notifyDataSetChanged()
                }
            }
        }
    }

    fun getList_item(): ArrayList<GroupModel> {
        return itemsArrayList
    }

    override fun getItemCount(): Int {
        return filtered_List.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val charString = charSequence?.toString() ?: ""
                val filteredList: List<GroupModel> = if (charString.isEmpty()) {
                    ArrayList(list_item)
                } else {
                    val result = ArrayList<GroupModel>()
                    for (row in list_item) {
                        val name = row.name
                        if (name.lowercase().contains(charString.lowercase())) {
                            result.add(row)
                        }
                    }
                    result
                }
                val filterResults = FilterResults()
                filterResults.count = filteredList.size
                filterResults.values = filteredList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults) {
                itemsArrayList = filterResults.values as ArrayList<GroupModel>
                filtered_List = itemsArrayList
                if (itemsArrayList.isNotEmpty()) {
                    check_allselected()
                }
                notifyDataSetChanged()
            }
        }
    }

    fun selectOrDeselectAll(isChecked: Boolean): Boolean {
        for (groupModel in filtered_List) {
            groupModel.isChecked = isChecked
        }
        group.selectedtmlist(filtered_List)
        notifyDataSetChanged()
        return isChecked
    }

    fun check_allselected() {
        var allSelected = true
        for (groupModel in itemsArrayList) {
            if (!groupModel.isChecked) {
                allSelected = false
                break
            }
        }
        group.check_select_all(allSelected)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cb_team_members: CheckBox? = itemView.findViewById(R.id.chk_selected)
        val tv_tm_name: TextView? = itemView.findViewById(R.id.tv_tm_name)
        val rb_tv_name: TextView? = itemView.findViewById(R.id.rb_tv_name)
        val rb_group_head: CheckBox? = itemView.findViewById(R.id.rb_selected)
    }
}
