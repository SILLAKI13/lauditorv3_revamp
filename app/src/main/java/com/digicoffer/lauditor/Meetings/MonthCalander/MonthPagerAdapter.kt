package com.digicoffer.lauditor.Meetings.MonthCalander

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.digicoffer.lauditor.Appointments.Models.AppointmentModel
import com.digicoffer.lauditor.Meetings.ViewModels.Meetings
import com.digicoffer.lauditor.Meetings.Models.Events_Do
import java.time.LocalDate
import java.util.ArrayList

class MonthPagerAdapter(
    fragment: Fragment,
    private val baseDate: LocalDate,
    private var events_list: ArrayList<Events_Do> = ArrayList(),
    private var appointments_list: ArrayList<AppointmentModel> = ArrayList(),
    private var filterType: Meetings.FilterType = Meetings.FilterType.ALL,
    private val dateSelectedListener: CalendarDayAdapter.OnDateSelectedListener
) : FragmentStateAdapter(fragment) {

    private var dataVersion: Long = 0

    override fun createFragment(position: Int): Fragment {
        val targetMonth = baseDate.plusMonths((position - START_POSITION).toLong())
        return SingleMonthFragment.newInstance(
            targetMonth,
            events_list,
            appointments_list,
            filterType,
            dateSelectedListener
        )
    }

    override fun getItemCount(): Int {
        return 240
    }

    override fun getItemId(position: Int): Long {
        // Combine position with dataVersion to force refresh when data changes
        return position + dataVersion * 1000L
    }

    override fun containsItem(itemId: Long): Boolean {
        // Accept all item IDs in valid range for current dataVersion
        val position = itemId % 1000L
        return position >= 0 && position < itemCount
    }

    fun updateFilter(newFilter: Meetings.FilterType) {
        this.filterType = newFilter
        dataVersion++ // Force fragment refresh with new filter
        notifyDataSetChanged()
    }

    fun updateEventsList(newList: ArrayList<Events_Do>) {
        this.events_list = newList
        dataVersion++ // force fragments to be considered "new"
        notifyDataSetChanged()
    }

    fun updateAppointmentsList(newList: ArrayList<AppointmentModel>) {
        this.appointments_list = newList
        dataVersion++ // force fragments to be considered "new"
        notifyDataSetChanged()
    }

    // Update both lists at once to avoid double refresh
    fun updateBothLists(newEventsList: ArrayList<Events_Do>, newAppointmentsList: ArrayList<AppointmentModel>) {
        this.events_list = newEventsList
        this.appointments_list = newAppointmentsList
        dataVersion++ // force fragments to be considered "new"
        notifyDataSetChanged()
    }

    companion object {
        const val START_POSITION = 120
    }
}
