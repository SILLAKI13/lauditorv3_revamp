package com.digicoffer.lauditor.Matter.Models

import org.json.JSONArray

class HistoryModel {
    var id: String? = null
    var title: String? = null
    var to_ts: String? = null
    var notes: String? = null
    var notes_list: JSONArray? = null
    var from_ts: String? = null
    var event_type: String? = null
    var description: String? = null

    @get:JvmName("isAllday")
    @set:JvmName("setAllday")
    var allday: Boolean = false
}
