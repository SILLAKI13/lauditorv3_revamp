package com.digicoffer.lauditor.feature.meetings.presentation.state

sealed interface MeetingsUiEvent {
    data class LoadWeeklyData(val startDate: String, val endDate: String) : MeetingsUiEvent
    data class LoadMonthlyData(val dateStr: String, val startDate: String, val endDate: String) : MeetingsUiEvent
    data class SearchQueryChanged(val query: String) : MeetingsUiEvent
    data class FilterChanged(val filter: String) : MeetingsUiEvent
    data class SwitchViewMode(val isMonthView: Boolean) : MeetingsUiEvent

    // Interactive Action Events
    data class EventRsvpChanged(val eventId: String, val rsvp: String) : MeetingsUiEvent
    data class AppointmentRsvpChanged(val appointmentId: String, val rsvp: String) : MeetingsUiEvent
    data class CancelAppointment(val appointmentId: String) : MeetingsUiEvent

    // Detail Modal Intents
    data class ShowEventDetails(val eventId: String) : MeetingsUiEvent
    object DismissEventDetails : MeetingsUiEvent

    // Alerts Intents
    object DismissDialogs : MeetingsUiEvent
}
