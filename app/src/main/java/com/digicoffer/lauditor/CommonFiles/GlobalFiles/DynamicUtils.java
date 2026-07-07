package com.digicoffer.lauditor.CommonFiles.GlobalFiles;

import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.util.Log;

public class DynamicUtils {
    public static int fifteen = 15;
    public static int twelve = 12;
    public static int eighteen = 18;
    public static int twenty = 20;
    public static int twentyFive = 25;
    public static int oneFifty = 150;
    public static int fifty = 50;
    public static int fiftyFive = 55;
    public static int sixtySeven = 67;
    public static int thirty = 30;
    public static int thirtyFive = 35;

    public static boolean isTablet(Context context) {
        Configuration config = context.getResources().getConfiguration();
        int screenLayout = config.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        boolean isLargeScreen = screenLayout >= Configuration.SCREENLAYOUT_SIZE_LARGE;

        boolean isSw600 = config.smallestScreenWidthDp >= 600;

        Log.d(
                "DeviceCheck",
                "screenLayout: " + screenLayout + ", isLargeScreen: " + isLargeScreen + ", sw600: " + isSw600
        );

        return isLargeScreen || isSw600;
    }

    public static void loadRefreshDynamicSizes(Context context) {

        if (isTablet(context)) {
            fifteen = 20;
            twelve = 17;
            eighteen = 24;
            twenty = 27;
            oneFifty = 150;
            twentyFive = 34;
            thirty = 40;
            fifty = 60;
            fiftyFive = 70;
            sixtySeven = 77;
            thirtyFive = 42;
        } else {
            twenty = 20;
            fifteen = 15;
            twelve = 12;
            oneFifty = 150;
            eighteen = 18;
            twentyFive = 25;
            thirty = 30;
            fifty = 50;
            fiftyFive = 55;
            sixtySeven = 67;
            thirtyFive = 35;
        }
    }

    public static String isNormalPhone(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        String phoneSize;

        if (screenWidth >= 1080) {
            phoneSize = "Normal";
        } else if (screenWidth >= 720) {
            phoneSize = "Medium";
        } else {
            phoneSize = "Small";
        }

        return phoneSize;
    }
}

