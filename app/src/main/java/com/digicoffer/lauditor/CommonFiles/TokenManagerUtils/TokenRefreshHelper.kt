package com.digicoffer.lauditor.CommonFiles.TokenManagerUtils

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import com.digicoffer.lauditor.LoginActivity.ViewModels.LoginActivity
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.CommonApiHelper.HttpExecuteTask
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import org.json.JSONObject

class TokenRefreshHelper : AsyncTaskCompleteListener {

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {}

    override fun onClick(view: View) {}

    private class PendingRetry(
        val request: RetryRequest,
        val callback: AsyncTaskCompleteListener
    )

    private interface OnRefreshResultListener {
        fun onRefreshSuccess()
        fun onRefreshFailed()
    }

    companion object {
        private const val TAG = "TokenRefreshHelper"

        private var isRefreshing = false
        private val retryQueue = ArrayList<PendingRetry>()

        @JvmStatic
        @Synchronized
        fun handleUnauthorized(
            context: Context?,
            originalRequest: RetryRequest?,
            originalCallback: AsyncTaskCompleteListener?
        ) {
            if (context == null || originalRequest == null || originalCallback == null) return
            Log.d(TAG, "401 received for: ${originalRequest.requestType}")

            retryQueue.add(PendingRetry(originalRequest, originalCallback))
            Log.d(TAG, "Queued: ${originalRequest.requestType} | queue size: ${retryQueue.size}")

            if (isRefreshing) {
                Log.d(TAG, "Refresh already in progress — waiting in queue")
                return
            }

            isRefreshing = true
            refreshToken(context, object : OnRefreshResultListener {
                override fun onRefreshSuccess() {
                    Log.d(TAG, "Token refreshed — draining queue: ${retryQueue.size} requests")
                    drainQueue(context)
                }

                override fun onRefreshFailed() {
                    Log.d(TAG, "Token refresh failed — clearing queue and redirecting to login")
                    clearQueue()
                    TokenManager.clearTokens(context)
                    redirectToLogin(context)
                }
            })
        }

        @JvmStatic
        @Synchronized
        private fun drainQueue(context: Context) {
            val snapshot = ArrayList(retryQueue)
            retryQueue.clear()
            isRefreshing = false

            Log.d(TAG, "Draining ${snapshot.size} queued requests")

            for (pending in snapshot) {
                Log.d(TAG, "Retrying queued: ${pending.request.requestType}")
                retryOriginalRequest(context, pending.request, pending.callback)
            }
        }

        @JvmStatic
        @Synchronized
        private fun clearQueue() {
            Log.d(TAG, "Clearing ${retryQueue.size} queued requests (refresh failed)")
            retryQueue.clear()
            isRefreshing = false
        }

        @JvmStatic
        private fun refreshToken(context: Context, listener: OnRefreshResultListener) {
            try {
                var refreshToken = TokenManager.getRefreshToken()
                if (refreshToken.isNullOrEmpty()) {
                    refreshToken = Constants.Refresh_token
                }

                if (refreshToken.isNullOrEmpty()) {
                    Log.e(TAG, "No refresh token available — cannot refresh")
                    listener.onRefreshFailed()
                    return
                }

                val body = JSONObject()
                body.put("refresh_token", refreshToken)

                val refreshUrl = "refresh/token"

                val refreshCallback = object : AsyncTaskCompleteListener {
                    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                        try {
                            val responseContent = httpResult.responseContent

                            if (responseContent.isNullOrEmpty()) {
                                Log.e(TAG, "Refresh response null/empty — status: ${httpResult.status_code}")
                                listener.onRefreshFailed()
                                return
                            }

                            if (httpResult.status_code == 200) {
                                val result = JSONObject(responseContent)
                                if (!result.optBoolean("error", false)) {
                                    val data = result.optJSONObject("data")
                                    if (data != null) {
                                        val newAccess = data.optString("access_token")
                                        val newRefresh = data.optString("refresh_token", TokenManager.getRefreshToken())

                                        // Update in-memory tokens
                                        Constants.TOKEN = newAccess
                                        Constants.Refresh_token = newRefresh
                                        Log.d("Refresh_Token", Constants.Refresh_token ?: "")
                                        // Persist to TokenManager
                                        TokenManager.saveTokens(context, newAccess, newRefresh)

                                        // Persist to SharedPreferences
                                        val prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                                        prefs.edit()
                                            .putString("Token", newAccess)
                                            .putString("refresh_token", Constants.Refresh_token)
                                            .apply()

                                        Log.d(TAG, "Token saved — refresh success")
                                        listener.onRefreshSuccess()
                                        return
                                    }
                                }
                            }

                            Log.e(TAG, "Refresh failed — status: ${httpResult.status_code}")
                            listener.onRefreshFailed()

                        } catch (e: Exception) {
                            Log.e(TAG, "Exception in refresh callback: ${e.message}")
                            listener.onRefreshFailed()
                        }
                    }

                    override fun onClick(view: View) {}
                }

                val task = HttpExecuteTask(
                    "refresh_token_internal",
                    false,
                    WebServiceHelper.RestMethodType.POST,
                    refreshUrl,
                    refreshCallback,
                    context,
                    "REFRESH_TOKEN_INTERNAL"
                )
                task.execute(body.toString())

            } catch (e: Exception) {
                Log.e(TAG, "Failed to build refresh request: ${e.message}")
                listener.onRefreshFailed()
            }
        }

        @JvmStatic
        private fun retryOriginalRequest(
            context: Context,
            req: RetryRequest,
            callback: AsyncTaskCompleteListener
        ) {
            Log.d(TAG, "Retrying: ${req.requestType}")

            val task = HttpExecuteTask(
                req.requestId,
                false,
                req.methodType,
                req.fullUrl,
                callback,
                context,
                req.requestType
            )
            task.isRetry = true
            task.execute(req.body)
        }

        @JvmStatic
        private fun redirectToLogin(context: Context) {
            val intent = Intent(context, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(intent)
        }
    }
}
