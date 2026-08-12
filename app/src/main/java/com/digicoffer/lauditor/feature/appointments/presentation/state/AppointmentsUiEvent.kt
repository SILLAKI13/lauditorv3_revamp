package com.digicoffer.lauditor.feature.appointments.presentation.state

import com.digicoffer.lauditor.Appointments.Models.AppointmentModel

sealed interface AppointmentsUiEvent {
    object LoadAppointments : AppointmentsUiEvent
    data class SearchQueryChanged(val query: String) : AppointmentsUiEvent
    object PageNext : AppointmentsUiEvent
    object PagePrev : AppointmentsUiEvent
    data class ToggleActionMenu(val position: Int) : AppointmentsUiEvent

    // Card Action Menu Intents
    data class CancelAppointment(val model: AppointmentModel) : AppointmentsUiEvent
    data class DeleteAppointment(val model: AppointmentModel) : AppointmentsUiEvent

    // History Timeline Intents
    data class OpenHistory(val model: AppointmentModel) : AppointmentsUiEvent
    object CloseHistory : AppointmentsUiEvent

    // Dialog & Feedback Intents
    object DismissDialogs : AppointmentsUiEvent

    // History Timeline Notes Intents
    data class ToggleNotesExpanded(val appointmentId: String) : AppointmentsUiEvent
    data class NoteAddingDraftChanged(val appointmentId: String, val noteText: String) : AppointmentsUiEvent
    data class NoteEditingDraftChanged(val noteId: String, val noteText: String) : AppointmentsUiEvent
    data class SaveNewNote(val appointmentId: String) : AppointmentsUiEvent
    data class StartEditingNote(val noteId: String, val initialText: String) : AppointmentsUiEvent
    data class CancelEditingNote(val noteId: String) : AppointmentsUiEvent
    data class SaveEditedNote(val appointmentId: String, val noteId: String) : AppointmentsUiEvent
    data class DeleteNote(val appointmentId: String, val noteId: String) : AppointmentsUiEvent

    // Highlight Intents
    data class SetPendingHighlight(val highlightId: String) : AppointmentsUiEvent
}
