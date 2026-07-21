package com.digicoffer.lauditor.Meetings.Models

import java.util.Objects

class RelationshipsDO {
    var id: String? = null
    var name: String? = null
    var type: String? = null
    var entity_id: String? = null

    @get:JvmName("isSelected")
    @set:JvmName("setSelected")
    var isSelected: Boolean = false

    @get:JvmName("isChecked")
    @set:JvmName("setChecked")
    var isChecked: Boolean = false

    @get:JvmName("isIsenabled")
    @set:JvmName("setIsenabled")
    var isenabled: Boolean = false

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as RelationshipsDO
        return id == that.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
