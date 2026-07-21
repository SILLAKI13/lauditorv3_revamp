package com.digicoffer.lauditor.Meetings.Models

class DocumentsDo {
    var docid: String? = null
    var doctype: String? = null
    var name: String? = null
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val d = other as DocumentsDo

        return if (docid != null) docid == d.docid else d.docid == null
    }

    override fun hashCode(): Int {
        return docid?.hashCode() ?: 0
    }
}
