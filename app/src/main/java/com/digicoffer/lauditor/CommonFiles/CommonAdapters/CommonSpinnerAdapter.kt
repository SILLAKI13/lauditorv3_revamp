package com.digicoffer.lauditor.CommonFiles.CommonAdapters

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.digicoffer.lauditor.AuditTrails.Model.SpinnerItemModal
import com.digicoffer.lauditor.Meetings.Models.CalendarDo
import com.digicoffer.lauditor.Meetings.Models.MinutesDO
import com.digicoffer.lauditor.Meetings.Models.RelationshipsDO
import com.digicoffer.lauditor.Meetings.Models.TaskDo
import com.digicoffer.lauditor.Meetings.Models.TeamDo
import com.digicoffer.lauditor.Relationships.Model.CountriesDO
import com.digicoffer.lauditor.Relationships.Model.EntityModel
import com.digicoffer.lauditor.Documents.Models.ClientsModel
import com.digicoffer.lauditor.Documents.Models.MattersModel
import com.digicoffer.lauditor.Groups.Models.ActionModel
import com.digicoffer.lauditor.Groups.Models.ViewGroupModel
import com.digicoffer.lauditor.LoginActivity.Models.FirmsDo
import com.digicoffer.lauditor.Matter.Models.ViewMatterModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.TimeSheets.Models.ProjectTMModel
import com.digicoffer.lauditor.TimeSheets.Models.ProjectsModel
import com.digicoffer.lauditor.TimeSheets.Models.StatusModel
import com.digicoffer.lauditor.TimeSheets.Models.TSMatterModel
import com.digicoffer.lauditor.TimeSheets.Models.TasksModel
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.TimeZonesDO
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.UsersDO
import java.util.ArrayList

class CommonSpinnerAdapter<T>(
    private val context: Activity?,
    listArrayData: ArrayList<T>
) : BaseAdapter(), Filterable {

    private var listArrayData: ArrayList<T> = ArrayList(listArrayData)
    private var originalList: ArrayList<T> = ArrayList(listArrayData)
    private var selectedPosition = -1
    private var suggestedItems: MutableSet<String> = HashSet()

    fun setSelectedPosition(position: Int) {
        this.selectedPosition = position
        notifyDataSetChanged()
    }

    fun setSuggestedItems(items: Collection<String>?) {
        this.suggestedItems = if (items != null) HashSet(items) else HashSet()
        notifyDataSetChanged()
    }

    private fun getLabel(item: T?): String {
        if (item == null) return ""
        if (item is String) return item.toString()
        if (item is UsersDO) return item.name ?: ""
        if (item is FirmsDo) return item.getName() ?: ""
        if (item is TimeZonesDO) return item.name ?: ""
        if (item is ActionModel) return item.name ?: ""
        if (item is ViewGroupModel) return item.group_name ?: ""
        if (item is CountriesDO) return item.name ?: ""
        if (item is EntityModel) return item.entityID ?: ""
        if (item is MattersModel) return item.title ?: ""
        if (item is ClientsModel) return item.name ?: ""
        if (item is TSMatterModel) return item.mattername ?: ""
        if (item is TasksModel) return item.displayValue ?: ""
        if (item is StatusModel) return item.name ?: ""
        if (item is ProjectsModel) return item.projectName ?: ""
        if (item is ProjectTMModel) return item.name ?: ""
        if (item is CalendarDo) return item.projectName ?: ""
        if (item is ViewMatterModel) return item.title ?: ""
        if (item is TaskDo) return item.taskName ?: ""
        if (item is TeamDo) return item.name ?: ""
        if (item is RelationshipsDO) return item.name ?: ""
        if (item is MinutesDO) return item.name ?: ""
        if (item is SpinnerItemModal) return item.name ?: ""
        return ""
    }

    override fun getView(pos: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        if (view == null) {
            view = context?.layoutInflater?.inflate(R.layout.spinnerdropdownview, null, true)
        }

        val tv = view!!.findViewById<TextView>(R.id.spinnerDropDownTextview)
        val label = getLabel(listArrayData[pos])
        tv.text = label

        if (pos == selectedPosition) {
            tv.setTextColor(context?.resources?.getColor(android.R.color.white) ?: 0)
            view.setBackgroundColor(context?.resources?.getColor(R.color.green_count_color) ?: 0)
        } else {
            tv.setTextColor(context?.resources?.getColor(android.R.color.black) ?: 0)
            view.setBackgroundColor(context?.resources?.getColor(android.R.color.transparent) ?: 0)
        }

        val ivSuggest = view.findViewById<View>(R.id.iv_suggest)
        if (ivSuggest != null) {
            ivSuggest.visibility = if (suggestedItems.contains(label)) View.VISIBLE else View.GONE
        }
        return view
    }

    override fun getCount(): Int {
        return listArrayData.size
    }

    override fun getItem(i: Int): T {
        return listArrayData[i]
    }

    override fun getItemId(pos: Int): Long {
        return pos.toLong()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                if (constraint.isNullOrBlank()) {
                    results.values = ArrayList(originalList)
                    results.count = originalList.size
                } else {
                    val query = constraint.toString().lowercase().trim()
                    val filtered = ArrayList<T>()
                    for (item in originalList) {
                        if (getLabel(item).lowercase().contains(query)) {
                            filtered.add(item)
                        }
                    }
                    results.values = filtered
                    results.count = filtered.size
                }
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults) {
                listArrayData = results.values as ArrayList<T>
                notifyDataSetChanged()
            }
        }
    }

    fun updateData(newList: ArrayList<T>) {
        originalList = ArrayList(newList)
        listArrayData = ArrayList(newList)
        selectedPosition = -1
        notifyDataSetChanged()
    }
}
