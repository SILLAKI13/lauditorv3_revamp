package com.digicoffer.lauditor.Groups.Models

import org.json.JSONArray

class ViewGroupModel {
    var id: String = ""
    var name: String = ""
    var description: String = ""
    var created: String = ""

    @get:JvmName("getUser_type")
    @set:JvmName("setUser_type")
    var User_type: String = ""

    var owner_name: String = ""
    var date: String = ""
    var group_id: String = ""

    var group_name: String = ""

    var memberCount: String = ""
    var members: JSONArray? = null
    var group_head_id: String = ""
    var group_head_name: String = ""
    
    var isSelected: Boolean = false
    var isChecked: Boolean = false

    var isCan_delete: Boolean = true
    var isCan_assign_docs: Boolean = false
    var isIsdisabled: Boolean = false
}
