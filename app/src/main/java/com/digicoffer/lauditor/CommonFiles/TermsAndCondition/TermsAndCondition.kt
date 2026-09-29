package com.digicoffer.lauditor.CommonFiles.TermsAndCondition

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.View
import android.view.Window
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.Webservice.HttpResultDo
import org.json.JSONException
import org.json.JSONObject

class TermsAndCondition(private val context: Context) : AsyncTaskCompleteListener {
    private var termsProgressDialog: Dialog? = null
    private val termsDialog: TermsAndConditionsDialog = TermsAndConditionsDialog.getInstance()
    private var conditionListener: OnTermsAndConditionListener? = null
    private var currentVersion: String? = null
    private var currentRequiresAcceptance: Boolean = false
    private var acceptedVersionName: String? = null

    interface OnTermsAndConditionListener {
        fun onTermsAccepted()
        fun onTermsDeclined()
        fun onTermsCheckComplete(needsToShow: Boolean)
    }

    fun setOnTermsAndConditionListener(listener: OnTermsAndConditionListener?) {
        this.conditionListener = listener
    }

    override fun onClick(view: View) {}

    private fun dismissTermsProgressLoader() {
        termsProgressDialog?.let { dialog ->
            try {
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
            } catch (e: Exception) {
                Log.e("TermsAndCondition", "Error dismissing terms progress dialog: ${e.message}")
            }
        }
        termsProgressDialog = null
    }

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        val requestType = httpResult.requestType ?: ""
        val isAcceptTc = requestType.equals("Accept TC", ignoreCase = true)

        if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                if (requestType.equals("Get TC", ignoreCase = true)) {
                    val result = JSONObject(httpResult.responseContent ?: "{}")
                    if (!result.optBoolean("error", false)) {
                        val jsonObject = result.optJSONObject("data")
                        if (jsonObject != null) {
                            val url = jsonObject.optString("url")
                            val version = jsonObject.optString("version", "v1.0")
                            val msg = jsonObject.optString("msg")

                            checkAndShowTermsDialog(url, version, msg)
                        } else {
                            conditionListener?.onTermsCheckComplete(false)
                        }
                    } else {
                        val errorMsg = result.optString("msg", "Terms and Conditions not available.")
                        AndroidUtils.showValidationALert(
                            "Alert",
                            errorMsg,
                            context
                        )
                        conditionListener?.onTermsCheckComplete(false)
                    }
                } else if (isAcceptTc) {
                    val result = JSONObject(httpResult.responseContent ?: "{}")
                    val isError = result.optBoolean("error", false)
                    if (!isError) {
                        val msg = result.optString("msg", "Terms and Conditions accepted successfully.")
                        Log.d("Terms Accept", msg)

                        // 1. Immediately dismiss terms progress dialog instance
                        dismissTermsProgressLoader()

                        // 2. Immediately dismiss terms modal dialog
                        termsDialog.dismiss()

                        // 3. Notify listener on main thread
                        conditionListener?.onTermsAccepted()

                        // 4. Persist accepted version and updated flags asynchronously
                        val finalVersion = acceptedVersionName ?: currentVersion ?: "v1.0"
                        saveAcceptedTermsVersion(finalVersion)
                        Constants.termsVersion = finalVersion
                        Constants.requiresTermsAcceptance = false

                        // Update shared preferences across app storage
                        try {
                            val myPrefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                            myPrefs.edit()
                                .putBoolean("requiresTermsAcceptance", false)
                                .putString("termsVersion", finalVersion)
                                .apply()

                            val savedJson = myPrefs.getString("Json_key", "") ?: ""
                            if (savedJson.isNotEmpty()) {
                                val jsonKey = JSONObject(savedJson)
                                jsonKey.put("requiresTermsAcceptance", false)
                                jsonKey.put("termsVersion", finalVersion)
                                myPrefs.edit().putString("Json_key", jsonKey.toString()).apply()
                            }
                        } catch (e: Exception) {
                            Log.e("TermsAccept", "Failed to patch storage: ${e.message}")
                        }
                    } else {
                        dismissTermsProgressLoader()
                        val errorMsg = result.optString("msg", "Failed to accept Terms and Conditions.")
                        if (context is Activity) {
                            AndroidUtils.showErrorAlert(errorMsg, context)
                        } else {
                            AndroidUtils.showToast(errorMsg, context)
                        }
                        termsDialog.updateAcceptButtonState()
                    }
                }
            } catch (e: Exception) {
                dismissTermsProgressLoader()
                Log.e("TermsAndCondition", "JSON parsing error: ${e.message}")
                if (context is Activity) {
                    AndroidUtils.showErrorAlert("An unexpected error occurred while processing Terms and Conditions.", context)
                }
                termsDialog.updateAcceptButtonState()
            }
        } else {
            dismissTermsProgressLoader()
            // Handle network or backend failures cleanly
            val errorMsg = if (!httpResult.responseContent.isNullOrBlank()) {
                AndroidUtils.extractCleanErrorMessage(httpResult.responseContent)
            } else {
                "Unable to connect to the server. Please check your internet connection and try again."
            }

            if (requestType.equals("Accept TC", ignoreCase = true)) {
                if (context is Activity) {
                    AndroidUtils.showErrorAlert(errorMsg, context)
                } else {
                    AndroidUtils.showToast(errorMsg, context)
                }
                termsDialog.updateAcceptButtonState()
            } else {
                conditionListener?.onTermsCheckComplete(false)
            }
        }
    }

    private fun checkAndShowTermsDialog(pdfUrl: String?, newVersion: String, message: String?) {
        val isVersionNoneOrNull = (currentVersion == null
                || currentVersion.isNullOrEmpty()
                || currentVersion.equals("none", ignoreCase = true))

        val acceptedVersion = getAcceptedTermsVersion() ?: Constants.termsVersion

        Log.d("TermsCheck", "Server version: $newVersion | Locally accepted: $acceptedVersion | requiresTermsAcceptance: $currentRequiresAcceptance")

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
        if (pdfUrl.isNullOrEmpty()) {
            conditionListener?.onTermsCheckComplete(false)
            return
        }
        if (termsDialog.isShowing()) {
            Log.d("TermsCheck", "Dialog already visible — skipping duplicate show")
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
        this.acceptedVersionName = versionName
        Constants.PROBIZ_TYPE = "PROFESSIONAL"
        Constants.base_URL = Constants.PROF_URL
        val act = context as? Activity
        if (act != null && !act.isFinishing && !act.isDestroyed) {
            try {
                termsProgressDialog = Dialog(act).apply {
                    requestWindowFeature(Window.FEATURE_NO_TITLE)
                    setContentView(R.layout.loading)
                    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    setCancelable(false)
                    setCanceledOnTouchOutside(false)
                }
                termsProgressDialog?.show()
            } catch (e: Exception) {
                Log.e("TermsAndCondition", "Error showing terms progress dialog: ${e.message}")
            }
        }
        val postData = JSONObject()
        try {
            postData.put("version", versionName ?: "v1.0")
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
            dismissTermsProgressLoader()
            termsDialog.updateAcceptButtonState()
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
