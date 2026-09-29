package com.digicoffer.lauditor.feature.meetings.presentation.state

import com.digicoffer.lauditor.Appointments.Models.AppointmentModel
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.TimeZonesDO
import com.digicoffer.lauditor.Matter.Models.ViewMatterModel
import com.digicoffer.lauditor.Meetings.Models.Day
import com.digicoffer.lauditor.Meetings.Models.Event_Details_DO
import com.digicoffer.lauditor.Meetings.Models.RelationshipsDO
import com.digicoffer.lauditor.Meetings.Models.TeamDo
import com.digicoffer.lauditor.feature.meetings.presentation.components.MonthDay

data class MeetingsUiState(
    val isMonthView: Boolean = false,
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

    // Week Calendar state
    val selectedDate: String = "",
    val weekDateRangeText: String = "",
    val weekDays: List<Day> = emptyList(),
    val selectedDayIndex: Int = 0,
    val todayDayIndex: Int = 0,

    // Month Calendar state
    val currentMonthTitle: String = "",
    val monthDays: List<MonthDay> = emptyList(),

    // Create / Edit state
    val isCreateMode: Boolean = false,
    val editingEvent: Event_Details_DO? = null,

    // Metadata for Create/Edit Form
    val timezones: List<TimeZonesDO> = emptyList(),
    val matters: List<ViewMatterModel> = emptyList(),
    val teamMembers: List<TeamDo> = emptyList(),
    val clients: List<RelationshipsDO> = emptyList(),
    val corporateClients: List<RelationshipsDO> = emptyList(),
    val corporateTeamMembers: List<RelationshipsDO> = emptyList(),
    val meetingRoomId: String? = null,
    val generatedMeetingLink: String? = null,

    // Detail Popups / Sheets state
    val selectedEventDetails: Event_Details_DO? = null,

    // Dialog flags
    val showRecurrenceChoiceDialog: Boolean = false,
    val recurrenceActionType: String? = null, // "EDIT" or "DELETE"
    val pendingRecurrenceEvent: Event_Details_DO? = null,
    val showTimesheetDeleteConfirm: Boolean = false
)
