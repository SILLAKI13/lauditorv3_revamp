package com.digicoffer.lauditor.TimeSheets.ViewModels

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.TimeSheets.Adapters.TimeSheetsAdapter
import com.digicoffer.lauditor.TimeSheets.Adapters.WeeklyTSAdapter
import com.digicoffer.lauditor.TimeSheets.Models.EventsModel
import com.digicoffer.lauditor.TimeSheets.Models.StatusModel
import com.digicoffer.lauditor.TimeSheets.Models.TSMatterModel
import com.digicoffer.lauditor.TimeSheets.Models.TaskModel
import com.digicoffer.lauditor.TimeSheets.Models.TasksModel
import com.digicoffer.lauditor.TimeSheets.Models.TimeSheetModel
import com.digicoffer.lauditor.TimeSheets.Models.WeekModel
import com.digicoffer.lauditor.TimeSheets.Models.WeekTotalModel
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale

class SubmittedTimeSheets : Fragment(), AsyncTaskCompleteListener, View.OnClickListener {
    private var view: View? = null
    var date = ""
    var issubmitted = true
    var current_date = ""
    private var cv_details_activity_log: CardView? = null
    private var progressDialog: Dialog? = null
    private val timeSheetsList = ArrayList<TimeSheetModel>()
    private val matterList = ArrayList<TSMatterModel>()
    private val tasksList = ArrayList<TasksModel>()
    private val eventsList = ArrayList<EventsModel>()
    private val statusList = ArrayList<StatusModel>()
    private val weektotalList = ArrayList<WeekTotalModel>()
    private val weeksList = ArrayList<WeekModel>()
    var rv_submitted_timesheets: RecyclerView? = null

    var layout_empty_state: LinearLayout? = null
    var tv_empty_subtitle: TextView? = null
    var tv_title: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.submitted_timesheets, container, false)
        this.view = view
        val bundle = arguments
        if (bundle != null) {
            date = bundle.getString("date") ?: ""
            val weekDates = bundle.getStringArrayList("weekDates")
            if (weekDates != null && weekDates.isNotEmpty()) {
                current_date = weekDates[0]

                val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
                val outputFormat = SimpleDateFormat("EEE MMM d,yyyy", Locale.ENGLISH)
                val formattedDates = ArrayList<String>()

                for (dateStr in weekDates) {
                    try {
                        val d1 = inputFormat.parse(dateStr)
                        if (d1 != null) {
                            formattedDates.add(outputFormat.format(d1))
                        }
                    } catch (e: ParseException) {
                        e.fillInStackTrace()
                    }
                }
                for (value in formattedDates) {
                    weeksList.add(WeekModel(value))
                }
            }
        }

        rv_submitted_timesheets = view.findViewById(R.id.rv_submitted_timesheets)
        layout_empty_state = view.findViewById(R.id.layout_empty_state)
        tv_empty_subtitle = view.findViewById(R.id.tv_empty_subtitle)
        tv_title = view.findViewById(R.id.tv_title)

        if (date.isEmpty()) {
            callCurrentDateTimeSheetsWebservice(date)
        } else {
            callTimeSheetsWebservice(date)
        }

        return view
    }

    private fun callCurrentDateTimeSheetsWebservice(date: String?) {
        try {
            clearList()
            progressDialog = AndroidUtils.get_progress(requireActivity())
            val data = JSONObject()
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v3/user/timesheets?submitted=true",
                "TimeSheets",
                data.toString()
            )
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun callTimeSheetsWebservice(date: String?) {
        try {
            clearList()
            progressDialog = AndroidUtils.get_progress(requireActivity())
            val data = JSONObject()
            val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
            val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
            val new_date = inputFormat.parse(date!!)
            val outputDate = outputFormat.format(new_date!!)
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v3/user/timesheets/$outputDate?submitted=true",
                "TimeSheets",
                data.toString()
            )
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun clearList() {
        timeSheetsList.clear()
        weektotalList.clear()
        eventsList.clear()
        matterList.clear()
        statusList.clear()
        tasksList.clear()
    }

    override fun onClick(view: View) {}

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        if (progressDialog != null && progressDialog!!.isShowing) {
            AndroidUtils.dismiss_dialog(progressDialog)
        }
        if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                val result = JSONObject(httpResult.responseContent)
                if (httpResult.requestType == "TimeSheets") {
                    val dates = result.getJSONObject("dates")
                    timeSheetsList.clear()
                    loadTimesheetData(dates, result)
                }
            } catch (e: Exception) {
                e.fillInStackTrace()
            }
        } else if (httpResult.result == WebServiceHelper.ServiceCallStatus.Failed) {
            if (progressDialog != null && progressDialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progressDialog)
            }
            try {
                val result = JSONObject(httpResult.responseContent)
                AndroidUtils.showErrorAlert(result.optString("msg"), activity)
            } catch (e: Exception) {
                e.fillInStackTrace()
            }
        } else {
            if (progressDialog != null && progressDialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progressDialog)
            }
            AndroidUtils.showErrorAlert(httpResult.responseContent.toString(), activity)
        }
    }

    private fun loadTimesheetData(dates: JSONObject, result: JSONObject) {
        timeSheetsList.clear()
        val timeSheetModel = TimeSheetModel()
        timeSheetModel.isFrozen = dates.getBoolean("isFrozen")
        timeSheetModel.currentWeek = dates.getString("currentWeek")
        timeSheetModel.nextWeek = dates.getString("nextWeek")
        timeSheetModel.prevWeek = dates.getString("prevWeek")
        val timesheets = result.getJSONObject("timesheetList")

        val matters = timesheets.getJSONArray("matters")
        val weektotal = timesheets.getJSONObject("weekTotal")
        val weekModel = WeekTotalModel().apply {
            Mon = weektotal.getString("Mon")
            Tue = weektotal.getString("Tue")
            Wed = weektotal.getString("Wed")
            Thu = weektotal.getString("Thu")
            Fri = weektotal.getString("Fri")
            Sat = weektotal.getString("Sat")
            Sun = weektotal.getString("Sun")
        }
        weektotalList.add(weekModel)

        for (i in 0 until matters.length()) {
            val jsonObject = matters.getJSONObject(i)
            val matterModel = TSMatterModel().apply {
                mattername = jsonObject.getString("matterName")
                matterid = jsonObject.getString("matterId")
                Tasks = jsonObject.getJSONArray("tasks")
                if (jsonObject.has("matterType")) {
                    matter_type = jsonObject.getString("matterType")
                }
            }
            matterList.add(matterModel)
        }

        timeSheetsList.add(timeSheetModel)

        for (j in 0 until matterList.size) {
            val tasks = matterList[j].Tasks
            if (tasks != null) {
                for (m in 0 until tasks.length()) {
                    val jsonObject = tasks.getJSONObject(m)
                    val eventsModel = EventsModel()
                    if (jsonObject.has("billing")) {
                        eventsModel.billing = jsonObject.getString("billing")
                    }
                    eventsModel.taskName = jsonObject.getString("taskName")
                    eventsModel.total = jsonObject.getString("total")
                    eventsModel.mon = jsonObject.getJSONObject("Mon")
                    eventsModel.tue = jsonObject.getJSONObject("Tue")
                    eventsModel.wed = jsonObject.getJSONObject("Wed")
                    eventsModel.thu = jsonObject.getJSONObject("Thu")
                    eventsModel.fri = jsonObject.getJSONObject("Fri")
                    eventsModel.sat = jsonObject.getJSONObject("Sat")
                    eventsModel.sun = jsonObject.getJSONObject("Sun")
                    eventsModel.matter_id = matterList[j].matterid
                    eventsModel.matter_name = matterList[j].mattername
                    eventsList.add(eventsModel)
                }
            }
        }

        if (timeSheetModel.isFrozen) {
            updateEmptyState()
            loadTimesheetsRecyclerview()
        } else {
            showEmptyState("Timesheets have not been submitted yet.")
        }
    }

    private fun updateEmptyState() {
        if (eventsList.isEmpty()) {
            layout_empty_state?.visibility = View.VISIBLE
            rv_submitted_timesheets?.visibility = View.GONE
            tv_title?.text = "Timesheet Not Submitted PLease Select Other Week"
            tv_empty_subtitle?.visibility = View.GONE
        } else {
            layout_empty_state?.visibility = View.GONE
            rv_submitted_timesheets?.visibility = View.VISIBLE
        }
    }

    private fun showEmptyState(message: String) {
        layout_empty_state?.visibility = View.VISIBLE
        rv_submitted_timesheets?.visibility = View.GONE
        tv_empty_subtitle?.text = message
    }

    private fun loadTimesheetsRecyclerview() {
        val timeSheetsAdapter = TimeSheetsAdapter(
            weeksList,
            eventsList,
            weektotalList,
            requireContext(),
            issubmitted,
            object : WeeklyTSAdapter.InterfaceListener {
                override fun DeleteEvent_Timesheet(taskModel: TaskModel) {}
                override fun EditEvent_Timesheet(eventsModels: TaskModel, date: String, matter_type: String) {}
            },
            requireActivity()
        )
        rv_submitted_timesheets?.adapter = timeSheetsAdapter
        AndroidUtils.LoadingRecyclerview(rv_submitted_timesheets, context)
        AndroidUtils.setupBottomSpacerFooter(
            rv_submitted_timesheets,
            resources.getDimensionPixelSize(R.dimen.twentyeight_dp)
        )
        if (timeSheetsAdapter.itemCount > 0) {
            val lastPosition = timeSheetsAdapter.itemCount - 1
            rv_submitted_timesheets?.smoothScrollToPosition(lastPosition)
        }
    }
}
