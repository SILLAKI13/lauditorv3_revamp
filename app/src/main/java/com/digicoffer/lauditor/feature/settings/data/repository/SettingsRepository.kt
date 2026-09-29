package com.digicoffer.lauditor.feature.settings.data.repository

import android.content.Context
import android.view.View
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.Webservice.HttpResultDo
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import kotlin.coroutines.resume

class SettingsRepository(private val context: Context) {

    suspend fun fetchSettingsOverview(): HttpResultDo = suspendCancellableCoroutine { continuation ->
        try {
            WebServiceHelper.callHttpWebService(
                object : AsyncTaskCompleteListener {
                    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                        continuation.resume(httpResult)
                    }
                    override fun onClick(view: View) {}
                },
                context,
                WebServiceHelper.RestMethodType.GET,
                "v3/settings/overview",
                "SETTINGS_OVERVIEW",
                JSONObject().toString()
            )
        } catch (e: Exception) {
            val failedResult = HttpResultDo().apply {
                result = WebServiceHelper.ServiceCallStatus.Exception
                responseContent = e.message ?: ""
            }
            continuation.resume(failedResult)
        }
    }

    suspend fun changePassword(
        currentPass: String,
        newPass: String,
        confirmPass: String
    ): HttpResultDo = suspendCancellableCoroutine { continuation ->
        try {
            val payload = JSONObject().apply {
                put("current_password", currentPass)
                put("new_password", newPass)
                put("confirm_password", confirmPass)
            }
            WebServiceHelper.callHttpWebService(
                object : AsyncTaskCompleteListener {
                    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                        continuation.resume(httpResult)
                    }
                    override fun onClick(view: View) {}
                },
                context,
                WebServiceHelper.RestMethodType.POST,
                "v3/settings/change-password",
                "CHANGE_PASSWORD",
                payload.toString()
            )
        } catch (e: Exception) {
            val failedResult = HttpResultDo().apply {
                result = WebServiceHelper.ServiceCallStatus.Exception
                responseContent = e.message ?: ""
            }
            continuation.resume(failedResult)
        }
    }

    suspend fun generateCheckoutTicket(): HttpResultDo = suspendCancellableCoroutine { continuation ->
        try {
            WebServiceHelper.callHttpWebService(
                object : AsyncTaskCompleteListener {
                    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                        continuation.resume(httpResult)
                    }
                    override fun onClick(view: View) {}
                },
                context,
                WebServiceHelper.RestMethodType.POST,
                "v3/generate/checkout/ticket",
                "GENERATE_CHECKOUT_TICKET",
                JSONObject().toString()
            )
        } catch (e: Exception) {
            val failedResult = HttpResultDo().apply {
                result = WebServiceHelper.ServiceCallStatus.Exception
                responseContent = e.message ?: ""
            }
            continuation.resume(failedResult)
        }
    }

    suspend fun deleteAccount(): HttpResultDo = suspendCancellableCoroutine { continuation ->
        try {
            WebServiceHelper.callHttpWebService(
                object : AsyncTaskCompleteListener {
                    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                        continuation.resume(httpResult)
                    }
                    override fun onClick(view: View) {}
                },
                context,
                WebServiceHelper.RestMethodType.POST,
                "v3/profile/delete-account",
                "DELETE_ACCOUNT",
                JSONObject().toString()
            )
        } catch (e: Exception) {
            val failedResult = HttpResultDo().apply {
                result = WebServiceHelper.ServiceCallStatus.Exception
                responseContent = e.message ?: ""
            }
            continuation.resume(failedResult)
        }
    }
}
