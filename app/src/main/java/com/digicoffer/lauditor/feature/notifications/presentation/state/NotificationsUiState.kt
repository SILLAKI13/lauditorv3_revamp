package com.digicoffer.lauditor.feature.notifications.presentation.state

import com.digicoffer.lauditor.Notifications.Models.Navigation
import com.digicoffer.lauditor.Notifications.Models.NotificationsDo

data class NotificationsUiState(
    val notificationList: List<NotificationsDo> = emptyList(),
    val filteredList: List<NotificationsDo> = emptyList(),
    val groupedItems: Map<String, List<NotificationsDo>> = emptyMap(),
    val selectedNotificationIds: Set<String> = emptySet(),
    val isAllSelected: Boolean = false,
    val hasSelection: Boolean = false,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val toastMessage: String? = null,
    val alertTitle: String? = null,
    val alertMessage: String? = null,
    val pendingHighlightId: String = "",
    val activeHighlightIds: Set<String> = emptySet(),
    val pendingNavigation: Navigation? = null
)
