package com.digicoffer.lauditor.Meetings.Models

class TeamDo {
    var id: String? = null
    var name: String? = null

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

        val d = other as TeamDo

        return if (id != null) id == d.id else d.id == null
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
