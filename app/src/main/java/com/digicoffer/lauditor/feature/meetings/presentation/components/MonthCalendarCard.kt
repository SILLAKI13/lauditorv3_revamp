package com.digicoffer.lauditor.feature.meetings.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.R

data class MonthDay(
    val dayNumber: Int,
    val dateStr: String, // "dd-MM-yyyy"
    val isCurrentMonth: Boolean,
    val isSelected: Boolean,
    val isToday: Boolean,
    val eventCount: Int = 0,
    val hasEvents: Boolean = false,
    val hasAppointments: Boolean = false
)

@Composable
fun MonthCalendarCard(
    monthTitle: String,
    monthDays: List<MonthDay>,
    onPreviousMonthClick: () -> Unit,
    onNextMonthClick: () -> Unit,
    onDayClick: (MonthDay) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeBlue = Color(0xFF004D87)
    val textAndBorderColor = Color(0xFFC0C0C0)
    val defaultBackground = Color(0xFFF5F5F5)
    val weekdays = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: <  Month YYYY  >
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(4.dp))
                        .clickable { onPreviousMonthClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.back_arrow),
                        contentDescription = "Previous Month",
                        modifier = Modifier.size(16.dp),
                        colorFilter = ColorFilter.tint(Color.Black)
                    )
                }

                Text(
                    text = monthTitle,
                    color = activeBlue,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                    textAlign = TextAlign.Center
                )

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(4.dp))
                        .clickable { onNextMonthClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.back_arrow),
                        contentDescription = "Next Month",
                        modifier = Modifier
                            .size(16.dp)
                            .scale(-1f, 1f),
                        colorFilter = ColorFilter.tint(Color.Black)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Weekday labels (Sun, Mon, Tue, Wed, Thu, Fri, Sat in activeBlue)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekdays.forEach { dayName ->
                    Text(
                        text = dayName,
                        color = activeBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Days Grid (chunked into 7 columns)
            val rows = monthDays.chunked(7)
            rows.forEach { week ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    week.forEach { day ->
                        if (!day.isCurrentMonth || day.dayNumber <= 0) {
                            // Blank cell for padding leading / trailing days
                            Spacer(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .padding(horizontal = 2.dp)
                            )
                        } else {
                            val backgroundModifier = if (day.isSelected) {
                                Modifier.background(activeBlue, RoundedCornerShape(6.dp))
                            } else {
                                Modifier
                                    .background(defaultBackground, RoundedCornerShape(6.dp))
                                    .border(0.5.dp, textAndBorderColor, RoundedCornerShape(6.dp))
                            }

                            val textColor = if (day.isSelected) Color.White else Color.Black

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .padding(horizontal = 2.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .then(backgroundModifier)
                                    .clickable { onDayClick(day) },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = day.dayNumber.toString(),
                                    color = textColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                if (day.eventCount > 0) {
                                    if (day.isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(17.dp)
                                                .background(Color.White, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = day.eventCount.toString(),
                                                color = activeBlue,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = day.eventCount.toString(),
                                            color = activeBlue,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.height(17.dp))
                                }
                            }
                        }
                    }
                    val emptyInRow = 7 - week.size
                    for (k in 0 until emptyInRow) {
                        Spacer(
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .padding(horizontal = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
