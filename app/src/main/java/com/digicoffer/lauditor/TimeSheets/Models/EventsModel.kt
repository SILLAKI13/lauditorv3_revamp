package com.digicoffer.lauditor.TimeSheets.Models

import org.json.JSONObject

class EventsModel {
    var billing: String? = null
    var taskName: String? = null
    var total: String? = null
    
    @get:JvmName("getIs_editable")
    @set:JvmName("setIs_editable")
    var is_editable: Boolean? = true
    
    var mon: JSONObject? = null
    var tue: JSONObject? = null
    var wed: JSONObject? = null
    var thu: JSONObject? = null
    var fri: JSONObject? = null
    var sat: JSONObject? = null
    var sun: JSONObject? = null
    var matter_type: String? = null
    var matter_name: String? = null
    var matter_id: String? = null
}
