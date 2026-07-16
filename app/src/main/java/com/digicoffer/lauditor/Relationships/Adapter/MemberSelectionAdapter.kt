package com.digicoffer.lauditor.Relationships.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Relationships.MemberModel
import java.util.ArrayList

class MemberSelectionAdapter(
    memberList: ArrayList<MemberModel>,
    private val listener: OnMemberSelectionChanged
) : RecyclerView.Adapter<MemberSelectionAdapter.ViewHolder>(), Filterable {

    private var originalList: ArrayList<MemberModel> = ArrayList(memberList)
    private var filteredList: ArrayList<MemberModel> = ArrayList(memberList)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.select_team_members, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val member = filteredList[position]

        holder.tvMemberName.text = member.name
        holder.cbMember.isChecked = member.isChecked
        holder.cbMember.setOnClickListener {
            member.isChecked = holder.cbMember.isChecked
            notifySelectionChanged(true) // ✅ Pass user interaction
        }

        holder.selectLayout.setOnClickListener {
            val isChecked = !holder.cbMember.isChecked
            holder.cbMember.isChecked = isChecked
            member.isChecked = isChecked
            notifySelectionChanged(true) // ✅ Pass user interaction
        }
    }

    private fun notifySelectionChanged(userInteracted: Boolean) {
        val selected = ArrayList<MemberModel>()
        for (member in originalList) {
            if (member.isChecked) {
                selected.add(member)
            }
        }
        listener.onSelectionChanged(selected, userInteracted)
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    fun selectOrDeselectAll(isChecked: Boolean) {
        for (member in originalList) {
            member.isChecked = isChecked
        }
        notifyDataSetChanged()
        notifySelectionChanged(false)
    }

    fun getSelectedMembers(): ArrayList<MemberModel> {
        val selected = ArrayList<MemberModel>()
        for (member in originalList) {
            if (member.isChecked) {
                selected.add(member)
            }
        }
        return selected
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val search = constraint?.toString()?.trim()?.lowercase() ?: ""
                if (search.isEmpty()) {
                    filteredList = ArrayList(originalList)
                } else {
                    val result = ArrayList<MemberModel>()
                    for (member in originalList) {
                        val name = member.name
                        if (name != null && name.lowercase().contains(search)) {
                            result.add(member)
                        }
                    }
                    filteredList = result
                }
                val results = FilterResults()
                results.values = filteredList
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = (results?.values as? ArrayList<MemberModel>) ?: ArrayList()
                notifyDataSetChanged()
            }
        }
    }

    fun interface OnMemberSelectionChanged {
        fun onSelectionChanged(selectedMembers: ArrayList<MemberModel>, userInteracted: Boolean)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cbMember: CheckBox = itemView.findViewById(R.id.chk_selected)
        val tvMemberName: TextView = itemView.findViewById(R.id.tv_tm_name)
        val selectLayout: LinearLayout = itemView.findViewById(R.id.select_tm_layout)
    }
}
