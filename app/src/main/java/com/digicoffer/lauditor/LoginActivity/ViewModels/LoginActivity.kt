package com.digicoffer.lauditor.LoginActivity.ViewModels

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentManager
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.Validation
import com.digicoffer.lauditor.LoginActivity.Models.Dashboard_Model
import com.digicoffer.lauditor.LoginActivity.Models.FirmsDo
import com.digicoffer.lauditor.MainActivity
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.CommonFiles.ChatService.ChatConnection
import com.digicoffer.lauditor.CommonFiles.ChatService.ChatConnectionService
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils
import com.digicoffer.lauditor.CommonFiles.TermsAndCondition.TermsAndCondition
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputEditText
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Arrays
import java.util.Date
import java.util.HashSet
import java.util.Locale
import java.util.Objects
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class LoginActivity : AppCompatActivity(), AsyncTaskCompleteListener {

    private var appUpdateManager: AppUpdateManager? = null
    private var fullText = "By signing-in, you agree to our T&Cs and Privacy Policy"
    private var comingFromPasswordReset = false

    // UI Elements
    private var et_login_email: TextInputEditText? = null
    private var et_register_phone: TextInputEditText? = null
    private var et_register_email: TextInputEditText? = null
    private var et_full_name: TextInputEditText? = null
    private var et_login_password: TextInputEditText? = null
    private var tv_sign_in: TextView? = null
    private var tv_biometric_info: TextView? = null
    private var tv_toggle_text: TextView? = null
    private var tv_contact_support: TextView? = null
    private var tv_toggle_link: TextView? = null
    private var tv_use_password: TextView? = null
    private var tv_use_password_otp_mode: TextView? = null
    private var tv_forgot_password: TextView? = null
    private var iv_toggle_password: ImageView? = null
    private var login_section: LinearLayout? = null
    private var register_section: LinearLayout? = null
    private var login_biometric_section: LinearLayout? = null
    private var ll_termsCondition: LinearLayout? = null
    private var password_section: LinearLayout? = null
    private var btn_send_otp: AppCompatButton? = null
    private var checkBox: CheckBox? = null
    private var cbTermsCondition: CheckBox? = null
    private var tv_terms: TextView? = null

    // Multi-firm views
    private var firm_layout: LinearLayout? = null
    private var sp_firm: ListView? = null
    private var spinner_firm_view: TextView? = null
    private var tv_multi_firm_msg: TextView? = null
    private var firmList = ArrayList<FirmsDo>()
    private var selectedFirmId = ""
    private var firmIschecked = true

    // Login/Register Mode
    private var isLoginMode = true
    private var isPasswordMode = false
    private var isPasswordVisible = false

    // Existing variables
    private var firm_password = ""
    private var firm_postData = JSONObject()
    private var firm_list_name = ""
    private var ischecked = true
    private var password = ""
    private var has_firm = false
    private var dashboardModels = ArrayList<Dashboard_Model>()
    private var progress_dialog: Dialog? = null
    private var ad_dialog: Dialog? = null
    private var et_firm_password: TextInputEditText? = null
    private var response_true = false
    private var ll_contact_support: LinearLayout? = null
    private var mConnection: ChatConnection? = null
    private var isForceUpdateRequired = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // First check for in-app update
        checkForInAppUpdate()

        // Only proceed with token validation if no force update is required
        if (!isForceUpdateRequired) {
            val prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val savedToken = prefs.getString("Token", "") ?: ""
            val savedJson = prefs.getString("Json_key", "") ?: ""
            Constants.Refresh_token = prefs.getString("refresh_token", "") ?: ""
            var fcmNav: String? = null
            if (intent != null) {
                fcmNav = intent.getStringExtra("fcm_navigation")
                if (fcmNav.isNullOrEmpty()) {
                    fcmNav = intent.getStringExtra("navigation")
                }
            }

            if (savedToken.isNotEmpty() && savedJson.isNotEmpty() && !fcmNav.isNullOrEmpty()) {
                try {
                    val userJson = JSONObject(savedJson)
                    Constants.TOKEN = savedToken
                    Constants.NAME = userJson.getString("name")
                    Constants.NAME_NEW = userJson.getString("name")
                    Constants.termsVersion = userJson.getString("termsVersion")
                    Constants.requiresTermsAcceptance = userJson.optBoolean("requiresTermsAcceptance")
                    Constants.USER_ID = userJson.getString("user_id")
                    Constants.UID = userJson.getString("uid")
                    Constants.OLD_PASSWORD = prefs.getString("password", "") ?: ""
                    Constants.PK = userJson.getString("pk")
                    Constants.PASSWORD_MODE = userJson.getString("password_mode")
                    Constants.IS_ADMIN = userJson.getBoolean("admin")
                    Constants.FIRM_NAME = userJson.getString("firm_name")
                    Constants.ROLE = userJson.getString("role")
                    Constants.CATEGORY = userJson.optString("category")
                    Constants.FirmEmail = userJson.optString("email")
                    Constants.Groups = userJson.getJSONArray("groups")
                    Constants.Email = prefs.getString("email", "") ?: ""
                    Constants.LOGIN_METHOD = prefs.getString("login_method", "email") ?: "email"
                    Constants.is_active = true

                    val namesString = prefs.getString("firmNames", "[]") ?: "[]"
                    val idsString = prefs.getString("firmIds", "[]") ?: "[]"
                    val namesArray = JSONArray(namesString)
                    val idsArray = JSONArray(idsString)
                    Constants.Firm_names.clear()
                    Constants.Firm_ids.clear()
                    for (i in 0 until namesArray.length()) {
                        Constants.Firm_names.add(namesArray.getString(i))
                    }
                    for (i in 0 until idsArray.length()) {
                        Constants.Firm_ids.add(idsArray.getString(i))
                    }

                    Constants.isAdmin = false
                    if ("AAM" == Constants.ROLE) {
                        Constants.isAdmin = true
                    } else if (Constants.Groups.length() == 1 && Constants.Groups.getString(0) == "AAM") {
                        Constants.isAdmin = true
                    }

                    val sub = userJson.optJSONObject("subscription")
                    if (sub != null) {
                        Constants.is_active = sub.optBoolean("is_active", true)
                        val feat = sub.optJSONObject("features")
                        if (feat != null) {
                            Constants.FEATURES.clear()
                            val k = feat.keys()
                            while (k.hasNext()) {
                                val key = k.next()
                                Constants.FEATURES[key] = feat.optBoolean(key, false)
                            }
                        }
                    }

                    val mainIntent = Intent(this, MainActivity::class.java).apply {
                        putExtra("fcm_navigation", fcmNav)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    }
                    startActivity(mainIntent)
                    Log.d("Refresh_Token", Constants.Refresh_token)
                    finish()
                    return

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        
        setContentView(R.layout.login)
        window.statusBarColor = ContextCompat.getColor(this, R.color.Blue_text_color)
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = true

        supportActionBar?.hide()
        Constants.show_register = false
        Constants.loginActivity = this

        initializeViews()
        setupListeners()

        Constants.ISPRODUCTION = false
        Constants.IS_STAGING = false
        DynamicUtils.loadRefreshDynamicSizes(applicationContext)

        supportFragmentManager.addOnBackStackChangedListener {
            finishAffinity()
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishAffinity()
            }
        })
    }

    private fun initializeViews() {
        adjustCardForTablet()
        Log.d("TABLET_CHECK", "isTablet=" + DynamicUtils.isTablet(this)
                + " dpWidth=" + (resources.displayMetrics.widthPixels / resources.displayMetrics.density))

        ll_contact_support = findViewById(R.id.ll_contact_support)
        tv_contact_support = findViewById(R.id.tv_contact_support)

        tv_sign_in = findViewById(R.id.tv_sign_in)
        tv_sign_in?.setText(R.string.login_with_otp)

        login_section = findViewById(R.id.login_section)
        register_section = findViewById(R.id.register_section)
        login_biometric_section = findViewById(R.id.login_biometric_section)
        ll_termsCondition = findViewById(R.id.ll_termsCondition)

        et_login_email = findViewById(R.id.et_login_email)

        password_section = findViewById(R.id.password_section)
        et_login_password = findViewById(R.id.et_login_password)
        tv_forgot_password = findViewById(R.id.tv_forgot_password)
        tv_forgot_password?.paintFlags = tv_forgot_password?.paintFlags?.or(Paint.UNDERLINE_TEXT_FLAG) ?: Paint.UNDERLINE_TEXT_FLAG
        
        tv_use_password = findViewById(R.id.tv_use_password)
        tv_use_password?.paintFlags = tv_use_password?.paintFlags?.or(Paint.UNDERLINE_TEXT_FLAG) ?: Paint.UNDERLINE_TEXT_FLAG
        
        tv_use_password_otp_mode = findViewById(R.id.tv_use_password_otp_mode)
        tv_use_password_otp_mode?.paintFlags = tv_use_password_otp_mode?.paintFlags?.or(Paint.UNDERLINE_TEXT_FLAG) ?: Paint.UNDERLINE_TEXT_FLAG
        
        iv_toggle_password = findViewById(R.id.iv_toggle_password)
        AndroidUtils.password(et_login_password, iv_toggle_password)
        password_section?.visibility = View.GONE

        et_full_name = findViewById(R.id.et_full_name)
        et_register_email = findViewById(R.id.et_register_email)
        et_register_phone = findViewById(R.id.et_register_phone)
        tv_toggle_text = findViewById(R.id.tv_toggle_text)
        tv_toggle_link = findViewById(R.id.tv_toggle_link)
        tv_toggle_link?.paintFlags = tv_toggle_link?.paintFlags?.or(Paint.UNDERLINE_TEXT_FLAG) ?: Paint.UNDERLINE_TEXT_FLAG

        btn_send_otp = findViewById(R.id.btn_send_otp)
        btn_send_otp?.setText(R.string.send_otp)

        tv_biometric_info = findViewById(R.id.tv_biometric_info)
        tv_biometric_info?.setTextColor(ContextCompat.getColor(this, R.color.Red))

        cbTermsCondition = findViewById(R.id.cbTermsCondition)
        cbTermsCondition?.visibility = View.GONE

        // Multi-firm views initialization
        tv_multi_firm_msg = findViewById(R.id.tv_multi_firm_msg)
        firm_layout = findViewById(R.id.firm_layout)
        sp_firm = findViewById(R.id.sp_firm)
        spinner_firm_view = findViewById(R.id.spinner_firm_view)

        // Initially hidden
        tv_multi_firm_msg?.visibility = View.GONE
        firm_layout?.visibility = View.GONE
        sp_firm?.visibility = View.GONE
        spinner_firm_view?.text = ""
        spinner_firm_view?.background = ContextCompat.getDrawable(this, R.drawable.background_transparent)

        checkBox = findViewById(R.id.checkBox)
        tv_terms = findViewById(R.id.tv_terms_full)

        login_section?.visibility = View.VISIBLE
        register_section?.visibility = View.GONE
        login_biometric_section?.visibility = View.GONE
    }

    private fun setupListeners() {
        setupTermsText()

        tv_contact_support?.setOnClickListener {
            openEmailComposer()
        }

        // Firm dropdown toggle
        spinner_firm_view?.setOnClickListener {
            AndroidUtils.display_listview(firmIschecked, sp_firm)
        }

        // Firm item selected
        sp_firm?.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            if (position < firmList.size) {
                selectedFirmId = firmList[position].value ?: ""
                spinner_firm_view?.text = firmList[position].getName()
                sp_firm?.visibility = View.GONE
                firmIschecked = true

                // Auto re-submit login with selected firm
                resubmitPasswordLoginWithFirm(selectedFirmId)
            }
        }

        // "Use Password Instead" — shown in OTP mode
        tv_use_password_otp_mode?.setOnClickListener {
            isPasswordMode = true
            password_section?.visibility = View.VISIBLE
            tv_use_password_otp_mode?.visibility = View.GONE
            tv_sign_in?.text = "Log in with Password"
            btn_send_otp?.text = "Login"
        }

        // "Use OTP Instead"
        tv_use_password?.setOnClickListener {
            isPasswordMode = false
            password_section?.visibility = View.GONE
            tv_use_password_otp_mode?.visibility = View.VISIBLE
            et_login_password?.setText("")
            isPasswordVisible = false
            et_login_password?.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            et_login_password?.setSelection(et_login_password?.text?.length ?: 0)
            iv_toggle_password?.setImageResource(R.drawable.eye_open)
            AndroidUtils.password(et_login_password, iv_toggle_password)
            tv_sign_in?.text = getString(R.string.login_with_otp)
            btn_send_otp?.text = getString(R.string.send_otp)
        }

        // Forgot Password link
        tv_forgot_password?.setOnClickListener {
            startActivity(Intent(this@LoginActivity, ForgetPassword::class.java))
        }

        // Toggle between Login and Register
        tv_toggle_link?.setOnClickListener {
            toggleMode()
        }

        // Send OTP / Login button
        btn_send_otp?.setOnClickListener {
            if (isLoginMode) {
                handleLoginOTP()
            } else {
                handleRegisterOTP()
            }
        }

        checkBox?.setOnCheckedChangeListener { _, isChecked ->
            Constants.is_biometric = isChecked
            val prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("Check_box", isChecked).apply()

            if (!isChecked) {
                clearBiometricData()
            }

            Log.d("Biometric", "Checkbox changed to: $isChecked, Constants.is_biometric: ${Constants.is_biometric}")
        }

        val loginTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateLoginFields()
                if (firm_layout?.visibility == View.VISIBLE) {
                    hideFirmSelection()
                }
            }
        }

        val registerTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateRegisterFields()
            }
        }

        et_login_email?.addTextChangedListener(loginTextWatcher)
        et_full_name?.addTextChangedListener(registerTextWatcher)
        et_register_email?.addTextChangedListener(registerTextWatcher)
        AndroidUtils.NumberFilter(et_register_phone, true)
        et_register_phone?.addTextChangedListener(Validation(et_register_phone))
    }

    private fun hideFirmSelection() {
        tv_multi_firm_msg?.visibility = View.GONE
        firm_layout?.visibility = View.GONE
        sp_firm?.visibility = View.GONE
        spinner_firm_view?.text = ""
        firmList.clear()
        selectedFirmId = ""
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to open link", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun toggleMode() {
        et_login_email?.setText("")
        et_full_name?.setText("")
        et_register_email?.setText("")
        et_register_phone?.setText("")
        et_login_password?.setText("")
        hideFirmSelection()

        if (isLoginMode) {
            isLoginMode = false
            isPasswordMode = false
            password_section?.visibility = View.GONE
            tv_use_password_otp_mode?.visibility = View.GONE
            ll_termsCondition?.visibility = View.GONE
            tv_sign_in?.text = "Register With OTP"
            login_section?.visibility = View.GONE
            register_section?.visibility = View.VISIBLE
            tv_toggle_text?.text = "Already have an account? "
            tv_toggle_link?.paintFlags = tv_toggle_link?.paintFlags?.or(Paint.UNDERLINE_TEXT_FLAG) ?: Paint.UNDERLINE_TEXT_FLAG
            tv_toggle_link?.text = "Log In"
            btn_send_otp?.isEnabled = true
            ll_contact_support?.visibility = View.GONE

            login_biometric_section?.visibility = View.GONE
            checkBox?.visibility = View.GONE
            tv_biometric_info?.visibility = View.GONE

            cbTermsCondition?.visibility = View.VISIBLE
            cbTermsCondition?.setOnCheckedChangeListener(null)
            cbTermsCondition?.isChecked = false

            fullText = "By signing-up, you agree to our T&Cs and Privacy Policy"

        } else {
            isLoginMode = true
            cbTermsCondition?.visibility = View.GONE
            cbTermsCondition?.setOnCheckedChangeListener(null)
            cbTermsCondition?.isChecked = false
            ll_termsCondition?.visibility = View.VISIBLE
            fullText = "By signing-in, you agree to our T&Cs and Privacy Policy"

            tv_sign_in?.text = "Login with OTP"
            login_section?.visibility = View.VISIBLE
            register_section?.visibility = View.GONE
            tv_toggle_text?.text = "Don't have an account? "
            tv_toggle_link?.text = "Register Here"
            btn_send_otp?.text = getString(R.string.send_otp)
            btn_send_otp?.isEnabled = true
            ll_contact_support?.visibility = View.VISIBLE
            tv_use_password_otp_mode?.visibility = View.VISIBLE

            checkBox?.visibility = View.VISIBLE
            CheckBiometric()
        }
        setupTermsText()
    }

    private fun validateLoginFields() {
        btn_send_otp?.isEnabled = true
    }

    private fun validateRegisterFields() {}

    fun handleLoginOTP() {
        if (isPasswordMode) {
            handlePasswordLogin()
            return
        }

        val input = et_login_email?.text?.toString()?.trim() ?: ""

        if (input.isEmpty()) {
            AndroidUtils.showAlert("Please enter email or mobile number", this)
            return
        }

        val looksLikeEmail = input.contains("@")
        val looksLikeMobile = input.matches(Regex("[0-9]+"))

        if (looksLikeEmail) {
            if (!AndroidUtils.isValidEmail(input)) {
                AndroidUtils.showAlert("Please enter a valid email address", this)
                return
            }
        } else if (looksLikeMobile) {
            if (input.length != 10) {
                AndroidUtils.showAlert("Mobile number must be 10 digits", this)
                return
            }
        } else {
            AndroidUtils.showAlert("Please enter a valid email or 10-digit mobile number", this)
            return
        }

        progress_dialog = AndroidUtils.get_progress(this)
        LoginAPI(input)
    }

    private fun handlePasswordLogin() {
        val input = et_login_email?.text?.toString()?.trim() ?: ""
        val pwd = et_login_password?.text?.toString()?.trim() ?: ""

        if (input.isEmpty()) {
            AndroidUtils.showAlert("Please enter email or mobile number", this)
            return
        }
        if (pwd.isEmpty()) {
            AndroidUtils.showAlert("Please enter your password", this)
            return
        }

        val looksLikeEmail = input.contains("@")
        val looksLikeMobile = input.matches(Regex("[0-9]+"))

        if (looksLikeEmail) {
            if (!AndroidUtils.isValidEmail(input)) {
                AndroidUtils.showAlert("Please enter a valid email address", this)
                return
            }
        } else if (looksLikeMobile) {
            if (input.length != 10) {
                AndroidUtils.showAlert("Mobile number must be 10 digits", this)
                return
            }
        } else {
            AndroidUtils.showAlert("Please enter a valid email or 10-digit mobile number", this)
            return
        }

        try {
            progress_dialog = AndroidUtils.get_progress(this)
            Constants.check_url()
            Constants.PROBIZ_TYPE = "PROFESSIONAL"
            Constants.base_URL = Constants.PROF_URL

            val postData = JSONObject()
            postData.put("plan", "lauditor")

            val isEmail = looksLikeEmail
            Constants.LOGIN_METHOD = if (isEmail) "email" else "mobile"

            if (isEmail) {
                postData.put("email", input.lowercase())
                Constants.Email = input.lowercase()
            } else {
                postData.put("mobile", input)
                Constants.Email = input
            }
            postData.put("password", pwd)

            WebServiceHelper.callHttpWebService(
                this,
                this,
                WebServiceHelper.RestMethodType.POST,
                "v2/login",
                "LOGIN_PASSWORD",
                postData.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
            e.printStackTrace()
        }
    }

    private fun handleRegisterOTP() {
        val context = this
        Constants.is_biometric = false
        val fullName = et_full_name?.text?.toString()?.trim() ?: ""
        val emailOrMobile = et_register_email?.text?.toString()?.trim() ?: ""
        val mobile = et_register_phone?.text?.toString()?.trim() ?: ""

        if (fullName.isEmpty()) {
            AndroidUtils.showAlert("Full name is required", this)
            return
        }
        if (fullName.length < 2) {
            AndroidUtils.showAlert("Name should be at least 2 characters", this)
            return
        }
        if (emailOrMobile.isEmpty()) {
            AndroidUtils.showAlert("Email is required", this)
            return
        }
        if (!AndroidUtils.isValidEmail(emailOrMobile)) {
            AndroidUtils.showAlert("Please enter a valid email address", this)
            return
        }

        if (mobile.isEmpty()) {
            AndroidUtils.showAlert("Please enter a valid phone number.", this)
            return
        }
        if (mobile.length != 10) {
            AndroidUtils.showAlert("Please enter a valid phone number.", this)
            return
        }
        if (!mobile.matches(Regex("[6789][0-9]{9}"))) {
            AndroidUtils.showAlert("Please enter a valid phone number.", this)
            return
        }

        AndroidUtils.showConfirmationDialog(this, "Confirmation",
            "Please confirm your email address : $emailOrMobile and phone number : $mobile", "Continue", "Edit",
            object : AndroidUtils.OnConfirmListener {
                override fun onSave() {
                    progress_dialog = AndroidUtils.get_progress(context)
                    fetchCityFromIP { RegisterAPI(fullName, emailOrMobile, mobile) }
                }

                override fun onCancel() {}
            })
    }

    fun LoginAPI(emailOrMobile: String) {
        try {
            progress_dialog = AndroidUtils.get_progress(this)
            Constants.check_url()
            Constants.PROBIZ_TYPE = "PROFESSIONAL"
            Constants.base_URL = Constants.PROF_URL

            val postData = JSONObject()
            postData.put("plan", "lauditor")

            val isEmail = emailOrMobile.contains("@")
            Constants.LOGIN_METHOD = if (isEmail) "email" else "mobile"

            if (isEmail) {
                postData.put("email", emailOrMobile.lowercase())
                Constants.Email = emailOrMobile.lowercase()
            } else {
                postData.put("mobile", emailOrMobile)
                Constants.Email = emailOrMobile
            }

            WebServiceHelper.callHttpWebService(
                this,
                this,
                WebServiceHelper.RestMethodType.POST,
                "login",
                "LOGIN_OTP",
                postData.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
            e.printStackTrace()
        }
    }

    private var detectedCity = ""

    private fun fetchCityFromIP(onComplete: Runnable) {
        Thread {
            try {
                val url = java.net.URL("https://ipwhois.app/json/")
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.connectTimeout = 5000
                conn.readTimeout = 5000

                val reader = java.io.BufferedReader(java.io.InputStreamReader(conn.inputStream))
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                reader.close()

                val json = JSONObject(sb.toString())
                detectedCity = json.optString("city", "")
                Log.d("IPGeo", "City: $detectedCity")

            } catch (e: Exception) {
                detectedCity = ""
                Log.e("IPGeo", "Error: " + e.message)
            } finally {
                runOnUiThread(onComplete)
            }
        }.start()
    }

    private fun RegisterAPI(fullName: String, emailOrMobile: String, mobile: String) {
        try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val currentDate = sdf.format(Date())

            val postData = JSONObject()
            postData.put("fullname", fullName)
            postData.put("country", "India")
            postData.put("contact_person", fullName)
            postData.put("product", "lauditor")
            postData.put("sub_model", "three_month_free")
            postData.put("is_active_sub", true)
            postData.put("sub_start_date", currentDate)
            postData.put("category", "solo")
            postData.put("reg_city", detectedCity)
            postData.put("reg_platform", "android")

            postData.put("email", emailOrMobile.lowercase())
            Constants.Email = emailOrMobile.lowercase()
            postData.put("mobile", mobile)
            Constants.Mobile = mobile

            Constants.base_URL = Constants.adminBaseURL

            WebServiceHelper.callHttpWebService(
                this,
                this,
                WebServiceHelper.RestMethodType.POST,
                "professional/onboard/lexis",
                "REGISTER_OTP",
                postData.toString()
            )
            Log.d("Register_OTP", postData.toString())
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
            e.printStackTrace()
        }
    }

    private fun showFirmSelectionDialog(lauditorFirms: JSONArray) {
        try {
            firmList.clear()
            selectedFirmId = ""
            spinner_firm_view?.text = ""

            for (i in 0 until lauditorFirms.length()) {
                val obj = lauditorFirms.getJSONObject(i)
                val f = FirmsDo()
                f.setName(obj.getString("firmName"))
                f.value = obj.getString("id")
                firmList.add(f)
            }

            if (firmList.isNotEmpty()) {
                val adapter = CommonSpinnerAdapter(this, firmList)
                sp_firm?.adapter = adapter

                tv_multi_firm_msg?.visibility = View.VISIBLE
                firm_layout?.visibility = View.VISIBLE

                Log.d("LoginActivity", "Showing ${firmList.size} firms inline")
            }

        } catch (e: JSONException) {
            e.printStackTrace()
            AndroidUtils.showAlert("Error loading firms. Please try again.", this)
        }
    }

    private fun resubmitPasswordLoginWithFirm(firmId: String) {
        try {
            val input = et_login_email?.text?.toString()?.trim() ?: ""
            val pwd = et_login_password?.text?.toString()?.trim() ?: ""

            tv_multi_firm_msg?.visibility = View.GONE
            firm_layout?.visibility = View.GONE

            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
            progress_dialog = AndroidUtils.get_progress(this)

            Constants.check_url()
            Constants.PROBIZ_TYPE = "PROFESSIONAL"
            Constants.base_URL = Constants.PROF_URL

            val postData = JSONObject()
            postData.put("plan", "lauditor")
            postData.put("userid", firmId)
            postData.put("password", pwd)

            val isEmail = input.contains("@")
            Constants.LOGIN_METHOD = if (isEmail) "email" else "mobile"

            if (isEmail) {
                postData.put("email", input.lowercase())
                Constants.Email = input.lowercase()
            } else {
                postData.put("mobile", input)
                Constants.Email = input
            }

            WebServiceHelper.callHttpWebService(
                this,
                this,
                WebServiceHelper.RestMethodType.POST,
                "v2/login",
                "LOGIN_PASSWORD",
                postData.toString()
            )
        } catch (e: Exception) {
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
            e.printStackTrace()
        }
    }

    private fun checkForInAppUpdate() {
        appUpdateManager = AppUpdateManagerFactory.create(applicationContext)
        val appUpdateInfoTask = appUpdateManager?.appUpdateInfo ?: return

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                isForceUpdateRequired = true
                try {
                    appUpdateManager?.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        this,
                        UPDATE_REQUEST_CODE
                    )
                } catch (e: IntentSender.SendIntentException) {
                    e.printStackTrace()
                    isForceUpdateRequired = false
                }
            } else {
                isForceUpdateRequired = false
            }
        }

        appUpdateInfoTask.addOnFailureListener { e ->
            isForceUpdateRequired = false
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UPDATE_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                Log.e("Update", "Update flow failed! Result code: $resultCode")
                isForceUpdateRequired = false
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra("password_reset_success", false)) {
            comingFromPasswordReset = true
            val prefillEmail = intent.getStringExtra("prefill_email")
            if (!prefillEmail.isNullOrEmpty()) {
                Constants.Email = prefillEmail
            }
        }
    }

    private fun hasSavedUserData(): Boolean {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        val savedEmail = sharedPreferences.getString("email", "") ?: ""
        val savedToken = sharedPreferences.getString("Token", "") ?: ""
        val savedJsonKey = sharedPreferences.getString("Json_key", "") ?: ""

        val hasEmail = savedEmail.trim().isNotEmpty()
        val hasToken = savedToken.trim().isNotEmpty()
        val hasJsonKey = savedJsonKey.trim().isNotEmpty()

        Log.d("BiometricCheck", "hasSavedUserData → email=$hasEmail token=$hasToken json=$hasJsonKey")

        return hasEmail && hasToken && hasJsonKey
    }

    fun check_Bio_metric() {
        if (!isLoginMode) return

        CheckBiometric()

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val sharedPreferences_bio = getSharedPreferences("BIO", Context.MODE_PRIVATE)

        Constants.is_biometric = sharedPreferences.getBoolean("Check_box", false)
        Constants.Biometric_checked = sharedPreferences_bio.getBoolean("BIO_AUTH_DONE", false)

        checkBox?.isChecked = Constants.is_biometric

        if (!Constants.is_biometric) {
            Log.d("BiometricCheck", "Biometric not enabled by user — skipping.")
            return
        }

        if (!hasSavedUserData()) {
            Log.d("BiometricCheck", "Biometric enabled but NO saved user data found — skipping prompt.")
            checkBox?.isChecked = false
            Constants.is_biometric = false
            sharedPreferences.edit().putBoolean("Check_box", false).apply()
            return
        }

        Constants.Email = sharedPreferences.getString("email", "") ?: ""
        val savedToken = sharedPreferences.getString("Token", "") ?: ""
        val refresh_token = sharedPreferences.getString("refresh_token", "") ?: ""
        val responseJson = sharedPreferences.getString("Json_key", "") ?: ""
        Constants.Refresh_token = refresh_token
        Constants.TOKEN = savedToken
        
        if (Constants.Biometric_checked) {
            try {
                val userJson = JSONObject(responseJson)

                Constants.NAME = userJson.getString("name")
                Constants.NAME_NEW = userJson.getString("name")
                Constants.LOGIN_METHOD = sharedPreferences.getString("login_method", "email") ?: "email"
                Constants.USER_ID = userJson.getString("user_id")
                Constants.UID = userJson.getString("uid")
                Constants.OLD_PASSWORD = sharedPreferences.getString("password", "") ?: ""
                Constants.PK = userJson.getString("pk")

                val namesString = sharedPreferences.getString("firmNames", "[]") ?: "[]"
                val idsString = sharedPreferences.getString("firmIds", "[]") ?: "[]"

                val namesArray = JSONArray(namesString)
                val idsArray = JSONArray(idsString)

                Constants.Firm_names.clear()
                Constants.Firm_ids.clear()

                for (i in 0 until namesArray.length()) {
                    Constants.Firm_names.add(namesArray.getString(i))
                }
                for (i in 0 until idsArray.length()) {
                    Constants.Firm_ids.add(idsArray.getString(i))
                }

                Constants.PASSWORD_MODE = userJson.getString("password_mode")
                Constants.IS_ADMIN = userJson.getBoolean("admin")
                Constants.FIRM_NAME = userJson.getString("firm_name")
                Constants.ROLE = userJson.getString("role")
                Constants.Groups = userJson.getJSONArray("groups")
                Constants.CATEGORY = userJson.optString("category")
                Constants.FirmEmail = userJson.optString("email")
                Constants.termsVersion = userJson.getString("termsVersion")
                Constants.requiresTermsAcceptance = userJson.optBoolean("requiresTermsAcceptance")
                Constants.isAdmin = false

                if (Constants.ROLE == "AAM") {
                    Constants.isAdmin = true
                } else if (Constants.Groups.length() == 1 && Constants.Groups.getString(0) == "AAM") {
                    Constants.isAdmin = true
                }

                if (savedToken.isNotEmpty()) {
                    Dashboard()
                } else if (Constants.Email.isNotEmpty()) {
                    LoginAPI(Constants.Email)
                }

            } catch (e: JSONException) {
                e.printStackTrace()
                AndroidUtils.showAlert("Error parsing saved user data", this)
            }

        } else {
            confirm_login()
        }
    }

    private fun setupTermsText() {
        val spannable = SpannableString(fullText)

        val termsStart = fullText.indexOf("T&Cs")
        val termsEnd = termsStart + "T&Cs".length
        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                openUrl("https://digicoffer.com/digicoffer_terms.html")
            }

            override fun updateDrawState(ds: android.text.TextPaint) {
                ds.color = ContextCompat.getColor(this@LoginActivity, R.color.Primary_new)
                ds.isFakeBoldText = false
                ds.isUnderlineText = true
            }
        }, termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val privacyStart = fullText.indexOf("Privacy Policy")
        val privacyEnd = privacyStart + "Privacy Policy".length
        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                openUrl("https://digicoffer.com/privacy.html")
            }

            override fun updateDrawState(ds: android.text.TextPaint) {
                ds.color = ContextCompat.getColor(this@LoginActivity, R.color.Primary_new)
                ds.isFakeBoldText = false
                ds.isUnderlineText = true
            }
        }, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        tv_terms = findViewById(R.id.tv_terms_full)
        tv_terms?.text = spannable
        tv_terms?.movementMethod = LinkMovementMethod.getInstance()
        tv_terms?.highlightColor = Color.TRANSPARENT
    }

    private fun CheckBiometric() {
        val biometricManager = BiometricManager.from(this)
        val canAuthenticate = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )

        tv_biometric_info?.text = "Please enable/add Biometric in your device."
        tv_biometric_info?.visibility = View.GONE

        when (canAuthenticate) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d("Fingerprint", "Biometric authentication is available.")
                tv_biometric_info?.visibility = View.GONE
                checkBox?.isEnabled = true
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Log.d("Fingerprint", "No fingerprint sensor.")
                checkBox?.isEnabled = false
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Log.d("Fingerprint", "Fingerprint sensor not working.")
                checkBox?.isEnabled = false
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Log.d("Fingerprint", "Fingerprint not enrolled.")
                checkBox?.isEnabled = false
            }
            else -> {
                checkBox?.isEnabled = false
            }
        }
    }

    override fun onResume() {
        super.onResume();

        checkForInAppUpdate()

        if (!isForceUpdateRequired) {
            if (intent != null && intent.getBooleanExtra("password_reset_success", false)) {
                comingFromPasswordReset = intent.getBooleanExtra("password_reset_success", false)
                intent.removeExtra("password_reset_success")
            }
            if (comingFromPasswordReset) {
                comingFromPasswordReset = false
                if (et_login_email != null) {
                    resetToPasswordMode()
                }
                val prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                prefs.edit().remove("pk").remove("user_id").remove("old_password").apply()
            }
            val prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val savedToken = prefs.getString("Token", "") ?: ""
            val savedJson = prefs.getString("Json_key", "") ?: ""

            var fcmNav: String? = null
            if (intent != null) {
                fcmNav = intent.getStringExtra("fcm_navigation")
                if (fcmNav.isNullOrEmpty()) {
                    fcmNav = intent.getStringExtra("navigation")
                }
            }

            if (savedToken.isNotEmpty() && savedJson.isNotEmpty() && !fcmNav.isNullOrEmpty()) {
                try {
                    val userJson = JSONObject(savedJson)
                    Constants.TOKEN = savedToken
                    Constants.Refresh_token = prefs.getString("refresh_token", "") ?: ""
                    Constants.NAME = userJson.getString("name")
                    Constants.NAME_NEW = userJson.getString("name")
                    Constants.termsVersion = userJson.getString("termsVersion")
                    Constants.requiresTermsAcceptance = userJson.optBoolean("requiresTermsAcceptance")
                    Constants.USER_ID = userJson.getString("user_id")
                    Constants.UID = userJson.getString("uid")
                    Constants.PK = userJson.getString("pk")
                    Constants.PASSWORD_MODE = userJson.getString("password_mode")
                    Constants.IS_ADMIN = userJson.getBoolean("admin")
                    Constants.FIRM_NAME = userJson.getString("firm_name")
                    Constants.ROLE = userJson.getString("role")
                    Constants.CATEGORY = userJson.optString("category")
                    Constants.FirmEmail = userJson.optString("email")
                    Constants.Groups = userJson.getJSONArray("groups")
                    Constants.Email = prefs.getString("email", "") ?: ""
                    Constants.LOGIN_METHOD = prefs.getString("login_method", "email") ?: "email"

                    val namesString = prefs.getString("firmNames", "[]") ?: "[]"
                    val idsString = prefs.getString("firmIds", "[]") ?: "[]"
                    val namesArray = JSONArray(namesString)
                    val idsArray = JSONArray(idsString)
                    Constants.Firm_names.clear()
                    Constants.Firm_ids.clear()
                    for (i in 0 until namesArray.length()) {
                        Constants.Firm_names.add(namesArray.getString(i))
                    }
                    for (i in 0 until idsArray.length()) {
                        Constants.Firm_ids.add(idsArray.getString(i))
                    }

                    Constants.isAdmin = false
                    if ("AAM" == Constants.ROLE) {
                        Constants.isAdmin = true
                    } else if (Constants.Groups.length() == 1 && Constants.Groups.getString(0) == "AAM") {
                        Constants.isAdmin = true
                    }

                    val mainIntent = Intent(this, MainActivity::class.java).apply {
                        putExtra("fcm_navigation", fcmNav)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    }
                    startActivity(mainIntent)
                    finish()
                    return

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            Constants.check_url()

            val bioPrefs = getSharedPreferences("BIO", Context.MODE_PRIVATE)
            bioPrefs.edit().putBoolean("BIO_AUTH_DONE", false).apply()
            Constants.Biometric_checked = false

            val sessionPrefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val sessionExpired = sessionPrefs.getBoolean("session_expired", false)

            if (sessionExpired) {
                Log.d("onResume", "session_expired=true — showing login form, skipping Dashboard")
                sessionPrefs.edit().putBoolean("session_expired", false).apply()

                var fcmNavOnExpiry: String? = null
                if (intent != null) {
                    fcmNavOnExpiry = intent.getStringExtra("fcm_navigation")
                    if (fcmNavOnExpiry.isNullOrEmpty()) {
                        fcmNavOnExpiry = intent.getStringExtra("navigation")
                    }
                }
                if (!fcmNavOnExpiry.isNullOrEmpty()) {
                    Constants.pendingFcmNavigation = fcmNavOnExpiry
                    Log.d("FCM_NAV", "Preserved pending nav through session expiry: $fcmNavOnExpiry")
                }
            } else {
                checkTokenAndLogin()
            }

            if (Constants.show_register) {
                isLoginMode = true
                toggleMode()
            }

            val rootView = window.decorView.findViewById<View>(android.R.id.content)
            rootView.post {
                val exclusionRects = ArrayList<Rect>()
                exclusionRects.add(Rect(0, 0, rootView.width, rootView.height))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    rootView.systemGestureExclusionRects = exclusionRects
                }
            }
        }
    }

    private fun checkTokenAndLogin() {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val savedToken = sharedPreferences.getString("Token", "") ?: ""
        val refresh_token = sharedPreferences.getString("refresh_token", "") ?: ""
        val responseJson = sharedPreferences.getString("Json_key", "") ?: ""

        if (savedToken.isEmpty() || responseJson.isEmpty()) {
            Log.d("checkTokenAndLogin", "No saved token or json — showing login form")
            return
        }

        try {
            if (savedToken.isNotEmpty()) {
                val userJson = JSONObject(responseJson)

                Constants.check_url()
                Constants.base_URL = Constants.PROF_URL
                Constants.PROBIZ_TYPE = "PROFESSIONAL"

                Constants.TOKEN = savedToken
                Constants.Refresh_token = refresh_token
                Constants.NAME = userJson.getString("name")
                Constants.NAME_NEW = userJson.getString("name")
                Constants.termsVersion = userJson.getString("termsVersion")
                Constants.requiresTermsAcceptance = userJson.optBoolean("requiresTermsAcceptance")
                Constants.USER_ID = userJson.getString("user_id")
                Constants.UID = userJson.getString("uid")
                Constants.OLD_PASSWORD = sharedPreferences.getString("password", "") ?: ""
                Constants.PK = userJson.getString("pk")
                Constants.PASSWORD_MODE = userJson.getString("password_mode")
                Constants.IS_ADMIN = userJson.getBoolean("admin")
                Constants.FIRM_NAME = userJson.getString("firm_name")
                Constants.ROLE = userJson.getString("role")
                Constants.Groups = userJson.getJSONArray("groups")
                Constants.CATEGORY = userJson.optString("category")
                Constants.FirmEmail = userJson.optString("email")
                Constants.Email = sharedPreferences.getString("email", "") ?: ""
                Constants.LOGIN_METHOD = sharedPreferences.getString("login_method", "email") ?: "email"
                Constants.is_active = true

                val namesString = sharedPreferences.getString("firmNames", "[]") ?: "[]"
                val idsString = sharedPreferences.getString("firmIds", "[]") ?: "[]"
                val namesArray = JSONArray(namesString)
                val idsArray = JSONArray(idsString)
                Constants.Firm_names.clear()
                Constants.Firm_ids.clear()
                for (i in 0 until namesArray.length()) {
                    Constants.Firm_names.add(namesArray.getString(i))
                }
                for (i in 0 until idsArray.length()) {
                    Constants.Firm_ids.add(idsArray.getString(i))
                }

                Constants.isAdmin = false
                if (Constants.ROLE == "AAM") {
                    Constants.isAdmin = true
                } else if (Constants.Groups.length() == 1 && Constants.Groups.getString(0) == "AAM") {
                    Constants.isAdmin = true
                }

                val sub = userJson.optJSONObject("subscription")
                if (sub != null) {
                    Constants.is_active = sub.optBoolean("is_active", true)
                    val feat = sub.optJSONObject("features")
                    if (feat != null) {
                        Constants.FEATURES.clear()
                        val k = feat.keys()
                        while (k.hasNext()) {
                            val key = k.next()
                            Constants.FEATURES[key] = feat.optBoolean(key, false)
                        }
                    }
                }

                var pendingNav: String? = intent.getStringExtra("fcm_navigation")
                if (pendingNav.isNullOrEmpty()) {
                    pendingNav = intent.getStringExtra("navigation")
                }
                if (!pendingNav.isNullOrEmpty()) {
                    Constants.pendingFcmNavigation = pendingNav
                    Log.d("FCM_NAV", "Saved pending nav: $pendingNav")
                }

                if (Constants.TOKEN.isNotEmpty()) {
                    Dashboard()
                } else if (Constants.Email.isNotEmpty()) {
                    LoginAPI(Constants.Email)
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun saveBiometricCredentials() {
        val email = Constants.Email
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putString("email", email?.lowercase())
            putString("refresh_token", Constants.Refresh_token)
            putBoolean("Check_box", true)

            try {
                val firmNamesArray = JSONArray(Constants.Firm_names)
                val firmIdsArray = JSONArray(Constants.Firm_ids)
                putString("firmNames", firmNamesArray.toString())
                putString("firmIds", firmIdsArray.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.apply()
        Constants.is_biometric = true
    }

    private fun clearBiometricData() {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putBoolean("Check_box", Constants.is_biometric)
            clear()
        }.apply()

        val sharedPreferences_bio = getSharedPreferences("BIO", Context.MODE_PRIVATE)
        sharedPreferences_bio.edit().apply {
            putBoolean("IS_Bio", false)
            putBoolean("BIO_AUTH_DONE", false)
        }.apply()

        Constants.is_biometric = false
        Constants.Biometric_checked = false
    }

    fun Dashboard() {
        AndroidUtils.updateCachedUserData(this)
        progress_dialog = AndroidUtils.get_progress(this)
        Constants.MYDAYCARDS.clear()
        Constants.KPICARDS.clear()
        Constants.check_url()
        val jsonObject = JSONObject()
        Constants.base_URL = Constants.PROF_URL
        WebServiceHelper.callHttpWebService(this, this,
            WebServiceHelper.RestMethodType.GET, Constants.Dashboard, "Dashboard",
            jsonObject.toString())
    }

    fun callRefreshLogin() {
        try {
            val jsonObject = JSONObject()
            Constants.base_URL = Constants.PROF_URL
            jsonObject.put("refresh_token", Constants.Refresh_token)
            WebServiceHelper.callHttpWebService(
                this, this,
                WebServiceHelper.RestMethodType.POST, "refresh/token", "Refresh Token",
                jsonObject.toString())
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    fun syncDevice() {
        try {
            val deviceId = Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ANDROID_ID
            )
            val osVersion = "Android " + Build.VERSION.RELEASE
            val deviceName = Build.MANUFACTURER + " " + Build.MODEL
            Constants.base_URL = Constants.adminBaseURL
            var appVersion = "1.0.0"
            try {
                appVersion = packageManager.getPackageInfo(packageName, 0).versionName ?: "1.0.0"
            } catch (ignored: Exception) {}
            
            val timezone = java.util.TimeZone.getDefault().id
            val postData = JSONObject()
            postData.put("device_id", deviceId)
            postData.put("user_id", Constants.USER_ID)
            postData.put("platform", "android")
            postData.put("app_version", appVersion.replace("-dev", ""))
            postData.put("timezone", timezone)
            postData.put("product", "lauditor")
            postData.put("os", osVersion)
            postData.put("device_name", deviceName)
            Log.d("DeviceSync", "Payload → $postData")

            WebServiceHelper.callHttpWebService(
                this,
                this,
                WebServiceHelper.RestMethodType.POST,
                "device/sync",
                "DEVICE_SYNC",
                postData.toString()
            )
            Log.e("DeviceSync", "syncDevice : $postData")
        } catch (e: Exception) {
            Log.e("DeviceSync", "syncDevice failed: " + e.message)
            e.printStackTrace()
        }
    }

    private fun checkPasswordResetMode(userData: JSONObject) {
        try {
            val passwordMode = userData.getString("password_mode")
            if ("reset" == passwordMode) {
                Constants.PASSWORD_MODE = passwordMode
                Constants.PK = userData.getString("pk")
                Constants.USER_ID = userData.getString("user_id")

                val prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                prefs.edit()
                    .putString("pk", Constants.PK)
                    .putString("user_id", Constants.USER_ID)
                    .putString("refresh_token", Constants.Refresh_token)
                    .putString("Token", Constants.TOKEN)
                    .apply()

                val resetIntent = Intent(this@LoginActivity, reset_password_file::class.java).apply {
                    putExtra("reset_mode", true)
                }
                startActivity(resetIntent)
                finish()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun resetToPasswordMode() {
        runOnUiThread {
            isLoginMode = true
            isPasswordMode = true

            password_section?.visibility = View.VISIBLE
            tv_use_password_otp_mode?.visibility = View.GONE
            tv_sign_in?.text = "Log in with Password"
            btn_send_otp?.text = "Login"
            hideFirmSelection()
            et_login_email?.setText(Constants.Email)
            login_section?.visibility = View.VISIBLE
            register_section?.visibility = View.GONE
        }
    }

    private fun handleLoginResponse(result: JSONObject) {
        try {
            Constants.Firm_ids.clear()
            Constants.Firm_names.clear()

            if (!result.getBoolean("error")) {
                val probiz_data = JSONObject(result.getString("data"))
                Constants.forgot_pwd_request = false
                Constants.jsonObject_dashboard = probiz_data
                if (!probiz_data.getString("plan").equals("lauditor", ignoreCase = true)) {
                    AndroidUtils.showAlert("Account not found", this)
                    return
                }

                val email = et_login_email?.text?.toString() ?: ""
                val password = et_login_password?.text?.toString() ?: ""
                val prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
                prefs.edit()
                    .putString("email", email.lowercase())
                    .putString("password", password)
                    .putBoolean("isLogin", true)
                    .putString("login_method", Constants.LOGIN_METHOD)
                    .putString("proBizType", Constants.PROBIZ_TYPE)
                    .apply()

                Constants.is_biometric = true
                Constants.NAME = probiz_data.getString("name")
                Constants.USER_ID = probiz_data.getString("user_id")
                Constants.termsVersion = probiz_data.getString("termsVersion")
                Constants.requiresTermsAcceptance = probiz_data.optBoolean("requiresTermsAcceptance")
                Constants.UID = probiz_data.getString("uid")
                Constants.OLD_PASSWORD = et_login_password?.text?.toString() ?: ""
                Constants.PK = probiz_data.getString("pk")
                Constants.PASSWORD_MODE = probiz_data.getString("password_mode")
                Constants.IS_ADMIN = probiz_data.getBoolean("admin")
                Constants.FIRM_NAME = probiz_data.getString("firm_name")
                Constants.ROLE = probiz_data.getString("role")
                Constants.CATEGORY = probiz_data.optString("category")
                Constants.Groups = probiz_data.getJSONArray("groups")
                Constants.FirmEmail = probiz_data.optString("email")
                Constants.Email = email.lowercase()
                Constants.Refresh_token = probiz_data.optString("refresh_token")
                Constants.TOKEN = probiz_data.optString("access_token")

                val subscription = probiz_data.optJSONObject("subscription")
                if (subscription != null) {
                    val features = subscription.optJSONObject("features")
                    if (features != null) {
                        Constants.FEATURES.clear()
                        val keys = features.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            val value = features.optBoolean(key, false)
                            Constants.FEATURES[key] = value
                        }
                    }
                    Constants.is_active = subscription.optBoolean("is_active")
                }

                Constants.User_Allowed = probiz_data.optInt("user_allowed")

                Constants.isAdmin = false
                if (Constants.ROLE == "AAM") {
                    Constants.isAdmin = true
                } else if (Constants.Groups.length() == 1 && Constants.Groups.getString(0) == "AAM") {
                    Constants.isAdmin = true
                }

                Constants.Firm_ids.clear()
                Constants.Firm_names.clear()
                val list = ArrayList<FirmsDo>()

                val adminJsonArray = probiz_data.getJSONArray("firms")
                for (i in 0 until adminJsonArray.length()) {
                    val obj = adminJsonArray.getJSONObject(i)
                    val firmsDo = FirmsDo()

                    val firmName = obj.getString("firmName")
                    val firmId = obj.getString("id")

                    firmsDo.setName(firmName)
                    firmsDo.value = firmId

                    Constants.Firm_names.add(firmName)
                    Constants.Firm_ids.add(firmId)
                    list.add(firmsDo)
                }
                Log.d("Groups_value", "" + Constants.isAdmin)
                
                if ("reset" == Constants.PASSWORD_MODE) {
                    Constants.PK = probiz_data.getString("pk")
                    Constants.USER_ID = probiz_data.getString("user_id")

                    val prefs1 = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                    prefs1.edit()
                        .putString("pk", Constants.PK)
                        .putString("user_id", Constants.USER_ID)
                        .putString("Token", Constants.TOKEN)
                        .putString("refresh_token", Constants.Refresh_token)
                        .apply()

                    val resetIntent = Intent(this@LoginActivity, reset_password_file::class.java).apply {
                        putExtra("reset_mode", true)
                    }
                    startActivity(resetIntent)
                    finish()
                } else {
                    Bio_metric_access()
                    Dashboard()
                    Constants.IS_MyDay = true
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun Bio_metric_access() {
        val token = Constants.TOKEN
        val email = Constants.Email
        val password = et_login_password?.text?.toString() ?: ""
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putString("email", email.lowercase())
            putString("password", password)
            putString("Token", token)
            putString("refresh_token", Constants.Refresh_token)
            putString("login_method", Constants.LOGIN_METHOD)
            putString("firm_id", Constants.Firm_id)
            putString("Json_key", Constants.jsonObject_dashboard?.toString() ?: "")
            putBoolean("Check_box", Constants.is_biometric)
        }.apply()
    }

    override fun onAsyncTaskComplete(httpResult: HttpResultDo?) {
        if (httpResult == null) return

        if (progress_dialog != null && progress_dialog!!.isShowing) {
            AndroidUtils.dismiss_dialog(progress_dialog)
        }

        val requestType = httpResult.requestType ?: ""

        if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
            Constants.IS_MyDay = true
            try {
                val result = JSONObject(httpResult.responseContent ?: "")

                if (requestType == "LOGIN_OTP") {
                    if (!result.optBoolean("error")) {
                        if (result.getBoolean("otp_sent")) {
                            if (!isLoginMode) {
                                val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                                sharedPreferences.edit().putBoolean("Check_box", Constants.is_biometric).apply()

                                val intent = Intent(this@LoginActivity, OtpVerificationActivity::class.java).apply {
                                    putExtra("email", Constants.Email)
                                    putExtra("Check_box", Constants.is_biometric)
                                    putExtra("is_register", false)
                                }
                                startActivity(intent)
                            } else {
                                val message = result.getString("msg")
                                AndroidUtils.showAlert(message, this, "Success") {
                                    val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                                    sharedPreferences.edit().putBoolean("Check_box", Constants.is_biometric).apply()

                                    val intent = Intent(this@LoginActivity, OtpVerificationActivity::class.java).apply {
                                        putExtra("email", Constants.Email)
                                        putExtra("Check_box", Constants.is_biometric)
                                        putExtra("is_register", false)
                                    }
                                    startActivity(intent)
                                }
                            }
                        }
                    } else {
                        val errorMsg = result.optString("msg", "Failed to send OTP")
                        AndroidUtils.showAlert(errorMsg, this)
                    }
                } else if (requestType == "Refresh Token") {
                    if (!result.optBoolean("error")) {
                        val jsonObject = result.optJSONObject("data")
                        if (jsonObject != null) {
                            Constants.Refresh_token = jsonObject.optString("refresh_token")
                            Constants.TOKEN = jsonObject.optString("access_token")
                        }
                    } else {
                        val errorMsg = result.optString("msg", "Login failed. Please check your credentials.")
                        AndroidUtils.showAlert(errorMsg, this)
                    }
                } else if (requestType == "LOGIN_PASSWORD") {
                    if (!result.optBoolean("error")) {
                        handleLoginResponse(result)
                    } else {
                        if (result.has("firms")) {
                            val firms = result.optJSONObject("firms")
                            val lauditorFirms = firms?.optJSONArray("lauditor")

                            if (lauditorFirms != null && lauditorFirms.length() > 0) {
                                showFirmSelectionDialog(lauditorFirms)
                            } else {
                                val errorMsg = result.optString("msg", "Login failed. No valid firms found.")
                                AndroidUtils.showAlert(errorMsg, this)
                            }
                        } else {
                            val errorMsg = result.optString("msg", "Login failed. Please check your credentials.")
                            AndroidUtils.showAlert(errorMsg, this)
                        }
                    }
                }

                if (requestType.equals("Get TC", ignoreCase = true)) {
                    if (!result.optBoolean("error")) {
                        val jsonObject = result.optJSONObject("data")
                        val url = jsonObject?.optString("url") ?: ""
                        val version = jsonObject?.optString("version") ?: ""
                        val msg = jsonObject?.optString("msg") ?: ""
                    } else {
                        AndroidUtils.showAlert("Alert", this, result.optString("msg", ""))
                    }
                } else if (requestType == "Accept TC") {
                    val msg = result.getString("msg")
                    Log.d("Terms Accept", msg)
                    AndroidUtils.showToast(msg, this)
                }

                else if (requestType == "REGISTER_OTP") {
                    if (result.has("error") && result.getBoolean("error")) {
                        val errorMsg = result.getString("msg")
                        if (result.has("redirect_to")) {
                            isLoginMode = false
                            if (result.optString("redirect_to") == "mobile") {
                                AndroidUtils.showAlert("Phone number is already registered. Please log in using the OTP sent via SMS", this, "Success") {
                                    et_login_email?.setText(Constants.Mobile)
                                    LoginAPI(Constants.Mobile)
                                }
                            } else if (result.optString("redirect_to") == "email") {
                                AndroidUtils.showAlert("Email is already registered. Please log in using the OTP sent to your inbox.", this, "Success") {
                                    et_login_email?.setText(Constants.Email)
                                    LoginAPI(Constants.Email)
                                }
                            } else {
                                AndroidUtils.showAlert("Account already exists. Please login via OTP sent to your inbox.", this, "Success") {
                                    et_login_email?.setText(Constants.Email)
                                    LoginAPI(Constants.Email)
                                }
                            }
                        } else {
                            AndroidUtils.showAlert(errorMsg, this)
                        }
                        Log.d("ErrorMsg", errorMsg)
                    } else {
                        val message = result.optString("msg", "Registration successful! OTP sent to your email.")
                        AndroidUtils.showAlert(message, this, "Success") {
                            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                            sharedPreferences.edit().putBoolean("Check_box", Constants.is_biometric).apply()

                            val intent = Intent(this@LoginActivity, OtpVerificationActivity::class.java).apply {
                                putExtra("email", Constants.Email)
                                putExtra("Check_box", Constants.is_biometric)
                                putExtra("is_register", true)
                            }
                            startActivity(intent)
                        }
                    }
                }

                else if (requestType == "DEVICE_SYNC") {
                    try {
                        val syncResult = JSONObject(httpResult.responseContent ?: "")
                        Log.d("Device_Synced_Successfully", "Response → " + syncResult.optString("msg", "no msg"))
                    } catch (e: Exception) {
                        Log.e("DeviceSync", "Failed to parse sync response: " + e.message)
                    }
                }

                else if (requestType == "Dashboard") {
                    if (result.optBoolean("error") && result.optString("msg").lowercase().contains("token")) {
                        Constants.Valid_Token = false
                        AndroidUtils.showAlert("Session expired. Please login again.", this)
                        return
                    }

                    Constants.Valid_Token = true
                    val dashboardArray = result.getJSONArray("cards")
                    Dashboard_data(dashboardArray)
                    saveBiometricCredentials()

                    Constants.check_url()
                    Constants.base_URL = Constants.PROF_URL
                    Constants.PROBIZ_TYPE = "PROFESSIONAL"

                    getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                        .edit()
                        .putString("Token", Constants.TOKEN)
                        .putString("refresh_token", Constants.Refresh_token)
                        .putString("base_url", Constants.base_URL)
                        .apply()

                    syncDevice()

                    val mainIntent = Intent(this, MainActivity::class.java).apply {
                        var pendingNav = intent.getStringExtra("fcm_navigation")
                        if (pendingNav.isNullOrEmpty()) {
                            pendingNav = intent.getStringExtra("navigation")
                        }
                        if (pendingNav.isNullOrEmpty()) {
                            pendingNav = Constants.pendingFcmNavigation
                        }
                        if (!pendingNav.isNullOrEmpty()) {
                            putExtra("fcm_navigation", pendingNav)
                            Constants.pendingFcmNavigation = ""
                            Log.d("FCM_NAV", "Forwarding nav to MainActivity: $pendingNav")
                        }
                    }

                    startActivity(mainIntent)
                    saveXmppPreferences()
                    
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        if (Constants.mainActivity != null) {
                            Constants.mainActivity.registerPendingFCMToken()
                        }
                    }, 2000)
                    finish()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                AndroidUtils.showAlert("Error: " + e.message, this)
            }
        } else {
            if (requestType == "DEVICE_SYNC") {
                Log.e("DeviceSync", "Sync failed silently — status: " + httpResult.status_code)
                return
            }
            if (progress_dialog != null && progress_dialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progress_dialog)
            }
            try {
                val result = JSONObject(httpResult.responseContent ?: "")
                AndroidUtils.showErrorAlert(result.optString("msg"), this)
            } catch (e: Exception) {
                AndroidUtils.showErrorAlert(httpResult.responseContent.toString(), this)
            }
        }
    }

    private fun handleDashboardResponse(result: JSONObject) {
        try {
            if (result.optBoolean("error") && result.optString("msg").lowercase().contains("token")) {
                Constants.Valid_Token = false
                AndroidUtils.showAlert("Session expired. Please login again.", this)
                return
            }

            Constants.Valid_Token = true
            val dashboardArray = result.getJSONArray("cards")
            Dashboard_data(dashboardArray)
            saveXmppPreferences()
            
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                if (Constants.mainActivity != null) {
                    Constants.mainActivity.registerPendingFCMToken()
                }
            }, 2000)
            
            val mainIntent = Intent(this, MainActivity::class.java).apply {
                val pendingNav = intent.getStringExtra("fcm_navigation")
                if (!pendingNav.isNullOrEmpty()) {
                    putExtra("fcm_navigation", pendingNav)
                }
            }
            startActivity(mainIntent)
            finish()

        } catch (e: Exception) {
            e.printStackTrace()
            AndroidUtils.showAlert("Error loading dashboard", this)
        }
    }

    private fun confirm_login() {
        val executor = Executors.newSingleThreadExecutor()
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Log.d("Fingerprint_success", "Biometric authentication successful")

                    runOnUiThread {
                        val bioPrefs = getSharedPreferences("BIO", Context.MODE_PRIVATE)
                        bioPrefs.edit()
                            .putBoolean("IS_Bio", true)
                            .putBoolean("BIO_AUTH_DONE", true)
                            .apply()

                        Constants.Biometric_checked = true
                        Constants.is_biometric = true

                        check_Bio_metric()
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    runOnUiThread {
                        AndroidUtils.showToast("Authentication failed. Please try again.", this@LoginActivity)
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    runOnUiThread {
                        if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                            errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                            Log.d("Fingerprint", "User cancelled biometric authentication")
                        } else {
                            AndroidUtils.showToast("Biometric error: $errString", this@LoginActivity)
                        }
                    }
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Touch ID for \"LexiZ Lawyers\"")
            .setSubtitle("Authenticate through Biometrics")
            .setDeviceCredentialAllowed(true)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun Dashboard_data(jsonArray_Dashboard: JSONArray) {
        Constants.MYDAYCARDS.clear()
        Constants.KPICARDS.clear()

        val skipList = HashSet(Arrays.asList("timesheets", "newclients", "groups", "teammembers"))

        for (i in 0 until jsonArray_Dashboard.length()) {
            val jsonObject = jsonArray_Dashboard.getJSONObject(i)
            val type = jsonObject.getString("type")
            val jsonArray_myday = jsonObject.getJSONArray("options")

            for (j in 0 until jsonArray_myday.length()) {
                val jsonObject1 = jsonArray_myday.getJSONObject(j)
                val name = jsonObject1.getString("name")

                if (skipList.contains(name.lowercase())) {
                    continue
                }

                val dashboardModel = Dashboard_Model()
                dashboardModel.name = name
                dashboardModel.sequence = jsonObject1.getInt("sequence")
                dashboardModels.add(dashboardModel)

                if (type == "MYDAY") {
                    Constants.MYDAYCARDS.add(dashboardModel)
                }
                if (type == "KPI") {
                    Constants.KPICARDS.add(dashboardModel)
                }
            }
        }
    }

    private fun saveXmppPreferences() {
        val prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
        var uid = Constants.UID
        if (!Constants.ROLE.equals("admin", ignoreCase = true)) {
            uid = uid + "_" + Constants.USER_ID
        }

        prefs.edit()
            .putString("xmpp_jid", uid)
            .putString("xmpp_password", Constants.TOKEN)
            .putBoolean("xmpp_logged_in", true)
            .apply()

        mConnection = ChatConnection(this)
        chatConnectionService = ChatConnectionService()
        JsonTask().execute(Constants.base_URL + "user/create/")
    }

    private fun checkTermsAndConditions() {
        val termsAndCondition = TermsAndCondition(this)

        termsAndCondition.setOnTermsAndConditionListener(object : TermsAndCondition.OnTermsAndConditionListener {
            override fun onTermsAccepted() {
                Log.d("TermsCheck", "Terms Accepted - proceeding to dashboard")
                saveTcAcceptanceTimestamp()
                Dashboard()
            }

            override fun onTermsDeclined() {
                Log.d("TermsCheck", "Terms Declined - returning to login screen")

                val prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
                prefs.edit()
                    .remove("xmpp_jid")
                    .remove("xmpp_password")
                    .remove("xmpp_logged_in")
                    .remove("EXTRA_CONTACT_JID")
                    .remove("CURRENTCHAT_JID")
                    .apply()

                Constants.Chat_id = ""
                Constants.fromjid = ""
                Constants.isClient_chat = true

                getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit().clear().apply()
                getSharedPreferences("BIO", Context.MODE_PRIVATE).edit().clear().apply()

                Constants.is_biometric = false

                runOnUiThread {
                    isLoginMode = false
                    toggleMode()

                    et_login_email?.setText("")
                    et_full_name?.setText("")
                    et_register_email?.setText("")
                    et_login_password?.setText("")
                }
            }

            override fun onTermsCheckComplete(needsToShow: Boolean) {
                Log.d("TermsCheck", "Terms check complete - needsToShow: $needsToShow")
                if (!needsToShow) {
                    Dashboard()
                }
            }
        })

        Log.d("TermsCheck", "Checking terms - Version: ${Constants.termsVersion}, RequiresAcceptance: ${Constants.requiresTermsAcceptance}")
        termsAndCondition.checkTermsWithData(Constants.termsVersion, Constants.requiresTermsAcceptance)
    }

    private fun saveTcAcceptanceTimestamp() {
        val timestamp = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())

        getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            .edit()
            .putString("tc_accepted_at", timestamp)
            .apply()

        Log.d("TermsCheck", "T&C acceptance saved at: $timestamp")
    }

    private fun adjustCardForTablet() {
        if (!DynamicUtils.isTablet(this)) return

        val density = resources.displayMetrics.density
        val screenWidth = resources.displayMetrics.widthPixels

        val rootLayout = findViewById<LinearLayout>(R.id.ll_login)
        rootLayout?.gravity = android.view.Gravity.TOP or android.view.Gravity.CENTER_HORIZONTAL

        val cardView = findViewById<androidx.cardview.widget.CardView>(R.id.cardview)
        cardView?.post {
            val params = LinearLayout.LayoutParams(
                (screenWidth * 0.82f).toInt(),
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.CENTER_HORIZONTAL
                topMargin = (30 * density).toInt()
                bottomMargin = (20 * density).toInt()
                leftMargin = 0
                rightMargin = 0
            }
            cardView.layoutParams = params
        }

        if (tv_sign_in != null) {
            tv_sign_in?.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 38f)
            val tp = tv_sign_in?.layoutParams as? ViewGroup.MarginLayoutParams
            if (tp != null) {
                tp.topMargin = (20 * density).toInt()
                tp.bottomMargin = (12 * density).toInt()
                tv_sign_in?.layoutParams = tp
            }
        }

        et_login_email?.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 26f)
        et_login_password?.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 26f)
        et_full_name?.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 26f)
        et_register_email?.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 26f)

        scaleAllInputLayouts(26f)

        setInputCardHeight(R.id.login_section, (70 * density).toInt())
        setInputCardHeight(R.id.register_section, (70 * density).toInt())

        val biometricLabel = findViewById<TextView>(R.id.textView2_login)
        biometricLabel?.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 24f)

        val checkboxCard = findViewById<androidx.cardview.widget.CardView>(R.id.checkbox_cardview_login)
        if (checkboxCard != null) {
            val cp = checkboxCard.layoutParams
            cp.width = (44 * density).toInt()
            cp.height = (44 * density).toInt()
            checkboxCard.layoutParams = cp
        }

        if (btn_send_otp != null) {
            btn_send_otp?.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 24f)
            val bp = LinearLayout.LayoutParams(
                (260 * density).toInt(),
                (65 * density).toInt()
            ).apply {
                gravity = android.view.Gravity.CENTER_HORIZONTAL
                topMargin = (20 * density).toInt()
                bottomMargin = (20 * density).toInt()
            }
            btn_send_otp?.layoutParams = bp
        }

        val toggleText = findViewById<TextView>(R.id.tv_toggle_text)
        toggleText?.textSize = DynamicUtils.twenty.toFloat()
        val toggleLink = findViewById<TextView>(R.id.tv_toggle_link)
        toggleLink?.textSize = DynamicUtils.twenty.toFloat()

        scaleStaticTextsInRegisterSection(DynamicUtils.fifteen.toFloat())
    }

    private fun scaleAllInputLayouts(sp: Float) {
        val root = window.decorView.findViewById<ViewGroup>(android.R.id.content)
        scaleInputsRecursive(root, sp)
    }

    private fun scaleInputsRecursive(vg: ViewGroup?, sp: Float) {
        if (vg == null) return
        for (i in 0 until vg.childCount) {
            val v = vg.getChildAt(i)
            if (v is com.google.android.material.textfield.TextInputLayout) {
                v.editText?.textSize = sp
            } else if (v is ViewGroup) {
                scaleInputsRecursive(v, sp)
            }
        }
    }

    private fun setInputCardHeight(sectionId: Int, heightPx: Int) {
        val section = findViewById<ViewGroup>(sectionId) ?: return
        for (i in 0 until section.childCount) {
            val child = section.getChildAt(i)
            if (child is androidx.cardview.widget.CardView) {
                val lp = child.layoutParams
                lp.height = heightPx
                child.layoutParams = lp
            }
        }
    }

    private fun scaleStaticTextsInRegisterSection(sp: Float) {
        val registerSection = findViewById<LinearLayout>(R.id.register_section) ?: return
        scaleStaticTextViewsRecursive(registerSection, sp)
    }

    private fun scaleStaticTextViewsRecursive(vg: ViewGroup, sp: Float) {
        for (i in 0 until vg.childCount) {
            val v = vg.getChildAt(i)
            if (v is TextView && v !is com.google.android.material.textfield.TextInputEditText) {
                val text = v.text
                if (text != null && (text.toString().contains("By signing") || text.toString().contains(" & "))) {
                    v.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, sp)
                }
            } else if (v is ViewGroup) {
                scaleStaticTextViewsRecursive(v, sp)
            }
        }
    }

    private fun openEmailComposer() {
        val supportEmail = "support@lexiz.ai"
        val subject = "Support Request"

        val mailUri = Uri.parse("mailto:$supportEmail?subject=${Uri.encode(subject)}")
        val intent = Intent(Intent.ACTION_SENDTO, mailUri)

        try {
            startActivity(Intent.createChooser(intent, "Send email"))
        } catch (e: android.content.ActivityNotFoundException) {
            AlertDialog.Builder(this)
                .setTitle("No Email App Found")
                .setMessage("Please email us at: $supportEmail")
                .setPositiveButton("Copy Email") { _, _ ->
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("email", supportEmail)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(this, "Email copied to clipboard", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("OK", null)
                .show()
        }
    }

    override fun onClick(view: View?) {}

    private inner class JsonTask : AsyncTask<String, String, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: String?): String {
            try {
                var uid = Constants.UID
                if (!Constants.ROLE.equals("admin", ignoreCase = true)) {
                    uid = uid + "_" + Constants.USER_ID
                }
                ChatConnection.loginUser(this@LoginActivity, uid, Constants.TOKEN)
            } catch (e: Exception) {
                Log.d("Chat Error", "Something went wrong while connecting")
                e.fillInStackTrace()
                chatConnectionService?.stopSelf()
            }
            return ""
        }
    }

    companion object {
        private const val UPDATE_REQUEST_CODE = 101
        
        @JvmStatic
        var chatConnectionService: ChatConnectionService? = null
    }
}
