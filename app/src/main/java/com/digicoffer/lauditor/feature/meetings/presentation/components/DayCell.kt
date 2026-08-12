package com.digicoffer.lauditor.feature.meetings.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.R

@Composable
fun DayCell(
    dateText: String,
    eventCount: Int,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeBlue = Color(0xFF004D87)
    val textAndBorderColor = Color(0xFFC0C0C0)
    val defaultBackground = Color(0xFFF5F5F5)

    val backgroundModifier = if (isSelected || isToday) {
        Modifier.background(activeBlue, RoundedCornerShape(6.dp))
    } else {
        Modifier
            .background(defaultBackground, RoundedCornerShape(6.dp))
            .border(0.5.dp, textAndBorderColor, RoundedCornerShape(6.dp))
    }

    val textColor = if (isSelected || isToday) Color.White else Color.Black

    Column(
        modifier = modifier
            .width(35.dp)
            .height(55.dp)
            .clip(RoundedCornerShape(6.dp))
            .then(backgroundModifier)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = dateText,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(2.dp))

        if (eventCount > 0) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = eventCount.toString(),
                    color = activeBlue,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
