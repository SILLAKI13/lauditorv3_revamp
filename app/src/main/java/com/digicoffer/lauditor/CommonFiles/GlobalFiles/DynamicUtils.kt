package com.digicoffer.lauditor.CommonFiles.GlobalFiles

import android.content.Context
import android.content.res.Configuration
import android.util.Log

object DynamicUtils {
    @JvmField var fifteen: Int = 15
    @JvmField var twelve: Int = 12
    @JvmField var eighteen: Int = 18
    @JvmField var twenty: Int = 20
    @JvmField var twentyFive: Int = 25
    @JvmField var oneFifty: Int = 150
    @JvmField var fifty: Int = 50
    @JvmField var fiftyFive: Int = 55
    @JvmField var sixtySeven: Int = 67
    @JvmField var thirty: Int = 30
    @JvmField var thirtyFive: Int = 35

    @JvmStatic
    fun isTablet(context: Context): Boolean {
        val config = context.resources.configuration
        val screenLayout = config.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
        val isLargeScreen = screenLayout >= Configuration.SCREENLAYOUT_SIZE_LARGE
        val isSw600 = config.smallestScreenWidthDp >= 600

        Log.d(
            "DeviceCheck",
            "screenLayout: $screenLayout, isLargeScreen: $isLargeScreen, sw600: $isSw600"
        )

        return isLargeScreen || isSw600
    }

    @JvmStatic
    fun loadRefreshDynamicSizes(context: Context) {
        if (isTablet(context)) {
            fifteen = 20
            twelve = 17
            eighteen = 24
            twenty = 27
            oneFifty = 150
            twentyFive = 34
            thirty = 40
            fifty = 60
            fiftyFive = 70
            sixtySeven = 77
            thirtyFive = 42
        } else {
            twenty = 20
            fifteen = 15
            twelve = 12
            oneFifty = 150
            eighteen = 18
            twentyFive = 25
            thirty = 30
            fifty = 50
            fiftyFive = 55
            sixtySeven = 67
            thirtyFive = 35
        }
    }

    @JvmStatic
    fun isNormalPhone(context: Context): String {
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        return when {
            screenWidth >= 1080 -> "Normal"
            screenWidth >= 720 -> "Medium"
            else -> "Small"
        }
    }
}
