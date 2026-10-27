package com.digicoffer.lauditor.feature.dashboard.presentation.state

import com.digicoffer.lauditor.Notifications.Models.Navigation
import com.digicoffer.lauditor.feature.dashboard.data.model.DashboardDateInfo
import com.digicoffer.lauditor.feature.dashboard.data.model.DashboardSectionModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DashboardUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val sections: List<DashboardSectionModel> = emptyList(),
    val currentDateInfo: DashboardDateInfo = getCurrentDateInfo(),
    val clientChatCount: Int = 0,
    val teamChatCount: Int = 0,
    val userRole: String? = null,
    val userCategory: String? = null,
    val alertTitle: String? = null,
    val alertMessage: String? = null,
    val isNoInternet: Boolean = false,
    val pendingNavigation: Navigation? = null,
    val pendingCardTypeNavigation: PendingCardNav? = null,
    val pendingSubscriptionPay: PendingSubscriptionPay? = null
) {
    val totalChatCount: Int
        get() = clientChatCount + teamChatCount

    companion object {
        fun getCurrentDateInfo(): DashboardDateInfo {
            val now = Date()
            val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
            val dateFormat = SimpleDateFormat("dd", Locale.getDefault())
            val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
            return DashboardDateInfo(
                month = monthFormat.format(now),
                date = dateFormat.format(now),
                year = yearFormat.format(now)
            )
        }
    }
}

data class PendingCardNav(
    val cardType: Int,
    val navigation: Navigation?,
    val subType: String? = null
)

data class PendingSubscriptionPay(
    val planLabel: String?,
    val validityText: String?
)
