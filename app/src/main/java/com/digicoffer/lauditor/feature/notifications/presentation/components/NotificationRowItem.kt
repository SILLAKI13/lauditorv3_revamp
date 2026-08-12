package com.digicoffer.lauditor.feature.notifications.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.Notifications.Models.NotificationsDo
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.designsystem.components.AppCircleCheckbox
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private val GillSans = FontFamily(Font(R.font.gill_sans))

@Composable
fun NotificationRowItem(
    notification: NotificationsDo,
    onCheckedChange: (Boolean) -> Unit,
    onRowClick: () -> Unit,
    isLastItem: Boolean,
    modifier: Modifier = Modifier
) {
    val isUnread = "unread".equals(notification.status, ignoreCase = true)
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onRowClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 72.dp)
                .padding(vertical = 8.dp)
        ) {
            // Unread Indicator dot or Read Alignment Spacer
            if (isUnread) {
                Box(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .size(8.dp)
                        .background(Color(0xFF004D87), CircleShape) // @color/Primary_new blue_dot
                )
            } else {
                Spacer(modifier = Modifier.width(20.dp)) // view_indicator_space
            }
            
            // Message Content Block
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 8.dp)
            ) {
                Text(
                    text = notification.message ?: "",
                    style = TextStyle(
                        fontFamily = GillSans,
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                        color = if (isUnread) Color(0xFF004D87) else Color(0xFF1A1A2E) // Primary_new or dark_text
                    )
                )
                
                // Clock & Timestamp Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.clock_blue),
                        contentDescription = "Time",
                        modifier = Modifier
                            .size(13.dp)
                            .padding(end = 4.dp)
                    )
                    Text(
                        text = formatTimeOnly(notification.timestamp),
                        style = TextStyle(
                            fontFamily = GillSans,
                            fontSize = 12.sp,
                            color = Color(0xFF999999)
                        )
                    )
                }
            }
            
            // Checkbox Container
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(end = 12.dp)
            ) {
                AppCircleCheckbox(
                    checked = notification.isChecked,
                    onCheckedChange = onCheckedChange,
                    size = 22.dp
                )
            }
        }
        
        // Row Divider (hidden if last item in group)
        if (!isLastItem) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .padding(start = 24.dp)
                    .background(Color(0xFFE0E0E0)) // #E0E0E0 divider
            )
        }
    }
}

private fun formatTimeOnly(timestamp: String?): String {
    if (timestamp.isNullOrEmpty()) return ""
    return try {
        val isoFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val outFmt = SimpleDateFormat("hh:mm a", Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }
        val date = isoFmt.parse(timestamp)
        if (date != null) outFmt.format(date) else ""
    } catch (e: Exception) {
        ""
    }
}

@Preview(showBackground = true, name = "NotificationRowItem Unread Preview")
@Composable
fun NotificationRowItemUnreadPreview() {
    LauditorTheme {
        val n = NotificationsDo().apply {
            message = "This is an unread notification message."
            timestamp = "2026-07-29T10:00:00.000Z"
            status = "unread"
            isChecked = false
        }
        NotificationRowItem(notification = n, onCheckedChange = {}, onRowClick = {}, isLastItem = false)
    }
}

@Preview(showBackground = true, name = "NotificationRowItem Read Last Preview")
@Composable
fun NotificationRowItemReadPreview() {
    LauditorTheme {
        val n = NotificationsDo().apply {
            message = "This is a read notification message."
            timestamp = "2026-07-29T09:30:00.000Z"
            status = "read"
            isChecked = true
        }
        NotificationRowItem(notification = n, onCheckedChange = {}, onRowClick = {}, isLastItem = true)
    }
}
