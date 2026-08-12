package com.digicoffer.lauditor.feature.meetings.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.scale
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.ui.inputs.AppDropdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventForm(
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeBlue = Color(0xFF004D87)
    val textGrey = Color(0xFF5A5A7A)
    val inputBorderColor = Color(0xFFCCCCCC)

    var eventType by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("Aug 03, 2026") }
    var startTime by remember { mutableStateOf("12:15") }
    var endTime by remember { mutableStateOf("12:45") }
    var duration by remember { mutableStateOf("Duration: 30 min") }
    var isAllDay by remember { mutableStateOf(false) }
    var timezone by remember { mutableStateOf("(GMT+05:30) India Standard Time -...") }
    var repetition by remember { mutableStateOf("None") }

    var meetingLink by remember { mutableStateOf("") }
    var dialinNumber by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var meetingAgenda by remember { mutableStateOf("") }

    // Dynamic notifications list
    var notificationsList by remember { mutableStateOf(listOf(Pair("Minutes", "10"))) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Main Form Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Event Type Dropdown
                Text(
                    text = "Event Type *",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = activeBlue,
                    fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                )
                AppDropdown(
                    options = listOf("Legal Matter", "General Matter", "Overhead", "Others", "Reminders"),
                    selectedOption = if (eventType.isEmpty()) null else eventType,
                    onOptionSelected = { eventType = it },
                    label = "Select Event Type",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Date field
                Text(
                    text = "Date *",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = activeBlue,
                    fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = selectedDate,
                    onValueChange = { selectedDate = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = activeBlue,
                        unfocusedBorderColor = inputBorderColor
                    ),
                    shape = RoundedCornerShape(4.dp),
                    trailingIcon = {
                        Image(
                            painter = painterResource(id = R.drawable.ic_time_blue), // calendar icon placeholder
                            contentDescription = "Select Date",
                            modifier = Modifier.size(20.dp),
                            colorFilter = ColorFilter.tint(activeBlue)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Time fields (Start / End)
                Text(
                    text = "Time *",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = activeBlue,
                    fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = activeBlue,
                            unfocusedBorderColor = inputBorderColor
                        ),
                        shape = RoundedCornerShape(4.dp),
                        trailingIcon = {
                            Image(
                                painter = painterResource(id = R.drawable.ic_time_blue),
                                contentDescription = "Start Time",
                                modifier = Modifier.size(20.dp),
                                colorFilter = ColorFilter.tint(activeBlue)
                            )
                        }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = activeBlue,
                            unfocusedBorderColor = inputBorderColor
                        ),
                        shape = RoundedCornerShape(4.dp),
                        trailingIcon = {
                            Image(
                                painter = painterResource(id = R.drawable.ic_time_blue),
                                contentDescription = "End Time",
                                modifier = Modifier.size(20.dp),
                                colorFilter = ColorFilter.tint(activeBlue)
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Duration & All Day checkbox
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = duration,
                        color = activeBlue,
                        fontSize = 13.sp,
                        fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "All Day",
                            color = activeBlue,
                            fontSize = 13.sp,
                            fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Checkbox(
                            checked = isAllDay,
                            onCheckedChange = { isAllDay = it },
                            colors = CheckboxDefaults.colors(checkedColor = activeBlue)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Time Zone
                Text(
                    text = "Time Zone *",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = activeBlue,
                    fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                )
                AppDropdown(
                    options = listOf("(GMT+05:30) India Standard Time", "(GMT-05:00) Eastern Standard Time", "(GMT+00:00) Greenwich Mean Time"),
                    selectedOption = if (timezone.isEmpty()) null else timezone,
                    onOptionSelected = { timezone = it },
                    label = "Select Time Zone",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Repetition
                Text(
                    text = "Repetition",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = activeBlue,
                    fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                )
                AppDropdown(
                    options = listOf("None", "Daily", "Weekly", "Monthly", "Yearly"),
                    selectedOption = if (repetition.isEmpty()) null else repetition,
                    onOptionSelected = { repetition = it },
                    label = "Select Repetition",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Meeting Details Form Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Section Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.videocall_icon),
                        contentDescription = "Meeting details icon",
                        modifier = Modifier.size(20.dp),
                        colorFilter = ColorFilter.tint(activeBlue)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Meeting Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = activeBlue,
                        fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Meeting Link
                Text(
                    text = "Meeting Link",
                    fontSize = 13.sp,
                    color = activeBlue,
                    fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = meetingLink,
                    onValueChange = { meetingLink = it },
                    placeholder = { Text("Meeting Link", fontFamily = FontFamily(Font(R.font.gill_sans_regular))) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = activeBlue,
                        unfocusedBorderColor = inputBorderColor
                    ),
                    shape = RoundedCornerShape(4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Dial-in Number
                Text(
                    text = "Dial-in Number",
                    fontSize = 13.sp,
                    color = activeBlue,
                    fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = dialinNumber,
                    onValueChange = { dialinNumber = it },
                    placeholder = { Text("Dial-in Number", fontFamily = FontFamily(Font(R.font.gill_sans_regular))) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = activeBlue,
                        unfocusedBorderColor = inputBorderColor
                    ),
                    shape = RoundedCornerShape(4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Location
                Text(
                    text = "Location",
                    fontSize = 13.sp,
                    color = activeBlue,
                    fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    placeholder = { Text("Location", fontFamily = FontFamily(Font(R.font.gill_sans_regular))) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = activeBlue,
                        unfocusedBorderColor = inputBorderColor
                    ),
                    shape = RoundedCornerShape(4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Meeting Agenda
                Text(
                    text = "Meeting Agenda",
                    fontSize = 13.sp,
                    color = activeBlue,
                    fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = meetingAgenda,
                    onValueChange = { meetingAgenda = it },
                    placeholder = { Text("Meeting Agenda", fontFamily = FontFamily(Font(R.font.gill_sans_regular))) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = activeBlue,
                        unfocusedBorderColor = inputBorderColor
                    ),
                    shape = RoundedCornerShape(4.dp),
                    maxLines = 4
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Notify Me Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Section Title + Add button row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.notifications),
                            contentDescription = "Notify icon",
                            modifier = Modifier.size(20.dp),
                            colorFilter = ColorFilter.tint(activeBlue)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Notify Me",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = activeBlue,
                            fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                        )
                    }

                    Button(
                        onClick = {
                            notificationsList = notificationsList + Pair("Minutes", "10")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = activeBlue),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "+ Add Notification",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Notifications rows list
                notificationsList.forEachIndexed { index, notifyRow ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppDropdown(
                            options = listOf("Minutes", "Hours", "Days"),
                            selectedOption = notifyRow.first,
                            onOptionSelected = { selectedUnit ->
                                notificationsList = notificationsList.mapIndexed { idx, pair ->
                                    if (idx == index) Pair(selectedUnit, pair.second) else pair
                                }
                            },
                            label = "Select Unit",
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Number/value input
                        OutlinedTextField(
                            value = notifyRow.second,
                            onValueChange = { newVal ->
                                notificationsList = notificationsList.mapIndexed { idx, pair ->
                                    if (idx == index) Pair(pair.first, newVal) else pair
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(4.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = inputBorderColor
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Delete button
                        IconButton(
                            onClick = {
                                notificationsList = notificationsList.filterIndexed { idx, _ -> idx != index }
                            }
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.delete_de),
                                contentDescription = "Delete notification",
                                modifier = Modifier.size(20.dp),
                                colorFilter = ColorFilter.tint(Color.Red)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Save & Cancel action buttons at bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = onCancelClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp)
            ) {
                Text(
                    text = "Cancel",
                    color = Color.Black,
                    fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                )
            }

            Button(
                onClick = onSaveClick,
                colors = ButtonDefaults.buttonColors(containerColor = activeBlue),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp)
            ) {
                Text(
                    text = "Save",
                    color = Color.White,
                    fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                )
            }
        }
    }
}
