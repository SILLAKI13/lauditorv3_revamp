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

    // Settlement History States (for active client settlement screen)
    val settlementClientId: String = "",
    val settlementClientName: String = "",
    val settlementClientProfilePic: String = "",
    val rawSettlementList: List<AppointmentModel> = emptyList(),
    val filteredSettlementList: List<AppointmentModel> = emptyList(),
    val currentSettlementPageList: List<AppointmentModel> = emptyList(),
    val settlementCurrentPage: Int = 0,
    val settlementTotalPages: Int = 0,
    val settlementSearchQuery: String = "",
    val settlementStatusFilter: String = "All Status",
    val settlementSortOption: String = "Latest First",
    val isSettlementLoading: Boolean = false,
    val settlementApiTotalPaid: Double? = null,
    val settlementApiTotalTransactions: Int? = null,
    val settlementApiTotalRefunded: Double? = null,
    val settlementApiTotalRefundInitiated: Double? = null,

    // Note Input states maps
    val noteAddingMap: Map<String, String> = emptyMap(),      // key: appointmentId -> draft text
    val noteEditingMap: Map<String, String> = emptyMap(),     // key: noteId -> draft text
    val noteEditingState: Map<String, Boolean> = emptyMap(),  // key: noteId -> isEditing
    val noteExpandedState: Map<String, Boolean> = emptyMap()  // key: appointmentId -> isExpanded
)
