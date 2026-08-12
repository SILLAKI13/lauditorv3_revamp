package com.digicoffer.lauditor.Webservice.CommonApiHelper

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.CacheUtils.NetworkUtils
import com.digicoffer.lauditor.CommonFiles.TokenManagerUtils.RetryRequest
import com.digicoffer.lauditor.CommonFiles.TokenManagerUtils.TokenRefreshHelper
import com.digicoffer.lauditor.Webservice.core.RetrofitClient
import com.digicoffer.lauditor.Webservice.core.ApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

class HttpExecuteTask(
    private val requestId: String?,
    private val sslFlag: Boolean,
    private val restMethodType: WebServiceHelper.RestMethodType,
    private val URL: String?,
    private val callback: AsyncTaskCompleteListener?,
    private val activity: Context?,
    private val requestType: String?
) : AsyncTask<String, Int, HttpResultDo>() {

    companion object {
        @JvmField
        val openCounter = AtomicInteger(0)
        
        const val MAX_CONCURRENCY = 1
    }

    private var lastBody = ""
    @JvmField var isRetry = false


    override fun onPreExecute() {
        super.onPreExecute()
    }

    override fun doInBackground(vararg params: String?): HttpResultDo {
        lastBody = if (params.isNotEmpty() && params[0] != null) params[0]!! else ""
        val httpResult = HttpResultDo()

        if (activity == null || !NetworkUtils.isInternetAvailable(activity)) {
            httpResult.status_code = 404
            httpResult.result = WebServiceHelper.ServiceCallStatus.Exception
            httpResult.responseContent = Constants.NO_INTERNET_MSG
            return httpResult
        }

        try {
            val urlString = URL ?: ""
            val typeString = requestType ?: ""
            var resolvedUrl = ""

            if (typeString == "Label"
                || typeString == "auth"
                || typeString == "messages_rows"
                || urlString.contains(Constants.EMAIL_UPLOAD_URL)
                || typeString.startsWith("messages_attachment_")
                || typeString == "CREATE_MEETING_LINK"
            ) {
                resolvedUrl = urlString
            } else if (typeString == "Get DocEditor List"
                || typeString == "Delete DocEditor List"
                || typeString == "Save Document"
                || typeString == "Save As Document"
                || typeString == "Save DocEditor"
                || typeString == "Open Document"
            ) {
                resolvedUrl = urlString
            } else if (typeString == "GET UNREAD COUNT"
                || typeString == "RESET COUNT"
                || typeString == "UPDATE COUNT"
                || typeString == "FCM_TOKEN_LOGOUT"
                || typeString == "FCM_TOKEN_UPDATE"
            ) {
                resolvedUrl = urlString
            } else if (restMethodType == WebServiceHelper.RestMethodType.GET && typeString == "Other Doc View") {
                resolvedUrl = urlString
            } else {
                resolvedUrl = Constants.base_URL + urlString
            }

            Log.e("URL", ":$resolvedUrl")

            val headers = HashMap<String, String>()
            headers["Accept"] = "application/json"

            if (typeString != "LOGIN"
                && typeString != "ONBOARD"
                && typeString != "RESEND_OTP"
                && typeString != "SWITCH_FIRM"
                && typeString != "SIGNUP"
                && typeString != "LOGIN_PASSWORD"
                && typeString != "FORGET_PASSWORD"
                && typeString != "VERIFY_TOKEN"
                && typeString != "GET UNREAD COUNT"
                && typeString != "REFRESH_TOKEN_INTERNAL"
                && typeString != "RESET COUNT"
                && typeString != "UPDATE COUNT"
            ) {
                headers["Authorization"] = "Bearer " + Constants.TOKEN
                Log.d("Token", ":Bearer " + Constants.TOKEN)
            }

            if (typeString == "Get DocEditor List"
                || typeString == "Delete DocEditor List"
                || typeString == "Save Document"
                || typeString == "Save As Document"
                || typeString == "Save DocEditor"
                || typeString == "Open Document"
            ) {
                headers["Cofferid"] = Constants.USER_ID ?: ""
            }

            if (typeString == "GET UNREAD COUNT"
                || typeString == "RESET COUNT"
                || typeString == "UPDATE COUNT"
                || typeString == "FCM_TOKEN_LOGOUT"
                || typeString == "FCM_TOKEN_UPDATE"
            ) {
                headers["Content-Type"] = "application/json"
            }

            if (typeString == "Other Doc View" || typeString == "Decrypt Doc") {
                headers["x-client-type"] = "mobile"
            }

            var requestBody: RequestBody? = null
            if (restMethodType == WebServiceHelper.RestMethodType.POST
                || restMethodType == WebServiceHelper.RestMethodType.PUT
                || restMethodType == WebServiceHelper.RestMethodType.PATCH
                || restMethodType == WebServiceHelper.RestMethodType.DELETE
            ) {
                if (restMethodType == WebServiceHelper.RestMethodType.POST && typeString == "Decrypt Doc") {
                    val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
                    val json = JSONObject(lastBody)
                    val keys = json.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val value = json.optString(key)
                        builder.addFormDataPart(key, value)
                    }
                    requestBody = builder.build()
                } else if (restMethodType == WebServiceHelper.RestMethodType.DELETE && lastBody.isEmpty()) {
                    requestBody = null
                } else {
                    val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                    requestBody = lastBody.toRequestBody(mediaType)
                }
            }

            val apiService = RetrofitClient.getApiService()
            val call: Call<ResponseBody> = when (restMethodType) {
                WebServiceHelper.RestMethodType.GET -> apiService.executeGet(resolvedUrl, headers)
                WebServiceHelper.RestMethodType.POST -> apiService.executePost(resolvedUrl, headers, requestBody)
                WebServiceHelper.RestMethodType.PUT -> apiService.executePut(resolvedUrl, headers, requestBody)
                WebServiceHelper.RestMethodType.PATCH -> apiService.executePatch(resolvedUrl, headers, requestBody)
                WebServiceHelper.RestMethodType.DELETE -> {
                    if (requestBody != null) {
                        apiService.executeDeleteWithBody(resolvedUrl, headers, requestBody)
                    } else {
                        apiService.executeDelete(resolvedUrl, headers)
                    }
                }
            }

            val response: Response<ResponseBody> = call.execute()
            val statusCode = response.code()
            Log.i("status_code:", statusCode.toString())
            httpResult.status_code = statusCode

            var responseContent = ""
            if (response.isSuccessful) {
                if (response.body() != null) {
                    responseContent = response.body()!!.string()
                }
                httpResult.result = WebServiceHelper.ServiceCallStatus.Success
                httpResult.responseContent = responseContent
            } else {
                if (response.errorBody() != null) {
                    responseContent = response.errorBody()!!.string()
                }
                if (statusCode == 400) {
                    httpResult.result = WebServiceHelper.ServiceCallStatus.Success
                } else if (statusCode == 502) {
                    httpResult.result = WebServiceHelper.ServiceCallStatus.Pending
                    responseContent = "502 Bad Gateway"
                } else {
                    httpResult.result = WebServiceHelper.ServiceCallStatus.Failed
                }
                httpResult.responseContent = responseContent
            }

        } catch (e: IOException) {
            httpResult.result = WebServiceHelper.ServiceCallStatus.Exception
            httpResult.errorMessage = "Exception: " + e.message
            AndroidUtils.logMsg("WebServiceHelper.callWebService(): IO Exception " + e.message)
        } catch (e: Exception) {
            AndroidUtils.logMsg("HttpExecuteTask.doInBackground(): Exception " + e.message)
            httpResult.result = WebServiceHelper.ServiceCallStatus.Exception
            httpResult.responseContent = e.message ?: ""
        }
        return httpResult
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)
    }

    override fun onPostExecute(result: HttpResultDo) {
        openCounter.decrementAndGet()
        result.requestId = requestId
        result.requestType = requestType ?: ""
        super.onPostExecute(result)

        try {
            Log.e("Response_Msg", result.responseContent)

            val typeString = requestType ?: ""
            val urlString = URL ?: ""
            val skipRefresh = (typeString == "LOGIN_OTP"
                    || typeString == "LOGIN_PASSWORD"
                    || typeString == "REGISTER_OTP"
                    || typeString == "DEVICE_SYNC"
                    || typeString == "Label"
                    || typeString == "auth"
                    || typeString == "messages_rows"
                    || typeString == "REFRESH_TOKEN_INTERNAL"
                    || urlString.contains(Constants.EMAIL_UPLOAD_URL))

            if (result.status_code == 401 && !skipRefresh && !isRetry) {
                val retryRequest = RetryRequest(
                    requestId,
                    restMethodType,
                    URL,
                    lastBody,
                    requestType
                )
                TokenRefreshHelper.handleUnauthorized(activity, retryRequest, callback)
                return
            }

            callback?.onAsyncTaskComplete(result)

        } catch (e: Exception) {
            Log.d("Error_mg", e.message ?: "")
        }
    }
}
