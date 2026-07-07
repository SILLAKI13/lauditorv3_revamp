package com.digicoffer.lauditor.LoginActivity.ViewModels;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;

import com.digicoffer.lauditor.LoginActivity.Models.FirmsDo;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils;
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class ForgetPassword extends AppCompatActivity implements AsyncTaskCompleteListener {

    private Button submitButton, cancel;
    private TextInputEditText tetEmail;
    private Dialog progressDialog;

    ListView sp_firm;
    LinearLayout firm_layout;
    ArrayList<FirmsDo> list = new ArrayList<>();

    String firm_name = "";
    String firm_list_name = "";
    boolean ischecked = true;

    // true once the server has returned firms and user must pick one before submitting
    private boolean awaitingFirmSelection = false;

    TextView spinner_firm_view, tv_msg_info, tv_forgot_pwd, tv_contentText;

    // ─────────────────────────────────────────────────────────────────
    // LIFECYCLE
    // ─────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgetpassword);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.Blue_text_color));
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightStatusBars(true);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initViews();
        setupListeners();
    }

    // ─────────────────────────────────────────────────────────────────
    // INIT
    // ─────────────────────────────────────────────────────────────────

    private void initViews() {
        tetEmail = findViewById(R.id.et_login_email);
        sp_firm = findViewById(R.id.sp_firm);
        spinner_firm_view = findViewById(R.id.spinner_firm_view);
        firm_layout = findViewById(R.id.firm_layout);
        tv_msg_info = findViewById(R.id.tv_msg_info);
        tv_forgot_pwd = findViewById(R.id.tv_forgot_pwd);
        tv_contentText = findViewById(R.id.tv_contentText);
        submitButton = findViewById(R.id.Submit);
        cancel = findViewById(R.id.Cancel);
        tv_contentText.setText("Don't worry ! It happens. Please enter the email associated with your account");
        // Initial text / visibility
        tv_forgot_pwd.setText(R.string.forgot_password);
        tv_msg_info.setText(R.string.note_text);
        tv_msg_info.setTextSize(DynamicUtils.fifteen);
//        cancel.setText("Back");

        // Firm dropdown — hidden until server returns multi-firm list
        firm_layout.setVisibility(View.GONE);
        sp_firm.setVisibility(View.GONE);

        // Adapter
        sp_firm.setBackground(getDrawable(R.drawable.rectangular_white_background));
        spinner_firm_view.setBackground(getDrawable(R.drawable.background_transparent));
        spinner_firm_view.setPadding(30, 3, 3, 0);
        spinner_firm_view.setText("");

        CommonSpinnerAdapter<FirmsDo> adapter = new CommonSpinnerAdapter<>(this, list);
        sp_firm.setAdapter(adapter);
        submitButton.setText(R.string.submit);
        // Submit disabled until email is typed
        submitButton.setEnabled(false);
        submitButton.setBackgroundTintList(
                ColorStateList.valueOf(getResources().getColor(R.color.dullBlueColor)));
    }

    private void setupListeners() {
        // Email field — enable / disable submit
        tetEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                boolean hasText = !s.toString().trim().isEmpty();
                submitButton.setEnabled(hasText);
                submitButton.setBackgroundTintList(ColorStateList.valueOf(
                        getResources().getColor(hasText ? R.color.blue : R.color.dullBlueColor)));

                // If the user edits the email after firms were shown, reset firm state
                if (awaitingFirmSelection) {
                    resetFirmSelection();
                }
            }
        });

        // Firm dropdown toggle
        spinner_firm_view.setOnClickListener(v ->
                AndroidUtils.display_listview(ischecked, sp_firm));

        // Firm item selected
        sp_firm.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                firm_name = list.get(position).getValue();
                firm_list_name = list.get(position).getName();
                Log.d("FIRM_SELECTED", "id=" + firm_name + " name=" + firm_list_name);

                spinner_firm_view.setText(firm_list_name);
                sp_firm.setVisibility(View.GONE);
                ischecked = true;

                // Now that a firm is chosen, re-submit automatically
                submitWithSelectedFirm();
            }
        });

        // Submit button
        submitButton.setOnClickListener(v -> {
            String email = Objects.requireNonNull(tetEmail.getText()).toString().trim();
            if (email.isEmpty()) {
                AndroidUtils.showAlert("Please enter your email address", ForgetPassword.this);
                return;
            }
            if (!AndroidUtils.isValidEmail(email)) {
                AndroidUtils.showAlert("Please enter a valid email address", ForgetPassword.this);
                return;
            }
            // If firms are showing but none picked yet, prompt user
            if (awaitingFirmSelection && firm_name.isEmpty()) {
                AndroidUtils.showAlert("Please select a firm to continue", ForgetPassword.this);
                return;
            }
            resetPassword();
        });

        // Back button
        cancel.setOnClickListener(v -> navigateToLoginActivity());
    }

    // ─────────────────────────────────────────────────────────────────
    // FIRM HELPERS
    // ─────────────────────────────────────────────────────────────────

    /**
     * Populate the firm dropdown from the server's "firms.lauditor" array
     * and show it so the user can pick one.
     */
    private void showFirmDropdown(JSONArray lauditorFirms) {
        list.clear();
        firm_name = "";
        firm_list_name = "";
        spinner_firm_view.setText("");

        try {
            for (int i = 0; i < lauditorFirms.length(); i++) {
                JSONObject obj = lauditorFirms.getJSONObject(i);
                FirmsDo firmsDo = new FirmsDo();
                firmsDo.setName(obj.getString("firmName"));
                firmsDo.setValue(obj.getString("id"));
                list.add(firmsDo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!list.isEmpty()) {
            // Refresh adapter
            CommonSpinnerAdapter<FirmsDo> adapter = new CommonSpinnerAdapter<>(this, list);
            sp_firm.setAdapter(adapter);

            firm_layout.setVisibility(View.VISIBLE);
            awaitingFirmSelection = true;

            // Update hint text
            tv_msg_info.setText("Multiple firms found. Please select the firm you want to reset the password for.");
            Log.d("FirmDropdown", "Showing " + list.size() + " firms");
        } else {
            firm_layout.setVisibility(View.GONE);
        }
    }

    /**
     * Called when the user picks a firm from the dropdown — re-submits the
     * reset-password request with the chosen firm's userid.
     */
    private void submitWithSelectedFirm() {
        if (firm_name.isEmpty()) return;
        awaitingFirmSelection = false;
        resetPassword();
    }

    /**
     * Resets firm-selection state (called when user edits email after firms were shown).
     */
    private void resetFirmSelection() {
        awaitingFirmSelection = false;
        firm_name = "";
        firm_list_name = "";
        list.clear();
        spinner_firm_view.setText("");
        firm_layout.setVisibility(View.GONE);
        sp_firm.setVisibility(View.GONE);
        tv_msg_info.setText(getString(R.string.note_text));
    }

    // ─────────────────────────────────────────────────────────────────
    // API CALL
    // ─────────────────────────────────────────────────────────────────

    private void resetPassword() {
        try {
            Constants.check_url();
            Constants.PROBIZ_TYPE = "PROFESSIONAL";
            Constants.base_URL = Constants.PROF_URL;

            String email = Objects.requireNonNull(tetEmail.getText()).toString().trim();

            JSONObject postData = new JSONObject();
            postData.put("email", email);
            postData.put("userid", firm_name);   // empty string on first attempt; firm id on retry
            postData.put("plan", "lauditor");
            progressDialog = AndroidUtils.get_progress(ForgetPassword.this);

            WebServiceHelper.callHttpWebService(
                    this,
                    ForgetPassword.this,
                    WebServiceHelper.RestMethodType.PUT,
                    "reset-pwd",
                    "FORGET_PASSWORD",
                    postData.toString()
            );
        } catch (Exception e) {
            if (progressDialog != null && progressDialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progressDialog);
            }
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // ASYNC CALLBACK
    // ─────────────────────────────────────────────────────────────────

    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {
        if (progressDialog != null && progressDialog.isShowing())
            AndroidUtils.dismiss_dialog(progressDialog);

        if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                JSONObject result = new JSONObject(httpResult.getResponseContent());

                if (httpResult.getRequestType().equals("FORGET_PASSWORD")) {

                    if (!result.getBoolean("error")) {
                        // ── Success ─────────────────────────────────────────────
                        String message = result.getString("msg");
                        AndroidUtils.showToast(message, ForgetPassword.this);
                        Constants.forgot_pwd_request = true;
                        Constants.Email = Objects.requireNonNull(tetEmail.getText()).toString().trim();
                        navigateToLoginActivity();

                    } else {
                        // ── Error ────────────────────────────────────────────────
                        Constants.forgot_pwd_request = false;

                        if (result.has("msg")) {
                            AndroidUtils.showToast(result.getString("msg"), ForgetPassword.this);
                        }

                        // Multi-firm response — show the dropdown
                        if (result.has("firms")) {
                            JSONObject firms = result.getJSONObject("firms");
                            Log.d("ForgetPassword", "firms=" + firms.toString());

                            JSONArray lauditorFirms = firms.optJSONArray("lauditor");
                            if (lauditorFirms != null && lauditorFirms.length() > 0) {
                                showFirmDropdown(lauditorFirms);
                            } else {
                                // No lauditor firms in the response
                                firm_layout.setVisibility(View.GONE);
                                AndroidUtils.showAlert(
                                        "No firms found for this email. Please contact support.",
                                        ForgetPassword.this);
                            }
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                AndroidUtils.showToast(e.getMessage(), ForgetPassword.this);
            }

        } else {
            AndroidUtils.showToast(httpResult.getResponseContent(), ForgetPassword.this);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // NAVIGATION
    // ─────────────────────────────────────────────────────────────────

    private void navigateToLoginActivity() {
        String email = Objects.requireNonNull(tetEmail.getText()).toString().trim();
        Constants.Email = email;

        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("password_reset_success", true);
        intent.putExtra("prefill_email", email);
        // Use CLEAR_TOP + SINGLE_TOP so existing LoginActivity instance is reused
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            navigateToLoginActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onClick(View view) {
        // unused
    }

    // ─────────────────────────────────────────────────────────────────
    // VALIDATION
    // ─────────────────────────────────────────────────────────────────

    private boolean isValidEmail(String email) {
        return email != null
                && email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[A-Za-z]{2,}");
    }
}