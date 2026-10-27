package com.digicoffer.lauditor.feature.dashboard.data.model

import com.digicoffer.lauditor.Notifications.Models.Navigation

/**
 * Domain data models for Dashboard in Jetpack Compose architecture.
 */

data class DashboardSectionModel(
    val sectionId: Int,
    val title: String,
    val items: List<DashboardCardItemModel>
) {
    val cardCount: Int get() = items.size

    companion object {
        const val SECTION_TODAY = 0
        const val SECTION_ANALYTICS = 1
        const val SECTION_METRICS = 2
    }
}

data class DashboardCardItemModel(
    val type: Int,
    val data: Any,
    val navigation: Navigation? = null
) {
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

data class DashboardDateInfo(
    val month: String,
    val date: String,
    val year: String
)
