package com.digicoffer.lauditor.CommonFiles.GlobalFiles

import android.content.Context
import android.graphics.drawable.Drawable

class CalendarHelper {
    companion object {
        @JvmStatic
        fun getDrawableText(context: Context, string: String, color: Int, size: Int): Drawable? {
            return try {
                val clazz = Class.forName("com.applandeo.materialcalendarview.CalendarUtils")
                val method = clazz.getMethod(
                    "getDrawableText",
                    Context::class.java,
                    String::class.java,
                    android.graphics.Typeface::class.java,
                    Int::class.javaPrimitiveType ?: Int::class.java,
                    Int::class.javaPrimitiveType ?: Int::class.java
                )
                method.invoke(null, context, string, null, color, size) as? Drawable
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        @JvmStatic
        fun parseColor(context: Context, colorRes: Int): Int {
            return try {
                val clazz = Class.forName("com.applandeo.materialcalendarview.utils.DayColorsUtils")
                val method = clazz.getMethod(
                    "parseColor",
                    Context::class.java,
                    Int::class.javaPrimitiveType ?: Int::class.java
                )
                method.invoke(null, context, colorRes) as Int
            } catch (e: Exception) {
                e.printStackTrace()
                0
            }
        }
    }
}
