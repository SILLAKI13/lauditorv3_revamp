package com.digicoffer.lauditor.LoginActivity.ViewModels

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Paint
import android.os.AsyncTask
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
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
import com.digicoffer.lauditor.CommonFiles.TermsAndCondition.TermsAndCondition
import org.json.JSONArray
import org.json.JSONObject
import java.util.ArrayList
import java.util.Arrays
import java.util.HashSet
import java.util.concurrent.Executors

class OtpVerificationActivity : AppCompatActivity(), AsyncTaskCompleteListener {

    var otp = ""
    private var otpBox1: EditText? = null
    private var otpBox2: EditText? = null
    private var otpBox3: EditText? = null
    private var otpBox4: EditText? = null
    private var otpBox5: EditText? = null
    private var otpBox6: EditText? = null
    private lateinit var otpBoxes: Array<EditText>
    
    private var btnCancel: Button? = null
    private var btnVerifyOtp: Button? = null
    private var tvResendOtp: TextView? = null
    private var tvRegisterHere: TextView? = null
    private var progressDialog: Dialog? = null
    private var emailOrMobile: String? = null
    private var isRegister = false
    private var isCheckBoxChecked = false
    private var resendTimer: CountDownTimer? = null
    private var canResend = false
    private var isProgrammaticChange = false

    var dashboardModels = ArrayList<Dashboard_Model>()
    private var mConnection: ChatConnection? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.otpverification)
        window.statusBarColor = ContextCompat.getColor(this, R.color.Blue_text_color)
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = true

        supportActionBar?.hide()

        initializeViews()
        setupOtpBoxes()
        setupClickListeners()

        emailOrMobile = intent.getStringExtra("email")
        isCheckBoxChecked = intent.getBooleanExtra("Check_box", false)
        isRegister = intent.getBooleanExtra("is_register", false)

        startResendTimer()
        canResend = false
    }

    private fun initializeViews() {
        otpBox1 = findViewById(R.id.otpBox1)
        otpBox2 = findViewById(R.id.otpBox2)
        otpBox3 = findViewById(R.id.otpBox3)
        otpBox4 = findViewById(R.id.otpBox4)
        otpBox5 = findViewById(R.id.otpBox5)
        otpBox6 = findViewById(R.id.otpBox6)

        otpBoxes = arrayOf(otpBox1!!, otpBox2!!, otpBox3!!, otpBox4!!, otpBox5!!, otpBox6!!)

        btnCancel = findViewById(R.id.btnCancel)
        btnCancel?.text = "Cancel"
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp)
        btnVerifyOtp?.text = "Verify OTP"
        
        tvResendOtp = findViewById(R.id.tvResendOtp)
        tvResendOtp?.paintFlags = tvResendOtp?.paintFlags?.or(Paint.UNDERLINE_TEXT_FLAG) ?: Paint.UNDERLINE_TEXT_FLAG
        
        tvRegisterHere = findViewById(R.id.tv_register_here)
        tvRegisterHere?.paintFlags = tvRegisterHere?.paintFlags?.or(Paint.UNDERLINE_TEXT_FLAG) ?: Paint.UNDERLINE_TEXT_FLAG
    }

    private fun setupOtpBoxes() {
        for (i in otpBoxes.indices) {
            val index = i

            otpBoxes[i].filters = arrayOf(InputFilter { source, _, _, dest, _, _ ->
                val incoming = source.toString().replace(Regex("[^0-9]"), "")

                if (incoming.length > 1) {
                    handlePaste(incoming)
                    return@InputFilter ""
                }

                if (incoming.length == 1) {
                    if (dest.length >= 1) {
                        isProgrammaticChange = true
                        otpBoxes[index].setText(incoming)
                        otpBoxes[index].setSelection(1)
                        isProgrammaticChange = false
                        if (index < otpBoxes.size - 1) {
                            otpBoxes[index + 1].requestFocus()
                        }
                        return@InputFilter null
                    }
                    return@InputFilter incoming
                }

                null
            })

            otpBoxes[i].addTextChangedListener(OtpTextWatcher(index))

            otpBoxes[i].setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                    val currentBox = v as EditText
                    if (currentBox.text.toString().isEmpty() && index > 0) {
                        otpBoxes[index - 1].setText("")
                        otpBoxes[index - 1].requestFocus()
                        return@OnKeyListener true
                    }
                }
                false
            })

            otpBoxes[i].setOnClickListener { v ->
                val editText = v as EditText
                editText.setSelection(editText.text.length)
            }
        }

        otpBoxes[0].requestFocus()
    }

    private fun setupClickListeners() {
        btnCancel?.setOnClickListener {
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
            finish()
        }

        btnVerifyOtp?.setOnClickListener { verifyOtp() }

        tvResendOtp?.setOnClickListener {
            if (canResend) {
                resendOtp()
            }
        }

        tvRegisterHere?.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("show_register", true)
            }
            Constants.show_register = true
            startActivity(intent)
            finish()
        }
    }

    private fun handlePaste(pastedText: String) {
        val sanitized = pastedText.replace(Regex("[^0-9]"), "")
        if (sanitized.isEmpty()) return

        isProgrammaticChange = true

        for (box in otpBoxes) {
            box.setText("")
        }

        val length = Math.min(sanitized.length, otpBoxes.size)
        for (i in 0 until length) {
            otpBoxes[i].setText(sanitized[i].toString())
        }

        val focusIndex = Math.min(length, otpBoxes.size - 1)
        otpBoxes[focusIndex].requestFocus()

        isProgrammaticChange = false
    }

    private fun startResendTimer() {
        canResend = false
        tvResendOtp?.isEnabled = false
        tvResendOtp?.setTextColor(ContextCompat.getColor(this, R.color.grey))

        resendTimer?.cancel()

        resendTimer = object : CountDownTimer(RESEND_TIMER_DURATION, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                tvResendOtp?.text = "Resend OTP in ${secondsRemaining}s"
            }

            override fun onFinish() {
                canResend = true
                tvResendOtp?.isEnabled = true
                tvResendOtp?.setText(R.string.resend_otp)
                tvResendOtp?.setTextColor(ContextCompat.getColor(this@OtpVerificationActivity, R.color.Primary_new))
            }
        }.start()
    }

    private fun verifyOtp() {
        otp = (otpBoxes[0].text.toString().trim() +
                otpBoxes[1].text.toString().trim() +
                otpBoxes[2].text.toString().trim() +
                otpBoxes[3].text.toString().trim() +
                otpBoxes[4].text.toString().trim() +
                otpBoxes[5].text.toString().trim())

        if (otp.isEmpty()) {
            Toast.makeText(this, "OTP is required", Toast.LENGTH_SHORT).show()
            return
        }

        if (otp.length != 6) {
            Toast.makeText(this, "OTP must be 6 digits", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            Constants.check_url()
            Constants.PROBIZ_TYPE = "PROFESSIONAL"
            Constants.base_URL = Constants.PROF_URL

            val postData = JSONObject()
            postData.put("otp", otp)
            postData.put("plan", "lauditor")

            if (emailOrMobile != null && emailOrMobile!!.contains("@")) {
                postData.put("email", emailOrMobile!!.lowercase())
            } else {
                postData.put("mobile", emailOrMobile)
            }

            Log.d(TAG, "LOGIN REQUEST: $postData")

            progressDialog = AndroidUtils.get_progress(this)

            WebServiceHelper.callHttpWebService(
                this,
                this,
                WebServiceHelper.RestMethodType.POST,
                "v2/login",
                "LOGIN",
                postData.toString()
            )

        } catch (e: Exception) {
            dismissDialog()
            Toast.makeText(this, "Request error", Toast.LENGTH_SHORT).show()
            Log.e(TAG, e.message, e)
        }
    }

    fun Dashboard() {
        AndroidUtils.updateCachedUserData(this)
        Constants.MYDAYCARDS.clear()
        Constants.KPICARDS.clear()
        Constants.check_url()
        val jsonObject = JSONObject()
        Constants.base_URL = Constants.PROF_URL
        WebServiceHelper.callHttpWebService(
            this,
            this,
            WebServiceHelper.RestMethodType.GET,
            Constants.Dashboard ?: "",
            "Dashboard",
            jsonObject.toString()
        )
    }

    fun callAcceptTC(versionName: String) {
        Constants.PROBIZ_TYPE = "PROFESSIONAL"
        Constants.base_URL = Constants.PROF_URL
        val postData = JSONObject()
        progressDialog = AndroidUtils.get_progress(this)
        try {
            postData.put("version", versionName)
            WebServiceHelper.callHttpWebService(
                this,
                this,
                WebServiceHelper.RestMethodType.POST,
                "terms/accept",
                "Accept TC",
                postData.toString()
            )
            Log.d("Accept_TC", postData.toString())
        } catch (e: Exception) {
            e.printStackTrace()
            AndroidUtils.showToast("Error accepting terms", this)
            dismissDialog()
        }
    }

    override fun onClick(view: View?) {
        // unused
    }

    override fun onAsyncTaskComplete(httpResult: HttpResultDo?) {
        dismissDialog()

        if (httpResult == null) {
            Toast.makeText(this, "No server response", Toast.LENGTH_SHORT).show()
            return
        }

        val requestType = httpResult.requestType ?: ""

        if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                val result = JSONObject(httpResult.responseContent ?: "")

                if (requestType == "LOGIN") {
                    handleLoginResponse(result)
                } else if (requestType == "Accept TC") {
                    val msg = result.getString("msg")
                    Log.d("Terms Accept", msg)
                } else if (requestType == "Dashboard") {
                    handleDashboardResponse(result)
                } else if (requestType == "RESEND_OTP") {
                    handleResendOtpResponse(result)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                AndroidUtils.showAlert("Error: ${e.message}", this)
            }
        } else if (requestType == "Dashboard" && httpResult.status_code == 401) {
            if (!Constants.Email.isNullOrEmpty() && otp.isNotEmpty()) {
                verifyOtp()
            }
        } else {
            try {
                val result = JSONObject(httpResult.responseContent ?: "")
                AndroidUtils.showErrorAlert(result.optString("msg", "An error occurred"), this)
            } catch (e: Exception) {
                AndroidUtils.showErrorAlert(httpResult.responseContent, this)
            }
        }
    }

    private fun handleLoginResponse(result: JSONObject) {
        try {
            Constants.Firm_ids.clear()
            Constants.Firm_names.clear()

            if (!result.getBoolean("error")) {
                Constants.forgot_pwd_request = false
                val probizData = JSONObject(result.getString("data"))
                Constants.jsonObject_dashboard = probizData
                if (!probizData.getString("plan").equals("lauditor", ignoreCase = true)) {
                    AndroidUtils.showAlert("Account not found", this)
                    return
                }

                val email = emailOrMobile ?: ""
                val password = otp
                val prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
                prefs.edit()
                    .putString("email", email.lowercase())
                    .putString("password", password)
                    .putBoolean("isLogin", true)
                    .putString("login_method", Constants.LOGIN_METHOD)
                    .putString("proBizType", Constants.PROBIZ_TYPE)
                    .apply()

                Constants.is_biometric = isCheckBoxChecked
                Constants.NAME = probizData.getString("name")
                Constants.USER_ID = probizData.getString("user_id")
                Constants.termsVersion = probizData.getString("termsVersion")
                Constants.requiresTermsAcceptance = probizData.optBoolean("requiresTermsAcceptance")
                Constants.UID = probizData.getString("uid")
                Constants.OLD_PASSWORD = otp
                Constants.PK = probizData.getString("pk")
                Constants.PASSWORD_MODE = probizData.getString("password_mode")
                Constants.IS_ADMIN = probizData.getBoolean("admin")
                Constants.FIRM_NAME = probizData.getString("firm_name")
                Constants.ROLE = probizData.getString("role")
                Constants.CATEGORY = probizData.optString("category")
                Constants.Groups = probizData.getJSONArray("groups")
                Constants.FirmEmail = probizData.optString("email")
                Constants.Refresh_token = probizData.optString("refresh_token")
                Constants.TOKEN = probizData.optString("access_token")
                Constants.Email = email.lowercase()

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
                    Constants.is_active = subscription.optBoolean("is_active")
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
                val list = ArrayList<FirmsDo>()

                val adminJsonArray = probizData.getJSONArray("firms")
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

                BioMetricAccess()
                Dashboard()
                Constants.IS_MyDay = true

            } else if (result.getBoolean("error") && !result.has("firms")) {
                val errorMsg = result.getString("msg")
                if (isRegister && errorMsg.lowercase().contains("not found")) {
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        verifyOtp()
                    }, 2000)
                    return
                }
                AndroidUtils.showAlert(errorMsg, this)
            } else {
                handleFirmsResponse(result)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkTermsAndConditions() {
        val termsAndCondition = TermsAndCondition(this)

        termsAndCondition.setOnTermsAndConditionListener(object : TermsAndCondition.OnTermsAndConditionListener {
            override fun onTermsAccepted() {
                Log.d("TermsCheck", "Terms Accepted - proceeding to dashboard")
                Dashboard()
            }

            override fun onTermsDeclined() {
                Log.d("TermsCheck", "Terms Declined - navigating to login screen")

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
                Constants.Email = ""

                getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit().clear().apply()
                getSharedPreferences("BIO", Context.MODE_PRIVATE).edit().clear().apply()

                Constants.is_biometric = false

                val intent = Intent(this@OtpVerificationActivity, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("show_register", false)
                }
                startActivity(intent)
                finish()
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

    private fun BioMetricAccess() {
        val token = Constants.TOKEN
        val email = Constants.Email
        val password = otp
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

    private fun handleFirmsResponse(result: JSONObject) {
        try {
            Constants.forgot_pwd_request = false
            if (result.has("firms")) {
                Constants.Firm_ids.clear()
                Constants.Firm_names.clear()
                val list = ArrayList<FirmsDo>()

                val firms = result.getJSONObject("firms")
                val adminJsonArray = firms.getJSONArray("lauditor")

                if (adminJsonArray.length() == 0) {
                    AndroidUtils.showAlert("Account not found", this)
                } else if (adminJsonArray.length() > 1) {
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
                    AndroidUtils.showAlert("Multiple firms found. Please contact support.", this)
                } else {
                    val obj = adminJsonArray.getJSONObject(0)
                    val userId = obj.getString("id")

                    val postData = JSONObject()
                    postData.put("email", emailOrMobile?.lowercase())
                    postData.put("userid", userId)
                    postData.put("otp", otp)

                    WebServiceHelper.callHttpWebService(
                        this,
                        this,
                        WebServiceHelper.RestMethodType.POST,
                        "login",
                        "LOGIN",
                        postData.toString()
                    )
                }
            } else {
                val errorMsg = if (result.has("plan") && result.getString("plan") == "lauditor") {
                    result.getString("msg")
                } else {
                    "Account not found"
                }
                AndroidUtils.showAlert(errorMsg, this)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleDashboardResponse(result: JSONObject) {
        try {
            if (result.optBoolean("error") && result.optString("msg").lowercase().contains("token")) {
                Constants.Valid_Token = false

                if (!Constants.Email.isNullOrEmpty() && otp.isNotEmpty()) {
                    verifyOtp()
                    return
                }

                AndroidUtils.showAlert("Session expired. Please login again.", this)
                return
            }

            Constants.Valid_Token = true

            val dashboardArray = result.getJSONArray("cards")
            DashboardData(dashboardArray)
            Constants.loginActivity?.syncDevice()
            saveXmppPreferences()
            startActivity(Intent(this, MainActivity::class.java))
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                Constants.mainActivity?.registerPendingFCMToken()
            }, 2000)
            finish()

        } catch (e: Exception) {
            e.printStackTrace()
            AndroidUtils.showAlert("Error loading dashboard", this)
        }
    }

    private fun handleResendOtpResponse(result: JSONObject) {
        try {
            if (!result.getBoolean("error")) {
                Toast.makeText(this, "OTP resent successfully", Toast.LENGTH_SHORT).show()
                clearOtpBoxes()
                startResendTimer()
            } else {
                val errorMsg = result.optString("msg", "Failed to resend OTP")
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun DashboardData(jsonArrayDashboard: JSONArray) {
        Constants.MYDAYCARDS.clear()
        Constants.KPICARDS.clear()

        val skipList = HashSet(Arrays.asList("timesheets", "newclients", "groups", "teammembers"))

        for (i in 0 until jsonArrayDashboard.length()) {
            val jsonObject = jsonArrayDashboard.getJSONObject(i)
            val type = jsonObject.getString("type")
            val jsonArrayMyday = jsonObject.getJSONArray("options")

            for (j in 0 until jsonArrayMyday.length()) {
                val jsonObject1 = jsonArrayMyday.getJSONObject(j)
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

    private fun resendOtp() {
        if (!canResend) {
            return
        }

        Constants.check_url()
        Constants.PROBIZ_TYPE = "PROFESSIONAL"
        Constants.base_URL = Constants.PROF_URL
        try {
            val postData = JSONObject()
            postData.put("plan", "lauditor")

            if (emailOrMobile != null && emailOrMobile!!.contains("@")) {
                postData.put("email", emailOrMobile!!.lowercase())
            } else {
                postData.put("mobile", emailOrMobile)
            }

            progressDialog = AndroidUtils.get_progress(this)

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
            Toast.makeText(this, "Failed to resend OTP", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Resend OTP error", e)
        }
    }

    private fun clearOtpBoxes() {
        isProgrammaticChange = true
        for (box in otpBoxes) {
            box.setText("")
        }
        isProgrammaticChange = false
        otpBoxes[0].requestFocus()
    }

    private fun dismissDialog() {
        if (progressDialog != null && progressDialog!!.isShowing) {
            AndroidUtils.dismiss_dialog(progressDialog)
        }
    }

    override fun onDestroy() {
        resendTimer?.cancel()
        dismissDialog()
        super.onDestroy()
    }

    private inner class OtpTextWatcher(private val currentIndex: Int) : TextWatcher {
        private var previousText = ""

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            previousText = s.toString()
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            if (isProgrammaticChange) {
                return
            }

            val text = s.toString()

            if (text.length == 1 && previousText.isEmpty()) {
                if (currentIndex < otpBoxes.size - 1) {
                    otpBoxes[currentIndex + 1].requestFocus()
                }
            } else if (text.length == 1 && previousText.isNotEmpty()) {
                if (currentIndex < otpBoxes.size - 1) {
                    otpBoxes[currentIndex + 1].requestFocus()
                }
            }
        }
    }

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
                ChatConnection.loginUser(this@OtpVerificationActivity, uid, Constants.TOKEN)
            } catch (e: Exception) {
                Log.d("Chat Error", "Something went wrong while connecting")
                e.printStackTrace()
                chatConnectionService?.stopSelf()
            }
            return ""
        }
    }

    companion object {
        private const val TAG = "OtpVerification"
        private const val RESEND_TIMER_DURATION = 60000L
        
        @JvmStatic
        var chatConnectionService: ChatConnectionService? = null
    }
}
