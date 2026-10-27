package com.digicoffer.lauditor.CommonFiles.GlobalFiles

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.Webservice.HttpResultDo
import org.json.JSONObject

object DeviceSyncManager {

    private const val TAG = "DeviceSync"

    @Volatile
    private var isSyncInProgress = false

    fun getDeviceId(context: Context): String {
        return try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    fun syncDevice(context: Context, onComplete: ((Boolean) -> Unit)? = null) {
        val deviceId = getDeviceId(context)
        val userId = Constants.USER_ID ?: ""
        if (deviceId.isEmpty() || userId.isEmpty()) {
            Log.d(TAG, "Skipping device sync: deviceId='$deviceId', userId='$userId'")
            onComplete?.invoke(false)
            return
        }

        if (isSyncInProgress) {
            Log.d(TAG, "Device sync already in progress, skipping duplicate call")
            onComplete?.invoke(false)
            return
        }

        isSyncInProgress = true
        try {
            val osVersion = "Android " + Build.VERSION.RELEASE
            val deviceName = (Build.MANUFACTURER + " " + Build.MODEL).trim()
            var appVersion = "1.0.0"
            try {
                appVersion = context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
            } catch (ignored: Exception) {}
            val timezone = java.util.TimeZone.getDefault().id

            val payload = JSONObject().apply {
                put("device_id", deviceId)
                put("user_id", userId)
                put("platform", "android")
                put("app_version", appVersion.replace("-dev", ""))
                put("timezone", timezone)
                put("product", "lauditor")
                put("os", osVersion)
                put("device_name", deviceName)
            }

            val syncUrl = "${Constants.adminBaseURL}device/sync"
            Log.d(TAG, "Calling device/sync url=$syncUrl payload=$payload")

            WebServiceHelper.callHttpWebService(
                object : AsyncTaskCompleteListener {
                    override fun onClick(view: android.view.View) {}
                    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                        isSyncInProgress = false
                        val isSuccess = httpResult.result == WebServiceHelper.ServiceCallStatus.Success
                        Log.d(TAG, "device/sync response: status=${httpResult.status_code} content=${httpResult.responseContent}")
                        onComplete?.invoke(isSuccess)
                    }
                },
                context,
                WebServiceHelper.RestMethodType.POST,
                syncUrl,
                "DEVICE_SYNC",
                payload.toString()
            )
        } catch (e: Exception) {
            isSyncInProgress = false
            Log.e(TAG, "device/sync exception: ${e.message}", e)
            onComplete?.invoke(false)
        }
    }
}
