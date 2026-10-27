package com.digicoffer.lauditor.feature.dashboard.presentation.state

import com.digicoffer.lauditor.Notifications.Models.Navigation

sealed interface DashboardUiEvent {
    data class LoadDashboard(val isRefresh: Boolean = false) : DashboardUiEvent
    object RefreshChatCounts : DashboardUiEvent
    data class OnCardClick(
        val cardType: Int,
        val navigation: Navigation? = null,
        val subType: String? = null
    ) : DashboardUiEvent
    data class OnPaySubscription(
        val planLabel: String?,
        val validityText: String?
    ) : DashboardUiEvent
    data class SetClientChatActive(val isClient: Boolean) : DashboardUiEvent
    object DismissAlert : DashboardUiEvent
    object NavigationHandled : DashboardUiEvent
}
