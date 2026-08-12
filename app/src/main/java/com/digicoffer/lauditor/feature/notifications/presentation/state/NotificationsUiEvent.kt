package com.digicoffer.lauditor.feature.notifications.presentation.state

import com.digicoffer.lauditor.Notifications.Models.NotificationsDo

sealed interface NotificationsUiEvent {
    object LoadNotifications : NotificationsUiEvent
    data class SearchQueryChanged(val query: String) : NotificationsUiEvent
    data class NotificationCheckedChange(val notification: NotificationsDo, val isChecked: Boolean) : NotificationsUiEvent
    data class ToggleSelectAll(val isChecked: Boolean) : NotificationsUiEvent
    object MarkSelectedAsRead : NotificationsUiEvent
    data class ReadSingleNotification(val notification: NotificationsDo) : NotificationsUiEvent
    object RequestDeleteSelected : NotificationsUiEvent
    object ConfirmDeleteSelected : NotificationsUiEvent
    object DismissDialogs : NotificationsUiEvent
    data class SetPendingHighlight(val highlightId: String) : NotificationsUiEvent
    data class HighlightCardDismissed(val notificationIds: List<String>) : NotificationsUiEvent
    object NavigationCompleted : NotificationsUiEvent
}
