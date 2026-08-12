package com.digicoffer.lauditor.feature.meetings.presentation.state

import com.digicoffer.lauditor.Appointments.Models.AppointmentModel
import com.digicoffer.lauditor.Meetings.Models.Event_Details_DO

data class MeetingsUiState(
    val isMonthView: Boolean = true,
    val selectedFilter: String = "All",
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val toastMessage: String? = null,
    val alertTitle: String? = null,
    val alertMessage: String? = null,

    // Loaded Data Lists
    val weeklyEvents: List<Event_Details_DO> = emptyList(),
    val weeklyAppointments: List<AppointmentModel> = emptyList(),
    val monthlyEvents: List<Event_Details_DO> = emptyList(),
    val monthlyAppointments: List<AppointmentModel> = emptyList(),

    // Detail Popups / Sheets state
    val selectedEventDetails: Event_Details_DO? = null
)
