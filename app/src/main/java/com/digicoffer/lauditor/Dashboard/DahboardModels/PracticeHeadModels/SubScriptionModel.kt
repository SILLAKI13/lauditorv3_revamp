package com.digicoffer.lauditor.Dashboard.DahboardModels.PracticeHeadModels

class SubScriptionModel {
    var active_pay_button: Boolean = false
        @JvmName("isActive_pay_button") get
        @JvmName("setActive_pay_button") set

    var is_active_sub: Boolean = false
        @JvmName("isIs_active_sub") get
        @JvmName("setIs_active_sub") set

    var is_paid_sub: Boolean = false
        @JvmName("isIs_paid_sub") get
        @JvmName("setIs_paid_sub") set

    var user_allowed: String? = null
    var message: String? = null
    var month: String? = null
    var email: String? = null
}
