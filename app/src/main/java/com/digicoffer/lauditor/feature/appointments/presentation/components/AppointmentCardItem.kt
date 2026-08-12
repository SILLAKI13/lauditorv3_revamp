package com.digicoffer.lauditor.feature.appointments.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.Appointments.Models.AppointmentModel
import java.text.SimpleDateFormat
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.R
import java.util.Locale

@Composable
fun AppointmentCardItem(
    appointment: AppointmentModel,
    isMenuExpanded: Boolean,
    onMenuToggle: () -> Unit,
    onHistoryClick: () -> Unit,
    onCancelClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onVideoCallClick: () -> Unit,
    onChatClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val status = (appointment.appointment_status ?: "").lowercase(Locale.ROOT).trim()

    // Enabled window status checks
    val isChatEnabled = AndroidUtils.isWithinOneHour(appointment.appointment_from) || status == "completed"
    val isVideoEnabled = AndroidUtils.isWithinOneMinute(appointment.appointment_from)

    // Visibility triggers
    val showChatIcon = status == "upcoming" || status == "ongoing" || status == "completed"
    val showVideoIcon = status == "upcoming" || status == "ongoing"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                // Top Row: Profile Avatar, Name, Three-Dot Menu
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Profile Avatar Initials fallback
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(30.dp)
                            .background(Color(0xFF004D87), shape = CircleShape)
                    ) {
                        val initials = if (appointment.client_name.isNotEmpty()) {
                            appointment.client_name.take(1).uppercase(Locale.ROOT)
                        } else {
                            "P"
                        }
                        Text(
                            text = initials,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = appointment.client_name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF004D87),
                        fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Three-dot Action Trigger
                    Image(
                        painter = painterResource(id = R.drawable.img_17),
                        contentDescription = "Menu Options",
                        modifier = Modifier
                            .size(30.dp)
                            .clickable { onMenuToggle() }
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Date-Time Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img),
                        contentDescription = "Date-Time",
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color(0xFF4A5565)),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))

                    val formattedDateTime = formatAppointmentDateTime(
                        appointment.appointment_from,
                        appointment.appointment_to
                    )
                    Text(
                        text = formattedDateTime,
                        fontSize = 13.sp,
                        color = Color(0xFF4A5565),
                        fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Payment Status
                val paymentStatus = appointment.payment?.status ?: ""
                val amountPaid = appointment.payment?.amount_paid ?: ""
                val symbol = appointment.payment?.symbol ?: ""
                val isPaid = "paid".equals(paymentStatus, ignoreCase = true)

                Text(
                    text = if (isPaid) "Paid - $symbol$amountPaid" else "Pending - ${symbol}0",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPaid) Color(0xFF4CAF50) else Color(0xFFFF9800),
                    fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Status Badge & Action Icons Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatusBadge(status = appointment.appointment_status)

                    Spacer(modifier = Modifier.weight(1f))

                    // Chat icon button
                    if (showChatIcon) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(30.dp)
                                .background(Color(0xFF9ECCF2), shape = CircleShape)
                                .alpha(if (isChatEnabled) 1.0f else 0.4f)
                                .clickable(enabled = isChatEnabled) { onChatClick() }
                                .padding(8.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.message_icon),
                                contentDescription = "Chat",
                                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color(0xFF1976D2))
                            )
                        }
                    }

                    if (showVideoIcon) {
                        Spacer(modifier = Modifier.width(4.dp))
                        // Video Call button
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(30.dp)
                                .background(Color(0xFFC8E6C9), shape = CircleShape)
                                .alpha(if (isVideoEnabled) 1.0f else 0.4f)
                                .clickable(enabled = isVideoEnabled) { onVideoCallClick() }
                                .padding(8.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.videocall_icon),
                                contentDescription = "Video Call",
                                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color(0xFF2E7D32))
                            )
                        }
                    }
                }
            }
        }

        // Action Dropdown Card Overlay
        if (isMenuExpanded) {
            ActionDropdownCard(
                appointment = appointment,
                onHistoryClick = {
                    onMenuToggle()
                    onHistoryClick()
                },
                onCancelClick = {
                    onMenuToggle()
                    onCancelClick()
                },
                onDeleteClick = {
                    onMenuToggle()
                    onDeleteClick()
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 40.dp, end = 10.dp)
            )
        }
    }
}

// Replicate datetime formats
private fun formatAppointmentDateTime(from: String, to: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
        val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)
        val timeFormatter = SimpleDateFormat("h:mm a", Locale.ENGLISH)

        val dateFrom = parser.parse(from)
        val dateTo = parser.parse(to)

        if (dateFrom != null && dateTo != null) {
            "${dateFormatter.format(dateFrom)} • ${timeFormatter.format(dateFrom)} - ${timeFormatter.format(dateTo)}"
        } else {
            "$from - $to"
        }
    } catch (e: Exception) {
        "$from - $to"
    }
}
