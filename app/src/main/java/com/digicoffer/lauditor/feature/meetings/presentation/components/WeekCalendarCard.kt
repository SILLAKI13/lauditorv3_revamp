package com.digicoffer.lauditor.feature.meetings.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.scale
import com.digicoffer.lauditor.R

@Composable
fun WeekCalendarCard(
    dateRangeText: String,
    days: List<Pair<String, Int>>,
    selectedDayIndex: Int,
    todayDayIndex: Int,
    onPreviousWeekClick: () -> Unit,
    onNextWeekClick: () -> Unit,
    onDayClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeBlue = Color(0xFF004D87)
    val lightGrey = Color(0xFFE4F2FF)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Navigation Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Prev Week Button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(4.dp))
                        .clickable { onPreviousWeekClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.back_arrow),
                        contentDescription = "Previous Week",
                        modifier = Modifier.size(16.dp),
                        colorFilter = ColorFilter.tint(Color.Black)
                    )
                }

                // Date Range text
                Text(
                    text = dateRangeText,
                    color = activeBlue,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )

                // Next Week Button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(4.dp))
                        .clickable { onNextWeekClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.back_arrow),
                        contentDescription = "Next Week",
                        modifier = Modifier
                            .size(16.dp)
                            .scale(-1f, 1f),
                        colorFilter = ColorFilter.tint(Color.Black)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Horizontal Day Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                days.forEachIndexed { index, day ->
                    DayCell(
                        dateText = day.first,
                        eventCount = day.second,
                        isSelected = index == selectedDayIndex,
                        isToday = index == todayDayIndex,
                        onClick = { onDayClick(index) }
                    )
                }
            }
        }
    }
}
