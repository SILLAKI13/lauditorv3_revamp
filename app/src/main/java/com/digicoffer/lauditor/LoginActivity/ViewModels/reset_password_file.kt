package com.digicoffer.lauditor.LoginActivity.ViewModels

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.check_url
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject

class reset_password_file : AppCompatActivity(), AsyncTaskCompleteListener {

    private var progressDialog: Dialog? = null
    private var password1: TextInputEditText? = null
    private var password2: TextInputEditText? = null
    private var submit: Button? = null
    private var tvErrorMsg: TextView? = null
    private var ivTogglePassword1: ImageView? = null
    private var ivTogglePassword2: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reset_password_file)

        window.statusBarColor = ContextCompat.getColor(this, R.color.Blue_text_color)
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = true
        supportActionBar?.hide()

        // Check if this is a forced password reset
        val isResetMode = intent.getBooleanExtra("reset_mode", false)

        tvErrorMsg = findViewById(R.id.tv_error_msg)
        password1 = findViewById(R.id.et_login_password)
        ivTogglePassword1 = findViewById(R.id.iv_toggle_password)

        val llPassword2: LinearLayout = findViewById(R.id.ll_password2)
        password2 = llPassword2.findViewById(R.id.et_login_password)
        ivTogglePassword2 = llPassword2.findViewById(R.id.iv_toggle_password)

        password1?.setHint(R.string.password)
        password2?.setHint(R.string.confirm_password)

        submit = findViewById(R.id.Submit)
        submit?.isEnabled = false
        submit?.setText(R.string.reset)
        submit?.backgroundTintList = ColorStateList.valueOf(
            resources.getColor(R.color.dullBlueColor)
        )

        // Load saved data if in reset mode
        if (isResetMode) {
            val prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            Constants.PK = prefs.getString("pk", "")
            Constants.USER_ID = prefs.getString("user_id", "")
            Constants.TOKEN = prefs.getString("Token", "")
        }

        // Show all 3 conditions as soon as password1 is focused
        tvErrorMsg?.visibility = View.VISIBLE
        tvErrorMsg?.text = (
            "• Must be 8–15 characters long\n" +
            "• Must include uppercase and lowercase letters\n" +
            "• Must include a number and a special character"
        )

        // password1 watcher — validates conditions one by one
        password1?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                checkPasswordValidation()
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkPasswordValidation()
            }
            override fun afterTextChanged(s: Editable?) {
                checkPasswordValidation()
                checkFields()
            }
        })

        // password2 watcher — only updates button state
        password2?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                checkFields()
            }
        })

        if (password1 != null && ivTogglePassword1 != null) {
            AndroidUtils.password(password1, ivTogglePassword1)
        }
        if (password2 != null && ivTogglePassword2 != null) {
            AndroidUtils.password(password2, ivTogglePassword2)
        }

        // Submit click — show alert if mismatch or invalid
        submit?.setOnClickListener {
            val passwordCheck1 = password1?.text?.toString() ?: ""
            val passwordCheck2 = password2?.text?.toString() ?: ""

            if (!isValidPassword(passwordCheck1)) {
                AndroidUtils.showAlert(
                    "Password does not meet the required conditions.",
                    this
                )
            } else if (passwordCheck1 != passwordCheck2) {
                AndroidUtils.showAlert(
                    "Confirm password mismatch.",
                    this
                )
            } else {
                resetPwd()
            }
        }

        val cancel: Button = findViewById(R.id.Cancel)
        cancel.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    // Conditions disappear one by one as each is met
    private fun checkPasswordValidation() {
        val password = password1?.text?.toString() ?: ""

        if (password.isEmpty()) {
            // Show all conditions again if field is cleared
            tvErrorMsg?.visibility = View.VISIBLE
            tvErrorMsg?.text = (
                "• Must be 8–15 characters long\n" +
                "• Must include uppercase and lowercase letters\n" +
                "• Must include a number and a special character"
            )
            return
        }

        val errorMsg = StringBuilder()

        if (password.length < 8 || password.length > 15) {
            errorMsg.append("• Must be 8–15 characters long\n")
        }
        if (!password.matches(Regex(".*[A-Z].*")) || !password.matches(Regex(".*[a-z].*"))) {
            errorMsg.append("• Must include uppercase and lowercase letters\n")
        }
        if (!password.matches(Regex(".*[0-9].*")) || !password.matches(Regex(".*[-!@#$%&*^+=_].*"))) {
            errorMsg.append("• Must include a number and a special character\n")
        }

        if (errorMsg.isNotEmpty()) {
            tvErrorMsg?.visibility = View.VISIBLE
            tvErrorMsg?.text = errorMsg.toString().trim()
        } else {
            tvErrorMsg?.visibility = View.GONE // All conditions met ✓
        }
    }

    // Enable submit only when both fields are non-empty
    private fun checkFields() {
        val p1 = password1?.text?.toString()?.trim() ?: ""
        val p2 = password2?.text?.toString()?.trim() ?: ""

        if (p1.isNotEmpty() && p2.isNotEmpty()) {
            submit?.isEnabled = true
            submit?.backgroundTintList = ColorStateList.valueOf(
                resources.getColor(R.color.blue)
            )
        } else {
            submit?.isEnabled = false
            submit?.backgroundTintList = ColorStateList.valueOf(
                resources.getColor(R.color.dullBlueColor)
            )
        }
    }

    private fun resetPwd() {
        try {
            check_url()
            Constants.PROBIZ_TYPE = "PROFESSIONAL"
            Constants.base_URL = Constants.PROF_URL

            val postData = JSONObject()
            progressDialog = AndroidUtils.get_progress(this)
            postData.put("old_password", Constants.OLD_PASSWORD)
            postData.put("field", "password")
            postData.put("password", password1?.text?.toString() ?: "")

            val urlpath = "password/${Constants.PK}/user/${Constants.USER_ID}/update"
            WebServiceHelper.callHttpWebService(
                this,
                this,
                WebServiceHelper.RestMethodType.PUT,
                urlpath,
                "UPDATE",
                postData.toString()
            )
            Log.e("Reset Password", "Path: $urlpath")
        } catch (e: Exception) {
            if (progressDialog != null && progressDialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progressDialog)
            }
        }
    }

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        if (progressDialog != null && progressDialog!!.isShowing) {
            AndroidUtils.dismiss_dialog(progressDialog)
        }

        if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                val result = JSONObject(httpResult.responseContent ?: "")
                if (httpResult.requestType == "UPDATE") {
                    if (!result.getBoolean("error")) {
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
                        AndroidUtils.showToast(
                            "Password reset failed: ${result.optString("msg")}",
                            this
                        )
                    }
                }
            } catch (e: Exception) {
                AndroidUtils.showToast(e.message, this)
            }
        } else {
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
