package com.digicoffer.lauditor.Meetings.Models;

import com.digicoffer.lauditor.Appointments.Models.AppointmentModel;

public class CalendarItem {
    private Events_Do event;
    private AppointmentModel appointment;
    private ItemType type;

    public enum ItemType {
        EVENT, APPOINTMENT
    }

    // Constructor for Event
    public CalendarItem(Events_Do event) {
        this.event = event;
        this.type = ItemType.EVENT;
    }

    // Constructor for Appointment
    public CalendarItem(AppointmentModel appointment) {
        this.appointment = appointment;
        this.type = ItemType.APPOINTMENT;
    }

    public ItemType getType() {
        return type;
    }

    public Events_Do getEvent() {
        return event;
    }

    public AppointmentModel getAppointment() {
        return appointment;
    }

    public boolean isEvent() {
        return type == ItemType.EVENT;
    }

    public boolean isAppointment() {
        return type == ItemType.APPOINTMENT;
    }
}
