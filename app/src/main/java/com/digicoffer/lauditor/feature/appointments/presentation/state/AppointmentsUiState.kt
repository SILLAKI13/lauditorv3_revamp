package com.digicoffer.lauditor.feature.appointments.presentation.state

import com.digicoffer.lauditor.Appointments.Models.AppointmentModel

data class AppointmentsUiState(
    // Main Appointments Listing States
    val appointmentList: List<AppointmentModel> = emptyList(),
    val filteredList: List<AppointmentModel> = emptyList(),
    val currentPageList: List<AppointmentModel> = emptyList(),
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val toastMessage: String? = null,
    val alertTitle: String? = null,
    val alertMessage: String? = null,
    val pendingHighlightId: String = "",
    val expandedCardPosition: Int = -1,

    // History Timeline States (for active client history sub-screen overlay)
    val historyClientId: String = "",
    val historyClientName: String = "",
    val historyClientProfilePic: String = "",
    val historyList: List<AppointmentModel> = emptyList(),

    // Note Input states maps
    val noteAddingMap: Map<String, String> = emptyMap(),      // key: appointmentId -> draft text
    val noteEditingMap: Map<String, String> = emptyMap(),     // key: noteId -> draft text
    val noteEditingState: Map<String, Boolean> = emptyMap(),  // key: noteId -> isEditing
    val noteExpandedState: Map<String, Boolean> = emptyMap()  // key: appointmentId -> isExpanded
)
