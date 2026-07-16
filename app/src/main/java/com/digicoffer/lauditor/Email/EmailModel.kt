package com.digicoffer.lauditor.Email

class EmailModel {
    var id: String? = null
    var name: String? = null
    var type: String? = null
    var messagesTotal: Int = 0
    var messagesUnread: Int = 0
    var threadsTotal: Int = 0
    var isauthaccess: String? = null
    var threadsUnread: Int = 0

    companion object {
        @JvmStatic
        fun getInstance(): Any? {
            return null
        }
    }
}
