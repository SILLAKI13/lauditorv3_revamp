package com.digicoffer.lauditor.core.designsystem.theme

import androidx.compose.runtime.staticCompositionLocalOf
import com.digicoffer.lauditor.core.designsystem.colors.LightColors
import com.digicoffer.lauditor.core.designsystem.dimensions.DimensTokens
import com.digicoffer.lauditor.core.designsystem.shapes.ShapeTokens
import com.digicoffer.lauditor.core.designsystem.typography.TextStyles

val LocalLauditorColors = staticCompositionLocalOf { LightColors }
val LocalLauditorTypography = staticCompositionLocalOf { TextStyles }
val LocalLauditorDimensions = staticCompositionLocalOf { DimensTokens.DefaultDimensions }
val LocalLauditorShapes = staticCompositionLocalOf { ShapeTokens.DefaultShapes }
