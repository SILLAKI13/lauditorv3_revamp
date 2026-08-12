package com.digicoffer.lauditor.core.designsystem.animations

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Immutable

@Immutable
object LauditorAnimations {
    val defaultDurationMillis: Int = 300
    val fastDurationMillis: Int = 150

    val fadeInSpec: AnimationSpec<Float> = tween(
        durationMillis = defaultDurationMillis,
        easing = FastOutSlowInEasing
    )

    val fadeOutSpec: AnimationSpec<Float> = tween(
        durationMillis = fastDurationMillis,
        easing = FastOutSlowInEasing
    )
}
