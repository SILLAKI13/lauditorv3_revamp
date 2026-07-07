package com.digicoffer.lauditor.LoginActivity.ViewModels;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class biometric_page extends AppCompatActivity {

    Button tv_log_on, btn_biometric, btn_password;
    BiometricPrompt biometricPrompt;
    BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.biometric_nav_layout);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        tv_log_on = findViewById(R.id.tv_log_on);
        btn_biometric = findViewById(R.id.btn_biometric);
        btn_password = findViewById(R.id.btn_password);

        tv_log_on.setText(R.string.login_to_lexiz_lawyers);
        tv_log_on.setTextSize(DynamicUtils.twenty);
        btn_biometric.setText(R.string.login_with_biometric);
//        btn_biometric.setBackground(getDrawable(R.drawable.rectangular_complete_blue_background));
        btn_password.setText(R.string.login_with_otp);

        btn_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogout();
            }
        });
        btn_biometric.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm_login();
            }
        });
    }
    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        finishAffinity();
    }

    private void CheckBiometric() {
        BiometricManager biometricManager = BiometricManager.from(this);

        int canAuthenticate = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG |
                        BiometricManager.Authenticators.BIOMETRIC_WEAK |
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
        );
        switch (canAuthenticate) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                // Device supports biometrics AND at least one is enrolled
                Log.d("Fingerprint", "Biometric authentication is available.");
                break;

            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                // Device has no fingerprint sensor
                Log.d("Fingerprint", "No fingerprint sensor.");
                AndroidUtils.showAlert("Please enable/add Biometric in your device.", this, "Biometric Authentication");
                break;

            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                // Sensor present but not working
                Log.d("Fingerprint", "Fingerprint sensor not working.");
                AndroidUtils.showAlert("Please enable/add Biometric in your device.", this, "Biometric Authentication");
                break;

            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                // Fingerprint supported but user has not enrolled any fingerprint
                Log.d("Fingerprint", "Fingerprint not enrolled.");
                AndroidUtils.showAlert("Please enable/add Biometric in your device.", this, "Biometric Authentication");
                break;

            default:
                AndroidUtils.showAlert("Please enable/add Biometric in your device.", this, "Biometric Authentication");
                break;
        }
    }

    private void confirm_login() {
        CheckBiometric();
//        Executor executor = ContextCompat.getMainExecutor(this);
        Executor executor = Executors.newSingleThreadExecutor();
        biometricPrompt = new BiometricPrompt(biometric_page.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
//                AndroidUtils.showToast("Mobile Password Verified successfully", getApplicationContext());
                Log.d("Fingerprint_success", "Mobile Password Verified successfully");
                // Get the SharedPreferences object
                SharedPreferences sharedPreferences = getSharedPreferences("BIO", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.putBoolean("IS_Bio", true);          // biometric enabled
                editor.putBoolean("BIO_AUTH_DONE", true);   // biometric verified THIS session

                editor.apply();

                Constants.Biometric_checked = true;
                Constants.is_biometric = true;

                check_Bio_metric();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
            }
        });
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Touch ID for \"LexiZ Lawyers\"")
                .setSubtitle("Authenticate through Biometrics.").setDeviceCredentialAllowed(true).build();
        biometricPrompt.authenticate(promptInfo);

    }

    public void check_Bio_metric() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences sharedPreferences_bio = getSharedPreferences("BIO", Context.MODE_PRIVATE);

//        Constants.Biometric_checked = sharedPreferences_bio.getBoolean("IS_Bio", false);
        Constants.is_biometric = sharedPreferences.getBoolean("Check_box", false);

        Constants.Email = sharedPreferences.getString("email", "");
        String password = sharedPreferences.getString("password", "");
//        Constants.Firm_ids=
//        Constants.Firm_names=
        Constants.firm_id = sharedPreferences.getString("firm_id","");
        String token = sharedPreferences.getString("Token", "");
        String responseJson = sharedPreferences.getString("Json_key", "");

        if (Constants.is_biometric) {
            if (Constants.Biometric_checked) {
                try {
                    JSONObject userJson = new JSONObject(responseJson);

//                    firm_list_name=userJson.getString("firm_name");
                    Constants.NAME = userJson.getString("name");
                    Constants.NAME_NEW = userJson.getString("name");
                    Constants.USER_ID = userJson.getString("user_id");
                    Constants.UID = userJson.getString("uid");
                    Constants.OLD_PASSWORD = password;
                    Constants.PK = userJson.getString("pk");
                    String namesString = sharedPreferences.getString("firmNames", "[]");
                    String idsString = sharedPreferences.getString("firmIds", "[]");

                    JSONArray namesArray = new JSONArray(namesString);
                    JSONArray idsArray = new JSONArray(idsString);

                    Constants.Firm_names.clear();
                    Constants.Firm_ids.clear();

                    for (int i = 0; i < namesArray.length(); i++) {
                        Constants.Firm_names.add(namesArray.getString(i));
                    }

                    for (int i = 0; i < idsArray.length(); i++) {
                        Constants.Firm_ids.add(idsArray.getString(i));
                    }
                    Constants.PASSWORD_MODE = userJson.getString("password_mode");
                    Constants.IS_ADMIN = userJson.getBoolean("admin");
                    Constants.FIRM_NAME = userJson.getString("firm_name");
                    Constants.ROLE = userJson.getString("role");
                    Constants.Groups = userJson.getJSONArray("groups");
                    Constants.FirmEmail = userJson.optString("email");
                    Constants.CATEGORY = userJson.optString("category");
//                    Constants.Email = email;
                    Constants.isAdmin = false;
                    if (Constants.ROLE.equals("AAM")) {
                        Constants.isAdmin = true;
                    } else if (Constants.Groups.length() == 1 && Constants.Groups.getString(0).equals("AAM")) {
                        Constants.isAdmin = true;
                    } else {
                        Constants.isAdmin = false;
                    }

                    Constants.TOKEN = token;

                    // Update UI
//                    checkBox.setChecked(true);
//                    tet_email.setText(email);
//                    tet_password.setText(password);

                    // Go to dashboard if token is valid, else login
                    // Always try dashboard first when biometric login
                    if (!token.isEmpty()) {
//                        Constants.TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1aWQiOiJBQ0IxMEE2QjUzRjlEMjdEIiwiYWRtaW4iOmZhbHNlLCJwbGFuIjoibGF1ZGl0b3IiLCJyb2xlIjoiU1UiLCJuYW1lIjoiU291bmRhcnlhIERpZ2ljb2ZmZXIgViIsInVzZXJfaWQiOiI2ODI0NGQwMGExZGI3MjA1MDI1OTZlMTQiLCJleHAiOjE3Njk3MTkyMzB9.zX1acAr2mYxcvBqvtZK__oUVsI5oCge_PQqJYg8cd1I";
                        Constants.loginActivity.Dashboard();
                    } else if (!Constants.Email .isEmpty() && !password.isEmpty()) {
//                        Constants.loginActivity.handleLoginOTP();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
//                    AndroidUtils.showAlert("Error parsing saved user data", this);
                }
            } else {
                // Go to biometric page
                SharedPreferences bioPrefs = getSharedPreferences("BIO", MODE_PRIVATE);

                boolean isBioEnabled = bioPrefs.getBoolean("IS_Bio", false);
                boolean bioDone = bioPrefs.getBoolean("BIO_AUTH_DONE", false);

                if (isBioEnabled && !bioDone) {
                    startActivity(new Intent(this, biometric_page.class));
                    finish();
                }

            }
        }
    }

    private void performLogout() {
        Constants.isClient_chat = true;
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor1 = sharedPreferences.edit();
        editor1.clear();
        editor1.apply();
//        SharedPreferences sharedPreferences_bio = getSharedPreferences("BIO", Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences_bio.edit();
//        editor.putBoolean("IS_Bio", false);
//        editor.putBoolean("BIO_AUTH_DONE", false);
//        editor.apply();
        Constants.is_biometric = false;
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}