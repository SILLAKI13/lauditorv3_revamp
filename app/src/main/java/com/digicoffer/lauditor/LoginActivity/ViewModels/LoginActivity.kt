package com.digicoffer.lauditor.LoginActivity.ViewModels

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.FirebaseInitHelper
import com.digicoffer.lauditor.CommonFiles.TermsAndCondition.TermsAndCondition
import com.digicoffer.lauditor.LoginActivity.Models.Dashboard_Model
import com.digicoffer.lauditor.LoginActivity.Models.FirmsDo
import com.digicoffer.lauditor.MainActivity
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.google.android.gms.tasks.Task
import com.google.android.play.core.appupdate.AppUpdateInfo
import android.graphics.Rect
import android.os.Build
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class LoginActivity : AppCompatActivity(), AsyncTaskCompleteListener, View.OnClickListener {

    private var et_login_email: TextInputEditText? = null
    private var et_login_password: TextInputEditText? = null
    private var btn_send_otp: Button? = null
    private var tv_forgot_password: TextView? = null
    private var tv_use_password_otp_mode: TextView? = null
    private var password_section: LinearLayout? = null
    private var tv_sign_in: TextView? = null
    private var login_section: LinearLayout? = null
    private var register_section: LinearLayout? = null
    private var et_full_name: TextInputEditText? = null
    private var et_register_email: TextInputEditText? = null
    private var et_register_phone: TextInputEditText? = null
    private var cbTermsCondition: CheckBox? = null
    private var tv_terms_full: TextView? = null
    private var ll_termsCondition: LinearLayout? = null
    private var tv_toggle_text: TextView? = null
    private var tv_toggle_link: TextView? = null
    private var tv_contact_support: TextView? = null
    private var ll_contact_support: LinearLayout? = null

    private var sp_firm: Spinner? = null
    private var spinner_firm_view: TextView? = null
    private var tv_multi_firm_msg: TextView? = null
    private var firm_layout: LinearLayout? = null

    private var login_biometric_section: LinearLayout? = null
    private var checkBox: CheckBox? = null
    private var tv_biometric_info: TextView? = null

    private var progressDialog: Dialog? = null
    private var isLoginMode by mutableStateOf(true)
    private var isPasswordMode by mutableStateOf(false)
    private val firmList = androidx.compose.runtime.mutableStateListOf<FirmsDo>()
    private var selectedFirmId = ""
    private var fullText = "By signing-in, you agree to our T&Cs and Privacy Policy"

    private lateinit var appUpdateManager: AppUpdateManager
    private var isForceUpdateRequired = false
    private var comingFromPasswordReset = false
    private var fcmToken: String = ""

    private var composeEmailValue by mutableStateOf("")
    private var composePasswordValue by mutableStateOf("")
    private var composeFullNameValue by mutableStateOf("")
    private var composeRegisterEmailValue by mutableStateOf("")
    private var composeRegisterPhoneValue by mutableStateOf("")
    private var composeSelectedFirmState by mutableStateOf<String?>(null)
    private var composeBiometricCheckedState by mutableStateOf(false)
    private var composeTermsAcceptedState by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Constants.loginActivity = this

        val composeView = androidx.compose.ui.platform.ComposeView(this).apply {
            setContent {
                com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme {
                    com.digicoffer.lauditor.ui.auth.LoginScreen(
                        emailOrMobile = composeEmailValue,
                        onEmailOrMobileChange = { composeEmailValue = it },
                        passwordValue = composePasswordValue,
                        onPasswordChange = { composePasswordValue = it },
                        fullNameValue = composeFullNameValue,
                        onFullNameChange = { composeFullNameValue = it },
                        registerEmailValue = composeRegisterEmailValue,
                        onRegisterEmailChange = { composeRegisterEmailValue = it },
                        registerPhoneValue = composeRegisterPhoneValue,
                        onRegisterPhoneChange = { input ->
                            val filtered = input.filter { it.isDigit() || it == '+' }
                            if (filtered.length <= 10) {
                                composeRegisterPhoneValue = filtered
                            }
                        },
                        isLoginMode = isLoginMode,
                        isPasswordMode = isPasswordMode,
                        onTogglePasswordModeClick = {
                            isPasswordMode = !isPasswordMode
                        },
                        onToggleModeClick = {
                            toggleMode()
                        },
                        firmList = firmList.map { it.getName() ?: "" },
                        selectedFirm = composeSelectedFirmState,
                        onFirmSelected = { selected ->
                            composeSelectedFirmState = selected
                            val match = firmList.find { it.getName() == selected }
                            selectedFirmId = match?.value ?: ""
                        },
                        isBiometricAvailable = (login_biometric_section?.visibility == View.VISIBLE),
                        biometricChecked = composeBiometricCheckedState,
                        onBiometricCheckedChange = {
                            composeBiometricCheckedState = it
                        },
                        termsAccepted = composeTermsAcceptedState,
                        onTermsAcceptedChange = {
                            composeTermsAcceptedState = it
                        },
                        isLoading = false,
                        onSubmitClick = {
                            if (isLoginMode) {
                                if (isPasswordMode) handleLoginPassword() else handleLoginOTP()
                            } else {
                                handleRegisterOTP()
                            }
                        },
                        onForgotPasswordClick = {
                            startActivity(Intent(this@LoginActivity, ForgetPassword::class.java))
                        },
                        onContactSupportClick = {
                            openUrl("https://digicoffer.com/contact.html")
                        },
                        onTermsClick = {
                            openUrl("https://digicoffer.com/digicoffer_terms.html")
                        },
                        onPrivacyClick = {
                            openUrl("https://digicoffer.com/privacy.html")
                        }
                    )
                }
            }
        }
        setContentView(composeView)

        window.statusBarColor = ContextCompat.getColor(this, R.color.blue_pale)
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = true
        supportActionBar?.hide()

        initViews()
        setupListeners()

        Constants.ISPRODUCTION = true
        Constants.IS_STAGING = false
        FirebaseInitHelper.initializeFirebase(this)
        DynamicUtils.loadRefreshDynamicSizes(applicationContext)

        val showRegister = intent.getBooleanExtra("show_register", false)
        if (showRegister && isLoginMode) {
            toggleMode()
        }

        appUpdateManager = AppUpdateManagerFactory.create(this)
        checkForInAppUpdate()

        if (intent.getBooleanExtra("password_reset_success", false)) {
            comingFromPasswordReset = true
            val prefillEmail = intent.getStringExtra("prefill_email")
            if (!prefillEmail.isNullOrEmpty()) {
                Constants.Email = prefillEmail
                composeEmailValue = prefillEmail
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
                composeEmailValue = prefillEmail
            }
        }
    }

    override fun onResume() {
        super.onResume()

        getFCMToken()
        checkForInAppUpdate()

        if (!isForceUpdateRequired) {
            if (intent != null && intent.getBooleanExtra("password_reset_success", false)) {
                comingFromPasswordReset = intent.getBooleanExtra("password_reset_success", false)
                intent.removeExtra("password_reset_success")
            }
            if (comingFromPasswordReset) {
                comingFromPasswordReset = false
                resetToPasswordMode()
                val prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                prefs.edit().remove("pk").remove("user_id").remove("old_password").apply()
            }

            val prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val savedToken = prefs.getString("Token", "") ?: ""
            val savedJson = prefs.getString("Json_key", "") ?: ""

            var fcmNav = intent?.getStringExtra("fcm_navigation")
            if (fcmNav.isNullOrEmpty()) {
                fcmNav = intent?.getStringExtra("navigation")
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
                    Constants.LOGIN_METHOD = prefs.getString("login_method", "email")

                    val namesString = prefs.getString("firmNames", "[]") ?: "[]"
                    val idsString = prefs.getString("firmIds", "[]") ?: "[]"
                    val namesArray = JSONArray(namesString)
                    val idsArray = JSONArray(idsString)
                    Constants.Firm_names.clear()
                    Constants.Firm_ids.clear()
                    for (i in 0 until namesArray.length())
                        Constants.Firm_names.add(namesArray.getString(i))
                    for (i in 0 until idsArray.length())
                        Constants.Firm_ids.add(idsArray.getString(i))

                    Constants.isAdmin = false
                    if ("AAM" == Constants.ROLE) {
                        Constants.isAdmin = true
                    } else if (Constants.Groups.length() == 1 && Constants.Groups.getString(0) == "AAM") {
                        Constants.isAdmin = true
                    }

                    registerFCMTokenWithServer()

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
                sessionPrefs.edit().putBoolean("session_expired", false).apply()

                var fcmNavOnExpiry = intent?.getStringExtra("fcm_navigation")
                if (fcmNavOnExpiry.isNullOrEmpty()) {
                    fcmNavOnExpiry = intent?.getStringExtra("navigation")
                }
                if (!fcmNavOnExpiry.isNullOrEmpty()) {
                    Constants.pendingFcmNavigation = fcmNavOnExpiry
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
                Constants.LOGIN_METHOD = sharedPreferences.getString("login_method", "email")
                Constants.is_active = true

                val namesString = sharedPreferences.getString("firmNames", "[]") ?: "[]"
                val idsString = sharedPreferences.getString("firmIds", "[]") ?: "[]"
                val namesArray = JSONArray(namesString)
                val idsArray = JSONArray(idsString)
                Constants.Firm_names.clear()
                Constants.Firm_ids.clear()
                for (i in 0 until namesArray.length())
                    Constants.Firm_names.add(namesArray.getString(i))
                for (i in 0 until idsArray.length())
                    Constants.Firm_ids.add(idsArray.getString(i))

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

                var pendingNav = intent?.getStringExtra("fcm_navigation")
                if (pendingNav.isNullOrEmpty()) {
                    pendingNav = intent?.getStringExtra("navigation")
                }
                if (!pendingNav.isNullOrEmpty()) {
                    Constants.pendingFcmNavigation = pendingNav
                }

                registerFCMTokenWithServer()

                if (Constants.TOKEN?.isNotEmpty() == true) {
                    Dashboard()
                } else if (Constants.Email?.isNotEmpty() == true) {
                    handleLoginOTP()
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun resetToPasswordMode() {
        isPasswordMode = true
        isLoginMode = true
        fullText = "By signing-in, you agree to our T&Cs and Privacy Policy"
    }

    private fun getFCMToken() {
        com.google.firebase.messaging.FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@addOnCompleteListener
                }
                fcmToken = task.result ?: ""
                val prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                prefs.edit().putString("fcm_token", fcmToken).apply()
            }
    }

    private fun registerFCMTokenWithServer() {
        if (fcmToken.isEmpty()) {
            getFCMToken()
            return
        }

        try {
            val postData = JSONObject()
            postData.put("fcm_token", fcmToken)
            postData.put("user_id", Constants.USER_ID)
            postData.put("platform", "android")
            postData.put("device_id", android.provider.Settings.Secure.getString(
                contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            ))

            Constants.base_URL = Constants.adminBaseURL
            WebServiceHelper.callHttpWebService(
                this,
                this,
                WebServiceHelper.RestMethodType.POST,
                "fcm/register",
                "FCM_REGISTER",
                postData.toString()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initViews() {
        hideFirmSelection()
    }

    private fun setupListeners() {}

    private fun hideFirmSelection() {
        firmList.clear()
        selectedFirmId = ""
        composeSelectedFirmState = null
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
        composeEmailValue = ""
        composePasswordValue = ""
        composeFullNameValue = ""
        composeRegisterEmailValue = ""
        composeRegisterPhoneValue = ""
        hideFirmSelection()

        if (isLoginMode) {
            isLoginMode = false
            isPasswordMode = false
            fullText = "By signing-up, you agree to our T&Cs and Privacy Policy"
        } else {
            isLoginMode = true
            isPasswordMode = true
            fullText = "By signing-in, you agree to our T&Cs and Privacy Policy"
        }
    }

    fun handleLoginOTP() {
        val input = composeEmailValue.ifEmpty { et_login_email?.text?.toString() ?: "" }.trim()
        if (input.isEmpty()) {
            AndroidUtils.showAlert("Please enter your email or mobile number", this)
            return
        }

        try {
            Constants.check_url()
            Constants.PROBIZ_TYPE = "PROFESSIONAL"
            Constants.base_URL = Constants.PROF_URL

            val postData = JSONObject()
            postData.put("plan", "lauditor")

            if (input.contains("@")) {
                postData.put("email", input.lowercase())
            } else {
                postData.put("mobile", input)
            }

            if (selectedFirmId.isNotEmpty()) {
                postData.put("userid", selectedFirmId)
            }

            progressDialog = AndroidUtils.get_progress(this)

            WebServiceHelper.callHttpWebService(
                this,
                this,
                WebServiceHelper.RestMethodType.POST,
                "login",
                "LOGIN_OTP",
                postData.toString()
            )

        } catch (e: Exception) {
            dismissDialog()
            AndroidUtils.showAlert("Error initiating login", this)
        }
    }

    fun handleLoginPassword() {
        val email = composeEmailValue.ifEmpty { et_login_email?.text?.toString() ?: "" }.trim()
        val password = composePasswordValue.ifEmpty { et_login_password?.text?.toString() ?: "" }

        if (email.isEmpty()) {
            AndroidUtils.showAlert("Please enter your email address", this)
            return
        }
        if (password.isEmpty()) {
            AndroidUtils.showAlert("Please enter your password", this)
            return
        }

        try {
            Constants.check_url()
            Constants.PROBIZ_TYPE = "PROFESSIONAL"
            Constants.base_URL = Constants.PROF_URL

            val postData = JSONObject()
            postData.put("email", email.lowercase())
            postData.put("password", password)
            postData.put("plan", "lauditor")

            if (selectedFirmId.isNotEmpty()) {
                postData.put("userid", selectedFirmId)
            }

            progressDialog = AndroidUtils.get_progress(this)

            WebServiceHelper.callHttpWebService(
                this,
                this,
                WebServiceHelper.RestMethodType.POST,
                "v2/login",
                "LOGIN_PASSWORD",
                postData.toString()
            )

        } catch (e: Exception) {
            dismissDialog()
            AndroidUtils.showAlert("Error logging in", this)
        }
    }

    fun handleRegisterOTP() {
        val fullName = composeFullNameValue.ifEmpty { et_full_name?.text?.toString() ?: "" }.trim()
        val email = composeRegisterEmailValue.ifEmpty { et_register_email?.text?.toString() ?: "" }.trim()
        val phone = composeRegisterPhoneValue.ifEmpty { et_register_phone?.text?.toString() ?: "" }.trim()

        if (fullName.isEmpty()) {
            AndroidUtils.showAlert("Please enter your full name", this)
            return
        }
        if (email.isEmpty()) {
            AndroidUtils.showAlert("Please enter your email address", this)
            return
        }
        if (phone.isEmpty()) {
            AndroidUtils.showAlert("Please enter your mobile number", this)
            return
        }
        if (!composeTermsAcceptedState) {
            AndroidUtils.showAlert("Please accept the Terms & Conditions and Privacy Policy to register.", this)
            return
        }

        try {
            Constants.check_url()
            Constants.PROBIZ_TYPE = "PROFESSIONAL"
            Constants.base_URL = Constants.adminBaseURL

            val currentDate = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())

            val postData = JSONObject()
            postData.put("fullname", fullName)
            postData.put("country", "India")
            postData.put("contact_person", fullName)
            postData.put("product", "lauditor")
            postData.put("sub_model", "three_month_free")
            postData.put("is_active_sub", true)
            postData.put("sub_start_date", currentDate)
            postData.put("category", "solo")
            postData.put("reg_city", "")
            postData.put("reg_platform", "android")
            postData.put("email", email.lowercase())
            postData.put("mobile", phone)

            progressDialog = AndroidUtils.get_progress(this)

            WebServiceHelper.callHttpWebService(
                this,
                this,
                WebServiceHelper.RestMethodType.POST,
                "professional/onboard/lexis",
                "REGISTER_OTP",
                postData.toString()
            )

        } catch (e: Exception) {
            dismissDialog()
            AndroidUtils.showAlert("Error initiating registration", this)
        }
    }

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        dismissDialog()

        val requestType = httpResult.requestType ?: ""

        if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                val result = JSONObject(httpResult.responseContent ?: "")

                when (requestType) {
                    "LOGIN_OTP" -> handleLoginOtpResponse(result)
                    "LOGIN_PASSWORD" -> handleLoginPasswordResponse(result)
                    "REGISTER_OTP" -> handleRegisterOtpResponse(result)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                AndroidUtils.showAlert("Error parsing server response", this)
            }
        } else {
            try {
                val result = JSONObject(httpResult.responseContent ?: "")
                val errorMsg = result.optString("msg", "An error occurred")
                AndroidUtils.showAlert(errorMsg, this)
            } catch (e: Exception) {
                AndroidUtils.showAlert(httpResult.responseContent, this)
            }
        }
    }

    private fun handleLoginOtpResponse(result: JSONObject) {
        try {
            if (!result.getBoolean("error")) {
                val email = composeEmailValue.ifEmpty { et_login_email?.text?.toString() ?: "" }.trim()
                Constants.Email = email

                val intent = Intent(this, OtpVerificationActivity::class.java).apply {
                    putExtra("email", email)
                    putExtra("Check_box", composeBiometricCheckedState)
                    putExtra("is_register", false)
                }
                startActivity(intent)
            } else if (result.getBoolean("error") && result.has("firms")) {
                handleFirmsResponse(result)
            } else {
                AndroidUtils.showAlert(result.optString("msg", "Login failed"), this)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleLoginPasswordResponse(result: JSONObject) {
        try {
            if (!result.getBoolean("error")) {
                val probizData = JSONObject(result.getString("data"))
                Constants.jsonObject_dashboard = probizData
                if (!probizData.getString("plan").equals("lauditor", ignoreCase = true)) {
                    AndroidUtils.showAlert("Account not found", this)
                    return
                }

                val email = composeEmailValue.ifEmpty { et_login_email?.text?.toString() ?: "" }.trim()
                val password = composePasswordValue.ifEmpty { et_login_password?.text?.toString() ?: "" }

                val prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
                prefs.edit()
                    .putString("email", email.lowercase())
                    .putString("password", password)
                    .putBoolean("isLogin", true)
                    .putString("login_method", Constants.LOGIN_METHOD)
                    .putString("proBizType", Constants.PROBIZ_TYPE)
                    .apply()

                Constants.is_biometric = composeBiometricCheckedState
                Constants.NAME = probizData.getString("name")
                Constants.USER_ID = probizData.getString("user_id")
                Constants.termsVersion = probizData.getString("termsVersion")
                Constants.requiresTermsAcceptance = probizData.optBoolean("requiresTermsAcceptance")
                Constants.UID = probizData.getString("uid")
                Constants.OLD_PASSWORD = password
                Constants.PK = probizData.getString("pk")
                Constants.PASSWORD_MODE = probizData.getString("password_mode")
                Constants.IS_ADMIN = probizData.getBoolean("admin")
                Constants.FIRM_NAME = probizData.getString("firm_name")
                Constants.ROLE = probizData.getString("role")
                Constants.CATEGORY = probizData.optString("category")
                Constants.Groups = probizData.getJSONArray("groups")
                Constants.FirmEmail = probizData.optString("email")
                Constants.Email = email.lowercase()
                Constants.Refresh_token = probizData.optString("refresh_token")
                Constants.TOKEN = probizData.optString("access_token")

                val subscription = probizData.optJSONObject("subscription")
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
                    Constants.is_active = subscription.optBoolean("is_active", true)
                }

                Constants.User_Allowed = probizData.optInt("user_allowed")

                Constants.isAdmin = false
                if (Constants.ROLE == "AAM") {
                    Constants.isAdmin = true
                } else if (Constants.Groups.length() == 1 && Constants.Groups.getString(0) == "AAM") {
                    Constants.isAdmin = true
                }

                Constants.Firm_ids.clear()
                Constants.Firm_names.clear()
                val adminJsonArray = probizData.optJSONArray("firms")
                if (adminJsonArray != null) {
                    for (i in 0 until adminJsonArray.length()) {
                        val obj = adminJsonArray.getJSONObject(i)
                        val firmName = obj.optString("firmName", "")
                        val firmId = obj.optString("id", "")
                        Constants.Firm_names.add(firmName)
                        Constants.Firm_ids.add(firmId)
                    }
                }

                val myPrefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                myPrefs.edit()
                    .putString("firmNames", JSONArray(Constants.Firm_names).toString())
                    .putString("firmIds", JSONArray(Constants.Firm_ids).toString())
                    .putBoolean("requiresTermsAcceptance", Constants.requiresTermsAcceptance)
                    .putString("termsVersion", Constants.termsVersion)
                    .apply()

                checkPasswordResetMode(probizData)

                if ("reset" == Constants.PASSWORD_MODE) {
                    return
                }

                if ("reset" == Constants.PASSWORD_MODE) {
                    Constants.PK = probizData.getString("pk")
                    Constants.USER_ID = probizData.getString("user_id")
                    Constants.OLD_PASSWORD = password

                    val prefs1 = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                    prefs1.edit()
                        .putString("pk", Constants.PK)
                        .putString("user_id", Constants.USER_ID)
                        .putString("Token", Constants.TOKEN)
                        .putString("refresh_token", Constants.Refresh_token)
                        .putString("password", password)
                        .apply()

                    Log.d("LOGIN_AUDIT", "Force Reset Flow -> password length=${password.length}")

                    val resetIntent = Intent(this@LoginActivity, reset_password_file::class.java).apply {
                        putExtra("reset_mode", true)
                        putExtra("pk", Constants.PK)
                        putExtra("user_id", Constants.USER_ID)
                    }
                    startActivity(resetIntent)
                    finish()
                } else {
                    registerFCMTokenWithServer()
                    Bio_metric_access()
                    Dashboard()
                }

            } else if (result.getBoolean("error") && result.has("firms")) {
                handleFirmsResponse(result)
            } else {
                AndroidUtils.showAlert(result.optString("msg", "Login failed"), this)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleRegisterOtpResponse(result: JSONObject) {
        try {
            val email = composeRegisterEmailValue.ifEmpty { et_register_email?.text?.toString() ?: "" }.trim()
            Constants.Email = email

            if (!result.getBoolean("error")) {
                val intent = Intent(this, OtpVerificationActivity::class.java).apply {
                    putExtra("email", email)
                    putExtra("Check_box", false)
                    putExtra("is_register", true)
                }
                startActivity(intent)
            } else {
                val errorMsg = result.optString("msg", "Registration failed")
                if (errorMsg == "User Already Exists") {
                    val redirectTo = result.optString("redirect_to", "")
                    if (redirectTo == "password") {
                        AndroidUtils.showAlert("Password has been setup. Please login using your password.", this, "Success", Runnable {
                            composeEmailValue = email
                            isLoginMode = true
                            isPasswordMode = true
                            fullText = "By signing-in, you agree to our T&Cs and Privacy Policy"
                        })
                    } else if (redirectTo == "email") {
                        AndroidUtils.showAlert("Email is already registered. Please log in using the OTP sent to your inbox.", this, "Success", Runnable {
                            composeEmailValue = email
                            isLoginMode = true
                            isPasswordMode = false
                            fullText = "By signing-in, you agree to our T&Cs and Privacy Policy"
                            handleLoginOTP()
                        })
                    } else {
                        AndroidUtils.showAlert("Account already exists. Please login via OTP sent to your inbox.", this, "Success", Runnable {
                            composeEmailValue = email
                            isLoginMode = true
                            isPasswordMode = false
                            fullText = "By signing-in, you agree to our T&Cs and Privacy Policy"
                            handleLoginOTP()
                        })
                    }
                } else {
                    AndroidUtils.showAlert(errorMsg, this)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleFirmsResponse(result: JSONObject) {
        try {
            firmList.clear()
            val firmsObj = result.getJSONObject("firms")
            val lauditorFirms = firmsObj.optJSONArray("lauditor")

            if (lauditorFirms != null && lauditorFirms.length() == 1) {
                val obj = lauditorFirms.getJSONObject(0)
                val userId = obj.getString("id")
                selectedFirmId = userId

                val email = composeEmailValue.ifEmpty { et_login_email?.text?.toString() ?: "" }.trim()
                val password = composePasswordValue.ifEmpty { et_login_password?.text?.toString() ?: "" }

                val postData = JSONObject()
                postData.put("email", email.lowercase())
                postData.put("password", password)
                postData.put("userid", userId)
                postData.put("plan", "lauditor")

                progressDialog = AndroidUtils.get_progress(this)

                WebServiceHelper.callHttpWebService(
                    this,
                    this,
                    WebServiceHelper.RestMethodType.POST,
                    "v2/login",
                    "LOGIN_PASSWORD",
                    postData.toString()
                )
            } else if (lauditorFirms != null && lauditorFirms.length() > 1) {
                for (i in 0 until lauditorFirms.length()) {
                    val obj = lauditorFirms.getJSONObject(i)
                    val firmsDo = FirmsDo()
                    firmsDo.setName(obj.getString("firmName"))
                    firmsDo.value = obj.getString("id")
                    firmList.add(firmsDo)
                }
                AndroidUtils.showAlert("This email is associated with multiple firms. Please select a firm to continue.", this)
            } else {
                AndroidUtils.showAlert("No firms found for this account", this)
            }
        } catch (e: Exception) {
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
                val password = composePasswordValue.ifEmpty { et_login_password?.text?.toString() ?: "" }
                Constants.OLD_PASSWORD = password

                val prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                prefs.edit()
                    .putString("pk", Constants.PK)
                    .putString("user_id", Constants.USER_ID)
                    .putString("refresh_token", Constants.Refresh_token)
                    .putString("Token", Constants.TOKEN)
                    .putString("password", password)
                    .apply()

                Log.d("LOGIN_AUDIT", "Force Reset Flow -> password length=${password.length}")

                val resetIntent = Intent(this@LoginActivity, reset_password_file::class.java).apply {
                    putExtra("reset_mode", true)
                    putExtra("pk", Constants.PK)
                    putExtra("user_id", Constants.USER_ID)
                }
                startActivity(resetIntent)
                finish()
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun Bio_metric_access() {
        val token = Constants.TOKEN
        val email = Constants.Email
        val password = composePasswordValue.ifEmpty { et_login_password?.text?.toString() ?: "" }
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putString("email", email?.lowercase())
            putString("password", password)
            putString("Token", token)
            putString("refresh_token", Constants.Refresh_token)
            putString("login_method", Constants.LOGIN_METHOD)
            putString("firm_id", Constants.Firm_id)
            putString("Json_key", Constants.jsonObject_dashboard?.toString() ?: "")
            putBoolean("Check_box", Constants.is_biometric)
        }.apply()
    }

    fun syncDevice() {
        Log.d("LOGIN_AUDIT", "syncDevice called")
    }

    fun Dashboard() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun checkForInAppUpdate() {
        val appUpdateInfoTask: Task<AppUpdateInfo> = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                isForceUpdateRequired = true
                try {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        this,
                        UPDATE_REQUEST_CODE
                    )
                } catch (e: Exception) {
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

    private fun dismissDialog() {
        if (progressDialog != null && progressDialog!!.isShowing) {
            AndroidUtils.dismiss_dialog(progressDialog)
        }
    }

    override fun onClick(v: View) {
        // Not used
    }

    companion object {
        private const val UPDATE_REQUEST_CODE = 500
    }
}
