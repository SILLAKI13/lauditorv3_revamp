package com.digicoffer.lauditor.core.designsystem.typography

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.R

object FontTokens {
    val DefaultFontFamily = FontFamily(
        Font(R.font.gill_sans_regular, FontWeight.Normal),
        Font(R.font.gill_sans_bold, FontWeight.Bold),
        Font(R.font.gill_sans, FontWeight.Medium)
    )
}

val TextStyles = LauditorTypography(
    displayLarge = TextStyle(
        fontFamily = FontTokens.DefaultFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 36.sp
    ),
    headerTitle = TextStyle(
        fontFamily = FontTokens.DefaultFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontTokens.DefaultFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    subtitle = TextStyle(
        fontFamily = FontTokens.DefaultFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontTokens.DefaultFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyRegular = TextStyle(
        fontFamily = FontTokens.DefaultFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontTokens.DefaultFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelBold = TextStyle(
        fontFamily = FontTokens.DefaultFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    caption = TextStyle(
        fontFamily = FontTokens.DefaultFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 14.sp
    )
)
