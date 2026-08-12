package com.digicoffer.lauditor.feature.meetings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digicoffer.lauditor.Appointments.Models.AppointmentModel
import com.digicoffer.lauditor.Appointments.Models.PaymentModel
import com.digicoffer.lauditor.Meetings.Models.Event_Details_DO
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.feature.meetings.data.repository.MeetingsRepository
import com.digicoffer.lauditor.feature.meetings.presentation.state.MeetingsUiEvent
import com.digicoffer.lauditor.feature.meetings.presentation.state.MeetingsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.GregorianCalendar
import java.util.concurrent.TimeUnit

class MeetingsViewModel(
    private val repository: MeetingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MeetingsUiState())
    val uiState: StateFlow<MeetingsUiState> = _uiState.asStateFlow()

    fun onEvent(event: MeetingsUiEvent) {
        when (event) {
            is MeetingsUiEvent.LoadWeeklyData -> loadWeeklyData(event.startDate, event.endDate)
            is MeetingsUiEvent.LoadMonthlyData -> loadMonthlyData(event.dateStr, event.startDate, event.endDate)
            is MeetingsUiEvent.SearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
            }
            is MeetingsUiEvent.FilterChanged -> {
                _uiState.update { it.copy(selectedFilter = event.filter) }
            }
            is MeetingsUiEvent.SwitchViewMode -> {
                _uiState.update { it.copy(isMonthView = event.isMonthView) }
            }
            is MeetingsUiEvent.EventRsvpChanged -> updateEventRsvp(event.eventId, event.rsvp)
            is MeetingsUiEvent.AppointmentRsvpChanged -> updateAppointmentRsvp(event.appointmentId, event.rsvp)
            is MeetingsUiEvent.CancelAppointment -> cancelAppointment(event.appointmentId)
            is MeetingsUiEvent.ShowEventDetails -> loadEventDetails(event.eventId)
            is MeetingsUiEvent.DismissEventDetails -> {
                _uiState.update { it.copy(selectedEventDetails = null) }
            }
            is MeetingsUiEvent.DismissDialogs -> {
                _uiState.update { it.copy(alertTitle = null, alertMessage = null, toastMessage = null) }
            }
        }
    }

    private fun loadWeeklyData(startDate: String, endDate: String) {
        val timezoneOffset = getTimezoneOffset()
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // 1. Fetch Events
            val eventsResult = repository.fetchWeeklyEvents(timezoneOffset, startDate, endDate)
            if (eventsResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val json = JSONObject(eventsResult.responseContent ?: "")
                    if (!json.getBoolean("error")) {
                        val eventsArray = json.optJSONArray("events") ?: JSONArray()
                        _uiState.update { it.copy(weeklyEvents = parseEvents(eventsArray)) }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // 2. Fetch Appointments
            val apptResult = repository.fetchAppointments(startDate, endDate)
            if (apptResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val json = JSONObject(apptResult.responseContent ?: "")
                    if (!json.getBoolean("error")) {
                        val apptsArray = json.optJSONArray("appointments") ?: JSONArray()
                        _uiState.update { it.copy(weeklyAppointments = parseAppointments(apptsArray)) }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun loadMonthlyData(dateStr: String, startDate: String, endDate: String) {
        val timezoneOffset = getTimezoneOffset()
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // 1. Fetch Events
            val eventsResult = repository.fetchMonthlyEvents(timezoneOffset, dateStr)
            if (eventsResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val json = JSONObject(eventsResult.responseContent ?: "")
                    if (!json.getBoolean("error")) {
                        val eventsArray = json.optJSONArray("events") ?: JSONArray()
                        _uiState.update { it.copy(monthlyEvents = parseEvents(eventsArray)) }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // 2. Fetch Appointments
            val apptResult = repository.fetchAppointments(startDate, endDate)
            if (apptResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val json = JSONObject(apptResult.responseContent ?: "")
                    if (!json.getBoolean("error")) {
                        val apptsArray = json.optJSONArray("appointments") ?: JSONArray()
                        _uiState.update { it.copy(monthlyAppointments = parseAppointments(apptsArray)) }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun updateEventRsvp(eventId: String, response: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.updateEventRsvp(eventId, response)
            _uiState.update { it.copy(isLoading = false) }

            if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                val json = JSONObject(result.responseContent ?: "")
                _uiState.update { it.copy(toastMessage = json.optString("msg", "RSVP saved successfully")) }
            } else {
                _uiState.update { it.copy(alertTitle = "Error", alertMessage = "Failed to submit RSVP") }
            }
        }
    }

    private fun updateAppointmentRsvp(appointmentId: String, status: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.updateAppointmentRsvp(appointmentId, status)
            _uiState.update { it.copy(isLoading = false) }

            if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                val json = JSONObject(result.responseContent ?: "")
                _uiState.update { it.copy(toastMessage = json.optString("msg", "RSVP saved successfully")) }
            } else {
                _uiState.update { it.copy(alertTitle = "Error", alertMessage = "Failed to submit RSVP") }
            }
        }
    }

    private fun cancelAppointment(appointmentId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.cancelAppointment(appointmentId)
            _uiState.update { it.copy(isLoading = false) }

            if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                val json = JSONObject(result.responseContent ?: "")
                _uiState.update { it.copy(toastMessage = json.optString("msg", "Appointment cancelled successfully")) }
            } else {
                _uiState.update { it.copy(alertTitle = "Error", alertMessage = "Failed to cancel appointment") }
            }
        }
    }

    private fun loadEventDetails(eventId: String) {
        val timezoneOffset = getTimezoneOffset()
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.fetchEventDetails(eventId, timezoneOffset)
            _uiState.update { it.copy(isLoading = false) }

            if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val json = JSONObject(result.responseContent ?: "")
                    if (!json.getBoolean("error")) {
                        val eventDetailsObj = json.optJSONObject("event") ?: JSONObject()
                        val details = parseEventDetails(eventDetailsObj)
                        _uiState.update { it.copy(selectedEventDetails = details) }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun parseEvents(array: JSONArray): List<Event_Details_DO> {
        val list = mutableListOf<Event_Details_DO>()
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            val model = Event_Details_DO().apply {
                id = obj.optString("id", "")
                title = obj.optString("title", "")
                description = obj.optString("description", "")
                from_ts = obj.optString("from_ts", "")
                to_ts = obj.optString("to_ts", "")
                event_type = obj.optString("event_type", "")
                location = obj.optString("location", "")
                dialin = obj.optString("dialin", "")
                meeting_link = obj.optString("meeting_link", "")
                all_day = obj.optBoolean("allday", false)
                isRecurring = obj.optBoolean("isrecurring", false)
                repeat_interval = obj.optString("repeat_interval", "")
                owner_name = obj.optString("owner_name", "")
                owner = obj.optBoolean("owner", false)

                // Invitee lists
                if (obj.has("invitees_internal")) {
                    team_name = obj.optJSONArray("invitees_internal") ?: JSONArray()
                }
                if (obj.has("invitees_external")) {
                    tm_name = obj.optJSONArray("invitees_external") ?: JSONArray()
                }
                if (obj.has("invitees_corporate")) {
                    corporate = obj.optJSONArray("invitees_corporate") ?: JSONArray()
                }
                if (obj.has("invitees_consumer_external")) {
                    consumer_external = obj.optJSONArray("invitees_consumer_external") ?: JSONArray()
                }
            }
            list.add(model)
        }
        return list
    }

    private fun parseEventDetails(obj: JSONObject): Event_Details_DO {
        return Event_Details_DO().apply {
            id = obj.optString("id", "")
            title = obj.optString("title", "")
            description = obj.optString("description", "")
            from_ts = obj.optString("from_ts", "")
            to_ts = obj.optString("to_ts", "")
            event_type = obj.optString("event_type", "")
            location = obj.optString("location", "")
            dialin = obj.optString("dialin", "")
            meeting_link = obj.optString("meeting_link", "")
            all_day = obj.optBoolean("allday", false)
            isRecurring = obj.optBoolean("isrecurring", false)
            repeat_interval = obj.optString("repeat_interval", "")
            owner_name = obj.optString("owner_name", "")
            owner = obj.optBoolean("owner", false)

            if (obj.has("invitees_internal")) {
                team_name = obj.optJSONArray("invitees_internal") ?: JSONArray()
            }
            if (obj.has("invitees_external")) {
                tm_name = obj.optJSONArray("invitees_external") ?: JSONArray()
            }
            if (obj.has("invitees_corporate")) {
                corporate = obj.optJSONArray("invitees_corporate") ?: JSONArray()
            }
            if (obj.has("invitees_consumer_external")) {
                consumer_external = obj.optJSONArray("invitees_consumer_external") ?: JSONArray()
            }
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
                appointment_status = jsonObject.optString("appointment_status", "")
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
                    }
                }
            }
            list.add(model)
        }
        return list
    }

    private fun getTimezoneOffset(): Long {
        val calendar = GregorianCalendar()
        val timeZone = calendar.timeZone
        val offset = timeZone.rawOffset
        val hours = TimeUnit.MILLISECONDS.toMinutes(offset.toLong())
        return -1 * hours
    }
}
