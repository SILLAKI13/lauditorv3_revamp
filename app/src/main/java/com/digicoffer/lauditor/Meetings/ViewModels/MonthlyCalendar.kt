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
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.applandeo.materialcalendarview.CalendarView
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnCalendarPageChangeListener
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import com.digicoffer.lauditor.Meetings.Models.Event_Details_DO
import com.digicoffer.lauditor.Meetings.Models.Events_Do
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DrawableUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MonthlyCalendar(private var Meeting: View) : Fragment(), AsyncTaskCompleteListener, View.OnClickListener, Events_Adapter.EventListener {
    private var calendarView: CalendarView? = null
    private var thiscontext: Context? = null
    private var progress_dialog: Dialog? = null
    private var filter = ""
    private var event_id = ""
    private var event_creation_date = ""
    private var events_adapter: Events_Adapter? = null
    private var Currenr_date = ""
    private var eventDetailsListener: EventDetailsListener? = null
    private var ad_dialog: AlertDialog? = null
    private var Current_day = ""
    private var recurring_edit_choice: String? = null
    private var tv_event_name: TextView? = null
    private var tv_event_description: TextView? = null
    private var tv_event_time: TextView? = null
    private var tv_event_repetetion: TextView? = null
    private var tv_event_date: TextView? = null
    private var btn_event_save: Button? = null
    private var Current_month = ""
    private var rv_displayEvents: RecyclerView? = null

    var events_list = ArrayList<Events_Do>()
    var events: MutableList<EventDay> = ArrayList()
    private var meetings: Meetings? = null
    var event_details_list = ArrayList<Event_Details_DO>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.month_view_calendar, container, false)
        thiscontext = container?.context

        calendarView = v.findViewById(R.id.prolificcalendarview)
        calendarView?.setSelectionBackground(R.drawable.custom_selector)
        calendarView?.setSwipeEnabled(true)
        calendarView?.setOnDayClickListener(object : OnDayClickListener {
            override fun onDayClick(eventDay: EventDay) {
                events_list.clear()
                val myCalendar = eventDay.calendar
                Updatelabel(myCalendar)
            }
        })
        calendarView?.setForwardButtonImage(resources.getDrawable(com.applandeo.materialcalendarview.R.drawable.ic_arrow_right))
        calendarView?.setPreviousButtonImage(resources.getDrawable(com.applandeo.materialcalendarview.R.drawable.ic_arrow_left))

        val mCalendar = Calendar.getInstance()

        calendarView?.setOnPreviousPageChangeListener(object : OnCalendarPageChangeListener {
            override fun onChange() {
                events_list.clear()
                mCalendar.set(Calendar.MONTH, mCalendar.get(Calendar.MONTH) - 1)
                val myFormat = "MMM dd, yyyy"
                val sdf = SimpleDateFormat(myFormat, Locale.US)
                filter = sdf.format(mCalendar.time)
                callEventListwebservice(filter)
            }
        })
        calendarView?.setOnForwardPageChangeListener(object : OnCalendarPageChangeListener {
            override fun onChange() {
                events_list.clear()
                mCalendar.set(Calendar.MONTH, mCalendar.get(Calendar.MONTH) + 1)
                val myFormat = "MMM dd, yyyy"
                val sdf = SimpleDateFormat(myFormat, Locale.US)
                filter = sdf.format(mCalendar.time)
                callEventListwebservice(filter)
            }
        })
        rv_displayEvents = v.findViewById(R.id.rv_events)
        callEventListwebservice(filter)
        meetings = parentFragment as? Meetings
        return v
    }

    override fun onClick(view: View) {}

    interface EventDetailsListener {
        fun onEventDetailsPassed(event_details_list: ArrayList<Event_Details_DO>, calendar_Type: String)
    }

    private fun Updatelabel(myCalendar: Calendar) {
        val myFormat = "MMM dd, yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        filter = sdf.format(myCalendar.time)
        callEventListwebservice(filter)
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
            Currenr_date = AndroidUtils.getDateToString(event_date, "yyyy-MM-dd")
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
                "v3/events/$timezoneoffset/M$event_creation_date",
                "Events_List",
                postData.toString()
            )
        } catch (e: Exception) {
            e.fillInStackTrace()
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
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    private fun loadEvents(jsonArray: JSONArray) {
        Thread {
            try {
                events_list.clear()
                events.clear()

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

                    val from_ts = events_do.event_start_time
                    val to_ts = events_do.event_end_time
                    val event_date = AndroidUtils.stringToDateTimeDefault(from_ts, "yyyy-MM-dd'T'HH:mm:ss")
                    events_do.converted_Start_time = AndroidUtils.getDateToString(event_date, "HH:mm a")

                    val event_date2 = AndroidUtils.stringToDateTimeDefault(to_ts, "yyyy-MM-dd'T'HH:mm:ss")
                    events_do.cOnverted_End_time = AndroidUtils.getDateToString(event_date2, "HH:mm a")

                    val converted_from_ts = AndroidUtils.getDateToString(event_date, "yyyy-MM-dd")
                    val converted_to_ts = AndroidUtils.getDateToString(event_date2, "yyyy-MM-dd")

                    val calendar = Calendar.getInstance().apply {
                        time = event_date
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    events.add(EventDay(calendar, DrawableUtils.getThreeDots(requireContext())))

                    if (converted_from_ts.contains(Currenr_date) || converted_to_ts.contains(Currenr_date)) {
                        events_list.add(events_do)
                    }
                }

                Handler(Looper.getMainLooper()).post {
                    try {
                        events_list.sortWith(object : Comparator<Events_Do> {
                            override fun compare(eventDay: Events_Do, t1: Events_Do): Int {
                                val s1 = eventDay.converted_Start_time
                                val s2 = t1.converted_Start_time
                                if (s1 == null || s2 == null) return 0
                                return s1.compareTo(s2)
                            }
                        })
                        calendarView?.setEvents(events)
                        loadRecyclerView()
                    } catch (e: Exception) {
                        throw RuntimeException(e)
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

    private fun loadRecyclerView() {
        try {
            for (event in events_list) {
                Log.d("Event List", event.event_Name ?: "")
            }
            events_adapter = Events_Adapter(events_list, this, requireContext(), requireActivity())
            rv_displayEvents?.adapter = events_adapter
            AndroidUtils.LoadingRecyclerview(rv_displayEvents, context)
        } catch (e: Exception) {
            AndroidUtils.showToast(e.message, context)
            e.fillInStackTrace()
        }
    }

    override fun onEvent(event_details_list: ArrayList<Event_Details_DO>) {
        if (eventDetailsListener != null) {
            val Calendar_Type = "Monthly"
            Log.d("EventsList", event_details_list.toString())
            eventDetailsListener?.onEventDetailsPassed(event_details_list, Calendar_Type)
        }
    }

    override fun delete_events(events_do: Events_Do) {
        delete_event(events_do)
    }

    override fun load_events() {
        callEventListwebservice(filter)
    }

    private fun delete_event(events_do: Events_Do) {
        AndroidUtils.Delete_Popup(
            requireActivity(),
            "Are you sure do you want to delete ${events_do.event_Name} ?",
            "Delete_Event",
            events_do.event_id,
            null,
            null,
            Meeting,
            events_do.isrecurring,
            events_do.is_linked_with_timesheet
        )
    }

    fun delete_recurring_event(event_id: String) {
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
            progress_dialog = dialog

            btn_close_event.setOnClickListener { progress_dialog?.dismiss() }
            delete.setOnClickListener {
                if (delete_only_this.isChecked || delete_following.isChecked || delete_all.isChecked) {
                    progress_dialog?.dismiss()
                    callDeleteEventwebservice(event_id, recurring_edit_choice)
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

    fun callDeleteEventwebservice(id: String, recurring_choice: String?) {
        progress_dialog = AndroidUtils.get_progress(activity)
        val postData = JSONObject()
        try {
            if (recurring_choice != null) {
                postData.put("choice", recurring_choice)
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

    override fun delete(event_id: String, recur: Boolean) {}
}
