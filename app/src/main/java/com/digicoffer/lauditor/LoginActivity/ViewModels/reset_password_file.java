package com.digicoffer.lauditor.LoginActivity.ViewModels;

import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.check_url;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.util.Objects;

public class reset_password_file extends AppCompatActivity implements AsyncTaskCompleteListener {

    private Dialog progressDialog;
    private TextInputEditText password1, password2;
    private Button submit;
    private TextView tvErrorMsg;
    ImageView iv_toggle_password1, iv_toggle_password2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_password_file);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.Blue_text_color));
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightStatusBars(true);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Check if this is a forced password reset
        boolean isResetMode = getIntent().getBooleanExtra("reset_mode", false);

        tvErrorMsg       = findViewById(R.id.tv_error_msg);
        password1        = findViewById(R.id.et_login_password);
        iv_toggle_password1 = findViewById(R.id.iv_toggle_password);

        LinearLayout ll_password2 = findViewById(R.id.ll_password2);
        password2        = ll_password2.findViewById(R.id.et_login_password);
        iv_toggle_password2 = ll_password2.findViewById(R.id.iv_toggle_password);

        password1.setHint(R.string.password);
        password2.setHint(R.string.confirm_password);

        submit = findViewById(R.id.Submit);
        submit.setEnabled(false);
        submit.setText(R.string.reset);
        submit.setBackgroundTintList(ColorStateList.valueOf(
                getResources().getColor(R.color.dullBlueColor)));

        // Load saved data if in reset mode
        if (isResetMode) {
            SharedPreferences prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            Constants.PK      = prefs.getString("pk", "");
            Constants.USER_ID = prefs.getString("user_id", "");
            Constants.TOKEN   = prefs.getString("Token", "");
        }

        // Show all 3 conditions as soon as password1 is focused
        tvErrorMsg.setVisibility(View.VISIBLE);
        tvErrorMsg.setText(
                "• Must be 8–15 characters long\n" +
                        "• Must include uppercase and lowercase letters\n" +
                        "• Must include a number and a special character"
        );
//        password1.setOnFocusChangeListener((v, hasFocus) -> {
//            if (hasFocus) {
//                tvErrorMsg.setVisibility(View.VISIBLE);
//                tvErrorMsg.setText(
//                        "• Must be 8–15 characters long\n" +
//                                "• Must include uppercase and lowercase letters\n" +
//                                "• Must include a number and a special character"
//                );
//            }
//        });

        // password1 watcher — validates conditions one by one
        password1.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                checkPasswordValidation();
            }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkPasswordValidation();
            }
            @Override
            public void afterTextChanged(Editable s) {
                checkPasswordValidation();
                checkFields();
            }
        });

        // password2 watcher — only updates button state
        password2.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                checkFields();
            }
        });

        AndroidUtils.password(password1, iv_toggle_password1);
        AndroidUtils.password(password2, iv_toggle_password2);

        // Submit click — show alert if mismatch or invalid
        submit.setOnClickListener(view -> {
            String password_check1 = password1.getText().toString();
            String password_check2 = password2.getText().toString();

            if (!isValidPassword(password_check1)) {
                AndroidUtils.showAlert(
                        "Password does not meet the required conditions.",
                        reset_password_file.this
                );
            } else if (!password_check1.equals(password_check2)) {
                AndroidUtils.showAlert(
                        "Confirm password mismatch.",
                        reset_password_file.this
                );
            } else {
                reset_pwd();
            }
        });

        Button cancel = findViewById(R.id.Cancel);
        cancel.setOnClickListener(view -> {
            startActivity(new Intent(reset_password_file.this, LoginActivity.class));
            finish();
        });
    }

    // Conditions disappear one by one as each is met
    private void checkPasswordValidation() {
        String password = password1.getText().toString();

        if (password.isEmpty()) {
            // Show all conditions again if field is cleared
            tvErrorMsg.setVisibility(View.VISIBLE);
            tvErrorMsg.setText(
                    "• Must be 8–15 characters long\n" +
                            "• Must include uppercase and lowercase letters\n" +
                            "• Must include a number and a special character"
            );
            return;
        }

        StringBuilder errorMsg = new StringBuilder();

        if (password.length() < 8 || password.length() > 15) {
            errorMsg.append("• Must be 8–15 characters long\n");
        }
        if (!password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*")) {
            errorMsg.append("• Must include uppercase and lowercase letters\n");
        }
        if (!password.matches(".*[0-9].*") || !password.matches(".*[-!@#$%&*^+=_].*")) {
            errorMsg.append("• Must include a number and a special character\n");
        }

        if (errorMsg.length() > 0) {
            tvErrorMsg.setVisibility(View.VISIBLE);
            tvErrorMsg.setText(errorMsg.toString().trim());
        } else {
            tvErrorMsg.setVisibility(View.GONE); // All conditions met ✓
        }
    }

    // Enable submit only when both fields are non-empty
    private void checkFields() {
        String p1 = password1.getText().toString().trim();
        String p2 = password2.getText().toString().trim();

        if (!p1.isEmpty() && !p2.isEmpty()) {
            submit.setEnabled(true);
            submit.setBackgroundTintList(ColorStateList.valueOf(
                    getResources().getColor(R.color.blue)));
        } else {
            submit.setEnabled(false);
            submit.setBackgroundTintList(ColorStateList.valueOf(
                    getResources().getColor(R.color.dullBlueColor)));
        }
    }

    private void reset_pwd() {
        try {
            check_url();
            Constants.PROBIZ_TYPE = "PROFESSIONAL";
            Constants.base_URL    = Constants.PROF_URL;

            JSONObject postData = new JSONObject();
            progressDialog = AndroidUtils.get_progress(reset_password_file.this);
            postData.put("old_password", Constants.OLD_PASSWORD);
            postData.put("field", "password");
            postData.put("password", Objects.requireNonNull(password1.getText().toString()));

            String urlpath = "password/" + Constants.PK + "/user/" + Constants.USER_ID + "/update";
            WebServiceHelper.callHttpWebService(
                    reset_password_file.this,
                    reset_password_file.this,
                    WebServiceHelper.RestMethodType.PUT,
                    urlpath, "UPDATE",
                    postData.toString()
            );
            Log.e("Reset Password", "Path: " + urlpath);
        } catch (Exception e) {
            if (progressDialog != null && progressDialog.isShowing())
                AndroidUtils.dismiss_dialog(progressDialog);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onClick(View view) {}

    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {
        if (progressDialog != null && progressDialog.isShowing())
            AndroidUtils.dismiss_dialog(progressDialog);

        if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                JSONObject result = new JSONObject(httpResult.getResponseContent());
                if (httpResult.getRequestType().equals("UPDATE")) {
                    if (!result.getBoolean("error")) {
                        AndroidUtils.showToast(result.getString("msg"), reset_password_file.this);

                        SharedPreferences prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                        prefs.edit()
                                .remove("pk")
                                .remove("user_id")
                                .remove("old_password")
                                .apply();

                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.putExtra("password_reset_success", true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        finish();
                    } else {
                        AndroidUtils.showToast(
                                "Password reset failed: " + result.optString("msg"),
                                reset_password_file.this
                        );
                    }
                }
            } catch (Exception e) {
                AndroidUtils.showToast(e.getMessage(), reset_password_file.this);
            }
        } else {
            AndroidUtils.showToast(httpResult.getResponseContent(), reset_password_file.this);
        }
    }

    private boolean isValidPassword(String password) {
        if (password.length() < 8 || password.length() > 15) return false;
        if (!password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*")) return false;
        if (!password.matches(".*[0-9].*") || !password.matches(".*[-!@#$%&*^+=_].*")) return false;
        return true;
    }
}