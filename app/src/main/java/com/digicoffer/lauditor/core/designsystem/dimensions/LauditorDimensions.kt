package com.digicoffer.lauditor.core.designsystem.dimensions

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class LauditorDimensions(
    val none: Dp = 0.dp,
    val extraSmall: Dp = 2.dp,
    val small: Dp = 4.dp,
    val medium: Dp = 8.dp,
    val defaultPadding: Dp = 12.dp,
    val large: Dp = 16.dp,
    val extraLarge: Dp = 20.dp,
    val huge: Dp = 24.dp,
    val giant: Dp = 32.dp,
    val iconSmall: Dp = 16.dp,
    val iconMedium: Dp = 24.dp,
    val iconLarge: Dp = 32.dp,
    val cardElevation: Dp = 4.dp,
    val dividerThickness: Dp = 1.dp,
    val buttonHeight: Dp = 48.dp,
    val inputHeight: Dp = 56.dp,
    val toolbarHeight: Dp = 56.dp
)

object DimensTokens {
    val DefaultDimensions = LauditorDimensions()
}
