package com.digicoffer.lauditor.core.ui.feedback

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme

@Composable
fun AppShimmer(
    modifier: Modifier = Modifier,
    height: Dp = 100.dp
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200)
        ),
        label = "shimmerAnim"
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            LauditorTheme.colors.border.copy(alpha = 0.6f),
            LauditorTheme.colors.border.copy(alpha = 0.2f),
            LauditorTheme.colors.border.copy(alpha = 0.6f)
        ),
        start = Offset(10f, 10f),
        end = Offset(translateAnim, translateAnim)
    )

    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(brush, shape = LauditorTheme.shapes.medium)
    )
}

@Preview(showBackground = true, name = "AppShimmer Preview")
@Composable
fun AppShimmerPreview() {
    LauditorTheme {
        AppShimmer(height = 80.dp)
    }
}
