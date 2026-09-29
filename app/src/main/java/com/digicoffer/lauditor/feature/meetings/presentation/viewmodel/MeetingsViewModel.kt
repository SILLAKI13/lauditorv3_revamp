package com.digicoffer.lauditor.feature.meetings.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digicoffer.lauditor.Appointments.Models.AppointmentModel
import com.digicoffer.lauditor.Appointments.Models.PaymentModel
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.TimeZonesDO
import com.digicoffer.lauditor.Matter.Models.ViewMatterModel
import com.digicoffer.lauditor.Meetings.Models.Day
import com.digicoffer.lauditor.Meetings.Models.Event_Details_DO
import com.digicoffer.lauditor.Meetings.Models.RelationshipsDO
import com.digicoffer.lauditor.Meetings.Models.TeamDo
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.feature.meetings.data.repository.MeetingsRepository
import com.digicoffer.lauditor.feature.meetings.presentation.components.MonthDay
import com.digicoffer.lauditor.feature.meetings.presentation.state.MeetingsUiEvent
import com.digicoffer.lauditor.feature.meetings.presentation.state.MeetingsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class MeetingsViewModel(
    private val repository: MeetingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MeetingsUiState())
    val uiState: StateFlow<MeetingsUiState> = _uiState.asStateFlow()

    private var currentWeekCal: Calendar = Calendar.getInstance()
    private var currentWeekStartDateStr: String = "" // "ddMMyyyy"
    private var currentWeekEndDateStr: String = "" // "ddMMyyyy"

    private var currentMonthCal: Calendar = Calendar.getInstance()

    init {
        setupCurrentWeek(Calendar.getInstance())
        setupCurrentMonth(Calendar.getInstance())
        loadData()
        loadInitialMetadata()
    }

    fun onEvent(event: MeetingsUiEvent) {
        when (event) {
            is MeetingsUiEvent.LoadInitialData -> {
                loadData()
            }
            is MeetingsUiEvent.SelectDay -> {
                val days = _uiState.value.weekDays
                if (event.index in days.indices) {
                    val selectedDate = days[event.index].date
                    _uiState.update {
                        it.copy(
                            selectedDayIndex = event.index,
                            selectedDate = selectedDate
                        )
                    }
                }
            }
            is MeetingsUiEvent.PreviousWeek -> {
                shiftWeek(-1)
            }
            is MeetingsUiEvent.NextWeek -> {
                shiftWeek(1)
            }
            is MeetingsUiEvent.PreviousMonth -> {
                shiftMonth(-1)
            }
            is MeetingsUiEvent.NextMonth -> {
                shiftMonth(1)
            }
            is MeetingsUiEvent.SelectMonthDate -> {
                _uiState.update { it.copy(selectedDate = event.dateStr) }
                rebuildMonthDays()
            }
            is MeetingsUiEvent.SearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
            }
            is MeetingsUiEvent.FilterChanged -> {
                _uiState.update { it.copy(selectedFilter = event.filter) }
            }
            is MeetingsUiEvent.SwitchViewMode -> {
                _uiState.update { it.copy(isMonthView = event.isMonthView) }
                if (event.isMonthView) {
                    setupCurrentMonth(Calendar.getInstance())
                } else {
                    setupCurrentWeek(Calendar.getInstance())
                }
                loadData()
            }
            is MeetingsUiEvent.EventRsvpChanged -> updateEventRsvp(event.eventId, event.rsvp)
            is MeetingsUiEvent.AppointmentRsvpChanged -> updateAppointmentRsvp(event.appointmentId, event.rsvp)
            is MeetingsUiEvent.CancelAppointment -> cancelAppointment(event.appointmentId)

            is MeetingsUiEvent.OpenCreateEvent -> {
                _uiState.update {
                    it.copy(
                        isCreateMode = true,
                        editingEvent = null,
                        meetingRoomId = null,
                        generatedMeetingLink = null
                    )
                }
                loadInitialMetadata()
                generateMeetingLink()
            }
            is MeetingsUiEvent.OpenEditEvent -> {
                _uiState.update {
                    it.copy(
                        isCreateMode = true,
                        editingEvent = event.event,
                        meetingRoomId = null,
                        generatedMeetingLink = event.event.meeting_link
                    )
                }
                loadInitialMetadata()
                val eventType = event.event.event_type ?: ""
                if (eventType.equals("legal", ignoreCase = true) || eventType.equals("general", ignoreCase = true)) {
                    val matterParam = if (eventType.equals("legal", ignoreCase = true)) "legal" else "general"
                    loadMatters(matterParam)
                }
            }
            is MeetingsUiEvent.CloseForm -> {
                _uiState.update {
                    it.copy(
                        isCreateMode = false,
                        editingEvent = null,
                        meetingRoomId = null,
                        generatedMeetingLink = null
                    )
                }
            }
            is MeetingsUiEvent.SubmitCreateEvent -> {
                createEvent(event.payload)
            }
            is MeetingsUiEvent.SubmitEditEvent -> {
                updateEvent(event.eventId, event.payload, event.recurringChoice, event.updateScope)
            }
            is MeetingsUiEvent.RequestDeleteEvent -> {
                val evt = event.event
                if (evt.isRecurring) {
                    _uiState.update {
                        it.copy(
                            showRecurrenceChoiceDialog = true,
                            recurrenceActionType = "DELETE",
                            pendingRecurrenceEvent = evt
                        )
                    }
                } else if (evt.is_linked_with_timesheet || evt.timesheet_added) {
                    _uiState.update {
                        it.copy(
                            showTimesheetDeleteConfirm = true,
                            pendingRecurrenceEvent = evt
                        )
                    }
                } else {
                    deleteEvent(evt.id.orEmpty(), recurringChoice = null, eventDeleteScope = "DELETE_EVENT_ONLY")
                }
            }
            is MeetingsUiEvent.ConfirmDeleteEvent -> {
                deleteEvent(event.eventId, event.recurringChoice, event.eventDeleteScope)
            }
            is MeetingsUiEvent.DismissRecurrenceDialog -> {
                _uiState.update {
                    it.copy(
                        showRecurrenceChoiceDialog = false,
                        recurrenceActionType = null,
                        pendingRecurrenceEvent = null
                    )
                }
            }
            is MeetingsUiEvent.DismissTimesheetConfirm -> {
                _uiState.update {
                    it.copy(
                        showTimesheetDeleteConfirm = false,
                        pendingRecurrenceEvent = null
                    )
                }
            }
            is MeetingsUiEvent.LoadMatters -> {
                loadMatters(event.matterType)
            }
            is MeetingsUiEvent.LoadCorporateTeamMembers -> {
                loadCorporateTeamMembers(event.corporateId)
            }
            is MeetingsUiEvent.GenerateMeetingLink -> {
                generateMeetingLink()
            }
            is MeetingsUiEvent.ShowEventDetails -> {
                loadEventDetails(event.eventId)
            }
            is MeetingsUiEvent.DismissEventDetails -> {
                _uiState.update { it.copy(selectedEventDetails = null) }
            }
            is MeetingsUiEvent.DismissDialogs -> {
                _uiState.update { it.copy(alertTitle = null, alertMessage = null, toastMessage = null) }
            }
        }
    }

    private fun setupCurrentWeek(baseCal: Calendar) {
        currentWeekCal = baseCal.clone() as Calendar
        val firstDay = currentWeekCal.firstDayOfWeek
        while (currentWeekCal.get(Calendar.DAY_OF_WEEK) != firstDay) {
            currentWeekCal.add(Calendar.DATE, -1)
        }

        val d1 = SimpleDateFormat("ddMMyyyy", Locale.US)
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        val todayStr = dateFormat.format(Calendar.getInstance().time)

        currentWeekStartDateStr = d1.format(currentWeekCal.time)
        val fromDateStr = dateFormat.format(currentWeekCal.time)

        val daysList = mutableListOf<Day>()
        var todayIdx = 0
        var selIdx = 0

        for (i in 0..6) {
            val date = dateFormat.format(currentWeekCal.time)
            if (date == todayStr) {
                todayIdx = i
                selIdx = i
            }
            daysList.add(Day(date))
            currentWeekCal.add(Calendar.DATE, 1)
        }

        currentWeekCal.add(Calendar.DATE, -1)
        currentWeekEndDateStr = d1.format(currentWeekCal.time)
        val toDateStr = dateFormat.format(currentWeekCal.time)

        val rangeText = "${AndroidUtils.formatToMMMddYYYY(fromDateStr)} - ${AndroidUtils.formatToMMMddYYYY(toDateStr)}"
        val selectedDateStr = daysList.getOrNull(selIdx)?.date ?: todayStr

        _uiState.update {
            it.copy(
                weekDateRangeText = rangeText,
                weekDays = daysList,
                selectedDayIndex = selIdx,
                todayDayIndex = todayIdx,
                selectedDate = selectedDateStr
            )
        }
    }

    private fun shiftWeek(offset: Int) {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        val currentDays = _uiState.value.weekDays
        if (currentDays.isEmpty()) return

        try {
            val firstDayDate = dateFormat.parse(currentDays[0].date) ?: return
            val newCal = Calendar.getInstance().apply {
                time = firstDayDate
                add(Calendar.WEEK_OF_YEAR, offset)
            }
            setupCurrentWeek(newCal)
            loadData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupCurrentMonth(cal: Calendar) {
        currentMonthCal = cal.clone() as Calendar
        val monthTitleFormat = SimpleDateFormat("MMMM yyyy", Locale.US)
        val monthTitle = monthTitleFormat.format(currentMonthCal.time)

        val todayDateStr = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Calendar.getInstance().time)
        val currentSelectedDate = _uiState.value.selectedDate.ifEmpty { todayDateStr }

        _uiState.update {
            it.copy(
                currentMonthTitle = monthTitle,
                selectedDate = currentSelectedDate
            )
        }
        rebuildMonthDays()
    }

    private fun shiftMonth(offset: Int) {
        currentMonthCal.add(Calendar.MONTH, offset)
        val monthTitleFormat = SimpleDateFormat("MMMM yyyy", Locale.US)
        val monthTitle = monthTitleFormat.format(currentMonthCal.time)

        // Set selected date to 1st of month if switching months
        val monthDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        val firstDayCal = currentMonthCal.clone() as Calendar
        firstDayCal.set(Calendar.DAY_OF_MONTH, 1)
        val newSelectedDate = monthDateFormat.format(firstDayCal.time)

        _uiState.update {
            it.copy(
                currentMonthTitle = monthTitle,
                selectedDate = newSelectedDate
            )
        }
        rebuildMonthDays()
        loadData()
    }

    private fun rebuildMonthDays() {
        val cal = currentMonthCal.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)

        val currentMonthIndex = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        // First day of week (Sunday = 1, Monday = 2, ...)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday
        val leadingEmptyDays = firstDayOfWeek - 1

        val todayCal = Calendar.getInstance()
        val todayStr = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(todayCal.time)
        val selectedDateStr = _uiState.value.selectedDate

        val monthDaysList = mutableListOf<MonthDay>()

        // Leading empty days to align the 1st of month with day of week
        for (i in 0 until leadingEmptyDays) {
            monthDaysList.add(
                MonthDay(
                    dayNumber = 0,
                    dateStr = "",
                    isCurrentMonth = false,
                    isSelected = false,
                    isToday = false,
                    eventCount = 0,
                    hasEvents = false,
                    hasAppointments = false
                )
            )
        }

        // Current month days
        val currentEvents = _uiState.value.monthlyEvents
        val currentAppts = _uiState.value.monthlyAppointments

        for (day in 1..daysInMonth) {
            val dCal = cal.clone() as Calendar
            dCal.set(Calendar.DAY_OF_MONTH, day)
            val dStr = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(dCal.time)

            val isSelected = dStr == selectedDateStr
            val isToday = dStr == todayStr

            val eventsCount = currentEvents.count { isSameDay(it.from_ts, dStr) }
            val apptsCount = currentAppts.count { isSameDay(it.appointment_from, dStr) }
            val totalCount = eventsCount + apptsCount

            monthDaysList.add(
                MonthDay(
                    dayNumber = day,
                    dateStr = dStr,
                    isCurrentMonth = true,
                    isSelected = isSelected,
                    isToday = isToday,
                    eventCount = totalCount,
                    hasEvents = eventsCount > 0,
                    hasAppointments = apptsCount > 0
                )
            )
        }

        // Trailing empty days to complete the row
        val remainder = monthDaysList.size % 7
        if (remainder != 0) {
            val trailingDays = 7 - remainder
            for (day in 1..trailingDays) {
                monthDaysList.add(
                    MonthDay(
                        dayNumber = 0,
                        dateStr = "",
                        isCurrentMonth = false,
                        isSelected = false,
                        isToday = false,
                        eventCount = 0,
                        hasEvents = false,
                        hasAppointments = false
                    )
                )
            }
        }

        _uiState.update { it.copy(monthDays = monthDaysList) }
    }

    private fun isSameDay(dateStr: String?, targetDdmmyyyy: String): Boolean {
        if (dateStr.isNullOrEmpty() || targetDdmmyyyy.isEmpty()) return false
        return try {
            if (dateStr.contains("T")) {
                val d = AndroidUtils.stringToDateTimeDefault(dateStr, "yyyy-MM-dd'T'HH:mm:ss")
                val formatted = AndroidUtils.getDateToString(d, "dd-MM-yyyy")
                formatted == targetDdmmyyyy
            } else if (dateStr.contains("-")) {
                val parts = dateStr.split("-")
                if (parts.size == 3 && parts[0].length == 4) {
                    val d = AndroidUtils.stringToDateTimeDefault(dateStr, "yyyy-MM-dd")
                    val formatted = AndroidUtils.getDateToString(d, "dd-MM-yyyy")
                    formatted == targetDdmmyyyy
                } else {
                    dateStr == targetDdmmyyyy
                }
            } else false
        } catch (e: Exception) {
            false
        }
    }

    private fun loadData() {
        val timezoneOffset = getTimezoneOffset()
        val startDateFormatted = AndroidUtils.convertAnyDateToYYYYMMDD(currentWeekStartDateStr)
        val endDateFormatted = AndroidUtils.convertAnyDateToYYYYMMDD(currentWeekEndDateStr)

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            if (!_uiState.value.isMonthView) {
                // 1. Fetch Weekly Events
                val eventsResult = repository.fetchWeeklyEvents(timezoneOffset, currentWeekStartDateStr, currentWeekEndDateStr)
                if (eventsResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                    try {
                        val json = JSONObject(eventsResult.responseContent ?: "")
                        if (!json.optBoolean("error", false)) {
                            val eventsArray = json.optJSONArray("events") ?: JSONArray()
                            val events = parseEvents(eventsArray)
                            _uiState.update { it.copy(weeklyEvents = events) }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // 2. Fetch Appointments
                val apptResult = repository.fetchAppointments(startDateFormatted, endDateFormatted)
                if (apptResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                    try {
                        val json = JSONObject(apptResult.responseContent ?: "")
                        if (!json.optBoolean("error", false)) {
                            val apptsArray = json.optJSONArray("appointments") ?: JSONArray()
                            val appts = parseAppointments(apptsArray)
                            _uiState.update { it.copy(weeklyAppointments = appts) }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } else {
                // Month View
                val sdfMonth = SimpleDateFormat("MMyyyy", Locale.US)
                val monthStr = sdfMonth.format(currentMonthCal.time)

                // Month start and end dates for appointments
                val cal = currentMonthCal.clone() as Calendar
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val monthStartDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                val monthEndDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)

                val monthEventsResult = repository.fetchMonthlyEvents(timezoneOffset, monthStr)
                if (monthEventsResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                    try {
                        val json = JSONObject(monthEventsResult.responseContent ?: "")
                        if (!json.optBoolean("error", false)) {
                            val eventsArray = json.optJSONArray("events") ?: JSONArray()
                            val events = parseEvents(eventsArray)
                            _uiState.update { it.copy(monthlyEvents = events) }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                val apptResult = repository.fetchAppointments(monthStartDate, monthEndDate)
                if (apptResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                    try {
                        val json = JSONObject(apptResult.responseContent ?: "")
                        if (!json.optBoolean("error", false)) {
                            val apptsArray = json.optJSONArray("appointments") ?: JSONArray()
                            val appts = parseAppointments(apptsArray)
                            _uiState.update { it.copy(monthlyAppointments = appts) }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                rebuildMonthDays()
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun loadInitialMetadata() {
        viewModelScope.launch {
            // Timezones
            val tzResult = repository.fetchTimezones()
            if (tzResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val json = JSONObject(tzResult.responseContent ?: "")
                    if (!json.optBoolean("error", false)) {
                        val tzArray = json.optJSONArray("timezones") ?: JSONArray()
                        val list = mutableListOf<TimeZonesDO>()
                        for (i in 0 until tzArray.length()) {
                            val item = tzArray.getJSONArray(i)
                            val model = TimeZonesDO().apply {
                                GMT = item.optString(0)
                                NAME = item.optString(1)
                            }
                            list.add(model)
                        }
                        _uiState.update { it.copy(timezones = list) }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Team members
            val tmResult = repository.fetchTeamMembers()
            if (tmResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val json = JSONObject(tmResult.responseContent ?: "")
                    if (!json.optBoolean("error", false)) {
                        val usersArray = json.optJSONArray("users") ?: JSONArray()
                        val list = mutableListOf<TeamDo>()
                        for (i in 0 until usersArray.length()) {
                            val obj = usersArray.getJSONObject(i)
                            val model = TeamDo().apply {
                                id = obj.optString("id")
                                name = obj.optString("name")
                            }
                            list.add(model)
                        }
                        _uiState.update { it.copy(teamMembers = list) }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Clients
            if (Constants.ROLE != "AAM") {
                val clientResult = repository.fetchClients()
                if (clientResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                    try {
                        val json = JSONObject(clientResult.responseContent ?: "")
                        if (!json.optBoolean("error", false)) {
                            val data = json.optJSONObject("data") ?: JSONObject()
                            val relArray = data.optJSONArray("relationships") ?: JSONArray()
                            val list = mutableListOf<RelationshipsDO>()
                            for (i in 0 until relArray.length()) {
                                val obj = relArray.getJSONObject(i)
                                val model = RelationshipsDO().apply {
                                    id = obj.optString("id")
                                    name = obj.optString("name")
                                    type = obj.optString("type")
                                }
                                list.add(model)
                            }
                            _uiState.update { it.copy(clients = list) }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // Corporate Clients
                val corpResult = repository.fetchCorporateClients()
                if (corpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                    try {
                        val json = JSONObject(corpResult.responseContent ?: "")
                        if (!json.optBoolean("error", false)) {
                            val relArray = json.optJSONArray("relationships") ?: JSONArray()
                            val list = mutableListOf<RelationshipsDO>()
                            for (i in 0 until relArray.length()) {
                                val obj = relArray.getJSONObject(i)
                                val model = RelationshipsDO().apply {
                                    id = obj.optString("id")
                                    name = obj.optString("name")
                                    type = obj.optString("type")
                                }
                                list.add(model)
                            }
                            _uiState.update { it.copy(corporateClients = list) }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun loadMatters(matterType: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.fetchMatters(matterType)
            _uiState.update { it.copy(isLoading = false) }
            if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val json = JSONObject(result.responseContent ?: "")
                    if (!json.optBoolean("error", false)) {
                        val mattersArray = json.optJSONArray("matters") ?: JSONArray()
                        val list = mutableListOf<ViewMatterModel>()
                        for (i in 0 until mattersArray.length()) {
                            val obj = mattersArray.getJSONObject(i)
                            val model = ViewMatterModel().apply {
                                id = obj.optString("id")
                                title = obj.optString("title")
                                status = obj.optString("status")
                                documents = obj.optJSONArray("documents")
                                clients = obj.optJSONArray("clients")
                                members = obj.optJSONArray("members")
                                corporate = obj.optJSONArray("corporate")
                                corp_has_value = obj.has("corporate")
                            }
                            list.add(model)
                        }
                        _uiState.update { it.copy(matters = list) }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun loadCorporateTeamMembers(corporateId: String) {
        viewModelScope.launch {
            val result = repository.fetchEntityTeamMembers(corporateId)
            if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val json = JSONObject(result.responseContent ?: "")
                    if (!json.optBoolean("error", false)) {
                        val usersArray = json.optJSONArray("users") ?: JSONArray()
                        val list = mutableListOf<RelationshipsDO>()
                        for (i in 0 until usersArray.length()) {
                            val obj = usersArray.getJSONObject(i)
                            val model = RelationshipsDO().apply {
                                id = obj.optString("id")
                                name = obj.optString("name")
                                type = "corporate_tm"
                            }
                            list.add(model)
                        }
                        _uiState.update { it.copy(corporateTeamMembers = list) }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun generateMeetingLink() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.generateMeetingLink()
            _uiState.update { it.copy(isLoading = false) }

            if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val json = JSONObject(result.responseContent ?: "")
                    val roomId = json.optString("roomId", "")
                    if (roomId.isNotEmpty()) {
                        _uiState.update { it.copy(meetingRoomId = roomId) }
                    } else {
                        val fallbackRoomId = java.util.UUID.randomUUID().toString().replace("-", "").take(16)
                        _uiState.update { it.copy(meetingRoomId = fallbackRoomId) }
                    }
                } catch (e: Exception) {
                    val fallbackRoomId = java.util.UUID.randomUUID().toString().replace("-", "").take(16)
                    _uiState.update { it.copy(meetingRoomId = fallbackRoomId) }
                }
            } else {
                val fallbackRoomId = java.util.UUID.randomUUID().toString().replace("-", "").take(16)
                _uiState.update { it.copy(meetingRoomId = fallbackRoomId) }
            }
        }
    }

    private fun createEvent(payload: JSONObject) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.createEvent(payload)
            _uiState.update { it.copy(isLoading = false) }

            if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val json = JSONObject(result.responseContent ?: "")
                    if (json.has("errors")) {
                        val errors = json.getJSONArray("errors")
                        val firstError = errors.getJSONObject(0)
                        val msg = firstError.optString("msg", "Validation error")
                        _uiState.update { it.copy(alertTitle = "Error", alertMessage = msg) }
                    } else if (json.optBoolean("error", false)) {
                        _uiState.update { it.copy(alertTitle = "Error", alertMessage = json.optString("msg", "Failed to create event")) }
                    } else {
                        val msg = json.optString("msg", "Event created successfully")
                        _uiState.update {
                            it.copy(
                                toastMessage = msg,
                                isCreateMode = false,
                                editingEvent = null
                            )
                        }
                        loadData()
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(toastMessage = "Event created successfully", isCreateMode = false) }
                    loadData()
                }
            } else {
                _uiState.update { it.copy(alertTitle = "Error", alertMessage = "Failed to create event. Please try again.") }
            }
        }
    }

    private fun updateEvent(
        eventId: String,
        payload: JSONObject,
        recurringChoice: String?,
        updateScope: String?
    ) {
        val timezoneOffset = getTimezoneOffset()
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            if (!recurringChoice.isNullOrEmpty()) {
                payload.put("recurrent_edit_choice", recurringChoice)
            }
            if (!updateScope.isNullOrEmpty()) {
                payload.put("event_update_scope", updateScope)
            }
            val result = repository.updateEvent(eventId, timezoneOffset, payload)
            _uiState.update { it.copy(isLoading = false) }

            if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val json = JSONObject(result.responseContent ?: "")
                    if (json.has("errors")) {
                        val errors = json.getJSONArray("errors")
                        val firstError = errors.getJSONObject(0)
                        val msg = firstError.optString("msg", "Validation error")
                        _uiState.update { it.copy(alertTitle = "Error", alertMessage = msg) }
                    } else if (json.optBoolean("error", false)) {
                        _uiState.update { it.copy(alertTitle = "Error", alertMessage = json.optString("msg", "Failed to update event")) }
                    } else {
                        val msg = json.optString("msg", "Event updated successfully")
                        _uiState.update {
                            it.copy(
                                toastMessage = msg,
                                isCreateMode = false,
                                editingEvent = null
                            )
                        }
                        loadData()
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(toastMessage = "Event updated successfully", isCreateMode = false) }
                    loadData()
                }
            } else {
                _uiState.update { it.copy(alertTitle = "Error", alertMessage = "Failed to update event. Please try again.") }
            }
        }
    }

    private fun deleteEvent(
        eventId: String,
        recurringChoice: String?,
        eventDeleteScope: String?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = repository.deleteEvent(eventId, recurringChoice, eventDeleteScope)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    showRecurrenceChoiceDialog = false,
                    showTimesheetDeleteConfirm = false,
                    pendingRecurrenceEvent = null
                )
            }

            if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val json = JSONObject(result.responseContent ?: "")
                    val msg = json.optString("msg", "Event deleted successfully")
                    _uiState.update { it.copy(toastMessage = msg) }
                } catch (e: Exception) {
                    _uiState.update { it.copy(toastMessage = "Event deleted successfully") }
                }
                loadData()
            } else {
                _uiState.update { it.copy(alertTitle = "Error", alertMessage = "Failed to delete event. Please try again.") }
            }
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
                loadData()
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
                loadData()
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
                loadData()
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
                    if (!json.optBoolean("error", false)) {
                        val eventDetailsObj = json.optJSONObject("event") ?: JSONObject()
                        val details = parseEventDetails(eventDetailsObj)
                        val updatedWeekly = _uiState.value.weeklyEvents.map { if (it.id == eventId) details else it }
                        val updatedMonthly = _uiState.value.monthlyEvents.map { if (it.id == eventId) details else it }
                        _uiState.update {
                            it.copy(
                                weeklyEvents = updatedWeekly,
                                monthlyEvents = updatedMonthly,
                                selectedEventDetails = details
                            )
                        }
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
                offset = obj.optString("timezone_offset", "")
                offset_location = obj.optString("timezone_location", "")
                matter_id = obj.optString("matter_id", "")
                matter_name = obj.optString("matter_name", "")
                matter_type = obj.optString("matter_type", "")
                is_linked_with_timesheet = obj.optBoolean("is_linked_with_timesheet", false)
                timesheet_added = obj.optBoolean("timesheet_added", false)

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
                if (obj.has("notifications")) {
                    notifications = obj.optJSONArray("notifications") ?: JSONArray()
                }
                if (obj.has("attachments")) {
                    attachments = obj.optJSONArray("attachments") ?: JSONArray()
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
            offset = obj.optString("timezone_offset", "")
            offset_location = obj.optString("timezone_location", "")
            matter_id = obj.optString("matter_id", "")
            matter_name = obj.optString("matter_name", "")
            matter_type = obj.optString("matter_type", "")
            is_linked_with_timesheet = obj.optBoolean("is_linked_with_timesheet", false)
            timesheet_added = obj.optBoolean("timesheet_added", false)

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
            if (obj.has("notifications")) {
                notifications = obj.optJSONArray("notifications") ?: JSONArray()
            }
            if (obj.has("attachments")) {
                attachments = obj.optJSONArray("attachments") ?: JSONArray()
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
