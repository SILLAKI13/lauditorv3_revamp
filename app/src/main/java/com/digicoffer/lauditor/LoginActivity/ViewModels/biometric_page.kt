package com.digicoffer.lauditor.LoginActivity.ViewModels

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils
import com.digicoffer.lauditor.R
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class biometric_page : AppCompatActivity() {

    private var tvLogOn: Button? = null
    private var btnBiometric: Button? = null
    private var btnPassword: Button? = null
    private var biometricPrompt: BiometricPrompt? = null
    private var promptInfo: BiometricPrompt.PromptInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.biometric_nav_layout)
        supportActionBar?.hide()

        tvLogOn = findViewById(R.id.tv_log_on)
        btnBiometric = findViewById(R.id.btn_biometric)
        btnPassword = findViewById(R.id.btn_password)

        tvLogOn?.text = getString(R.string.login_to_lexiz_lawyers)
        tvLogOn?.textSize = DynamicUtils.twenty.toFloat()
        btnBiometric?.text = getString(R.string.login_with_biometric)
        btnPassword?.text = getString(R.string.login_with_otp)

        btnPassword?.setOnClickListener { performLogout() }
        btnBiometric?.setOnClickListener { confirmLogin() }
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        finishAffinity()
    }

    private fun CheckBiometric() {
        val biometricManager = BiometricManager.from(this)

        val canAuthenticate = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.BIOMETRIC_WEAK or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        when (canAuthenticate) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d("Fingerprint", "Biometric authentication is available.")
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Log.d("Fingerprint", "No fingerprint sensor.")
                AndroidUtils.showAlert("Please enable/add Biometric in your device.", this, "Biometric Authentication")
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Log.d("Fingerprint", "Fingerprint sensor not working.")
                AndroidUtils.showAlert("Please enable/add Biometric in your device.", this, "Biometric Authentication")
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Log.d("Fingerprint", "Fingerprint not enrolled.")
                AndroidUtils.showAlert("Please enable/add Biometric in your device.", this, "Biometric Authentication")
            }
            else -> {
                AndroidUtils.showAlert("Please enable/add Biometric in your device.", this, "Biometric Authentication")
            }
        }
    }

    private fun confirmLogin() {
        CheckBiometric()
        val executor: Executor = Executors.newSingleThreadExecutor()
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Log.d("Fingerprint_success", "Mobile Password Verified successfully")
                
                val sharedPreferences = getSharedPreferences("BIO", Context.MODE_PRIVATE)
                sharedPreferences.edit().apply {
                    putBoolean("IS_Bio", true)
                    putBoolean("BIO_AUTH_DONE", true)
                }.apply()

                Constants.Biometric_checked = true
                Constants.is_biometric = true

                checkBioMetric()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
            }
        })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Touch ID for \"LexiZ Lawyers\"")
            .setSubtitle("Authenticate through Biometrics.")
            .setDeviceCredentialAllowed(true)
            .build()

        biometricPrompt?.authenticate(promptInfo!!)
    }

    fun checkBioMetric() {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        Constants.is_biometric = sharedPreferences.getBoolean("Check_box", false)
        Constants.Email = sharedPreferences.getString("email", "")
        val password = sharedPreferences.getString("password", "")
        Constants.firm_id = sharedPreferences.getString("firm_id", "")
        val token = sharedPreferences.getString("Token", "")
        val responseJson = sharedPreferences.getString("Json_key", "")

        if (Constants.is_biometric) {
            if (Constants.Biometric_checked) {
                try {
                    val userJson = JSONObject(responseJson ?: "")

                    Constants.NAME = userJson.getString("name")
                    Constants.NAME_NEW = userJson.getString("name")
                    Constants.USER_ID = userJson.getString("user_id")
                    Constants.UID = userJson.getString("uid")
                    Constants.OLD_PASSWORD = password
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
                    Constants.FirmEmail = userJson.optString("email")
                    Constants.CATEGORY = userJson.optString("category")
                    Constants.isAdmin = false
                    
                    if (Constants.ROLE == "AAM") {
                        Constants.isAdmin = true
                    } else if (Constants.Groups.length() == 1 && Constants.Groups.getString(0) == "AAM") {
                        Constants.isAdmin = true
                    } else {
                        Constants.isAdmin = false
                    }

                    Constants.TOKEN = token

                    if (!token.isNullOrEmpty()) {
                        Constants.loginActivity?.Dashboard()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            } else {
                val bioPrefs = getSharedPreferences("BIO", Context.MODE_PRIVATE)
                val isBioEnabled = bioPrefs.getBoolean("IS_Bio", false)
                val bioDone = bioPrefs.getBoolean("BIO_AUTH_DONE", false)

                if (isBioEnabled && !bioDone) {
                    val intent = Intent(this, biometric_page::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    private fun performLogout() {
        Constants.isClient_chat = true
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
        Constants.is_biometric = false
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
