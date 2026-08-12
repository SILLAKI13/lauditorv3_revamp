package com.digicoffer.lauditor.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AuditTrailCard(
    categoryName: String,
    timestamp: String,
    messageBody: String,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp,
    elevation: Dp = 10.dp,
    backgroundColor: Color = Color.White,
    titleColor: Color = Color(0xFF004D87),
    timestampColor: Color = Color(0xFF004D87),
    messageColor: Color = Color.Black,
    textStyle: TextStyle = TextStyle.Default
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 4.dp)
            .shadow(elevation, shape = RoundedCornerShape(cornerRadius))
            .background(backgroundColor, shape = RoundedCornerShape(cornerRadius))
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = categoryName.uppercase(),
                    style = textStyle,
                    color = titleColor,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = timestamp,
                    style = textStyle,
                    color = timestampColor
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = messageBody,
                    style = textStyle,
                    color = messageColor
                )
            }
        }
    }
}
