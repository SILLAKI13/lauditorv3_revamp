package com.digicoffer.lauditor.Documents.Models

import org.json.JSONArray
import org.json.JSONObject

class ViewDocumentsModel {
    var created: String? = null
    var description: String? = null
    
    @get:JvmName("isAdded_encryption")
    @set:JvmName("setAdded_encryption")
    var added_encryption: Boolean = false
    
    var expiration_date: String? = null
    var filename: String? = null
    
    @get:JvmName("getContent_type")
    @set:JvmName("setContent_type")
    var content_type: String? = null
    
    var id: String? = null
    
    @get:JvmName("isIsdisabled")
    @set:JvmName("setIsdisabled")
    var isdisabled: Boolean = false
    
    @get:JvmName("isIs_disabled")
    @set:JvmName("setIs_disabled")
    var is_disabled: Boolean = false
    
    @get:JvmName("getDownload_permission")
    @set:JvmName("setDownload_permission")
    var download_permission: Boolean = true
    
    @get:JvmName("isIs_encrypted")
    @set:JvmName("setIs_encrypted")
    var is_encrypted: Boolean = false
    
    @get:JvmName("isIs_password")
    @set:JvmName("setIs_password")
    var is_password: Boolean = false
    
    var name: String? = null
    var origin: String? = null
    var uploaded_by: String? = null
    var doc_type: String? = null
    var deletedBy: String? = null
    var deletedOn: String? = null
    var category: String? = null
    var tag: JSONObject? = null
    var tagslist: JSONArray? = null
    
    @get:JvmName("getChecked")
    @set:JvmName("setChecked")
    var isChecked: Boolean? = false

    fun getIsChecked(): Boolean {
        return isChecked ?: false
    }

    fun setIsChecked(isChecked: Boolean) {
        this.isChecked = isChecked
    }
}
