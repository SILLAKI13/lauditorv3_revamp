package com.digicoffer.lauditor.feature.dashboard.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.feature.dashboard.data.model.DashboardDateInfo

private val GillSansRegular = FontFamily(Font(R.font.gill_sans_regular))

@Composable
fun DashboardDateHeader(
    dateInfo: DashboardDateInfo,
    modifier: Modifier = Modifier
) {
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF004D87),
            Color(0xFF0A75B8),
            Color(0xFF004D87)
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(brush = gradientBrush)
            .padding(vertical = 12.dp, horizontal = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Month
            DateColumn(
                label = "Month",
                value = dateInfo.month,
                modifier = Modifier.weight(1f)
            )

            // Divider 1
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(Color(0x44FFFFFF))
            )

            // Date
            DateColumn(
                label = "Date",
                value = dateInfo.date,
                modifier = Modifier.weight(1f)
            )

            // Divider 2
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(Color(0x44FFFFFF))
            )

            // Year
            DateColumn(
                label = "Year",
                value = dateInfo.year,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DateColumn(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = GillSansRegular,
                fontSize = 12.sp,
                color = Color.White,
                letterSpacing = 0.5.sp
            ),
            modifier = Modifier.padding(bottom = 2.dp)
        )
        Text(
            text = value,
            style = TextStyle(
                fontFamily = GillSansRegular,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
    }
}
