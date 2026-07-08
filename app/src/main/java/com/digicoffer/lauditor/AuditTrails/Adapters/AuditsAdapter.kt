package com.digicoffer.lauditor.AuditTrails.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.AuditTrails.Model.AuditsModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import java.util.ArrayList
import java.util.regex.Pattern

class AuditsAdapter(auditsList: ArrayList<AuditsModel>) : RecyclerView.Adapter<AuditsAdapter.MyViewHolder>(), Filterable {

    @JvmField
    var filtered_list = ArrayList<AuditsModel>()
    
    @JvmField
    var itemList = ArrayList<AuditsModel>()
    
    private var auditsList: List<AuditsModel>? = null
    var item_position = 0

    init {
        this.filtered_list = auditsList
        this.itemList = auditsList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.audit_recyclerview, parent, false)
        return MyViewHolder(itemView)
    }

    fun updateData(newData: List<AuditsModel>) {
        this.auditsList = newData
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val auditsModel = filtered_list[position]
        when (auditsModel.name) {
            "AUTH" -> {
                holder.tv_category_name.setText(R.string.authentication_c)
                loadHiddenData(holder, auditsModel)
            }
            "TEAM MEMBER" -> {
                holder.tv_category_name.setText(R.string.team_member_c)
                loadHiddenData(holder, auditsModel)
            }
            "RELATIONSHIP" -> {
                holder.tv_category_name.setText(R.string.relationship_c)
                loadHiddenData(holder, auditsModel)
            }
            "RELATIONSHIP INVITE" -> {
                holder.tv_category_name.setText(R.string.relationship_invite_c)
                loadHiddenData(holder, auditsModel)
            }
            "SHARE" -> {
                holder.tv_category_name.setText(R.string.share_c)
                loadHiddenData(holder, auditsModel)
            }
            "DOCUMENT" -> {
                holder.tv_category_name.setText(R.string.document_c)
                loadHiddenData(holder, auditsModel)
            }
            "LEGAL MATTER" -> {
                holder.tv_category_name.setText(R.string.legal_matter_c)
                loadHiddenData(holder, auditsModel)
            }
            "GENERAL MATTER" -> {
                holder.tv_category_name.setText(R.string.general_matter_c)
                loadHiddenData(holder, auditsModel)
            }
            "GROUPS" -> {
                holder.tv_category_name.setText(R.string.group_c)
                loadHiddenData(holder, auditsModel)
            }
            else -> {
                holder.tv_category_name.text = auditsModel.name
                loadHiddenData(holder, auditsModel)
            }
        }
    }

    private fun loadHiddenData(holder: MyViewHolder, auditsModel: AuditsModel) {
        holder.tv_audit_matter.text = auditsModel.message
        holder.tv_timestamp.text = auditsModel.timestamp
    }

    fun removeItem(position: Int) {
        filtered_list.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val charString = charSequence?.toString()?.lowercase()?.trim() ?: ""
                if (charString.isEmpty()) {
                    filtered_list = itemList
                } else {
                    val filteredList = ArrayList<AuditsModel>()
                    for (row in itemList) {
                        val pattern = Pattern.compile(Pattern.quote(charString), Pattern.CASE_INSENSITIVE)
                        if (pattern.matcher(AndroidUtils.isNull(row.message).lowercase()).find()
                            || pattern.matcher(AndroidUtils.isNull(row.timestamp).lowercase()).find()
                            || pattern.matcher(AndroidUtils.isNull(row.name).lowercase()).find()
                        ) {
                            filteredList.add(row)
                        }
                    }
                    filtered_list = filteredList
                }
                val filterResults = FilterResults()
                filterResults.count = filtered_list.size
                filterResults.values = filtered_list
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults) {
                filtered_list = filterResults.values as ArrayList<AuditsModel>
                notifyDataSetChanged()
            }
        }
    }

    fun clearData() {
        filtered_list.clear()
        notifyDataSetChanged()
    }

    fun setData(newData: ArrayList<AuditsModel>) {
        filtered_list = newData
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return filtered_list.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv_category_name: TextView = itemView.findViewById(R.id.tv_category_name)
        val tv_timestamp: TextView = itemView.findViewById(R.id.tv_timestamp)
        val tv_audit_matter: TextView = itemView.findViewById(R.id.tv_audit_matter)
    }
}
