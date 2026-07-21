package com.digicoffer.lauditor.Meetings.ViewModels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.digicoffer.lauditor.Meetings.Models.Event_Details_DO
import com.digicoffer.lauditor.Meetings.MonthCalander.CalendarMonthView
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NewModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.FilterOptionAdapter
import java.util.ArrayList
import java.util.Objects

class Meetings : Fragment(), AsyncTaskCompleteListener, View.OnClickListener, CalendarMonthView.EventDetailsListener, WeeklyCalendar.EventDetailsListener {
    private var ll_view_type: LinearLayoutCompat? = null
    private var mViewModel: NewModel? = null
    var cl_meeting: LinearLayout? = null
    private var ll_event_type: LinearLayout? = null
    private var sp_event_Filter: ListView? = null
    private var isEventTypeSelected = true
    private var currentFilter = FilterType.ALL
    private var selectedEventType = "All" // Default to show all
    private var img_filter_icon: ImageView? = null
    private var tv_switchCreate: LinearLayout? = null
    private var tv_switchView: LinearLayout? = null
    private var ll_event_Filter: LinearLayout? = null
    private var tv_event_Filter: TextView? = null
    private var tv_create_event: TextView? = null
    private var tv_view_event: TextView? = null
    private var tv_day_view: TextView? = null
    private var tv_month_view: TextView? = null
    var existingList = ArrayList<Event_Details_DO>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.calendar, container, false)
        mViewModel = ViewModelProvider(requireActivity()).get(NewModel::class.java)
        ll_view_type = view.findViewById(R.id.ll_view_type)
        ll_event_type = view.findViewById(R.id.ll_event_type)
        cl_meeting = view.findViewById(R.id.cl_meeting)
        ll_event_Filter = view.findViewById(R.id.ll_event_Filter)
        tv_event_Filter = ll_event_Filter?.findViewById(R.id.tv_spinner_view)
        img_filter_icon = ll_event_Filter?.findViewById(R.id.img_filter_icon)
        sp_event_Filter = view.findViewById(R.id.sp_event_Filter)
        tv_create_event = view.findViewById(R.id.tv_create_event)
        tv_create_event?.setText(R.string.create)
        tv_view_event = view.findViewById(R.id.tv_view_calendar)
        tv_view_event?.setText(R.string.view)
        tv_switchCreate = view.findViewById(R.id.tv_switchCreate)
        tv_switchView = view.findViewById(R.id.tv_switchView)
        tv_day_view = view.findViewById(R.id.tv_day_view)
        tv_day_view?.setText(R.string.days)
        tv_month_view = view.findViewById(R.id.tv_month_view)
        tv_month_view?.setText(R.string.month)
        tv_view_event?.setOnClickListener(this)
        tv_create_event?.setOnClickListener(this)
        val data = "Meetings"
        setViewModelData(data)

        if (Constants.isCreate) {
            Constants.is_meeting = "Create"
            loadCreateEvent(null)
        } else {
            loadView()
        }

        AndroidUtils.setupModuleView(
            tv_switchCreate,
            getString(R.string.view_event),
            false, true, context, getString(R.string.create_event)
        ) {
            loadView()
        }

        AndroidUtils.setupModuleView(
            tv_switchView,
            getString(R.string.create_event),
            true, false, context, getString(R.string.view_event)
        ) {
            if (!Constants.is_active) {
                AndroidUtils.showRenewalPopup(requireActivity())
            } else {
                Constants.is_meeting = "Create"
                loadCreateEvent(null)
            }
        }

        val selectionFilterList = ArrayList<String>()
        selectionFilterList.add("My Meetings")
        selectionFilterList.add("Client Bookings")
        selectionFilterList.add("All Appointments")

        val filterIcons = ArrayList<Int>()
        filterIcons.add(R.drawable.circle_blue_bg) // My Meetings
        filterIcons.add(R.drawable.orange_circular) // Client Bookings
        filterIcons.add(R.drawable.grey_circular)

        val adapter = FilterOptionAdapter(requireActivity(), selectionFilterList, filterIcons)
        sp_event_Filter?.adapter = adapter
        selectedEventType = selectionFilterList[2]
        tv_event_Filter?.text = selectedEventType
        img_filter_icon?.setImageResource(filterIcons[2])
        currentFilter = FilterType.ALL
        applyFilter()

        sp_event_Filter?.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            selectedEventType = selectionFilterList[position]
            tv_event_Filter?.text = selectedEventType
            loadSelectedEvents(selectedEventType)
            sp_event_Filter?.visibility = View.GONE
            isEventTypeSelected = true
        }

        ll_event_Filter?.setOnClickListener {
            AndroidUtils.display_listview(isEventTypeSelected, sp_event_Filter)
            isEventTypeSelected = !isEventTypeSelected
        }

        tv_view_event?.setOnClickListener {
            loadView()
        }

        tv_day_view?.setOnClickListener {
            ll_view_type?.visibility = View.VISIBLE
            loadDayView()
        }

        tv_month_view?.setOnClickListener {
            tv_view_event?.setTextColor(requireContext().getColor(R.color.white))
            ll_view_type?.visibility = View.VISIBLE
            loadMonthView()
        }

        tv_create_event?.setOnClickListener {
            Constants.is_meeting = "Create"
            loadCreateEvent(null)
        }

        return view
    }

    private fun loadSelectedEvents(selectedEventType: String) {
        if (selectedEventType == "My Meetings") {
            currentFilter = FilterType.MY_MEETINGS
            img_filter_icon?.setImageDrawable(requireContext().getDrawable(R.drawable.circle_blue_bg))
        } else if (selectedEventType == "Client Bookings") {
            currentFilter = FilterType.APPOINTMENTS
            img_filter_icon?.setImageDrawable(requireContext().getDrawable(R.drawable.orange_circular))
        } else {
            currentFilter = FilterType.ALL
            img_filter_icon?.setImageDrawable(requireContext().getDrawable(R.drawable.grey_circular))
        }
        applyFilter()
    }

    private fun setViewModelData(data: String) {
        mViewModel?.setData(data)
    }

    private fun loadMonthView() {
        tv_day_view?.setTextColor(requireContext().getColor(R.color.black))
        tv_month_view?.setTextColor(requireContext().getColor(R.color.white))
        tv_month_view?.setBackgroundDrawable(requireContext().resources.getDrawable(R.drawable.button_right_green_count))
        tv_day_view?.setBackgroundDrawable(requireContext().resources.getDrawable(R.drawable.button_left_background))
        val ft = childFragmentManager.beginTransaction()
        val nonSubmittedTimesheets = CalendarMonthView(cl_meeting!!, currentFilter)
        ft.replace(R.id.child_container_timesheets, nonSubmittedTimesheets)
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        ft.addToBackStack(null)
        ft.commit()
    }

    enum class FilterType {
        ALL, // Show both events and appointments
        MY_MEETINGS, // Show only events
        APPOINTMENTS // Show only appointments
    }

    private fun loadDayView() {
        tv_day_view?.setTextColor(requireContext().getColor(R.color.white))
        tv_month_view?.setTextColor(requireContext().getColor(R.color.black))
        tv_month_view?.setBackgroundDrawable(requireContext().resources.getDrawable(R.drawable.button_right_background))
        tv_day_view?.setBackgroundDrawable(requireContext().resources.getDrawable(R.drawable.button_left_green_background))
        val ft = childFragmentManager.beginTransaction()
        val nonSubmittedTimesheets = WeeklyCalendar(cl_meeting!!, currentFilter)
        ft.replace(R.id.child_container_timesheets, nonSubmittedTimesheets)
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        ft.addToBackStack(null)
        ft.commit()
    }

    fun loadView() {
        tv_switchView?.visibility = View.VISIBLE
        tv_switchCreate?.visibility = View.GONE
        ll_view_type?.visibility = View.VISIBLE
        ll_event_type?.visibility = View.VISIBLE
        tv_create_event?.setTextColor(requireContext().getColor(R.color.black))
        tv_view_event?.setTextColor(requireContext().getColor(R.color.white))
        tv_view_event?.setBackgroundDrawable(requireContext().resources.getDrawable(R.drawable.button_right_green_count))
        tv_create_event?.setBackgroundDrawable(requireContext().resources.getDrawable(R.drawable.button_left_background))
        loadDayView()
    }

    fun loadCreateEvent(event_details_list: ArrayList<Event_Details_DO>?) {
        tv_switchView?.visibility = View.GONE
        tv_switchCreate?.visibility = View.VISIBLE
        ll_event_type?.visibility = View.GONE
        if (!Constants.is_active) {
            AndroidUtils.showRenewalPopup(requireActivity())
        } else {
            tv_view_event?.setTextColor(requireContext().getColor(R.color.black))
            tv_create_event?.setTextColor(requireContext().getColor(R.color.white))
            tv_view_event?.setBackgroundDrawable(requireContext().resources.getDrawable(R.drawable.button_right_background))
            tv_create_event?.setBackgroundDrawable(requireContext().resources.getDrawable(R.drawable.button_left_green_background))
            val ft = childFragmentManager.beginTransaction()
            val nonSubmittedTimesheets = CreateEvent(this, event_details_list)
            ft.replace(R.id.child_container_timesheets, nonSubmittedTimesheets)
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            ft.addToBackStack(null)
            ll_view_type?.visibility = View.GONE
            ft.commit()
        }
    }

    private fun applyFilter() {
        val currentFragment = childFragmentManager.findFragmentById(R.id.child_container_timesheets)
        if (currentFragment is WeeklyCalendar) {
            currentFragment.applyFilter(currentFilter)
        } else if (currentFragment is CalendarMonthView) {
            currentFragment.applyFilter(currentFilter)
        }
    }

    fun getCurrentFilter(): FilterType {
        return currentFilter
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.tv_view_calendar -> { // ID - tv_view_calender is named as tv_view_event.
                loadView()
                ll_view_type?.visibility = View.VISIBLE
            }
            R.id.tv_create_event -> {
                ll_view_type?.visibility = View.GONE
                Constants.is_meeting = "Create"
                loadCreateEvent(null)
            }
        }
    }

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {}

    override fun onEventDetailsPassed(event_details_list: ArrayList<Event_Details_DO>, calendar_Type: String) {
        Constants.is_meeting = "Edit"
        AndroidUtils.updateModuleTitle(
            tv_switchCreate,
            getString(R.string.edit_event)
        )
        loadCreateEvent(event_details_list)
    }

    fun loadViewEvent(calendar_type: String) {
        var fragment: Fragment = Fragment()
        if (calendar_type == "Monthly") {
            fragment = MonthlyCalendar(cl_meeting!!)
        } else if (calendar_type == "Weekly") {
            fragment = WeeklyCalendar(cl_meeting!!, currentFilter)
        } else {
            fragment = WeeklyCalendar(cl_meeting!!, currentFilter)
        }
        val fragmentManager = childFragmentManager
        fragmentManager.beginTransaction()
            .replace(R.id.child_container_timesheets, fragment)
            .commit()
    }
}
