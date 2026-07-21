package com.digicoffer.lauditor.Meetings.Models

import org.json.JSONArray
import java.util.ArrayList

class Events_Do {
    var event_Name: String? = null
    var converted_Start_time: String? = null
    var cOnverted_End_time: String? = null
    var event_id: String? = null
    var converted_date: String = ""
    var event_description: String? = null
    var event_repetetion: String? = null
    var event_Notifications: String? = null
    var event_clients: String? = null
    var event_team: String? = null

    @get:JvmName("isIs_linked_with_timesheet")
    @set:JvmName("setIs_linked_with_timesheet")
    var is_linked_with_timesheet: Boolean = false

    @get:JvmName("isAll_day")
    @set:JvmName("setAll_day")
    var isAll_day: Boolean = false

    var description: String? = null
    var dialin: String? = null
    var event_type: String? = null
    var invitees_consumer_external: JSONArray? = null
    var invitees_external: JSONArray? = null
    var invitees_internal: JSONArray? = null

    @get:JvmName("isIsrecurring")
    @set:JvmName("setIsrecurring")
    var isrecurring: Boolean = false

    var location: String? = null
    var matter_id: String? = null
    var matter_type: String? = null
    var matter_name: String? = null
    var meeting_link: String? = null
    var notes: String? = null
    var notifications: JSONArray? = null

    @get:JvmName("isOwner")
    @set:JvmName("setOwner")
    var isOwner: Boolean = false

    var owner_name: String? = null
    var repeat_interval: String? = null
    var timezone_location: String? = null
    var timezone_offset: String? = null
    var title: String? = null
    var attachments: JSONArray? = null

    @get:JvmName("isExpanded")
    @set:JvmName("setExpanded")
    var isExpanded: Boolean = false

    var userRsvp: String = ""

    var team_name: ArrayList<String>? = null
    var tm_name: ArrayList<String>? = null

    @get:JvmName("isRecurring")
    @set:JvmName("setRecurring")
    var isRecurring: Boolean = false

    var attachment_name: ArrayList<String>? = null
    var event_start_time: String? = null
    var event_end_time: String? = null
}
