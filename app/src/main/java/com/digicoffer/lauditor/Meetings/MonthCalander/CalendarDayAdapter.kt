package com.digicoffer.lauditor.Meetings.MonthCalander

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.digicoffer.lauditor.Appointments.Models.AppointmentModel
import com.digicoffer.lauditor.Meetings.ViewModels.Meetings
import com.digicoffer.lauditor.Meetings.Models.Events_Do
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import java.time.LocalDate
import java.util.ArrayList

class CalendarDayAdapter(
    private val days: List<CalendarItem>,
    private var events_list: ArrayList<Events_Do> = ArrayList(),
    private var appointments_list: ArrayList<AppointmentModel> = ArrayList(),
    private var currentFilter: Meetings.FilterType = Meetings.FilterType.ALL
) : RecyclerView.Adapter<CalendarDayAdapter.DayViewHolder>() {

    private var dateSelectedListener: OnDateSelectedListener? = null
    private var selectedDate: CalendarItem? = null
    private val today = LocalDate.now()
    private var isInitialSelectionDone = false

    init {
        // Find and set the item corresponding to today's date
        for (item in days) {
            if (!item.isHeader() && today == item.date) {
                selectedDate = item
                break
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_day_cell, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(days[position])
    }

    override fun getItemCount(): Int {
        return days.size
    }

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateText: TextView = itemView.findViewById(R.id.dateText)
        private val dayText: TextView = itemView.findViewById(R.id.dayText)
        private val dotView: TextView = itemView.findViewById(R.id.dotView)
        private val ll_dotView: LinearLayout = itemView.findViewById(R.id.ll_dotView)
        private val dayCard: LinearLayout = itemView.findViewById(R.id.dayCard)

        fun bind(item: CalendarItem) {
            val context = itemView.context

            if (item.isHeader()) {
                dayText.text = item.label
                dayText.visibility = View.VISIBLE
                ll_dotView.visibility = View.GONE
                dayCard.visibility = View.VISIBLE
                dayCard.background = null
                return
            }

            val date = item.date

            if (date == null) {
                dateText.text = ""
                dotView.visibility = View.GONE
                dayCard.visibility = View.GONE
                return
            }

            dayText.visibility = View.GONE
            dateText.visibility = View.VISIBLE
            dateText.text = date.dayOfMonth.toString()
            ll_dotView.visibility = View.VISIBLE
            dayCard.visibility = View.VISIBLE

            // Background and text color logic
            if (date == today) {
                dayCard.background = context.getDrawable(R.drawable.rounder_button_blue)
                dateText.setTextColor(Color.WHITE)
            } else {
                if (item == selectedDate) {
                    dayCard.background = context.getDrawable(R.drawable.save_bg)
                    dateText.setTextColor(Color.WHITE)
                } else {
                    dayCard.setBackgroundResource(R.drawable.rectangle_light_grey)
                    dateText.setTextColor(Color.BLACK)
                }
            }

            // Count events and appointments for this date
            var totalCount = 0
            val dateString = date.toString() // yyyy-MM-dd

            // EVENTS
            if (currentFilter == Meetings.FilterType.ALL || currentFilter == Meetings.FilterType.MY_MEETINGS) {
                for (event in events_list) {
                    if (event.converted_date != null && event.converted_date == dateString) {
                        totalCount++
                    }
                }
            }

            // APPOINTMENTS
            if (currentFilter == Meetings.FilterType.ALL || currentFilter == Meetings.FilterType.APPOINTMENTS) {
                for (appointment in appointments_list) {
                    try {
                        val appointmentDate = AndroidUtils.stringToDateTimeDefault(
                            appointment.appointment_from,
                            "yyyy-MM-dd'T'HH:mm:ss"
                        )
                        val converted = AndroidUtils.getDateToString(
                            appointmentDate,
                            "yyyy-MM-dd"
                        )
                        if (dateString == converted) {
                            totalCount++
                        }
                    } catch (ignored: Exception) {
                    }
                }
            }

            // Display dot with count
            if (totalCount > 0) {
                dotView.visibility = View.VISIBLE
                dotView.text = totalCount.toString()
            } else {
                dotView.visibility = View.GONE
            }

            // Default selection trigger for today's date
            if (!isInitialSelectionDone && item == selectedDate) {
                isInitialSelectionDone = true
                if (dateSelectedListener != null) {
                    filterAndNotify(date)
                }
            }

            itemView.setOnClickListener {
                selectedDate = item
                notifyDataSetChanged()
                filterAndNotify(date)
            }
        }
    }

    interface OnDateSelectedListener {
        fun onDateSelected(
            selectedDate: LocalDate,
            filteredEvents: ArrayList<Events_Do>,
            filteredAppointments: ArrayList<AppointmentModel>
        )
    }

    fun setOnDateSelectedListener(listener: OnDateSelectedListener?) {
        this.dateSelectedListener = listener
    }

    fun updateFilter(newFilter: Meetings.FilterType) {
        this.currentFilter = newFilter
        notifyDataSetChanged()

        // Re-trigger filter for currently selected date
        val selDate = selectedDate
        if (selDate != null && selDate.date != null && dateSelectedListener != null) {
            filterAndNotify(selDate.date)
        }
    }

    fun updateBothLists(newEventsList: ArrayList<Events_Do>, newAppointmentsList: ArrayList<AppointmentModel>) {
        this.events_list = newEventsList
        this.appointments_list = newAppointmentsList
        notifyDataSetChanged()

        // Re-trigger filter for currently selected date
        val selDate = selectedDate
        if (selDate != null && selDate.date != null && dateSelectedListener != null) {
            filterAndNotify(selDate.date)
        }
    }

    fun updateEventsList(newEventsList: ArrayList<Events_Do>) {
        this.events_list = newEventsList
        notifyDataSetChanged()
    }

    fun updateAppointmentsList(newAppointmentsList: ArrayList<AppointmentModel>) {
        this.appointments_list = newAppointmentsList
        notifyDataSetChanged()
    }

    private fun filterAndNotify(date: LocalDate) {
        val dateString = date.toString()
        val filteredEvents = ArrayList<Events_Do>()
        val filteredAppointments = ArrayList<AppointmentModel>()

        if (currentFilter == Meetings.FilterType.ALL || currentFilter == Meetings.FilterType.MY_MEETINGS) {
            for (event in events_list) {
                if (event.converted_date != null && event.converted_date == dateString) {
                    filteredEvents.add(event)
                }
            }
        }

        if (currentFilter == Meetings.FilterType.ALL || currentFilter == Meetings.FilterType.APPOINTMENTS) {
            for (appointment in appointments_list) {
                try {
                    val d = AndroidUtils.stringToDateTimeDefault(
                        appointment.appointment_from,
                        "yyyy-MM-dd'T'HH:mm:ss"
                    )
                    val converted = AndroidUtils.getDateToString(d, "yyyy-MM-dd")
                    if (dateString == converted) {
                        filteredAppointments.add(appointment)
                    }
                } catch (ignored: Exception) {
                }
            }
        }

        dateSelectedListener?.onDateSelected(
            date,
            filteredEvents,
            filteredAppointments
        )
    }
}
