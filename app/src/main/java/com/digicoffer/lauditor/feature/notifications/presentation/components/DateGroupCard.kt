package com.digicoffer.lauditor.feature.notifications.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.Notifications.Models.NotificationsDo
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme
import kotlinx.coroutines.delay

private val GillSansBold = FontFamily(Font(R.font.gill_sans_bold))
private val GillSans = FontFamily(Font(R.font.gill_sans))

@Composable
fun DateGroupCard(
    dateKey: String,
    notifications: List<NotificationsDo>,
    onCheckedChange: (NotificationsDo, Boolean) -> Unit,
    onRowClick: (NotificationsDo) -> Unit,
    highlightIds: Set<String>,
    onHighlightDismiss: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    val initialHighlight = remember(highlightIds, notifications) {
        notifications.any { highlightIds.contains(it.id ?: "") }
    }
    
    var isHighlighted by remember(initialHighlight) { mutableStateOf(initialHighlight) }
    
    LaunchedEffect(initialHighlight) {
        if (initialHighlight) {
            delay(5000)
            isHighlighted = false
            onHighlightDismiss(notifications.mapNotNull { it.id })
        }
    }
    
    Card(
        shape = RoundedCornerShape(12.dp),
        border = if (isHighlighted) BorderStroke(1.dp, Color(0xFF004D87)) else null, // blue_stroke_card
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHighlighted) 8.dp else 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 5.dp) // Layout margins start/end 8dp, top/bottom 5dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {
            // Date Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 8.dp) // exact padding
            ) {
                Text(
                    text = dateKey,
                    style = TextStyle(
                        fontFamily = GillSansBold,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF004D87) // @color/blue
                    )
                )
                Text(
                    text = "(${notifications.size})",
                    style = TextStyle(
                        fontFamily = GillSans,
                        fontSize = 14.sp,
                        color = Color(0xFF999999) // #999999 count_header
                    ),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            
            // Header Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFFE0E0E0)) // #E0E0E0 header divider
            )
            
            // Notification Items List
            notifications.forEachIndexed { index, notification ->
                NotificationRowItem(
                    notification = notification,
                    onCheckedChange = { isChecked -> onCheckedChange(notification, isChecked) },
                    onRowClick = { onRowClick(notification) },
                    isLastItem = index == notifications.size - 1
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "DateGroupCard Highlighted Preview")
@Composable
fun DateGroupCardHighlightedPreview() {
    LauditorTheme {
        val list = listOf(
            NotificationsDo().apply {
                id = "n1"
                message = "Highlight sample entry."
                timestamp = "2026-07-29T10:00:00.000Z"
                status = "unread"
            }
        )
        DateGroupCard(
            dateKey = "Jul 29, 2026",
            notifications = list,
            onCheckedChange = { _, _ -> },
            onRowClick = {},
            highlightIds = setOf("n1"),
            onHighlightDismiss = {}
        )
    }
}
