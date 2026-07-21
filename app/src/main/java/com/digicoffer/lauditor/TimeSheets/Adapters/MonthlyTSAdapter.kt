package com.digicoffer.lauditor.TimeSheets.Adapters

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.TimeSheets.Models.Month_Model
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils
import java.util.ArrayList

class MonthlyTSAdapter(
    var context: Context,
    var teamList: ArrayList<Month_Model>,
    var status: String,
    var choosen_month: String
) : RecyclerView.Adapter<MonthlyTSAdapter.MyViewHolder>(), Filterable {

    var itemsList: ArrayList<Month_Model> = teamList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.monthly_timesheets, parent, false)
        return MyViewHolder(view)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    teamList = itemsList
                } else {
                    val filteredList = ArrayList<Month_Model>()
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
                teamList = filterResults.values as ArrayList<Month_Model>
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val tmModel = teamList[position]
        holder.ll_total_hours.visibility = View.VISIBLE
        holder.month_matter_name.text = tmModel.name
        Log.d("Model_name", "" + tmModel.name)
        holder.tv_matter_hours.text = tmModel.billable_week_tot + " Hour"
        holder.tv_matter_minutes.text = tmModel.non_billable_week_tot + " Hour"
        holder.tv_total_hours.text = tmModel.Total + " Hours"

        holder.billable1.text = tmModel.billable_week_1
        holder.billable2.text = tmModel.billable_week_2
        holder.billable3.text = tmModel.billable_week_3
        holder.billable4.text = tmModel.billable_week_4
        holder.billable5.text = tmModel.billable_week_5

        holder.non_billable1.text = tmModel.non_billable_week_1
        holder.non_billable2.text = tmModel.non_billable_week_2
        holder.non_billable3.text = tmModel.non_billable_week_3
        holder.non_billable4.text = tmModel.non_billable_week_4
        holder.non_billable5.text = tmModel.non_billable_week_5

        holder.tot_billable.text = tmModel.billable_week_tot
        holder.tot_non_billable.text = tmModel.non_billable_week_tot
    }

    override fun getItemCount(): Int {
        return teamList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tv_matter_name: TextView = itemView.findViewById(R.id.tv_matter_name)
        var tv_matter_hours: TextView = itemView.findViewById(R.id.tv_matter_hours)
        var tv_matter_minutes: TextView = itemView.findViewById(R.id.tv_matter_minutes)
        var tv_matter_billable: TextView = itemView.findViewById(R.id.tv_matter_billable)
        var tv_matter_task_name: TextView = itemView.findViewById(R.id.tv_matter_task_name)
        var month_matter_name: TextView = itemView.findViewById(R.id.month_matter_name)
        var total_hours_id: TextView = itemView.findViewById(R.id.total_hours_id)
        var ll_total_hours: LinearLayout = itemView.findViewById(R.id.ll_total_hours)
        var week_linear: LinearLayoutCompat = itemView.findViewById(R.id.week_linear)
        var timeheets_month: TextView = itemView.findViewById(R.id.timeheets_month)
        var week1: TextView = itemView.findViewById(R.id.week1)
        var week2: TextView = itemView.findViewById(R.id.week2)
        var week3: TextView = itemView.findViewById(R.id.week3)
        var week4: TextView = itemView.findViewById(R.id.week4)
        var week5: TextView = itemView.findViewById(R.id.week5)
        var total: TextView = itemView.findViewById(R.id.total)
        var month_billable: TextView = itemView.findViewById(R.id.month_billable)
        var billable1: TextView = itemView.findViewById(R.id.billable1)
        var billable2: TextView = itemView.findViewById(R.id.billable2)
        var billable3: TextView = itemView.findViewById(R.id.billable3)
        var billable4: TextView = itemView.findViewById(R.id.billable4)
        var billable5: TextView = itemView.findViewById(R.id.billable5)
        var month_non_billable: TextView = itemView.findViewById(R.id.month_non_billable)
        var non_billable1: TextView = itemView.findViewById(R.id.non_billable1)
        var non_billable2: TextView = itemView.findViewById(R.id.non_billable2)
        var non_billable3: TextView = itemView.findViewById(R.id.non_billable3)
        var non_billable4: TextView = itemView.findViewById(R.id.non_billable4)
        var non_billable5: TextView = itemView.findViewById(R.id.non_billable5)
        var tot_non_billable: TextView = itemView.findViewById(R.id.tot_non_billable)
        var tot_billable: TextView = itemView.findViewById(R.id.tot_billable)
        var tv_total_hours: TextView = itemView.findViewById(R.id.tv_total_hours)

        init {
            val MonthName = "Month of $choosen_month"
            timeheets_month.text = MonthName
            week1.setText(R.string.week_1)
            week2.setText(R.string.week_2)
            week3.setText(R.string.week_3)
            week4.setText(R.string.week_4)
            week5.setText(R.string.week_5)
            total.setText(R.string.total)
            month_billable.setText(R.string.billable)
            month_billable.gravity = Gravity.CENTER

            month_non_billable.setText(R.string.non_billable)
            month_non_billable.gravity = Gravity.CENTER

            tv_matter_name.visibility = View.GONE
            month_matter_name.textSize = DynamicUtils.eighteen.toFloat()
            total_hours_id.setText(R.string.total_hours)

            week_linear.setPadding(30, 10, 10, 10)
            tv_total_hours.setTextColor(context.resources.getColor(R.color.black))
            tv_total_hours.textSize = DynamicUtils.twenty.toFloat()
            tv_matter_hours.setTextColor(context.resources.getColor(R.color.black))
            tv_matter_minutes.setTextColor(context.resources.getColor(R.color.black))
            tv_matter_task_name.setTextColor(context.resources.getColor(R.color.blue))
            tv_matter_task_name.setText(R.string.non_billable)
            tv_matter_billable.setTextColor(context.resources.getColor(R.color.blue))
            tv_matter_billable.setText(R.string.billable)
            total_hours_id.setTextColor(context.resources.getColor(R.color.blue))
        }
    }
}
