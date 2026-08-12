package com.digicoffer.lauditor.feature.appointments.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.Appointments.Models.AppointmentModel
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.R
import java.util.Locale

@Composable
fun ActionDropdownCard(
    appointment: AppointmentModel,
    onHistoryClick: () -> Unit,
    onCancelClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val status = (appointment.appointment_status ?: "").lowercase(Locale.ROOT).trim()
    val isUpcomingOrOngoing = status == "upcoming" || status == "ongoing"
    val isCancelEnabled = !AndroidUtils.isWithinTwoHours(appointment.appointment_from)
    val canDelete = status == "completed" || status == "cancelled" || status == "canceled"

    Card(
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
        modifier = modifier.width(150.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // History Item
            Text(
                text = "History",
                fontSize = 15.sp,
                fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                color = Color.Black,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onHistoryClick() }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )

            // Cancel Item
            if (isUpcomingOrOngoing) {
                HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE0E0E0))
                Text(
                    text = "Cancel",
                    fontSize = 15.sp,
                    fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                    color = Color.Black,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(if (isCancelEnabled) 1.0f else 0.4f)
                        .clickable(enabled = isCancelEnabled) { onCancelClick() }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            // Delete Item
            if (canDelete) {
                HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE0E0E0))
                Text(
                    text = "Delete",
                    fontSize = 15.sp,
                    fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                    color = Color.Black,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDeleteClick() }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }
}
