package com.digicoffer.lauditor.core.ui.common.foundation

import android.graphics.Color as AndroidColor
import android.util.TypedValue
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.R

@Composable
fun AppProfileAvatar(
    imageUrl: String?,
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 30.dp,
    fontSize: TextUnit = 12.sp,
    fallbackBgColor: Color = Color(0xFF004D87)
) {
    AndroidView(
        modifier = modifier.size(size),
        factory = { ctx ->
            val frameLayout = FrameLayout(ctx).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            }

            val fallbackTextView = TextView(ctx).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                gravity = Gravity.CENTER
                setTextColor(AndroidColor.WHITE)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize.value)
                typeface = ResourcesCompat.getFont(ctx, R.font.gill_sans_bold)
                val bgDrawable = ContextCompat.getDrawable(ctx, R.drawable.blue_circular)?.mutate()?.apply {
                    setTint(fallbackBgColor.toArgb())
                }
                background = bgDrawable
            }

            val imageView = ImageView(ctx).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.CENTER_CROP
            }

            frameLayout.addView(fallbackTextView)
            frameLayout.addView(imageView)

            AndroidUtils.loadProfileImage(ctx, imageUrl, imageView, fallbackTextView, name)
            frameLayout
        },
        update = { frameLayout ->
            val fallbackTextView = frameLayout.getChildAt(0) as? TextView
            val imageView = frameLayout.getChildAt(1) as? ImageView
            if (fallbackTextView != null && imageView != null) {
                AndroidUtils.loadProfileImage(frameLayout.context, imageUrl, imageView, fallbackTextView, name)
            }
        }
    )
}
