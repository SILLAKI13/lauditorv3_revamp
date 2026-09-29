package com.digicoffer.lauditor.feature.meetings.presentation.components

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.Appointments.Models.AppointmentModel
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.Meetings.Models.Event_Details_DO
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.ui.common.badges.AppStatusBadge
import com.digicoffer.lauditor.core.ui.common.badges.AppStatusStyle
import org.json.JSONArray
import java.util.Locale

private fun getInitials(name: String?): String {
    if (name.isNullOrBlank()) return "U"
    val parts = name.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
    return if (parts.size == 1) {
        parts[0].take(2).uppercase(Locale.US)
    } else {
        "${parts[0].take(1)}${parts[1].take(1)}".uppercase(Locale.US)
    }
}

private fun formatNotificationSummary(notifications: JSONArray?): String {
    if (notifications == null || notifications.length() == 0) return "10 Minutes before"
    val sb = StringBuilder()
    for (i in 0 until notifications.length()) {
        val raw = notifications.optString(i)
        if (raw.contains("-")) {
            val parts = raw.split("-")
            val amount = parts[0].trim()
            var unit = if (parts.size > 1) parts[1].trim() else ""
            if (unit.isNotEmpty()) {
                unit = unit.substring(0, 1).uppercase(Locale.US) + unit.substring(1)
            }
            if (i > 0) sb.append(", ")
            sb.append("$amount $unit before")
        } else if (raw.isNotEmpty()) {
            if (i > 0) sb.append(", ")
            sb.append("$raw before")
        }
    }
    return sb.toString()
}

private fun formatTimeRange(fromTs: String?, toTs: String?, allDay: Boolean): String {
    if (allDay) return "All Day"
    if (fromTs.isNullOrEmpty() || toTs.isNullOrEmpty()) return ""
    return try {
        val fromDate = AndroidUtils.stringToDateTimeDefault(fromTs, "yyyy-MM-dd'T'HH:mm:ss")
        val toDate = AndroidUtils.stringToDateTimeDefault(toTs, "yyyy-MM-dd'T'HH:mm:ss")
        val fromTime = AndroidUtils.getDateToString(fromDate, "hh:mma").orEmpty().uppercase(Locale.US)
        val toTime = AndroidUtils.getDateToString(toDate, "hh:mma").orEmpty().uppercase(Locale.US)
        "$fromTime - $toTime"
    } catch (e: Exception) {
        ""
    }
}

@Composable
fun EventCardItem(
    event: Event_Details_DO?,
    appointment: AppointmentModel?,
    isExpanded: Boolean = false,
    onExpandToggle: () -> Unit = {},
    onRsvpClick: (String) -> Unit,
    onCancelClick: () -> Unit,
    onChatClick: () -> Unit,
    onVideoCallClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onMeetingLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }

    val activeBlue = Color(0xFF004D87)
    val textGrey = Color(0xFF5A5A7A)

    val isAppointment = appointment != null
    val titleText = if (isAppointment) appointment?.client_name.orEmpty() else event?.title.orEmpty()

    val dateText = if (isAppointment) {
        val dateObj = AndroidUtils.stringToDateTimeDefault(
            appointment?.appointment_from, "yyyy-MM-dd'T'HH:mm:ss"
        )
        AndroidUtils.getDateToString(dateObj, "dd MMMM yyyy").orEmpty()
    } else {
        val dateObj = AndroidUtils.stringToDateTimeDefault(
            event?.from_ts, "yyyy-MM-dd'T'HH:mm:ss"
        )
        AndroidUtils.getDateToString(dateObj, "dd MMMM yyyy").orEmpty()
    }

    val timeRangeText = if (isAppointment) {
        formatTimeRange(appointment?.appointment_from, appointment?.appointment_to, false)
    } else {
        formatTimeRange(event?.from_ts, event?.to_ts, event?.all_day == true)
    }

    val repeatText: String = if (isAppointment) "" else {
        val rep = event?.repeat_interval
        if (rep.isNullOrBlank()) "None" else AndroidUtils.CapitalizeFirstLetter(rep).orEmpty()
    }

    val timezoneText = if (isAppointment) {
        "(GMT+05:30) India Standard Time - Kolkata"
    } else {
        val loc = event?.offset_location.orEmpty().ifEmpty { event?.timezone_location.orEmpty() }
        if (loc.isNotEmpty()) {
            if (loc.startsWith("(") || loc.contains("GMT")) loc else "(GMT+05:30) $loc"
        } else {
            "(GMT+05:30) India Standard Time - Kolkata"
        }
    }

    val isOwner = if (isAppointment) false else event?.owner == true
    val statusText = if (isAppointment) appointment?.appointment_status.orEmpty() else ""

    val leftStripColor = if (isAppointment) Color(0xFFF57C00) else Color(0xFF004D87)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Left blue / orange vertical status strip
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(leftStripColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp)
            ) {
                // Header (Date + 3-dot action menu or RSVP buttons)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dateText,
                        color = activeBlue,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                    )

                    if (isOwner) {
                        Box {
                            IconButton(
                                onClick = { menuExpanded = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_17),
                                    contentDescription = "Options",
                                    modifier = Modifier.size(18.dp)
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
                                HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
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
                        // Yes/No RSVP buttons for appointments
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
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                            Text(
                                text = "No",
                                color = Color.Black,
                                fontSize = 12.sp,
                                fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                                modifier = Modifier
                                    .clickable { onCancelClick() }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    } else if (!isAppointment) {
                        // RSVP Buttons for Event Invitees
                        Row(
                            modifier = Modifier
                                .background(Color(0xFFF5F5F5), RoundedCornerShape(20.dp))
                                .padding(horizontal = 2.dp, vertical = 2.dp)
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

                Spacer(modifier = Modifier.height(4.dp))

                // Title
                Text(
                    text = titleText,
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Time Range & Repetition Row
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeRangeText,
                        color = Color.Black,
                        fontSize = 13.sp,
                        fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                    )
                    if (repeatText.isNotEmpty()) {
                        Text(
                            text = " | ",
                            color = Color.Black,
                            fontSize = 13.sp,
                            fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                        )
                        Text(
                            text = repeatText,
                            color = Color.Black,
                            fontSize = 13.sp,
                            fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Timezone
                Text(
                    text = timezoneText,
                    color = textGrey,
                    fontSize = 13.sp,
                    fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                )

                // ═══════════════════════════════════════════════════
                // EXPANDED VIEW MORE SECTION
                // ═══════════════════════════════════════════════════
                if (isExpanded) {
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
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = desc,
                                fontSize = 13.sp,
                                color = textGrey,
                                fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Join Meeting Link
                        val nonNullLink = event.meeting_link.orEmpty()
                        if (nonNullLink.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { onMeetingLinkClick(nonNullLink) }
                                    .padding(vertical = 4.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.videocall_icon),
                                    contentDescription = "Join Meeting",
                                    modifier = Modifier.size(18.dp),
                                    colorFilter = ColorFilter.tint(activeBlue)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Join Meeting",
                                    color = activeBlue,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    textDecoration = TextDecoration.Underline,
                                    fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        // Dial-in
                        if (!event.dialin.isNullOrEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.notify_bell_1),
                                    contentDescription = "Dial-in",
                                    modifier = Modifier.size(16.dp),
                                    colorFilter = ColorFilter.tint(activeBlue)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Dial-in Number: ${event.dialin}",
                                    color = textGrey,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        // Location
                        if (!event.location.isNullOrEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.notify_bell_1),
                                    contentDescription = "Location",
                                    modifier = Modifier.size(16.dp),
                                    colorFilter = ColorFilter.tint(activeBlue)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Location: ${event.location}",
                                    color = textGrey,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        // Notifications (Always show if present or default)
                        val notifSummary = formatNotificationSummary(event.notifications)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.notify_bell_1),
                                contentDescription = "Notification",
                                modifier = Modifier.size(20.dp),
                                colorFilter = ColorFilter.tint(activeBlue)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = notifSummary,
                                    color = Color.Black,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                                )
                                Text(
                                    text = "Email Notification",
                                    color = Color(0xFFA0A0A0),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // Attendees / Team Members Section
                        val teamArray = event.team_name
                        val clientsArray = event.tm_name
                        val corpArray = event.corporate
                        val consumerArray = event.consumer_external
                        val ownerName: String = if (!event.owner_name.isNullOrBlank()) {
                            event.owner_name ?: ""
                        } else if (event.owner) {
                            Constants.NAME.orEmpty()
                        } else {
                            ""
                        }

                        // Header: Team icon + | Team Members
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.team_member_group),
                                contentDescription = "Team",
                                modifier = Modifier.size(20.dp),
                                colorFilter = ColorFilter.tint(activeBlue)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "| Team Members",
                                color = activeBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Organizer Row
                        if (ownerName.isNotBlank()) {
                            AttendeeRow(
                                name = "$ownerName (Organizer)",
                                isOrganizer = true,
                                rsvp = "yes"
                            )
                        }

                        // Team Members list
                        if (teamArray != null) {
                            for (i in 0 until teamArray.length()) {
                                val obj = teamArray.optJSONObject(i) ?: continue
                                val name = obj.optString("name", "")
                                val rsvp = obj.optString("rsvp", "")
                                if (name != ownerName) {
                                    AttendeeRow(name = name, isOrganizer = false, rsvp = rsvp)
                                }
                            }
                        }

                        // Clients list
                        if (clientsArray != null && clientsArray.length() > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "| Clients",
                                color = activeBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                            )
                            for (i in 0 until clientsArray.length()) {
                                val obj = clientsArray.optJSONObject(i) ?: continue
                                val name = obj.optString("tmName", obj.optString("name", ""))
                                val rsvp = obj.optString("rsvp", "")
                                AttendeeRow(name = name, isOrganizer = false, rsvp = rsvp)
                            }
                        }

                        // Consumer external list
                        if (consumerArray != null && consumerArray.length() > 0) {
                            for (i in 0 until consumerArray.length()) {
                                val obj = consumerArray.optJSONObject(i) ?: continue
                                val name = obj.optString("tmName", obj.optString("name", ""))
                                val rsvp = obj.optString("rsvp", "")
                                AttendeeRow(name = name, isOrganizer = false, rsvp = rsvp)
                            }
                        }

                        // Corporate Clients list
                        if (corpArray != null && corpArray.length() > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "| Corporate Clients",
                                color = activeBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                            )
                            for (i in 0 until corpArray.length()) {
                                val obj = corpArray.optJSONObject(i) ?: continue
                                val name = obj.optString("tmName", obj.optString("name", ""))
                                val rsvp = obj.optString("rsvp", "")
                                AttendeeRow(name = name, isOrganizer = false, rsvp = rsvp)
                            }
                        }

                        // Documents (Attachments)
                        val attachments = event.attachments
                        if (attachments != null && attachments.length() > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "| Documents",
                                color = activeBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            for (a in 0 until attachments.length()) {
                                val attObj = attachments.optJSONObject(a) ?: continue
                                val docName = attObj.optString("name", "Document")
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                        .background(Color(0xFFF5F5F5), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = docName,
                                        color = Color.Black,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Image(
                                        painter = painterResource(id = R.drawable.eye_open),
                                        contentDescription = "View Doc",
                                        modifier = Modifier.size(20.dp),
                                        colorFilter = ColorFilter.tint(activeBlue)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Action Link (View More / View Less)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isExpanded) "View Less" else "View More",
                        color = activeBlue,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                        modifier = Modifier.clickable {
                            onExpandToggle()
                        }
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

                            val showChat = AndroidUtils.isWithinOneHour(appointment.appointment_from) || statusText.lowercase() == "completed"
                            val showVideo = AndroidUtils.isWithinOneMinute(appointment.appointment_from)

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

@Composable
private fun AttendeeRow(
    name: String,
    isOrganizer: Boolean,
    rsvp: String
) {
    val activeBlue = Color(0xFF004D87)
    val initials = getInitials(name)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar Circle with Initials
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(activeBlue, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily(Font(R.font.gill_sans_regular))
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Name
        Text(
            text = name,
            color = Color.Black,
            fontSize = 14.sp,
            fontWeight = if (isOrganizer) FontWeight.Bold else FontWeight.Normal,
            fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
            modifier = Modifier.weight(1f)
        )

        // RSVP Status Icon
        val cleanRsvp = rsvp.lowercase(Locale.ROOT).trim()
        when (cleanRsvp) {
            "yes" -> {
                Image(
                    painter = painterResource(id = R.drawable.ic_check_green),
                    contentDescription = "RSVP Yes",
                    modifier = Modifier.size(18.dp)
                )
            }
            "no" -> {
                Image(
                    painter = painterResource(id = R.drawable.icon_cancel),
                    contentDescription = "RSVP No",
                    modifier = Modifier.size(18.dp),
                    colorFilter = ColorFilter.tint(Color(0xFFE74C3C))
                )
            }
            else -> {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(Color(0xFFEEEEEE), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "?",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
