package com.digicoffer.lauditor.CommonFiles.PushNotifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.digicoffer.lauditor.MainActivity;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

public class MyFirebaseMessagingService extends FirebaseMessagingService implements AsyncTaskCompleteListener {

    private static final String TAG = "FCM_Service";
    private static final String CHANNEL_ID = "lauditor_channel";
    private static final String CHANNEL_NAME = "Lauditor Notifications";
    private static final String PREFS_NAME = "MyPrefs";
    private static final String TOKEN_KEY = "fcm_token";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "New FCM Token Generated: " + token);
        saveTokenLocally(token);
        sendTokenToServer(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = "";
        String body = "";
        String navigation = "";

        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle() != null
                    ? remoteMessage.getNotification().getTitle() : "";
            body = remoteMessage.getNotification().getBody() != null
                    ? remoteMessage.getNotification().getBody() : "";
        }

        if (!remoteMessage.getData().isEmpty()) {
            if (remoteMessage.getData().get("title") != null) {
                title = remoteMessage.getData().get("title");
            }
            if (remoteMessage.getData().get("body") != null) {
                body = remoteMessage.getData().get("body");
            }
            if (remoteMessage.getData().get("navigation") != null) {
                navigation = remoteMessage.getData().get("navigation");
            }
        }
        // Push notification for chat disabled — skip if this is a chat notification
        try {
            if (navigation != null && !navigation.isEmpty()) {
                JSONObject navJson = new JSONObject(navigation);
                String routeName = navJson.optString("route_name", "");
                if ("message_client_inbox".equalsIgnoreCase(routeName)) {
                    Log.d(TAG, "onMessageReceived — chat notification suppressed");
                    return;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "onMessageReceived — failed to parse navigation JSON: " + e.getMessage());
        }

        if (!title.isEmpty() || !body.isEmpty()) {
            showNotification(title, body, navigation);
        }
    }
//    @Override
//    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
//        super.onMessageReceived(remoteMessage);
//
//        String title = "";
//        String body = "";
//        String navigation = ""; // ← the full navigation JSON string
//
//        if (remoteMessage.getNotification() != null) {
//            title = remoteMessage.getNotification().getTitle() != null
//                    ? remoteMessage.getNotification().getTitle() : "";
//            body = remoteMessage.getNotification().getBody() != null
//                    ? remoteMessage.getNotification().getBody() : "";
//        }
//
//        if (!remoteMessage.getData().isEmpty()) {
//            if (remoteMessage.getData().get("title") != null) {
//                title = remoteMessage.getData().get("title");
//            }
//            if (remoteMessage.getData().get("body") != null) {
//                body = remoteMessage.getData().get("body");
//            }
//            // ── Pull the full navigation JSON string from data payload ─
//            if (remoteMessage.getData().get("navigation") != null) {
//                navigation = remoteMessage.getData().get("navigation");
//            }
//        }
//
//        if (!title.isEmpty() || !body.isEmpty()) {
//            showNotification(title, body, navigation); // ← 3 args now
//        }
//    }

    // ── Updated to 3 params — navigation replaces screen + itemId ────────
    private void showNotification(String title, String body, String navigation) {

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        // Pass the full navigation JSON — MainActivity will parse it
        if (navigation != null && !navigation.isEmpty()) {
            intent.putExtra("fcm_navigation", navigation);
        }

        int requestCode = (int) System.currentTimeMillis();
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (manager == null) {
            Log.e(TAG, "NotificationManager is null — cannot show notification");
            return;
        }

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("Push notifications for LexiZLawyers App");
        channel.enableLights(true);
        channel.setLightColor(Color.GREEN);
        channel.enableVibration(true);
        channel.setVibrationPattern(new long[]{0, 500, 200, 500});
        channel.setShowBadge(true);
        channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        manager.createNotificationChannel(channel);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(body)
                                .setBigContentTitle(title))
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setSound(soundUri)
                        .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                        .setLights(Color.GREEN, 1000, 500)
                        .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                        .setContentIntent(pendingIntent);

        int notificationId = (int) System.currentTimeMillis();
        manager.notify(notificationId, builder.build());

        Log.d(TAG, "Notification displayed with ID: " + notificationId);
    }

    private void saveTokenLocally(String token) {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(TOKEN_KEY, token)
                .apply();
        Log.d(TAG, "FCM Token saved to SharedPreferences ✅");
    }

    private void sendTokenToServer(String token) {
        try {
            JSONObject postData = new JSONObject();
            postData.put("userId", Constants.USER_ID);
            postData.put("fcmToken", token);
            postData.put("platform", "android");

            // ✅ Correct parameter order for your WebServiceHelper:
            // callHttpWebService(callback, context, methodType, url, requestType, body)
            WebServiceHelper.callHttpWebService(
                    null, this,                                      // context
                    WebServiceHelper.RestMethodType.POST,      // REST method
                    Constants.Notification_Base_Url + "/register-token",                       // ← Replace with your actual API endpoint
                    "FCM_TOKEN_UPDATE",                        // request type tag — handled in onAsyncTaskComplete()
                    postData.toString()                        // POST body as JSON string
            );

            Log.d("FCM", "FCM Token sent to backend ✅");

        } catch (Exception e) {
            Log.e("FCM", "Failed to send FCM token to backend: "
                    + e.getMessage());
            e.printStackTrace();
        }
    }

    // Add this method to MyFirebaseMessagingService class
    public static void clearAllNotifications(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancelAll();
            Log.d(TAG, "All notifications cleared from notification center ✅");
        }
    }

    public static void logoutToken(Context context, String token) {
        try {
            JSONObject postData = new JSONObject();
            postData.put("fcmToken", token);

            WebServiceHelper.callHttpWebService(
                    null, context,
                    WebServiceHelper.RestMethodType.POST,
                    Constants.Notification_Base_Url + "/logout-token",
                    "FCM_TOKEN_LOGOUT",
                    postData.toString()
            );

            Log.d("FCM", "Logout token sent to backend ✅");
        } catch (Exception e) {
            Log.e("FCM", "Failed to send logout token to backend: " + e.getMessage());
        }
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
        Log.d(TAG, "Messages deleted from FCM server");
    }

    @Override
    public void onSendError(@NonNull String msgId, @NonNull Exception exception) {
        super.onSendError(msgId, exception);
        Log.e(TAG, "Message send error — ID: " + msgId + " Error: " + exception.getMessage());
    }

    @Override
    public void onMessageSent(@NonNull String msgId) {
        super.onMessageSent(msgId);
        Log.d(TAG, "Message sent successfully — ID: " + msgId);
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {
        try {
            if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Success) {

                JSONObject result = new JSONObject(httpResult.getResponseContent());
                if (httpResult.getRequestType().equals("FCM_TOKEN_UPDATE")) {
                    boolean error = result.optBoolean("error");
                    String msg = result.optString("message");
                    Log.d("FCM_TOKEN_UPDATE", msg);
                }
            }
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }
}