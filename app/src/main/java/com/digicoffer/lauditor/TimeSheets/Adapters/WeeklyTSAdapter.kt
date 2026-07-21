package com.digicoffer.lauditor.TimeSheets.Adapters

import android.content.Context
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.TimeSheets.Models.TaskModel
import com.digicoffer.lauditor.TimeSheets.ViewModels.TimeSheets
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils
import java.util.ArrayList

class WeeklyTSAdapter(
    var eventsList: ArrayList<TaskModel>,
    private val issubmitted: Boolean,
    var context_timesheets: Context,
    var eventListener: InterfaceListener,
    var date: String,
    var matter_type: String
) : RecyclerView.Adapter<WeeklyTSAdapter.MyViewHolder>() {

    var timeSheets = TimeSheets()
    var timesheet_delete_scope = "DELETE_TIMESHEET_ONLY"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.weekly_timesheets, parent, false)
        return MyViewHolder(view)
    }

    interface InterfaceListener {
        fun DeleteEvent_Timesheet(taskModel: TaskModel)
        fun EditEvent_Timesheet(eventsModels: TaskModel, date: String, matter_type: String)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val eventsModel = eventsList[position]
        if (eventsModel.taskid != null) {
            holder.tv_matter_task_name.text = eventsModel.Task_name
            holder.tv_matter_name.text = eventsModel.Task_matter_name
            val hour = eventsModel.hours + " Hour"
            val minutes = eventsModel.minutes + " Min"
            holder.tv_matter_hours.text = hour
            holder.tv_matter_minutes.text = minutes
            holder.tv_matter_billable.text = AndroidUtils.CapitalizeFirstLetter(eventsModel.Task_billing)
        }
        if (holder.tv_matter_name.text.toString().isEmpty()) {
            holder.cardView.visibility = View.GONE
            holder.edit_del_timesheets.visibility = View.GONE
        } else {
            if (eventsModel.is_editable == true) {
                holder.edit_timesheets.visibility = View.VISIBLE
            } else {
                holder.edit_timesheets.visibility = View.GONE
            }
            if (!issubmitted) {
                holder.edit_del_timesheets.visibility = View.VISIBLE
            } else {
                holder.edit_del_timesheets.visibility = View.GONE
            }

            holder.cardView.visibility = View.VISIBLE
        }
        holder.edit_timesheets.setOnClickListener {
            Log.d("Edit_timesheets", "Edit_timesheets")
            eventListener.EditEvent_Timesheet(eventsModel, date, matter_type)
        }
        holder.delete_timesheets.setOnClickListener {
            eventListener.DeleteEvent_Timesheet(eventsModel)
        }
    }

    override fun getItemCount(): Int {
        return eventsList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tv_matter_name: TextView = itemView.findViewById(R.id.tv_matter_name)
        var tv_matter_hours: TextView = itemView.findViewById(R.id.tv_matter_hours)
        var tv_matter_minutes: TextView = itemView.findViewById(R.id.tv_matter_minutes)
        var tv_matter_billable: TextView = itemView.findViewById(R.id.tv_matter_billable)
        var tv_matter_task_name: TextView = itemView.findViewById(R.id.tv_matter_task_name)
        var tv_total_hours: TextView = itemView.findViewById(R.id.tv_total_hours)
        var total_hours_id: TextView = itemView.findViewById(R.id.total_hours_id)
        var ll_total_hours: LinearLayout = itemView.findViewById(R.id.ll_total_hours)
        var edit_del_timesheets: LinearLayout = itemView.findViewById(R.id.edit_del_timesheets)
        var cardView: CardView = itemView.findViewById(R.id.cardview_week)
        var edit_timesheets: ImageView = itemView.findViewById(R.id.edit_timesheets)
        var delete_timesheets: ImageView = itemView.findViewById(R.id.delete_timesheets)
        var week_linear: LinearLayoutCompat = itemView.findViewById(R.id.week_linear)

        init {
            week_linear.setPadding(30, 10, 10, 10)
            tv_matter_billable.setAutoSizeTextTypeUniformWithConfiguration(1, 15, 1, TypedValue.COMPLEX_UNIT_SP)
            tv_matter_task_name.setAutoSizeTextTypeUniformWithConfiguration(1, 15, 1, TypedValue.COMPLEX_UNIT_SP)
            ll_total_hours.visibility = View.GONE
            total_hours_id.setText(R.string.total_hours)

            tv_total_hours.setTextColor(context_timesheets.resources.getColor(R.color.black))
            tv_total_hours.textSize = DynamicUtils.twenty.toFloat()
            tv_matter_hours.setTextColor(context_timesheets.resources.getColor(R.color.black))
            tv_matter_minutes.setTextColor(context_timesheets.resources.getColor(R.color.black))
            tv_matter_task_name.setTextColor(context_timesheets.resources.getColor(R.color.Primary_new))
            tv_matter_billable.setTextColor(context_timesheets.resources.getColor(R.color.Primary_new))
            total_hours_id.setTextColor(context_timesheets.resources.getColor(R.color.Primary_new))
        }
    }
}
