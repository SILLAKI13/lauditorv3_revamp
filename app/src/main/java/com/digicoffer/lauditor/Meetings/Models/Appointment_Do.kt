package com.digicoffer.lauditor.Meetings.Models

import java.util.ArrayList

class Appointment_Do {
    var id: String? = null
    var services_offered: ArrayList<String>? = null
    var client_id: String? = null
    var client_name: String? = null
    var appointment_from: String? = null
    var appointment_to: String? = null
    var consultation_mode: String? = null
    var appointment_status: String? = null
    var meeting_link: String? = null
    var rsvp_status: String? = null
    var created_at: String? = null
    var payment: Payment_Do? = null
}
