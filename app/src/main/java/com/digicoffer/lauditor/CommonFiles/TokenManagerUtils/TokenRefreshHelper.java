package com.digicoffer.lauditor.CommonFiles.TokenManagerUtils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;

import com.digicoffer.lauditor.LoginActivity.ViewModels.LoginActivity;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.HttpExecuteTask;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TokenRefreshHelper implements AsyncTaskCompleteListener {

    private static final String TAG = "TokenRefreshHelper";

    // ── Queue state ────────────────────────────────────────────────────────────
    private static boolean isRefreshing = false;

    private static final List<PendingRetry> retryQueue = new ArrayList<>();

    private static class PendingRetry {
        final RetryRequest request;
        final AsyncTaskCompleteListener callback;

        PendingRetry(RetryRequest request, AsyncTaskCompleteListener callback) {
            this.request = request;
            this.callback = callback;
        }
    }

    // ── Entry point — called from HttpExecuteTask on every 401 ────────────────

    public static synchronized void handleUnauthorized(
            Context context,
            RetryRequest originalRequest,
            AsyncTaskCompleteListener originalCallback) {

        Log.d(TAG, "401 received for: " + originalRequest.requestType);

        // Always enqueue the failed request
        retryQueue.add(new PendingRetry(originalRequest, originalCallback));
        Log.d(TAG, "Queued: " + originalRequest.requestType
                + " | queue size: " + retryQueue.size());

        if (isRefreshing) {
            // Refresh already in flight — just wait, it will drain the queue
            Log.d(TAG, "Refresh already in progress — waiting in queue");
            return;
        }

        // First 401 — kick off the refresh
        isRefreshing = true;
        refreshToken(context, new OnRefreshResultListener() {
            @Override
            public void onRefreshSuccess() {
                Log.d(TAG, "Token refreshed — draining queue: " + retryQueue.size() + " requests");
                drainQueue(context);
            }

            @Override
            public void onRefreshFailed() {
                Log.d(TAG, "Token refresh failed — clearing queue and redirecting to login");
                clearQueue();
                TokenManager.clearTokens(context);
                redirectToLogin(context);
            }
        });
    }

    // ── Drain all queued requests with the new token ───────────────────────────

    private static synchronized void drainQueue(Context context) {
        List<PendingRetry> snapshot = new ArrayList<>(retryQueue);
        retryQueue.clear();
        isRefreshing = false;

        Log.d(TAG, "Draining " + snapshot.size() + " queued requests");

        for (PendingRetry pending : snapshot) {
            Log.d(TAG, "Retrying queued: " + pending.request.requestType);
            retryOriginalRequest(context, pending.request, pending.callback);
        }
    }

    // ── Clear queue without retrying (on refresh failure) ─────────────────────

    private static synchronized void clearQueue() {
        Log.d(TAG, "Clearing " + retryQueue.size() + " queued requests (refresh failed)");
        retryQueue.clear();
        isRefreshing = false;
    }

    // ── Refresh token API call ─────────────────────────────────────────────────

    private static void refreshToken(Context context, OnRefreshResultListener listener) {
        try {
            // Prefer TokenManager, fall back to Constants
            String refreshToken = TokenManager.getRefreshToken();
            if (refreshToken == null || refreshToken.isEmpty()) {
                refreshToken = Constants.Refresh_token;
            }

            if (refreshToken == null || refreshToken.isEmpty()) {
                Log.e(TAG, "No refresh token available — cannot refresh");
                listener.onRefreshFailed();
                return;
            }

            JSONObject body = new JSONObject();
            body.put("refresh_token", refreshToken);

            String refreshUrl = "refresh/token";

            AsyncTaskCompleteListener refreshCallback = new AsyncTaskCompleteListener() {
                @Override
                public void onAsyncTaskComplete(HttpResultDo httpResult) {
                    try {
                        String responseContent = httpResult.getResponseContent();

                        if (responseContent == null || responseContent.trim().isEmpty()) {
                            Log.e(TAG, "Refresh response null/empty — status: "
                                    + httpResult.getStatus_code());
                            listener.onRefreshFailed();
                            return;
                        }

                        if (httpResult.getStatus_code() == 200) {
                            JSONObject result = new JSONObject(responseContent);
                            if (!result.optBoolean("error", false)) {
                                JSONObject data = result.optJSONObject("data");
                                if (data != null) {
                                    String newAccess = data.optString("access_token");
                                    String newRefresh = data.optString("refresh_token",
                                            TokenManager.getRefreshToken());

                                    // Update in-memory tokens
                                    Constants.TOKEN = newAccess;
                                    Constants.Refresh_token = newRefresh;
                                    Log.d("Refresh_Token", Constants.Refresh_token);
                                    // Persist to TokenManager
                                    TokenManager.saveTokens(context, newAccess, newRefresh);

                                    // Persist to SharedPreferences
                                    SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                                    prefs.edit()
                                            .putString("Token", newAccess)
                                            .putString("refresh_token", Constants.Refresh_token)
                                            .apply();

                                    Log.d(TAG, "Token saved — refresh success");
                                    listener.onRefreshSuccess();
                                    return;
                                }
                            }
                        }

                        Log.e(TAG, "Refresh failed — status: " + httpResult.getStatus_code());
                        listener.onRefreshFailed();

                    } catch (Exception e) {
                        Log.e(TAG, "Exception in refresh callback: " + e.getMessage());
                        listener.onRefreshFailed();
                    }
                }

                @Override
                public void onClick(View view) {
                }
            };

            HttpExecuteTask task = new HttpExecuteTask(
                    "refresh_token_internal",
                    false,
                    WebServiceHelper.RestMethodType.POST,
                    refreshUrl,
                    refreshCallback,
                    context,
                    "REFRESH_TOKEN_INTERNAL"
            );
            task.execute(body.toString());

        } catch (Exception e) {
            Log.e(TAG, "Failed to build refresh request: " + e.getMessage());
            listener.onRefreshFailed();
        }
    }

    // ── Retry a single request ─────────────────────────────────────────────────

    private static void retryOriginalRequest(
            Context context,
            RetryRequest req,
            AsyncTaskCompleteListener callback) {

        Log.d(TAG, "Retrying: " + req.requestType);

        HttpExecuteTask task = new HttpExecuteTask(
                req.requestId,
                false,
                req.methodType,
                req.fullUrl,
                callback,
                context,
                req.requestType
        );
        task.execute(req.body);
    }

    // ── Redirect to login ──────────────────────────────────────────────────────

    private static void redirectToLogin(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    // ── Unused — actual handling in anonymous class above ─────────────────────

    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {
    }

    @Override
    public void onClick(View view) {
    }

    // ── Listener interface ─────────────────────────────────────────────────────

    private interface OnRefreshResultListener {
        void onRefreshSuccess();

        void onRefreshFailed();
    }
}