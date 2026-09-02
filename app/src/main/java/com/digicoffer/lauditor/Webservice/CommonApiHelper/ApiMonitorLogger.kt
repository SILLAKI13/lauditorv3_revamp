package com.digicoffer.lauditor.Webservice.CommonApiHelper

import android.util.Log
import com.digicoffer.lauditor.Webservice.HttpResultDo
import org.json.JSONArray
import org.json.JSONObject

/**
 * Centralized API / APP Monitoring logger.
 * Produces structured visual log blocks in Logcat matching the standard LexiZ monitor format.
 * Searchable via "API_MONITOR" or "APP_MONITOR".
 */
object ApiMonitorLogger {
    const val TAG = "API_MONITOR"
    const val TAG_APP = "APP_MONITOR"
    private const val DIVIDER = "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

    private val SENSITIVE_KEYS = setOf(
        "password",
        "otp",
        "token",
        "access_token",
        "refresh_token",
        "authorization",
        "secret",
        "api_key",
        "apikey",
        "client_secret",
        "credentials"
    )

    private fun maskSensitiveData(input: String?): String {
        if (input.isNullOrBlank() || input == "none" || input == "null") return input ?: "none"
        val trimmed = input.trim()
        return try {
            when {
                trimmed.startsWith("{") -> {
                    val jsonObject = JSONObject(trimmed)
                    maskJsonObject(jsonObject)
                    jsonObject.toString()
                }
                trimmed.startsWith("[") -> {
                    val jsonArray = JSONArray(trimmed)
                    maskJsonArray(jsonArray)
                    jsonArray.toString()
                }
                else -> maskRegex(trimmed)
            }
        } catch (e: Exception) {
            maskRegex(trimmed)
        }
    }

    private fun maskJsonObject(obj: JSONObject) {
        val keys = obj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val lowerKey = key.lowercase(java.util.Locale.ROOT)
            val isSensitive = SENSITIVE_KEYS.any { lowerKey.contains(it) }

            val value = obj.opt(key)
            if (isSensitive && value is String && value.isNotEmpty()) {
                obj.put(key, "***")
            } else if (value is JSONObject) {
                maskJsonObject(value)
            } else if (value is JSONArray) {
                maskJsonArray(value)
            }
        }
    }

    private fun maskJsonArray(arr: JSONArray) {
        for (i in 0 until arr.length()) {
            val item = arr.opt(i)
            if (item is JSONObject) {
                maskJsonObject(item)
            } else if (item is JSONArray) {
                maskJsonArray(item)
            }
        }
    }

    private fun maskRegex(text: String): String {
        var result = text
        for (k in SENSITIVE_KEYS) {
            result = result.replace(Regex("(?i)(\"${k}\"\\s*:\\s*\")[^\"]*(\")"), "$1***$2")
            result = result.replace(Regex("(?i)(${k}=)[^&\\s,]*"), "$1***")
        }
        return result
    }

    @JvmStatic
    fun logSending(
        requestType: String,
        url: String,
        method: String,
        params: String? = null
    ) {
        try {
            val formattedParams = if (!params.isNullOrEmpty() && params != "null") maskSensitiveData(params) else "none"
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

            val formattedParams = if (!params.isNullOrEmpty() && params != "null") maskSensitiveData(params) else "none"
            val responseContent = if (!httpResult.responseContent.isNullOrEmpty()) maskSensitiveData(httpResult.responseContent) else "none"

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
