package com.digicoffer.lauditor.Relationships.Model

import org.json.JSONArray

class SharedDocumentsDo {
    var category: String? = null
    var created: String? = null
    var description: String? = null
    var doctype: String? = null
    var id: String? = null
    var name: String? = null
    var shareOn: String? = null

    @get:JvmName("isAdded_encryption")
    @set:JvmName("setAdded_encryption")
    var added_encryption: Boolean = false

    var isSelected: Boolean = false

    @get:JvmName("isHas_Confidential")
    @set:JvmName("setHas_Confidential")
    var has_Confidential: Boolean = false

    var isChecked: Boolean = false

    @get:JvmName("isIsenabled")
    @set:JvmName("setIsenabled")
    var isenabled: Boolean = false

    var content_type: String? = null
    var expiration_date: String? = null
    var filename: String? = null
    var matter_details_name: String? = null
    var matter_details_id: String? = null
    var matter_details: JSONArray? = null

    @get:JvmName("isIs_disabled")
    @set:JvmName("setIs_disabled")
    var is_disabled: Boolean = false

    @get:JvmName("isIs_encrypted")
    @set:JvmName("setIs_encrypted")
    var is_encrypted: Boolean = false

    @get:JvmName("isIs_password")
    @set:JvmName("setIs_password")
    var is_password: Boolean = false

    var origin: String? = null
    var uploaded_by: String? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SharedDocumentsDo) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
