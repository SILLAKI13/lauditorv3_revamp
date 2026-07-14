package com.digicoffer.lauditor.LoginActivity.ViewModels

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.digicoffer.lauditor.LoginActivity.Models.FirmsDo
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONArray
import org.json.JSONObject
import java.util.ArrayList

class ForgetPassword : AppCompatActivity(), AsyncTaskCompleteListener {

    private var submitButton: Button? = null
    private var cancel: Button? = null
    private var tetEmail: TextInputEditText? = null
    private var progressDialog: Dialog? = null

    private var spFirm: ListView? = null
    private var firmLayout: LinearLayout? = null
    private val list = ArrayList<FirmsDo>()

    private var firmName = ""
    private var firmListName = ""
    private val ischecked = true

    // true once the server has returned firms and user must pick one before submitting
    private var awaitingFirmSelection = false

    private var spinnerFirmView: TextView? = null
    private var tvMsgInfo: TextView? = null
    private var tvForgotPwd: TextView? = null
    private var tvContentText: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgetpassword)

        window.statusBarColor = ContextCompat.getColor(this, R.color.Blue_text_color)
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = true
        supportActionBar?.hide()

        initViews()
        setupListeners()
    }

    private fun initViews() {
        tetEmail = findViewById(R.id.et_login_email)
        spFirm = findViewById(R.id.sp_firm)
        spinnerFirmView = findViewById(R.id.spinner_firm_view)
        firmLayout = findViewById(R.id.firm_layout)
        tvMsgInfo = findViewById(R.id.tv_msg_info)
        tvForgotPwd = findViewById(R.id.tv_forgot_pwd)
        tvContentText = findViewById(R.id.tv_contentText)
        submitButton = findViewById(R.id.Submit)
        cancel = findViewById(R.id.Cancel)

        tvContentText?.text = "Don't worry ! It happens. Please enter the email associated with your account"
        tvForgotPwd?.setText(R.string.forgot_password)
        tvMsgInfo?.setText(R.string.note_text)
        tvMsgInfo?.textSize = DynamicUtils.fifteen.toFloat()

        firmLayout?.visibility = View.GONE
        spFirm?.visibility = View.GONE

        spFirm?.background = ContextCompat.getDrawable(this, R.drawable.rectangular_white_background)
        spinnerFirmView?.background = ContextCompat.getDrawable(this, R.drawable.background_transparent)
        spinnerFirmView?.setPadding(30, 3, 3, 0)
        spinnerFirmView?.text = ""

        val adapter = CommonSpinnerAdapter(this, list)
        spFirm?.adapter = adapter

        submitButton?.setText(R.string.submit)
        submitButton?.isEnabled = false
        submitButton?.backgroundTintList = ColorStateList.valueOf(
            resources.getColor(R.color.dullBlueColor)
        )
    }

    private fun setupListeners() {
        tetEmail?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val hasText = !s.toString().trim().isEmpty()
                submitButton?.isEnabled = hasText
                submitButton?.backgroundTintList = ColorStateList.valueOf(
                    resources.getColor(if (hasText) R.color.blue else R.color.dullBlueColor)
                )

                if (awaitingFirmSelection) {
                    resetFirmSelection()
                }
            }
        })

        spinnerFirmView?.setOnClickListener {
            AndroidUtils.display_listview(ischecked, spFirm)
        }

        spFirm?.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            firmName = list[position].value ?: ""
            firmListName = list[position].getName() ?: ""
            Log.d("FIRM_SELECTED", "id=$firmName name=$firmListName")

            spinnerFirmView?.text = firmListName
            spFirm?.visibility = View.GONE

            submitWithSelectedFirm()
        }

        submitButton?.setOnClickListener {
            val email = tetEmail?.text?.toString()?.trim() ?: ""
            if (email.isEmpty()) {
                AndroidUtils.showAlert("Please enter your email address", this)
                return@setOnClickListener
            }
            if (!AndroidUtils.isValidEmail(email)) {
                AndroidUtils.showAlert("Please enter a valid email address", this)
                return@setOnClickListener
            }
            if (awaitingFirmSelection && firmName.isEmpty()) {
                AndroidUtils.showAlert("Please select a firm to continue", this)
                return@setOnClickListener
            }
            resetPassword()
        }

        cancel?.setOnClickListener { navigateToLoginActivity() }
    }

    private fun showFirmDropdown(lauditorFirms: JSONArray) {
        list.clear()
        firmName = ""
        firmListName = ""
        spinnerFirmView?.text = ""

        try {
            for (i in 0 until lauditorFirms.length()) {
                val obj = lauditorFirms.getJSONObject(i)
                val firmsDo = FirmsDo()
                firmsDo.setName(obj.getString("firmName"))
                firmsDo.value = obj.getString("id")
                list.add(firmsDo)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (list.isNotEmpty()) {
            val adapter = CommonSpinnerAdapter(this, list)
            spFirm?.adapter = adapter

            firmLayout?.visibility = View.VISIBLE
            awaitingFirmSelection = true

            tvMsgInfo?.text = "Multiple firms found. Please select the firm you want to reset the password for."
            Log.d("FirmDropdown", "Showing ${list.size} firms")
        } else {
            firmLayout?.visibility = View.GONE
        }
    }

    private fun submitWithSelectedFirm() {
        if (firmName.isEmpty()) return
        awaitingFirmSelection = false
        resetPassword()
    }

    private fun resetFirmSelection() {
        awaitingFirmSelection = false
        firmName = ""
        firmListName = ""
        list.clear()
        spinnerFirmView?.text = ""
        firmLayout?.visibility = View.GONE
        spFirm?.visibility = View.GONE
        tvMsgInfo?.text = getString(R.string.note_text)
    }

    private fun resetPassword() {
        try {
            Constants.check_url()
            Constants.PROBIZ_TYPE = "PROFESSIONAL"
            Constants.base_URL = Constants.PROF_URL

            val email = tetEmail?.text?.toString()?.trim() ?: ""

            val postData = JSONObject()
            postData.put("email", email)
            postData.put("userid", firmName)
            postData.put("plan", "lauditor")
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
            if (progressDialog != null && progressDialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progressDialog)
            }
            e.printStackTrace()
        }
    }

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        if (progressDialog != null && progressDialog!!.isShowing) {
            AndroidUtils.dismiss_dialog(progressDialog)
        }

        if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                val result = JSONObject(httpResult.responseContent ?: "")

                if (httpResult.requestType == "FORGET_PASSWORD") {
                    if (!result.getBoolean("error")) {
                        val message = result.getString("msg")
                        AndroidUtils.showToast(message, this)
                        Constants.forgot_pwd_request = true
                        Constants.Email = tetEmail?.text?.toString()?.trim() ?: ""
                        navigateToLoginActivity()
                    } else {
                        Constants.forgot_pwd_request = false

                        if (result.has("msg")) {
                            AndroidUtils.showToast(result.getString("msg"), this)
                        }

                        if (result.has("firms")) {
                            val firms = result.getJSONObject("firms")
                            Log.d("ForgetPassword", "firms=$firms")

                            val lauditorFirms = firms.optJSONArray("lauditor")
                            if (lauditorFirms != null && lauditorFirms.length() > 0) {
                                showFirmDropdown(lauditorFirms)
                            } else {
                                firmLayout?.visibility = View.GONE
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
            AndroidUtils.showToast(httpResult.responseContent, this)
        }
    }

    private fun navigateToLoginActivity() {
        val email = tetEmail?.text?.toString()?.trim() ?: ""
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

    override fun onClick(view: View) {
        // unused
    }

    private fun isValidEmail(email: String?): Boolean {
        return email != null && email.matches(Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[A-Za-z]{2,}"))
    }
}
