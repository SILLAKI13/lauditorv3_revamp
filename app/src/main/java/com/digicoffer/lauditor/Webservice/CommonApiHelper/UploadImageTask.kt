package com.digicoffer.lauditor.Webservice.CommonApiHelper

import android.content.Context
import android.os.AsyncTask
import android.webkit.MimeTypeMap
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.Webservice.core.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.HashMap

class UploadImageTask(
    private val uploadFile: File,
    private val sslFlag: Boolean,
    private val restMethodType: WebServiceHelper.RestMethodType,
    private val baseURL: String,
    private val callback: AsyncTaskCompleteListener,
    private val activity: Context,
    private val requestType: String
) : AsyncTask<String, String, HttpResultDo>() {

    override fun doInBackground(vararg params: String?): HttpResultDo {
        val httpResult = HttpResultDo()
        try {
            val url = if (requestType == "Upload DocEditor File") {
                baseURL
            } else {
                Constants.base_URL + baseURL
            }

            val headers = HashMap<String, String>()
            headers["Cofferid"] = Constants.USER_ID ?: ""
            headers["Authorization"] = "Bearer ${Constants.TOKEN}"

            val builder = MultipartBody.Builder().setType(MultipartBody.FORM)

            val paramsJson = if (params.isNotEmpty() && params[0] != null) params[0] else "{}"
            ApiMonitorLogger.logSending(
                requestType = requestType,
                url = url,
                method = "POST (Multipart)",
                params = "File: ${uploadFile.name} | Params: $paramsJson"
            )

            val json = JSONObject(paramsJson)
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val value = json.optString(key)
                builder.addFormDataPart(key, value)
            }

            var contentType = getMimeType(uploadFile.absolutePath)
            if (contentType == null) {
                contentType = "application/octet-stream"
            }

            builder.addFormDataPart("content_type", contentType)

            val fileMediaType = contentType.toMediaTypeOrNull()
            val fileBody = uploadFile.asRequestBody(fileMediaType)
            builder.addFormDataPart("file", uploadFile.name, fileBody)

            val requestBody = builder.build()
            val call = RetrofitClient.getApiService().executePost(url, headers, requestBody)
            val response = call.execute()

            val statusCode = response.code()
            httpResult.status_code = statusCode

            val responseData = if (response.isSuccessful) {
                httpResult.result = WebServiceHelper.ServiceCallStatus.Success
                response.body()?.string() ?: ""
            } else {
                httpResult.result = WebServiceHelper.ServiceCallStatus.Failed
                response.errorBody()?.string() ?: ""
            }
            httpResult.responseContent = responseData

        } catch (e: IOException) {
            httpResult.result = WebServiceHelper.ServiceCallStatus.Exception
            httpResult.responseContent = "IOException: ${e.message}"
            AndroidUtils.logMsg("UploadImageTask: IO Exception ${e.message}")
        } catch (e: Exception) {
            httpResult.result = WebServiceHelper.ServiceCallStatus.Exception
            httpResult.responseContent = e.message ?: "Unknown error"
            AndroidUtils.logMsg("UploadImageTask: Exception ${e.message}")
        }

        return httpResult
    }

    override fun onPostExecute(result: HttpResultDo) {
        result.requestType = requestType
        super.onPostExecute(result)
        try {
            val url = if (requestType == "Upload DocEditor File") {
                baseURL
            } else {
                Constants.base_URL + baseURL
            }
            ApiMonitorLogger.logResponse(
                requestType = requestType,
                url = url,
                params = "File: ${uploadFile.name}",
                httpResult = result
            )
            callback.onAsyncTaskComplete(result)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getMimeType(filePath: String): String? {
        val extension = filePath.substringAfterLast('.', "").lowercase()
        return when (extension) {
            "pdf" -> "application/pdf"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xls" -> "application/vnd.ms-excel"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "ppt" -> "application/vnd.ms-powerpoint"
            "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "txt" -> "text/plain"
            "csv" -> "text/csv"
            "zip" -> "application/zip"
            else -> MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/octet-stream"
        }
    }
}
