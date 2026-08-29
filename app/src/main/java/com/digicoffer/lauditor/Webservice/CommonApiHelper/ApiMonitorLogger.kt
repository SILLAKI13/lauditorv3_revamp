package com.digicoffer.lauditor.Webservice.CommonApiHelper

import android.util.Log
import com.digicoffer.lauditor.Webservice.HttpResultDo

/**
 * Centralized API / APP Monitoring logger.
 * Produces structured visual log blocks in Logcat matching the standard LexiZ monitor format.
 * Searchable via "API_MONITOR" or "APP_MONITOR".
 */
object ApiMonitorLogger {
    const val TAG = "API_MONITOR"
    const val TAG_APP = "APP_MONITOR"
    private const val DIVIDER = "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

    @JvmStatic
    fun logSending(
        requestType: String,
        url: String,
        method: String,
        params: String? = null
    ) {
        try {
            val formattedParams = if (!params.isNullOrEmpty() && params != "null") params else "none"
            val logMessage = buildString {
                appendLine(DIVIDER)
                appendLine("🔄  REQUEST  : $requestType")
                appendLine("   URL      : $url")
                appendLine("   METHOD   : $method")
                appendLine("   PARAMS   : $formattedParams")
                appendLine("   RESULT   : SENDING")
                append(DIVIDER)
            }
            logChunked(TAG, logMessage)
            logChunked(TAG_APP, logMessage)
        } catch (e: Exception) {
            Log.e(TAG, "Error logging API sending", e)
        }
    }

    @JvmStatic
    fun logResponse(
        requestType: String,
        url: String,
        params: String? = null,
        httpResult: HttpResultDo
    ) {
        try {
            val statusEmoji = when {
                httpResult.result == WebServiceHelper.ServiceCallStatus.Success -> "✅"
                httpResult.result == WebServiceHelper.ServiceCallStatus.Exception -> "💥"
                else -> "❌"
            }

            val formattedParams = if (!params.isNullOrEmpty() && params != "null") params else "none"
            val responseContent = httpResult.responseContent ?: "none"

            val logMessage = buildString {
                appendLine(DIVIDER)
                appendLine("$statusEmoji  REQUEST  : $requestType")
                appendLine("   URL      : $url")
                appendLine("   PARAMS   : $formattedParams")
                appendLine("   STATUS   : ${httpResult.status_code}")
                appendLine("   RESULT   : ${httpResult.result}")
                appendLine("   RESPONSE : $responseContent")
                if (!httpResult.errorMessage.isNullOrEmpty()) {
                    appendLine("   ERROR    : ${httpResult.errorMessage}")
                }
                append(DIVIDER)
            }

            if (httpResult.result == WebServiceHelper.ServiceCallStatus.Exception ||
                httpResult.result == WebServiceHelper.ServiceCallStatus.Failed) {
                logChunked(TAG, logMessage, isError = true)
                logChunked(TAG_APP, logMessage, isError = true)
            } else {
                logChunked(TAG, logMessage, isError = false)
                logChunked(TAG_APP, logMessage, isError = false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error logging API response", e)
        }
    }

    private fun logChunked(tag: String, message: String, isError: Boolean = false) {
        val maxLogSize = 3500
        for (line in message.split("\n")) {
            if (line.length > maxLogSize) {
                var i = 0
                while (i < line.length) {
                    val end = minOf(line.length, i + maxLogSize)
                    val chunk = line.substring(i, end)
                    if (isError) Log.e(tag, chunk) else Log.d(tag, chunk)
                    i += maxLogSize
                }
            } else {
                if (isError) Log.e(tag, line) else Log.d(tag, line)
            }
        }
    }
}
