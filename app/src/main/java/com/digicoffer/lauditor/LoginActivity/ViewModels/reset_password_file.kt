package com.digicoffer.lauditor.LoginActivity.ViewModels

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.check_url
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import org.json.JSONObject

class reset_password_file : AppCompatActivity(), AsyncTaskCompleteListener, View.OnClickListener {

    private var progressDialog: Dialog? = null

    // Reactive Compose States
    private var p1State by mutableStateOf("")
    private var p2State by mutableStateOf("")
    private var isLoadingState by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load and validate session data from Intent and SharedPreferences
        loadSessionData()

        Log.d("RESET_PASSWORD", "onCreate Session Audit: PK=${Constants.PK}, USER_ID=${Constants.USER_ID}, OLD_PASSWORD=${Constants.OLD_PASSWORD}, TOKEN=${Constants.TOKEN?.take(10)}...")

        val composeView = androidx.compose.ui.platform.ComposeView(this).apply {
            setContent {
                com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme {
                    val lengthRule = p1State.length in 8..15
                    val casingRule = p1State.matches(Regex(".*[A-Z].*")) && p1State.matches(Regex(".*[a-z].*"))
                    val numSpecialRule = p1State.matches(Regex(".*[0-9].*")) && p1State.matches(Regex(".*[-!@#$%&*^+=_].*"))

                    val reqList = listOf(
                        com.digicoffer.lauditor.core.ui.common.feedback.PasswordRequirementItem("Must be 8–15 characters long", lengthRule),
                        com.digicoffer.lauditor.core.ui.common.feedback.PasswordRequirementItem("Must include uppercase and lowercase letters", casingRule),
                        com.digicoffer.lauditor.core.ui.common.feedback.PasswordRequirementItem("Must include a number and a special character", numSpecialRule)
                    )

                    val canSubmit = p1State.isNotEmpty() && p2State.isNotEmpty()

                    com.digicoffer.lauditor.ui.auth.ResetPasswordScreen(
                        password1Value = p1State,
                        onPassword1Change = { p1State = it },
                        password2Value = p2State,
                        onPassword2Change = { p2State = it },
                        requirements = reqList,
                        isSubmitEnabled = canSubmit,
                        isLoading = isLoadingState,
                        onSubmitClick = {
                            Log.d("RESET_PASSWORD", "Submit pressed: canSubmit=$canSubmit, p1State='$p1State', p2State='$p2State'")
                            if (!isValidPassword(p1State)) {
                                Log.d("RESET_PASSWORD", "Validation Exit Point: Password rules failed for p1State='$p1State' (length=${p1State.length}, casing=$casingRule, numSpecial=$numSpecialRule)")
                                AndroidUtils.showAlert("Password does not meet the required conditions.", this@reset_password_file)
                            } else if (p1State != p2State) {
                                Log.d("RESET_PASSWORD", "Validation Exit Point: Password mismatch (p1State='$p1State', p2State='$p2State')")
                                AndroidUtils.showAlert("Confirm password mismatch.", this@reset_password_file)
                            } else {
                                Log.d("RESET_PASSWORD", "Validation Passed: Invoking resetPwd()")
                                resetPwd()
                            }
                        },
                        onCancelClick = {
                            Log.d("RESET_PASSWORD", "Cancel pressed: Navigating to LoginActivity")
                            startActivity(Intent(this@reset_password_file, LoginActivity::class.java))
                            finish()
                        }
                    )
                }
            }
        }
        setContentView(composeView)

        window.statusBarColor = ContextCompat.getColor(this, R.color.Blue_text_color)
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = true
        supportActionBar?.hide()
    }

    private fun loadSessionData() {
        val intentPk = intent.getStringExtra("pk")
        val intentUserId = intent.getStringExtra("user_id")

        if (!intentPk.isNullOrEmpty()) Constants.PK = intentPk
        if (!intentUserId.isNullOrEmpty()) Constants.USER_ID = intentUserId

        val prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        if (Constants.PK.isNullOrEmpty()) Constants.PK = prefs.getString("pk", "") ?: ""
        if (Constants.USER_ID.isNullOrEmpty()) Constants.USER_ID = prefs.getString("user_id", "") ?: ""
        if (Constants.TOKEN.isNullOrEmpty()) Constants.TOKEN = prefs.getString("Token", "") ?: ""
        if (Constants.OLD_PASSWORD.isNullOrEmpty()) Constants.OLD_PASSWORD = prefs.getString("password", "") ?: ""
    }

    private fun resetPwd() {
        loadSessionData()

        if (Constants.PK.isNullOrEmpty() || Constants.USER_ID.isNullOrEmpty()) {
            Log.e("RESET_PASSWORD", "Exit Point: Session Error - PK or USER_ID is null/empty. PK='${Constants.PK}', USER_ID='${Constants.USER_ID}'")
            AndroidUtils.showAlert("Session expired. Please sign in again.", this)
            return
        }

        try {
            isLoadingState = true
            check_url()
            Constants.PROBIZ_TYPE = "PROFESSIONAL"
            Constants.base_URL = Constants.PROF_URL

            val postData = JSONObject()
            progressDialog = AndroidUtils.get_progress(this)
            postData.put("old_password", Constants.OLD_PASSWORD ?: "")
            postData.put("field", "password")
            postData.put("password", p1State)

            val urlpath = "password/${Constants.PK}/user/${Constants.USER_ID}/update"
            Log.d("RESET_PASSWORD", "API Execution: Endpoint=${Constants.base_URL}$urlpath")
            Log.d("RESET_PASSWORD", "API Execution: Request Payload=$postData")

            WebServiceHelper.callHttpWebService(
                this,
                this,
                WebServiceHelper.RestMethodType.PUT,
                urlpath,
                "UPDATE",
                postData.toString()
            )
        } catch (e: Exception) {
            isLoadingState = false
            if (progressDialog != null && progressDialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progressDialog)
            }
            e.printStackTrace()
            Log.e("RESET_PASSWORD", "Exit Point: Exception during resetPwd: ${e.message}")
        }
    }

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        isLoadingState = false
        if (progressDialog != null && progressDialog!!.isShowing) {
            AndroidUtils.dismiss_dialog(progressDialog)
        }

        Log.d("RESET_PASSWORD", "Raw httpResult: status=${httpResult.result}, statusCode=${httpResult.status_code}, content=${httpResult.responseContent}")

        if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                val result = JSONObject(httpResult.responseContent ?: "")
                if (httpResult.requestType == "UPDATE") {
                    if (!result.getBoolean("error")) {
                        Log.d("RESET_PASSWORD", "Success Exit Point: Password updated successfully! Clearing session & navigating.")
                        AndroidUtils.showToast(result.getString("msg"), this)

                        val prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                        prefs.edit()
                            .remove("pk")
                            .remove("user_id")
                            .remove("old_password")
                            .apply()

                        val intent = Intent(this, LoginActivity::class.java).apply {
                            putExtra("password_reset_success", true)
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        Log.d("RESET_PASSWORD", "Failure Exit Point: API returned error=true, msg='${result.optString("msg")}'")
                        AndroidUtils.showToast(
                            "Password reset failed: ${result.optString("msg")}",
                            this
                        )
                    }
                }
            } catch (e: Exception) {
                Log.d("RESET_PASSWORD", "Failure Exit Point: JSON Parse Error: ${e.message}")
                AndroidUtils.showToast(e.message, this)
            }
        } else {
            Log.d("RESET_PASSWORD", "Failure Exit Point: API Network Call Failed with content='${httpResult.responseContent}'")
            AndroidUtils.showToast(httpResult.responseContent, this)
        }
    }

    override fun onClick(v: View) {
        // Not used
    }

    private fun isValidPassword(password: String): Boolean {
        if (password.length < 8 || password.length > 15) return false
        if (!password.matches(Regex(".*[A-Z].*")) || !password.matches(Regex(".*[a-z].*"))) return false
        if (!password.matches(Regex(".*[0-9].*")) || !password.matches(Regex(".*[-!@#$%&*^+=_].*"))) return false
        return true
    }
}
