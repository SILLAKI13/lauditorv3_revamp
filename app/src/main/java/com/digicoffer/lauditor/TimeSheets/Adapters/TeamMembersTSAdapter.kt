package com.digicoffer.lauditor.TimeSheets.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.TimeSheets.Models.TMModel
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils
import java.util.ArrayList

class TeamMembersTSAdapter(
    var context: Context,
    var teamList: ArrayList<TMModel>,
    var status: String
) : RecyclerView.Adapter<TeamMembersTSAdapter.MyviewHolder>(), Filterable {

    var itemsList: ArrayList<TMModel> = teamList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyviewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.weekly_timesheets, parent, false)
        return MyviewHolder(view)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    teamList = itemsList
                } else {
                    val filteredList = ArrayList<TMModel>()
                    for (row in itemsList) {
                        if (AndroidUtils.isNull(row.name).lowercase().contains(charString.lowercase())) {
                            filteredList.add(row)
                        }
                    }
                    teamList = filteredList
                }
                val filterResults = FilterResults()
                filterResults.count = teamList.size
                filterResults.values = teamList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                teamList = filterResults.values as ArrayList<TMModel>
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: MyviewHolder, position: Int) {
        val tmModel = teamList[position]
        holder.ll_total_hours.visibility = View.VISIBLE
        holder.tv_matter_name.text = tmModel.name
        holder.tv_matter_billable.setText(R.string.billable)
        holder.tv_matter_task_name.setText(R.string.non_billable)
        holder.tv_matter_hours.text = tmModel.tb + " Hour"
        holder.tv_matter_minutes.text = tmModel.tnb + " Hour"
        holder.tv_total_hours.text = tmModel.total + " Hours"
    }

    override fun getItemCount(): Int {
        return teamList.size
    }

    inner class MyviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tv_matter_name: TextView = itemView.findViewById(R.id.tv_matter_name)
        var tv_matter_hours: TextView = itemView.findViewById(R.id.tv_matter_hours)
        var tv_matter_minutes: TextView = itemView.findViewById(R.id.tv_matter_minutes)
        var tv_matter_billable: TextView = itemView.findViewById(R.id.tv_matter_billable)
        var tv_matter_task_name: TextView = itemView.findViewById(R.id.tv_matter_task_name)
        var tv_total_hours: TextView = itemView.findViewById(R.id.tv_total_hours)
        var total_hours_id: TextView = itemView.findViewById(R.id.total_hours_id)
        var ll_total_hours: LinearLayout = itemView.findViewById(R.id.ll_total_hours)
        var cardView: CardView = itemView.findViewById(R.id.cardview_week)
        var week_linear: LinearLayoutCompat = itemView.findViewById(R.id.week_linear)

        init {
            week_linear.setPadding(30, 10, 10, 10)
            tv_matter_name.setTextColor(context.resources.getColor(R.color.blue))
            tv_matter_name.textSize = DynamicUtils.fifteen.toFloat()
            total_hours_id.setText(R.string.total_hours)

            val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            params.setMargins(10, 10, 0, 10)
            cardView.layoutParams = params

            tv_total_hours.setTextColor(context.resources.getColor(R.color.black))
            tv_total_hours.textSize = DynamicUtils.twenty.toFloat()
            tv_matter_hours.setTextColor(context.resources.getColor(R.color.black))
            tv_matter_minutes.setTextColor(context.resources.getColor(R.color.black))
            tv_matter_task_name.setTextColor(context.resources.getColor(R.color.blue))
            tv_matter_task_name.textSize = 17f
            tv_matter_billable.setTextColor(context.resources.getColor(R.color.blue))
            tv_matter_billable.textSize = 17f
            total_hours_id.setTextColor(context.resources.getColor(R.color.blue))
            total_hours_id.textSize = 17f
        }
    }
}
