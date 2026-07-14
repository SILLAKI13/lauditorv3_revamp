package com.digicoffer.lauditor.CommonFiles.PushNotifications

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class AppSessionManager {

    interface FCMTokenCallback {
        fun onTokenReady(token: String)
    }

    companion object {
        private const val TAG = "AppSessionManager"
        private const val PREFS_NAME = "MyPrefs"
        private const val FCM_KEY = "fcm_token"
        private const val TOKEN_KEY = "Token"
        private const val JSON_KEY = "Json_key"
        private const val EMAIL_KEY = "email"
        private var pendingNavExtracted = false

        @JvmStatic
        fun hasValidSession(ctx: Context): Boolean {
            val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val token = prefs.getString(TOKEN_KEY, "") ?: ""
            val json = prefs.getString(JSON_KEY, "") ?: ""
            val email = prefs.getString(EMAIL_KEY, "") ?: ""
            val valid = token.isNotEmpty() && json.isNotEmpty() && email.isNotEmpty()
            Log.d(TAG, "hasValidSession=$valid")
            return valid
        }

        @JvmStatic
        fun restoreSession(ctx: Context): Boolean {
            val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedToken = prefs.getString(TOKEN_KEY, "") ?: ""
            val responseJson = prefs.getString(JSON_KEY, "") ?: ""

            if (savedToken.isEmpty() || responseJson.isEmpty()) return false

            return try {
                val userJson = JSONObject(responseJson)

                Constants.TOKEN = savedToken
                Constants.NAME = userJson.getString("name")
                Constants.NAME_NEW = userJson.getString("name")
                Constants.USER_ID = userJson.getString("user_id")
                Constants.UID = userJson.getString("uid")
                Constants.PK = userJson.getString("pk")
                Constants.PASSWORD_MODE = userJson.getString("password_mode")
                Constants.IS_ADMIN = userJson.getBoolean("admin")
                Constants.FIRM_NAME = userJson.getString("firm_name")
                Constants.ROLE = userJson.getString("role")
                Constants.Groups = userJson.getJSONArray("groups")
                Constants.CATEGORY = userJson.optString("category")
                Constants.FirmEmail = userJson.optString("email")
                Constants.Email = prefs.getString(EMAIL_KEY, "")

                val subscription = userJson.optJSONObject("subscription")
                if (subscription != null) {
                    Constants.is_active = subscription.optBoolean("is_active")
                    val features = subscription.optJSONObject("features")
                    if (features != null) {
                        Constants.FEATURES.clear()
                        val keys = features.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            Constants.FEATURES[key] = features.optBoolean(key, false)
                        }
                    }
                }

                val namesStr = prefs.getString("firmNames", "[]") ?: "[]"
                val idsStr = prefs.getString("firmIds", "[]") ?: "[]"
                val namesArr = JSONArray(namesStr)
                val idsArr = JSONArray(idsStr)
                Constants.Firm_names.clear()
                Constants.Firm_ids.clear()
                for (i in 0 until namesArr.length()) {
                    Constants.Firm_names.add(namesArr.getString(i))
                }
                for (i in 0 until idsArr.length()) {
                    Constants.Firm_ids.add(idsArr.getString(i))
                }

                Constants.isAdmin = false
                if ("AAM" == Constants.ROLE) {
                    Constants.isAdmin = true
                } else if (Constants.Groups?.length() == 1 && "AAM" == Constants.Groups?.getString(0)) {
                    Constants.isAdmin = true
                }

                Log.d(TAG, "Session restored for: ${Constants.Email}")
                true
            } catch (e: JSONException) {
                Log.e(TAG, "restoreSession — corrupt JSON, clearing prefs: ${e.message}")
                clearSession(ctx)
                false
            }
        }

        @JvmStatic
        fun saveSession(ctx: Context, token: String, userJson: JSONObject) {
            try {
                val ed = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
                ed.putString(TOKEN_KEY, token)
                ed.putString(JSON_KEY, userJson.toString())
                ed.putString(EMAIL_KEY, Constants.Email?.lowercase() ?: "")
                ed.putBoolean("Check_box", true)

                val firmNamesArr = JSONArray(Constants.Firm_names)
                val firmIdsArr = JSONArray(Constants.Firm_ids)
                ed.putString("firmNames", firmNamesArr.toString())
                ed.putString("firmIds", firmIdsArr.toString())
                ed.apply()

                Log.d(TAG, "Session saved ✅")
            } catch (e: Exception) {
                Log.e(TAG, "saveSession failed: ${e.message}")
            }
        }

        @JvmStatic
        fun clearSession(ctx: Context) {
            ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
            ctx.getSharedPreferences("BIO", Context.MODE_PRIVATE).edit().clear().apply()
            PreferenceManager.getDefaultSharedPreferences(ctx).edit()
                .remove("xmpp_jid").remove("xmpp_password").remove("xmpp_logged_in")
                .remove("EXTRA_CONTACT_JID").remove("CURRENTCHAT_JID").apply()
            Constants.TOKEN = ""
            Constants.Email = ""
            Constants.USER_ID = ""
            Log.d(TAG, "Session cleared")
        }

        @JvmStatic
        fun fetchAndRegisterFCMToken(ctx: Context, callback: FCMTokenCallback?) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.e(TAG, "FCM token fetch FAILED: ${task.exception}")
                    return@addOnCompleteListener
                }
                val newToken = task.result
                val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val savedToken = prefs.getString(FCM_KEY, "") ?: ""

                prefs.edit().putString(FCM_KEY, newToken).apply()

                val tokenChanged = newToken != savedToken
                val userLoggedIn = !Constants.USER_ID.isNullOrEmpty()

                if (userLoggedIn && tokenChanged) {
                    Log.d(TAG, "FCM token changed — sending to backend")
                    callback?.onTokenReady(newToken)
                } else if (!userLoggedIn) {
                    Log.d(TAG, "USER_ID not ready — token saved, will register after login")
                } else {
                    Log.d(TAG, "FCM token unchanged — skip backend call")
                }
            }
        }

        @JvmStatic
        fun registerPendingFCMToken(ctx: Context, callback: FCMTokenCallback?) {
            val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedToken = prefs.getString(FCM_KEY, "") ?: ""
            if (savedToken.isNotEmpty() && !Constants.USER_ID.isNullOrEmpty()) {
                Log.d(TAG, "Registering pending FCM token post-login ✅")
                callback?.onTokenReady(savedToken)
            }
        }

        @JvmStatic
        fun extractAndCachePendingNavigation(intent: Intent?): String {
            if (intent == null) return ""
            if (pendingNavExtracted) return Constants.pendingNotificationNavJson ?: ""

            var navJson = intent.getStringExtra("fcm_navigation")
            if (navJson.isNullOrEmpty()) {
                navJson = intent.getStringExtra("navigation")
            }
            if (navJson.isNullOrEmpty()) return ""

            intent.removeExtra("fcm_navigation")
            intent.removeExtra("navigation")

            Constants.pendingNotificationNavJson = navJson
            pendingNavExtracted = true
            Log.d(TAG, "Pending navigation cached (once): $navJson")
            return navJson
        }

        @JvmStatic
        fun resetPendingNavigation() {
            pendingNavExtracted = false
            Constants.pendingNotificationNavJson = ""
        }
    }
}
