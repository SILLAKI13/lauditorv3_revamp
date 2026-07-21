package com.digicoffer.lauditor.TimeSheets.Adapters

import android.content.Context
import android.graphics.Color
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
import com.digicoffer.lauditor.TimeSheets.Models.ProjectTMModel
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils
import java.util.ArrayList

class ProjectTMAdapter(
    var context: Context,
    projectTmList: ArrayList<ProjectTMModel>
) : RecyclerView.Adapter<ProjectTMAdapter.MyViewHolder>(), Filterable {

    private var projectTmList = ArrayList(projectTmList)
    private var projectTmListFull = ArrayList(projectTmList)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.weekly_timesheets, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val projectTMModel = projectTmList[position]
        holder.ll_total_hours.visibility = View.VISIBLE
        holder.tv_matter_name.text = projectTMModel.name
        holder.tv_matter_billable.setText(R.string.billable)
        holder.tv_matter_task_name.setText(R.string.non_billable)

        val billableHour = projectTMModel.billableHours + " Hour"
        val nonBillableHour = projectTMModel.nonBillablehours + " Hour"
        val totalHours = projectTMModel.total + " Hours"

        holder.tv_matter_hours.text = billableHour
        holder.tv_matter_minutes.text = nonBillableHour
        holder.tv_total_hours.text = totalHours
    }

    override fun getItemCount(): Int {
        return projectTmList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = ArrayList<ProjectTMModel>()
                if (constraint == null || constraint.length == 0) {
                    filteredList.addAll(projectTmListFull)
                } else {
                    val filterPattern = constraint.toString().lowercase().trim()
                    for (model in projectTmListFull) {
                        if (model.name != null && model.name!!.lowercase().contains(filterPattern)) {
                            filteredList.add(model)
                        }
                    }
                }
                val results = FilterResults()
                results.values = filteredList
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults) {
                projectTmList.clear()
                projectTmList.addAll(results.values as ArrayList<ProjectTMModel>)
                notifyDataSetChanged()
            }
        }
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tv_matter_name: TextView = itemView.findViewById(R.id.tv_matter_name)
        var tv_matter_hours: TextView = itemView.findViewById(R.id.tv_matter_hours)
        var tv_matter_minutes: TextView = itemView.findViewById(R.id.tv_matter_minutes)
        var tv_matter_billable: TextView = itemView.findViewById(R.id.tv_matter_billable)
        var tv_matter_task_name: TextView = itemView.findViewById(R.id.tv_matter_task_name)
        var tv_total_hours: TextView = itemView.findViewById(R.id.tv_total_hours)
        var total_hours_id: TextView = itemView.findViewById(R.id.total_hours_id)
        var tv_tm_label: TextView = itemView.findViewById(R.id.tv_tm_label)
        var ll_total_hours: LinearLayout = itemView.findViewById(R.id.ll_total_hours)
        var llc_week: LinearLayoutCompat = itemView.findViewById(R.id.llc_week)
        var week_line: View = itemView.findViewById(R.id.week_line)

        init {
            tv_tm_label.visibility = View.VISIBLE
            tv_tm_label.setText(R.string.team_members__)
            
            tv_matter_name.setTextColor(Color.BLACK)
            tv_total_hours.setTextColor(itemView.context.resources.getColor(R.color.black))
            tv_total_hours.textSize = DynamicUtils.twentyFive.toFloat()
            tv_matter_hours.setTextColor(itemView.context.resources.getColor(R.color.black))
            tv_matter_minutes.setTextColor(itemView.context.resources.getColor(R.color.black))
            tv_matter_task_name.setTextColor(itemView.context.resources.getColor(R.color.blue))
            tv_matter_task_name.textSize = DynamicUtils.twenty.toFloat()
            tv_matter_billable.setTextColor(itemView.context.resources.getColor(R.color.blue))
            tv_matter_billable.textSize = DynamicUtils.twenty.toFloat()
            total_hours_id.setTextColor(itemView.context.resources.getColor(R.color.blue))
            total_hours_id.textSize = DynamicUtils.twenty.toFloat()
            total_hours_id.setText(R.string.total_hours)

            week_line.visibility = View.VISIBLE

            val layoutParams = LinearLayoutCompat.LayoutParams(
                LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(40, 20, 20, 20)
            llc_week.layoutParams = layoutParams
        }
    }
}
