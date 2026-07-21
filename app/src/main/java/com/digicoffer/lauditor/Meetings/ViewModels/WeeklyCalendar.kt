package com.digicoffer.lauditor.Meetings.ViewModels

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.applandeo.materialcalendarview.EventDay
import com.digicoffer.lauditor.Appointments.Models.AppointmentModel
import com.digicoffer.lauditor.Appointments.Models.PaymentModel
import com.digicoffer.lauditor.Meetings.Models.Day
import com.digicoffer.lauditor.Meetings.Models.Event_Details_DO
import com.digicoffer.lauditor.Meetings.Models.Events_Do
import com.digicoffer.lauditor.Meetings.Models.InviteesInternal_Model
import com.digicoffer.lauditor.Meetings.Adapter.WeeklyCalendarAdapter
import com.digicoffer.lauditor.Matter.Models.DocumentsModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.TimeSheets.Models.WeekDateInfo
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DrawableUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class WeeklyCalendar(
    private var meetings: View,
    private var currentFilter: Meetings.FilterType
) : Fragment(), View.OnClickListener, AsyncTaskCompleteListener, Events_Adapter.EventListener, WeeklyCalendarAdapter.OnDaySelectedListener {

    private var calendar = Calendar.getInstance()
    private var weekDateInfo: WeekDateInfo? = null
    private var start_date: String? = null
    private var eventId = ""
    private var end_date: String? = null
    private var filter = ""
    private var cal: Calendar? = null
    private var recurring_edit_choice: String? = null
    private var adapter: WeeklyCalendarAdapter? = null
    private val days: MutableList<Day> = ArrayList()
    private var progress_dialog: Dialog? = null
    private var Current_month = ""
    private var rv_week_dates: RecyclerView? = null
    private var eventDetailsListener: EventDetailsListener? = null
    private var ad_dialog: AlertDialog? = null
    private var event_creation_date = ""
    private var event_end_date = ""
    private var Currenr_date = ""
    private var rv_displayEvents: RecyclerView? = null
    private var Current_day = ""
    private var events_adapter: Events_Adapter? = null
    private var tv_from_date_timesheet: TextView? = null
    private var tv_to_date_timesheet: TextView? = null
    private var event_details_list = ArrayList<Event_Details_DO>()
    private val mCalendar = Calendar.getInstance()
    var events_list = ArrayList<Events_Do>()
    var Individual_events_list = ArrayList<Events_Do>()
    var selected_date_events = ArrayList<Events_Do>()
    var appointments_list = ArrayList<AppointmentModel>()
    var selected_date_appointments = ArrayList<AppointmentModel>()
    var events: MutableList<EventDay> = ArrayList()
    private var inviteesInternalArrayList = ArrayList<InviteesInternal_Model>()
    private var mcontext: Context? = null
    private var documentsModel = DocumentsModel()
    private var documentsList: MutableList<DocumentsModel> = ArrayList()

    // Empty state views
    private var layout_empty_state: LinearLayout? = null
    private var tv_empty_subtitle: TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.weekly_calendar, container, false)
        mcontext = context
        val iv_next_week = v.findViewById<ImageButton>(R.id.iv_next_week)
        iv_next_week.setImageDrawable(requireContext().getDrawable(R.drawable.baseline_arrow_forward_ios_24))
        val iv_previous_week = v.findViewById<ImageButton>(R.id.iv_previous_week)
        rv_displayEvents = v.findViewById(R.id.rv_events)
        rv_week_dates = v.findViewById(R.id.rv_week_dates)
        tv_from_date_timesheet = v.findViewById(R.id.tv_from_date_timesheet)
        tv_to_date_timesheet = v.findViewById(R.id.tv_to_date_timesheet)
        tv_to_date_timesheet?.setTextColor(requireContext().getColor(R.color.Blue_text_color))
        tv_from_date_timesheet?.setTextColor(requireContext().getColor(R.color.Blue_text_color))

        layout_empty_state = v.findViewById(R.id.layout_empty_state)
        tv_empty_subtitle = v.findViewById(R.id.tv_empty_subtitle)

        if (Constants.isFromNotification) {
            handleNotificationNavigation()
        }
        CurrentWeek()
        Currenr_date = AndroidUtils.getDateToString(Calendar.getInstance().time, "dd-MM-yyyy")
        callEventListwebservice(filter)
        Constants.isfwd_or_isbwd = false
        adapter = WeeklyCalendarAdapter(days, this, requireContext())
        rv_week_dates?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rv_week_dates?.adapter = adapter
        iv_previous_week.setOnClickListener { showPreviousWeek() }
        iv_next_week.setOnClickListener { showNextWeek() }
        return v
    }

    private fun handleNotificationNavigation() {
        val bundle = Constants.notificationBundle
        val route = bundle.getString(Constants.NavKeys.ROUTE_NAME)
        if (route != null) {
            eventId = bundle.getString(Constants.NavKeys.EVENT_ID, "")
            if (eventId.isNotEmpty()) {
                callEventDetailsWebservice()
            }
        }
        Constants.isFromNotification = false
        Constants.notificationBundle.clear()
    }

    private fun CurrentWeek() {
        cal = Calendar.getInstance()
        val firstDayOfWeek = cal!!.firstDayOfWeek

        while (cal!!.get(Calendar.DAY_OF_WEEK) != firstDayOfWeek) {
            cal!!.add(Calendar.DATE, -1)
        }

        val d1 = SimpleDateFormat("ddMMyyyy", Locale.US)
        start_date = d1.format(cal!!.time)
        Log.d("F_date", "" + start_date)

        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        val fromDate = dateFormat.format(cal!!.time)

        days.clear()
        for (i in 0..6) {
            val date = dateFormat.format(cal!!.time)
            days.add(Day(date))
            cal!!.add(Calendar.DATE, 1)
        }

        end_date = d1.format(cal!!.time)
        Log.d("L_date", "" + end_date)

        cal!!.add(Calendar.DATE, -1)
        val toDate = dateFormat.format(cal!!.time)

        tv_from_date_timesheet?.text = AndroidUtils.formatToMMMddYYYY(fromDate)
        tv_to_date_timesheet?.text = AndroidUtils.formatToMMMddYYYY(toDate)
    }

    override fun onClick(v: View) {}

    private fun showPreviousWeek() {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        val d1 = SimpleDateFormat("ddMMyyyy", Locale.US)
        cal = Calendar.getInstance()

        try {
            val currentDate = dateFormat.parse(days[0].date)
            if (currentDate != null) {
                cal!!.time = currentDate
                cal!!.add(Calendar.WEEK_OF_YEAR, -1)

                start_date = d1.format(cal!!.time)
                val fromDate = dateFormat.format(cal!!.time)

                days.clear()
                for (i in 0..6) {
                    val date = dateFormat.format(cal!!.time)
                    days.add(Day(date))
                    cal!!.add(Calendar.DATE, 1)
                }

                cal!!.add(Calendar.DATE, -1)
                end_date = d1.format(cal!!.time)
                val toDate = dateFormat.format(cal!!.time)

                Log.d("PREV_WEEK", "Start: $start_date | End: $end_date")

                tv_from_date_timesheet?.text = AndroidUtils.formatToMMMddYYYY(fromDate)
                tv_to_date_timesheet?.text = AndroidUtils.formatToMMMddYYYY(toDate)

                Constants.isfwd_or_isbwd = true
                adapter?.notifyDataSetChanged()
                events_list.clear()
                selected_date_events.clear()

                val myFormat = "MMM dd, yyyy"
                val sdf = SimpleDateFormat(myFormat, Locale.US)
                filter = sdf.format(cal!!.time)
                Currenr_date = AndroidUtils.getDateToString(Calendar.getInstance().time, "dd-MM-yyyy")
                callEventListwebservice(filter)
            }

        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }

    private fun showNextWeek() {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        val d1 = SimpleDateFormat("ddMMyyyy", Locale.US)
        cal = Calendar.getInstance()

        try {
            val currentDate = dateFormat.parse(days[0].date)
            if (currentDate != null) {
                cal!!.time = currentDate
                cal!!.add(Calendar.WEEK_OF_YEAR, 1)

                start_date = d1.format(cal!!.time)
                val fromDate = dateFormat.format(cal!!.time)

                days.clear()
                for (i in 0..6) {
                    val date = dateFormat.format(cal!!.time)
                    days.add(Day(date))
                    cal!!.add(Calendar.DATE, 1)
                }

                cal!!.add(Calendar.DATE, -1)
                end_date = d1.format(cal!!.time)
                val toDate = dateFormat.format(cal!!.time)

                Log.d("NEXT_WEEK", "Start: $start_date | End: $end_date")

                tv_from_date_timesheet?.text = AndroidUtils.formatToMMMddYYYY(fromDate)
                tv_to_date_timesheet?.text = AndroidUtils.formatToMMMddYYYY(toDate)

                Constants.isfwd_or_isbwd = true
                adapter?.notifyDataSetChanged()
                events_list.clear()
                selected_date_events.clear()

                val myFormat = "MMM dd, yyyy"
                val sdf = SimpleDateFormat(myFormat, Locale.US)
                filter = sdf.format(cal!!.time)
                Currenr_date = AndroidUtils.getDateToString(Calendar.getInstance().time, "dd-MM-yyyy")
                callEventListwebservice(filter)
            }

        } catch (e: ParseException) {
            e.printStackTrace()
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

    private fun callEventListwebservice(filter: String) {
        progress_dialog = AndroidUtils.get_progress(activity)
        val postData = JSONObject()
        try {
            val event_date = AndroidUtils.stringToDateTimeDefault(filter, "MMM dd, yyyy")
            event_creation_date = AndroidUtils.getDateToString(event_date, "MMyyyy")
            Current_day = AndroidUtils.getDateToString(event_date, "dd")
            Current_month = AndroidUtils.getDateToString(event_date, "MM")
            val calendar = GregorianCalendar()
            val timeZone = calendar.timeZone
            val offset = timeZone.rawOffset
            val hours = TimeUnit.MILLISECONDS.toMinutes(offset.toLong())
            val timezoneoffset = -1 * hours
            WebServiceHelper.callHttpWebService(
                this, requireContext(),
                WebServiceHelper.RestMethodType.GET,
                "v3/events/$timezoneoffset/W$start_date-$end_date",
                "Events_List",
                postData.toString()
            )
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun callAppointmentListWebService() {
        progress_dialog = AndroidUtils.get_progress(activity)
        val postData = JSONObject()
        try {
            val startDate = AndroidUtils.convertAnyDateToYYYYMMDD(start_date)
            val endDate = AndroidUtils.convertAnyDateToYYYYMMDD(end_date)
            val event_date = AndroidUtils.stringToDateTimeDefault(filter, "MMM dd, yyyy")
            event_creation_date = AndroidUtils.getDateToString(event_date, "MMyyyy")
            Current_day = AndroidUtils.getDateToString(event_date, "dd")
            Current_month = AndroidUtils.getDateToString(event_date, "MM")
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
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun getWeekDateRange(calendar: Calendar): WeekDateInfo {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.US)

        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            calendar.add(Calendar.DATE, -1)
        }
        val startDate = format.format(calendar.time)

        val weekDates = ArrayList<String>()
        for (i in 0..6) {
            val date = format.format(calendar.time)
            weekDates.add(date)
            calendar.add(Calendar.DATE, 1)
        }
        calendar.add(Calendar.DATE, -1)
        val endDate = format.format(calendar.time)
        return WeekDateInfo(startDate + " - " + endDate, weekDates)
    }

    fun callEventDetailsWebservice() {
        progress_dialog = AndroidUtils.get_progress(activity)
        val postData = JSONObject()
        val calendar = GregorianCalendar()
        val timeZone = calendar.timeZone
        val offset = timeZone.rawOffset
        val hours = TimeUnit.MILLISECONDS.toMinutes(offset.toLong())
        val timezoneoffset = -1 * hours
        WebServiceHelper.callHttpWebService(
            this, requireContext(),
            WebServiceHelper.RestMethodType.GET,
            "v3/event/$eventId/$timezoneoffset",
            "INDIVIDUAL EVENT DETAILS",
            postData.toString()
        )
    }

    private fun load_event_details(event_details: JSONObject) {
        event_details_list.clear()
        Individual_events_list.clear()
        try {
            val events_do = Events_Do().apply {
                description = event_details.getString("description")
                event_Name = event_details.getString("title")
                dialin = event_details.getString("dialin")
                event_type = event_details.getString("event_type")
                location = event_details.getString("location")
                if (event_details.has("matter_id")) {
                    matter_id = event_details.getString("matter_id")
                }
                if (event_details.has("matter_name")) {
                    matter_name = event_details.getString("matter_name")
                }
                if (event_details.has("matter_type")) {
                    matter_type = event_details.getString("matter_type")
                }
                isrecurring = event_details.getBoolean("isrecurring")
                meeting_link = event_details.getString("meeting_link")
                is_linked_with_timesheet = event_details.optBoolean("is_linked_with_timesheet")
                notes = event_details.getString("notes")
                repeat_interval = event_details.getString("repeat_interval")
                timezone_location = event_details.getString("timezone_location")
                timezone_offset = event_details.getString("timezone_offset")
                attachments = event_details.getJSONArray("attachments")
                invitees_external = event_details.getJSONArray("invitees_external")
                invitees_internal = event_details.getJSONArray("invitees_internal")
                notifications = event_details.getJSONArray("notifications")
                event_start_time = event_details.getString("from_ts")
                event_end_time = event_details.getString("to_ts")
                isAll_day = event_details.getBoolean("allday")
                event_id = event_details.getString("id")
                isOwner = event_details.getBoolean("owner")
            }

            val from_ts = events_do.event_start_time
            val to_ts = events_do.event_end_time
            val event_date = AndroidUtils.stringToDateTimeDefault(from_ts, "yyyy-MM-dd'T'HH:mm:ss")
            val event_start_time = AndroidUtils.getDateToString(event_date, "HH:mm a")
            events_do.converted_Start_time = event_start_time
            val event_date2 = AndroidUtils.stringToDateTimeDefault(to_ts, "yyyy-MM-dd'T'HH:mm:ss")
            val event_end_time = AndroidUtils.getDateToString(event_date2, "HH:mm a")
            events_do.cOnverted_End_time = event_end_time
            val converted_from_ts = AndroidUtils.getDateToString(event_date, "dd-MM-yyyy")
            val converted_to_ts = AndroidUtils.getDateToString(event_date2, "dd-MM-yyyy")
            val year = Integer.parseInt(AndroidUtils.getDateToString(event_date, "yyyy"))

            val calendar = Calendar.getInstance().apply {
                if (event_date != null) {
                    set(year, event_date.month, event_date.date)
                }
            }
            events.add(EventDay(calendar, DrawableUtils.getThreeDots(requireContext())))
            Individual_events_list.add(events_do)

            val event_details_do = Event_Details_DO().apply {
                event_type = event_details.getString("event_type")
                id = event_details.getString("id")
                title = event_details.getString("title")
                description = event_details.getString("description")
                this.from_ts = event_details.getString("from_ts")
                all_day = event_details.getBoolean("allday")
                is_linked_with_timesheet = event_details.optBoolean("is_linked_with_timesheet")
                val event_date_forevents = AndroidUtils.getDateToString(event_date, "dd-MM-yyyy")
                date = event_date_forevents
                isRecurring = event_details.getBoolean("isrecurring")
                repeat_interval = event_details.getString("repeat_interval")
                location = event_details.getString("location")
                dialin = event_details.getString("dialin")
                this.to_ts = event_details.getString("to_ts")
                offset = event_details.getString("timezone_offset")
                offset_location = event_details.getString("timezone_location")
                converted_Start_time = event_start_time
                owner = event_details.getBoolean("owner")
                meeting_link = event_details.getString("meeting_link")
                converted_End_time = event_end_time
                repeat_interval = event_details.getString("repeat_interval")
                notifications = event_details.getJSONArray("notifications")
                owner_name = event_details.getString("owner_name")
                attachments = event_details.getJSONArray("attachments")
                team_name = event_details.getJSONArray("invitees_internal")
                tm_name = event_details.getJSONArray("invitees_external")
                corporate = event_details.optJSONArray("invitees_corporate")
                if (event_details.has("invitees_consumer_external")) {
                    Log.d("ArrayListLog", event_details.getJSONArray("invitees_consumer_external").toString())
                    consumer_external = event_details.getJSONArray("invitees_consumer_external")
                }
                if (event_details.has("matter_name")) {
                    matter_name = event_details.getString("matter_name")
                }
                if (event_details.has("matter_id")) {
                    matter_id = event_details.getString("matter_id")
                }
                if (event_details.has("matter_type")) {
                    matter_type = event_details.getString("matter_type")
                }
                if (event_details.has("timesheet_added")) {
                    timesheet_added = event_details.optBoolean("timesheet_added")
                }
            }
            event_details_list.add(event_details_do)

            showIndividualEventPopup(event_details_list, Individual_events_list)
        } catch (e: JSONException) {
            e.fillInStackTrace()
        }
    }

    private fun showIndividualEventPopup(event_details_list: ArrayList<Event_Details_DO>, eventsList: ArrayList<Events_Do>) {
        try {
            val builder = AlertDialog.Builder(activity)
            val inflater = layoutInflater
            val dialog = inflater.inflate(R.layout.recyclerview, null)
            val rv_displayEvents = dialog.findViewById<RecyclerView>(R.id.rv_view_members)
            val dialogLayout = builder.create()
            dialogLayout.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialogLayout.setView(dialog)
            dialogLayout.show()
            rv_displayEvents.layoutManager = GridLayoutManager(context, 1)
            for (event in events_list) {
                Log.d("Event List", event.event_Name ?: "")
            }
            Log.d("Event_size.", "" + events_list.size)
            val events_adapter = Events_Adapter(eventsList, this, requireContext(), requireActivity(), event_details_list, dialogLayout)
            rv_displayEvents.adapter = events_adapter
            rv_displayEvents.layoutAnimation = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_fall_down)
            rv_displayEvents.scheduleLayoutAnimation()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun load_Nothing(holder: Events_Adapter.MyViewHolder) {
        holder.tv_yes.setTextColor(Color.BLACK)
        holder.tv_no.setTextColor(Color.BLACK)
        holder.tv_maybe.setTextColor(Color.BLACK)
        holder.tv_yes.setBackgroundDrawable(requireContext().getDrawable(R.drawable.button_left_round_background))
        holder.tv_no.setBackgroundDrawable(requireContext().getDrawable(R.drawable.radiobutton_centre_background))
        holder.tv_maybe.setBackgroundDrawable(requireContext().getDrawable(R.drawable.button_right_round_background))
    }

    private fun load_Yes(holder: Events_Adapter.MyViewHolder) {
        holder.tv_yes.setTextColor(Color.WHITE)
        holder.tv_no.setTextColor(Color.BLACK)
        holder.tv_maybe.setTextColor(Color.BLACK)
        holder.tv_yes.setBackgroundDrawable(requireContext().getDrawable(R.drawable.button_left_green_round_background))
        holder.tv_no.setBackgroundDrawable(requireContext().getDrawable(R.drawable.radiobutton_centre_background))
        holder.tv_maybe.setBackgroundDrawable(requireContext().getDrawable(R.drawable.button_right_round_background))
    }

    private fun load_Maybe(holder: Events_Adapter.MyViewHolder) {
        holder.tv_yes.setTextColor(Color.BLACK)
        holder.tv_no.setTextColor(Color.BLACK)
        holder.tv_maybe.setTextColor(Color.WHITE)
        holder.tv_yes.setBackgroundDrawable(requireContext().getDrawable(R.drawable.button_left_round_background))
        holder.tv_no.setBackgroundDrawable(requireContext().getDrawable(R.drawable.radiobutton_centre_background))
        holder.tv_maybe.setBackgroundDrawable(requireContext().getDrawable(R.drawable.button_right_green_round_background))
    }

    private fun load_No(holder: Events_Adapter.MyViewHolder) {
        holder.tv_yes.setTextColor(Color.BLACK)
        holder.tv_no.setTextColor(Color.WHITE)
        holder.tv_maybe.setTextColor(Color.BLACK)
        holder.tv_yes.setBackgroundDrawable(requireContext().getDrawable(R.drawable.button_left_round_background))
        holder.tv_no.setBackgroundDrawable(requireContext().getDrawable(R.drawable.radiobutton_centre_green_background))
        holder.tv_maybe.setBackgroundDrawable(requireContext().getDrawable(R.drawable.button_right_round_background))
    }

    private fun loadRsvpView(events_do: Events_Do, holder: Events_Adapter.MyViewHolder) {
        val rsvp = events_do.userRsvp?.lowercase(Locale.ROOT) ?: ""
        if (rsvp == "yes") {
            load_Yes(holder)
        } else if (rsvp == "maybe") {
            load_Maybe(holder)
        } else if (rsvp == "no") {
            load_No(holder)
        } else {
            load_Nothing(holder)
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
                    val converted_from_ts = AndroidUtils.getDateToString(appointment_date, "dd-MM-yyyy")

                    val to_ts = appointmentModel.appointment_to
                    val appointment_end_date = AndroidUtils.stringToDateTimeDefault(to_ts, "yyyy-MM-dd'T'HH:mm:ss")
                    val converted_to_ts = AndroidUtils.getDateToString(appointment_end_date, "dd-MM-yyyy")

                    if (converted_from_ts.contains(Currenr_date)) {
                        selected_date_appointments.add(appointmentModel)
                    }

                    for (day in days) {
                        val d = day.date ?: ""
                        if (converted_from_ts.contains(d) || converted_to_ts.contains(d)) {
                            appointments_list.add(appointmentModel)
                            break
                        }
                    }
                }

                Handler(Looper.getMainLooper()).post {
                    try {
                        selected_date_appointments.sortWith(compareBy(nullsLast()) { it.appointment_from })

                        setEventsPerDay()
                        adapter?.notifyDataSetChanged()
                        loadRecyclerView()

                        Log.d("Appointments_Loaded", "Appointments for week: ${appointments_list.size}")

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    AndroidUtils.showToast(e.message, context)
                    Log.e("LoadAppointmentsException", e.message ?: "")
                }
            }
        }.start()
    }

    private fun setEventsPerDay() {
        Log.d("SetEventsPerDay", "Starting - Events: ${events_list.size}, Appointments: ${appointments_list.size}, Filter: $currentFilter")

        for (day in days) {
            val dayEvents = ArrayList<EventDay>()
            var eventCount = 0
            var appointmentCount = 0

            if (currentFilter == Meetings.FilterType.ALL || currentFilter == Meetings.FilterType.MY_MEETINGS) {
                for (event in events_list) {
                    try {
                        val from_ts = event.event_start_time
                        val event_date = AndroidUtils.stringToDateTimeDefault(from_ts, "yyyy-MM-dd'T'HH:mm:ss")
                        val converted_from_ts = AndroidUtils.getDateToString(event_date, "dd-MM-yyyy")

                        if (converted_from_ts == day.date) {
                            val year = Integer.parseInt(AndroidUtils.getDateToString(event_date, "yyyy"))
                            val calendar = Calendar.getInstance().apply {
                                if (event_date != null) {
                                    set(year, event_date.month, event_date.date)
                                }
                            }
                            dayEvents.add(EventDay(calendar, DrawableUtils.getThreeDots(requireContext())))
                            eventCount++
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            if (currentFilter == Meetings.FilterType.ALL || currentFilter == Meetings.FilterType.APPOINTMENTS) {
                for (appointment in appointments_list) {
                    try {
                        val from_ts = appointment.appointment_from
                        val appointment_date = AndroidUtils.stringToDateTimeDefault(from_ts, "yyyy-MM-dd'T'HH:mm:ss")
                        val converted_from_ts = AndroidUtils.getDateToString(appointment_date, "dd-MM-yyyy")

                        if (converted_from_ts == day.date) {
                            val year = Integer.parseInt(AndroidUtils.getDateToString(appointment_date, "yyyy"))
                            val calendar = Calendar.getInstance().apply {
                                if (appointment_date != null) {
                                    set(year, appointment_date.month, appointment_date.date)
                                }
                            }
                            dayEvents.add(EventDay(calendar, DrawableUtils.getThreeDots(requireContext())))
                            appointmentCount++
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            day.events = dayEvents

            Log.d("DayEvents", "Day: ${day.date} | Events: $eventCount, Appointments: $appointmentCount, Total: ${dayEvents.size}")
        }
    }

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
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
                        callAppointmentListWebService()
                    } else if (httpResult.requestType == "Appointments_List") {
                        if (!result.getBoolean("error")) {
                            val jsonArray = result.getJSONArray("appointments")
                            loadAppointments(jsonArray)
                        } else {
                            AndroidUtils.showAlert(result.getString("msg"), activity)
                        }
                    } else if (httpResult.requestType == "INDIVIDUAL EVENT DETAILS") {
                        if (result.has("error")) {
                            if (!result.optBoolean("error")) {
                                event_details_list.clear()
                                load_event_details(result.optJSONObject("event")!!)
                            } else {
                                AndroidUtils.showAlert(result.optString("msg"), activity)
                            }
                        } else {
                            AndroidUtils.showAlert(result.optString("msg"), activity)
                        }
                    } else if (httpResult.requestType == "EVENT DETAILS") {
                        if (!result.getBoolean("error")) {
                            event_details_list.clear()
                        }
                    } else if (httpResult.requestType == "EVENT_DELETE") {
                        AndroidUtils.showToast("Event Deleted Successfully", context)
                        progress_dialog?.dismiss()
                        event_details_list.clear()
                        events.clear()
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
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun loadEvents(jsonArray: JSONArray) {
        Thread {
            try {
                events_list.clear()
                selected_date_events.clear()

                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val events_do = Events_Do().apply {
                        description = jsonObject.getString("description")
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
                        meeting_link = jsonObject.getString("meeting_link")
                        notes = jsonObject.getString("notes")
                        is_linked_with_timesheet = jsonObject.optBoolean("is_linked_with_timesheet")
                        repeat_interval = jsonObject.getString("repeat_interval")
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

                    val from_ts = events_do.event_start_time
                    val to_ts = events_do.event_end_time
                    val event_date = AndroidUtils.stringToDateTimeDefault(from_ts, "yyyy-MM-dd'T'HH:mm:ss")
                    val event_start_time = AndroidUtils.getDateToString(event_date, "HH:mm a")
                    events_do.converted_Start_time = event_start_time

                    val event_date2 = AndroidUtils.stringToDateTimeDefault(to_ts, "yyyy-MM-dd'T'HH:mm:ss")
                    val event_end_time = AndroidUtils.getDateToString(event_date2, "HH:mm a")
                    events_do.cOnverted_End_time = event_end_time

                    val converted_from_ts = AndroidUtils.getDateToString(event_date, "dd-MM-yyyy")
                    val converted_to_ts = AndroidUtils.getDateToString(event_date2, "dd-MM-yyyy")

                    if (converted_from_ts.contains(Currenr_date)) {
                        selected_date_events.add(events_do)
                    }

                    for (day in days) {
                        val d = day.date ?: ""
                        if (converted_from_ts.contains(d) || converted_to_ts.contains(d)) {
                            events_list.add(events_do)
                            break
                        }
                    }
                }

                Handler(Looper.getMainLooper()).post {
                    try {
                        selected_date_events.sortWith(compareBy(nullsLast()) { it.converted_Start_time })

                        adapter?.notifyDataSetChanged()
                        loadRecyclerView()

                        Log.d("Events_Loaded", "Events for week: ${events_list.size}")

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    AndroidUtils.showToast(e.message, context)
                    Log.e("LoadPageException", e.message ?: "")
                }
            }
        }.start()
    }

    fun applyFilter(filterType: Meetings.FilterType) {
        this.currentFilter = filterType
        filterDataByType()
        setEventsPerDay()
        adapter?.notifyDataSetChanged()
        try {
            loadRecyclerView()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun filterDataByType() {
        selected_date_events.clear()
        selected_date_appointments.clear()

        for (event in events_list) {
            try {
                val from_ts = event.event_start_time
                val event_date = AndroidUtils.stringToDateTimeDefault(from_ts, "yyyy-MM-dd'T'HH:mm:ss")
                val converted_from_ts = AndroidUtils.getDateToString(event_date, "dd-MM-yyyy")

                if (converted_from_ts == Currenr_date) {
                    if (currentFilter == Meetings.FilterType.ALL || currentFilter == Meetings.FilterType.MY_MEETINGS) {
                        selected_date_events.add(event)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        for (appointment in appointments_list) {
            try {
                val from_ts = appointment.appointment_from
                val appointment_date = AndroidUtils.stringToDateTimeDefault(from_ts, "yyyy-MM-dd'T'HH:mm:ss")
                val converted_from_ts = AndroidUtils.getDateToString(appointment_date, "dd-MM-yyyy")

                if (converted_from_ts == Currenr_date) {
                    if (currentFilter == Meetings.FilterType.ALL || currentFilter == Meetings.FilterType.APPOINTMENTS) {
                        selected_date_appointments.add(appointment)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        selected_date_events.sortWith(compareBy(nullsLast()) { it.converted_Start_time })

        selected_date_appointments.sortWith(compareBy(nullsLast()) { it.appointment_from })
    }

    private fun updateEmptyState() {
        val isEmpty = selected_date_events.isEmpty() && selected_date_appointments.isEmpty()
        if (isEmpty) {
            layout_empty_state?.visibility = View.VISIBLE
            rv_displayEvents?.visibility = View.GONE
            when (currentFilter) {
                Meetings.FilterType.MY_MEETINGS -> tv_empty_subtitle?.text = "No meetings found for this date."
                Meetings.FilterType.APPOINTMENTS -> tv_empty_subtitle?.text = "No appointments found for this date."
                else -> tv_empty_subtitle?.text = "There are no events for this date."
            }
        } else {
            layout_empty_state?.visibility = View.GONE
            rv_displayEvents?.visibility = View.VISIBLE
        }
    }

    private fun loadRecyclerView() {
        try {
            updateEmptyState()

            events_adapter = Events_Adapter(
                selected_date_events,
                selected_date_appointments,
                this,
                requireContext(),
                requireActivity()
            )

            rv_displayEvents?.adapter = events_adapter
            AndroidUtils.LoadingRecyclerview(rv_displayEvents, context)
        } catch (e: Exception) {
            AndroidUtils.showToast(e.message, context)
            e.printStackTrace()
        }
    }

    override fun onEvent(event_details_list: ArrayList<Event_Details_DO>) {
        if (eventDetailsListener != null) {
            Log.d("EventsList", event_details_list.toString())
            val Calendar_Type = "Weekly"
            eventDetailsListener?.onEventDetailsPassed(event_details_list, Calendar_Type)
        }
    }

    override fun delete_events(events_do: Events_Do) {
        delete_event(events_do)
    }

    override fun load_events() {
        callEventListwebservice(filter)
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
                if (event_delete_scope.isNotEmpty()) {
                    postData.put("event_delete_scope", event_delete_scope)
                }
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

    fun delete_recurring_event(event_id: String, event_delete_scope: Boolean) {
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
                    callDeleteEventwebservice(event_id, recurring_edit_choice, event_delete_scope)
                } else {
                    AndroidUtils.showAlert("Please choose one of the Delete recurring event", activity)
                }
            }
            dialog.setView(dialogLayout)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun delete_event(events_do: Events_Do) {
        AndroidUtils.Delete_Popup(
            requireActivity(),
            "Are you sure do you want to delete ${events_do.event_Name} ?",
            "Delete_Event",
            events_do.event_id,
            this,
            null,
            meetings,
            events_do.isrecurring,
            events_do.is_linked_with_timesheet
        )
    }

    override fun delete(event_id: String, recur: Boolean) {}

    override fun onDaySelected(day: Day) {
        val selectedDate = day.date
        Log.d("DaySelected", "Selected date: $selectedDate")

        Currenr_date = selectedDate ?: ""

        filterDataByType()

        try {
            loadRecyclerView()
            Log.d("FilteredData", "Events: ${selected_date_events.size}, Appointments: ${selected_date_appointments.size}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
