package com.digicoffer.lauditor.LoginActivity.ViewModels;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.KPICARDS;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.MYDAYCARDS;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.is_biometric;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;

import com.digicoffer.lauditor.LoginActivity.Models.Dashboard_Model;
import com.digicoffer.lauditor.LoginActivity.Models.FirmsDo;
import com.digicoffer.lauditor.MainActivity;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.ChatService.ChatConnection;
import com.digicoffer.lauditor.CommonFiles.ChatService.ChatConnectionService;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.digicoffer.lauditor.CommonFiles.TermsAndCondition.TermsAndCondition;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

public class OtpVerificationActivity extends AppCompatActivity implements AsyncTaskCompleteListener {

    private static final String TAG = "OtpVerification";
    private static final int RESEND_TIMER_DURATION = 60000; // 60 seconds in milliseconds

    String otp = "";
    private EditText otpBox1, otpBox2, otpBox3, otpBox4, otpBox5, otpBox6;
    private EditText[] otpBoxes;
    private Button btnCancel, btnVerifyOtp;
    private TextView tvResendOtp, tvRegisterHere;
    private Dialog progress_dialog;
    private String emailOrMobile;
    private boolean isRegister = false;
    private boolean isCheckBoxChecked = false;
    private CountDownTimer resendTimer;
    private boolean canResend = false;
    private boolean isProgrammaticChange = false;

    ArrayList<Dashboard_Model> dashboardModels = new ArrayList<>();
    private ChatConnection mConnection;
    private static ChatConnectionService chatConnectionService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.otpverification);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.Blue_text_color));
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightStatusBars(true); // white icons on blue bg

// fitsSystemWindows paints blue_pale into the status bar area —
// overlay it with a solid blue view placed directly on the DecorView
//        ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
//        View statusBarOverlay = new View(this);
//        statusBarOverlay.setBackgroundColor(
//                ContextCompat.getColor(this, R.color.blue_pale));
//        int statusBarHeight = 0;
//        int resId = getResources().getIdentifier("status_bar_height", "dimen", "android");
//        if (resId > 0)
//            statusBarHeight = getResources().getDimensionPixelSize(resId);
//        FrameLayout.LayoutParams overlayParams = new FrameLayout.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT, statusBarHeight);
//        decorView.addView(statusBarOverlay, overlayParams);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        initializeViews();
        setupOtpBoxes();
        setupClickListeners();

        // Get email / mobile from previous screen
        emailOrMobile = getIntent().getStringExtra("email");
        isCheckBoxChecked = getIntent().getBooleanExtra("Check_box", false);
        isRegister = getIntent().getBooleanExtra("is_register", false);

        // Initially, resend is available (no timer running)
        startResendTimer();
        canResend = false;
    }

    /* -------------------- View Setup -------------------- */

    private void initializeViews() {
        otpBox1 = findViewById(R.id.otpBox1);
        otpBox2 = findViewById(R.id.otpBox2);
        otpBox3 = findViewById(R.id.otpBox3);
        otpBox4 = findViewById(R.id.otpBox4);
        otpBox5 = findViewById(R.id.otpBox5);
        otpBox6 = findViewById(R.id.otpBox6);

        otpBoxes = new EditText[]{otpBox1, otpBox2, otpBox3, otpBox4, otpBox5, otpBox6};

        btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setText("Cancel");
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        btnVerifyOtp.setText("Verify OTP");
        tvResendOtp = findViewById(R.id.tvResendOtp);
        tvResendOtp.setPaintFlags(tvResendOtp.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvRegisterHere = findViewById(R.id.tv_register_here);
        tvRegisterHere.setPaintFlags(tvRegisterHere.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

//        handlePaste("111111");
//        verifyOtp();

    }

    private void setupOtpBoxes() {
        for (int i = 0; i < otpBoxes.length; i++) {
            final int index = i;

            // Custom filter: intercept paste (multi-char), allow single char
            otpBoxes[i].setFilters(new InputFilter[]{
                    new InputFilter() {
                        @Override
                        public CharSequence filter(CharSequence source, int start, int end,
                                                   android.text.Spanned dest, int dstart, int dend) {
                            String incoming = source.toString().replaceAll("[^0-9]", "");

                            // Multi-character input = paste attempt
                            if (incoming.length() > 1) {
                                handlePaste(incoming);
                                return "";
                            }

                            // Single digit
                            if (incoming.length() == 1) {
                                // If destination already has a character, replace it instead of appending
                                if (dest.length() >= 1) {
                                    isProgrammaticChange = true;
                                    otpBoxes[index].setText(incoming);
                                    otpBoxes[index].setSelection(1);
                                    isProgrammaticChange = false;
                                    // Move to next box if not last
                                    if (index < otpBoxes.length - 1) {
                                        otpBoxes[index + 1].requestFocus();
                                    }
                                    return null; // block further processing since we handled it manually
                                }
                                return incoming;
                            }

                            // Deletion or empty
                            return null;
                        }
                    }
            });

            // TextWatcher for focus navigation only
            otpBoxes[i].addTextChangedListener(new OtpTextWatcher(index));

            // Handle backspace to go to previous box
            otpBoxes[i].setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                        EditText currentBox = (EditText) v;
                        if (currentBox.getText().toString().isEmpty() && index > 0) {
                            otpBoxes[index - 1].setText("");
                            otpBoxes[index - 1].requestFocus();
                            return true;
                        }
                    }
                    return false;
                }
            });

            // Position cursor at end on click
            otpBoxes[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText editText = (EditText) v;
                    editText.setSelection(editText.getText().length());
                }
            });
        }

        otpBox1.requestFocus();
    }

    private void setupClickListeners() {
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = getDefaultSharedPreferences(getApplicationContext());
                prefs.edit()
                        .remove("xmpp_jid")
                        .remove("xmpp_password")
                        .remove("xmpp_logged_in")
                        .remove("EXTRA_CONTACT_JID")
                        .remove("CURRENTCHAT_JID")
                        .apply();
                Constants.Chat_id = "";
                Constants.fromjid = "";
                Constants.isClient_chat = true;
                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor1 = sharedPreferences.edit();
                editor1.clear();
                editor1.apply();
                SharedPreferences sharedPreferences_bio = getSharedPreferences("BIO", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences_bio.edit();
                editor.clear();
                editor.apply();
                Constants.is_biometric = false;
                finish();
            }
        });

        btnVerifyOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyOtp();
            }
        });

        tvResendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canResend) {
                    resendOtp();
                }
            }
        });

        tvRegisterHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to LoginActivity in register mode
                Intent intent = new Intent(OtpVerificationActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                Constants.show_register = true;
                intent.putExtra("show_register", true);
                startActivity(intent);
                finish();
            }
        });
    }

    /* -------------------- Paste Handler -------------------- */

    private void handlePaste(String pastedText) {
        pastedText = pastedText.replaceAll("[^0-9]", "");

        if (pastedText.length() == 0) return;

        isProgrammaticChange = true;

        for (EditText box : otpBoxes) {
            box.setText("");
        }

        int length = Math.min(pastedText.length(), otpBoxes.length);
        for (int i = 0; i < length; i++) {
            otpBoxes[i].setText(String.valueOf(pastedText.charAt(i)));
        }

        int focusIndex = Math.min(length, otpBoxes.length - 1);
        otpBoxes[focusIndex].requestFocus();

        isProgrammaticChange = false;
    }


    /* -------------------- Resend Timer -------------------- */

    private void startResendTimer() {
        canResend = false;
        tvResendOtp.setEnabled(false);
        tvResendOtp.setTextColor(ContextCompat.getColor(this, R.color.grey));

        if (resendTimer != null) {
            resendTimer.cancel();
        }

        resendTimer = new CountDownTimer(RESEND_TIMER_DURATION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsRemaining = millisUntilFinished / 1000;
                tvResendOtp.setText("Resend OTP in " + secondsRemaining + "s");
            }

            @Override
            public void onFinish() {
                canResend = true;
                tvResendOtp.setEnabled(true);
                tvResendOtp.setText(R.string.resend_otp);
                tvResendOtp.setTextColor(ContextCompat.getColor(OtpVerificationActivity.this, R.color.Primary_new));
            }
        };

        resendTimer.start();
    }

    /* -------------------- OTP Verification -------------------- */

    private void verifyOtp() {
        otp = otpBox1.getText().toString().trim()
                + otpBox2.getText().toString().trim()
                + otpBox3.getText().toString().trim()
                + otpBox4.getText().toString().trim()
                + otpBox5.getText().toString().trim()
                + otpBox6.getText().toString().trim();

        // Empty OTP check
        if (otp.isEmpty()) {
            Toast.makeText(this, "OTP is required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Length check
        if (otp.length() != 6) {
            Toast.makeText(this, "OTP must be 6 digits", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Constants.check_url();
            Constants.PROBIZ_TYPE = "PROFESSIONAL";
            Constants.base_URL = Constants.PROF_URL;

            JSONObject postData = new JSONObject();
            postData.put("otp", otp);
            postData.put("plan", "lauditor");

            if (emailOrMobile != null && emailOrMobile.contains("@")) {
                postData.put("email", emailOrMobile.toLowerCase());
            } else {
                postData.put("mobile", emailOrMobile);
            }

            Log.d(TAG, "LOGIN REQUEST: " + postData);

            progress_dialog = AndroidUtils.get_progress(this);

            WebServiceHelper.callHttpWebService(
                    this,
                    this,
                    WebServiceHelper.RestMethodType.POST,
                    "v2/login",
                    "LOGIN",
                    postData.toString()
            );

        } catch (Exception e) {
            dismissDialog();
            Toast.makeText(this, "Request error", Toast.LENGTH_SHORT).show();
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void Dashboard() {
//        Constants.requiresTermsAcceptance = false;
        AndroidUtils.updateCachedUserData(this);
        MYDAYCARDS.clear();
        KPICARDS.clear();
        Constants.check_url();
        JSONObject jsonObject = new JSONObject();
        Constants.base_URL = Constants.PROF_URL;
        WebServiceHelper.callHttpWebService(
                OtpVerificationActivity.this,
                OtpVerificationActivity.this,
                WebServiceHelper.RestMethodType.GET,
                Constants.Dashboard,
                "Dashboard",
                jsonObject.toString()
        );
    }

    /* -------------------- API Callback -------------------- */
    public void callAcceptTC(String versionName) {
        Constants.PROBIZ_TYPE = "PROFESSIONAL";
        Constants.base_URL = Constants.PROF_URL;
        JSONObject postData = new JSONObject();
        progress_dialog = AndroidUtils.get_progress(OtpVerificationActivity.this);
        try {
            postData.put("version", versionName);
            WebServiceHelper.callHttpWebService(OtpVerificationActivity.this, OtpVerificationActivity.this, WebServiceHelper.RestMethodType.POST, "terms/accept", "Accept TC", postData.toString());
            Log.d("Accept_TC", postData.toString());
        } catch (Exception e) {
            e.printStackTrace();
            AndroidUtils.showToast("Error accepting terms", this);
            if (progress_dialog != null && progress_dialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progress_dialog);
            }
        }
    }

    @Override
    public void onClick(View view) {
    }

    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {
        dismissDialog();

        if (httpResult == null) {
            Toast.makeText(this, "No server response", Toast.LENGTH_SHORT).show();
            return;
        }

        if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                JSONObject result = new JSONObject(httpResult.getResponseContent());

                if (httpResult.getRequestType().equals("LOGIN")) {
                    handleLoginResponse(result);
                } else if (httpResult.getRequestType().equals("Accept TC")) {
                    String msg = result.getString("msg");
                    Log.d("Terms Accept", msg);
//                    AndroidUtils.showToast(msg, OtpVerificationActivity.this);
                } else if (httpResult.getRequestType().equals("Dashboard")) {
                    handleDashboardResponse(result);
//                    callAcceptTC("v1.0");
                } else if (httpResult.getRequestType().equals("RESEND_OTP")) {
                    handleResendOtpResponse(result);
                }

            } catch (Exception e) {
                e.printStackTrace();
                AndroidUtils.showAlert("Error: " + e.getMessage(), this);
            }
        } else if (httpResult.getRequestType().equals("Dashboard") && (httpResult.getStatus_code() == 401)) {
            // Token expired, re-login
            if (!Constants.Email.isEmpty() && !otp.isEmpty()) {
                verifyOtp();
            }
        } else {
            try {
                JSONObject result = new JSONObject(httpResult.getResponseContent());
                AndroidUtils.showErrorAlert(result.optString("msg", "An error occurred"), this);
            } catch (Exception e) {
                AndroidUtils.showErrorAlert(httpResult.getResponseContent(), this);
            }
        }
    }

    private void handleLoginResponse(JSONObject result) {
        try {
            Constants.Firm_ids.clear();
            Constants.Firm_names.clear();

            if (!result.getBoolean("error")) {
                Constants.forgot_pwd_request = false;
                JSONObject probiz_data = new JSONObject(result.getString("data"));
                Constants.jsonObject_dashboard = probiz_data;
                if (!probiz_data.getString("plan").equalsIgnoreCase("lauditor")) {
                    AndroidUtils.showAlert("Account not found", this);
                    return;
                }

                // Save credentials
                String email = Objects.requireNonNull(emailOrMobile);
                String password = Objects.requireNonNull(otp);
                SharedPreferences prefs = getDefaultSharedPreferences(getApplicationContext());
                prefs.edit()
                        .putString("email", email.toLowerCase())
                        .putString("password", password)
                        .putBoolean("isLogin", true)
                        .putString("login_method", Constants.LOGIN_METHOD)
                        .putString("proBizType", Constants.PROBIZ_TYPE)
                        .apply();

                // Set constants
                is_biometric = isCheckBoxChecked;
//                Constants.TOKEN = result.getString("token");
                Constants.NAME = probiz_data.getString("name");
                Constants.USER_ID = probiz_data.getString("user_id");
                Constants.termsVersion = probiz_data.getString("termsVersion");
                Constants.requiresTermsAcceptance = probiz_data.optBoolean("requiresTermsAcceptance");
                Constants.UID = probiz_data.getString("uid");
                Constants.OLD_PASSWORD = otp;
                Constants.PK = probiz_data.getString("pk");
                Constants.PASSWORD_MODE = probiz_data.getString("password_mode");
                Constants.IS_ADMIN = probiz_data.getBoolean("admin");
                Constants.FIRM_NAME = probiz_data.getString("firm_name");
                Constants.ROLE = probiz_data.getString("role");
                Constants.CATEGORY = probiz_data.optString("category");
                Constants.Groups = probiz_data.getJSONArray("groups");
                Constants.FirmEmail = probiz_data.optString("email");
                Constants.Refresh_token = probiz_data.optString("refresh_token");
                Constants.TOKEN = probiz_data.optString("access_token");
                Constants.Email = email.toLowerCase();

                // Handle subscription
                JSONObject subscription = probiz_data.optJSONObject("subscription");
                if (subscription != null) {
                    JSONObject features = subscription.optJSONObject("features");
                    if (features != null) {
                        Constants.FEATURES.clear();
                        Iterator<String> keys = features.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            boolean value = features.optBoolean(key, false);
                            Constants.FEATURES.put(key, value);
                        }
                    }
                    Constants.is_active = subscription.optBoolean("is_active");
                }

                Constants.User_Allowed = probiz_data.optInt("user_allowed");

                // Check admin status
                Constants.isAdmin = false;
                if (Constants.ROLE.equals("AAM")) {
                    Constants.isAdmin = true;
                } else if (Constants.Groups.length() == 1 && Constants.Groups.getString(0).equals("AAM")) {
                    Constants.isAdmin = true;
                }

                Constants.Firm_ids.clear();
                Constants.Firm_names.clear();
                ArrayList<FirmsDo> list = new ArrayList<>();

                JSONArray adminJsonArray = probiz_data.getJSONArray("firms");
                // Multiple firms - need to show firm selection
                for (int i = 0; i < adminJsonArray.length(); i++) {
                    JSONObject obj = adminJsonArray.getJSONObject(i);
                    FirmsDo firmsDo = new FirmsDo();

                    String firmName = obj.getString("firmName");
                    String firmId = obj.getString("id");

                    firmsDo.setName(firmName);
                    firmsDo.setValue(firmId);

                    Constants.Firm_names.add(firmName);
                    Constants.Firm_ids.add(firmId);
                    list.add(firmsDo);
                }
                Log.d("Groups_value", "" + Constants.isAdmin);

                // Load dashboard and proceed
                Bio_metric_access();
                Dashboard();
                Constants.IS_MyDay = true;

            } else if (result.getBoolean("error") && (!result.has("firms"))) {
                String error_msg = result.getString("msg");
                if (isRegister && error_msg.toLowerCase().contains("not found")) {
                    // Retry once after 2 seconds — new account may still be provisioning on server
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        verifyOtp();
                    }, 2000);
                    return;
                }
                AndroidUtils.showAlert(error_msg, this);
            } else {
                handleFirmsResponse(result);
            }

        } catch (Exception e) {
            e.printStackTrace();
//            AndroidUtils.showAlert("Error processing login response", this);
        }
    }

    // Add this method to check Terms & Conditions
    // Update this method in OtpVerificationActivity
    private void checkTermsAndConditions() {
        TermsAndCondition termsAndCondition = new TermsAndCondition(this);

        termsAndCondition.setOnTermsAndConditionListener(new TermsAndCondition.OnTermsAndConditionListener() {
            @Override
            public void onTermsAccepted() {
                Log.d("TermsCheck", "Terms Accepted - proceeding to dashboard");
                Dashboard();
            }

            @Override
            public void onTermsDeclined() {
                Log.d("TermsCheck", "Terms Declined - navigating to login screen");

                // Clear all session data
                SharedPreferences prefs = getDefaultSharedPreferences(getApplicationContext());
                prefs.edit()
                        .remove("xmpp_jid")
                        .remove("xmpp_password")
                        .remove("xmpp_logged_in")
                        .remove("EXTRA_CONTACT_JID")
                        .remove("CURRENTCHAT_JID")
                        .apply();

                Constants.Chat_id = "";
                Constants.fromjid = "";
                Constants.isClient_chat = true;
                Constants.Email = "";

                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                sharedPreferences.edit().clear().apply();

                SharedPreferences sharedPreferences_bio = getSharedPreferences("BIO", Context.MODE_PRIVATE);
                sharedPreferences_bio.edit().clear().apply();

                Constants.is_biometric = false;

                // ✅ Navigate to LoginActivity in login mode with fields cleared
                Intent intent = new Intent(OtpVerificationActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("show_register", false); // ensure login mode
                startActivity(intent);
                finish();
            }

            @Override
            public void onTermsCheckComplete(boolean needsToShow) {
                Log.d("TermsCheck", "Terms check complete - needsToShow: " + needsToShow);
                if (!needsToShow) {
                    Dashboard();
                }
            }
        });

        // Check terms with version and requiresTermsAcceptance flag
        Log.d("TermsCheck", "Checking terms - Version: " + Constants.termsVersion + ", RequiresAcceptance: " + Constants.requiresTermsAcceptance);
        termsAndCondition.checkTermsWithData(Constants.termsVersion, Constants.requiresTermsAcceptance);
    }

    private void Bio_metric_access() {
        //when the Check Box is checked then the data is stored in Internal Storage for the Bio-Metric Authentication.
//        if (is_biometric) {
        String Token = Constants.TOKEN;
        String email = Constants.Email;
        String password = otp;
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", email.toLowerCase());
        editor.putString("password", password);
        editor.putString("Token", Token);
        editor.putString("refresh_token", Constants.Refresh_token);
        editor.putString("login_method", Constants.LOGIN_METHOD);
        editor.putString("firm_id", Constants.Firm_id);
        editor.putString("Json_key", String.valueOf(Constants.jsonObject_dashboard));
        editor.putBoolean("Check_box", is_biometric);
        editor.apply();
//            Constants.is_biometric = true;
//        } else {
//            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
//            SharedPreferences.Editor editor = sharedPreferences.edit();
//            editor.clear();
//            editor.apply();
//            Constants.is_biometric = false;
//        }
    }

    private void handleFirmsResponse(JSONObject result) {
        try {
            Constants.forgot_pwd_request = false;
            if (result.has("firms")) {
                Constants.Firm_ids.clear();
                Constants.Firm_names.clear();
                ArrayList<FirmsDo> list = new ArrayList<>();

                JSONObject firms = result.getJSONObject("firms");
                JSONArray adminJsonArray = firms.getJSONArray("lauditor");

                if (adminJsonArray.length() == 0) {
                    AndroidUtils.showAlert("Account not found", this);
                } else if (adminJsonArray.length() > 1) {
                    // Multiple firms - need to show firm selection
                    for (int i = 0; i < adminJsonArray.length(); i++) {
                        JSONObject obj = adminJsonArray.getJSONObject(i);
                        FirmsDo firmsDo = new FirmsDo();

                        String firmName = obj.getString("firmName");
                        String firmId = obj.getString("id");

                        firmsDo.setName(firmName);
                        firmsDo.setValue(firmId);

                        Constants.Firm_names.add(firmName);
                        Constants.Firm_ids.add(firmId);
                        list.add(firmsDo);
                    }
                    // TODO: Show firm selection dialog
                    AndroidUtils.showAlert("Multiple firms found. Please contact support.", this);
                } else {
                    // Single firm - auto login
                    JSONObject obj = adminJsonArray.getJSONObject(0);
                    String userId = obj.getString("id");

                    JSONObject postData = new JSONObject();
                    postData.put("email", emailOrMobile.toLowerCase());
                    postData.put("userid", userId);
                    postData.put("otp", otp);

                    WebServiceHelper.callHttpWebService(
                            this,
                            this,
                            WebServiceHelper.RestMethodType.POST,
                            "login",
                            "LOGIN",
                            postData.toString()
                    );
                }
            } else {
                String error_msg = result.has("plan") && result.getString("plan").equals("lauditor")
                        ? result.getString("msg")
                        : "Account not found";
                AndroidUtils.showAlert(error_msg, this);
            }
        } catch (Exception e) {
            e.printStackTrace();
//            AndroidUtils.showAlert("Error processing firms response", this);
        }
    }

    private void handleDashboardResponse(JSONObject result) {
        try {
            // If dashboard fails due to token expiry
            if (result.optBoolean("error") && result.optString("msg").toLowerCase().contains("token")) {
                Constants.Valid_Token = false;

                // Auto re-login
                if (!Constants.Email.isEmpty() && !otp.isEmpty()) {
                    verifyOtp();
                    return;
                }

                AndroidUtils.showAlert("Session expired. Please login again.", this);
                return;
            }

            // Token valid
            Constants.Valid_Token = true;

            JSONArray dashboardArray = result.getJSONArray("cards");
            Dashboard_data(dashboardArray);
            if (Constants.loginActivity != null) {
                Constants.loginActivity.syncDevice();
            }
            saveXmppPreferences();
            startActivity(new Intent(this, MainActivity.class));
            new android.os.Handler().postDelayed(() -> {
                if (Constants.mainActivity != null) {
                    Constants.mainActivity.registerPendingFCMToken();
                }
            }, 2000); // 2s delay lets MainActivity fully initialize first
            finish();

        } catch (Exception e) {
            e.printStackTrace();
            AndroidUtils.showAlert("Error loading dashboard", this);
        }
    }

    private void handleResendOtpResponse(JSONObject result) {
        try {
            if (!result.getBoolean("error")) {
                Toast.makeText(this, "OTP resent successfully", Toast.LENGTH_SHORT).show();

                // Clear OTP boxes
                clearOtpBoxes();

                // Start timer only after successful resend
                startResendTimer();
            } else {
                String errorMsg = result.optString("msg", "Failed to resend OTP");
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
//            Toast.makeText(this, "Error processing response", Toast.LENGTH_SHORT).show();
        }
    }

    private void Dashboard_data(JSONArray jsonArray_Dashboard) throws Exception {
        MYDAYCARDS.clear();
        KPICARDS.clear();

        Set<String> skipList = new HashSet<>(Arrays.asList(
                "timesheets", "newclients", "groups", "teammembers"
        ));

        for (int i = 0; i < jsonArray_Dashboard.length(); i++) {
            JSONObject jsonObject = jsonArray_Dashboard.getJSONObject(i);
            String type = jsonObject.getString("type");
            JSONArray jsonArray_myday = jsonObject.getJSONArray("options");

            for (int j = 0; j < jsonArray_myday.length(); j++) {
                JSONObject jsonObject1 = jsonArray_myday.getJSONObject(j);
                String name = jsonObject1.getString("name");

                if (skipList.contains(name.toLowerCase())) {
                    continue;
                }

                Dashboard_Model dashboardModel = new Dashboard_Model();
                dashboardModel.setName(name);
                dashboardModel.setSequence(jsonObject1.getInt("sequence"));
                dashboardModels.add(dashboardModel);

                if (type.equals("MYDAY")) {
                    MYDAYCARDS.add(dashboardModel);
                }
                if (type.equals("KPI")) {
                    KPICARDS.add(dashboardModel);
                }
            }
        }
    }

    private void saveXmppPreferences() {
        SharedPreferences prefs = getDefaultSharedPreferences(getApplicationContext());
        String uid = Constants.UID;
        if (!Constants.ROLE.equalsIgnoreCase("admin")) {
            uid = uid + "_" + Constants.USER_ID;
        }

        prefs.edit()
                .putString("xmpp_jid", uid)
                .putString("xmpp_password", Constants.TOKEN)
                .putBoolean("xmpp_logged_in", true)
                .apply();

        mConnection = new ChatConnection(this);
        chatConnectionService = new ChatConnectionService();
        new JsonTask().execute(Constants.base_URL + "user/create/");
    }

    /* -------------------- Resend OTP -------------------- */

    private void resendOtp() {
        if (!canResend) {
            return;
        }

        // Call the same API that sent OTP initially
        Constants.check_url();
        Constants.PROBIZ_TYPE = "PROFESSIONAL";
        Constants.base_URL = Constants.PROF_URL;
        try {
            JSONObject postData = new JSONObject();
            postData.put("plan", "lauditor");

            if (emailOrMobile != null && emailOrMobile.contains("@")) {
                postData.put("email", emailOrMobile.toLowerCase());
            } else {
                postData.put("mobile", emailOrMobile);
            }

            progress_dialog = AndroidUtils.get_progress(this);

            WebServiceHelper.callHttpWebService(
                    this,
                    this,
                    WebServiceHelper.RestMethodType.POST,
                    "login",
                    "RESEND_OTP",
                    postData.toString()
            );

        } catch (Exception e) {
            dismissDialog();
            Toast.makeText(this, "Failed to resend OTP", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Resend OTP error", e);
        }
    }

    private void clearOtpBoxes() {
        isProgrammaticChange = true;
        for (EditText box : otpBoxes) {
            box.setText("");
        }
        isProgrammaticChange = false;
        otpBox1.requestFocus();
    }

    /* -------------------- Helpers -------------------- */

    private void dismissDialog() {
        if (progress_dialog != null && progress_dialog.isShowing()) {
            AndroidUtils.dismiss_dialog(progress_dialog);
        }
    }

    @Override
    protected void onDestroy() {
        if (resendTimer != null) {
            resendTimer.cancel();
        }
        dismissDialog();
        super.onDestroy();
    }

    /* -------------------- OTP TextWatcher -------------------- */

    private class OtpTextWatcher implements TextWatcher {
        private int currentIndex;
        private String previousText = "";

        OtpTextWatcher(int currentIndex) {
            this.currentIndex = currentIndex;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            previousText = s.toString();
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (isProgrammaticChange) {
                return;
            }

            String text = s.toString();

            // Handle single digit entry
            if (text.length() == 1 && previousText.isEmpty()) {
                // New digit entered, move to next box
                if (currentIndex < otpBoxes.length - 1) {
                    otpBoxes[currentIndex + 1].requestFocus();
                }
            }
            // Handle editing (replacing existing digit)
            else if (text.length() == 1 && !previousText.isEmpty()) {
                // Digit replaced, move to next box
                if (currentIndex < otpBoxes.length - 1) {
                    otpBoxes[currentIndex + 1].requestFocus();
                }
            }
            // Handle deletion - no action needed, handled by OnKeyListener
            else if (text.length() == 0 && !previousText.isEmpty()) {
                // Deletion handled by key listener
            }
        }
    }

    /* -------------------- Chat Connection -------------------- */

    private class JsonTask extends AsyncTask<String, String, String> {
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... params) {
            try {
                String uid = Constants.UID;
                if (!Constants.ROLE.equalsIgnoreCase("admin")) {
                    uid = uid + "_" + Constants.USER_ID;
                }
                mConnection.loginUser(OtpVerificationActivity.this, uid, Constants.TOKEN);
            } catch (Exception e) {
                Log.d("Chat Error", "Something went wrong while connecting");
                e.printStackTrace();
                if (chatConnectionService != null) {
                    chatConnectionService.stopSelf();
                }
            }
            return "";
        }
    }
}