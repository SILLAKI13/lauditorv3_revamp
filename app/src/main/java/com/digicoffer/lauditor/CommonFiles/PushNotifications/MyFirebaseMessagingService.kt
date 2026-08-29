package com.digicoffer.lauditor.CommonFiles.PushNotifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.util.Log
import android.view.View
import androidx.core.app.NotificationCompat
import com.digicoffer.lauditor.MainActivity
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject

class MyFirebaseMessagingService : FirebaseMessagingService(), AsyncTaskCompleteListener {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Token Generated: $token")
        saveTokenLocally(token)
        sendTokenToServer(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        var title = ""
        var body = ""
        var navigation = ""

        remoteMessage.notification?.let {
            title = it.title ?: ""
            body = it.body ?: ""
        }

        if (remoteMessage.data.isNotEmpty()) {
            remoteMessage.data["title"]?.let { title = it }
            remoteMessage.data["body"]?.let { body = it }
            remoteMessage.data["navigation"]?.let { navigation = it }
        }

        try {
            if (navigation.isNotEmpty()) {
                val navJson = JSONObject(navigation)
                val routeName = navJson.optString("route_name", "")
                if ("message_client_inbox".equals(routeName, ignoreCase = true)) {
                    Log.d(TAG, "onMessageReceived — chat notification suppressed")
                    return
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "onMessageReceived — failed to parse navigation JSON: ${e.message}")
        }

        if (title.isNotEmpty() || body.isNotEmpty()) {
            showNotification(title, body, navigation)
        }
    }

    private fun showNotification(title: String, body: String, navigation: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                        Intent.FLAG_ACTIVITY_NEW_TASK
            )
        }

        if (!navigation.isNullOrEmpty()) {
            intent.putExtra("fcm_navigation", navigation)
        }

        val requestCode = System.currentTimeMillis().toInt()
        val pendingIntent = PendingIntent.getActivity(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

        if (manager == null) {
            Log.e(TAG, "NotificationManager is null — cannot show notification")
            return
        }

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Push notifications for Lex-Z Lawyers App"
            enableLights(true)
            lightColor = Color.GREEN
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 200, 500)
            setShowBadge(true)
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        }
        manager.createNotificationChannel(channel)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(body)
                    .setBigContentTitle(title)
            )
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSound(soundUri)
            .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
            .setLights(Color.GREEN, 1000, 500)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            .setContentIntent(pendingIntent)

        val notificationId = System.currentTimeMillis().toInt()
        manager.notify(notificationId, builder.build())

        Log.d(TAG, "Notification displayed with ID: $notificationId")
    }

    private fun saveTokenLocally(token: String) {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(TOKEN_KEY, token)
            .apply()
        Log.d(TAG, "FCM Token saved to SharedPreferences ✅")
    }

    private fun sendTokenToServer(token: String) {
        try {
            val postData = JSONObject().apply {
                put("userId", Constants.USER_ID)
                put("fcmToken", token)
                put("platform", "android")
            }

            WebServiceHelper.callHttpWebService(
                this,
                this,
                WebServiceHelper.RestMethodType.POST,
                Constants.Notification_Base_Url + "/register-token",
                "FCM_TOKEN_UPDATE",
                postData.toString()
            )

            Log.d("FCM", "FCM Token sent to backend ✅")
        } catch (e: Exception) {
            Log.e("FCM", "Failed to send FCM token to backend: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun onClick(view: View) {}

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        try {
            if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                val result = JSONObject(httpResult.responseContent ?: "")
                if ("FCM_TOKEN_UPDATE" == httpResult.requestType) {
                    val msg = result.optString("message")
                    Log.d("FCM_TOKEN_UPDATE", msg)
                }
            }
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    companion object {
        private const val TAG = "FCM_Service"
        private const val CHANNEL_ID = "lauditor_channel"
        private const val CHANNEL_NAME = "Lauditor Notifications"
        private const val PREFS_NAME = "MyPrefs"
        private const val TOKEN_KEY = "fcm_token"

        @JvmStatic
        fun clearAllNotifications(context: Context) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.let {
                it.cancelAll()
                Log.d(TAG, "All notifications cleared from notification center ✅")
            }
        }

        @JvmStatic
        fun logoutToken(context: Context, token: String) {
            try {
                val postData = JSONObject().apply {
                    put("fcmToken", token)
                }

                val dummyCallback = object : AsyncTaskCompleteListener {
                    override fun onClick(view: View) {}
                    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {}
                }
                WebServiceHelper.callHttpWebService(
                    dummyCallback,
                    context,
                    WebServiceHelper.RestMethodType.POST,
                    Constants.Notification_Base_Url + "/logout-token",
                    "FCM_TOKEN_LOGOUT",
                    postData.toString()
                )

                Log.d("FCM", "Logout token sent to backend ✅")
            } catch (e: Exception) {
                Log.e("FCM", "Failed to send logout token to backend: ${e.message}")
            }
        }
    }
}
