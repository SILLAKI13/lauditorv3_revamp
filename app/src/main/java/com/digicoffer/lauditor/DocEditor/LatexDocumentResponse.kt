package com.digicoffer.lauditor.DocEditor

class LatexDocumentResponse {
    var createdon: String? = null
    var docid: String? = null
    var document: String? = null
    var page: Int = 0
    var pageid: String? = null
    var updatedon: String? = null
    var userid: String? = null

    class DateWrapper {
        var date: String? = null

        fun `get$date`(): String? = date
        
        fun `set$date`(value: String?) {
            date = value
        }
    }
}
