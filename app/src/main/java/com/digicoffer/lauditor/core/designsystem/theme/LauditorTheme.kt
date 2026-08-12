package com.digicoffer.lauditor.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import com.digicoffer.lauditor.core.designsystem.colors.DarkColors
import com.digicoffer.lauditor.core.designsystem.colors.LauditorColors
import com.digicoffer.lauditor.core.designsystem.colors.LightColors
import com.digicoffer.lauditor.core.designsystem.dimensions.DimensTokens
import com.digicoffer.lauditor.core.designsystem.dimensions.LauditorDimensions
import com.digicoffer.lauditor.core.designsystem.shapes.LauditorShapes
import com.digicoffer.lauditor.core.designsystem.shapes.ShapeTokens
import com.digicoffer.lauditor.core.designsystem.typography.LauditorTypography
import com.digicoffer.lauditor.core.designsystem.typography.TextStyles

object LauditorTheme {
    val colors: LauditorColors
        @Composable
        @ReadOnlyComposable
        get() = LocalLauditorColors.current

    val typography: LauditorTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalLauditorTypography.current

    val dimensions: LauditorDimensions
        @Composable
        @ReadOnlyComposable
        get() = LocalLauditorDimensions.current

    val shapes: LauditorShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalLauditorShapes.current
}

@Composable
fun LauditorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val typography = TextStyles
    val dimensions = DimensTokens.DefaultDimensions
    val shapes = ShapeTokens.DefaultShapes

    CompositionLocalProvider(
        LocalLauditorColors provides colors,
        LocalLauditorTypography provides typography,
        LocalLauditorDimensions provides dimensions,
        LocalLauditorShapes provides shapes,
        content = content
    )
}
