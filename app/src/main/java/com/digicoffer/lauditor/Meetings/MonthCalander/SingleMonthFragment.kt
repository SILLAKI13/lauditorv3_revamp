package com.digicoffer.lauditor.Meetings.MonthCalander

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.Appointments.Models.AppointmentModel
import com.digicoffer.lauditor.Meetings.ViewModels.Meetings
import com.digicoffer.lauditor.Meetings.Models.Events_Do
import com.digicoffer.lauditor.R
import java.time.LocalDate
import java.time.YearMonth
import java.util.ArrayList

class SingleMonthFragment : Fragment() {
    private var month: LocalDate? = null
    private var adapter: CalendarDayAdapter? = null
    private var dateSelectedListener: CalendarDayAdapter.OnDateSelectedListener? = null
    private var events_list = ArrayList<Events_Do>()
    private var appointments_list = ArrayList<AppointmentModel>()

    fun setDateSelectedListener(listener: CalendarDayAdapter.OnDateSelectedListener?) {
        this.dateSelectedListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            val dateStr = args.getString(ARG_DATE)
            if (dateStr != null) {
                month = LocalDate.parse(dateStr)
            }
            val filter = args.getSerializable("currentFilter")
            if (filter is Meetings.FilterType) {
                currentFilter = filter
            }
            val evList = args.getSerializable("events_list")
            if (evList is ArrayList<*>) {
                events_list = evList as ArrayList<Events_Do>
            }
            val appList = args.getSerializable("appointments_list")
            if (appList is ArrayList<*>) {
                appointments_list = appList as ArrayList<AppointmentModel>
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_single_month, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

        recyclerView.layoutManager = GridLayoutManager(context, 7)
        val days = generateCalendarItems(month ?: LocalDate.now())

        // Pass both events and appointments to the adapter
        adapter = CalendarDayAdapter(days, events_list, appointments_list, currentFilter)
        adapter?.setOnDateSelectedListener(dateSelectedListener)
        recyclerView.adapter = adapter

        return view
    }

    fun updateFilter(newFilter: Meetings.FilterType) {
        currentFilter = newFilter
        adapter?.updateFilter(newFilter)
    }

    private fun generateCalendarItems(date: LocalDate): List<CalendarItem> {
        val list = ArrayList<CalendarItem>()

        // Add weekday headers
        val dayNames = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        for (day in dayNames) {
            list.add(CalendarItem(day))
        }

        // Add date placeholders
        val yearMonth = YearMonth.from(date)
        val firstOfMonth = yearMonth.atDay(1)
        val startDay = firstOfMonth.dayOfWeek.value % 7 // Sunday = 0

        // Add empty cells before first day of month
        for (i in 0 until startDay) {
            list.add(CalendarItem(null as LocalDate?))
        }

        // Add all days of the month
        for (i in 1..yearMonth.lengthOfMonth()) {
            list.add(CalendarItem(yearMonth.atDay(i)))
        }

        // Pad remaining cells to complete the grid (7 columns x 7 rows = 49 cells including headers)
        while (list.size < 49) {
            list.add(CalendarItem(null as LocalDate?))
        }

        return list
    }

    companion object {
        private const val ARG_DATE = "month_date"
        private var currentFilter = Meetings.FilterType.ALL

        @JvmStatic
        fun newInstance(
            date: LocalDate,
            events_list: ArrayList<Events_Do>,
            appointments_list: ArrayList<AppointmentModel>,
            filterType: Meetings.FilterType,
            dateSelectedListener: CalendarDayAdapter.OnDateSelectedListener
        ): SingleMonthFragment {
            val fragment = SingleMonthFragment()
            val args = Bundle().apply {
                putString(ARG_DATE, date.toString())
                putSerializable("currentFilter", filterType)
                putSerializable("events_list", events_list)
                putSerializable("appointments_list", appointments_list)
            }
            fragment.arguments = args
            fragment.setDateSelectedListener(dateSelectedListener)
            return fragment
        }
    }
}
