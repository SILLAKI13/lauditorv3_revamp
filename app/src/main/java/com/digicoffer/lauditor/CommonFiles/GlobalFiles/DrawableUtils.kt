package com.digicoffer.lauditor.CommonFiles.GlobalFiles

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.LayerDrawable
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.digicoffer.lauditor.R

class DrawableUtils private constructor() {
    companion object {
        @JvmStatic
        fun getCircleDrawableWithText(context: Context, string: String): Drawable {
            val background = ContextCompat.getDrawable(context, R.drawable.sample_circle)!!
            val text = CalendarHelper.getDrawableText(context, string, android.R.color.holo_blue_dark, 12)!!
            val layers = arrayOf(background, text)
            return LayerDrawable(layers)
        }

        @JvmStatic
        fun getThreeDots(context: Context): Drawable {
            val drawable = ContextCompat.getDrawable(context, R.drawable.sample_three_icons)!!
            return InsetDrawable(drawable, 100, 0, 100, 0)
        }

        @JvmStatic
        fun getDayCircle(context: Context, @ColorRes borderColor: Int, @ColorRes fillColor: Int): Drawable {
            val drawable = ContextCompat.getDrawable(context, R.drawable.calendar_day_background) as GradientDrawable
            drawable.setStroke(6, CalendarHelper.parseColor(context, borderColor))
            drawable.setColor(CalendarHelper.parseColor(context, fillColor))
            return drawable
        }
    }
}
