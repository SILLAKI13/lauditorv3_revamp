package com.digicoffer.lauditor.Webservice.CommonApiHelper

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.LoginActivity.ViewModels.LoginActivity
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.Webservice.core.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.io.IOException
import java.util.HashMap
import java.util.concurrent.atomic.AtomicInteger

class MultiPartHttpExecute(
    private val requestId: String,
    private val sslFlag: Boolean,
    private val restMethodType: WebServiceHelper.RestMethodType,
    private val URL: String,
    private val callback: AsyncTaskCompleteListener,
    private val activity: Context,
    private val requestType: String
) : AsyncTask<String, Int, HttpResultDo>() {

    companion object {
        @JvmField
        val openCounter = AtomicInteger(0)
        const val MAX_CONCURRENCY = 1
    }

    private val httpResult = HttpResultDo()

    override fun onPreExecute() {
        super.onPreExecute()
    }

    override fun doInBackground(vararg params: String?): HttpResultDo {
        var fullUrl = ""
        try {
            fullUrl = if (requestType == "Label" || requestType == "auth" || requestType == "messages_rows" || URL.contains(Constants.EMAIL_UPLOAD_URL)) {
                URL
            } else {
                Constants.base_URL + URL
            }

            val bodyParam = if (params.isNotEmpty() && params[0] != null) params[0]!! else ""

            ApiMonitorLogger.logSending(
                requestType = requestType,
                url = fullUrl,
                method = restMethodType.name,
                params = bodyParam
            )

            val headers = HashMap<String, String>()
            headers["Accept"] = "application/json"
            headers["Authorization"] = "Bearer ${Constants.TOKEN}"

            var requestBody: RequestBody? = null
            if (URL == "v3/decrypt") {
                val jsonObject = JSONObject(bodyParam)
                val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
                val keys = jsonObject.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val value = jsonObject.getString(key)
                    builder.addFormDataPart(key, value)
                }
                requestBody = builder.build()
            } else {
                headers["Content-Type"] = "application/json"
                val mediaType = "application/json".toMediaTypeOrNull()
                requestBody = RequestBody.create(mediaType, bodyParam)
            }

            val apiService = RetrofitClient.getApiService()
            val call: Call<ResponseBody> = when (restMethodType) {
                WebServiceHelper.RestMethodType.GET -> apiService.executeGet(fullUrl, headers)
                WebServiceHelper.RestMethodType.POST -> apiService.executePost(fullUrl, headers, requestBody)
                WebServiceHelper.RestMethodType.PUT -> apiService.executePut(fullUrl, headers, requestBody)
                WebServiceHelper.RestMethodType.PATCH -> apiService.executePatch(fullUrl, headers, requestBody)
                WebServiceHelper.RestMethodType.DELETE -> {
                    if (bodyParam.isNotEmpty()) {
                        apiService.executeDeleteWithBody(fullUrl, headers, requestBody)
                    } else {
                        apiService.executeDelete(fullUrl, headers)
                    }
                }
            }

            val response = call.execute()
            val statusCode = response.code()
            Log.i("status_code:", statusCode.toString())
            httpResult.status_code = statusCode

            val responseData = if (response.isSuccessful) {
                httpResult.result = WebServiceHelper.ServiceCallStatus.Success
                val bodyString = response.body()?.string() ?: ""
                Log.d("Response", bodyString)
                bodyString
            } else {
                httpResult.result = WebServiceHelper.ServiceCallStatus.Failed
                val errorString = response.errorBody()?.string() ?: ""
                Log.e("Response", "Error: $statusCode")
                Log.e("Response", "Body: $errorString")
                errorString
            }
            httpResult.responseContent = responseData

        } catch (e: Exception) {
            AndroidUtils.logMsg("MultiPartHttpExecute.doInBackground(): Exception ${e.message}")
            httpResult.result = WebServiceHelper.ServiceCallStatus.Exception
            httpResult.responseContent = e.message ?: "Unknown error"
        }
        return httpResult
    }

    override fun onPostExecute(result: HttpResultDo) {
        openCounter.decrementAndGet()
        result.requestId = requestId
        result.requestType = requestType
        super.onPostExecute(result)

        try {
            Log.e("Response_Msg", result.responseContent)
            val fullUrl = if (requestType == "Label" || requestType == "auth" || requestType == "messages_rows" || URL.contains(Constants.EMAIL_UPLOAD_URL)) {
                URL
            } else {
                Constants.base_URL + URL
            }

            ApiMonitorLogger.logResponse(
                requestType = requestType,
                url = fullUrl,
                params = null,
                httpResult = result
            )

            val statusCode = result.status_code

            if (statusCode == 401 && requestType != "Label" && requestType != "auth" &&
                requestType != "messages_rows" && !URL.contains(Constants.EMAIL_UPLOAD_URL) && requestType != "Dashboard") {
                val intent = Intent(activity, LoginActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                activity.startActivity(intent)
                AndroidUtils.showToast("Session expired, Login again", activity)
            } else if (requestType == "Dashboard" && statusCode == 401) {
                Constants.Valid_Token = false
                Log.d("Session", "Session expired, Login again")
                val intent = Intent(activity, LoginActivity::class.java)
                activity.startActivity(intent)
            } else {
                Constants.Valid_Token = true
            }

            callback.onAsyncTaskComplete(result)
        } catch (e: Exception) {
            Log.d("Error_mg", e.message ?: "Null message")
            AndroidUtils.logMsg("MultiPartHttpExecute.onPostExecute() : Exception ${e.message}")
        }
    }
}
