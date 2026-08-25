package com.digicoffer.lauditor.core.ui.common.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.digicoffer.lauditor.R

@Composable
fun ComposeHorizontalPagerRibbon(
    currentPage: Int,
    totalPages: Int,
    onPageSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    cellSize: Dp = 30.dp,
    cornerRadius: Dp = 8.dp,
    selectedTextColor: Color = Color.White,
    unselectedTextColor: Color = Color(0xFF004D87),
    selectedBrush: Brush = Brush.verticalGradient(listOf(Color(0xFF1976D2), Color(0xFF004D87))),
    textStyle: TextStyle = TextStyle.Default,
    leftArrowIcon: Int = R.drawable.left_arrow,
    rightArrowIcon: Int = R.drawable.right_arrow
) {
    if (totalPages <= 1) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Backward button
        IconButton(
            onClick = { if (currentPage > 1) onPageSelected(currentPage - 1) },
            enabled = currentPage > 1,
            modifier = Modifier.size(35.dp)
        ) {
            Icon(
                painter = painterResource(id = leftArrowIcon),
                contentDescription = "Previous Page",
                tint = if (currentPage > 1) Color(0xFF004D87) else Color.LightGray,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Horizontal pages
        Row(
            modifier = Modifier
                .weight(1f, fill = false)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..totalPages) {
                val isSelected = i == currentPage
                Box(
                    modifier = Modifier
                        .size(cellSize)
                        .clip(RoundedCornerShape(cornerRadius))
                        .then(
                            if (isSelected) {
                                Modifier.background(selectedBrush)
                            } else {
                                Modifier.background(Color.Transparent)
                            }
                        )
                        .clickable { onPageSelected(i) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = i.toString(),
                        style = textStyle,
                        color = if (isSelected) selectedTextColor else unselectedTextColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Forward button
        IconButton(
            onClick = { if (currentPage < totalPages) onPageSelected(currentPage + 1) },
            enabled = currentPage < totalPages,
            modifier = Modifier.size(35.dp)
        ) {
            Icon(
                painter = painterResource(id = rightArrowIcon),
                contentDescription = "Next Page",
                tint = if (currentPage < totalPages) Color(0xFF004D87) else Color.LightGray,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
