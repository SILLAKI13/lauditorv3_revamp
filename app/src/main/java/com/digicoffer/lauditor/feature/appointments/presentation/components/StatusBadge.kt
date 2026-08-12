package com.digicoffer.lauditor.feature.appointments.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.R
import java.util.Locale

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

    Text(
        text = displayText,
        color = textColor,
        fontSize = 12.sp,
        fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
        modifier = modifier
            .background(backgroundColor, shape = RoundedCornerShape(15.dp)) // Corner radius matches R.dimen.Fifteen_dp from scheduled_badge.xml
            .padding(horizontal = 10.dp, vertical = 5.dp) // padding matches 10dp and 5dp padding in XML
    )
}
