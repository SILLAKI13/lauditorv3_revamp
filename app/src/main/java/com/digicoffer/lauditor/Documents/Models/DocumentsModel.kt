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
    var isIsenabled: Boolean = false
    var file: File? = null
    var tag_type: String? = null
    var tag_name: String? = null
    var tags: JSONObject? = null
    var description: String = ""
    var groups: JSONArray? = null
    var isGroupSelected: Boolean = false
    var isGroupChecked: Boolean = false
    var isGroupenabled: Boolean = false

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
