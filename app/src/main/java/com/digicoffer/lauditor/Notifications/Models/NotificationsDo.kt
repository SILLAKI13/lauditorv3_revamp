package com.digicoffer.lauditor.Notifications.Models

class NotificationsDo {
    var message: String? = null
    var timestamp: String? = null
    var id: String? = null
    var status: String? = null
    var priority: Int = 0
    var navigation: Navigation? = null
    
    @get:JvmName("isIsenabled")
    @set:JvmName("setIsenabled")
    var isenabled: Boolean = false
    
    var isChecked: Boolean = false
    var isSelected: Boolean = false
}
