package com.digicoffer.lauditor.TimeSheets.ViewModels

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
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
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale
import java.util.Objects

class NonSubmittedTimesheets @JvmOverloads constructor(
    var timesheet_eventsModels: ArrayList<TaskModel>? = null
) : Fragment(), AsyncTaskCompleteListener, View.OnClickListener, WeeklyTSAdapter.InterfaceListener {

    private var progressDialog: Dialog? = null
    private var task_layout: LinearLayout? = null
    private var timeSheets: TimeSheets? = null
    var hasTimesheets = false
    var isEdit = false
    var weekTotal = ""
    var timesheet_update_scope = "UPDATE_TIMESHEET_ONLY"
    var isLinkedWithCalendar: Boolean? = false
    var isfrozen = false
    private val timeSheetsList = ArrayList<TimeSheetModel>()
    private val matterList = ArrayList<TSMatterModel>()
    private val activeProjectsList = ArrayList<TSMatterModel>()
    private val tasksList = ArrayList<TasksModel>()
    private val eventsList = ArrayList<EventsModel>()
    private val statusList = ArrayList<StatusModel>()
    private val weektotalList = ArrayList<WeekTotalModel>()
    private val weeksList = ArrayList<WeekModel>()
    private var selected_matter = ""
    private var selected_matter_id = ""
    private var selected_matter_type = ""
    var current_date = ""
    var matter_name: String? = null
    private var isMatterTypeExists = false
    private var selected_task = ""
    private var selected_status = ""
    private var date_status = false
    private var selected_date = ""
    private var sp_project: ListView? = null
    private var sp_task: ListView? = null
    private var sp_status: ListView? = null
    private var sp_date: ListView? = null
    private var tv_hours: TextInputEditText? = null
    private var rv_non_submitted_timesheets: RecyclerView? = null
    private var btn_cancel_timesheet: AppCompatButton? = null
    private var btn_save_timesheet: AppCompatButton? = null
    var btn_submit_timesheet: AppCompatButton? = null
    var bt_fifteen_minutes: AppCompatButton? = null
    var bt_thirty_minutes: AppCompatButton? = null
    var bt_forty_five_minutes: AppCompatButton? = null
    private var hoursString: String? = null
    private var minutesString: String? = null
    var img_clear_icon1: ImageView? = null
    var img_dropdown_icon1: ImageView? = null
    var img_clear_icon2: ImageView? = null
    var img_dropdown_icon2: ImageView? = null
    var img_clear_icon3: ImageView? = null
    var img_dropdown_icon3: ImageView? = null
    var img_clear_icon4: ImageView? = null
    var img_dropdown_icon4: ImageView? = null
    private var project_id: TextView? = null
    private var tv_sp_project: TextView? = null
    private var task_id: TextView? = null
    private var tv_sp_task: TextView? = null
    private var status_id: TextView? = null
    private var tv_sp_status: TextView? = null
    private var date_id: TextView? = null
    private var tv_sp_date: TextView? = null
    private var hours_id: TextView? = null
    private var minutes_id: TextView? = null
    private var tot_hours_id: TextView? = null
    private var tv_total_hours: TextView? = null
    var date = ""
    var scrollView: NestedScrollView? = null
    var ll_minutes: LinearLayout? = null
    var project_view: LinearLayout? = null
    var task_view: LinearLayout? = null
    var status_view: LinearLayout? = null
    var date_view: LinearLayout? = null
    var event_name = ""
    var status = ""
    var task_name = ""
    var chosen_date = ""
    var hours = ""
    var minutes = ""
    var event_task_id: String? = null
    var event_id: String? = null
    private var issubmitted = false
    private var ischecked_project = true
    private var ischecked_status = true
    private var ischecked_task = true
    private var ischecked_date = true

    private var projectSpinnerAdapter: CommonSpinnerAdapter<TSMatterModel>? = null
    private var taskSpinnerAdapter: CommonSpinnerAdapter<TasksModel>? = null
    private var statusSpinnerAdapter: CommonSpinnerAdapter<StatusModel>? = null
    private var dateSpinnerAdapter: CommonSpinnerAdapter<String>? = null
    var layout_empty_state: LinearLayout? = null
    var tv_empty_subtitle: TextView? = null

    @SuppressLint("WrongViewCast")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.not_submitted_timesheet, container, false)
        sp_project = view.findViewById(R.id.sp_project)
        scrollView = view.findViewById(R.id.scrollView)
        ll_minutes = view.findViewById(R.id.ll_minutes)
        sp_project?.visibility = View.GONE
        timeSheets = parentFragment as? TimeSheets
        sp_task = view.findViewById(R.id.sp_task)
        sp_task?.visibility = View.GONE
        sp_status = view.findViewById(R.id.sp_status)
        sp_status?.visibility = View.GONE
        sp_date = view.findViewById(R.id.sp_date)
        sp_date?.visibility = View.GONE
        rv_non_submitted_timesheets = view.findViewById(R.id.rv_non_submitted_timesheets)
        tv_hours = view.findViewById(R.id.tv_hours)
        tv_hours?.setInputType(InputType.TYPE_CLASS_NUMBER)
        tv_hours?.setHint(R.string.hours)
        tv_total_hours = view.findViewById(R.id.tv_total_hours)
        tv_total_hours?.setHint(R.string.total__)
        btn_cancel_timesheet = view.findViewById(R.id.btn_cancel_timesheet)
        btn_submit_timesheet = view.findViewById(R.id.btn_submit_timesheet)
        btn_submit_timesheet?.setText(R.string.submit)
        btn_submit_timesheet?.visibility = View.GONE
        project_id = view.findViewById(R.id.project_id)
        project_id?.setText(R.string.project)

        layout_empty_state = view.findViewById(R.id.layout_empty_state)
        tv_empty_subtitle = view.findViewById(R.id.tv_empty_subtitle)

        project_view = view.findViewById(R.id.sp_project_layout)
        tv_sp_project = project_view?.findViewById(R.id.tv_spinner_view)
        img_clear_icon1 = project_view?.findViewById(R.id.img_clear_icon)
        img_dropdown_icon1 = project_view?.findViewById(R.id.img_dropdown_icon)
        task_view = view.findViewById(R.id.sp_task_layout)
        tv_sp_task = task_view?.findViewById(R.id.tv_spinner_view)
        img_clear_icon2 = task_view?.findViewById(R.id.img_clear_icon)
        img_dropdown_icon2 = task_view?.findViewById(R.id.img_dropdown_icon)
        status_view = view.findViewById(R.id.sp_status_layout)
        tv_sp_status = status_view?.findViewById(R.id.tv_spinner_view)
        img_clear_icon3 = status_view?.findViewById(R.id.img_clear_icon)
        img_dropdown_icon3 = status_view?.findViewById(R.id.img_dropdown_icon)
        date_view = view.findViewById(R.id.sp_date_layout)
        tv_sp_date = date_view?.findViewById(R.id.tv_spinner_view)
        img_clear_icon4 = date_view?.findViewById(R.id.img_clear_icon)
        img_dropdown_icon4 = date_view?.findViewById(R.id.img_dropdown_icon)
        task_id = view.findViewById(R.id.task_id)
        task_id?.setText(R.string.task)
        task_layout = view.findViewById(R.id.task_layout)
        task_layout?.visibility = View.GONE
        status_id = view.findViewById(R.id.status_id)
        status_id?.setText(R.string.status)
        date_id = view.findViewById(R.id.date_id)
        date_id?.setText(R.string.date)
        hours_id = view.findViewById(R.id.hours_id)
        hours_id?.setText(R.string.hours)
        minutes_id = view.findViewById(R.id.minutes_id)
        minutes_id?.setText(R.string.minutes)
        tot_hours_id = view.findViewById(R.id.tot_hours_id)
        tot_hours_id?.setText(R.string.total_hours)
        btn_save_timesheet = view.findViewById(R.id.btn_save_timesheet)

        val bundle = arguments
        if (bundle != null) {
            date = bundle.getString("date") ?: ""
            val weekDates = bundle.getStringArrayList("weekDates")
            if (weekDates != null && weekDates.isNotEmpty()) {
                current_date = weekDates[0]

                dateSpinnerAdapter = CommonSpinnerAdapter(requireActivity(), weekDates)
                sp_date?.adapter = dateSpinnerAdapter
                AndroidUtils.LoadList(sp_date, requireContext(), weekDates.size, true)
                sp_date?.setOnItemClickListener { parent, _, position, _ ->
                    val selectedItem = parent.getItemAtPosition(position) as String
                    selected_date = selectedItem
                    AndroidUtils.DisplaySpinnerView(
                        sp_date,
                        tv_sp_date,
                        selected_date,
                        img_dropdown_icon4,
                        img_clear_icon4,
                        false,
                        dateSpinnerAdapter,
                        "Search Date"
                    )
                    ischecked_date = true
                }

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

        bt_thirty_minutes = view.findViewById(R.id.bt_thirty_minutes)
        bt_forty_five_minutes = view.findViewById(R.id.bt_forty_five_minutes)
        bt_fifteen_minutes = view.findViewById(R.id.bt_fifteen_minutes)
        bt_fifteen_minutes?.setOnClickListener(minutesButtonClickListener)
        bt_thirty_minutes?.setOnClickListener(minutesButtonClickListener)
        bt_forty_five_minutes?.setOnClickListener(minutesButtonClickListener)

        project_view?.setOnClickListener {
            if (!activeProjectsList.isEmpty()) {
                val isVisible = sp_project?.visibility == View.VISIBLE
                AndroidUtils.DisplaySpinnerView(
                    sp_project,
                    tv_sp_project,
                    tv_sp_project?.text.toString(),
                    img_dropdown_icon1,
                    img_clear_icon1,
                    !isVisible,
                    projectSpinnerAdapter,
                    "Search Project"
                )
            }
        }
        task_view?.setOnClickListener {
            if (!tasksList.isEmpty()) {
                val isVisible = sp_task?.visibility == View.VISIBLE
                AndroidUtils.DisplaySpinnerView(
                    sp_task,
                    tv_sp_task,
                    tv_sp_task?.text.toString(),
                    img_dropdown_icon2,
                    img_clear_icon2,
                    !isVisible,
                    taskSpinnerAdapter,
                    "Search Task"
                )
            }
        }
        btn_cancel_timesheet?.setOnClickListener {
            clear_date()
        }
        status_view?.setOnClickListener {
            if (!statusList.isEmpty()) {
                val isVisible = sp_status?.visibility == View.VISIBLE
                AndroidUtils.DisplaySpinnerView(
                    sp_status,
                    tv_sp_status,
                    tv_sp_status?.text.toString(),
                    img_dropdown_icon3,
                    img_clear_icon3,
                    !isVisible,
                    statusSpinnerAdapter,
                    "Search Status"
                )
            }
        }
        date_view?.setOnClickListener {
            val isVisible = sp_date?.visibility == View.VISIBLE
            AndroidUtils.DisplaySpinnerView(
                sp_date,
                tv_sp_date,
                tv_sp_date?.text.toString(),
                img_dropdown_icon4,
                img_clear_icon4,
                !isVisible,
                dateSpinnerAdapter,
                "Search Date"
            )
        }
        btn_save_timesheet?.setOnClickListener {
            if (!Constants.is_active) {
                AndroidUtils.showRenewalPopup(requireActivity())
            } else {
                var msg = "Please enter the"
                if (tv_sp_project?.text.toString().trim().isEmpty() ||
                    tv_sp_task?.text.toString().isEmpty() ||
                    tv_sp_status?.text.toString().isEmpty() ||
                    tv_total_hours?.text.toString().trim().isEmpty() ||
                    tv_total_hours?.text.toString() == "00:00" ||
                    tv_sp_date?.text.toString().trim().isEmpty()
                ) {
                    if (tv_sp_project?.text.toString().isEmpty()) {
                        msg += " Projects"
                    }
                    if (tv_sp_task?.text.toString().isEmpty()) {
                        msg += if (msg == "Please enter the") " Task" else ", Task"
                    }
                    if (tv_sp_status?.text.toString().isEmpty()) {
                        msg += if (msg == "Please enter the") " Status" else ", Status"
                    }
                    if (tv_sp_date?.text.toString().isEmpty()) {
                        msg += if (msg == "Please enter the") " Date" else ", Date"
                    }
                    if (tv_total_hours?.text.toString().trim().isEmpty() || tv_total_hours?.text.toString() == "00:00") {
                        msg += if (msg == "Please enter the") " Hours" else ", Hours"
                    }
                    AndroidUtils.showAlert(msg, timeSheets?.activity)
                } else {
                    try {
                        if (isEdit) {
                            if (isLinkedWithCalendar == true) {
                                AndroidUtils.showConfirmationDialog(
                                    requireContext(),
                                    "Confirmation",
                                    "This timesheet has an associated event entry. Do you want to update the event too?",
                                    requireContext().getString(R.string.update_both),
                                    requireContext().getString(R.string.update_timesheet_only),
                                    object : AndroidUtils.OnConfirmListener {
                                        override fun onSave() {
                                            timesheet_update_scope = "UPDATE_BOTH"
                                            callEditTimesheetwebservice()
                                        }

                                        override fun onCancel() {
                                            timesheet_update_scope = "UPDATE_TIMESHEET_ONLY"
                                            callEditTimesheetwebservice()
                                        }
                                    }
                                )
                            } else {
                                timesheet_update_scope = "UPDATE_TIMESHEET_ONLY"
                                callEditTimesheetwebservice()
                            }
                        } else {
                            callSaveTimeSheetWebservice()
                        }
                    } catch (e: Exception) {
                        e.fillInStackTrace()
                    }
                }
            }
        }
        tv_hours?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val hoursText = s.toString()
                if (hoursText.isNotEmpty()) {
                    val hVal = hoursText.toInt()
                    if (hVal > 24) {
                        tv_hours?.removeTextChangedListener(this)
                        tv_hours?.setText("24")
                        tv_hours?.setSelection(tv_hours?.text?.length ?: 0)
                        tv_hours?.addTextChangedListener(this)
                    } else if (hVal == 24) {
                        minutesString = "00"
                        deselectAllMinuteButtons()
                        updateTotalTime()
                    }
                }
                updateTotalTime()
            }
        })

        if (date.isEmpty()) {
            callCurrentDateTimeSheetsWebservice(date, date_status)
        } else {
            callTimeSheetsWebservice(date)
        }

        btn_submit_timesheet?.setOnClickListener {
            callSubmitTimeSheetWebService()
        }

        img_clear_icon1?.setOnClickListener {
            if (!tv_sp_project?.text.toString().isEmpty()) {
                AndroidUtils.DisplaySpinnerView(
                    sp_project,
                    tv_sp_project,
                    selected_matter,
                    img_dropdown_icon1,
                    img_clear_icon1,
                    false,
                    projectSpinnerAdapter,
                    "Search Project"
                )
                ischecked_project = true
                clearDetails()
            }
        }
        img_clear_icon2?.setOnClickListener {
            if (!tv_sp_task?.text.toString().isEmpty()) {
                AndroidUtils.DisplaySpinnerView(
                    sp_task,
                    tv_sp_task,
                    selected_task,
                    img_dropdown_icon2,
                    img_clear_icon2,
                    false,
                    taskSpinnerAdapter,
                    "Search Task"
                )
                ischecked_task = true
            }
        }
        img_clear_icon3?.setOnClickListener {
            if (!tv_sp_status?.text.toString().isEmpty()) {
                AndroidUtils.DisplaySpinnerView(
                    sp_status,
                    tv_sp_status,
                    selected_status,
                    img_dropdown_icon3,
                    img_clear_icon3,
                    false,
                    statusSpinnerAdapter,
                    "Search Status"
                )
                ischecked_status = true
            }
        }
        img_clear_icon4?.setOnClickListener {
            AndroidUtils.DisplaySpinnerView(
                sp_date,
                tv_sp_date,
                selected_date,
                img_dropdown_icon4,
                img_clear_icon4,
                false,
                dateSpinnerAdapter,
                "Search Date"
            )
            ischecked_date = true
        }

        if (!Constants.is_active) {
            AndroidUtils.showRenewalPopup(requireActivity())
        }
        return view
    }

    private fun updateEmptyState() {
        if (eventsList.isEmpty()) {
            layout_empty_state?.visibility = View.VISIBLE
            rv_non_submitted_timesheets?.visibility = View.GONE
            btn_submit_timesheet?.visibility = View.GONE
            tv_empty_subtitle?.text = "Add tasks or track time to begin."
        } else {
            layout_empty_state?.visibility = View.GONE
            rv_non_submitted_timesheets?.visibility = View.VISIBLE
        }
    }

    private fun callDeleteTimesheetwebservice(timesheet_id: String, timesheet_delete_scope: String) {
        try {
            progressDialog = AndroidUtils.get_progress(requireActivity())
            val postdata = JSONObject()
            postdata.put("id", timesheet_id)
            postdata.put("timesheet_delete_scope", timesheet_delete_scope)
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.DELETE,
                "v3/user/timesheets",
                "Delete TimeSheet",
                postdata.toString()
            )
        } catch (e: Exception) {
            if (progressDialog != null && progressDialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progressDialog)
            }
        }
    }

    private fun callSubmitTimeSheetWebService() {
        try {
            progressDialog = AndroidUtils.get_progress(requireActivity())
            val postdata = JSONObject()
            val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
            val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
            val new_date = inputFormat.parse(date)
            if (new_date != null) {
                val outputDate = outputFormat.format(new_date)
                WebServiceHelper.callHttpWebService(
                    this,
                    requireContext(),
                    WebServiceHelper.RestMethodType.POST,
                    "v3/user/timesheets/freeze/$outputDate",
                    "Submit TimeSheet",
                    postdata.toString()
                )
            }
        } catch (e: Exception) {
            if (progressDialog != null && progressDialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progressDialog)
            }
        }
    }

    private fun callEditTimesheetwebservice() {
        try {
            progressDialog = AndroidUtils.get_progress(requireActivity())
            val postdata = JSONObject()
            postdata.put("id", event_name)
            postdata.put("action", "hours")
            postdata.put("billing", selected_status)
            val inputformat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
            val outputformat = SimpleDateFormat("MMM d, yyyy", Locale.US)
            try {
                val dVal = inputformat.parse(selected_date)
                if (dVal != null) {
                    val formattedDate = outputformat.format(dVal)
                    postdata.put("date", formattedDate)
                }
            } catch (e: Exception) {
                e.fillInStackTrace()
            }
            postdata.put("duration_hours", hoursString)
            if (minutesString == "0") {
                postdata.put("duration_minutes", "00")
            } else {
                postdata.put("duration_minutes", minutesString)
            }
            postdata.put("matter_id", event_id)
            postdata.put("timesheet_update_scope", timesheet_update_scope)
            postdata.put("matter_type", matter_name)
            postdata.put("title", task_name)
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.PUT,
                "v3/user/timesheets",
                "Edit Timesheet",
                postdata.toString()
            )
            Log.d("Edit_Timesheet", postdata.toString())
        } catch (e: Exception) {
            if (progressDialog != null && progressDialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progressDialog)
            }
            e.fillInStackTrace()
        }
    }

    private fun callSaveTimeSheetWebservice() {
        try {
            val postdata = JSONObject()
            postdata.put("action", "hours")
            postdata.put("billing", selected_status.lowercase(Locale.ROOT))
            val inputformat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
            val outputformat = SimpleDateFormat("MMM d, yyyy", Locale.US)
            try {
                val dVal = inputformat.parse(selected_date)
                if (dVal != null) {
                    val formattedDate = outputformat.format(dVal)
                    postdata.put("date", formattedDate)
                }
            } catch (e: Exception) {
                e.fillInStackTrace()
            }
            postdata.put("duration_hours", hoursString)
            if (minutesString == "0") {
                postdata.put("duration_minutes", "00")
            } else {
                postdata.put("duration_minutes", minutesString)
            }
            postdata.put("matter_id", selected_matter_id)
            if (isMatterTypeExists) {
                postdata.put("matter_type", selected_matter_type)
            } else {
                postdata.put("matter_type", selected_matter)
            }
            postdata.put("title", selected_task)
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.POST,
                "v3/user/timesheets",
                "SAVE TIMESHEETS",
                postdata.toString()
            )
        } catch (e: Exception) {
            if (progressDialog != null && progressDialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progressDialog)
            }
            e.fillInStackTrace()
        }
    }

    var minutesButtonClickListener = View.OnClickListener { v ->
        val hoursText = tv_hours?.text.toString()
        var hVal = 0
        if (!hoursText.isEmpty()) {
            hVal = hoursText.toInt()
        }
        hoursString = hoursText
        if (hVal >= 24) {
            return@OnClickListener
        }
        val button = v as AppCompatButton
        val isCurrentlySelected = button.isSelected
        deselectAllMinuteButtons()
        if (!isCurrentlySelected) {
            button.isSelected = true
        }
        updateTotalTime()
    }

    private fun deselectAllMinuteButtons() {
        bt_fifteen_minutes?.isSelected = false
        bt_thirty_minutes?.isSelected = false
        bt_forty_five_minutes?.isSelected = false
    }

    private fun updateTotalTime() {
        val hoursText = tv_hours?.text.toString()
        val hVal = if (hoursText.isNotEmpty()) hoursText.toInt() else 0
        hoursString = hoursText
        var minutes = 0
        if (bt_fifteen_minutes?.isSelected == true) {
            minutes = 15
        } else if (bt_thirty_minutes?.isSelected == true) {
            minutes = 30
        } else if (bt_forty_five_minutes?.isSelected == true) {
            minutes = 45
        }
        minutesString = minutes.toString()
        selected_minutes(minutesString)
        val totalTimeText = String.format(Locale.US, "%02d:%02d", hVal, minutes)
        tv_total_hours?.text = totalTimeText
    }

    private fun clearSelectedMinutes() {
        val context = context ?: return
        bt_fifteen_minutes?.setBackgroundDrawable(context.getDrawable(R.drawable.rectangle_light_grey))
        bt_thirty_minutes?.setBackgroundDrawable(context.getDrawable(R.drawable.rectangle_light_grey))
        bt_forty_five_minutes?.setBackgroundDrawable(context.getDrawable(R.drawable.rectangle_light_grey))
        bt_fifteen_minutes?.setTextColor(Color.BLACK)
        bt_thirty_minutes?.setTextColor(Color.BLACK)
        bt_forty_five_minutes?.setTextColor(Color.BLACK)
    }

    private fun selected_minutes(minutes: String?) {
        val context = context ?: return
        when (minutes) {
            "15" -> {
                clearSelectedMinutes()
                bt_fifteen_minutes?.setBackgroundDrawable(context.getDrawable(R.drawable.rectangular_button_green_count))
                bt_fifteen_minutes?.setTextColor(Color.WHITE)
            }
            "30" -> {
                clearSelectedMinutes()
                bt_thirty_minutes?.setTextColor(Color.WHITE)
                bt_thirty_minutes?.setBackgroundDrawable(context.getDrawable(R.drawable.rectangular_button_green_count))
            }
            "45" -> {
                clearSelectedMinutes()
                bt_forty_five_minutes?.setTextColor(Color.WHITE)
                bt_forty_five_minutes?.setBackgroundDrawable(context.getDrawable(R.drawable.rectangular_button_green_count))
            }
            else -> {
                clearSelectedMinutes()
            }
        }
    }

    private fun callCurrentDateTimeSheetsWebservice(date: String?, date_status: Boolean) {
        try {
            clearList()
            progressDialog = AndroidUtils.get_progress(requireActivity())
            val data = JSONObject()
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v3/user/timesheets",
                "TimeSheets",
                data.toString()
            )
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun clear_date() {
        btn_save_timesheet?.setText(R.string.save)
        isEdit = false
        task_layout?.visibility = View.GONE
        tv_sp_project?.text = ""
        tv_sp_task?.text = ""
        tv_sp_status?.text = ""
        tv_sp_date?.text = ""
        tv_hours?.setText("")
        tv_total_hours?.text = ""
        clear_text(false)
    }

    private fun clearList() {
        timeSheetsList.clear()
        weektotalList.clear()
        eventsList.clear()
        matterList.clear()
        activeProjectsList.clear()
        statusList.clear()
        tasksList.clear()
    }

    private fun disableAllViews(view: View) {
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                disableAllViews(child)
            }
        } else {
            view.isEnabled = false
        }
    }

    private fun OpenPopup() {
        val builder = AlertDialog.Builder(context)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.custom_dialog, null)
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
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
                "v3/user/timesheets/$outputDate?submitted=false",
                "TimeSheets",
                data.toString()
            )
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
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
                } else if (httpResult.requestType == "Tasks") {
                    val tasks = result.getJSONArray("tasks")
                    tasksList.clear()
                    loadTasks(tasks)
                } else if (httpResult.requestType == "SAVE TIMESHEETS") {
                    deselectAllMinuteButtons()
                    AndroidUtils.showAlert(result.getString("msg"), activity, "")
                    tv_hours?.setText("")
                    tv_total_hours?.text = ""
                    minutesString = ""
                    selected_minutes(minutesString)
                    hoursString = ""
                    if (date.isEmpty()) {
                        callCurrentDateTimeSheetsWebservice(date, date_status)
                    } else {
                        callTimeSheetsWebservice(date)
                    }
                    clear_date()
                } else if (httpResult.requestType == "Submit TimeSheet") {
                    deselectAllMinuteButtons()
                    AndroidUtils.showAlert(result.getString("msg"), activity, "")
                    if (date.isEmpty()) {
                        callCurrentDateTimeSheetsWebservice(date, date_status)
                    } else {
                        callTimeSheetsWebservice(date)
                    }
                } else if (httpResult.requestType == "Delete TimeSheet") {
                    AndroidUtils.showAlert(result.getString("msg"), activity, "")
                    if (date.isEmpty()) {
                        callCurrentDateTimeSheetsWebservice(date, date_status)
                    } else {
                        callTimeSheetsWebservice(date)
                    }
                    clear_date()
                } else if (httpResult.requestType == "Edit Timesheet") {
                    AndroidUtils.showAlert(result.getString("msg"), activity, "")
                    deselectAllMinuteButtons()
                    if (date.isEmpty()) {
                        callCurrentDateTimeSheetsWebservice(date, date_status)
                    } else {
                        callTimeSheetsWebservice(date)
                    }
                    clear_date()
                    AndroidUtils.dismiss_dialog(progressDialog)
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

    private fun loadTasks(tasks: JSONArray) {
        for (i in 0 until tasks.length()) {
            val jsonObject = tasks.getJSONObject(i)
            val tasksModel = TasksModel().apply {
                displayValue = jsonObject.getString("displayValue")
                returnValue = jsonObject.getString("returnValue")
            }
            tasksList.add(tasksModel)
        }
        taskSpinnerAdapter = CommonSpinnerAdapter(requireActivity(), tasksList)
        sp_task?.adapter = taskSpinnerAdapter
        AndroidUtils.LoadList(sp_task, requireContext(), tasksList.size, true)
        sp_task?.setOnItemClickListener { parent, _, position, _ ->
            val selectedItem = parent.getItemAtPosition(position) as TasksModel
            selected_task = selectedItem.displayValue ?: ""
            AndroidUtils.DisplaySpinnerView(
                sp_task,
                tv_sp_task,
                selected_task,
                img_dropdown_icon2,
                img_clear_icon2,
                false,
                taskSpinnerAdapter,
                "Search Task"
            )
            ischecked_task = true
        }
    }

    private fun containsMatterType(matterList: ArrayList<TSMatterModel>, matterTypeToCheck: String?): Boolean {
        if (matterTypeToCheck == null) return false
        for (matter in matterList) {
            if (matter.matter_type != null && matter.matter_type == matterTypeToCheck) {
                return true
            }
        }
        return false
    }

    private fun loadTimesheetData(dates: JSONObject, result: JSONObject) {
        timeSheetsList.clear()
        eventsList.clear()
        val timeSheetModel = TimeSheetModel()
        timeSheetModel.isFrozen = dates.getBoolean("isFrozen")
        isfrozen = dates.getBoolean("isFrozen")
        timeSheetModel.currentWeek = dates.getString("currentWeek")
        timeSheetModel.nextWeek = dates.getString("nextWeek")
        timeSheetModel.prevWeek = dates.getString("prevWeek")
        val timesheets = result.optJSONObject("timesheetList")
        if (timesheets != null) {
            val matters = timesheets.optJSONArray("matters")
            val activeProjects = timesheets.optJSONArray("activeProjects")
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
            val wTotal = weektotal.optString("wTotal")
            weekTotal = wTotal
            weektotalList.add(weekModel)
            if (matters != null) {
                for (i in 0 until matters.length()) {
                    val jsonObject = matters.getJSONObject(i)
                    val matterModel = TSMatterModel().apply {
                        mattername = jsonObject.optString("matterName")
                        matterid = jsonObject.optString("matterId")
                        iseditable = jsonObject.optBoolean("is_editable")
                        Tasks = jsonObject.optJSONArray("tasks")
                        if (jsonObject.has("matterType")) {
                            matter_type = jsonObject.optString("matterType")
                        }
                    }
                    matterList.add(matterModel)
                }
            }
            if (activeProjects != null) {
                for (i in 0 until activeProjects.length()) {
                    val jsonObject = activeProjects.getJSONObject(i)
                    val matterModel = TSMatterModel().apply {
                        mattername = jsonObject.optString("matterName")
                        matterid = jsonObject.optString("matterId")
                        iseditable = jsonObject.optBoolean("is_editable")
                        Tasks = jsonObject.optJSONArray("tasks")
                        if (jsonObject.has("matterType")) {
                            matter_type = jsonObject.optString("matterType")
                        }
                    }
                    activeProjectsList.add(matterModel)
                }
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
                        eventsModel.is_editable = matterList[j].iseditable
                        if (jsonObject.has("matterType")) {
                            eventsModel.matter_type = jsonObject.getString("matterType")
                        }
                        eventsList.add(eventsModel)
                    }
                }
            }
            for (i in 0 until timeSheetsList.size) {
                if (timeSheetsList[i].isFrozen) {
                    view?.alpha = 0.8f
                    issubmitted = true
                    view?.let { disableAllViews(it) }
                    AlreadySubmittedTimeSheets()
                    btn_submit_timesheet?.visibility = View.GONE
                    rv_non_submitted_timesheets?.visibility = View.GONE
                    project_view?.isEnabled = false
                    status_view?.isEnabled = false
                    date_view?.isEnabled = false
                    tv_hours?.isClickable = false
                } else {
                    projectSpinnerAdapter = CommonSpinnerAdapter(requireActivity(), activeProjectsList)
                    sp_project?.adapter = projectSpinnerAdapter
                    AndroidUtils.LoadList(sp_project, requireContext(), activeProjectsList.size, true)
                    sp_project?.setOnItemClickListener { parent, _, position, _ ->
                        val selectedItem = parent.getItemAtPosition(position) as TSMatterModel
                        selected_matter = selectedItem.mattername ?: ""
                        selected_matter_id = selectedItem.matterid ?: ""
                        try {
                            selected_matter_type = selectedItem.matter_type ?: ""
                            isMatterTypeExists = containsMatterType(activeProjectsList, selected_matter_type)
                        } catch (e: NullPointerException) {
                            e.fillInStackTrace()
                        }
                        if (selected_matter_type.isNotEmpty()) {
                            callTaskWebservice(selected_matter_type)
                        } else {
                            callTaskWebservice(selected_matter_id.lowercase(Locale.ROOT))
                        }
                        AndroidUtils.DisplaySpinnerView(
                            sp_project,
                            tv_sp_project,
                            selected_matter,
                            img_dropdown_icon1,
                            img_clear_icon1,
                            false,
                            projectSpinnerAdapter,
                            "Search Project"
                        )
                        ischecked_project = true
                        unhideProject()
                    }

                    statusList.clear()
                    statusList.add(StatusModel(getString(R.string.billable)))
                    statusList.add(StatusModel(getString(R.string.non_billable)))
                    statusSpinnerAdapter = CommonSpinnerAdapter(requireActivity(), statusList)
                    sp_status?.adapter = statusSpinnerAdapter
                    AndroidUtils.LoadList(sp_status, requireContext(), statusList.size, true)
                    sp_status?.setOnItemClickListener { parent, _, position, _ ->
                        val selectedItem = parent.getItemAtPosition(position) as StatusModel
                        selected_status = selectedItem.name ?: ""
                        status = selected_status
                        AndroidUtils.DisplaySpinnerView(
                            sp_status,
                            tv_sp_status,
                            selected_status,
                            img_dropdown_icon3,
                            img_clear_icon3,
                            false,
                            statusSpinnerAdapter,
                            "Search Status"
                        )
                        ischecked_status = true
                    }
                    if (weekTotal.isNotEmpty() && weekTotal != "0:0") {
                        btn_submit_timesheet?.visibility = View.VISIBLE
                        rv_non_submitted_timesheets?.visibility = View.VISIBLE
                        hasTimesheets = true
                        break
                    } else {
                        hasTimesheets = false
                        rv_non_submitted_timesheets?.visibility = View.GONE
                        btn_submit_timesheet?.visibility = View.GONE
                    }
                    issubmitted = false
                }
            }
        }

        updateEmptyState()

        try {
            if (!issubmitted) {
                loadTimesheetsRecyclerview()
            }
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun unhideProject() {
        AndroidUtils.DisplaySpinnerView(
            sp_project,
            tv_sp_project,
            selected_matter,
            img_dropdown_icon1,
            img_clear_icon1,
            false,
            projectSpinnerAdapter,
            "Search Project"
        )
        ischecked_project = true
        clearDetails()
        task_layout?.visibility = View.VISIBLE
    }

    private fun clearDetails() {
        AndroidUtils.DisplaySpinnerView(
            sp_task,
            tv_sp_task,
            selected_task,
            img_dropdown_icon2,
            img_clear_icon2,
            false,
            taskSpinnerAdapter,
            "Search Task"
        )
        ischecked_task = true
        AndroidUtils.DisplaySpinnerView(
            sp_status,
            tv_sp_status,
            selected_status,
            img_dropdown_icon3,
            img_clear_icon3,
            false,
            statusSpinnerAdapter,
            "Search Status"
        )
        ischecked_status = true
        AndroidUtils.DisplaySpinnerView(
            sp_date,
            tv_sp_date,
            selected_date,
            img_dropdown_icon4,
            img_clear_icon4,
            false,
            dateSpinnerAdapter,
            "Search Date"
        )
        ischecked_date = true
        task_layout?.visibility = View.GONE
        hoursString = ""
        selected_status = ""
        status = ""
        selected_date = ""
        tv_hours?.setText("")
        minutesString = ""
        selected_minutes(minutesString)
        tv_total_hours?.text = ""
    }

    private fun loadTimesheetsRecyclerview() {
        rv_non_submitted_timesheets?.layoutManager = GridLayoutManager(context, 1)
        val timeSheetsAdapter = TimeSheetsAdapter(
            weeksList,
            eventsList,
            weektotalList,
            requireContext(),
            issubmitted,
            this,
            requireActivity()
        )
        rv_non_submitted_timesheets?.adapter = timeSheetsAdapter
        AndroidUtils.setupBottomSpacerFooter(
            rv_non_submitted_timesheets,
            resources.getDimensionPixelSize(R.dimen.twentyeight_dp)
        )
        if (timeSheetsAdapter.itemCount > 0) {
            val lastPosition = timeSheetsAdapter.itemCount - 1
            rv_non_submitted_timesheets?.smoothScrollToPosition(lastPosition)
        }
        timeSheetsAdapter.notifyDataSetChanged()
    }

    private fun callTaskWebservice(selected_matter_type: String) {
        progressDialog = AndroidUtils.get_progress(requireActivity())
        try {
            val jsonObject = JSONObject()
            WebServiceHelper.callHttpWebService(
                this,
                requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v3/event/tasks/$selected_matter_type",
                "Tasks",
                jsonObject.toString()
            )
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun AlreadySubmittedTimeSheets() {
        timeSheets?.Frozen_view()
    }

    var timesheet_delete_scope = "DELETE_TIMESHEET_ONLY"

    override fun DeleteEvent_Timesheet(taskModel: TaskModel) {
        try {
            val dialogBuilder = AlertDialog.Builder(activity)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.delete_relationship, null)
            val tv_confirmation = view.findViewById<TextView>(R.id.tv_confirmation)
            val close_details = view.findViewById<ImageView>(R.id.close_documents)
            tv_confirmation.setText(R.string.are_you_sure_you_want_to_delete_task)
            val bt_yes = view.findViewById<AppCompatButton>(R.id.btn_yes)
            val btn_no = view.findViewById<AppCompatButton>(R.id.btn_No)
            val dialog = dialogBuilder.create()
            close_details.setOnClickListener {
                dialog.dismiss()
            }
            btn_no.setOnClickListener {
                dialog.dismiss()
            }
            bt_yes.setOnClickListener {
                dialog.dismiss()
                if (taskModel.isLinkedWithCalendar == true) {
                    AndroidUtils.showConfirmationDialog(
                        requireContext(),
                        "Confirmation",
                        "This timesheet has an associated event entry. Do you want to update the event too?",
                        requireContext().getString(R.string.delete_both),
                        requireContext().getString(R.string.delete_timesheet_only),
                        object : AndroidUtils.OnConfirmListener {
                            override fun onSave() {
                                timesheet_delete_scope = "DELETE_BOTH"
                                callDeleteTimesheetwebservice(taskModel.taskid ?: "", timesheet_delete_scope)
                            }

                            override fun onCancel() {
                                timesheet_delete_scope = "DELETE_TIMESHEET_ONLY"
                                callDeleteTimesheetwebservice(taskModel.taskid ?: "", timesheet_delete_scope)
                            }
                        }
                    )
                } else {
                    timesheet_delete_scope = "DELETE_TIMESHEET_ONLY"
                    callDeleteTimesheetwebservice(taskModel.taskid ?: "", timesheet_delete_scope)
                }
            }
            dialog.setView(view)
            dialog.show()
        } catch (e: Exception) {
            AndroidUtils.showAlert(e.message, activity)
        }
    }

    private fun clear_text(isEdit: Boolean) {
        val proj = project_view ?: return
        val tsk = task_view ?: return
        val stat = status_view ?: return
        val dt = date_view ?: return
        val hrs = tv_hours ?: return
        val mins = ll_minutes ?: return
        if (isEdit) {
            AndroidUtils.ToggleButton(1, proj)
            AndroidUtils.ToggleButton(1, tsk)
            AndroidUtils.ToggleButton(1, stat)
            AndroidUtils.ToggleButton(1, dt)
            AndroidUtils.ToggleButton(1, hrs)
            AndroidUtils.ToggleButton(1, mins)
            proj.isEnabled = false
            tsk.isEnabled = true
            stat.isEnabled = true
            dt.isEnabled = true
            hrs.isClickable = true
            bt_fifteen_minutes?.isEnabled = true
            bt_thirty_minutes?.isEnabled = true
            bt_forty_five_minutes?.isEnabled = true
            img_clear_icon2?.visibility = View.VISIBLE
            img_dropdown_icon2?.visibility = View.GONE
            img_clear_icon1?.visibility = View.VISIBLE
            img_dropdown_icon1?.visibility = View.GONE
            img_clear_icon3?.visibility = View.VISIBLE
            img_dropdown_icon3?.visibility = View.GONE
            img_clear_icon4?.visibility = View.VISIBLE
            img_dropdown_icon4?.visibility = View.GONE
            img_clear_icon1?.isEnabled = false
            img_clear_icon2?.isEnabled = true
            img_clear_icon3?.isEnabled = true
        } else {
            AndroidUtils.ToggleButton(1, proj)
            AndroidUtils.ToggleButton(1, tsk)
            AndroidUtils.ToggleButton(1, stat)
            AndroidUtils.ToggleButton(1, dt)
            AndroidUtils.ToggleButton(1, mins)
            AndroidUtils.ToggleButton(1, hrs)
            proj.isEnabled = true
            tsk.isEnabled = true
            stat.isEnabled = true
            dt.isEnabled = true
            hrs.isEnabled = true
            bt_fifteen_minutes?.isEnabled = true
            bt_thirty_minutes?.isEnabled = true
            bt_forty_five_minutes?.isEnabled = true
            img_clear_icon2?.visibility = View.GONE
            img_dropdown_icon2?.visibility = View.VISIBLE
            img_clear_icon1?.visibility = View.GONE
            img_dropdown_icon1?.visibility = View.VISIBLE
            img_clear_icon3?.visibility = View.GONE
            img_dropdown_icon3?.visibility = View.VISIBLE
            img_clear_icon4?.visibility = View.GONE
            img_dropdown_icon4?.visibility = View.VISIBLE
            img_clear_icon1?.isEnabled = true
            img_clear_icon2?.isEnabled = true
            img_clear_icon3?.isEnabled = true
        }
        this.isEdit = isEdit
    }

    private fun getMatterPosition(matterId: String): Int {
        for (i in 0 until activeProjectsList.size) {
            if (activeProjectsList[i].matterid.equals(matterId, ignoreCase = true)) {
                return i
            }
        }
        return -1
    }

    override fun EditEvent_Timesheet(eventsModels: TaskModel, selected_date1: String, matter_type: String) {
        task_layout?.visibility = View.VISIBLE
        event_name = eventsModels.taskid ?: ""
        status = eventsModels.Task_billing ?: ""
        task_name = eventsModels.Task_name ?: ""
        matter_name = eventsModels.Task_matter_name
        event_task_id = eventsModels.Task_matter_id
        event_id = eventsModels.matterid
        hours = eventsModels.hours ?: ""
        isLinkedWithCalendar = eventsModels.isLinkedWithCalendar
        minutes = eventsModels.minutes ?: ""
        hoursString = hours
        tv_hours?.setText(hours)
        minutesString = minutes
        selected_minutes(minutesString)
        selected_matter_type = matter_type
        if (selected_matter_type.isNotEmpty()) {
            callTaskWebservice(selected_matter_type)
        } else {
            event_id?.let { callTaskWebservice(it) }
        }
        selected_status = status
        val value = selected_date1.substring(4)
        Log.d("Splitted", date + "./././" + value)
        val inputformat = SimpleDateFormat("MMMM dd,yyyy", Locale.US)
        val outputformat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        try {
            val dVal = inputformat.parse(value)
            if (dVal != null) {
                selected_date = outputformat.format(dVal)
            }
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
        tv_sp_task?.text = task_name
        val pos = event_id?.let { getMatterPosition(it) } ?: -1
        if (pos != -1) {
            val model = activeProjectsList[pos]
            selected_matter = model.mattername ?: ""
            selected_matter_id = model.matterid ?: ""
            selected_matter_type = model.matter_type ?: ""
            tv_sp_project?.text = selected_matter
            if (selected_matter_type.isNotEmpty()) {
                callTaskWebservice(selected_matter_type)
            } else {
                callTaskWebservice(selected_matter_id.lowercase(Locale.ROOT))
            }
        }
        tv_sp_date?.text = selected_date
        btn_save_timesheet?.setText(R.string.save)
        isEdit = true
        tv_sp_status?.text = status
        val hr = if (hours.isNotEmpty()) hours.toInt() else 0
        val min = if (minutes.isNotEmpty()) minutes.toInt() else 0
        val totalTimeText = String.format(Locale.US, "%02d:%02d", hr, min)
        tv_total_hours?.text = totalTimeText
        clear_text(true)
        scrollView?.post {
            project_view?.let { scrollView?.smoothScrollTo(0, it.top) }
            tv_sp_project?.requestFocus()
        }
        val proj = project_view ?: return
        val tsk = task_view ?: return
        val stat = status_view ?: return
        val dt = date_view ?: return
        val hrs = tv_hours ?: return
        val mins = ll_minutes ?: return
        if (eventsModels.editProject == true) {
            AndroidUtils.ToggleButton(1, proj)
        } else {
            AndroidUtils.ToggleButton(0, proj)
        }
        if (eventsModels.editTask == true) {
            AndroidUtils.ToggleButton(1, tsk)
        } else {
            AndroidUtils.ToggleButton(0, tsk)
        }
        if (eventsModels.editStatus == true) {
            AndroidUtils.ToggleButton(1, stat)
        } else {
            AndroidUtils.ToggleButton(0, stat)
        }
        if (eventsModels.editDate == true) {
            AndroidUtils.ToggleButton(1, dt)
        } else {
            AndroidUtils.ToggleButton(0, dt)
        }
        if (eventsModels.editHours == true) {
            AndroidUtils.ToggleButton(1, hrs)
        } else {
            AndroidUtils.ToggleButton(0, hrs)
        }
        if (eventsModels.editMinutes != true) {
            AndroidUtils.ToggleButton(0, mins)
            bt_fifteen_minutes?.isEnabled = false
            bt_thirty_minutes?.isEnabled = false
            bt_forty_five_minutes?.isEnabled = false
        } else {
            AndroidUtils.ToggleButton(1, mins)
            bt_fifteen_minutes?.isEnabled = true
            bt_thirty_minutes?.isEnabled = true
            bt_forty_five_minutes?.isEnabled = true
        }
        Log.d(
            "Edited_timesheet",
            "$matter_name...$event_name......$status......$task_name......$event_task_id......$event_id......$hours......$minutes......$selected_date"
        )
    }
}
