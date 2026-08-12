package com.digicoffer.lauditor.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.digicoffer.lauditor.R

@Composable
fun DateIntervalSelector(
    startDateText: String,
    endDateText: String,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit,
    onClearStartDate: (() -> Unit)? = null,
    onClearEndDate: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 6.dp,
    borderColor: Color = Color(0xFFC0C0C0),
    backgroundColor: Color = Color(0xFFF9FAFB),
    textColor: Color = Color.Black,
    textStyle: TextStyle = TextStyle.Default,
    calendarIcon: Int = R.drawable.calendar_icon_xsmall,
    cancelIcon: Int = R.drawable.cancel_red_icon
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Start Date field
        Column(modifier = Modifier.weight(1f)) {
            // Label above
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "From",
                    style = textStyle,
                    color = Color(0xFF004D87)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(backgroundColor)
                    .border(0.5.dp, borderColor, RoundedCornerShape(cornerRadius))
                    .clickable { onStartDateClick() }
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = startDateText.ifEmpty { "From" },
                        style = textStyle,
                        color = if (startDateText.isEmpty()) Color.Gray else textColor,
                        modifier = Modifier.weight(1f)
                    )
                    if (startDateText.isNotEmpty() && onClearStartDate != null) {
                        IconButton(
                            onClick = onClearStartDate,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = cancelIcon),
                                contentDescription = "Clear Start Date",
                                tint = Color.Unspecified
                            )
                        }
                    } else {
                        Icon(
                            painter = painterResource(id = calendarIcon),
                            contentDescription = "Start Date Icon",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // End Date field
        Column(modifier = Modifier.weight(1f)) {
            // Label above
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "To",
                    style = textStyle,
                    color = Color(0xFF004D87)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(backgroundColor)
                    .border(0.5.dp, borderColor, RoundedCornerShape(cornerRadius))
                    .clickable { onEndDateClick() }
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = endDateText.ifEmpty { "To" },
                        style = textStyle,
                        color = if (endDateText.isEmpty()) Color.Gray else textColor,
                        modifier = Modifier.weight(1f)
                    )
                    if (endDateText.isNotEmpty() && onClearEndDate != null) {
                        IconButton(
                            onClick = onClearEndDate,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = cancelIcon),
                                contentDescription = "Clear End Date",
                                tint = Color.Unspecified
                            )
                        }
                    } else {
                        Icon(
                            painter = painterResource(id = calendarIcon),
                            contentDescription = "End Date Icon",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
