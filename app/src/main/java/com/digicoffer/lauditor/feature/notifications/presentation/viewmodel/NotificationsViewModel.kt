package com.digicoffer.lauditor.feature.notifications.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.NotificationCountApi
import com.digicoffer.lauditor.Notifications.Models.Navigation
import com.digicoffer.lauditor.Notifications.Models.NotificationsDo
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.feature.notifications.data.repository.NotificationsRepository
import com.digicoffer.lauditor.feature.notifications.presentation.state.NotificationsUiEvent
import com.digicoffer.lauditor.feature.notifications.presentation.state.NotificationsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.TreeMap

class NotificationsViewModel(
    private val repository: NotificationsRepository,
    private val countApi: NotificationCountApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    fun onEvent(event: NotificationsUiEvent) {
        when (event) {
            is NotificationsUiEvent.LoadNotifications -> loadNotifications()
            is NotificationsUiEvent.SearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
                applyFilterAndSorting()
            }
            is NotificationsUiEvent.NotificationCheckedChange -> {
                val list = _uiState.value.filteredList
                for (item in list) {
                    if (item.id == event.notification.id) {
                        item.isChecked = event.isChecked
                    }
                }
                updateSelectionState()
            }
            is NotificationsUiEvent.ToggleSelectAll -> {
                val list = _uiState.value.filteredList
                for (item in list) {
                    item.isChecked = event.isChecked
                }
                updateSelectionState()
            }
            is NotificationsUiEvent.MarkSelectedAsRead -> markSelectedAsRead()
            is NotificationsUiEvent.ReadSingleNotification -> markSingleAsRead(event.notification)
            is NotificationsUiEvent.RequestDeleteSelected -> {
                if (_uiState.value.hasSelection) {
                    _uiState.update {
                        it.copy(
                            alertTitle = "Confirmation",
                            alertMessage = "Are you sure you want to delete the selected notifications?"
                        )
                    }
                }
            }
            is NotificationsUiEvent.ConfirmDeleteSelected -> deleteSelected()
            is NotificationsUiEvent.DismissDialogs -> {
                _uiState.update { it.copy(alertTitle = null, alertMessage = null, toastMessage = null) }
            }
            is NotificationsUiEvent.SetPendingHighlight -> {
                val activeIds = if (event.highlightId.isNotEmpty()) setOf(event.highlightId) else emptySet()
                _uiState.update { it.copy(pendingHighlightId = event.highlightId, activeHighlightIds = activeIds) }
            }
            is NotificationsUiEvent.HighlightCardDismissed -> {
                val remaining = _uiState.value.activeHighlightIds.filterNot { event.notificationIds.contains(it) }.toSet()
                _uiState.update { it.copy(activeHighlightIds = remaining) }
            }
            is NotificationsUiEvent.NavigationCompleted -> {
                _uiState.update { it.copy(pendingNavigation = null) }
            }
        }
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.fetchNotifications()
            _uiState.update { it.copy(isLoading = false) }

            if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val rootJson = JSONObject(result.responseContent ?: "")
                    if (!rootJson.getBoolean("error")) {
                        val responseData = JSONObject(rootJson.getString("data"))
                        val rawList = parseNotificationsResponse(responseData)
                        
                        // Copy selection states if loading updates
                        val currentSelectionMap = _uiState.value.filteredList.associate { (it.id ?: "") to it.isChecked }
                        for (item in rawList) {
                            item.isChecked = currentSelectionMap[item.id ?: ""] ?: false
                        }

                        _uiState.update { it.copy(notificationList = rawList) }
                        applyFilterAndSorting()
                    } else {
                        _uiState.update {
                            it.copy(
                                alertTitle = "Alert",
                                alertMessage = rootJson.getString("msg")
                            )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            alertTitle = "Error",
                            alertMessage = e.message ?: "Failed to parse data"
                        )
                    }
                }
            } else {
                _uiState.update {
                    it.copy(
                        alertTitle = "Alert",
                        alertMessage = result.responseContent ?: "Connection Error"
                    )
                }
            }
        }
    }

    private fun applyFilterAndSorting() {
        val query = _uiState.value.searchQuery
        val masterList = _uiState.value.notificationList
        val (filtered, grouped) = sortAndGroupNotifications(masterList, query)

        _uiState.update {
            it.copy(
                filteredList = filtered,
                groupedItems = grouped
            )
        }
        updateSelectionState()
    }

    private fun updateSelectionState() {
        val filtered = _uiState.value.filteredList
        val allChecked = filtered.isNotEmpty() && filtered.all { it.isChecked }
        val anyChecked = filtered.any { it.isChecked }
        _uiState.update {
            it.copy(
                isAllSelected = allChecked,
                hasSelection = anyChecked
            )
        }
    }

    private fun markSelectedAsRead() {
        val selectedIds = _uiState.value.filteredList.filter { it.isChecked }.mapNotNull { it.id }
        if (selectedIds.isEmpty()) {
            _uiState.update { it.copy(toastMessage = "Select at least 1 notification") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.markNotificationsAsRead(selectedIds, isSingle = false)
            _uiState.update { it.copy(isLoading = false) }

            if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                val rootJson = JSONObject(result.responseContent ?: "")
                _uiState.update {
                    it.copy(
                        alertTitle = if (rootJson.getBoolean("error")) "Alert" else "",
                        alertMessage = rootJson.getString("msg")
                    )
                }
                if (!rootJson.getBoolean("error")) {
                    loadNotifications()
                    countApi.fetchNotificationCount()
                }
            } else {
                _uiState.update {
                    it.copy(
                        alertTitle = "Alert",
                        alertMessage = result.responseContent ?: "Connection Error"
                    )
                }
            }
        }
    }

    private fun markSingleAsRead(notification: NotificationsDo) {
        val isUnread = "unread".equals(notification.status, ignoreCase = true)
        if (!isUnread) {
            // Already read, navigate immediately
            _uiState.update { it.copy(pendingNavigation = notification.navigation) }
            return
        }

        val id = notification.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.markNotificationsAsRead(listOf(id), isSingle = true)
            _uiState.update { it.copy(isLoading = false) }

            if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                val rootJson = JSONObject(result.responseContent ?: "")
                if (!rootJson.getBoolean("error")) {
                    notification.status = "read"
                    applyFilterAndSorting()
                    countApi.fetchNotificationCount()
                    _uiState.update { it.copy(pendingNavigation = notification.navigation) }
                }
            }
        }
    }

    private fun deleteSelected() {
        val selectedIds = _uiState.value.filteredList.filter { it.isChecked }.mapNotNull { it.id }
        if (selectedIds.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.deleteNotifications(selectedIds)
            _uiState.update { it.copy(isLoading = false) }

            if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                val rootJson = JSONObject(result.responseContent ?: "")
                _uiState.update {
                    it.copy(
                        alertTitle = if (rootJson.getBoolean("error")) "Alert" else "",
                        alertMessage = rootJson.getString("msg")
                    )
                }
                if (!rootJson.getBoolean("error")) {
                    loadNotifications()
                    countApi.fetchNotificationCount()
                }
            } else {
                _uiState.update {
                    it.copy(
                        alertTitle = "Alert",
                        alertMessage = result.responseContent ?: "Connection Error"
                    )
                }
            }
        }
    }

    private fun parseNotificationsResponse(data: JSONObject): List<NotificationsDo> {
        val list = ArrayList<NotificationsDo>()
        try {
            val jsonArray = data.getJSONArray("notifications")
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val n = NotificationsDo()
                n.id = obj.getString("id")
                n.message = obj.getString("message")
                n.timestamp = obj.getString("timestamp")
                n.status = obj.getString("status")
                if (obj.has("priority")) {
                    n.priority = obj.getInt("priority")
                }
                if (obj.has("navigation") && !obj.isNull("navigation")) {
                    val navObj = obj.getJSONObject("navigation")
                    val nav = Navigation()
                    nav.route_name = navObj.getString("route_name")
                    if (navObj.has("params")) {
                        nav.params = navObj.getJSONObject("params")
                    }
                    n.navigation = nav
                }
                list.add(n)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun sortAndGroupNotifications(
        list: List<NotificationsDo>,
        query: String
    ): Pair<List<NotificationsDo>, Map<String, List<NotificationsDo>>> {
        val filtered = if (query.isBlank()) {
            list
        } else {
            val lower = query.lowercase(Locale.getDefault()).trim()
            list.filter { it.message?.lowercase(Locale.getDefault())?.contains(lower) == true }
        }

        val sorted = filtered.sortedWith { n1, n2 ->
            val date1 = parseDate(n1.timestamp)
            val date2 = parseDate(n2.timestamp)
            if (date1 == null && date2 == null) 0
            else if (date1 == null) 1
            else if (date2 == null) -1
            else date2.compareTo(date1)
        }

        val dateKeyFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }
        val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        fun getDateKey(timestamp: String?): String {
            if (timestamp.isNullOrEmpty()) return "Unknown Date"
            return try {
                val date = isoFormatter.parse(timestamp)
                if (date != null) dateKeyFormatter.format(date) else "Unknown Date"
            } catch (e: Exception) {
                "Unknown Date"
            }
        }

        fun parseDateKey(dateKey: String): Date? {
            if (dateKey == "Unknown Date") return null
            return try {
                dateKeyFormatter.parse(dateKey)
            } catch (e: Exception) {
                null
            }
        }

        val grouped = TreeMap<String, List<NotificationsDo>> { date1, date2 ->
            val d1 = parseDateKey(date1)
            val d2 = parseDateKey(date2)
            if (d1 == null && d2 == null) 0
            else if (d1 == null) 1
            else if (d2 == null) -1
            else d2.compareTo(d1)
        }

        for (notification in sorted) {
            val key = getDateKey(notification.timestamp)
            val existing = grouped[key] ?: emptyList()
            grouped[key] = existing + notification
        }

        return Pair(sorted, grouped)
    }

    private fun parseDate(timestamp: String?): Date? {
        if (timestamp.isNullOrEmpty()) return null
        return try {
            val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            isoFormatter.parse(timestamp)
        } catch (e: Exception) {
            null
        }
    }
}
