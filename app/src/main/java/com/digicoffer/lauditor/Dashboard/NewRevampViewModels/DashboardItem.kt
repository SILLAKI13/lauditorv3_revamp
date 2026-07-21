package com.digicoffer.lauditor.Dashboard.NewRevampViewModels

import com.digicoffer.lauditor.Notifications.Models.Navigation

class DashboardItem(val type: Int, val data: Any) {
    var navigation: Navigation? = null

    companion object {
        const val TYPE_MEETING = 0
        const val TYPE_APPOINTMENT = 1
        const val TYPE_MESSAGES = 2
        const val TYPE_NOTIFICATION = 3
        const val TYPE_REVENUE_TREND = 4
        const val TYPE_APPOINTMENT_TREND = 5
        const val TYPE_MATTER = 6
        const val TYPE_STORAGE = 7
        const val TYPE_BILLABLE = 8
        const val TYPE_APPROX_REVENUE = 9
        const val TYPE_SUBSCRIPTION = 10
        const val TYPE_HIRING = 11
        const val TYPE_APPOINTMENT_REVENUE_TREND = 15
    }
}
