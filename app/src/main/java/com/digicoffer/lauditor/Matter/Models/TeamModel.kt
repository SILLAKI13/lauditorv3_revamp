package com.digicoffer.lauditor.Matter.Models

import org.json.JSONArray

class TeamModel {
    var tm_id: String? = null
    var tm_name: String? = null
    var user_id: String? = null

    @get:JvmName("isSelected")
    @set:JvmName("setSelected")
    var isSelected: Boolean = false

    @get:JvmName("isChecked")
    @set:JvmName("setChecked")
    var isChecked: Boolean = false

    @get:JvmName("isIsenabled")
    @set:JvmName("setIsenabled")
    var isenabled: Boolean = false

    var groups: JSONArray? = null

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val that = o as TeamModel

        return if (tm_id != null) tm_id == that.tm_id else that.tm_id == null
    }

    override fun hashCode(): Int {
        return tm_id?.hashCode() ?: 0
    }
}
