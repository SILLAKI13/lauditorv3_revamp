package com.digicoffer.lauditor.DocEditor

import org.json.JSONObject

class DocListingModel {
    @JvmField
    var docid: String? = null
    
    @JvmField
    var documentname: String? = null
    
    @JvmField
    var date: String? = null
    
    @JvmField
    var updatedon: JSONObject? = null

    fun `get$date`(): String? = date
    
    fun `set$date`(value: String?) {
        date = value
    }

    fun getDocid(): String? = docid
    
    fun setDocid(value: String?) {
        docid = value
    }

    fun getDocumentname(): String? = documentname
    
    fun setDocumentname(value: String?) {
        documentname = value
    }

    fun getUpdatedon(): JSONObject? = updatedon
    
    fun setUpdatedon(value: JSONObject?) {
        updatedon = value
    }
}
