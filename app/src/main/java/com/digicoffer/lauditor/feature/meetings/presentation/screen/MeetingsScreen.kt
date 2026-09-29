package com.digicoffer.lauditor.feature.meetings.presentation.screen

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.Appointments.Models.AppointmentModel
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.Meetings.Models.Event_Details_DO
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.ui.common.buttons.AppHeaderButton
import com.digicoffer.lauditor.core.ui.common.dialogs.AppConfirmationDialog
import com.digicoffer.lauditor.core.ui.common.dialogs.AppDialog
import com.digicoffer.lauditor.core.ui.common.dropdowns.AppDropdown
import com.digicoffer.lauditor.core.ui.common.feedback.AppLoader
import com.digicoffer.lauditor.feature.meetings.presentation.components.CreateEventForm
import com.digicoffer.lauditor.feature.meetings.presentation.components.EventCardItem
import com.digicoffer.lauditor.feature.meetings.presentation.components.MonthCalendarCard
import com.digicoffer.lauditor.feature.meetings.presentation.components.RecurrenceChoiceDialog
import com.digicoffer.lauditor.feature.meetings.presentation.components.WeekCalendarCard
import com.digicoffer.lauditor.feature.meetings.presentation.state.MeetingsUiEvent
import com.digicoffer.lauditor.feature.meetings.presentation.state.MeetingsUiState
import com.digicoffer.lauditor.feature.meetings.presentation.viewmodel.MeetingsViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private fun isSameDate(dateStr: String?, targetDateDdmmyyyy: String): Boolean {
    if (dateStr.isNullOrEmpty() || targetDateDdmmyyyy.isEmpty()) return false
    try {
        if (dateStr.contains("T")) {
            val parsed = AndroidUtils.stringToDateTimeDefault(dateStr, "yyyy-MM-dd'T'HH:mm:ss")
            val formatted = AndroidUtils.getDateToString(parsed, "dd-MM-yyyy")
            return formatted == targetDateDdmmyyyy
        } else if (dateStr.contains("-")) {
            val parts = dateStr.split("-")
            if (parts.size == 3 && parts[0].length == 4) { // yyyy-MM-dd
                val parsed = AndroidUtils.stringToDateTimeDefault(dateStr, "yyyy-MM-dd")
                val formatted = AndroidUtils.getDateToString(parsed, "dd-MM-yyyy")
                return formatted == targetDateDdmmyyyy
            }
            return dateStr == targetDateDdmmyyyy
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return false
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingsScreen(
    viewModel: MeetingsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val activeBlue = Color(0xFF004D87)
    val lightBlueBg = Color(0xFFE4F2FF)

    // Filter dropdown expanded state
    var filterDropdownExpanded by remember { mutableStateOf(false) }
    var expandedCardId by remember { mutableStateOf<String?>(null) }

    // Toast and Alert handling
    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.onEvent(MeetingsUiEvent.DismissDialogs)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(lightBlueBg)
    ) {
        if (uiState.isLoading) {
            AppLoader()
        }

        if (uiState.isCreateMode) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (uiState.editingEvent != null) "Edit Event" else "Create Event",
                        color = activeBlue,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                    )
                    AppHeaderButton(
                        text = "View Event",
                        iconRes = R.drawable.eye_icon,
                        onClick = { viewModel.onEvent(MeetingsUiEvent.CloseForm) }
                    )
                }

                CreateEventForm(
                    uiState = uiState,
                    viewModel = viewModel,
                    onCancelClick = { viewModel.onEvent(MeetingsUiEvent.CloseForm) }
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Main Header Row (Segmented Toggles + Create Event Button)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Segmented Days / Month View Toggle
                    Row(
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    if (!uiState.isMonthView) activeBlue else Color.White,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable {
                                    viewModel.onEvent(MeetingsUiEvent.SwitchViewMode(isMonthView = false))
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Days",
                                color = if (!uiState.isMonthView) Color.White else Color.Black,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(
                                    if (uiState.isMonthView) activeBlue else Color.White,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable {
                                    viewModel.onEvent(MeetingsUiEvent.SwitchViewMode(isMonthView = true))
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Month",
                                color = if (uiState.isMonthView) Color.White else Color.Black,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                            )
                        }
                    }

                    // Create Event button
                    AppHeaderButton(
                        text = "Create Event",
                        iconRes = null,
                        onClick = {
                            if (!Constants.is_active) {
                                AndroidUtils.showRenewalPopup(context as? android.app.Activity ?: return@AppHeaderButton)
                            } else {
                                viewModel.onEvent(MeetingsUiEvent.OpenCreateEvent)
                            }
                        }
                    )
                }

                // Filter Dropdown Section (Top-Right aligned)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier.wrapContentSize(Alignment.TopEnd)
                    ) {
                        val filterColor = when (uiState.selectedFilter) {
                            "My Meetings" -> activeBlue
                            "Client Bookings" -> Color(0xFFE59E35)
                            else -> Color(0xFFD9D9D9)
                        }
                        val filterLabel = when (uiState.selectedFilter) {
                            "My Meetings" -> "My Meetings"
                            "Client Bookings" -> "Client Bookings"
                            else -> "All Appointments"
                        }

                        Card(
                            modifier = Modifier
                                .width(220.dp)
                                .clickable { filterDropdownExpanded = !filterDropdownExpanded },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(filterColor, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = filterLabel,
                                        fontSize = 14.sp,
                                        fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                                        color = Color.Black
                                    )
                                }
                                Image(
                                    painter = painterResource(id = if (filterDropdownExpanded) R.drawable.up_arrow else R.drawable.down_arrow),
                                    contentDescription = "Dropdown filter",
                                    modifier = Modifier.size(16.dp),
                                    colorFilter = ColorFilter.tint(activeBlue)
                                )
                            }
                        }

                        // Floating Dropdown Card with Color Dots
                        if (filterDropdownExpanded) {
                            androidx.compose.ui.window.Popup(
                                alignment = Alignment.TopEnd,
                                offset = androidx.compose.ui.unit.IntOffset(0, 130),
                                onDismissRequest = { filterDropdownExpanded = false },
                                properties = androidx.compose.ui.window.PopupProperties(focusable = true)
                            ) {
                                Card(
                                    modifier = Modifier
                                        .width(220.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        // 1. My Meetings
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    filterDropdownExpanded = false
                                                    viewModel.onEvent(MeetingsUiEvent.FilterChanged("My Meetings"))
                                                }
                                                .padding(horizontal = 14.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .background(activeBlue, CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "My Meetings",
                                                fontSize = 14.sp,
                                                fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                                                color = Color.Black
                                            )
                                        }

                                        // 2. Client Bookings
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    filterDropdownExpanded = false
                                                    viewModel.onEvent(MeetingsUiEvent.FilterChanged("Client Bookings"))
                                                }
                                                .padding(horizontal = 14.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .background(Color(0xFFE59E35), CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "Client Bookings",
                                                fontSize = 14.sp,
                                                fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                                                color = Color.Black
                                            )
                                        }

                                        // 3. All Appointments
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    filterDropdownExpanded = false
                                                    viewModel.onEvent(MeetingsUiEvent.FilterChanged("All"))
                                                }
                                                .padding(horizontal = 14.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .background(Color(0xFFD9D9D9), CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "All Appointments",
                                                fontSize = 14.sp,
                                                fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                                                color = Color.Black
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Calendar Card (Days / Month)
                if (!uiState.isMonthView) {
                    val daysPairs = uiState.weekDays.map { day ->
                        val dayNum = try {
                            val parts = day.date.split("-")
                            if (parts.isNotEmpty()) parts[0] else "01"
                        } catch (e: Exception) {
                            "01"
                        }
                        val eventsOnDay = uiState.weeklyEvents.count { isSameDate(it.from_ts, day.date) }
                        val apptsOnDay = uiState.weeklyAppointments.count { isSameDate(it.appointment_from, day.date) }
                        Pair(dayNum, eventsOnDay + apptsOnDay)
                    }

                    WeekCalendarCard(
                        dateRangeText = uiState.weekDateRangeText,
                        days = daysPairs,
                        selectedDayIndex = uiState.selectedDayIndex,
                        todayDayIndex = uiState.todayDayIndex,
                        onPreviousWeekClick = { viewModel.onEvent(MeetingsUiEvent.PreviousWeek) },
                        onNextWeekClick = { viewModel.onEvent(MeetingsUiEvent.NextWeek) },
                        onDayClick = { idx -> viewModel.onEvent(MeetingsUiEvent.SelectDay(idx)) }
                    )
                } else {
                    MonthCalendarCard(
                        monthTitle = uiState.currentMonthTitle,
                        monthDays = uiState.monthDays,
                        onPreviousMonthClick = { viewModel.onEvent(MeetingsUiEvent.PreviousMonth) },
                        onNextMonthClick = { viewModel.onEvent(MeetingsUiEvent.NextMonth) },
                        onDayClick = { day -> viewModel.onEvent(MeetingsUiEvent.SelectMonthDate(day.dateStr)) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Filtered events & appointments for the selected date
                val targetDate = uiState.selectedDate
                val eventsSource = if (uiState.isMonthView) uiState.monthlyEvents else uiState.weeklyEvents
                val apptsSource = if (uiState.isMonthView) uiState.monthlyAppointments else uiState.weeklyAppointments

                val filteredEvents = if (uiState.selectedFilter == "Client Bookings") {
                    emptyList()
                } else {
                    eventsSource.filter { isSameDate(it.from_ts, targetDate) }
                }

                val filteredAppts = if (uiState.selectedFilter == "My Meetings") {
                    emptyList()
                } else {
                    apptsSource.filter { isSameDate(it.appointment_from, targetDate) }
                }

                val hasEvents = filteredEvents.isNotEmpty() || filteredAppts.isNotEmpty()

                if (!hasEvents) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.empty_appointments),
                            contentDescription = "No Events",
                            modifier = Modifier.size(130.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Events Yet!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "There are no events for this date.",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        // Appointments
                        items(filteredAppts, key = { "appt_${it.id}" }) { appt ->
                            val cardId = "appt_${appt.id}"
                            EventCardItem(
                                event = null,
                                appointment = appt,
                                isExpanded = expandedCardId == cardId,
                                onExpandToggle = {
                                    expandedCardId = if (expandedCardId == cardId) null else cardId
                                },
                                onRsvpClick = { rsvp ->
                                    viewModel.onEvent(MeetingsUiEvent.AppointmentRsvpChanged(appt.id, rsvp))
                                },
                                onCancelClick = {
                                    viewModel.onEvent(MeetingsUiEvent.CancelAppointment(appt.id))
                                },
                                onChatClick = {
                                    Toast.makeText(context, "Opening chat with ${appt.client_name}", Toast.LENGTH_SHORT).show()
                                },
                                onVideoCallClick = {
                                    if (appt.meeting_room_id.isNotEmpty()) {
                                        val url = AndroidUtils.getAVChatUrl(appt.meeting_room_id, "", "", "", Constants.NAME)
                                        AndroidUtils.openUrlInChrome(context, url)
                                    }
                                },
                                onEditClick = {},
                                onDeleteClick = {},
                                onMeetingLinkClick = { link ->
                                    AndroidUtils.openUrlInChrome(context, link)
                                }
                            )
                        }

                        // Events
                        items(filteredEvents, key = { "evt_${it.id.orEmpty()}" }) { evt ->
                            val cardId = "evt_${evt.id.orEmpty()}"
                            EventCardItem(
                                event = evt,
                                appointment = null,
                                isExpanded = expandedCardId == cardId,
                                onExpandToggle = {
                                    if (expandedCardId == cardId) {
                                        expandedCardId = null
                                    } else {
                                        expandedCardId = cardId
                                        viewModel.onEvent(MeetingsUiEvent.ShowEventDetails(evt.id.orEmpty()))
                                    }
                                },
                                onRsvpClick = { rsvp ->
                                    viewModel.onEvent(MeetingsUiEvent.EventRsvpChanged(evt.id.orEmpty(), rsvp))
                                },
                                onCancelClick = {},
                                onChatClick = {},
                                onVideoCallClick = {},
                                onEditClick = {
                                    viewModel.onEvent(MeetingsUiEvent.OpenEditEvent(evt))
                                },
                                onDeleteClick = {
                                    viewModel.onEvent(MeetingsUiEvent.RequestDeleteEvent(evt))
                                },
                                onMeetingLinkClick = { link ->
                                    AndroidUtils.openUrlInChrome(context, link)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Loading indicator overlay
        if (uiState.isLoading) {
            AppLoader()
        }
    }

    // Recurrence Delete Choice Dialog
    if (uiState.showRecurrenceChoiceDialog && uiState.pendingRecurrenceEvent != null) {
        val pendingEvt = uiState.pendingRecurrenceEvent!!
        RecurrenceChoiceDialog(
            onDismiss = { viewModel.onEvent(MeetingsUiEvent.DismissRecurrenceDialog) },
            onConfirm = { choice ->
                val mappedChoice = when (choice) {
                    "Only this event" -> "this"
                    "This and following events" -> "following"
                    else -> "all"
                }
                viewModel.onEvent(MeetingsUiEvent.DismissRecurrenceDialog)
                if (pendingEvt.is_linked_with_timesheet || pendingEvt.timesheet_added) {
                    viewModel.onEvent(
                        MeetingsUiEvent.ConfirmDeleteEvent(
                            eventId = pendingEvt.id.orEmpty(),
                            recurringChoice = mappedChoice,
                            eventDeleteScope = "DELETE_BOTH"
                        )
                    )
                } else {
                    viewModel.onEvent(
                        MeetingsUiEvent.ConfirmDeleteEvent(
                            eventId = pendingEvt.id.orEmpty(),
                            recurringChoice = mappedChoice,
                            eventDeleteScope = "DELETE_EVENT_ONLY"
                        )
                    )
                }
            }
        )
    }

    // Timesheet Delete Confirmation Dialog
    if (uiState.showTimesheetDeleteConfirm && uiState.pendingRecurrenceEvent != null) {
        val pendingEvt = uiState.pendingRecurrenceEvent!!
        AppConfirmationDialog(
            title = "Delete Timesheet Entry?",
            message = "This event has an associated timesheet entry. Do you want to update the timesheet too?",
            confirmText = "Delete Both",
            dismissText = "Delete Event Only",
            onConfirm = {
                viewModel.onEvent(MeetingsUiEvent.DismissTimesheetConfirm)
                viewModel.onEvent(
                    MeetingsUiEvent.ConfirmDeleteEvent(
                        eventId = pendingEvt.id.orEmpty(),
                        recurringChoice = null,
                        eventDeleteScope = "DELETE_BOTH"
                    )
                )
            },
            onDismiss = {
                viewModel.onEvent(MeetingsUiEvent.DismissTimesheetConfirm)
                viewModel.onEvent(
                    MeetingsUiEvent.ConfirmDeleteEvent(
                        eventId = pendingEvt.id.orEmpty(),
                        recurringChoice = null,
                        eventDeleteScope = "DELETE_EVENT_ONLY"
                    )
                )
            }
        )
    }

    // Generic Alert Dialog
    if (uiState.alertMessage != null) {
        AppDialog(
            title = uiState.alertTitle ?: "Notice",
            onConfirm = { viewModel.onEvent(MeetingsUiEvent.DismissDialogs) },
            onDismiss = { viewModel.onEvent(MeetingsUiEvent.DismissDialogs) },
            content = {
                Text(
                    text = uiState.alertMessage ?: "",
                    fontSize = 15.sp,
                    color = Color.Black,
                    fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                )
            }
        )
    }
}
