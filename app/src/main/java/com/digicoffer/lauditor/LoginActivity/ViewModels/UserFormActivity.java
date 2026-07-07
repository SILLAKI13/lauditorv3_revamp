package com.digicoffer.lauditor.LoginActivity.ViewModels;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

public class UserFormActivity extends AppCompatActivity
        implements AsyncTaskCompleteListener, View.OnClickListener {

    private static final String TAG = "UserFormActivity";

    private TextInputEditText etFullName, etEmailOrPhone;
    private TextInputLayout tilFullName, tilEmailOrPhone;
    private AppCompatButton btnCancel, btnSave;

    private ProgressDialog progress_dialog;

    private static final String DEFAULT_COUNTRY = "India";
    private static final String DEFAULT_CATEGORY = "solo";
    private static final String DEFAULT_PLAN = "lauditor";// lexiz

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.otplogin);

        initializeViews();
        setupValidation();
        setupClickListeners();
    }

    private void initializeViews() {
        etFullName = findViewById(R.id.et_full_name);
        etEmailOrPhone = findViewById(R.id.et_email_or_phone);
        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);

        tilFullName = (TextInputLayout) etFullName.getParent().getParent();
        tilEmailOrPhone = (TextInputLayout) etEmailOrPhone.getParent().getParent();
    }

    private void setupValidation() {
        etFullName.addTextChangedListener(new SimpleTextWatcher(this::validateFullName));
        etEmailOrPhone.addTextChangedListener(new SimpleTextWatcher(this::validateEmailOrPhone));
    }

    private void setupClickListeners() {
        btnCancel.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            if (validateAllFields()) {
                onboardProfessional();
            }
        });
    }

    private boolean validateFullName() {
        String name = etFullName.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            tilFullName.setError("Full name is required");
            return false;
        } else if (name.length() < 3) {
            tilFullName.setError("Name must be at least 3 characters");
            return false;
        } else {
            tilFullName.setError(null);
            return true;
        }
    }

    private boolean validateEmailOrPhone() {
        String input = etEmailOrPhone.getText().toString().trim();

        if (TextUtils.isEmpty(input)) {
            tilEmailOrPhone.setError("Email or phone number required");
            return false;
        }

        if (input.contains("@")) {
            boolean valid = Patterns.EMAIL_ADDRESS.matcher(input).matches();
            tilEmailOrPhone.setError(valid ? null : "Invalid email");
            return valid;
        } else {
            boolean valid = input.matches("^[6-9]\\d{9}$");
            tilEmailOrPhone.setError(valid ? null : "Invalid mobile number");
            return valid;
        }
    }

    private boolean validateAllFields() {
        return validateFullName() & validateEmailOrPhone();
    }

    private void onboardProfessional() {
        try {
            Constants.check_url();
            Constants.PROBIZ_TYPE = "PROFESSIONAL";
            Constants.base_URL = "https://adminapi.dev2.digicoffer.com";

            String fullName = etFullName.getText().toString().trim();
            String input = etEmailOrPhone.getText().toString().trim();

            JSONObject postData = new JSONObject();
            postData.put("fullname", fullName);
            postData.put("contact_person", fullName);
            postData.put("country", DEFAULT_COUNTRY);
            postData.put("category", DEFAULT_CATEGORY);
            postData.put("plan", DEFAULT_PLAN);

            if (input.contains("@")) {
                postData.put("email", input.toLowerCase());

            } else {
                postData.put("mobile", input);
                postData.put("email", "");
            }

            Log.d(TAG, "ONBOARD REQUEST: " + postData);

//            progress_dialog = AndroidUtils.get_progress(this);

            WebServiceHelper.callHttpWebService(
                    this,
                    this,
                    WebServiceHelper.RestMethodType.POST,
                    "/professional/onboard/lexis",
                    "ONBOARD",
                    postData.toString()
            );

        } catch (Exception e) {
            dismissDialog();
            Toast.makeText(this, "Request error", Toast.LENGTH_SHORT).show();
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {
        dismissDialog();

        if (httpResult == null) {
            Toast.makeText(this, "No server response", Toast.LENGTH_SHORT).show();
            return;
        }

        String action = httpResult.getRequestType();
        String response = httpResult.getResponseContent();
        boolean isSuccess =
                httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Success;

        Log.d(TAG, "Action: " + action);
        Log.d(TAG, "Response: " + response);

        if ("ONBOARD".equals(action)) {
            if (!response.isEmpty())
                handleOnboardResponse(response, isSuccess);
            else ResendOtp();
        } else if ("RESEND_OTP".equals(action)) {
            if (!response.isEmpty())
                handleOnboardResponse(response, isSuccess);
        }
    }

    private void ResendOtp() {
        try {
            Constants.check_url();
            Constants.PROBIZ_TYPE = "PROFESSIONAL";
            Constants.base_URL = Constants.PROF_URL;
            JSONObject postData = new JSONObject();
//            postData.put("otp", otp);
            String emailOrMobile = etEmailOrPhone.getText().toString().trim();
            if (emailOrMobile.contains("@")) {
                postData.put("email", emailOrMobile.toLowerCase());
            } else {
                postData.put("mobile", emailOrMobile);
            }

            Log.d(TAG, "LOGIN REQUEST: " + postData);

//            progressDialog = AndroidUtils.show_progress_dialog(
//                    this, "Verifying OTP", "Please wait..."
//            );

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
            Toast.makeText(this, "Request error", Toast.LENGTH_SHORT).show();
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private void handleOnboardResponse(String response, boolean isSuccess) {
        try {
            JSONObject json = new JSONObject(response);
            if (json.has("error")) {
                if (isSuccess && !json.optBoolean("error", true)) {
                    Toast.makeText(this, json.optString("msg", "Success"), Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(this, OtpVerificationActivity.class);
                    intent.putExtra("email", etEmailOrPhone.getText().toString().trim());
                    intent.putExtra("fullname", etFullName.getText().toString().trim());
                    startActivity(intent);
                    finish();

                }else{
                    ResendOtp();
                }
            } else {
                Toast.makeText(this, json.optString("msg", "Registration failed"), Toast.LENGTH_LONG).show();

            }

        } catch (JSONException e) {
            Toast.makeText(this, "Response parse error", Toast.LENGTH_SHORT).show();
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private void dismissDialog() {
        if (progress_dialog != null && progress_dialog.isShowing()) {
            AndroidUtils.dismiss_dialog(progress_dialog);
        }
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    protected void onDestroy() {
        dismissDialog();
        super.onDestroy();
    }

    /* -------- Helper class -------- */
    private static class SimpleTextWatcher implements TextWatcher {
        private final Runnable callback;

        SimpleTextWatcher(Runnable callback) {
            this.callback = callback;
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            callback.run();
        }

        public void afterTextChanged(Editable s) {
        }
    }
}
