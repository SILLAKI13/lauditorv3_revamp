package com.digicoffer.lauditor.TimeSheets.Adapters

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.TimeSheets.Models.EventsModel
import com.digicoffer.lauditor.TimeSheets.Models.TaskModel
import com.digicoffer.lauditor.TimeSheets.Models.WeekModel
import com.digicoffer.lauditor.TimeSheets.Models.WeekTotalModel
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils
import org.json.JSONException
import java.util.ArrayList

class TimeSheetsAdapter(
    var weeksList: ArrayList<WeekModel>,
    var eventsList: ArrayList<EventsModel>,
    var weekTotalList: ArrayList<WeekTotalModel>,
    var context: Context,
    private val issubmitted: Boolean,
    var interfaceListener: WeeklyTSAdapter.InterfaceListener,
    var activity: Activity
) : RecyclerView.Adapter<TimeSheetsAdapter.MyViewHolder>() {

    var date = ""
    var matter_type: String? = null
    
    val monday = ArrayList<TaskModel>()
    val tuesday = ArrayList<TaskModel>()
    val wednessday = ArrayList<TaskModel>()
    val thursday = ArrayList<TaskModel>()
    val friday = ArrayList<TaskModel>()
    val saturday = ArrayList<TaskModel>()
    val sunday = ArrayList<TaskModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.timesheets_recyclerview, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val weekModel = weeksList[position]
        try {
            monday.clear()
            tuesday.clear()
            wednessday.clear()
            thursday.clear()
            friday.clear()
            saturday.clear()
            sunday.clear()
            
            for (e in 0 until eventsList.size) {
                val eventsModel = eventsList[e]
                matter_type = eventsModel.matter_type

                // Monday
                val monObj = eventsModel.mon
                if (monObj != null) {
                    val taskModel = TaskModel()
                    if (monObj.has("taskId")) {
                        taskModel.taskid = monObj.getString("taskId")
                    }
                    if (monObj.has("isLinkedWithCalendar")) {
                        taskModel.isLinkedWithCalendar = monObj.optBoolean("isLinkedWithCalendar")
                    }
                    if (monObj.has("matterId")) {
                        taskModel.matterid = monObj.getString("matterId")
                    }
                    taskModel.minutes = monObj.getString("minutes")
                    taskModel.hours = monObj.getString("hours")
                    taskModel.Task_name = eventsModel.taskName
                    if (eventsModel.billing != null) {
                        taskModel.Task_billing = eventsModel.billing
                    }
                    taskModel.Task_matter_name = eventsModel.matter_name
                    taskModel.is_editable = eventsModel.is_editable
                    taskModel.Task_matter_id = eventsModel.matter_id
                    if (monObj.has("permissions")) {
                        val permissions = monObj.getJSONObject("permissions")
                        taskModel.Permissions = permissions
                        taskModel.editProject = permissions.getBoolean("editProject")
                        taskModel.editTask = permissions.getBoolean("editTask")
                        taskModel.editStatus = permissions.getBoolean("editStatus")
                        taskModel.editDate = permissions.getBoolean("editDate")
                        taskModel.editHours = permissions.getBoolean("editHours")
                        taskModel.editMinutes = permissions.getBoolean("editMinutes")
                    }
                    if (!(taskModel.hours == "0" && taskModel.minutes == "0")) {
                        monday.add(taskModel)
                    }
                }

                // Tuesday
                val tueObj = eventsModel.tue
                if (tueObj != null) {
                    val taskModel_tue = TaskModel()
                    if (tueObj.has("taskId")) {
                        taskModel_tue.taskid = tueObj.getString("taskId")
                    }
                    if (tueObj.has("permissions")) {
                        val permissions = tueObj.getJSONObject("permissions")
                        taskModel_tue.Permissions = permissions
                        taskModel_tue.editProject = permissions.getBoolean("editProject")
                        taskModel_tue.editTask = permissions.getBoolean("editTask")
                        taskModel_tue.editStatus = permissions.getBoolean("editStatus")
                        taskModel_tue.editDate = permissions.getBoolean("editDate")
                        taskModel_tue.editHours = permissions.getBoolean("editHours")
                        taskModel_tue.editMinutes = permissions.getBoolean("editMinutes")
                    }
                    if (tueObj.has("isLinkedWithCalendar")) {
                        taskModel_tue.isLinkedWithCalendar = tueObj.optBoolean("isLinkedWithCalendar")
                    }
                    if (tueObj.has("matterId")) {
                        taskModel_tue.matterid = tueObj.getString("matterId")
                    }
                    taskModel_tue.minutes = tueObj.getString("minutes")
                    taskModel_tue.hours = tueObj.getString("hours")
                    taskModel_tue.Task_name = eventsModel.taskName
                    if (eventsModel.billing != null) {
                        taskModel_tue.Task_billing = eventsModel.billing
                    }
                    taskModel_tue.Task_matter_name = eventsModel.matter_name
                    taskModel_tue.is_editable = eventsModel.is_editable
                    taskModel_tue.Task_matter_id = eventsModel.matter_id
                    if (!(taskModel_tue.hours == "0" && taskModel_tue.minutes == "0")) {
                        tuesday.add(taskModel_tue)
                    }
                }

                // Wednesday
                val wedObj = eventsModel.wed
                if (wedObj != null) {
                    val taskModel_wed = TaskModel()
                    if (wedObj.has("taskId")) {
                        taskModel_wed.taskid = wedObj.getString("taskId")
                    }
                    if (wedObj.has("permissions")) {
                        val permissions = wedObj.getJSONObject("permissions")
                        taskModel_wed.Permissions = permissions
                        taskModel_wed.editProject = permissions.getBoolean("editProject")
                        taskModel_wed.editTask = permissions.getBoolean("editTask")
                        taskModel_wed.editStatus = permissions.getBoolean("editStatus")
                        taskModel_wed.editDate = permissions.getBoolean("editDate")
                        taskModel_wed.editHours = permissions.getBoolean("editHours")
                        taskModel_wed.editMinutes = permissions.getBoolean("editMinutes")
                    }
                    if (wedObj.has("isLinkedWithCalendar")) {
                        taskModel_wed.isLinkedWithCalendar = wedObj.optBoolean("isLinkedWithCalendar")
                    }
                    if (wedObj.has("matterId")) {
                        taskModel_wed.matterid = wedObj.getString("matterId")
                    }
                    taskModel_wed.minutes = wedObj.getString("minutes")
                    taskModel_wed.hours = wedObj.getString("hours")
                    taskModel_wed.Task_name = eventsModel.taskName
                    if (eventsModel.billing != null) {
                        taskModel_wed.Task_billing = eventsModel.billing
                    }
                    taskModel_wed.Task_matter_name = eventsModel.matter_name
                    taskModel_wed.is_editable = eventsModel.is_editable
                    taskModel_wed.Task_matter_id = eventsModel.matter_id
                    if (!(taskModel_wed.hours == "0" && taskModel_wed.minutes == "0")) {
                        wednessday.add(taskModel_wed)
                    }
                }

                // Thursday
                val thuObj = eventsModel.thu
                if (thuObj != null) {
                    val taskModel_thu = TaskModel()
                    if (thuObj.has("taskId")) {
                        taskModel_thu.taskid = thuObj.getString("taskId")
                    }
                    if (thuObj.has("permissions")) {
                        val permissions = thuObj.getJSONObject("permissions")
                        taskModel_thu.Permissions = permissions
                        taskModel_thu.editProject = permissions.getBoolean("editProject")
                        taskModel_thu.editTask = permissions.getBoolean("editTask")
                        taskModel_thu.editStatus = permissions.getBoolean("editStatus")
                        taskModel_thu.editDate = permissions.getBoolean("editDate")
                        taskModel_thu.editHours = permissions.getBoolean("editHours")
                        taskModel_thu.editMinutes = permissions.getBoolean("editMinutes")
                    }
                    if (thuObj.has("isLinkedWithCalendar")) {
                        taskModel_thu.isLinkedWithCalendar = thuObj.optBoolean("isLinkedWithCalendar")
                    }
                    if (thuObj.has("matterId")) {
                        taskModel_thu.matterid = thuObj.getString("matterId")
                    }
                    taskModel_thu.minutes = thuObj.getString("minutes")
                    taskModel_thu.hours = thuObj.getString("hours")
                    taskModel_thu.Task_name = eventsModel.taskName
                    if (eventsModel.billing != null) {
                        taskModel_thu.Task_billing = eventsModel.billing
                    }
                    taskModel_thu.Task_matter_name = eventsModel.matter_name
                    taskModel_thu.is_editable = eventsModel.is_editable
                    taskModel_thu.Task_matter_id = eventsModel.matter_id
                    if (!(taskModel_thu.hours == "0" && taskModel_thu.minutes == "0")) {
                        thursday.add(taskModel_thu)
                    }
                }

                // Friday
                val friObj = eventsModel.fri
                if (friObj != null) {
                    val taskModel_fri = TaskModel()
                    if (friObj.has("taskId")) {
                        taskModel_fri.taskid = friObj.getString("taskId")
                    }
                    if (friObj.has("permissions")) {
                        val permissions = friObj.getJSONObject("permissions")
                        taskModel_fri.Permissions = permissions
                        taskModel_fri.editProject = permissions.getBoolean("editProject")
                        taskModel_fri.editTask = permissions.getBoolean("editTask")
                        taskModel_fri.editStatus = permissions.getBoolean("editStatus")
                        taskModel_fri.editDate = permissions.getBoolean("editDate")
                        taskModel_fri.editHours = permissions.getBoolean("editHours")
                        taskModel_fri.editMinutes = permissions.getBoolean("editMinutes")
                    }
                    if (friObj.has("isLinkedWithCalendar")) {
                        taskModel_fri.isLinkedWithCalendar = friObj.optBoolean("isLinkedWithCalendar")
                    }
                    if (friObj.has("matterId")) {
                        taskModel_fri.matterid = friObj.getString("matterId")
                    }
                    taskModel_fri.minutes = friObj.getString("minutes")
                    taskModel_fri.hours = friObj.getString("hours")
                    taskModel_fri.Task_name = eventsModel.taskName
                    if (eventsModel.billing != null) {
                        taskModel_fri.Task_billing = eventsModel.billing
                    }
                    taskModel_fri.Task_matter_name = eventsModel.matter_name
                    taskModel_fri.is_editable = eventsModel.is_editable
                    taskModel_fri.Task_matter_id = eventsModel.matter_id
                    if (!(taskModel_fri.hours == "0" && taskModel_fri.minutes == "0")) {
                        friday.add(taskModel_fri)
                    }
                }

                // Saturday
                val satObj = eventsModel.sat
                if (satObj != null) {
                    val taskModel_sat = TaskModel()
                    if (satObj.has("taskId")) {
                        taskModel_sat.taskid = satObj.getString("taskId")
                    }
                    if (satObj.has("permissions")) {
                        val permissions = satObj.getJSONObject("permissions")
                        taskModel_sat.Permissions = permissions
                        taskModel_sat.editProject = permissions.getBoolean("editProject")
                        taskModel_sat.editTask = permissions.getBoolean("editTask")
                        taskModel_sat.editStatus = permissions.getBoolean("editStatus")
                        taskModel_sat.editDate = permissions.getBoolean("editDate")
                        taskModel_sat.editHours = permissions.getBoolean("editHours")
                        taskModel_sat.editMinutes = permissions.getBoolean("editMinutes")
                    }
                    if (satObj.has("isLinkedWithCalendar")) {
                        taskModel_sat.isLinkedWithCalendar = satObj.optBoolean("isLinkedWithCalendar")
                    }
                    if (satObj.has("matterId")) {
                        taskModel_sat.matterid = satObj.getString("matterId")
                    }
                    taskModel_sat.minutes = satObj.getString("minutes")
                    taskModel_sat.hours = satObj.getString("hours")
                    taskModel_sat.Task_name = eventsModel.taskName
                    if (eventsModel.billing != null) {
                        taskModel_sat.Task_billing = eventsModel.billing
                    }
                    taskModel_sat.Task_matter_name = eventsModel.matter_name
                    taskModel_sat.is_editable = eventsModel.is_editable
                    taskModel_sat.Task_matter_id = eventsModel.matter_id
                    if (!(taskModel_sat.hours == "0" && taskModel_sat.minutes == "0")) {
                        saturday.add(taskModel_sat)
                    }
                }

                // Sunday
                val sunObj = eventsModel.sun
                if (sunObj != null) {
                    val taskModel_sun = TaskModel()
                    if (sunObj.has("taskId")) {
                        taskModel_sun.taskid = sunObj.getString("taskId")
                    }
                    if (sunObj.has("permissions")) {
                        val permissions = sunObj.getJSONObject("permissions")
                        taskModel_sun.Permissions = permissions
                        taskModel_sun.editProject = permissions.getBoolean("editProject")
                        taskModel_sun.editTask = permissions.getBoolean("editTask")
                        taskModel_sun.editStatus = permissions.getBoolean("editStatus")
                        taskModel_sun.editDate = permissions.getBoolean("editDate")
                        taskModel_sun.editHours = permissions.getBoolean("editHours")
                        taskModel_sun.editMinutes = permissions.getBoolean("editMinutes")
                    }
                    if (sunObj.has("isLinkedWithCalendar")) {
                        taskModel_sun.isLinkedWithCalendar = sunObj.optBoolean("isLinkedWithCalendar")
                    }
                    if (sunObj.has("matterId")) {
                        taskModel_sun.matterid = sunObj.getString("matterId")
                    }
                    taskModel_sun.minutes = sunObj.getString("minutes")
                    taskModel_sun.hours = sunObj.getString("hours")
                    taskModel_sun.Task_name = eventsModel.taskName
                    if (eventsModel.billing != null) {
                        taskModel_sun.Task_billing = eventsModel.billing
                    }
                    taskModel_sun.Task_matter_name = eventsModel.matter_name
                    taskModel_sun.is_editable = eventsModel.is_editable
                    taskModel_sun.Task_matter_id = eventsModel.matter_id
                    if (!(taskModel_sun.hours == "0" && taskModel_sun.minutes == "0")) {
                        sunday.add(taskModel_sun)
                    }
                }
            }
        } catch (e: JSONException) {
            e.fillInStackTrace()
        }

        holder.tv_date.text = weekModel.value
        date = weekModel.value ?: ""

        val inputString = weekModel.value ?: ""
        val parts = inputString.split(" ")
        val dayOfWeek = if (parts.isNotEmpty()) parts[0] else ""

        for (i in 0 until weekTotalList.size) {
            val totalItem = weekTotalList[i]
            when (dayOfWeek) {
                "Mon" -> holder.tv_total_hours.text = "${totalItem.Mon} Hours"
                "Tue" -> holder.tv_total_hours.text = "${totalItem.Tue} Hours"
                "Wed" -> holder.tv_total_hours.text = "${totalItem.Wed} Hours"
                "Thu" -> holder.tv_total_hours.text = "${totalItem.Thu} Hours"
                "Fri" -> holder.tv_total_hours.text = "${totalItem.Fri} Hours"
                "Sat" -> holder.tv_total_hours.text = "${totalItem.Sat} Hours"
                "Sun" -> holder.tv_total_hours.text = "${totalItem.Sun} Hours"
            }
        }

        when (dayOfWeek) {
            "Mon" -> {
                if (monday.isNotEmpty()) {
                    loadRecyclerview(holder, monday, issubmitted)
                    holder.timesheet_layout.visibility = View.VISIBLE
                } else {
                    holder.timesheet_layout.visibility = View.GONE
                }
            }
            "Tue" -> {
                if (tuesday.isNotEmpty()) {
                    loadRecyclerview(holder, tuesday, issubmitted)
                    holder.timesheet_layout.visibility = View.VISIBLE
                } else {
                    holder.timesheet_layout.visibility = View.GONE
                }
            }
            "Wed" -> {
                if (wednessday.isNotEmpty()) {
                    loadRecyclerview(holder, wednessday, issubmitted)
                    holder.timesheet_layout.visibility = View.VISIBLE
                } else {
                    holder.timesheet_layout.visibility = View.GONE
                }
            }
            "Thu" -> {
                if (thursday.isNotEmpty()) {
                    loadRecyclerview(holder, thursday, issubmitted)
                    holder.timesheet_layout.visibility = View.VISIBLE
                } else {
                    holder.timesheet_layout.visibility = View.GONE
                }
            }
            "Fri" -> {
                if (friday.isNotEmpty()) {
                    loadRecyclerview(holder, friday, issubmitted)
                    holder.timesheet_layout.visibility = View.VISIBLE
                } else {
                    holder.timesheet_layout.visibility = View.GONE
                }
            }
            "Sat" -> {
                if (saturday.isNotEmpty()) {
                    loadRecyclerview(holder, saturday, issubmitted)
                    holder.timesheet_layout.visibility = View.VISIBLE
                } else {
                    holder.timesheet_layout.visibility = View.GONE
                }
            }
            "Sun" -> {
                if (sunday.isNotEmpty()) {
                    loadRecyclerview(holder, sunday, issubmitted)
                    holder.timesheet_layout.visibility = View.VISIBLE
                } else {
                    holder.timesheet_layout.visibility = View.GONE
                }
            }
        }
    }

    private fun loadRecyclerview(holder: MyViewHolder, list: ArrayList<TaskModel>, issubmitted: Boolean) {
        holder.rv_time_sheets.layoutManager = GridLayoutManager(context, 1)
        val weeklyTSAdapter = WeeklyTSAdapter(list, issubmitted, context, interfaceListener, date, matter_type ?: "")
        holder.rv_time_sheets.adapter = weeklyTSAdapter
        if (weeklyTSAdapter.itemCount > 0) {
            val lastPosition = weeklyTSAdapter.itemCount - 1
            holder.rv_time_sheets.smoothScrollToPosition(lastPosition)
        }
        weeklyTSAdapter.notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return weeksList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tv_date: TextView = itemView.findViewById(R.id.tv_date)
        var tv_total_hours: TextView = itemView.findViewById(R.id.tv_total_hours)
        var total_hours_id: TextView = itemView.findViewById(R.id.total_hours_id)
        var timesheet_layout: LinearLayoutCompat = itemView.findViewById(R.id.ll_documents)
        var rv_time_sheets: RecyclerView = itemView.findViewById(R.id.rv_time_sheets)

        init {
            total_hours_id.textSize = DynamicUtils.twenty.toFloat()
            tv_total_hours.textSize = DynamicUtils.twenty.toFloat()
            tv_date.setTextColor(context.getColor(R.color.Primary_new))
            total_hours_id.setTextColor(context.getColor(R.color.Primary_new))
            total_hours_id.setText(R.string.total_hours)
            tv_total_hours.setTextColor(context.getColor(R.color.black))
        }
    }
}
