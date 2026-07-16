package com.digicoffer.lauditor.CommonFiles.GlobalFiles;

import android.content.Context;
import android.graphics.drawable.Drawable;
import com.applandeo.materialcalendarview.CalendarUtils;
import com.applandeo.materialcalendarview.utils.DayColorsUtils;

public class CalendarHelper {
    public static Drawable getDrawableText(Context context, String string, int color, int size) {
        return CalendarUtils.getDrawableText(context, string, null, color, size);
    }

    public static int parseColor(Context context, int colorRes) {
        return DayColorsUtils.parseColor(context, colorRes);
    }
}
