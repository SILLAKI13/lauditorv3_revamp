package com.digicoffer.lauditor.TimeSheets.Models

import org.json.JSONArray

class TSMatterModel {
    @get:JvmName("getMattername")
    @set:JvmName("setMattername")
    var mattername: String? = null

    @get:JvmName("getMatterid")
    @set:JvmName("setMatterid")
    var matterid: String? = null

    @get:JvmName("getMatter_type")
    @set:JvmName("setMatter_type")
    var matter_type: String? = null
    
    @get:JvmName("getIseditable")
    @set:JvmName("setIseditable")
    var iseditable: Boolean? = true
    
    @get:JvmName("getTasks")
    @set:JvmName("setTasks")
    var Tasks: JSONArray? = null
}
