package com.digicoffer.lauditor.LoginActivity.ViewModels

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONException
import org.json.JSONObject

class UserFormActivity : AppCompatActivity(), AsyncTaskCompleteListener, View.OnClickListener {

    private var etFullName: TextInputEditText? = null
    private var etEmailOrPhone: TextInputEditText? = null
    private var tilFullName: TextInputLayout? = null
    private var tilEmailOrPhone: TextInputLayout? = null
    private var btnCancel: AppCompatButton? = null
    private var btnSave: AppCompatButton? = null

    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.otplogin)

        initializeViews()
        setupValidation()
        setupClickListeners()
    }

    private fun initializeViews() {
        etFullName = findViewById(R.id.et_full_name)
        etEmailOrPhone = findViewById(R.id.et_email_or_phone)
        btnCancel = findViewById(R.id.btnCancel)
        btnSave = findViewById(R.id.btnSave)

        tilFullName = etFullName?.parent?.parent as? TextInputLayout
        tilEmailOrPhone = etEmailOrPhone?.parent?.parent as? TextInputLayout
    }

    private fun setupValidation() {
        etFullName?.addTextChangedListener(SimpleTextWatcher { validateFullName() })
        etEmailOrPhone?.addTextChangedListener(SimpleTextWatcher { validateEmailOrPhone() })
    }

    private fun setupClickListeners() {
        btnCancel?.setOnClickListener { finish() }

        btnSave?.setOnClickListener {
            if (validateAllFields()) {
                onboardProfessional()
            }
        }
    }

    private fun validateFullName(): Boolean {
        val name = etFullName?.text?.toString()?.trim() ?: ""

        return when {
            TextUtils.isEmpty(name) -> {
                tilFullName?.error = "Full name is required"
                false
            }
            name.length < 3 -> {
                tilFullName?.error = "Name must be at least 3 characters"
                false
            }
            else -> {
                tilFullName?.error = null
                true
            }
        }
    }

    private fun validateEmailOrPhone(): Boolean {
        val input = etEmailOrPhone?.text?.toString()?.trim() ?: ""

        if (TextUtils.isEmpty(input)) {
            tilEmailOrPhone?.error = "Email or phone number required"
            return false
        }

        return if (input.contains("@")) {
            val valid = Patterns.EMAIL_ADDRESS.matcher(input).matches()
            tilEmailOrPhone?.error = if (valid) null else "Invalid email"
            valid
        } else {
            val valid = input.matches(Regex("^[6-9]\\d{9}$"))
            tilEmailOrPhone?.error = if (valid) null else "Invalid mobile number"
            valid
        }
    }

    private fun validateAllFields(): Boolean {
        val validName = validateFullName()
        val validEmailOrPhone = validateEmailOrPhone()
        return validName && validEmailOrPhone
    }

    private fun onboardProfessional() {
        try {
            Constants.check_url()
            Constants.PROBIZ_TYPE = "PROFESSIONAL"
            Constants.base_URL = "https://adminapi.dev2.digicoffer.com"

            val fullName = etFullName?.text?.toString()?.trim() ?: ""
            val input = etEmailOrPhone?.text?.toString()?.trim() ?: ""

            val postData = JSONObject()
            postData.put("fullname", fullName)
            postData.put("contact_person", fullName)
            postData.put("country", DEFAULT_COUNTRY)
            postData.put("category", DEFAULT_CATEGORY)
            postData.put("plan", DEFAULT_PLAN)

            if (input.contains("@")) {
                postData.put("email", input.lowercase())
            } else {
                postData.put("mobile", input)
                postData.put("email", "")
            }

            Log.d(TAG, "ONBOARD REQUEST: $postData")

            WebServiceHelper.callHttpWebService(
                this,
                this,
                WebServiceHelper.RestMethodType.POST,
                "/professional/onboard/lexis",
                "ONBOARD",
                postData.toString()
            )

        } catch (e: Exception) {
            dismissDialog()
            Toast.makeText(this, "Request error", Toast.LENGTH_SHORT).show()
            Log.e(TAG, e.message, e)
        }
    }

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        dismissDialog()

        val action = httpResult.requestType
        val response = httpResult.responseContent ?: ""
        val isSuccess = httpResult.result == WebServiceHelper.ServiceCallStatus.Success

        Log.d(TAG, "Action: $action")
        Log.d(TAG, "Response: $response")

        if ("ONBOARD" == action) {
            if (response.isNotEmpty()) {
                handleOnboardResponse(response, isSuccess)
            } else {
                resendOtp()
            }
        } else if ("RESEND_OTP" == action) {
            if (response.isNotEmpty()) {
                handleOnboardResponse(response, isSuccess)
            }
        }
    }

    private fun resendOtp() {
        try {
            Constants.check_url()
            Constants.PROBIZ_TYPE = "PROFESSIONAL"
            Constants.base_URL = Constants.PROF_URL
            val postData = JSONObject()
            val emailOrMobile = etEmailOrPhone?.text?.toString()?.trim() ?: ""
            if (emailOrMobile.contains("@")) {
                postData.put("email", emailOrMobile.lowercase())
            } else {
                postData.put("mobile", emailOrMobile)
            }

            Log.d(TAG, "LOGIN REQUEST: $postData")

            WebServiceHelper.callHttpWebService(
                this,
                this,
                WebServiceHelper.RestMethodType.POST,
                "login",
                "RESEND_OTP",
                postData.toString()
            )

        } catch (e: Exception) {
            dismissDialog()
            Toast.makeText(this, "Request error", Toast.LENGTH_SHORT).show()
            Log.e(TAG, e.message, e)
        }
    }

    private fun handleOnboardResponse(response: String, isSuccess: Boolean) {
        try {
            val json = JSONObject(response)
            if (json.has("error")) {
                if (isSuccess && !json.optBoolean("error", true)) {
                    Toast.makeText(this, json.optString("msg", "Success"), Toast.LENGTH_LONG).show()

                    val intent = Intent(this, OtpVerificationActivity::class.java).apply {
                        putExtra("email", etEmailOrPhone?.text?.toString()?.trim())
                        putExtra("fullname", etFullName?.text?.toString()?.trim())
                    }
                    startActivity(intent)
                    finish()
                } else {
                    resendOtp()
                }
            } else {
                Toast.makeText(this, json.optString("msg", "Registration failed"), Toast.LENGTH_LONG).show()
            }

        } catch (e: JSONException) {
            Toast.makeText(this, "Response parse error", Toast.LENGTH_SHORT).show()
            Log.e(TAG, e.message, e)
        }
    }

    private fun dismissDialog() {
        if (progressDialog != null && progressDialog!!.isShowing) {
            AndroidUtils.dismiss_dialog(progressDialog)
        }
    }

    override fun onClick(v: View) {
        // Not used
    }

    override fun onDestroy() {
        dismissDialog()
        super.onDestroy()
    }

    /* -------- Helper class -------- */
    private class SimpleTextWatcher(private val callback: () -> Unit) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            callback()
        }

        override fun afterTextChanged(s: Editable?) {}
    }

    companion object {
        private const val TAG = "UserFormActivity"
        private const val DEFAULT_COUNTRY = "India"
        private const val DEFAULT_CATEGORY = "solo"
        private const val DEFAULT_PLAN = "lauditor"
    }
}
