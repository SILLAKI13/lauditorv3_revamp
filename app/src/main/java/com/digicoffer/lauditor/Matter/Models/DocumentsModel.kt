package com.digicoffer.lauditor.Matter.Models

import org.json.JSONObject
import java.io.File

class DocumentsModel {
    var description: String? = null
    var docid: String? = null
    var doctype: String? = null
    var contentType: String? = null
    var expiration_date: String? = null

    @get:JvmName("getIs_encrypted_property")
    @set:JvmName("setIs_encrypted_property")
    var is_encrypted: Boolean = false

    var isIs_encrypted: Boolean
        get() = is_encrypted
        set(value) { is_encrypted = value }

    @get:JvmName("getIs_password_property")
    @set:JvmName("setIs_password_property")
    var is_password: Boolean = false

    var isIs_password: Boolean
        get() = is_password
        set(value) { is_password = value }

    @get:JvmName("getAdded_encryption_property")
    @set:JvmName("setAdded_encryption_property")
    var added_encryption: Boolean = false

    var isAdded_encryption: Boolean
        get() = added_encryption
        set(value) { added_encryption = value }

    var viewUrl: String? = null
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

    var tag_type: String? = null
    var tag_name: String? = null
    var tags_list: JSONObject = JSONObject()
    var file: File? = null

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val d = o as DocumentsModel

        return if (docid != null) docid == d.docid else d.docid == null
    }

    override fun hashCode(): Int {
        return docid?.hashCode() ?: 0
    }
}
