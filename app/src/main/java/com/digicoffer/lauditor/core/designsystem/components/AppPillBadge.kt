package com.digicoffer.lauditor.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    paddingVertical: Dp = 2.dp
) {
    if (text.isNotEmpty()) {
        Text(
            text = text,
            color = textColor,
            style = textStyle,
            modifier = modifier
                .background(backgroundColor, CircleShape)
                .padding(horizontal = paddingHorizontal, vertical = paddingVertical)
        )
    }
}

@Preview(showBackground = true, name = "AppPillBadge Preview")
@Composable
fun AppPillBadgePreview() {
    LauditorTheme {
        AppPillBadge(text = "5")
    }
}
