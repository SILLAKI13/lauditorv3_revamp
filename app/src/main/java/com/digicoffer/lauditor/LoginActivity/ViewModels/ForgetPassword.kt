package com.digicoffer.lauditor.LoginActivity.ViewModels

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.digicoffer.lauditor.LoginActivity.Models.FirmsDo
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import org.json.JSONArray
import org.json.JSONObject
import java.util.ArrayList

class ForgetPassword : AppCompatActivity(), AsyncTaskCompleteListener, View.OnClickListener {

    private var progressDialog: Dialog? = null
    private val list = ArrayList<FirmsDo>()

    private var firmName = ""
    private var firmListName = ""

    // Reactive Compose States
    private var emailState by mutableStateOf("")
    private var firmSelectedState by mutableStateOf("")
    private var awaitingFirmSelectionState by mutableStateOf(false)
    private var isLoadingState by mutableStateOf(false)
    private val firmListState = mutableStateListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val composeView = androidx.compose.ui.platform.ComposeView(this).apply {
            setContent {
                com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme {
                    com.digicoffer.lauditor.ui.auth.ForgotPasswordScreen(
                        emailValue = emailState,
                        onEmailChange = { emailState = it },
                        firmList = firmListState,
                        selectedFirm = firmSelectedState,
                        onFirmSelected = { selected ->
                            val match = list.find { it.getName() == selected }
                            if (match != null) {
                                firmName = match.value ?: ""
                                firmSelectedState = selected
                                submitWithSelectedFirm()
                            }
                        },
                        awaitingFirmSelection = awaitingFirmSelectionState,
                        isLoading = isLoadingState,
                        onSubmitClick = {
                            val email = emailState.trim()
                            Log.d("FORGOT_PASSWORD", "Submit clicked with email: $email")
                            if (email.isEmpty()) {
                                AndroidUtils.showAlert("Please enter your email address", this@ForgetPassword)
                            } else if (!isValidEmail(email)) {
                                AndroidUtils.showAlert("Please enter a valid email address", this@ForgetPassword)
                            } else {
                                Log.d("FORGOT_PASSWORD", "resetPassword invoked")
                                resetPassword()
                            }
                        },
                        onCancelClick = { navigateToLoginActivity() }
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

    private fun showFirmDropdown(lauditorFirms: JSONArray) {
        list.clear()
        firmListState.clear()
        firmName = ""
        firmListName = ""
        firmSelectedState = ""

        try {
            for (i in 0 until lauditorFirms.length()) {
                val obj = lauditorFirms.getJSONObject(i)
                val firmsDo = FirmsDo()
                firmsDo.setName(obj.getString("firmName"))
                firmsDo.value = obj.getString("id")
                list.add(firmsDo)
                firmListState.add(obj.getString("firmName"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (list.isNotEmpty()) {
            awaitingFirmSelectionState = true
            Log.d("FORGOT_PASSWORD", "Showing ${list.size} firms in dropdown")
        } else {
            awaitingFirmSelectionState = false
        }
    }

    private fun submitWithSelectedFirm() {
        if (firmName.isEmpty()) return
        awaitingFirmSelectionState = false
        resetPassword()
    }

    private fun resetPassword() {
        try {
            isLoadingState = true
            Constants.check_url()
            Constants.PROBIZ_TYPE = "PROFESSIONAL"
            Constants.base_URL = Constants.PROF_URL

            val email = emailState.trim()

            val postData = JSONObject()
            postData.put("email", email)
            postData.put("userid", firmName)
            postData.put("plan", "lauditor")

            Log.d("FORGOT_PASSWORD", "API Request payload: $postData")
            progressDialog = AndroidUtils.get_progress(this)

            WebServiceHelper.callHttpWebService(
                this,
                this,
                WebServiceHelper.RestMethodType.PUT,
                "reset-pwd",
                "FORGET_PASSWORD",
                postData.toString()
            )
        } catch (e: Exception) {
            isLoadingState = false
            if (progressDialog != null && progressDialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progressDialog)
            }
            e.printStackTrace()
        }
    }

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        isLoadingState = false
        if (progressDialog != null && progressDialog!!.isShowing) {
            AndroidUtils.dismiss_dialog(progressDialog)
        }

        Log.d("FORGOT_PASSWORD", "API Response Received: status=${httpResult.result}, content=${httpResult.responseContent}")

        if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                val result = JSONObject(httpResult.responseContent ?: "")

                if (httpResult.requestType == "FORGET_PASSWORD") {
                    if (!result.getBoolean("error")) {
                        Log.d("FORGOT_PASSWORD", "API Success")
                        val message = result.getString("msg")
                        AndroidUtils.showToast(message, this)
                        Constants.forgot_pwd_request = true
                        Constants.Email = emailState.trim()
                        navigateToLoginActivity()
                    } else {
                        Log.d("FORGOT_PASSWORD", "API Failure: ${result.optString("msg")}")
                        Constants.forgot_pwd_request = false

                        if (result.has("msg")) {
                            AndroidUtils.showToast(result.getString("msg"), this)
                        }

                        if (result.has("firms")) {
                            val firms = result.getJSONObject("firms")
                            Log.d("FORGOT_PASSWORD", "firms=$firms")

                            val lauditorFirms = firms.optJSONArray("lauditor")
                            if (lauditorFirms != null && lauditorFirms.length() > 0) {
                                showFirmDropdown(lauditorFirms)
                            } else {
                                awaitingFirmSelectionState = false
                                AndroidUtils.showAlert(
                                    "No firms found for this email. Please contact support.",
                                    this
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                AndroidUtils.showToast(e.message, this)
            }
        } else {
            Log.d("FORGOT_PASSWORD", "API Network Error")
            AndroidUtils.showToast(httpResult.responseContent, this)
        }
    }

    private fun navigateToLoginActivity() {
        val email = emailState.trim()
        Constants.Email = email

        val intent = Intent(this, LoginActivity::class.java).apply {
            putExtra("password_reset_success", true)
            putExtra("prefill_email", email)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            navigateToLoginActivity()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onClick(v: View) {
        // unused
    }

    private fun isValidEmail(email: String?): Boolean {
        return email != null && email.matches(Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[A-Za-z]{2,}"))
    }
}
