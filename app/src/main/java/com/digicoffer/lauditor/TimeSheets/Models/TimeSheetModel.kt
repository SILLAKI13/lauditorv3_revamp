package com.digicoffer.lauditor.TimeSheets.Models

import org.json.JSONObject

class TimeSheetModel {
    var currentWeek: String? = null
    var nextWeek: String? = null
    var prevWeek: String? = null
    
    @get:JvmName("isFrozen")
    @set:JvmName("setFrozen")
    var isFrozen: Boolean = false
    
    var headers: JSONObject? = null
    var Fri: String? = null
    var Mon: String? = null
    var Sat: String? = null
    var Sun: String? = null
    var Thu: String? = null
    var Tue: String? = null
    var Wed: String? = null
}
