package com.digicoffer.lauditor.TimeSheets.Models

import org.json.JSONObject

class TaskModel {
    var taskid: String? = null
    var hours: String? = null
    var minutes: String? = null
    var matterid: String? = null
    
    @get:JvmName("getPermissions")
    @set:JvmName("setPermissions")
    var Permissions: JSONObject? = null
    
    var Task_matter_name: String? = null
    var Task_billing: String? = null
    var Task_matter_id: String? = null
    var Task_name: String? = null
    
    @get:JvmName("getIs_editable")
    @set:JvmName("setIs_editable")
    var is_editable: Boolean? = true
    
    @get:JvmName("getLinkedWithCalendar")
    @set:JvmName("setLinkedWithCalendar")
    var isLinkedWithCalendar: Boolean? = true
    
    var editDate: Boolean? = true
    var editHours: Boolean? = true
    var editMinutes: Boolean? = true
    var editProject: Boolean? = true
    var editTask: Boolean? = true
    var editStatus: Boolean? = true
}
