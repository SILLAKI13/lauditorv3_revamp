package com.digicoffer.lauditor.Meetings.Models

import org.json.JSONArray
import java.util.ArrayList

class Event_Details_DO {
    var title: String? = null
    var event_type: String? = null
    var description: String? = null
    var team_name: JSONArray? = null
    var consumer_external: JSONArray? = null
    var corporate: JSONArray? = null

    @get:JvmName("isIs_linked_with_timesheet")
    @set:JvmName("setIs_linked_with_timesheet")
    var is_linked_with_timesheet: Boolean = false

    var time: String? = null
    var id: String? = null
    var format: String? = null
    var offset: String? = null
    var dialin: String? = null
    var offset_location: String? = null
    var matter_name: String? = null
    var owner_name: String? = null
    var repeat_interval: String? = null
    var team_members: String? = null
    var clients: String? = null

    @get:JvmName("isRecurring")
    @set:JvmName("setRecurring")
    var isRecurring: Boolean = false

    @get:JvmName("isTimesheet_added")
    @set:JvmName("setTimesheet_added")
    var timesheet_added: Boolean = false

    var from_ts: String? = null
    var to_ts: String? = null

    @get:JvmName("isOwner")
    @set:JvmName("setOwner")
    var owner: Boolean = false

    var display_time: String? = null
    var entity_name: String? = null
    var attachments: JSONArray? = null
    var doc_id: ArrayList<String>? = null
    var date: String? = null

    @get:JvmName("isAll_day")
    @set:JvmName("setAll_day")
    var all_day: Boolean = false

    var doc_type: ArrayList<String>? = null
    var tm_name: JSONArray? = null
    var notifications: JSONArray? = null
    var converted_Start_time: String? = null
    var converted_End_time: String? = null
    var tmid: String? = null
    var matter_id: String? = null
    var matter_type: String? = null
    var meeting_link: String? = null
    var timezone_location: String? = null
    var location: String? = null
}
