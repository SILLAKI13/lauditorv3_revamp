package com.digicoffer.lauditor.feature.meetings.presentation.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.draw.scale
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.feature.meetings.presentation.components.CreateEventForm
import com.digicoffer.lauditor.feature.meetings.presentation.components.EventCardItem
import com.digicoffer.lauditor.feature.meetings.presentation.components.RecurrenceChoiceDialog
import com.digicoffer.lauditor.feature.meetings.presentation.components.WeekCalendarCard
import com.digicoffer.lauditor.feature.meetings.presentation.state.MeetingsUiEvent
import com.digicoffer.lauditor.feature.meetings.presentation.state.MeetingsUiState
import com.digicoffer.lauditor.feature.meetings.presentation.viewmodel.MeetingsViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingsScreen(
    viewModel: MeetingsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var isCreateMode by remember { mutableStateOf(false) }
    var showRecurrenceDialog by remember { mutableStateOf(false) }

    val activeBlue = Color(0xFF004D87)
    val lightBlueBg = Color(0xFFE4F2FF)

    // Calendar states
    var selectedWeekDayIndex by remember { mutableStateOf(1) } // Default Tuesday

    val weekDays = listOf(
        Pair("02", 1),
        Pair("03", 0),
        Pair("04", 0),
        Pair("05", 0),
        Pair("06", 0),
        Pair("07", 0),
        Pair("08", 0)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(lightBlueBg)
    ) {
        if (isCreateMode) {
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
                        text = "Create Event",
                        color = activeBlue,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                    )
                    Button(
                        onClick = { isCreateMode = false },
                        colors = ButtonDefaults.buttonColors(containerColor = activeBlue),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.eye_open),
                            contentDescription = "View Events",
                            modifier = Modifier.size(16.dp),
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "View Event",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                        )
                    }
                }

                CreateEventForm(
                    onCancelClick = { isCreateMode = false },
                    onSaveClick = {
                        isCreateMode = false
                        Toast.makeText(context, "Event created successfully", Toast.LENGTH_SHORT).show()
                    }
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
                    Button(
                        onClick = { isCreateMode = true },
                        colors = ButtonDefaults.buttonColors(containerColor = activeBlue),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "+ Create Event",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                        )
                    }
                }

                // Dropdown Filter Selection Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(Color.LightGray, RoundedCornerShape(6.dp))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "All Appointments",
                                fontSize = 14.sp,
                                fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                                color = Color.Black
                            )
                        }
                        Image(
                            painter = painterResource(id = R.drawable.back_arrow), // arrow icon placeholder
                            contentDescription = "Dropdown filter",
                            modifier = Modifier
                                .size(14.dp)
                                .scale(1f, -1f)
                        )
                    }
                }

                // Calendar View
                if (!uiState.isMonthView) {
                    WeekCalendarCard(
                        dateRangeText = "Aug 02, 2026   Aug 08, 2026",
                        days = weekDays,
                        selectedDayIndex = selectedWeekDayIndex,
                        todayDayIndex = 1,
                        onPreviousWeekClick = {},
                        onNextWeekClick = {},
                        onDayClick = { idx -> selectedWeekDayIndex = idx }
                    )
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                val root = android.view.LayoutInflater.from(ctx).inflate(
                                    R.layout.month_view_calendar, null, false
                                )
                                val prolific = root.findViewById<com.applandeo.materialcalendarview.CalendarView>(
                                    R.id.prolificcalendarview
                                )
                                prolific.setDate(Calendar.getInstance())
                                // Remove view parent before returning to prevent illegal state in AndroidView
                                (prolific.parent as? android.view.ViewGroup)?.removeView(prolific)
                                prolific
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .padding(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Events Card List or Empty State
                val hasEvents = uiState.weeklyEvents.isNotEmpty() || uiState.weeklyAppointments.isNotEmpty()
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
                        items(uiState.weeklyAppointments) { appt ->
                            EventCardItem(
                                event = null,
                                appointment = appt,
                                onRsvpClick = { rsvp ->
                                    viewModel.onEvent(MeetingsUiEvent.AppointmentRsvpChanged(appt.id, rsvp))
                                },
                                onCancelClick = {
                                    viewModel.onEvent(MeetingsUiEvent.CancelAppointment(appt.id))
                                },
                                onChatClick = {},
                                onVideoCallClick = {},
                                onEditClick = {},
                                onDeleteClick = {},
                                onMeetingLinkClick = {}
                            )
                        }

                        // Events
                        items(uiState.weeklyEvents) { evt ->
                            EventCardItem(
                                event = evt,
                                appointment = null,
                                onRsvpClick = { rsvp ->
                                    viewModel.onEvent(MeetingsUiEvent.EventRsvpChanged(evt.id.orEmpty(), rsvp))
                                },
                                onCancelClick = {},
                                onChatClick = {},
                                onVideoCallClick = {},
                                onEditClick = {
                                    showRecurrenceDialog = true
                                },
                                onDeleteClick = {
                                    showRecurrenceDialog = true
                                },
                                onMeetingLinkClick = {}
                            )
                        }
                    }
                }
            }
        }
    }

    if (showRecurrenceDialog) {
        RecurrenceChoiceDialog(
            onDismiss = { showRecurrenceDialog = false },
            onConfirm = { choice ->
                showRecurrenceDialog = false
                Toast.makeText(context, "Selection: $choice", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
