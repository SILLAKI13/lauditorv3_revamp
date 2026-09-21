package com.digicoffer.lauditor.Members

import org.json.JSONArray

class MembersModel {
    var currency: String? = null
    var defaultRate: String? = null
    var designation: String? = null
    var email: String? = null
    var mobile: String? = null
    var phone: String? = null
    var contact_phone: String? = null
    var phone_number: String? = null
    var id: String? = null
    var lastLogin: String? = null
    var name: String? = null
    var groups: JSONArray? = null
    var group_id: String? = null
    var group_name: String? = null

    @get:JvmName("isIsdisabled")
    @set:JvmName("setIsdisabled")
    var isdisabled: Boolean = false
}
