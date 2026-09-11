package com.digicoffer.lauditor.CommonFiles.TermsAndCondition

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.View
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.Webservice.HttpResultDo
import org.json.JSONException
import org.json.JSONObject

class TermsAndCondition(private val context: Context) : AsyncTaskCompleteListener {
    private var progressDialog: Dialog? = null
    private val termsDialog: TermsAndConditionsDialog = TermsAndConditionsDialog.getInstance()
    private var conditionListener: OnTermsAndConditionListener? = null
    private var currentVersion: String? = null
    private var currentRequiresAcceptance: Boolean = false

    interface OnTermsAndConditionListener {
        fun onTermsAccepted()
        fun onTermsDeclined()
        fun onTermsCheckComplete(needsToShow: Boolean)
    }

    fun setOnTermsAndConditionListener(listener: OnTermsAndConditionListener?) {
        this.conditionListener = listener
    }

    override fun onClick(view: View) {}

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                progressDialog?.let {
                    if (it.isShowing) {
                        AndroidUtils.dismiss_dialog(it)
                    }
                }
                val requestType = httpResult.requestType ?: ""
                if (requestType.equals("Get TC", ignoreCase = true)) {
                    val result = JSONObject(httpResult.responseContent ?: "")
                    if (!result.optBoolean("error")) {
                        val jsonObject = result.optJSONObject("data")
                        if (jsonObject != null) {
                            val url = jsonObject.optString("url")
                            val version = jsonObject.optString("version")
                            val msg = jsonObject.optString("msg")

                            // Check if we need to show terms based on version
                            checkAndShowTermsDialog(url, version, msg)
                        }
                    } else {
                        AndroidUtils.showValidationALert(
                            "Alert",
                            result.optString("msg", ""),
                            context
                        )
                        conditionListener?.onTermsCheckComplete(false)
                    }
                } else if (requestType.equals("Accept TC")) {
                    val result = JSONObject(httpResult.responseContent ?: "")
                    val msg = result.optString("msg", "Terms and Conditions accepted successfully.")
                    Log.d("Terms Accept", msg)
                    AndroidUtils.showAlert("Terms and Conditions accepted successfully.", context as Activity)

                    saveAcceptedTermsVersion(currentVersion)
                    Constants.requiresTermsAcceptance = false

                    // FIX: update requiresTermsAcceptance in MyPrefs AND in Json_key
                    try {
                        val myPrefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                        myPrefs.edit().putBoolean("requiresTermsAcceptance", false).apply()

                        // Also patch the Json_key JSON so restore doesn't reload requiresTermsAcceptance=true
                        val savedJson = myPrefs.getString("Json_key", "") ?: ""
                        if (savedJson.isNotEmpty()) {
                            val jsonKey = JSONObject(savedJson)
                            jsonKey.put("requiresTermsAcceptance", false)
                            myPrefs.edit().putString("Json_key", jsonKey.toString()).apply()
                        }
                    } catch (e: Exception) {
                        Log.e("TermsAccept", "Failed to patch Json_key: ${e.message}")
                    }

                    termsDialog.dismiss()
                    conditionListener?.onTermsAccepted()
                }
            } catch (e: JSONException) {
                throw RuntimeException(e)
            }
        } else {
            conditionListener?.onTermsCheckComplete(false)
        }
    }

    private fun checkAndShowTermsDialog(pdfUrl: String?, newVersion: String, message: String?) {
        val isVersionNoneOrNull = (currentVersion == null
                || currentVersion.isNullOrEmpty()
                || currentVersion.equals("none", ignoreCase = true))

        val acceptedVersion = Constants.termsVersion

        Log.d("TermsCheck", "Server version: $newVersion")
        Log.d("TermsCheck", "Locally accepted version: $acceptedVersion")
        Log.d("TermsCheck", "requiresTermsAcceptance: $currentRequiresAcceptance")

        var needsToShow = false

        if (isVersionNoneOrNull) {
            needsToShow = currentRequiresAcceptance
        } else if (acceptedVersion == null || acceptedVersion.isEmpty()
            || acceptedVersion.equals("none", ignoreCase = true)
        ) {
            needsToShow = true
        } else {
            val comparison = compareVersions(newVersion, acceptedVersion)
            if (comparison > 0) {
                needsToShow = true
            } else if (comparison == 0) {
                needsToShow = currentRequiresAcceptance
            } else {
                needsToShow = false
            }
        }

        if (needsToShow) {
            showTermsDialog(pdfUrl, newVersion, message)
        } else {
            conditionListener?.onTermsCheckComplete(false)
        }
    }

    private fun showTermsDialog(pdfUrl: String?, version: String, message: String?) {
        if (context == null || pdfUrl.isNullOrEmpty()) {
            conditionListener?.onTermsCheckComplete(false)
            return
        }
        if (termsDialog.isShowing()) {
            Log.d("TermsCheck", "Dialog already visible — skipping")
            return
        }
        termsDialog.show(context, pdfUrl, version, message, object : TermsAndConditionsDialog.OnTermsActionListener {
            override fun onAccept(version: String) {
                callAcceptTC(version)
            }

            override fun onDecline() {
                conditionListener?.onTermsDeclined()
            }
        })
    }

    private fun compareVersions(v1: String?, v2: String?): Int {
        var version1 = v1 ?: "0"
        var version2 = v2 ?: "0"

        if (version1.isEmpty()) version1 = "0"
        if (version2.isEmpty()) version2 = "0"

        if (version1 == version2) return 0

        // Remove 'v' or 'V' prefix if present
        version1 = version1.replace("^[vV]".toRegex(), "")
        version2 = version2.replace("^[vV]".toRegex(), "")

        // Split by dot
        val v1Parts = version1.split("\\.".toRegex()).toTypedArray()
        val v2Parts = version2.split("\\.".toRegex()).toTypedArray()

        val maxLength = maxOf(v1Parts.size, v2Parts.size)

        for (i in 0 until maxLength) {
            var v1Part = 0.0
            var v2Part = 0.0

            if (i < v1Parts.size) {
                try {
                    v1Part = v1Parts[i].toDouble()
                } catch (e: NumberFormatException) {
                    v1Part = 0.0
                }
            }

            if (i < v2Parts.size) {
                try {
                    v2Part = v2Parts[i].toDouble()
                } catch (e: NumberFormatException) {
                    v2Part = 0.0
                }
            }

            if (v1Part > v2Part) return 1
            if (v1Part < v2Part) return -1
        }

        return 0
    }

    private fun saveAcceptedTermsVersion(version: String?) {
        val prefs = context.getSharedPreferences("terms_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("accepted_terms_version", version).apply()
    }

    private fun getAcceptedTermsVersion(): String? {
        val prefs = context.getSharedPreferences("terms_prefs", Context.MODE_PRIVATE)
        return prefs.getString("accepted_terms_version", null)
    }

    fun callAcceptTC(versionName: String?) {
        Constants.PROBIZ_TYPE = "PROFESSIONAL"
        Constants.base_URL = Constants.PROF_URL
        val postData = JSONObject()
        progressDialog = AndroidUtils.get_progress(context as Activity)
        try {
            postData.put("version", versionName)
            WebServiceHelper.callHttpWebService(
                this,
                context,
                WebServiceHelper.RestMethodType.POST,
                "terms/accept",
                "Accept TC",
                postData.toString()
            )
            Log.d("Accept_TC", postData.toString())
        } catch (e: Exception) {
            e.printStackTrace()
            AndroidUtils.showToast("Error accepting terms", context)
            progressDialog?.let {
                if (it.isShowing) {
                    AndroidUtils.dismiss_dialog(it)
                }
            }
        }
    }

    fun callGetTC() {
        try {
            Log.d("TermsAndCondition", "callGetTC - Making API call to fetch terms")
            Constants.PROBIZ_TYPE = "PROFESSIONAL"
            Constants.base_URL = Constants.PROF_URL
            val jsonObject = JSONObject()
            WebServiceHelper.callHttpWebService(
                this,
                context,
                WebServiceHelper.RestMethodType.GET,
                "terms",
                "Get TC",
                jsonObject.toString()
            )
            Log.d("Get_TC", "Calling get terms API")
        } catch (e: Exception) {
            e.fillInStackTrace()
            Log.e("TermsAndCondition", "Error in callGetTC: ${e.message}")
            conditionListener?.onTermsCheckComplete(false)
        }
    }

    fun checkTermsWithData(serverVersion: String?, requiresTermsAcceptance: Boolean) {
        this.currentVersion = serverVersion
        this.currentRequiresAcceptance = requiresTermsAcceptance

        val isVersionNoneOrNull = (serverVersion == null
                || serverVersion.isEmpty()
                || serverVersion.equals("none", ignoreCase = true))

        val acceptedVersion = getAcceptedTermsVersion()

        Log.d("TermsAndCondition", "Server version: $serverVersion | Locally accepted: $acceptedVersion | requiresAcceptance: $requiresTermsAcceptance")

        var needsToShow = false

        if (isVersionNoneOrNull) {
            // No version to compare against — fall back to the API flag
            needsToShow = requiresTermsAcceptance
        } else if (acceptedVersion == null || acceptedVersion.isEmpty()
            || acceptedVersion.equals("none", ignoreCase = true)
        ) {
            // Never accepted any version on this device — must show
            needsToShow = true
        } else {
            val comparison = compareVersions(serverVersion, acceptedVersion)
            needsToShow = comparison > 0
        }

        if (needsToShow) {
            callGetTC()
        } else {
            conditionListener?.onTermsCheckComplete(false)
        }
    }

    fun dismissDialog() {
        termsDialog.dismiss()
    }
}
