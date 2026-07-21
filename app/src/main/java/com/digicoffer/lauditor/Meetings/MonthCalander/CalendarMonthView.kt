package com.digicoffer.lauditor.Meetings.MonthCalander

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.digicoffer.lauditor.Appointments.Models.AppointmentModel
import com.digicoffer.lauditor.Appointments.Models.PaymentModel
import com.digicoffer.lauditor.Meetings.ViewModels.Events_Adapter
import com.digicoffer.lauditor.Meetings.ViewModels.Meetings
import com.digicoffer.lauditor.Meetings.Models.Event_Details_DO
import com.digicoffer.lauditor.Meetings.Models.Events_Do
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit

class CalendarMonthView(
    private var meetings: View,
    private var currentFilter: Meetings.FilterType
) : Fragment(), AsyncTaskCompleteListener, View.OnClickListener, Events_Adapter.EventListener, CalendarDayAdapter.OnDateSelectedListener {

    private var viewPager: ViewPager2? = null
    private var rv_monthView: RecyclerView? = null
    private val currentDate = LocalDate.now()
    private var currentMonth = LocalDate.now()
    private var Currenr_date = ""
    private var tv_month: TextView? = null
    var appointments_list = ArrayList<AppointmentModel>()
    var selected_date_appointments = ArrayList<AppointmentModel>()
    private var eventDetailsListener: EventDetailsListener? = null
    var recurring_edit_choice = "this"
    private var event_creation_date = ""
    private var filter = ""
    private var adapter: MonthPagerAdapter? = null
    private var progress_dialog: Dialog? = null
    private val selected_date_events = ArrayList<Events_Do>()
    private val events_list = ArrayList<Events_Do>()
    private var isViewPagerInitialized = false
    private val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())

    // Empty state views
    private var layout_empty_state: LinearLayout? = null
    private var tv_empty_subtitle: TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.custom_month_calander, container, false)

        viewPager = view.findViewById(R.id.viewPager)
        rv_monthView = view.findViewById(R.id.rv_monthView)
        val previous_img = view.findViewById<ImageButton>(R.id.iv_previous_week)
        val forward_img = view.findViewById<ImageButton>(R.id.iv_forward_week)
        tv_month = view.findViewById(R.id.tv_month)

        layout_empty_state = view.findViewById(R.id.layout_empty_state)
        tv_empty_subtitle = view.findViewById(R.id.tv_empty_subtitle)

        tv_month?.typeface = Typeface.DEFAULT_BOLD
        tv_month?.gravity = Gravity.CENTER
        tv_month?.text = currentMonth.format(formatter)

        previous_img.setOnClickListener {
            val currentItem = viewPager?.currentItem ?: 0
            if (currentItem > 0) {
                viewPager?.setCurrentItem(currentItem - 1, true)
            }
        }

        forward_img.setOnClickListener {
            val currentItem = viewPager?.currentItem ?: 0
            viewPager?.setCurrentItem(currentItem + 1, true)
        }

        val mCalendar = Calendar.getInstance()
        events_list.clear()
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        filter = sdf.format(mCalendar.time)
        adjustViewPagerHeight(currentMonth)

        hideEmptyState()
        callEventListwebservice(filter)
        return view
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (layout_empty_state == null || rv_monthView == null) return

        if (isEmpty) {
            rv_monthView?.visibility = View.GONE
            layout_empty_state?.visibility = View.VISIBLE
        } else {
            rv_monthView?.visibility = View.VISIBLE
            layout_empty_state?.visibility = View.GONE
        }
    }

    private fun hideEmptyState() {
        layout_empty_state?.visibility = View.GONE
        rv_monthView?.visibility = View.VISIBLE
    }

    private fun filterDataByType() {
        selected_date_events.clear()
        selected_date_appointments.clear()

        if (currentFilter == Meetings.FilterType.ALL || currentFilter == Meetings.FilterType.MY_MEETINGS) {
            for (event in events_list) {
                if (isSameSelectedDate(event.event_start_time)) {
                    selected_date_events.add(event)
                }
            }
        }

        if (currentFilter == Meetings.FilterType.ALL || currentFilter == Meetings.FilterType.APPOINTMENTS) {
            for (appointment in appointments_list) {
                if (isSameSelectedDate(appointment.appointment_from)) {
                    selected_date_appointments.add(appointment)
                }
            }
        }

        sortLists()
    }

    private fun isSameSelectedDate(timestamp: String?): Boolean {
        if (timestamp.isNullOrEmpty()) return false
        return try {
            val date = AndroidUtils.stringToDateTimeDefault(timestamp, "yyyy-MM-dd'T'HH:mm:ss")
            val d = AndroidUtils.getDateToString(date, "yyyy-MM-dd")
            d == Currenr_date
        } catch (e: Exception) {
            false
        }
    }

    private fun sortLists() {
        selected_date_events.sortWith(compareBy(nullsLast()) { it.converted_Start_time })

        selected_date_appointments.sortWith(compareBy(nullsLast()) { it.appointment_from })
    }

    private fun getMonthRowCount(monthDate: LocalDate): Int {
        val firstDay = monthDate.withDayOfMonth(1)
        val firstDayOfWeek = firstDay.dayOfWeek.value // 1(Mon)-7(Sun)
        val daysInMonth = monthDate.lengthOfMonth()
        val offset = if (firstDayOfWeek == 7) 0 else firstDayOfWeek
        val totalCells = offset + daysInMonth
        return Math.ceil(totalCells / 7.0).toInt()
    }

    private fun adjustViewPagerHeight(monthDate: LocalDate) {
        val rowCount = getMonthRowCount(monthDate)
        val cellHeight = resources.getDimension(R.dimen.calendar_day_height).toInt()
        val newHeight = cellHeight * rowCount
        val params = viewPager?.layoutParams
        if (params != null) {
            params.height = newHeight
            viewPager?.layoutParams = params
        }
    }

    private fun loadAppointments(appointments: JSONArray) {
        Thread {
            try {
                appointments_list.clear()
                selected_date_appointments.clear()

                for (i in 0 until appointments.length()) {
                    val jsonObject = appointments.optJSONObject(i) ?: continue

                    val appointmentModel = AppointmentModel().apply {
                        id = jsonObject.optString("id")
                        client_id = jsonObject.optString("client_id")
                        guid = jsonObject.optString("guid")
                        client_name = jsonObject.optString("client_name")
                        appointment_from = jsonObject.optString("appointment_from")
                        appointment_to = jsonObject.optString("appointment_to")
                        consultation_mode = jsonObject.optString("consultation_mode")
                        appointment_status = jsonObject.optString("appointment_status")
                        meeting_room_id = jsonObject.optString("meeting_room_id")
                        meeting_room_expires_at = jsonObject.optString("meeting_room_expires_at")
                        rsvp_status = jsonObject.optString("rsvp_status")
                        created_at = jsonObject.optString("created_at")
                        if (jsonObject.has("client_profile_pic")) {
                            client_profile_pic = jsonObject.optString("client_profile_pic", "")
                        }
                        if (jsonObject.has("services_offered")) {
                            services_offered = jsonObject.optJSONArray("services_offered")
                        }
                        if (jsonObject.has("payment")) {
                            val paymentObject = jsonObject.getJSONObject("payment")
                            val paymentModel = PaymentModel().apply {
                                status = paymentObject.optString("status")
                                amount_paid = paymentObject.optString("amount_paid")
                                currency = paymentObject.optString("currency")
                                symbol = paymentObject.optString("symbol")
                                label = paymentObject.optString("label")
                            }
                            payment = paymentModel
                        }
                    }

                    val from_ts = appointmentModel.appointment_from
                    val appointment_date = AndroidUtils.stringToDateTimeDefault(from_ts, "yyyy-MM-dd'T'HH:mm:ss")
                    val converted_from_ts = AndroidUtils.getDateToString(appointment_date, "yyyy-MM-dd")

                    val to_ts = appointmentModel.appointment_to
                    val appointment_end_date = AndroidUtils.stringToDateTimeDefault(to_ts, "yyyy-MM-dd'T'HH:mm:ss")
                    val converted_to_ts = AndroidUtils.getDateToString(appointment_end_date, "yyyy-MM-dd")

                    if (converted_from_ts.contains(Currenr_date) || converted_to_ts.contains(Currenr_date)) {
                        selected_date_appointments.add(appointmentModel)
                    }

                    appointments_list.add(appointmentModel)
                }

                activity?.runOnUiThread {
                    adapter?.updateBothLists(events_list, appointments_list)
                    loadRecyclerView()
                }

            } catch (e: Exception) {
                e.fillInStackTrace()
            }
        }.start()
    }

    private fun setupViewPager() {
        adapter = MonthPagerAdapter(this, currentDate, events_list, appointments_list, currentFilter, this)
        viewPager?.adapter = adapter
        viewPager?.setCurrentItem(MonthPagerAdapter.START_POSITION, false)
        viewPager?.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val selectedMonth = currentDate.plusMonths((position - MonthPagerAdapter.START_POSITION).toLong())
                currentMonth = selectedMonth
                adjustViewPagerHeight(selectedMonth)

                val formattedDate = AndroidUtils.getDateToString(
                    AndroidUtils.localDateToDate(selectedMonth),
                    "MMM dd, yyyy"
                )
                filter = formattedDate

                activity?.runOnUiThread { hideEmptyState() }

                callEventListwebservice(formattedDate)
            }
        })
    }

    private fun loadRecyclerView() {
        val hasEvents = selected_date_events.isNotEmpty()
        val hasAppointments = selected_date_appointments.isNotEmpty()
        val isEmpty = !hasEvents && !hasAppointments

        val eventsAdapter = Events_Adapter(
            selected_date_events,
            selected_date_appointments,
            this,
            requireContext(),
            requireActivity()
        )

        rv_monthView?.adapter = eventsAdapter
        AndroidUtils.LoadingRecyclerview(rv_monthView, context)
        AndroidUtils.setupBottomSpacerFooter(rv_monthView, resources.getDimensionPixelSize(R.dimen.twentyeight_dp))

        updateEmptyState(isEmpty)
    }

    fun applyFilter(filterType: Meetings.FilterType) {
        this.currentFilter = filterType

        adapter?.updateFilter(currentFilter)

        hideEmptyState()

        filterDataByType()

        loadRecyclerView()
    }

    private fun loadEvents(jsonArray: JSONArray) {
        Thread {
            try {
                events_list.clear()
                selected_date_events.clear()

                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val events_do = Events_Do().apply {
                        event_Name = jsonObject.getString("title")
                        dialin = jsonObject.getString("dialin")
                        event_type = jsonObject.getString("event_type")
                        location = jsonObject.getString("location")
                        if (jsonObject.has("matter_id")) {
                            matter_id = jsonObject.getString("matter_id")
                        }
                        if (jsonObject.has("matter_name")) {
                            matter_name = jsonObject.getString("matter_name")
                        }
                        if (jsonObject.has("matter_type")) {
                            matter_type = jsonObject.getString("matter_type")
                        }
                        isrecurring = jsonObject.getBoolean("isrecurring")
                        repeat_interval = jsonObject.optString("repeat_interval")
                        meeting_link = jsonObject.getString("meeting_link")
                        notes = jsonObject.getString("notes")
                        timezone_location = jsonObject.getString("timezone_location")
                        timezone_offset = jsonObject.getString("timezone_offset")
                        attachments = jsonObject.getJSONArray("attachments")
                        invitees_external = jsonObject.getJSONArray("invitees_external")
                        invitees_internal = jsonObject.getJSONArray("invitees_internal")
                        notifications = jsonObject.getJSONArray("notifications")
                        event_start_time = jsonObject.getString("from_ts")
                        event_end_time = jsonObject.getString("to_ts")
                        isAll_day = jsonObject.getBoolean("allday")
                        event_id = jsonObject.getString("id")
                        isOwner = jsonObject.getBoolean("owner")
                    }

                    val event_date = AndroidUtils.stringToDateTimeDefault(
                        events_do.event_start_time, "yyyy-MM-dd'T'HH:mm:ss"
                    )
                    val event_end_date = AndroidUtils.stringToDateTimeDefault(
                        events_do.event_end_time, "yyyy-MM-dd'T'HH:mm:ss"
                    )

                    events_do.converted_Start_time = AndroidUtils.getDateToString(event_date, "HH:mm a")
                    events_do.cOnverted_End_time = AndroidUtils.getDateToString(event_end_date, "HH:mm a")
                    events_do.converted_date = AndroidUtils.getDateToString(event_date, "yyyy-MM-dd")

                    val converted_from_ts = AndroidUtils.getDateToString(event_date, "dd-MM-yyyy")
                    val converted_to_ts = AndroidUtils.getDateToString(event_end_date, "dd-MM-yyyy")

                    if (converted_from_ts.contains(Currenr_date) || converted_to_ts.contains(Currenr_date)) {
                        selected_date_events.add(events_do)
                    }

                    events_list.add(events_do)
                }

                val handler = Handler(Looper.getMainLooper())
                handler.post {
                    try {
                        selected_date_events.sortWith(compareBy(nullsLast()) { it.converted_Start_time })

                        events_list.sortWith(compareBy(nullsLast()) { it.converted_Start_time })

                        loadRecyclerView()

                        if (!isViewPagerInitialized) {
                            setupViewPager()
                            isViewPagerInitialized = true
                        } else {
                            adapter?.updateEventsList(events_list)
                        }

                        tv_month?.text = currentMonth.format(formatter)

                    } catch (e: Exception) {
                        e.fillInStackTrace()
                    }
                }

            } catch (e: Exception) {
                e.fillInStackTrace()
            }
        }.start()
    }

    private fun callEventListwebservice(filter: String) {
        progress_dialog = AndroidUtils.get_progress(activity)
        val postData = JSONObject()
        try {
            val event_date = AndroidUtils.stringToDateTimeDefault(filter, "MMM dd, yyyy")
            event_creation_date = AndroidUtils.getDateToString(event_date, "MMyyyy")
            Currenr_date = AndroidUtils.getDateToString(event_date, "yyyy-MM-dd")

            val calendar = GregorianCalendar()
            val timeZone = calendar.timeZone
            val offset = timeZone.rawOffset
            val hours = TimeUnit.MILLISECONDS.toMinutes(offset.toLong())
            val timezoneoffset = -1 * hours

            WebServiceHelper.callHttpWebService(
                this, requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v3/events/$timezoneoffset/M$event_creation_date",
                "Events_List",
                postData.toString()
            )
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun callAppointmentListWebService(filter: String) {
        progress_dialog = AndroidUtils.get_progress(activity)
        val postData = JSONObject()
        try {
            val monthDates = getMonthStartAndEndDates(filter)
            if (monthDates != null) {
                val startDate = monthDates[0]
                val endDate = monthDates[1]

                val event_date = AndroidUtils.stringToDateTimeDefault(filter, "MMM dd, yyyy")
                event_creation_date = AndroidUtils.getDateToString(event_date, "MMyyyy")
                Currenr_date = AndroidUtils.getDateToString(event_date, "yyyy-MM-dd")

                val calendar = GregorianCalendar()
                val timeZone = calendar.timeZone
                val offset = timeZone.rawOffset
                val hours = TimeUnit.MILLISECONDS.toMinutes(offset.toLong())
                val timezoneoffset = -1 * hours

                WebServiceHelper.callHttpWebService(
                    this, requireContext(),
                    WebServiceHelper.RestMethodType.GET,
                    "v3/appointments?schedule_after=$startDate&schedule_before=$endDate",
                    "Appointments_List",
                    postData.toString()
                )

                Log.d("AppoinemtmentListCall", "v3/appointments?schedule_after=$startDate&schedule_before=$endDate")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getMonthStartAndEndDates(filter: String): Array<String>? {
        return try {
            val filterDate = AndroidUtils.stringToDateTimeDefault(filter, "MMM dd, yyyy")
            val calendar = Calendar.getInstance()
            calendar.time = filterDate

            calendar.set(Calendar.DAY_OF_MONTH, 1)
            val startDate = AndroidUtils.getDateToString(calendar.time, "yyyy-MM-dd")

            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            val endDate = AndroidUtils.getDateToString(calendar.time, "yyyy-MM-dd")

            arrayOf(startDate, endDate)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        if (progress_dialog != null && progress_dialog!!.isShowing) {
            AndroidUtils.dismiss_dialog(progress_dialog)
        }

        try {
            if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val result = JSONObject(httpResult.responseContent)

                    if (httpResult.requestType == "Events_List") {
                        if (!result.getBoolean("error")) {
                            val jsonArray = result.getJSONArray("events")
                            loadEvents(jsonArray)
                        } else {
                            AndroidUtils.showAlert(result.getString("msg"), activity)
                        }
                        callAppointmentListWebService(filter)

                    } else if (httpResult.requestType == "Appointments_List") {
                        if (!result.getBoolean("error")) {
                            val jsonArray = result.getJSONArray("appointments")
                            loadAppointments(jsonArray)
                        } else {
                            AndroidUtils.showAlert(result.getString("msg"), activity)
                        }

                    } else if (httpResult.requestType == "EVENT_DELETE") {
                        AndroidUtils.showAlert("Event Deleted Successfully", activity)
                        progress_dialog?.dismiss()
                        callEventListwebservice(filter)
                    }

                } catch (e: Exception) {
                    e.fillInStackTrace()
                }

            } else if (httpResult.result == WebServiceHelper.ServiceCallStatus.Failed) {
                if (progress_dialog != null && progress_dialog!!.isShowing) {
                    AndroidUtils.dismiss_dialog(progress_dialog)
                }
                try {
                    val result = JSONObject(httpResult.responseContent)
                    AndroidUtils.showErrorAlert(result.optString("msg"), activity)
                } catch (e: Exception) {
                    e.fillInStackTrace()
                }

            } else {
                if (progress_dialog != null && progress_dialog!!.isShowing) {
                    AndroidUtils.dismiss_dialog(progress_dialog)
                }
                AndroidUtils.showErrorAlert(httpResult.responseContent.toString(), activity)
            }

        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    var event_delete_scope = "DELETE_EVENT_ONLY"

    fun callDeleteEventwebservice(id: String, recurring_choice: String?, isevent_delete_scope: Boolean) {
        if (isevent_delete_scope) {
            AndroidUtils.showConfirmationDialog(
                requireContext(), "Confirmation",
                "This event has an associated timesheet entry. Do you want to update the timesheet too?",
                requireContext().getString(R.string.delete_both),
                requireContext().getString(R.string.delete_event_only),
                object : AndroidUtils.OnConfirmListener {
                    override fun onSave() {
                        event_delete_scope = "DELETE_BOTH"
                        callDeleteEventWebservice(id, recurring_choice)
                    }

                    override fun onCancel() {
                        event_delete_scope = "DELETE_EVENT_ONLY"
                        callDeleteEventWebservice(id, recurring_choice)
                    }
                }
            )
        } else {
            event_delete_scope = "DELETE_EVENT_ONLY"
            callDeleteEventWebservice(id, recurring_choice)
        }
    }

    fun callDeleteEventWebservice(id: String, recurring_choice: String?) {
        progress_dialog = AndroidUtils.get_progress(activity)
        val postData = JSONObject()
        try {
            if (recurring_choice != null) {
                postData.put("choice", recurring_choice)
                postData.put("event_delete_scope", event_delete_scope)
            }
            WebServiceHelper.callHttpWebService(
                this, requireContext(),
                WebServiceHelper.RestMethodType.DELETE,
                "v3/event/$id",
                "EVENT_DELETE",
                postData.toString()
            )
        } catch (e: JSONException) {
            e.fillInStackTrace()
        }
    }

    fun delete_recurring_event(event_id: String, isevent_delete_scope: Boolean) {
        try {
            val builder = AlertDialog.Builder(activity)
            val inflater = layoutInflater
            val dialogLayout = inflater.inflate(R.layout.edit_recurring_choice, null)
            val edit_event_dialog = dialogLayout.findViewById<TextView>(R.id.edit_event_dialog)
            edit_event_dialog.setText(R.string.delete_recurring_event)

            val ll_only_this = dialogLayout.findViewById<LinearLayout>(R.id.radio_delete_only_this)
            val delete_only_this = ll_only_this.findViewById<CheckBox>(R.id.chk_select_all)
            val delete_only_this_txt = ll_only_this.findViewById<TextView>(R.id.chk_select_all_text)
            ll_only_this.setBackgroundResource(R.drawable.background_transparent)
            delete_only_this_txt.setText(R.string.this_event)
            delete_only_this_txt.textSize = DynamicUtils.twenty.toFloat()

            val ll_all = dialogLayout.findViewById<LinearLayout>(R.id.radio_delete_all)
            val delete_all = ll_all.findViewById<CheckBox>(R.id.chk_select_all)
            val delete_all_txt = ll_all.findViewById<TextView>(R.id.chk_select_all_text)
            ll_all.setBackgroundResource(R.drawable.background_transparent)
            delete_all_txt.setText(R.string.all_events)
            delete_all_txt.textSize = DynamicUtils.twenty.toFloat()

            val ll_following = dialogLayout.findViewById<LinearLayout>(R.id.radio_delete_ts_fe)
            val delete_following = ll_following.findViewById<CheckBox>(R.id.chk_select_all)
            val delete_following_txt = ll_following.findViewById<TextView>(R.id.chk_select_all_text)
            ll_following.setBackgroundResource(R.drawable.background_transparent)
            delete_following_txt.setText(R.string.this_and_following_events)
            delete_following_txt.textSize = DynamicUtils.twenty.toFloat()

            val delete = dialogLayout.findViewById<Button>(R.id.delete_event)
            val btn_close_event = dialogLayout.findViewById<Button>(R.id.btn_close_event)
            btn_close_event.setTextColor(Color.RED)
            btn_close_event.setText(R.string.cancel)

            delete_only_this.setOnClickListener {
                delete_only_this.isChecked = true
                recurring_edit_choice = "this"
                delete_all.isChecked = false
                delete_following.isChecked = false
            }

            delete_following.setOnClickListener {
                delete_following.isChecked = true
                recurring_edit_choice = "forward"
                delete_all.isChecked = false
                delete_only_this.isChecked = false
            }

            delete_all.setOnClickListener {
                delete_all.isChecked = true
                recurring_edit_choice = "all"
                delete_only_this.isChecked = false
                delete_following.isChecked = false
            }

            val dialog = builder.create()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            progress_dialog = dialog

            btn_close_event.setOnClickListener { progress_dialog?.dismiss() }

            delete.setOnClickListener {
                if (delete_only_this.isChecked || delete_following.isChecked || delete_all.isChecked) {
                    progress_dialog?.dismiss()
                    callDeleteEventwebservice(event_id, recurring_edit_choice, isevent_delete_scope)
                } else {
                    AndroidUtils.showAlert("Please choose one of the Delete recurring event", activity)
                }
            }

            dialog.setView(dialogLayout)
            dialog.show()
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    interface EventDetailsListener {
        fun onEventDetailsPassed(event_details_list: ArrayList<Event_Details_DO>, calendar_Type: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            Log.d("Interface", "Interface Called")
            eventDetailsListener = parentFragment as? EventDetailsListener
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    override fun onEvent(event_details_list: ArrayList<Event_Details_DO>) {
        if (eventDetailsListener != null) {
            Log.d("EventsList", event_details_list.toString())
            eventDetailsListener?.onEventDetailsPassed(event_details_list, "Weekly")
        }
    }

    override fun delete_events(events_do: Events_Do) {
        AndroidUtils.Delete_Popup(
            requireActivity(),
            "Are you sure do you want to delete ${events_do.event_Name} ?",
            "Delete_Event",
            events_do.event_id,
            null,
            this,
            meetings,
            events_do.isrecurring,
            events_do.is_linked_with_timesheet
        )
    }

    override fun load_events() {
        callEventListwebservice(filter)
    }

    override fun delete(event_id: String, recur: Boolean) {}

    override fun onClick(view: View) {}

    override fun onDateSelected(
        selectedDate: LocalDate,
        filteredEvents: ArrayList<Events_Do>,
        filteredAppointments: ArrayList<AppointmentModel>
    ) {
        Currenr_date = selectedDate.toString() // yyyy-MM-dd

        selected_date_events.clear()
        selected_date_events.addAll(filteredEvents)

        selected_date_appointments.clear()
        selected_date_appointments.addAll(filteredAppointments)

        sortLists()

        loadRecyclerView()

        Log.d("DateSelected", "Date: $Currenr_date, Events: ${filteredEvents.size}, Appointments: ${filteredAppointments.size}")
    }
}
