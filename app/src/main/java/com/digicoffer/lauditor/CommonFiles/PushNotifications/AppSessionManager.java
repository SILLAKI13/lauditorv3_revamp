package com.digicoffer.lauditor.CommonFiles.PushNotifications;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.Nullable;

import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * AppSessionManager — Single Responsibility: Session bootstrap + FCM token lifecycle.
 * <p>
 * DESIGN RULES:
 * 1. Always called from LoginActivity — never from MainActivity.
 * 2. LoginActivity is ALWAYS the launch Activity (check AndroidManifest).
 * 3. On notification tap (killed app), Android re-launches LoginActivity first,
 * which calls restoreSessionAndNavigate() → auto-login → then MainActivity
 * handles the navigation extra normally via handleNotificationNavigation().
 * 4. MainActivity only calls registerPendingFCMToken() ONCE after login completes.
 */
public class AppSessionManager {

    private static final String TAG = "AppSessionManager";
    private static final String PREFS_NAME = "MyPrefs";
    private static final String FCM_KEY = "fcm_token";
    private static final String TOKEN_KEY = "Token";
    private static final String JSON_KEY = "Json_key";
    private static final String EMAIL_KEY = "email";
    private static boolean pendingNavExtracted = false;

    // ─────────────────────────────────────────────────────────────────────────
    // 1.  SESSION CHECK — called in LoginActivity.onResume()
    //     Returns true  → valid saved session found, caller should auto-login.
    //     Returns false → no session, show normal login UI.
    // ─────────────────────────────────────────────────────────────────────────
    public static boolean hasValidSession(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String token = prefs.getString(TOKEN_KEY, "");
        String json = prefs.getString(JSON_KEY, "");
        String email = prefs.getString(EMAIL_KEY, "");
        boolean valid = !token.isEmpty() && !json.isEmpty() && !email.isEmpty();
        Log.d(TAG, "hasValidSession=" + valid);
        return valid;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2.  RESTORE SESSION DATA INTO Constants — called before Dashboard() API.
    //     Returns false if the stored JSON is corrupt (forces fresh login).
    // ─────────────────────────────────────────────────────────────────────────
    public static boolean restoreSession(Context ctx) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedToken = prefs.getString(TOKEN_KEY, "");
        String responseJson = prefs.getString(JSON_KEY, "");

        if (savedToken.isEmpty() || responseJson.isEmpty()) return false;

        try {
            JSONObject userJson = new JSONObject(responseJson);

            Constants.TOKEN = savedToken;
            Constants.NAME = userJson.getString("name");
            Constants.NAME_NEW = userJson.getString("name");
            Constants.USER_ID = userJson.getString("user_id");
            Constants.UID = userJson.getString("uid");
            Constants.PK = userJson.getString("pk");
            Constants.PASSWORD_MODE = userJson.getString("password_mode");
            Constants.IS_ADMIN = userJson.getBoolean("admin");
            Constants.FIRM_NAME = userJson.getString("firm_name");
            Constants.ROLE = userJson.getString("role");
            Constants.Groups = userJson.getJSONArray("groups");
            Constants.CATEGORY = userJson.optString("category");
            Constants.FirmEmail = userJson.optString("email");
            Constants.Email = prefs.getString(EMAIL_KEY, "");

            // Subscription / features
            JSONObject subscription = userJson.optJSONObject("subscription");
            if (subscription != null) {
                Constants.is_active = subscription.optBoolean("is_active");
                JSONObject features = subscription.optJSONObject("features");
                if (features != null) {
                    Constants.FEATURES.clear();
                    Iterator<String> keys = features.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        Constants.FEATURES.put(key, features.optBoolean(key, false));
                    }
                }
            }

            // Firms list
            String namesStr = prefs.getString("firmNames", "[]");
            String idsStr = prefs.getString("firmIds", "[]");
            JSONArray namesArr = new JSONArray(namesStr);
            JSONArray idsArr = new JSONArray(idsStr);
            Constants.Firm_names.clear();
            Constants.Firm_ids.clear();
            for (int i = 0; i < namesArr.length(); i++)
                Constants.Firm_names.add(namesArr.getString(i));
            for (int i = 0; i < idsArr.length(); i++) Constants.Firm_ids.add(idsArr.getString(i));

            // Admin flag
            Constants.isAdmin = false;
            if ("AAM".equals(Constants.ROLE)) {
                Constants.isAdmin = true;
            } else if (Constants.Groups.length() == 1
                    && "AAM".equals(Constants.Groups.getString(0))) {
                Constants.isAdmin = true;
            }

            Log.d(TAG, "Session restored for: " + Constants.Email);
            return true;

        } catch (JSONException e) {
            Log.e(TAG, "restoreSession — corrupt JSON, clearing prefs: " + e.getMessage());
            clearSession(ctx);
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3.  SAVE SESSION — called after every successful OTP login / firm switch.
    // ─────────────────────────────────────────────────────────────────────────
    public static void saveSession(Context ctx, String token, JSONObject userJson) {
        try {
            SharedPreferences.Editor ed =
                    ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
            ed.putString(TOKEN_KEY, token);
            ed.putString(JSON_KEY, userJson.toString());
            ed.putString(EMAIL_KEY, Constants.Email.toLowerCase());
            ed.putBoolean("Check_box", true);

            JSONArray firmNamesArr = new JSONArray(Constants.Firm_names);
            JSONArray firmIdsArr = new JSONArray(Constants.Firm_ids);
            ed.putString("firmNames", firmNamesArr.toString());
            ed.putString("firmIds", firmIdsArr.toString());
            ed.apply();

            Log.d(TAG, "Session saved ✅");
        } catch (Exception e) {
            Log.e(TAG, "saveSession failed: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4.  CLEAR SESSION — called on logout.
    // ─────────────────────────────────────────────────────────────────────────
    public static void clearSession(Context ctx) {
        ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply();
        ctx.getSharedPreferences("BIO", Context.MODE_PRIVATE).edit().clear().apply();
        PreferenceManager.getDefaultSharedPreferences(ctx).edit()
                .remove("xmpp_jid").remove("xmpp_password").remove("xmpp_logged_in")
                .remove("EXTRA_CONTACT_JID").remove("CURRENTCHAT_JID").apply();
        Constants.TOKEN = "";
        Constants.Email = "";
        Constants.USER_ID = "";
        Log.d(TAG, "Session cleared");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5.  FCM TOKEN — fetch, store, and send to backend.
    //     Called from LoginActivity.onCreate() for fresh installs / token refresh.
    //     Called from MainActivity.registerPendingFCMToken() post-login.
    // ─────────────────────────────────────────────────────────────────────────
    public static void fetchAndRegisterFCMToken(Context ctx,
                                                @Nullable FCMTokenCallback callback) {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "FCM token fetch FAILED: " + task.getException());
                return;
            }
            String newToken = task.getResult();
            SharedPreferences prefs =
                    ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String savedToken = prefs.getString(FCM_KEY, "");

            // Always persist locally
            prefs.edit().putString(FCM_KEY, newToken).apply();

            boolean tokenChanged = !newToken.equals(savedToken);
            boolean userLoggedIn = Constants.USER_ID != null && !Constants.USER_ID.isEmpty();

            if (userLoggedIn && tokenChanged) {
                Log.d(TAG, "FCM token changed — sending to backend");
                if (callback != null) callback.onTokenReady(newToken);
            } else if (!userLoggedIn) {
                Log.d(TAG, "USER_ID not ready — token saved, will register after login");
            } else {
                Log.d(TAG, "FCM token unchanged — skip backend call");
            }
        });
    }

    /**
     * Called by MainActivity ONCE after login succeeds and USER_ID is populated.
     * Sends the locally-saved FCM token to the backend if not yet registered.
     */
    public static void registerPendingFCMToken(Context ctx,
                                               @Nullable FCMTokenCallback callback) {
        SharedPreferences prefs =
                ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedToken = prefs.getString(FCM_KEY, "");
        if (!savedToken.isEmpty()
                && Constants.USER_ID != null
                && !Constants.USER_ID.isEmpty()) {
            Log.d(TAG, "Registering pending FCM token post-login ✅");
            if (callback != null) callback.onTokenReady(savedToken);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 6.  NOTIFICATION NAVIGATION — extract nav JSON from the launch Intent.
    //     LoginActivity stores it in Constants; MainActivity reads and executes it.
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Call in LoginActivity.onCreate() / onNewIntent().
     * Extracts and CACHES the navigation payload so it survives the
     * LoginActivity → MainActivity transition.
     * <p>
     * Returns the raw navigation JSON string, or "" if none present.
     */
    public static String extractAndCachePendingNavigation(Intent intent) {
        if (intent == null) return "";
        if (pendingNavExtracted) return Constants.pendingNotificationNavJson; // already done

        String navJson = intent.getStringExtra("fcm_navigation");
        if (navJson == null || navJson.isEmpty()) {
            navJson = intent.getStringExtra("navigation");
        }
        if (navJson == null || navJson.isEmpty()) return "";

        intent.removeExtra("fcm_navigation");
        intent.removeExtra("navigation");

        Constants.pendingNotificationNavJson = navJson;
        pendingNavExtracted = true; // ← mark extracted so onResume can't re-cache
        Log.d(TAG, "Pending navigation cached (once): " + navJson);
        return navJson;
    }

    public static void resetPendingNavigation() {
        pendingNavExtracted = false;
        Constants.pendingNotificationNavJson = "";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Callback interface for FCM token events
    // ─────────────────────────────────────────────────────────────────────────
    public interface FCMTokenCallback {
        void onTokenReady(String token);
    }
}