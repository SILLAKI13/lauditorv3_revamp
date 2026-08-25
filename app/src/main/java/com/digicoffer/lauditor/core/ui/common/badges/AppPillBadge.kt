package com.digicoffer.lauditor.core.ui.common.badges

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme

private val GillSansRegular = FontFamily(Font(R.font.gill_sans_regular))

@Composable
fun AppPillBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF004D87), // Brand Blue count badge background
    textColor: Color = Color.White,
    textStyle: TextStyle = TextStyle(
        fontFamily = GillSansRegular,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
    ),
    paddingHorizontal: Dp = 8.dp,
    paddingVertical: Dp = 2.dp,
    border: BorderStroke? = null,
    shape: Shape = CircleShape,
    onRemove: (() -> Unit)? = null,
    removeIcon: @Composable (() -> Unit)? = null
) {
    if (text.isNotEmpty()) {
        var baseModifier = modifier.background(backgroundColor, shape)
        if (border != null) {
            baseModifier = baseModifier.border(border, shape)
        }

        Row(
            modifier = baseModifier.padding(horizontal = paddingHorizontal, vertical = paddingVertical),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                color = textColor,
                style = textStyle
            )
            if (onRemove != null) {
                Spacer(modifier = Modifier.width(4.dp))
                if (removeIcon != null) {
                    removeIcon()
                } else {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = textColor,
                        modifier = Modifier
                            .size(14.dp)
                            .clickable { onRemove() }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "AppPillBadge Preview")
@Composable
fun AppPillBadgePreview() {
    LauditorTheme {
        AppPillBadge(text = "5")
    }
}
