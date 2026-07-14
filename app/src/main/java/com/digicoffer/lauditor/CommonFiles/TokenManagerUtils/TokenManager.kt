package com.digicoffer.lauditor.CommonFiles.TokenManagerUtils

import android.content.Context
import android.util.Log
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants

class TokenManager {
    companion object {
        private const val PREF_NAME = "MyPrefs"

        @JvmStatic
        fun getAccessToken(): String? {
            return Constants.TOKEN
        }

        @JvmStatic
        fun getRefreshToken(): String? {
            return Constants.Refresh_token
        }

        @JvmStatic
        fun saveTokens(context: Context, accessToken: String?, refreshToken: String?) {
            Constants.TOKEN = accessToken
            Constants.Refresh_token = refreshToken

            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putString("Token", accessToken)
                .putString("refresh_token", Constants.Refresh_token)
                .apply()
        }

        @JvmStatic
        fun clearTokens(context: Context) {
            Constants.TOKEN = ""
            Constants.Refresh_token = ""

            val prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            prefs.edit()
                .remove("Token")
                .remove("Json_key")
                .putBoolean("session_expired", true)
                .apply()

            Log.d("TokenManager", "Tokens cleared — session_expired=true written to prefs")
        }

        @JvmStatic
        fun hasValidTokens(): Boolean {
            return !Constants.TOKEN.isNullOrEmpty() && !Constants.Refresh_token.isNullOrEmpty()
        }
    }
}
