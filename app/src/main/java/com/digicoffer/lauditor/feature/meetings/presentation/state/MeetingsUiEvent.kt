package com.digicoffer.lauditor.feature.meetings.presentation.state

import com.digicoffer.lauditor.Meetings.Models.Event_Details_DO
import org.json.JSONObject

sealed interface MeetingsUiEvent {
    object LoadInitialData : MeetingsUiEvent
    data class SelectDay(val index: Int) : MeetingsUiEvent
    object PreviousWeek : MeetingsUiEvent
    object NextWeek : MeetingsUiEvent
    object PreviousMonth : MeetingsUiEvent
    object NextMonth : MeetingsUiEvent
    data class SelectMonthDate(val dateStr: String) : MeetingsUiEvent
    data class SearchQueryChanged(val query: String) : MeetingsUiEvent
    data class FilterChanged(val filter: String) : MeetingsUiEvent
    data class SwitchViewMode(val isMonthView: Boolean) : MeetingsUiEvent

    // Interactive Action Events
    data class EventRsvpChanged(val eventId: String, val rsvp: String) : MeetingsUiEvent
    data class AppointmentRsvpChanged(val appointmentId: String, val rsvp: String) : MeetingsUiEvent
    data class CancelAppointment(val appointmentId: String) : MeetingsUiEvent

    // Create / Edit Flow
    object OpenCreateEvent : MeetingsUiEvent
    data class OpenEditEvent(val event: Event_Details_DO) : MeetingsUiEvent
    object CloseForm : MeetingsUiEvent
    data class SubmitCreateEvent(val payload: JSONObject) : MeetingsUiEvent
    data class SubmitEditEvent(
        val eventId: String,
        val payload: JSONObject,
        val recurringChoice: String? = null,
        val updateScope: String? = null
    ) : MeetingsUiEvent

    // Delete Flow
    data class RequestDeleteEvent(val event: Event_Details_DO) : MeetingsUiEvent
    data class ConfirmDeleteEvent(
        val eventId: String,
        val recurringChoice: String? = null,
        val eventDeleteScope: String? = null
    ) : MeetingsUiEvent

    // Dialog dismissal
    object DismissRecurrenceDialog : MeetingsUiEvent
    object DismissTimesheetConfirm : MeetingsUiEvent

    // Metadata loading for Form
    data class LoadMatters(val matterType: String) : MeetingsUiEvent
    data class LoadCorporateTeamMembers(val corporateId: String) : MeetingsUiEvent
    object GenerateMeetingLink : MeetingsUiEvent

    // Detail Modal Intents
    data class ShowEventDetails(val eventId: String) : MeetingsUiEvent
    object DismissEventDetails : MeetingsUiEvent

    // Alerts Intents
    object DismissDialogs : MeetingsUiEvent
}
