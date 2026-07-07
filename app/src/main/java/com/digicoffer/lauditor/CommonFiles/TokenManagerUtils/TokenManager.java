package com.digicoffer.lauditor.CommonFiles.TokenManagerUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;

public class TokenManager {

    private static final String PREF_NAME = "MyPrefs";

    public static String getAccessToken() {
        return Constants.TOKEN;
    }

    public static String getRefreshToken() {
        return Constants.Refresh_token;
    }

    public static void saveTokens(Context context, String accessToken, String refreshToken) {
        Constants.TOKEN = accessToken;
        Constants.Refresh_token = refreshToken;

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString("Token", accessToken)
                .putString("refresh_token", Constants.Refresh_token)
                .apply();
    }

    // TokenManager.java
    public static void clearTokens(Context context) {
        Constants.TOKEN = "";
        Constants.Refresh_token = "";

        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        prefs.edit()
                .remove("Token")
                .remove("Json_key")
                .putBoolean("session_expired", true)
                .apply();

        Log.d("TokenManager", "Tokens cleared — session_expired=true written to prefs");
    }

    public static boolean hasValidTokens() {
        return Constants.TOKEN != null && !Constants.TOKEN.isEmpty()
                && Constants.Refresh_token != null && !Constants.Refresh_token.isEmpty();
    }
}
