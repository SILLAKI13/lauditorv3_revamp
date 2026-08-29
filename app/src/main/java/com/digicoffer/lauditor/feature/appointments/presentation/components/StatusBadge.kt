package com.digicoffer.lauditor.feature.appointments.presentation.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.digicoffer.lauditor.core.ui.common.badges.AppPillBadge
import java.util.Locale

/**
 * Appointment status badge delegating directly to canonical [AppPillBadge].
 */
@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val cleanStatus = status.lowercase(Locale.ROOT).trim()

    val (backgroundColor, textColor, displayText) = when (cleanStatus) {
        "completed" -> Triple(Color(0xFFC8E6C9), Color(0xFF2E7D32), "Completed")
        "cancelled", "canceled" -> Triple(Color(0xFFFFCDD2), Color(0xFFC62828), "Cancelled")
        "upcoming", "ongoing" -> Triple(Color(0xFF9ECCF2), Color(0xFF1976D2), if (cleanStatus == "upcoming") "Upcoming" else "Ongoing")
        "payment_pending", "pending" -> Triple(Color(0xFFFFE0B2), Color(0xFFF57C00), "Payment Pending")
        else -> Triple(Color(0xFF9ECCF2), Color(0xFF004D87), "Scheduled")
    }

    AppPillBadge(
        text = displayText,
        backgroundColor = backgroundColor,
        textColor = textColor,
        shape = RoundedCornerShape(15.dp),
        paddingHorizontal = 10.dp,
        paddingVertical = 5.dp,
        modifier = modifier
    )
}
