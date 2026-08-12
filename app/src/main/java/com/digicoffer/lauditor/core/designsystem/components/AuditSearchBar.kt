package com.digicoffer.lauditor.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AuditSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 30.dp,
    elevation: Dp = 4.dp,
    borderColor: Color = Color.Transparent,
    backgroundColor: Color = Color.White,
    textColor: Color = Color.Black,
    placeholderColor: Color = Color.Gray,
    iconColor: Color = Color.Gray,
    icon: ImageVector = Icons.Default.Search,
    textStyle: TextStyle = TextStyle.Default
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation, shape = RoundedCornerShape(cornerRadius))
            .background(backgroundColor, shape = RoundedCornerShape(cornerRadius))
            .border(1.dp, borderColor, shape = RoundedCornerShape(cornerRadius))
            .height(40.dp)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Search",
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = textStyle.copy(color = placeholderColor)
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = textStyle.copy(color = textColor),
                    cursorBrush = SolidColor(textColor),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear Search",
                        tint = placeholderColor
                    )
                }
            }
        }
    }
}
