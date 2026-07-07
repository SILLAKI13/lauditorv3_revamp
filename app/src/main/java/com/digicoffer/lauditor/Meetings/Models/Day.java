package com.digicoffer.lauditor.Meetings.Models;


import com.applandeo.materialcalendarview.EventDay;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Day {
    private String date;
    private List<EventDay> events;
    private boolean hasEvents;
    private boolean isToday;
    private Calendar calendar;

    public Day(String date) {
        this.date = date;
        this.events = new ArrayList<>();
        this.hasEvents = false;
        this.isToday = false;

        // Initialize the Calendar instance and set the date
        calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        try {
            calendar.setTime(dateFormat.parse(date));
        } catch (ParseException e) {
            e.fillInStackTrace();
        }
    }

    public String getDate() {
        return date;
    }


    public boolean hasEvents() {
        return events != null && !events.isEmpty();
    }

    public List<EventDay> getEvents() {
        return events;
    }

    public void setEvents(List<EventDay> events) {
        this.events = events;
    }

    public void setHasEvents(boolean hasEvents) {
        this.hasEvents = hasEvents;
    }

    public boolean isToday() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String today = sdf.format(new Date());
        return today.equals(this.date);
    }

    public String getEventsAsString() {
        if (events.isEmpty()) {
            return "No events";
        } else {
            StringBuilder builder = new StringBuilder();
//            for (EventDay event : events) {
//                builder.append(event.getName()).append(": ").append(event.getTime()).append("\n");
//            }
            return builder.toString();
        }
    }
}