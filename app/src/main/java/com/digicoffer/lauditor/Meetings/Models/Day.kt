package com.digicoffer.lauditor.Meetings.Models

import com.applandeo.materialcalendarview.EventDay
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Day(@JvmField var date: String) {
    var events: List<EventDay> = ArrayList()
    private var hasEvents: Boolean = false
    var calendar: Calendar = Calendar.getInstance()

    init {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        try {
            val parsedDate = dateFormat.parse(date)
            if (parsedDate != null) {
                calendar.time = parsedDate
            }
        } catch (e: ParseException) {
            e.fillInStackTrace()
        }
    }

    fun getDate(): String {
        return date
    }



    fun hasEvents(): Boolean {
        return events.isNotEmpty()
    }

    fun setHasEvents(hasEvents: Boolean) {
        this.hasEvents = hasEvents
    }

    fun isToday(): Boolean {
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val today = sdf.format(Date())
        return today == this.date
    }

    fun getEventsAsString(): String {
        return if (events.isEmpty()) {
            "No events"
        } else {
            ""
        }
    }
}
