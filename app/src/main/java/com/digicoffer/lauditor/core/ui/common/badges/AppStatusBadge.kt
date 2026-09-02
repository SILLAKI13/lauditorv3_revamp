package com.digicoffer.lauditor.core.ui.common.badges

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme

enum class AppStatusStyle {
    SUCCESS,
    ERROR,
    INFO,
    WARNING,
    NEUTRAL
}

private val GillSansRegular = FontFamily(Font(R.font.gill_sans_regular))

/**
 * Generic semantic status badge.
 * Maps visual semantic styles ([AppStatusStyle]) to consistent design tokens
 * and renders via canonical [AppPillBadge].
 *
 * Feature layers are responsible for mapping domain-specific status strings to [AppStatusStyle].
 */
@Composable
fun AppStatusBadge(
    text: String,
    style: AppStatusStyle,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(15.dp),
    paddingHorizontal: Dp = 10.dp,
    paddingVertical: Dp = 5.dp,
    border: BorderStroke? = null,
    textStyle: TextStyle = TextStyle(
        fontFamily = GillSansRegular,
        fontSize = 12.sp
    )
) {
    val (backgroundColor, textColor) = when (style) {
        AppStatusStyle.SUCCESS -> Pair(Color(0xFFC8E6C9), Color(0xFF2E7D32))
        AppStatusStyle.ERROR -> Pair(Color(0xFFFFCDD2), Color(0xFFC62828))
        AppStatusStyle.INFO -> Pair(Color(0xFF9ECCF2), Color(0xFF1976D2))
        AppStatusStyle.WARNING -> Pair(Color(0xFFFFE0B2), Color(0xFFF57C00))
        AppStatusStyle.NEUTRAL -> Pair(Color(0xFF9ECCF2), Color(0xFF004D87))
    }

    AppPillBadge(
        text = text,
        backgroundColor = backgroundColor,
        textColor = textColor,
        textStyle = textStyle,
        shape = shape,
        paddingHorizontal = paddingHorizontal,
        paddingVertical = paddingVertical,
        border = border,
        modifier = modifier
    )
}

@Preview(showBackground = true, name = "AppStatusBadge Preview")
@Composable
fun AppStatusBadgePreview() {
    LauditorTheme {
        AppStatusBadge(
            text = "Completed",
            style = AppStatusStyle.SUCCESS
        )
    }
}
