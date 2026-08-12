package com.digicoffer.lauditor.core.designsystem.typography

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle

@Immutable
data class LauditorTypography(
    val displayLarge: TextStyle,
    val headerTitle: TextStyle,
    val titleMedium: TextStyle,
    val subtitle: TextStyle,
    val bodyLarge: TextStyle,
    val bodyRegular: TextStyle,
    val bodySmall: TextStyle,
    val labelBold: TextStyle,
    val caption: TextStyle
)
