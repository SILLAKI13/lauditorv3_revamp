package com.digicoffer.lauditor.Webservice.CommonApiHelper

import android.content.Context
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.DownloadHelper.DownloadFileFromURL
import com.digicoffer.lauditor.Webservice.DownloadHelper.DownloadFlie
import com.digicoffer.lauditor.Webservice.core.NetworkConfig
import java.io.File

class WebServiceHelper {

    enum class ServiceCallStatus {
        Pending, Sent, Success, Failed, Exception, ConcurrencyError
    }

    enum class RestMethodType {
        GET, POST, PUT, DELETE, PATCH
    }

    companion object {
        init {
            // Bind the generic networking framework configuration to LexiZ Lawyers Constants
            NetworkConfig.baseUrl = { Constants.base_URL }
            NetworkConfig.tokenProvider = { Constants.TOKEN }
            NetworkConfig.cofferIdProvider = { Constants.USER_ID }
        }

        @JvmStatic
        fun callHttpWebService(
            callback: AsyncTaskCompleteListener,
            activity: Context,
            restMethodType: RestMethodType,
            url: String,
            requestType: String,
            vararg formParams: String
        ): String {
            val requestId = ""
            HttpExecuteTask(requestId, false, restMethodType, url, callback, activity, requestType).execute(*formParams)
            return requestId
        }

        @JvmStatic
        fun callEmailHttpWebService(
            callback: AsyncTaskCompleteListener,
            activity: Context,
            restMethodType: RestMethodType,
            url: String,
            requestType: String,
            vararg formParams: String
        ): String {
            val requestId = ""
            MultiPartHttpExecute(requestId, false, restMethodType, url, callback, activity, requestType).execute(*formParams)
            return requestId
        }

        @JvmStatic
        fun callHttpUploadWebService(
            callback: AsyncTaskCompleteListener,
            activity: Context,
            restMethodType: RestMethodType,
            url: String,
            requestType: String,
            file: File,
            vararg formParams: String
        ): String {
            val requestId = ""
            UploadImageTask(file, false, restMethodType, url, callback, activity, requestType).execute(*formParams)
            return requestId
        }

        @JvmStatic
        fun callHttpDownloadFileWebService(
            callback: AsyncTaskCompleteListener,
            activity: Context,
            requestType: String,
            url: String,
            fileName: String
        ): String {
            val requestId = ""
            DownloadFlie(callback, activity, requestType, fileName).execute(url)
            return requestId
        }

        @JvmStatic
        fun callHttpDownloadNewFileWebService(
            callback: AsyncTaskCompleteListener,
            activity: Context,
            requestType: String,
            url: String,
            fileName: String
        ): String {
            val requestId = ""
            DownloadFileFromURL(callback, activity, requestType, fileName).execute(url)
            return requestId
        }
    }
}
