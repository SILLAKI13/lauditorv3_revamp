package com.digicoffer.lauditor.Meetings.Models

import com.digicoffer.lauditor.Appointments.Models.AppointmentModel

class CalendarItem {
    var event: Events_Do? = null
        private set
    var appointment: AppointmentModel? = null
        private set
    var type: ItemType? = null
        private set

    enum class ItemType {
        EVENT, APPOINTMENT
    }

    constructor(event: Events_Do) {
        this.event = event
        this.type = ItemType.EVENT
    }

    constructor(appointment: AppointmentModel) {
        this.appointment = appointment
        this.type = ItemType.APPOINTMENT
    }

    fun isEvent(): Boolean {
        return type == ItemType.EVENT
    }

    fun isAppointment(): Boolean {
        return type == ItemType.APPOINTMENT
    }
}
