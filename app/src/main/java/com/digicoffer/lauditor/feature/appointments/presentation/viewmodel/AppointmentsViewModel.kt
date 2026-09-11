package com.digicoffer.lauditor.feature.appointments.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digicoffer.lauditor.Appointments.Models.AppointmentModel
import com.digicoffer.lauditor.Appointments.Models.PaymentModel
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.feature.appointments.data.repository.AppointmentsRepository
import com.digicoffer.lauditor.feature.appointments.presentation.state.AppointmentsUiEvent
import com.digicoffer.lauditor.feature.appointments.presentation.state.AppointmentsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

class AppointmentsViewModel(
    private val repository: AppointmentsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppointmentsUiState())
    val uiState: StateFlow<AppointmentsUiState> = _uiState.asStateFlow()

    private val PAGE_SIZE = 10
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)

    fun onEvent(event: AppointmentsUiEvent) {
        when (event) {
            is AppointmentsUiEvent.LoadAppointments -> loadAppointments()
            is AppointmentsUiEvent.SearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
                applyFilterAndSorting()
            }
            is AppointmentsUiEvent.PageNext -> {
                val state = _uiState.value
                if (state.currentPage < state.totalPages - 1) {
                    val nextPage = state.currentPage + 1
                    _uiState.update { it.copy(currentPage = nextPage) }
                    renderCurrentPage()
                }
            }
            is AppointmentsUiEvent.PagePrev -> {
                val state = _uiState.value
                if (state.currentPage > 0) {
                    val prevPage = state.currentPage - 1
                    _uiState.update { it.copy(currentPage = prevPage) }
                    renderCurrentPage()
                }
            }
            is AppointmentsUiEvent.ToggleActionMenu -> {
                val current = _uiState.value.expandedCardPosition
                val newPos = if (current == event.position) -1 else event.position
                _uiState.update { it.copy(expandedCardPosition = newPos) }
            }
            is AppointmentsUiEvent.CancelAppointment -> cancelAppointment(event.model)
            is AppointmentsUiEvent.DeleteAppointment -> deleteAppointment(event.model)
            is AppointmentsUiEvent.OpenHistory -> {
                _uiState.update {
                    it.copy(
                        historyClientId = event.model.client_id,
                        historyClientName = event.model.client_name,
                        historyClientProfilePic = event.model.client_profile_pic,
                        historyList = emptyList()
                    )
                }
                loadHistory(event.model.client_id)
            }
            is AppointmentsUiEvent.CloseHistory -> {
                _uiState.update {
                    it.copy(
                        historyClientId = "",
                        historyClientName = "",
                        historyClientProfilePic = "",
                        historyList = emptyList()
                    )
                }
            }
            is AppointmentsUiEvent.OpenSettlementHistory -> {
                _uiState.update {
                    it.copy(
                        settlementClientId = event.model.client_id,
                        settlementClientName = event.model.client_name,
                        settlementClientProfilePic = event.model.client_profile_pic,
                        rawSettlementList = emptyList(),
                        filteredSettlementList = emptyList(),
                        currentSettlementPageList = emptyList(),
                        settlementSearchQuery = "",
                        settlementStatusFilter = "All Status",
                        settlementSortOption = "Latest First",
                        settlementCurrentPage = 0,
                        settlementTotalPages = 0
                    )
                }
                loadSettlementHistory(event.model.client_id)
            }
            is AppointmentsUiEvent.CloseSettlementHistory -> {
                _uiState.update {
                    it.copy(
                        settlementClientId = "",
                        settlementClientName = "",
                        settlementClientProfilePic = "",
                        rawSettlementList = emptyList(),
                        filteredSettlementList = emptyList(),
                        currentSettlementPageList = emptyList(),
                        settlementApiTotalPaid = null,
                        settlementApiTotalTransactions = null,
                        settlementApiTotalRefunded = null,
                        settlementApiTotalRefundInitiated = null
                    )
                }
            }
            is AppointmentsUiEvent.SettlementSearchQuerySubmitted -> {
                _uiState.update { it.copy(settlementSearchQuery = event.query) }
                applySettlementFilterAndSorting()
            }
            is AppointmentsUiEvent.SettlementStatusFilterChanged -> {
                _uiState.update { it.copy(settlementStatusFilter = event.status) }
                applySettlementFilterAndSorting()
            }
            is AppointmentsUiEvent.SettlementSortOptionChanged -> {
                _uiState.update { it.copy(settlementSortOption = event.sortOption) }
                applySettlementFilterAndSorting()
            }
            is AppointmentsUiEvent.SettlementPageNext -> {
                val state = _uiState.value
                if (state.settlementCurrentPage < state.settlementTotalPages - 1) {
                    val nextPage = state.settlementCurrentPage + 1
                    _uiState.update { it.copy(settlementCurrentPage = nextPage) }
                    renderCurrentSettlementPage()
                }
            }
            is AppointmentsUiEvent.SettlementPagePrev -> {
                val state = _uiState.value
                if (state.settlementCurrentPage > 0) {
                    val prevPage = state.settlementCurrentPage - 1
                    _uiState.update { it.copy(settlementCurrentPage = prevPage) }
                    renderCurrentSettlementPage()
                }
            }
            is AppointmentsUiEvent.DismissDialogs -> {
                _uiState.update { it.copy(alertTitle = null, alertMessage = null, toastMessage = null) }
            }
            is AppointmentsUiEvent.ToggleNotesExpanded -> {
                val currentMap = _uiState.value.noteExpandedState
                val isExpanded = currentMap[event.appointmentId] ?: false
                val newMap = currentMap.toMutableMap().apply {
                    put(event.appointmentId, !isExpanded)
                }
                _uiState.update { it.copy(noteExpandedState = newMap) }
            }
            is AppointmentsUiEvent.NoteAddingDraftChanged -> {
                val newMap = _uiState.value.noteAddingMap.toMutableMap().apply {
                    put(event.appointmentId, event.noteText)
                }
                _uiState.update { it.copy(noteAddingMap = newMap) }
            }
            is AppointmentsUiEvent.NoteEditingDraftChanged -> {
                val newMap = _uiState.value.noteEditingMap.toMutableMap().apply {
                    put(event.noteId, event.noteText)
                }
                _uiState.update { it.copy(noteEditingMap = newMap) }
            }
            is AppointmentsUiEvent.SaveNewNote -> saveNewNote(event.appointmentId)
            is AppointmentsUiEvent.StartEditingNote -> {
                val editingStates = _uiState.value.noteEditingState.toMutableMap().apply {
                    put(event.noteId, true)
                }
                val draftMap = _uiState.value.noteEditingMap.toMutableMap().apply {
                    put(event.noteId, event.initialText)
                }
                val expandedMap = _uiState.value.noteExpandedState.toMutableMap().apply {
                    put(event.appointmentId, true)
                }
                _uiState.update {
                    it.copy(
                        noteEditingState = editingStates,
                        noteEditingMap = draftMap,
                        noteExpandedState = expandedMap
                    )
                }
            }
            is AppointmentsUiEvent.CancelEditingNote -> {
                val editingStates = _uiState.value.noteEditingState.toMutableMap().apply {
                    put(event.noteId, false)
                }
                val draftMap = _uiState.value.noteEditingMap.toMutableMap().apply {
                    remove(event.noteId)
                }
                val expandedMap = _uiState.value.noteExpandedState.toMutableMap().apply {
                    put(event.appointmentId, false)
                }
                _uiState.update {
                    it.copy(
                        noteEditingState = editingStates,
                        noteEditingMap = draftMap,
                        noteExpandedState = expandedMap
                    )
                }
            }
            is AppointmentsUiEvent.SaveEditedNote -> saveEditedNote(event.appointmentId, event.noteId)
            is AppointmentsUiEvent.DeleteNote -> deleteNote(event.appointmentId, event.noteId)
            is AppointmentsUiEvent.SetPendingHighlight -> {
                _uiState.update { it.copy(pendingHighlightId = event.highlightId) }
            }
        }
    }

    private fun loadAppointments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.fetchAppointments()
            _uiState.update { it.copy(isLoading = false) }

            if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val rootJson = JSONObject(result.responseContent ?: "")
                    if (!rootJson.optBoolean("error", false)) {
                        val appointmentsArray = rootJson.optJSONArray("appointments") ?: JSONArray()
                        val list = parseAppointments(appointmentsArray)
                        _uiState.update { it.copy(appointmentList = list) }
                        applyFilterAndSorting()
                    } else {
                        val errorMsg = rootJson.optString("msg").ifEmpty { "Failed to fetch appointments" }
                        _uiState.update {
                            it.copy(
                                alertTitle = "Error",
                                alertMessage = errorMsg
                            )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(alertTitle = "Error", alertMessage = e.message ?: "Failed parsing response") }
                }
            } else {
                val cleanMsg = extractErrorMessage(result.responseContent)
                _uiState.update {
                    it.copy(
                        alertTitle = "Error",
                        alertMessage = cleanMsg.ifEmpty { "Failed to connect to web service" }
                    )
                }
            }
        }
    }

    private fun computeDynamicStatus(apiStatus: String, fromStr: String, toStr: String): String {
        val clean = apiStatus.trim().lowercase(Locale.ROOT)
        if (clean == "cancelled" || clean == "canceled") return "Cancelled"
        if (clean == "payment_pending" || clean == "payment pending") return "Payment Pending"

        val fromDate = parseApiDate(fromStr)
        val toDate = parseApiDate(toStr)
        val now = java.util.Date()

        return when {
            toDate != null && now.after(toDate) -> "Completed"
            fromDate != null && toDate != null && now.after(fromDate) && now.before(toDate) -> "Ongoing"
            fromDate != null && now.before(fromDate) -> "Upcoming"
            clean == "completed" -> "Completed"
            clean == "ongoing" -> "Ongoing"
            clean == "upcoming" -> "Upcoming"
            clean.isNotEmpty() -> apiStatus.replaceFirstChar { it.uppercase() }
            else -> "Upcoming"
        }
    }

    private fun parseAppointments(array: JSONArray): List<AppointmentModel> {
        val list = mutableListOf<AppointmentModel>()
        for (i in 0 until array.length()) {
            val jsonObject = array.optJSONObject(i) ?: continue
            val model = AppointmentModel().apply {
                id = jsonObject.optString("id", "")
                client_id = jsonObject.optString("client_id", "")
                guid = jsonObject.optString("guid", "")
                client_name = jsonObject.optString("client_name", "")
                appointment_from = jsonObject.optString("appointment_from", "")
                appointment_to = jsonObject.optString("appointment_to", "")
                consultation_mode = jsonObject.optString("consultation_mode", "")
                appointment_status = computeDynamicStatus(
                    jsonObject.optString("appointment_status", ""),
                    appointment_from,
                    appointment_to
                )
                meeting_room_id = jsonObject.optString("meeting_room_id", "")
                meeting_room_expires_at = jsonObject.optString("meeting_room_expires_at", "")
                rsvp_status = jsonObject.optString("rsvp_status", "")
                created_at = jsonObject.optString("created_at", "")

                if (jsonObject.has("client_profile_pic")) {
                    client_profile_pic = jsonObject.optString("client_profile_pic", "")
                }
                if (jsonObject.has("services_offered")) {
                    services_offered = jsonObject.optJSONArray("services_offered") ?: JSONArray()
                }
                if (jsonObject.has("payment")) {
                    val paymentObj = jsonObject.getJSONObject("payment")
                    payment = PaymentModel().apply {
                        status = paymentObj.optString("status")
                        amount_paid = paymentObj.optString("amount_paid")
                        currency = paymentObj.optString("currency")
                        symbol = paymentObj.optString("symbol")
                        label = paymentObj.optString("label")
                        method = paymentObj.optString("method")
                    }
                }
                if (jsonObject.has("notes")) {
                    notes = jsonObject.optJSONArray("notes") ?: JSONArray()
                }
            }
            list.add(model)
        }
        return list
    }

    private fun applyFilterAndSorting() {
        val state = _uiState.value
        val query = state.searchQuery.trim().lowercase(Locale.ROOT)

        val filtered = if (query.isEmpty()) {
            state.appointmentList
        } else {
            state.appointmentList.filter { model ->
                model.client_name.lowercase(Locale.ROOT).contains(query) ||
                        model.appointment_status.lowercase(Locale.ROOT).contains(query) ||
                        model.consultation_mode.lowercase(Locale.ROOT).contains(query)
            }
        }

        val sorted = filtered.sortedWith { o1, o2 ->
            val status1 = o1.appointment_status.lowercase(Locale.ROOT)
            val status2 = o2.appointment_status.lowercase(Locale.ROOT)

            val isPriority1 = status1 == "upcoming" || status1 == "ongoing"
            val isPriority2 = status2 == "upcoming" || status2 == "ongoing"

            val d1 = parseApiDate(o1.appointment_from)
            val d2 = parseApiDate(o2.appointment_from)

            if (isPriority1 && isPriority2) {
                // Both are upcoming/ongoing -> ASCENDING (earliest first)
                when {
                    d1 == null && d2 == null -> 0
                    d1 == null -> 1
                    d2 == null -> -1
                    else -> d1.compareTo(d2)
                }
            } else if (isPriority1) {
                -1
            } else if (isPriority2) {
                1
            } else {
                // Neither is priority (completed, cancelled, etc.) -> DESCENDING (most recent first)
                when {
                    d1 == null && d2 == null -> 0
                    d1 == null -> 1
                    d2 == null -> -1
                    else -> d2.compareTo(d1)
                }
            }
        }

        val total = sorted.size
        val pages = if (total == 0) 0 else Math.ceil(total.toDouble() / PAGE_SIZE).toInt()

        _uiState.update {
            it.copy(
                filteredList = sorted,
                totalPages = pages,
                currentPage = 0
            )
        }
        renderCurrentPage()
    }

    private fun parseApiDate(dateStr: String): java.util.Date? {
        if (dateStr.isBlank()) return null
        val patterns = arrayOf(
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd"
        )
        for (pattern in patterns) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.ENGLISH)
                val date = sdf.parse(dateStr)
                if (date != null) return date
            } catch (e: Exception) {
                // continue
            }
        }
        return null
    }

    private fun renderCurrentPage() {
        val state = _uiState.value
        val startIndex = state.currentPage * PAGE_SIZE
        val endIndex = Math.min(startIndex + PAGE_SIZE, state.filteredList.size)

        val pageList = if (startIndex < state.filteredList.size) {
            state.filteredList.subList(startIndex, endIndex)
        } else {
            emptyList()
        }

        _uiState.update { it.copy(currentPageList = pageList) }
    }

    private fun cancelAppointment(model: AppointmentModel) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.cancelAppointment(model.id)
            _uiState.update { it.copy(isLoading = false) }

            if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val rootJson = JSONObject(result.responseContent ?: "")
                    val msg = rootJson.optString("msg").ifEmpty { "Appointment cancelled successfully" }
                    val error = rootJson.optBoolean("error", false)
                    if (error) {
                        _uiState.update { it.copy(alertTitle = "Error", alertMessage = msg) }
                    } else {
                        _uiState.update { it.copy(alertTitle = "Success", alertMessage = msg) }
                        loadAppointments()
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(alertTitle = "Error", alertMessage = e.message ?: "Failed parsing response") }
                }
            } else {
                val cleanMsg = extractErrorMessage(result.responseContent)
                _uiState.update {
                    it.copy(
                        alertTitle = "Error",
                        alertMessage = cleanMsg.ifEmpty { "Failed to cancel appointment" }
                    )
                }
            }
        }
    }

    private fun deleteAppointment(model: AppointmentModel) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.deleteAppointment(model.id)
            _uiState.update { it.copy(isLoading = false) }

            if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val rootJson = JSONObject(result.responseContent ?: "")
                    val msg = rootJson.optString("msg").ifEmpty { "Appointment deleted successfully" }
                    val error = rootJson.optBoolean("error", false)
                    if (error) {
                        _uiState.update { it.copy(alertTitle = "Error", alertMessage = msg) }
                    } else {
                        _uiState.update { it.copy(alertTitle = "Success", alertMessage = msg) }
                        loadAppointments()
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(alertTitle = "Error", alertMessage = e.message ?: "Failed parsing response") }
                }
            } else {
                val cleanMsg = extractErrorMessage(result.responseContent)
                _uiState.update {
                    it.copy(
                        alertTitle = "Error",
                        alertMessage = cleanMsg.ifEmpty { "Failed to delete appointment" }
                    )
                }
            }
        }
    }

    private fun loadHistory(clientId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.fetchHistory(clientId)
            _uiState.update { it.copy(isLoading = false) }

            if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val rootJson = JSONObject(result.responseContent ?: "")
                    if (!rootJson.optBoolean("error", false)) {
                        val historyArray = rootJson.optJSONArray("appointments") ?: JSONArray()
                        val list = parseAppointments(historyArray)
                        _uiState.update { it.copy(historyList = list) }
                    } else {
                        val errorMsg = rootJson.optString("msg").ifEmpty { "Failed to fetch appointment history" }
                        _uiState.update {
                            it.copy(
                                alertTitle = "Error",
                                alertMessage = errorMsg
                            )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(alertTitle = "Error", alertMessage = e.message ?: "Failed parsing response") }
                }
            } else {
                val cleanMsg = extractErrorMessage(result.responseContent)
                _uiState.update {
                    it.copy(
                        alertTitle = "Error",
                        alertMessage = cleanMsg.ifEmpty { "Failed to connect to history service" }
                    )
                }
            }
        }
    }

    private fun saveNewNote(appointmentId: String) {
        val noteText = _uiState.value.noteAddingMap[appointmentId]?.trim() ?: ""
        if (noteText.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.addNote(appointmentId, noteText)
            _uiState.update { it.copy(isLoading = false) }

            if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val rootJson = JSONObject(result.responseContent ?: "")
                    val msg = rootJson.optString("msg").ifEmpty { "Note added successfully" }
                    val error = rootJson.optBoolean("error", false)
                    if (error) {
                        _uiState.update { it.copy(alertTitle = "Error", alertMessage = msg) }
                    } else {
                        _uiState.update {
                            val clearedAddingMap = it.noteAddingMap.toMutableMap().apply { remove(appointmentId) }
                            val clearedExpanded = it.noteExpandedState.toMutableMap().apply { put(appointmentId, false) }
                            it.copy(alertTitle = "Success", alertMessage = msg, noteAddingMap = clearedAddingMap, noteExpandedState = clearedExpanded)
                        }
                        loadHistory(_uiState.value.historyClientId)
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(alertTitle = "Error", alertMessage = e.message ?: "Failed parsing response") }
                }
            } else {
                val cleanMsg = extractErrorMessage(result.responseContent)
                _uiState.update {
                    it.copy(
                        alertTitle = "Error",
                        alertMessage = cleanMsg.ifEmpty { "Failed to save note" }
                    )
                }
            }
        }
    }

    private fun saveEditedNote(appointmentId: String, noteId: String) {
        val noteText = _uiState.value.noteEditingMap[noteId]?.trim() ?: ""
        if (noteText.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.updateNote(appointmentId, noteId, noteText)
            _uiState.update { it.copy(isLoading = false) }

            if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val rootJson = JSONObject(result.responseContent ?: "")
                    val msg = rootJson.optString("msg").ifEmpty { "Note updated successfully" }
                    val error = rootJson.optBoolean("error", false)
                    if (error) {
                        _uiState.update { it.copy(alertTitle = "Error", alertMessage = msg) }
                    } else {
                        _uiState.update {
                            val clearedStates = it.noteEditingState.toMutableMap().apply { put(noteId, false) }
                            val clearedEditingMap = it.noteEditingMap.toMutableMap().apply { remove(noteId) }
                            val clearedExpanded = it.noteExpandedState.toMutableMap().apply { put(appointmentId, false) }
                            it.copy(alertTitle = "Success", alertMessage = msg, noteEditingState = clearedStates, noteEditingMap = clearedEditingMap, noteExpandedState = clearedExpanded)
                        }
                        loadHistory(_uiState.value.historyClientId)
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(alertTitle = "Error", alertMessage = e.message ?: "Failed parsing response") }
                }
            } else {
                val cleanMsg = extractErrorMessage(result.responseContent)
                _uiState.update {
                    it.copy(
                        alertTitle = "Error",
                        alertMessage = cleanMsg.ifEmpty { "Failed to update note" }
                    )
                }
            }
        }
    }

    private fun deleteNote(appointmentId: String, noteId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.deleteNote(appointmentId, noteId)
            _uiState.update { it.copy(isLoading = false) }

            if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val rootJson = JSONObject(result.responseContent ?: "")
                    val msg = rootJson.optString("msg").ifEmpty { "Note deleted successfully" }
                    val error = rootJson.optBoolean("error", false)
                    if (error) {
                        _uiState.update { it.copy(alertTitle = "Error", alertMessage = msg) }
                    } else {
                        _uiState.update { it.copy(alertTitle = "Success", alertMessage = msg) }
                        loadHistory(_uiState.value.historyClientId)
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(alertTitle = "Error", alertMessage = e.message ?: "Failed parsing response") }
                }
            } else {
                val cleanMsg = extractErrorMessage(result.responseContent)
                _uiState.update {
                    it.copy(
                        alertTitle = "Error",
                        alertMessage = cleanMsg.ifEmpty { "Failed to delete note" }
                    )
                }
            }
        }
    }

    private fun loadSettlementHistory(clientId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSettlementLoading = true) }
            val result = repository.fetchHistory(clientId)
            _uiState.update { it.copy(isSettlementLoading = false) }

            if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val rootJson = JSONObject(result.responseContent ?: "")
                    if (!rootJson.optBoolean("error", false)) {
                        val historyArray = rootJson.optJSONArray("appointments") ?: JSONArray()
                        val list = parseAppointments(historyArray)

                        val summaryObj = rootJson.optJSONObject("summary") ?: rootJson.optJSONObject("settlement")
                        val apiTotalPaid = (summaryObj?.optDouble("total_paid") ?: rootJson.optDouble("total_paid")).takeIf { !it.isNaN() && it > 0 }
                        val apiTotalTrans = (summaryObj?.optInt("total_transactions") ?: rootJson.optInt("total_transactions")).takeIf { it > 0 }
                        val apiRefunded = (summaryObj?.optDouble("refunded") ?: rootJson.optDouble("refunded")).takeIf { !it.isNaN() && it >= 0 }
                        val apiRefundInitiated = (summaryObj?.optDouble("refund_initiated") ?: rootJson.optDouble("refund_initiated")).takeIf { !it.isNaN() && it >= 0 }

                        _uiState.update {
                            it.copy(
                                rawSettlementList = list,
                                settlementApiTotalPaid = apiTotalPaid,
                                settlementApiTotalTransactions = apiTotalTrans,
                                settlementApiTotalRefunded = apiRefunded,
                                settlementApiTotalRefundInitiated = apiRefundInitiated
                            )
                        }
                        applySettlementFilterAndSorting()
                    } else {
                        val errorMsg = rootJson.optString("msg").ifEmpty { "Failed to fetch settlement history" }
                        _uiState.update {
                            it.copy(
                                alertTitle = "Error",
                                alertMessage = errorMsg
                            )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(alertTitle = "Error", alertMessage = e.message ?: "Failed parsing response") }
                }
            } else {
                val cleanMsg = extractErrorMessage(result.responseContent)
                _uiState.update {
                    it.copy(
                        alertTitle = "Error",
                        alertMessage = cleanMsg.ifEmpty { "Failed to connect to settlement service" }
                    )
                }
            }
        }
    }

    private fun applySettlementFilterAndSorting() {
        val state = _uiState.value
        val query = state.settlementSearchQuery.trim().lowercase(Locale.ROOT)
        var list = state.rawSettlementList

        // 1. Search Filter (Local multi-field search)
        if (query.isNotEmpty()) {
            list = list.filter { item ->
                val p = item.payment
                val cleanAppointmentStatus = item.appointment_status.lowercase(Locale.ROOT).trim()
                val rawPaymentStatus = (p?.status ?: "").lowercase(Locale.ROOT).trim()
                val isCancelled = cleanAppointmentStatus == "cancelled" || cleanAppointmentStatus == "canceled"
                val displayStatus = when {
                    isCancelled || rawPaymentStatus == "refunded" -> "refunded"
                    rawPaymentStatus == "refund_initiated" || rawPaymentStatus == "refund initiated" -> "refund initiated"
                    rawPaymentStatus == "paid" -> "paid"
                    rawPaymentStatus == "pending" || rawPaymentStatus == "payment_pending" || rawPaymentStatus == "payment pending" -> "pending"
                    else -> rawPaymentStatus
                }

                item.client_name.lowercase(Locale.ROOT).contains(query) ||
                item.id.lowercase(Locale.ROOT).contains(query) ||
                displayStatus.contains(query) ||
                rawPaymentStatus.contains(query) ||
                (p?.method ?: "").lowercase(Locale.ROOT).contains(query) ||
                (p?.amount_paid ?: "").contains(query) ||
                (p?.label ?: "").lowercase(Locale.ROOT).contains(query) ||
                item.appointment_from.lowercase(Locale.ROOT).contains(query) ||
                item.appointment_to.lowercase(Locale.ROOT).contains(query) ||
                item.appointment_status.lowercase(Locale.ROOT).contains(query) ||
                item.consultation_mode.lowercase(Locale.ROOT).contains(query)
            }
        }

        // 2. Sorting by date (Latest First)
        list = list.sortedByDescending { parseApiDate(it.appointment_from)?.time ?: 0L }

        val total = list.size
        val pages = if (total == 0) 0 else Math.ceil(total.toDouble() / PAGE_SIZE).toInt()

        _uiState.update {
            it.copy(
                filteredSettlementList = list,
                settlementTotalPages = pages,
                settlementCurrentPage = 0
            )
        }
        renderCurrentSettlementPage()
    }

    private fun renderCurrentSettlementPage() {
        val state = _uiState.value
        val startIndex = state.settlementCurrentPage * PAGE_SIZE
        val endIndex = Math.min(startIndex + PAGE_SIZE, state.filteredSettlementList.size)

        val pageList = if (startIndex < state.filteredSettlementList.size) {
            state.filteredSettlementList.subList(startIndex, endIndex)
        } else {
            emptyList()
        }

        _uiState.update { it.copy(currentSettlementPageList = pageList) }
    }

    private fun extractErrorMessage(responseContent: String?): String {
        if (responseContent.isNullOrBlank()) return ""
        return try {
            val json = JSONObject(responseContent)
            json.optString("msg", "")
        } catch (e: Exception) {
            responseContent
        }
    }
}
