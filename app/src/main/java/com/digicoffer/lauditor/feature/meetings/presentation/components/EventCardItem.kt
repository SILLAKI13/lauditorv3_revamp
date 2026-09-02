package com.digicoffer.lauditor.feature.meetings.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.Appointments.Models.AppointmentModel
import com.digicoffer.lauditor.Meetings.Models.Event_Details_DO
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.ui.common.badges.AppStatusBadge
import com.digicoffer.lauditor.core.ui.common.badges.AppStatusStyle
import java.util.Locale
import org.json.JSONArray

private fun getEventUserRsvp(event: Event_Details_DO?): String {
    if (event == null) return ""
    val userName = com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.NAME
    val lists = listOf(event.team_name, event.tm_name, event.corporate, event.consumer_external)
    for (jsonArray in lists) {
        if (jsonArray != null) {
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.optJSONObject(i) ?: continue
                val tmName = obj.optString("tmName", "")
                val name = obj.optString("name", "")
                if (tmName == userName || name == userName) {
                    return obj.optString("rsvp", "")
                }
            }
        }
    }
    return ""
}

@Composable
fun EventCardItem(
    event: Event_Details_DO?,
    appointment: AppointmentModel?,
    onRsvpClick: (String) -> Unit,
    onCancelClick: () -> Unit,
    onChatClick: () -> Unit,
    onVideoCallClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onMeetingLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    val activeBlue = Color(0xFF004D87)
    val textGrey = Color(0xFF5A5A7A)
    val dividerColor = Color(0xFFCCCCDD)

    val isAppointment = appointment != null
    val titleText = if (isAppointment) appointment?.client_name.orEmpty() else event?.title.orEmpty()
    val dateText = if (isAppointment) {
        val dateObj = com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.stringToDateTimeDefault(
            appointment?.appointment_from, "yyyy-MM-dd'T'HH:mm:ss"
        )
        com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.getDateToString(dateObj, "dd MMMM yyyy").orEmpty()
    } else {
        val dateObj = com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.stringToDateTimeDefault(
            event?.from_ts, "yyyy-MM-dd'T'HH:mm:ss"
        )
        com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.getDateToString(dateObj, "dd MMMM yyyy").orEmpty()
    }

    val timeRangeText = if (isAppointment) {
        val fromDate = com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.stringToDateTimeDefault(
            appointment?.appointment_from, "yyyy-MM-dd'T'HH:mm:ss"
        )
        val toDate = com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.stringToDateTimeDefault(
            appointment?.appointment_to, "yyyy-MM-dd'T'HH:mm:ss"
        )
        val fromTime = com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.getDateToString(fromDate, "hh:mm a").orEmpty()
        val toTime = com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.getDateToString(toDate, "hh:mm a").orEmpty()
        "$fromTime - $toTime"
    } else {
        if (event?.all_day == true) {
            "All Day"
        } else {
            val fromDate = com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.stringToDateTimeDefault(
                event?.from_ts, "yyyy-MM-dd'T'HH:mm:ss"
            )
            val toDate = com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.stringToDateTimeDefault(
                event?.to_ts, "yyyy-MM-dd'T'HH:mm:ss"
            )
            val fromTime = com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.getDateToString(fromDate, "hh:mm a").orEmpty()
            val toTime = com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.getDateToString(toDate, "hh:mm a").orEmpty()
            "$fromTime - $toTime"
        }
    }

    val timezoneText = if (isAppointment) "Asia/Kolkata" else event?.timezone_location.orEmpty()
    val isOwner = if (isAppointment) false else event?.owner == true
    val statusText = if (isAppointment) appointment?.appointment_status.orEmpty() else ""

    val leftStripColor = if (isAppointment) {
        // Orange for appointments
        Color(0xFFF57C00)
    } else {
        // Blue for events
        Color(0xFF004D87)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left color strip
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(leftStripColor)
                    .align(Alignment.CenterVertically)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {
                // Header (date + 3-dot action spinner / RSVP button layout)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dateText,
                        color = activeBlue,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                    )

                    if (isOwner) {
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_17),
                                    contentDescription = "Options",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Edit",
                                            fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                                            color = Color.Black
                                        )
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        onEditClick()
                                    }
                                )
                                Divider(color = Color.LightGray, thickness = 0.5.dp)
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Delete",
                                            fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                                            color = Color.Black
                                        )
                                    },
                                    onClick = {
                                        menuExpanded = false
                                        onDeleteClick()
                                    }
                                )
                            }
                        }
                    } else if (isAppointment && appointment?.rsvp_status?.lowercase() != "accepted" && appointment?.rsvp_status?.lowercase() != "declined" && appointment?.appointment_status?.lowercase() != "cancelled" && appointment?.appointment_status?.lowercase() != "completed") {
                        // Show Yes/No RSVP buttons for appointments
                        Row(
                            modifier = Modifier
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(20.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Yes",
                                color = Color.Black,
                                fontSize = 12.sp,
                                fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                                modifier = Modifier
                                    .clickable { onRsvpClick("Yes") }
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                            Text(
                                text = "No",
                                color = Color.Black,
                                fontSize = 12.sp,
                                fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                                modifier = Modifier
                                    .clickable { onCancelClick() } // Cancel triggers decline flow
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    } else if (!isAppointment && getEventUserRsvp(event) != "Yes" && getEventUserRsvp(event) != "No" && getEventUserRsvp(event) != "Maybe") {
                        // Event RSVP Yes/No/Maybe
                        Row(
                            modifier = Modifier
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(20.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Yes",
                                color = Color.Black,
                                fontSize = 12.sp,
                                fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                                modifier = Modifier
                                    .clickable { onRsvpClick("Yes") }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                            Text(
                                text = "No",
                                color = Color.Black,
                                fontSize = 12.sp,
                                fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                                modifier = Modifier
                                    .clickable { onRsvpClick("No") }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                            Text(
                                text = "Maybe",
                                color = Color.Black,
                                fontSize = 12.sp,
                                fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                                modifier = Modifier
                                    .clickable { onRsvpClick("Maybe") }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Title
                Text(
                    text = titleText,
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Time Range Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_time_blue),
                        contentDescription = "Time",
                        modifier = Modifier.size(14.dp),
                        colorFilter = ColorFilter.tint(activeBlue)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = timeRangeText,
                        color = textGrey,
                        fontSize = 13.sp,
                        fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Timezone Row
                Text(
                    text = timezoneText,
                    color = textGrey,
                    fontSize = 12.sp,
                    fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                )

                // Expanded Info
                if (isExpanded) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = dividerColor, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (!isAppointment && event != null) {
                        // Description
                        val desc = event.description
                        if (!desc.isNullOrEmpty()) {
                            Text(
                                text = "Description",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.Black,
                                fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                            )
                            Text(
                                text = desc,
                                fontSize = 13.sp,
                                color = textGrey,
                                fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Join Meeting link
                        val link = event.meeting_link
                        if (!link.isNullOrEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { onMeetingLinkClick(link) }
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.videocall_icon),
                                    contentDescription = "Join Meeting",
                                    modifier = Modifier.size(16.dp),
                                    colorFilter = ColorFilter.tint(activeBlue)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Join Meeting",
                                    color = activeBlue,
                                    fontSize = 13.sp,
                                    textDecoration = TextDecoration.Underline,
                                    fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Dial-in
                        if (!event.dialin.isNullOrEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.notify_bell_1),
                                    contentDescription = "Dial-in",
                                    modifier = Modifier.size(16.dp),
                                    colorFilter = ColorFilter.tint(activeBlue)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Dial-in Number: ${event.dialin}",
                                    color = textGrey,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Location
                        if (!event.location.isNullOrEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.notify_bell_1),
                                    contentDescription = "Location",
                                    modifier = Modifier.size(16.dp),
                                    colorFilter = ColorFilter.tint(activeBlue)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Location: ${event.location}",
                                    color = textGrey,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action Bar (Expand toggle + Appointment options)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isExpanded) "View Less" else "View More",
                        color = activeBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                        modifier = Modifier.clickable { isExpanded = !isExpanded }
                    )

                    if (isAppointment && appointment != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val cleanStatus = statusText.lowercase(Locale.ROOT).trim()
                            val (displayStatus, statusStyle) = when (cleanStatus) {
                                "completed" -> Pair("Completed", AppStatusStyle.SUCCESS)
                                "cancelled", "canceled" -> Pair("Cancelled", AppStatusStyle.ERROR)
                                "upcoming" -> Pair("Upcoming", AppStatusStyle.INFO)
                                "ongoing" -> Pair("Ongoing", AppStatusStyle.INFO)
                                "payment_pending", "pending" -> Pair("Payment Pending", AppStatusStyle.WARNING)
                                else -> Pair(if (cleanStatus.isNotEmpty()) statusText.replaceFirstChar { it.uppercase() } else "Scheduled", AppStatusStyle.NEUTRAL)
                            }
                            AppStatusBadge(
                                text = displayStatus,
                                style = statusStyle
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            val showChat = com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.isWithinOneHour(appointment.appointment_from) || statusText.lowercase() == "completed"
                            val showVideo = com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.isWithinOneMinute(appointment.appointment_from)

                            if (showChat) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .background(Color(0xFFE4F2FF), CircleShape)
                                        .clickable { onChatClick() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.message_icon),
                                        contentDescription = "Chat",
                                        modifier = Modifier.size(16.dp),
                                        colorFilter = ColorFilter.tint(activeBlue)
                                    )
                                }
                            }

                            if (showVideo) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .background(Color(0xFFE8F5E9), CircleShape)
                                        .clickable { onVideoCallClick() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.videocall_icon),
                                        contentDescription = "Video Call",
                                        modifier = Modifier.size(16.dp),
                                        colorFilter = ColorFilter.tint(Color(0xFF2E7D32))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
