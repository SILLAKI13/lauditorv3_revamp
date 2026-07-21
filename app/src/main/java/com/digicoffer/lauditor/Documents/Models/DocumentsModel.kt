package com.digicoffer.lauditor.Documents.Models

import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class DocumentsModel {
    var name: String? = null
    var id: String = ""
    
    @get:JvmName("getContent_type")
    @set:JvmName("setContent_type")
    var content_type: String? = null
    
    @get:JvmName("getIsencrypted")
    @set:JvmName("setIsencrypted")
    var isencrypted: Boolean? = false
    
    var filename: String = ""
    var expiration_date: String = ""
    var group_id: String? = null
    
    fun group_id(): String? {
        return group_id
    }

    var group_name: String? = null
    var isSelected: Boolean = false
    var isChecked: Boolean = false
    @get:JvmName("getIsIsenabled_property")
    @set:JvmName("setIsIsenabled_property")
    var isIsenabled: Boolean = false
    var isenabled: Boolean
        get() = isIsenabled
        set(value) { isIsenabled = value }
    var isEnabled: Boolean
        get() = isIsenabled
        set(value) { isIsenabled = value }
    var file: File? = null
    var tag_type: String? = null
    var tag_name: String? = null
    var tags: JSONObject? = null
    var description: String = ""
    var groups: JSONArray? = null
    var isGroupSelected: Boolean = false
    var isGroupChecked: Boolean = false
    var isGroupenabled: Boolean = false

    var docid: String
        get() = id
        set(value) { id = value }
    var doctype: String = ""
    var user_id: String = ""
    var contentType: String?
        get() = content_type
        set(value) { content_type = value }
    var viewUrl: String = ""
    var is_encrypted: Boolean = false
    var is_password: Boolean = false
    var isAdded_encryption: Boolean = false
    var isIs_encrypted: Boolean
        get() = is_encrypted
        set(value) { is_encrypted = value }
    var isIs_password: Boolean
        get() = is_password
        set(value) { is_password = value }
    var tags_list: JSONObject?
        get() = tags
        set(value) { tags = value }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val that = other as DocumentsModel

        return id == that.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
