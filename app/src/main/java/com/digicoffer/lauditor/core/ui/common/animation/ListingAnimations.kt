package com.digicoffer.lauditor.core.ui.common.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay

/**
 * Exact Compose equivalent of Java/XML layout_fall_down.xml & fall_down.xml:
 * - Stagger delay: 15% (150ms per item)
 * - Duration: 1000ms
 * - Interpolator: @android:interpolator/decelerate_quad
 * - Translation: fromYDelta="-30%" (of item height) to 0%
 * - Alpha: 0.0 to 1.0
 */
val DecelerateQuadEasing = Easing { fraction -> 1f - (1f - fraction) * (1f - fraction) }

@Composable
fun Modifier.fallDownItem(
    index: Int,
    triggerKey: Any? = Unit
): Modifier {
    val animProgress = remember(triggerKey) { Animatable(0f) }

    LaunchedEffect(triggerKey) {
        val delayMillis = (index * 150L).coerceAtMost(900L)
        delay(delayMillis)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1000,
                easing = DecelerateQuadEasing
            )
        )
    }

    return this.graphicsLayer {
        val fraction = animProgress.value
        alpha = fraction
        translationY = (1f - fraction) * (-0.30f * size.height)
    }
}
