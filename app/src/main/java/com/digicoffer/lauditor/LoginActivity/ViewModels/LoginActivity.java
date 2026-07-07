package com.digicoffer.lauditor.LoginActivity.ViewModels;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.ISPRODUCTION;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.IS_STAGING;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.KPICARDS;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.MYDAYCARDS;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.adminBaseURL;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.is_biometric;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.show_register;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils.isTablet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.FragmentManager;

import com.digicoffer.lauditor.CommonFiles.ValidationUtils.Validation;
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
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils;
import com.digicoffer.lauditor.CommonFiles.TermsAndCondition.TermsAndCondition;
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity implements AsyncTaskCompleteListener {
    private static final int UPDATE_REQUEST_CODE = 101;
    private AppUpdateManager appUpdateManager;
    String fullText = "By signing-in, you agree to our T&Cs and Privacy Policy";
    private boolean comingFromPasswordReset = false;

    // UI Elements
    TextInputEditText et_login_email, et_register_phone, et_register_email, et_full_name;
    TextInputEditText et_login_password;
    TextView tv_sign_in, tv_biometric_info, tv_toggle_text, tv_contact_support, tv_toggle_link;
    TextView tv_use_password, tv_use_password_otp_mode, tv_forgot_password;
    ImageView iv_toggle_password;
    LinearLayout login_section, register_section, login_biometric_section, ll_termsCondition;
    LinearLayout password_section;
    AppCompatButton btn_send_otp;
    CheckBox checkBox;
    CheckBox cbTermsCondition;
    TextView tv_terms;

    // Multi-firm views
    LinearLayout firm_layout;
    ListView sp_firm;
    TextView spinner_firm_view;
    TextView tv_multi_firm_msg;
    ArrayList<FirmsDo> firmList = new ArrayList<>();
    String selectedFirmId = "";
    boolean firmIschecked = true;

    // Login/Register Mode
    private boolean isLoginMode = true;
    private boolean isPasswordMode = false;
    private boolean isPasswordVisible = false;

    // Existing variables
    String firm_password;
    JSONObject firm_postData = new JSONObject();
    String firm_list_name = "";
    boolean ischecked = true;
    String password = "";
    boolean has_firm;
    ArrayList<Dashboard_Model> dashboardModels = new ArrayList<>();
    Dialog progress_dialog;
    Dialog ad_dialog;
    TextInputEditText et_firm_password;
    boolean response_true;
    LinearLayout ll_contact_support;
    private ChatConnection mConnection;
    private static ChatConnectionService chatConnectionService;
    private Activity activity;
    private boolean isForceUpdateRequired = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // First check for in-app update
        checkForInAppUpdate();

        // Only proceed with token validation if no force update is required
        if (!isForceUpdateRequired) {
            SharedPreferences prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            String savedToken = prefs.getString("Token", "");
            String savedJson = prefs.getString("Json_key", "");
            Constants.Refresh_token = prefs.getString("refresh_token", "");
            String fcmNav = null;
            if (getIntent() != null) {
                fcmNav = getIntent().getStringExtra("fcm_navigation");
                if (fcmNav == null || fcmNav.isEmpty())
                    fcmNav = getIntent().getStringExtra("navigation");
            }

            if (!savedToken.isEmpty() && !savedJson.isEmpty() && fcmNav != null && !fcmNav.isEmpty()) {
                try {
                    JSONObject userJson = new JSONObject(savedJson);
                    Constants.TOKEN = savedToken;
                    Constants.NAME = userJson.getString("name");
                    Constants.NAME_NEW = userJson.getString("name");
                    Constants.termsVersion = userJson.getString("termsVersion");
                    Constants.requiresTermsAcceptance = userJson.optBoolean("requiresTermsAcceptance");
                    Constants.USER_ID = userJson.getString("user_id");
                    Constants.UID = userJson.getString("uid");
                    Constants.OLD_PASSWORD = prefs.getString("password", "");
                    Constants.PK = userJson.getString("pk");
                    Constants.PASSWORD_MODE = userJson.getString("password_mode");
                    Constants.IS_ADMIN = userJson.getBoolean("admin");
                    Constants.FIRM_NAME = userJson.getString("firm_name");
                    Constants.ROLE = userJson.getString("role");
                    Constants.CATEGORY = userJson.optString("category");
                    Constants.FirmEmail = userJson.optString("email");
//                    Constants.Refresh_token = userJson.optString("refresh_token");
//                    Constants.TOKEN = userJson.optString("access_token");
                    Constants.Groups = userJson.getJSONArray("groups");
                    Constants.Email = prefs.getString("email", "");
                    Constants.LOGIN_METHOD = prefs.getString("login_method", "email");
                    Constants.is_active = true;

                    String namesString = prefs.getString("firmNames", "[]");
                    String idsString = prefs.getString("firmIds", "[]");
                    JSONArray namesArray = new JSONArray(namesString);
                    JSONArray idsArray = new JSONArray(idsString);
                    Constants.Firm_names.clear();
                    Constants.Firm_ids.clear();
                    for (int i = 0; i < namesArray.length(); i++)
                        Constants.Firm_names.add(namesArray.getString(i));
                    for (int i = 0; i < idsArray.length(); i++)
                        Constants.Firm_ids.add(idsArray.getString(i));

                    Constants.isAdmin = false;
                    if ("AAM".equals(Constants.ROLE)) {
                        Constants.isAdmin = true;
                    } else if (Constants.Groups.length() == 1
                            && Constants.Groups.getString(0).equals("AAM")) {
                        Constants.isAdmin = true;
                    }

                    JSONObject sub = userJson.optJSONObject("subscription");
                    if (sub != null) {
                        Constants.is_active = sub.optBoolean("is_active", true);
                        JSONObject feat = sub.optJSONObject("features");
                        if (feat != null) {
                            Constants.FEATURES.clear();
                            Iterator<String> k = feat.keys();
                            while (k.hasNext()) {
                                String key = k.next();
                                Constants.FEATURES.put(key, feat.optBoolean(key, false));
                            }
                        }
                    }

                    Intent mainIntent = new Intent(this, MainActivity.class);
                    mainIntent.putExtra("fcm_navigation", fcmNav);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                    Log.d("Refresh_Token", Constants.Refresh_token);
                    finish();
                    return;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        setContentView(R.layout.login);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.Blue_text_color));
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightStatusBars(true);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        Constants.show_register = false;
        Constants.loginActivity = this;

        initializeViews();
        setupListeners();

        ISPRODUCTION = false;
        IS_STAGING = false;
//        syncDevice();
        DynamicUtils.loadRefreshDynamicSizes(getApplicationContext());

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                finishAffinity();
            }
        });
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishAffinity();
            }
        });
    }

    private void initializeViews() {
        adjustCardForTablet();
        Log.d("TABLET_CHECK", "isTablet=" + isTablet(this)
                + " dpWidth=" + (getResources().getDisplayMetrics().widthPixels
                / getResources().getDisplayMetrics().density));

        ll_contact_support = findViewById(R.id.ll_contact_support);
        tv_contact_support = findViewById(R.id.tv_contact_support);

        tv_sign_in = findViewById(R.id.tv_sign_in);
        tv_sign_in.setText(R.string.login_with_otp);

        login_section = findViewById(R.id.login_section);
        register_section = findViewById(R.id.register_section);
        login_biometric_section = findViewById(R.id.login_biometric_section);
        ll_termsCondition = findViewById(R.id.ll_termsCondition);

        et_login_email = findViewById(R.id.et_login_email);

        password_section = findViewById(R.id.password_section);
        et_login_password = findViewById(R.id.et_login_password);
        tv_forgot_password = findViewById(R.id.tv_forgot_password);
        tv_forgot_password.setPaintFlags(tv_forgot_password.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tv_use_password = findViewById(R.id.tv_use_password);
        tv_use_password.setPaintFlags(tv_use_password.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tv_use_password_otp_mode = findViewById(R.id.tv_use_password_otp_mode);
        tv_use_password_otp_mode.setPaintFlags(tv_use_password_otp_mode.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        iv_toggle_password = findViewById(R.id.iv_toggle_password);
        AndroidUtils.password(et_login_password, iv_toggle_password);
        password_section.setVisibility(GONE);

        et_full_name = findViewById(R.id.et_full_name);
        et_register_email = findViewById(R.id.et_register_email);
        et_register_phone = findViewById(R.id.et_register_phone);
        tv_toggle_text = findViewById(R.id.tv_toggle_text);
        tv_toggle_link = findViewById(R.id.tv_toggle_link);
        tv_toggle_link.setPaintFlags(tv_toggle_link.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        btn_send_otp = findViewById(R.id.btn_send_otp);
        btn_send_otp.setText(R.string.send_otp);

        tv_biometric_info = findViewById(R.id.tv_biometric_info);
        tv_biometric_info.setTextColor(getColor(R.color.Red));

        cbTermsCondition = findViewById(R.id.cbTermsCondition);
        cbTermsCondition.setVisibility(GONE);
//        AndroidUtils.ToggleButton(1, btn_send_otp);

        // Multi-firm views initialization
        tv_multi_firm_msg = findViewById(R.id.tv_multi_firm_msg);
        firm_layout = findViewById(R.id.firm_layout);
        sp_firm = findViewById(R.id.sp_firm);
        spinner_firm_view = findViewById(R.id.spinner_firm_view);

        // Initially hidden
        if (tv_multi_firm_msg != null) {
            tv_multi_firm_msg.setVisibility(GONE);
        }
        if (firm_layout != null) {
            firm_layout.setVisibility(GONE);
        }
        if (sp_firm != null) {
            sp_firm.setVisibility(GONE);
        }
        if (spinner_firm_view != null) {
            spinner_firm_view.setText("");
            spinner_firm_view.setBackground(getDrawable(R.drawable.background_transparent));
        }

        cbTermsCondition.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton compoundButton, boolean b) {
//                if (b) {
//                    AndroidUtils.ToggleButton(1, btn_send_otp);
//                } else {
//                    AndroidUtils.ToggleButton(0, btn_send_otp);
//                }
            }
        });

        checkBox = findViewById(R.id.checkBox);
        tv_terms = findViewById(R.id.tv_terms_full);

        login_section.setVisibility(VISIBLE);
        register_section.setVisibility(GONE);

        if (login_biometric_section != null) {
            login_biometric_section.setVisibility(GONE);
        }
    }

    private void setupListeners() {
        setupTermsText();

        tv_contact_support.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEmailComposer();
            }
        });

        // Firm dropdown toggle
        if (spinner_firm_view != null) {
            spinner_firm_view.setOnClickListener(v ->
                    AndroidUtils.display_listview(firmIschecked, sp_firm));
        }

        // Firm item selected
        if (sp_firm != null) {
            sp_firm.setOnItemClickListener((parent, view, position, id) -> {
                if (firmList != null && position < firmList.size()) {
                    selectedFirmId = firmList.get(position).getValue();
                    if (spinner_firm_view != null) {
                        spinner_firm_view.setText(firmList.get(position).getName());
                    }
                    if (sp_firm != null) {
                        sp_firm.setVisibility(GONE);
                    }
                    firmIschecked = true;

                    // Auto re-submit login with selected firm
                    resubmitPasswordLoginWithFirm(selectedFirmId);
                }
            });
        }

        // "Use Password Instead" — shown in OTP mode
        tv_use_password_otp_mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPasswordMode = true;
                password_section.setVisibility(VISIBLE);
                tv_use_password_otp_mode.setVisibility(GONE);
                tv_sign_in.setText("Log in with Password");
                btn_send_otp.setText("Login");
            }
        });

        // "Use OTP Instead"
        tv_use_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPasswordMode = false;
                password_section.setVisibility(GONE);
                tv_use_password_otp_mode.setVisibility(VISIBLE);
                et_login_password.setText("");
                isPasswordVisible = false;
                et_login_password.setInputType(
                        android.text.InputType.TYPE_CLASS_TEXT |
                                android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                et_login_password.setSelection(et_login_password.getText() != null
                        ? et_login_password.getText().length() : 0);
                iv_toggle_password.setImageResource(R.drawable.eye_open);
                AndroidUtils.password(et_login_password, iv_toggle_password);
                tv_sign_in.setText(getString(R.string.login_with_otp));
                btn_send_otp.setText(R.string.send_otp);
            }
        });

        // Forgot Password link
        tv_forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ForgetPassword.class));
            }
        });

        // Toggle between Login and Register
        tv_toggle_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleMode();
            }
        });

        // Send OTP / Login button
        btn_send_otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLoginMode) {
                    handleLoginOTP();
                } else {
                    handleRegisterOTP();
                }
            }
        });

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Constants.is_biometric = isChecked;

                SharedPreferences prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                prefs.edit().putBoolean("Check_box", isChecked).apply();

                if (!isChecked) {
                    clearBiometricData();
                }

                Log.d("Biometric", "Checkbox changed to: " + isChecked
                        + ", Constants.is_biometric: " + Constants.is_biometric);
            }
        });

        TextWatcher loginTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                validateLoginFields();
                // Hide firm UI when email changes
                if (firm_layout != null && firm_layout.getVisibility() == VISIBLE) {
                    hideFirmSelection();
                }
            }
        };

        TextWatcher registerTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                validateRegisterFields();
            }
        };

        et_login_email.addTextChangedListener(loginTextWatcher);
        et_full_name.addTextChangedListener(registerTextWatcher);
        et_register_email.addTextChangedListener(registerTextWatcher);
        AndroidUtils.NumberFilter(et_register_phone, true);
        et_register_phone.addTextChangedListener(new Validation(et_register_phone));
    }

    private void hideFirmSelection() {
        if (tv_multi_firm_msg != null) {
            tv_multi_firm_msg.setVisibility(GONE);
        }
        if (firm_layout != null) {
            firm_layout.setVisibility(GONE);
        }
        if (sp_firm != null) {
            sp_firm.setVisibility(GONE);
        }
        if (spinner_firm_view != null) {
            spinner_firm_view.setText("");
        }
        firmList.clear();
        selectedFirmId = "";
    }

    private void openUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open link", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void toggleMode() {
        et_login_email.setText("");
        et_full_name.setText("");
        et_register_email.setText("");
        et_register_phone.setText("");
        et_login_password.setText("");
        hideFirmSelection();

        if (isLoginMode) {
            isLoginMode = false;
            isPasswordMode = false;
            password_section.setVisibility(GONE);
            tv_use_password_otp_mode.setVisibility(GONE);
            ll_termsCondition.setVisibility(GONE);
            tv_sign_in.setText("Register With OTP");
            login_section.setVisibility(GONE);
            register_section.setVisibility(VISIBLE);
            tv_toggle_text.setText("Already have an account? ");
            tv_toggle_link.setPaintFlags(tv_toggle_link.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            tv_toggle_link.setText("Log In");
            btn_send_otp.setEnabled(true);
            ll_contact_support.setVisibility(GONE);

            if (login_biometric_section != null) {
                login_biometric_section.setVisibility(GONE);
            }
            checkBox.setVisibility(GONE);
            tv_biometric_info.setVisibility(GONE);

            cbTermsCondition.setVisibility(VISIBLE);
            cbTermsCondition.setOnCheckedChangeListener(null);
            cbTermsCondition.setChecked(false);
//            cbTermsCondition.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(@NonNull CompoundButton compoundButton, boolean b) {
//                    if (b) {
//                        AndroidUtils.ToggleButton(1, btn_send_otp);
//                    } else {
//                        AndroidUtils.ToggleButton(0, btn_send_otp);
//                    }
//                }
//            });
//            AndroidUtils.ToggleButton(0, btn_send_otp);

            fullText = "By signing-up, you agree to our T&Cs and Privacy Policy";

        } else {
            isLoginMode = true;
            cbTermsCondition.setVisibility(GONE);
            cbTermsCondition.setOnCheckedChangeListener(null);
            cbTermsCondition.setChecked(false);
            ll_termsCondition.setVisibility(VISIBLE);
            fullText = "By signing-in, you agree to our T&Cs and Privacy Policy";
//            AndroidUtils.ToggleButton(1, btn_send_otp);

            tv_sign_in.setText("Login with OTP");
            login_section.setVisibility(VISIBLE);
            register_section.setVisibility(GONE);
            tv_toggle_text.setText("Don't have an account? ");
            tv_toggle_link.setText("Register Here");
            btn_send_otp.setText(R.string.send_otp);
            btn_send_otp.setEnabled(true);
            ll_contact_support.setVisibility(VISIBLE);
            tv_use_password_otp_mode.setVisibility(VISIBLE);

            checkBox.setVisibility(VISIBLE);
            CheckBiometric();
        }
        setupTermsText();
    }

    private void validateLoginFields() {
        btn_send_otp.setEnabled(true);
    }

    private void validateRegisterFields() {
    }

    public void handleLoginOTP() {
        if (isPasswordMode) {
            handlePasswordLogin();
            return;
        }

        String input = et_login_email.getText().toString().trim();

        if (input.isEmpty()) {
            AndroidUtils.showAlert("Please enter email or mobile number", this);
            return;
        }

        boolean looksLikeEmail = input.contains("@");
        boolean looksLikeMobile = input.matches("[0-9]+");

        if (looksLikeEmail) {
            if (!AndroidUtils.isValidEmail(input)) {
                AndroidUtils.showAlert("Please enter a valid email address", this);
                return;
            }
        } else if (looksLikeMobile) {
            if (input.length() != 10) {
                AndroidUtils.showAlert("Mobile number must be 10 digits", this);
                return;
            }
        } else {
            AndroidUtils.showAlert("Please enter a valid email or 10-digit mobile number", this);
            return;
        }

        progress_dialog = AndroidUtils.get_progress(this);
        LoginAPI(input);
    }

    private void handlePasswordLogin() {
        String input = et_login_email.getText().toString().trim();
        String pwd = et_login_password.getText().toString().trim();

        if (input.isEmpty()) {
            AndroidUtils.showAlert("Please enter email or mobile number", this);
            return;
        }
        if (pwd.isEmpty()) {
            AndroidUtils.showAlert("Please enter your password", this);
            return;
        }

        boolean looksLikeEmail = input.contains("@");
        boolean looksLikeMobile = input.matches("[0-9]+");

        if (looksLikeEmail) {
            if (!AndroidUtils.isValidEmail(input)) {
                AndroidUtils.showAlert("Please enter a valid email address", this);
                return;
            }
        } else if (looksLikeMobile) {
            if (input.length() != 10) {
                AndroidUtils.showAlert("Mobile number must be 10 digits", this);
                return;
            }
        } else {
            AndroidUtils.showAlert("Please enter a valid email or 10-digit mobile number", this);
            return;
        }

        try {
            progress_dialog = AndroidUtils.get_progress(this);
            Constants.check_url();
            Constants.PROBIZ_TYPE = "PROFESSIONAL";
            Constants.base_URL = Constants.PROF_URL;

            JSONObject postData = new JSONObject();
            postData.put("plan", "lauditor");

            boolean isEmail = looksLikeEmail;
            Constants.LOGIN_METHOD = isEmail ? "email" : "mobile";

            if (isEmail) {
                postData.put("email", input.toLowerCase());
                Constants.Email = input.toLowerCase();
            } else {
                postData.put("mobile", input);
                Constants.Email = input;
            }
            postData.put("password", pwd);

            WebServiceHelper.callHttpWebService(
                    this,
                    this,
                    WebServiceHelper.RestMethodType.POST,
                    "v2/login",
                    "LOGIN_PASSWORD",
                    postData.toString()
            );
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
            e.printStackTrace();
        }
    }

    private void handleRegisterOTP() {
        android.app.Activity context = this;
        Constants.is_biometric = false;
        String fullName = et_full_name.getText().toString().trim();
        String emailOrMobile = et_register_email.getText().toString().trim();
        String mobile = et_register_phone.getText().toString().trim();

        if (fullName.isEmpty()) {
            AndroidUtils.showAlert("Full name is required", this);
            return;
        }
        if (fullName.length() < 2) {
            AndroidUtils.showAlert("Name should be at least 2 characters", this);
            return;
        }
        if (emailOrMobile.isEmpty()) {
            AndroidUtils.showAlert("Email is required", this);
            return;
        }
        if (!AndroidUtils.isValidEmail(emailOrMobile)) {
            AndroidUtils.showAlert("Please enter a valid email address", this);
            return;
        }

        // ── Phone validations ──────────────────────────────────────────────────
        if (mobile.isEmpty()) {
            AndroidUtils.showAlert("Please enter a valid phone number.", this);
            return;
        }
        if (mobile.length() != 10) {
            AndroidUtils.showAlert("Please enter a valid phone number.", this);
            return;
        }
        if (!mobile.matches("[6789][0-9]{9}")) {
            AndroidUtils.showAlert("Please enter a valid phone number.", this);
            return;
        }
        // ──────────────────────────────────────────────────────────────────────
//
//        if (!cbTermsCondition.isChecked()) {
//            AndroidUtils.showAlert("Please accept the Terms & Conditions to continue", this);
//            return;
//        }

        AndroidUtils.showConfirmationDialog(this, "Confirmation",
                "Please confirm your email address : " + emailOrMobile
                        + " and phone number : " + mobile, "Continue", "Edit",
                new AndroidUtils.OnConfirmListener() {
                    @Override
                    public void onSave() {
                        progress_dialog = AndroidUtils.get_progress(context);
                        fetchCityFromIP(() -> RegisterAPI(fullName, emailOrMobile, mobile));
//                        RegisterAPI(fullName, emailOrMobile, mobile);
                    }

                    @Override
                    public void onCancel() {
                    }
                });
    }

    public void LoginAPI(String emailOrMobile) {
        try {
            progress_dialog = AndroidUtils.get_progress(this);
            Constants.check_url();
            Constants.PROBIZ_TYPE = "PROFESSIONAL";
            Constants.base_URL = Constants.PROF_URL;

            JSONObject postData = new JSONObject();
            postData.put("plan", "lauditor");

            boolean isEmail = emailOrMobile.contains("@");
            Constants.LOGIN_METHOD = isEmail ? "email" : "mobile";

            if (isEmail) {
                postData.put("email", emailOrMobile.toLowerCase());
                Constants.Email = emailOrMobile.toLowerCase();
            } else {
                postData.put("mobile", emailOrMobile);
                Constants.Email = emailOrMobile;
            }

            WebServiceHelper.callHttpWebService(
                    this,
                    this,
                    WebServiceHelper.RestMethodType.POST,
                    "login",
                    "LOGIN_OTP",
                    postData.toString()
            );
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
            e.printStackTrace();
        }
    }

    private String detectedCity = "";

    private void fetchCityFromIP(Runnable onComplete) {
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL("https://ipwhois.app/json/");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                JSONObject json = new JSONObject(sb.toString());
                detectedCity = json.optString("city", "");
                Log.d("IPGeo", "City: " + detectedCity);

            } catch (Exception e) {
                detectedCity = "";
                Log.e("IPGeo", "Error: " + e.getMessage());
            } finally {
                runOnUiThread(onComplete);
            }
        }).start();
    }

    private void RegisterAPI(String fullName, String emailOrMobile, String mobile) {
        try {
//            Constants.LOGIN_METHOD = isEmail ? "email" : "mobile";
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String currentDate = sdf.format(new Date());

            JSONObject postData = new JSONObject();
            postData.put("fullname", fullName);
            postData.put("country", "India");
//            postData.put("accepted_terms", true);
            postData.put("contact_person", fullName);
            postData.put("product", "lauditor");
            postData.put("sub_model", "three_month_free");
            postData.put("is_active_sub", true);
            postData.put("sub_start_date", currentDate);
            postData.put("category", "solo");
            postData.put("reg_city", detectedCity);
            postData.put("reg_platform", "android");

//            if (isEmail) {
            postData.put("email", emailOrMobile.toLowerCase());
            Constants.Email = emailOrMobile.toLowerCase();
//            } else {
            postData.put("mobile", mobile);
            Constants.Mobile = mobile;
//            }

            Constants.base_URL = adminBaseURL;

            WebServiceHelper.callHttpWebService(
                    this,
                    this,
                    WebServiceHelper.RestMethodType.POST,
                    "professional/onboard/lexis",
                    "REGISTER_OTP",
                    postData.toString()
            );
            Log.d("Register_OTP", postData.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
            e.printStackTrace();
        }
    }

    private void showFirmSelectionDialog(JSONArray lauditorFirms) {
        try {
            if (firmList == null) {
                firmList = new ArrayList<>();
            }
            firmList.clear();
            selectedFirmId = "";
            if (spinner_firm_view != null) {
                spinner_firm_view.setText("");
            }

            for (int i = 0; i < lauditorFirms.length(); i++) {
                JSONObject obj = lauditorFirms.getJSONObject(i);
                FirmsDo f = new FirmsDo();
                f.setName(obj.getString("firmName"));
                f.setValue(obj.getString("id"));
                firmList.add(f);
            }

            if (!firmList.isEmpty() && sp_firm != null && spinner_firm_view != null) {
                CommonSpinnerAdapter<FirmsDo> adapter = new CommonSpinnerAdapter<>(this, firmList);
                sp_firm.setAdapter(adapter);

                if (tv_multi_firm_msg != null) {
                    tv_multi_firm_msg.setVisibility(VISIBLE);
                }
                if (firm_layout != null) {
                    firm_layout.setVisibility(VISIBLE);
                }

                Log.d("LoginActivity", "Showing " + firmList.size() + " firms inline");
            }

        } catch (JSONException e) {
            e.printStackTrace();
            AndroidUtils.showAlert("Error loading firms. Please try again.", this);
        }
    }

    private void resubmitPasswordLoginWithFirm(String firmId) {
        try {
            String input = et_login_email.getText() != null
                    ? et_login_email.getText().toString().trim() : "";
            String pwd = et_login_password.getText() != null
                    ? et_login_password.getText().toString().trim() : "";

            if (tv_multi_firm_msg != null) {
                tv_multi_firm_msg.setVisibility(GONE);
            }
            if (firm_layout != null) {
                firm_layout.setVisibility(GONE);
            }

            if (progress_dialog != null && progress_dialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progress_dialog);
            }
            progress_dialog = AndroidUtils.get_progress(this);

            Constants.check_url();
            Constants.PROBIZ_TYPE = "PROFESSIONAL";
            Constants.base_URL = Constants.PROF_URL;

            JSONObject postData = new JSONObject();
            postData.put("plan", "lauditor");
            postData.put("userid", firmId);
            postData.put("password", pwd);

            boolean isEmail = input.contains("@");
            Constants.LOGIN_METHOD = isEmail ? "email" : "mobile";

            if (isEmail) {
                postData.put("email", input.toLowerCase());
                Constants.Email = input.toLowerCase();
            } else {
                postData.put("mobile", input);
                Constants.Email = input;
            }

            WebServiceHelper.callHttpWebService(
                    this,
                    this,
                    WebServiceHelper.RestMethodType.POST,
                    "v2/login",
                    "LOGIN_PASSWORD",
                    postData.toString()
            );
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
            e.printStackTrace();
        }
    }

    private void checkForInAppUpdate() {
        appUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                isForceUpdateRequired = true;
                try {
                    appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.IMMEDIATE,
                            this,
                            UPDATE_REQUEST_CODE
                    );
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                    isForceUpdateRequired = false;
                }
            } else {
                isForceUpdateRequired = false;
            }
        });

        appUpdateInfoTask.addOnFailureListener(e -> {
            isForceUpdateRequired = false;
            e.printStackTrace();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPDATE_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                Log.e("Update", "Update flow failed! Result code: " + resultCode);
                isForceUpdateRequired = false;
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // critical, updates getIntent()
        if (intent.getBooleanExtra("password_reset_success", false)) {
            comingFromPasswordReset = true;
            String prefillEmail = intent.getStringExtra("prefill_email");
            if (prefillEmail != null && !prefillEmail.isEmpty()) {
                Constants.Email = prefillEmail;
            }
        }
    }

    private boolean hasSavedUserData() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        String savedEmail = sharedPreferences.getString("email", "");
        String savedToken = sharedPreferences.getString("Token", "");
        String savedJsonKey = sharedPreferences.getString("Json_key", "");

        boolean hasEmail = savedEmail != null && !savedEmail.trim().isEmpty();
        boolean hasToken = savedToken != null && !savedToken.trim().isEmpty();
        boolean hasJsonKey = savedJsonKey != null && !savedJsonKey.trim().isEmpty();

        Log.d("BiometricCheck",
                "hasSavedUserData → email=" + hasEmail
                        + " token=" + hasToken
                        + " json=" + hasJsonKey);

        return hasEmail && hasToken && hasJsonKey;
    }

    public void check_Bio_metric() {
        if (!isLoginMode) return;

        CheckBiometric();

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences sharedPreferences_bio = getSharedPreferences("BIO", Context.MODE_PRIVATE);

        Constants.is_biometric = sharedPreferences.getBoolean("Check_box", false);
        Constants.Biometric_checked = sharedPreferences_bio.getBoolean("BIO_AUTH_DONE", false);

        checkBox.setChecked(Constants.is_biometric);

        if (!Constants.is_biometric) {
            Log.d("BiometricCheck", "Biometric not enabled by user — skipping.");
            return;
        }

        if (!hasSavedUserData()) {
            Log.d("BiometricCheck",
                    "Biometric enabled but NO saved user data found — skipping prompt.");
            checkBox.setChecked(false);
            Constants.is_biometric = false;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("Check_box", false);
            editor.apply();
            return;
        }

        Constants.Email = sharedPreferences.getString("email", "");
        String savedToken = sharedPreferences.getString("Token", "");
        String refresh_token = sharedPreferences.getString("refresh_token", "");
        String responseJson = sharedPreferences.getString("Json_key", "");
        Constants.Refresh_token = refresh_token;
        Constants.TOKEN = savedToken;
        if (Constants.Biometric_checked) {
            try {
                JSONObject userJson = new JSONObject(responseJson);

                Constants.NAME = userJson.getString("name");
                Constants.NAME_NEW = userJson.getString("name");
                Constants.LOGIN_METHOD = sharedPreferences.getString("login_method", "email");
                Constants.USER_ID = userJson.getString("user_id");
                Constants.UID = userJson.getString("uid");
                Constants.OLD_PASSWORD = sharedPreferences.getString("password", "");
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
                Constants.CATEGORY = userJson.optString("category");
                Constants.FirmEmail = userJson.optString("email");
                Constants.termsVersion = userJson.getString("termsVersion");
                Constants.requiresTermsAcceptance = userJson.optBoolean("requiresTermsAcceptance");
                Constants.isAdmin = false;

                if (Constants.ROLE.equals("AAM")) {
                    Constants.isAdmin = true;
                } else if (Constants.Groups.length() == 1
                        && Constants.Groups.getString(0).equals("AAM")) {
                    Constants.isAdmin = true;
                }

//                Constants.TOKEN = savedToken;

                if (!savedToken.isEmpty()) {
                    Dashboard();
                } else if (!Constants.Email.isEmpty()) {
                    LoginAPI(Constants.Email);
                }

            } catch (JSONException e) {
                e.printStackTrace();
                AndroidUtils.showAlert("Error parsing saved user data", this);
            }

        } else {
            confirm_login();
        }
    }

    private void setupTermsText() {
        SpannableString spannable = new SpannableString(fullText);

        int termsStart = fullText.indexOf("T&Cs");
        int termsEnd = termsStart + "T&Cs".length();
        spannable.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                openUrl("https://digicoffer.com/digicoffer_terms.html");
            }

            @Override
            public void updateDrawState(@NonNull android.text.TextPaint ds) {
                ds.setColor(getResources().getColor(R.color.Primary_new));
                ds.setFakeBoldText(false);
                ds.setUnderlineText(true);
            }
        }, termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        int privacyStart = fullText.indexOf("Privacy Policy");
        int privacyEnd = privacyStart + "Privacy Policy".length();
        spannable.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                openUrl("https://digicoffer.com/privacy.html");
            }

            @Override
            public void updateDrawState(@NonNull android.text.TextPaint ds) {
                ds.setColor(getResources().getColor(R.color.Primary_new));
                ds.setFakeBoldText(false);
                ds.setUnderlineText(true);
            }
        }, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tv_terms = findViewById(R.id.tv_terms_full);
        tv_terms.setText(spannable);
        tv_terms.setMovementMethod(LinkMovementMethod.getInstance());
        tv_terms.setHighlightColor(Color.TRANSPARENT);
    }

    private void CheckBiometric() {
        BiometricManager biometricManager = BiometricManager.from(this);
        int canAuthenticate = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG |
                        BiometricManager.Authenticators.BIOMETRIC_WEAK |
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
        );

        tv_biometric_info.setText("Please enable/add Biometric in your device.");
        tv_biometric_info.setVisibility(GONE);

        switch (canAuthenticate) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d("Fingerprint", "Biometric authentication is available.");
                tv_biometric_info.setVisibility(GONE);
                checkBox.setEnabled(true);
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Log.d("Fingerprint", "No fingerprint sensor.");
                checkBox.setEnabled(false);
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Log.d("Fingerprint", "Fingerprint sensor not working.");
                checkBox.setEnabled(false);
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Log.d("Fingerprint", "Fingerprint not enrolled.");
                checkBox.setEnabled(false);
                break;
            default:
                checkBox.setEnabled(false);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // First check for in-app update
        checkForInAppUpdate();

        // Only proceed with token validation if no force update is required
        if (!isForceUpdateRequired) {
            if (getIntent() != null && getIntent().getBooleanExtra("password_reset_success", false)) {
                comingFromPasswordReset = getIntent().getBooleanExtra("password_reset_success", false);
                getIntent().removeExtra("password_reset_success");
            }
            if (comingFromPasswordReset) {
                comingFromPasswordReset = false;
                // Only call if views are already initialized
                if (et_login_email != null) {
                    resetToPasswordMode();
                }
                SharedPreferences prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                prefs.edit().remove("pk").remove("user_id").remove("old_password").apply();
            }
            SharedPreferences prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            String savedToken = prefs.getString("Token", "");
            String savedJson = prefs.getString("Json_key", "");

            String fcmNav = null;
            if (getIntent() != null) {
                fcmNav = getIntent().getStringExtra("fcm_navigation");
                if (fcmNav == null || fcmNav.isEmpty())
                    fcmNav = getIntent().getStringExtra("navigation");
            }

            if (!savedToken.isEmpty() && !savedJson.isEmpty() && fcmNav != null && !fcmNav.isEmpty()) {
                try {
                    JSONObject userJson = new JSONObject(savedJson);
                    Constants.TOKEN = savedToken;
                    Constants.Refresh_token = prefs.getString("refresh_token", "");
                    Constants.NAME = userJson.getString("name");
                    Constants.NAME_NEW = userJson.getString("name");
                    Constants.termsVersion = userJson.getString("termsVersion");
                    Constants.requiresTermsAcceptance = userJson.optBoolean("requiresTermsAcceptance");
                    Constants.USER_ID = userJson.getString("user_id");
                    Constants.UID = userJson.getString("uid");
                    Constants.PK = userJson.getString("pk");
                    Constants.PASSWORD_MODE = userJson.getString("password_mode");
                    Constants.IS_ADMIN = userJson.getBoolean("admin");
                    Constants.FIRM_NAME = userJson.getString("firm_name");
                    Constants.ROLE = userJson.getString("role");
                    Constants.CATEGORY = userJson.optString("category");
                    Constants.FirmEmail = userJson.optString("email");
//                    Constants.Refresh_token = refresh_token;
//                    Constants.TOKEN = userJson.optString("access_token");
                    Constants.Groups = userJson.getJSONArray("groups");
                    Constants.Email = prefs.getString("email", "");
                    Constants.LOGIN_METHOD = prefs.getString("login_method", "email");

                    String namesString = prefs.getString("firmNames", "[]");
                    String idsString = prefs.getString("firmIds", "[]");
                    JSONArray namesArray = new JSONArray(namesString);
                    JSONArray idsArray = new JSONArray(idsString);
                    Constants.Firm_names.clear();
                    Constants.Firm_ids.clear();
                    for (int i = 0; i < namesArray.length(); i++)
                        Constants.Firm_names.add(namesArray.getString(i));
                    for (int i = 0; i < idsArray.length(); i++)
                        Constants.Firm_ids.add(idsArray.getString(i));

                    Constants.isAdmin = false;
                    if ("AAM".equals(Constants.ROLE)) {
                        Constants.isAdmin = true;
                    } else if (Constants.Groups.length() == 1
                            && Constants.Groups.getString(0).equals("AAM")) {
                        Constants.isAdmin = true;
                    }

                    Intent mainIntent = new Intent(this, MainActivity.class);
                    mainIntent.putExtra("fcm_navigation", fcmNav);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                    finish();
                    return;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Constants.check_url();

            SharedPreferences bioPrefs = getSharedPreferences("BIO", Context.MODE_PRIVATE);
            bioPrefs.edit().putBoolean("BIO_AUTH_DONE", false).apply();
            Constants.Biometric_checked = false;

// ── If session was forcibly expired by TokenRefreshHelper, skip auto-login ──
            SharedPreferences sessionPrefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            boolean sessionExpired = sessionPrefs.getBoolean("session_expired", false);

            if (sessionExpired) {
                Log.d("onResume", "session_expired=true — showing login form, skipping Dashboard");
                sessionPrefs.edit().putBoolean("session_expired", false).apply();

                // Preserve FCM nav — store it so after manual login it navigates correctly
                String fcmNavOnExpiry = null;
                if (getIntent() != null) {
                    fcmNavOnExpiry = getIntent().getStringExtra("fcm_navigation");
                    if (fcmNavOnExpiry == null || fcmNavOnExpiry.isEmpty())
                        fcmNavOnExpiry = getIntent().getStringExtra("navigation");
                }
                if (fcmNavOnExpiry != null && !fcmNavOnExpiry.isEmpty()) {
                    Constants.pendingFcmNavigation = fcmNavOnExpiry;
                    Log.d("FCM_NAV", "Preserved pending nav through session expiry: " + fcmNavOnExpiry);
                }
                // Don't call checkTokenAndLogin — just show the login form
            } else {
                checkTokenAndLogin();
            }

//            checkTokenAndLogin();

            if (show_register) {
                isLoginMode = true;
                toggleMode();
            }

            final View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
            rootView.post(new Runnable() {
                @Override
                public void run() {
                    List<Rect> exclusionRects = new ArrayList<>();
                    exclusionRects.add(new Rect(0, 0, rootView.getWidth(), rootView.getHeight()));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        rootView.setSystemGestureExclusionRects(exclusionRects);
                    }
                }
            });
        }
    }

    private void checkTokenAndLogin() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String savedToken = sharedPreferences.getString("Token", "");
        String refresh_token = sharedPreferences.getString("refresh_token", "");
        String responseJson = sharedPreferences.getString("Json_key", "");

        // Guard: if either token or user data missing, show login form
        if (savedToken.isEmpty() || responseJson.isEmpty()) {
            Log.d("checkTokenAndLogin", "No saved token or json — showing login form");
            // Also clear in-memory token to prevent stale usage
//            Constants.TOKEN = "";
//            Constants.Refresh_token = "";
            return;
        }

        try {
            if (!savedToken.isEmpty()) {
                JSONObject userJson = new JSONObject(responseJson);

                Constants.check_url();
                Constants.base_URL = Constants.PROF_URL;
                Constants.PROBIZ_TYPE = "PROFESSIONAL";

                Constants.TOKEN = savedToken;
                Constants.Refresh_token = refresh_token;
                Constants.NAME = userJson.getString("name");
                Constants.NAME_NEW = userJson.getString("name");
                Constants.termsVersion = userJson.getString("termsVersion");
                Constants.requiresTermsAcceptance = userJson.optBoolean("requiresTermsAcceptance");
                Constants.USER_ID = userJson.getString("user_id");
                Constants.UID = userJson.getString("uid");
                Constants.OLD_PASSWORD = sharedPreferences.getString("password", "");
                Constants.PK = userJson.getString("pk");
                Constants.PASSWORD_MODE = userJson.getString("password_mode");
                Constants.IS_ADMIN = userJson.getBoolean("admin");
                Constants.FIRM_NAME = userJson.getString("firm_name");
                Constants.ROLE = userJson.getString("role");
                Constants.Groups = userJson.getJSONArray("groups");
                Constants.CATEGORY = userJson.optString("category");
                Constants.FirmEmail = userJson.optString("email");
//                Constants.Refresh_token = userJson.optString("refresh_token");
//                Constants.TOKEN = userJson.optString("access_token");
                Constants.Email = sharedPreferences.getString("email", "");
                Constants.LOGIN_METHOD = sharedPreferences.getString("login_method", "email");
                Constants.is_active = true;

                String namesString = sharedPreferences.getString("firmNames", "[]");
                String idsString = sharedPreferences.getString("firmIds", "[]");
                JSONArray namesArray = new JSONArray(namesString);
                JSONArray idsArray = new JSONArray(idsString);
                Constants.Firm_names.clear();
                Constants.Firm_ids.clear();
                for (int i = 0; i < namesArray.length(); i++)
                    Constants.Firm_names.add(namesArray.getString(i));
                for (int i = 0; i < idsArray.length(); i++)
                    Constants.Firm_ids.add(idsArray.getString(i));

                Constants.isAdmin = false;
                if (Constants.ROLE.equals("AAM")) {
                    Constants.isAdmin = true;
                } else if (Constants.Groups.length() == 1
                        && Constants.Groups.getString(0).equals("AAM")) {
                    Constants.isAdmin = true;
                }

                JSONObject sub = userJson.optJSONObject("subscription");
                if (sub != null) {
                    Constants.is_active = sub.optBoolean("is_active", true);
                    JSONObject feat = sub.optJSONObject("features");
                    if (feat != null) {
                        Constants.FEATURES.clear();
                        Iterator<String> k = feat.keys();
                        while (k.hasNext()) {
                            String key = k.next();
                            Constants.FEATURES.put(key, feat.optBoolean(key, false));
                        }
                    }
                }

                String pendingNav = getIntent().getStringExtra("fcm_navigation");
                if (pendingNav == null || pendingNav.isEmpty())
                    pendingNav = getIntent().getStringExtra("navigation");
                if (pendingNav != null && !pendingNav.isEmpty()) {
                    Constants.pendingFcmNavigation = pendingNav;
                    Log.d("FCM_NAV", "Saved pending nav: " + pendingNav);
                }

                if (!Constants.TOKEN.isEmpty()) {
                    Dashboard();
                } else if (!Constants.Email.isEmpty()) {
                    LoginAPI(Constants.Email);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void saveBiometricCredentials() {
        String Token = Constants.TOKEN;
        String email = Constants.Email;

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", email.toLowerCase());
        editor.putString("refresh_token", Constants.Refresh_token);
        editor.putBoolean("Check_box", true);

        try {
            JSONArray firmNamesArray = new JSONArray(Constants.Firm_names);
            JSONArray firmIdsArray = new JSONArray(Constants.Firm_ids);
            editor.putString("firmNames", firmNamesArray.toString());
            editor.putString("firmIds", firmIdsArray.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        editor.apply();
        Constants.is_biometric = true;
    }

    private void clearBiometricData() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("Check_box", is_biometric);
        editor.clear();
        editor.apply();

        SharedPreferences sharedPreferences_bio = getSharedPreferences("BIO", Context.MODE_PRIVATE);
        SharedPreferences.Editor bioEditor = sharedPreferences_bio.edit();
        bioEditor.putBoolean("IS_Bio", false);
        bioEditor.putBoolean("BIO_AUTH_DONE", false);
        bioEditor.apply();

        Constants.is_biometric = false;
        Constants.Biometric_checked = false;
    }

    public void Dashboard() {
//        Constants.requiresTermsAcceptance = false;
        AndroidUtils.updateCachedUserData(this);
        progress_dialog = AndroidUtils.get_progress(this);
        MYDAYCARDS.clear();
        KPICARDS.clear();
        Constants.check_url();
        JSONObject jsonObject = new JSONObject();
        Constants.base_URL = Constants.PROF_URL;
        WebServiceHelper.callHttpWebService(LoginActivity.this, LoginActivity.this,
                WebServiceHelper.RestMethodType.GET, Constants.Dashboard, "Dashboard",
                jsonObject.toString());
    }

    public void callRefreshLogin() {
        try {
            JSONObject jsonObject = new JSONObject();
            Constants.base_URL = Constants.PROF_URL;
            jsonObject.put("refresh_token", Constants.Refresh_token);
            WebServiceHelper.callHttpWebService(
                    LoginActivity.this, LoginActivity.this,
                    WebServiceHelper.RestMethodType.POST, "refresh/token", "Refresh Token",
                    jsonObject.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DEVICE SYNC — called after every successful login (password or dashboard)
    // POST device/sync  {device_id, user_id, platform, app_version, product}
    // ─────────────────────────────────────────────────────────────────────────
    public void syncDevice() {
        try {
            // Dynamic device ID — unique per physical device
            String deviceId = Settings.Secure.getString(
                    getContentResolver(),
                    Settings.Secure.ANDROID_ID
            );
            String osVersion = "Android " + Build.VERSION.RELEASE;
            String deviceName = Build.MANUFACTURER + " " + Build.MODEL;
            Constants.base_URL = adminBaseURL;
            // Dynamic app version from PackageManager
            String appVersion = "1.0.0";
            try {
                appVersion = getPackageManager()
                        .getPackageInfo(getPackageName(), 0)
                        .versionName;
            } catch (Exception ignored) {
            }
            String timezone = java.util.TimeZone.getDefault().getID();
//            String timezone = rawTz.replace("/", " / ");
            JSONObject postData = new JSONObject();
            postData.put("device_id", deviceId);
            postData.put("user_id", Constants.USER_ID);
            postData.put("platform", "android");
            postData.put("app_version", appVersion.replace("-dev", ""));
            postData.put("timezone", timezone);
            postData.put("product", "lauditor");
            postData.put("os", osVersion);
            postData.put("device_name", deviceName);
            Log.d("DeviceSync", "Payload → " + postData);

            WebServiceHelper.callHttpWebService(
                    this,
                    this,
                    WebServiceHelper.RestMethodType.POST,
                    "device/sync",
                    "DEVICE_SYNC",
                    postData.toString()
            );
            Log.e("DeviceSync", "syncDevice : " + postData);
        } catch (Exception e) {
            Log.e("DeviceSync", "syncDevice failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void checkPasswordResetMode(JSONObject userData) {
        try {
            String passwordMode = userData.getString("password_mode");
            if ("reset".equals(passwordMode)) {
                Constants.PASSWORD_MODE = passwordMode;
                Constants.PK = userData.getString("pk");
                Constants.USER_ID = userData.getString("user_id");
                Constants.USER_ID = userData.getString("user_id");
//                Constants.TOKEN = userData.getString("token");

                SharedPreferences prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                prefs.edit()
                        .putString("pk", Constants.PK)
                        .putString("user_id", Constants.USER_ID)
                        .putString("refresh_token", Constants.Refresh_token)
                        .putString("Token", Constants.TOKEN)
                        .apply();

                Intent resetIntent = new Intent(LoginActivity.this, reset_password_file.class);
                resetIntent.putExtra("reset_mode", true);
                startActivity(resetIntent);
                finish();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void resetToPasswordMode() {
        runOnUiThread(() -> {
            isLoginMode = true;
            isPasswordMode = true;

            password_section.setVisibility(VISIBLE);
            tv_use_password_otp_mode.setVisibility(GONE);
            tv_sign_in.setText("Log in with Password");
            btn_send_otp.setText("Login");
            hideFirmSelection();
            et_login_email.setText(Constants.Email);
            login_section.setVisibility(VISIBLE);
            register_section.setVisibility(GONE);
        });
    }

    private void handleLoginResponse(JSONObject result) {
        try {
            Constants.Firm_ids.clear();
            Constants.Firm_names.clear();

            if (!result.getBoolean("error")) {
                JSONObject probiz_data = new JSONObject(result.getString("data"));
                Constants.forgot_pwd_request = false;
                Constants.jsonObject_dashboard = probiz_data;
                if (!probiz_data.getString("plan").equalsIgnoreCase("lauditor")) {
                    AndroidUtils.showAlert("Account not found", this);
                    return;
                }

                // Save credentials
                String email = Objects.requireNonNull(et_login_email.getText().toString());
                String password = Objects.requireNonNull(et_login_password.getText().toString());
                SharedPreferences prefs = getDefaultSharedPreferences(getApplicationContext());
                prefs.edit()
                        .putString("email", email.toLowerCase())
                        .putString("password", password)
                        .putBoolean("isLogin", true)
                        .putString("login_method", Constants.LOGIN_METHOD)
                        .putString("proBizType", Constants.PROBIZ_TYPE)
                        .apply();

                // Set constants
                is_biometric = true;
//                Constants.TOKEN = result.getString("token");
                Constants.NAME = probiz_data.getString("name");
                Constants.USER_ID = probiz_data.getString("user_id");
                Constants.termsVersion = probiz_data.getString("termsVersion");
                Constants.requiresTermsAcceptance = probiz_data.optBoolean("requiresTermsAcceptance");
                Constants.UID = probiz_data.getString("uid");
                Constants.OLD_PASSWORD = et_login_password.getText().toString();
                Constants.PK = probiz_data.getString("pk");
                Constants.PASSWORD_MODE = probiz_data.getString("password_mode");
                Constants.IS_ADMIN = probiz_data.getBoolean("admin");
                Constants.FIRM_NAME = probiz_data.getString("firm_name");
                Constants.ROLE = probiz_data.getString("role");
                Constants.CATEGORY = probiz_data.optString("category");
                Constants.Groups = probiz_data.getJSONArray("groups");
                Constants.FirmEmail = probiz_data.optString("email");
                Constants.Email = email.toLowerCase();
                Constants.Refresh_token = probiz_data.optString("refresh_token");
                Constants.TOKEN = probiz_data.optString("access_token");


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
                } else if (Constants.Groups.length() == 1
                        && Constants.Groups.getString(0).equals("AAM")) {
                    Constants.isAdmin = true;
                }

                Constants.Firm_ids.clear();
                Constants.Firm_names.clear();
                ArrayList<FirmsDo> list = new ArrayList<>();

                JSONArray adminJsonArray = probiz_data.getJSONArray("firms");
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
                if ("reset".equals(Constants.PASSWORD_MODE)) {
                    Constants.PK = probiz_data.getString("pk");
                    Constants.USER_ID = probiz_data.getString("user_id");
                    Constants.USER_ID = probiz_data.getString("user_id");

                    SharedPreferences prefs1 = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                    prefs1.edit()
                            .putString("pk", Constants.PK)
                            .putString("user_id", Constants.USER_ID)
                            .putString("Token", Constants.TOKEN)
                            .putString("refresh_token", Constants.Refresh_token)
                            .apply();

                    Intent resetIntent = new Intent(LoginActivity.this, reset_password_file.class);
                    resetIntent.putExtra("reset_mode", true);
                    startActivity(resetIntent);
                    finish();
                } else {
                    // Save biometric access, sync device, then load dashboard
                    Bio_metric_access();
//                    syncDevice();
                    Dashboard();
                    Constants.IS_MyDay = true;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void Bio_metric_access() {
        String Token = Constants.TOKEN;
        String email = Constants.Email;
        String password = et_login_password.getText().toString();
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", email.toLowerCase());
        editor.putString("password", password);
        editor.putString("Token", Token);                                    // access_token saved here
        editor.putString("refresh_token", Constants.Refresh_token);          // ADD THIS
        editor.putString("login_method", Constants.LOGIN_METHOD);
        editor.putString("firm_id", Constants.Firm_id);
        editor.putString("Json_key", String.valueOf(Constants.jsonObject_dashboard));
        editor.putBoolean("Check_box", is_biometric);
        editor.apply();
    }

    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {
        if (progress_dialog != null && progress_dialog.isShowing())
            AndroidUtils.dismiss_dialog(progress_dialog);

        if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Success) {
            Constants.IS_MyDay = true;
            try {
                JSONObject result = new JSONObject(httpResult.getResponseContent());

                // ── LOGIN_OTP ─────────────────────────────────────────────────────────
                if (httpResult.getRequestType().equals("LOGIN_OTP")) {
                    if (!result.optBoolean("error")) {
                        if (result.getBoolean("otp_sent")) {
                            if (!isLoginMode) {
                                SharedPreferences sharedPreferences =
                                        getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                                sharedPreferences.edit().putBoolean("Check_box", is_biometric).apply();

                                Intent intent = new Intent(LoginActivity.this, OtpVerificationActivity.class);
                                intent.putExtra("email", Constants.Email);
                                intent.putExtra("Check_box", is_biometric);
                                intent.putExtra("is_register", false);
                                startActivity(intent);
                            } else {
                                String message = result.getString("msg");
                                AndroidUtils.showAlert(message, this, "Success", () -> {

                                    SharedPreferences sharedPreferences =
                                            getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                                    sharedPreferences.edit().putBoolean("Check_box", is_biometric).apply();

                                    Intent intent = new Intent(LoginActivity.this, OtpVerificationActivity.class);
                                    intent.putExtra("email", Constants.Email);
                                    intent.putExtra("Check_box", is_biometric);
                                    intent.putExtra("is_register", false);
                                    startActivity(intent);
                                });
                            }
                        }
                    } else {
                        String errorMsg = result.optString("msg", "Failed to send OTP");
                        AndroidUtils.showAlert(errorMsg, this);
                    }
                } else if (httpResult.getRequestType().equals("Refresh Token")) {
                    if (!result.optBoolean("error")) {
                        JSONObject jsonObject = result.optJSONObject("data");
                        assert jsonObject != null;
                        Constants.Refresh_token = jsonObject.optString("refresh_token");
                        Constants.TOKEN = jsonObject.optString("access_token");
                    } else {
                        String errorMsg = result.optString("msg",
                                "Login failed. Please check your credentials.");
                        AndroidUtils.showAlert(errorMsg, this);
                    }
                } else if (httpResult.getRequestType().equals("LOGIN_PASSWORD")) {
                    if (!result.optBoolean("error")) {
                        handleLoginResponse(result);
                    } else {
                        if (result.has("firms")) {
                            JSONObject firms = result.optJSONObject("firms");
                            JSONArray lauditorFirms = (firms != null)
                                    ? firms.optJSONArray("lauditor") : null;

                            if (lauditorFirms != null && lauditorFirms.length() > 0) {
                                showFirmSelectionDialog(lauditorFirms);
                            } else {
                                String errorMsg = result.optString("msg",
                                        "Login failed. No valid firms found.");
                                AndroidUtils.showAlert(errorMsg, this);
                            }
                        } else {
                            String errorMsg = result.optString("msg",
                                    "Login failed. Please check your credentials.");
                            AndroidUtils.showAlert(errorMsg, this);
                        }
                    }
                }

                // ── Get TC ────────────────────────────────────────────────────────────
                if (httpResult.getRequestType().equalsIgnoreCase("Get TC")) {
                    if (!result.optBoolean("error")) {
                        JSONObject jsonObject = result.optJSONObject("data");
                        String url = jsonObject != null ? jsonObject.optString("url") : "";
                        String version = jsonObject != null ? jsonObject.optString("version") : "";
                        String msg = jsonObject != null ? jsonObject.optString("msg") : "";
                    } else {
                        AndroidUtils.showAlert("Alert", this,
                                String.valueOf(result.get("msg")));
                    }

                    // ── Accept TC ─────────────────────────────────────────────────────────
                } else if (httpResult.getRequestType().equals("Accept TC")) {
                    String msg = result.getString("msg");
                    Log.d("Terms Accept", msg);
                    AndroidUtils.showToast(msg, this);
                }

                // ── REGISTER_OTP ──────────────────────────────────────────────────────
                else if (httpResult.getRequestType().equals("REGISTER_OTP")) {
                    if (result.has("error") && result.getBoolean("error")) {
                        String errorMsg = result.getString("msg");
//                        if (errorMsg.contains("already")) {
                        if (result.has("redirect_to")) {
                            isLoginMode = false;
                            if (result.optString("redirect_to").equals("mobile")) {
                                AndroidUtils.showAlert("Phone number is already registered. Please log in using the OTP sent via SMS", this, "Success", () -> {
                                    et_login_email.setText(Constants.Mobile);
                                    LoginAPI(Constants.Mobile);
                                });
                            } else if (result.optString("redirect_to").equals("email")) {
                                AndroidUtils.showAlert("Email is already registered. Please log in using the OTP sent to your inbox.", this, "Success", () -> {
                                    et_login_email.setText(Constants.Email);
                                    LoginAPI(Constants.Email);
                                });
                            } else {
                                AndroidUtils.showAlert("Account already exists. Please login via OTP sent to your inbox.", this, "Success", () -> {
                                    et_login_email.setText(Constants.Email);
                                    LoginAPI(Constants.Email);
                                });
                            }
                        } else {
                            AndroidUtils.showAlert(errorMsg, this);
                        }
                        Log.d("ErrorMsg", errorMsg);
                    } else {
                        String message = result.optString("msg",
                                "Registration successful! OTP sent to your email.");
                        AndroidUtils.showAlert(message, this, "Success", () -> {
                            SharedPreferences sharedPreferences =
                                    getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                            sharedPreferences.edit().putBoolean("Check_box", is_biometric).apply();

                            Intent intent = new Intent(LoginActivity.this, OtpVerificationActivity.class);
                            intent.putExtra("email", Constants.Email);
                            intent.putExtra("Check_box", is_biometric);
                            intent.putExtra("is_register", true);
                            startActivity(intent);
                        });
                    }
                }

                // ── DEVICE_SYNC ───────────────────────────────────────────────────────
                else if (httpResult.getRequestType().equals("DEVICE_SYNC")) {
                    // Fire-and-forget — just log the outcome, never block the user
                    try {
                        JSONObject syncResult = new JSONObject(httpResult.getResponseContent());
                        Log.d("Device_Synced_Successfully", "Response → " + syncResult.optString("msg", "no msg"));
                    } catch (Exception e) {
                        Log.e("DeviceSync", "Failed to parse sync response: " + e.getMessage());
                    }
                }

                // ── Dashboard ─────────────────────────────────────────────────────────
                else if (httpResult.getRequestType().equals("Dashboard")) {
                    if (result.optBoolean("error")
                            && result.optString("msg").toLowerCase().contains("token")) {
                        Constants.Valid_Token = false;
                        AndroidUtils.showAlert("Session expired. Please login again.", this);
                        return;
                    }

                    Constants.Valid_Token = true;
                    JSONArray dashboardArray = result.getJSONArray("cards");
                    Dashboard_data(dashboardArray);
                    saveBiometricCredentials();

                    Constants.check_url();
                    Constants.base_URL = Constants.PROF_URL;
                    Constants.PROBIZ_TYPE = "PROFESSIONAL";

                    getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                            .edit()
                            .putString("Token", Constants.TOKEN)
                            .putString("refresh_token", Constants.Refresh_token)
                            .putString("base_url", Constants.base_URL)
                            .apply();

                    // ── Sync device after dashboard loads (covers OTP + biometric login) ──
                    syncDevice();

//                    if (Constants.requiresTermsAcceptance) {
//                        Log.d("TermsCheck",
//                                "requiresTermsAcceptance=true — showing T&C dialog before MainActivity.");
//                        if (progress_dialog != null && progress_dialog.isShowing()) {
//                            AndroidUtils.dismiss_dialog(progress_dialog);
//                        }
//                        checkTermsAndConditions();
//                        return;
//                    }

                    Intent mainIntent = new Intent(this, MainActivity.class);

                    String pendingNav = getIntent().getStringExtra("fcm_navigation");
                    if (pendingNav == null || pendingNav.isEmpty())
                        pendingNav = getIntent().getStringExtra("navigation");
                    if (pendingNav == null || pendingNav.isEmpty())
                        pendingNav = Constants.pendingFcmNavigation;
                    if (pendingNav != null && !pendingNav.isEmpty()) {
                        mainIntent.putExtra("fcm_navigation", pendingNav);
                        Constants.pendingFcmNavigation = "";
                        Log.d("FCM_NAV", "Forwarding nav to MainActivity: " + pendingNav);
                    }

                    startActivity(mainIntent);
                    saveXmppPreferences();
                    new android.os.Handler().postDelayed(() -> {
                        if (Constants.mainActivity != null) {
                            Constants.mainActivity.registerPendingFCMToken();
                        }
                    }, 2000); // 2s delay lets MainActivity fully initialize first
                    finish();
                }

            } catch (Exception e) {
                e.printStackTrace();
                AndroidUtils.showAlert("Error: " + e.getMessage(), this);
            }

//        } else if (httpResult.getStatus_code() == 401) {
//            if (!Constants.Email.isEmpty()) {
//                callRefreshLogin();
//            } else {
//                AndroidUtils.showAlert("Session expired. Please login again.", this);
//            }
//        } else if (httpResult.getRequestType().equals("Dashboard")
//                && httpResult.getStatus_code() == 401) {
//            if (!Constants.Email.isEmpty()) {
//                LoginAPI(Constants.Email);
//            } else {
//                AndroidUtils.showAlert("Session expired. Please login again.", this);
//            }
//        } else if (httpResult.getRequestType().equals("Dashboard")
//                && httpResult.getStatus_code() == 500) {
//            if (!Constants.Email.isEmpty()) {
//                LoginAPI(Constants.Email);
//            } else {
//                AndroidUtils.showAlert("Session expired. Please login again.", this);
//            }
        } else {
            // Skip showing error alerts for DEVICE_SYNC failures — it is fire-and-forget
            if (httpResult.getRequestType().equals("DEVICE_SYNC")) {
                Log.e("DeviceSync", "Sync failed silently — status: " + httpResult.getStatus_code());
                return;
            }
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
            try {
                JSONObject result = new JSONObject(httpResult.getResponseContent());
                AndroidUtils.showErrorAlert(result.optString("msg"), LoginActivity.this);
            } catch (Exception e) {
                AndroidUtils.showErrorAlert(
                        httpResult.getResponseContent().toString(), LoginActivity.this);
            }
        }
    }

    private void handleDashboardResponse(JSONObject result) {
        try {
            if (result.optBoolean("error")
                    && result.optString("msg").toLowerCase().contains("token")) {
                Constants.Valid_Token = false;
                AndroidUtils.showAlert("Session expired. Please login again.", this);
                return;
            }

            Constants.Valid_Token = true;
            JSONArray dashboardArray = result.getJSONArray("cards");
            Dashboard_data(dashboardArray);
            saveXmppPreferences();
            new android.os.Handler().postDelayed(() -> {
                if (Constants.mainActivity != null) {
                    Constants.mainActivity.registerPendingFCMToken();
                }
            }, 2000); // 2s delay lets MainActivity fully initialize first
            Intent mainIntent = new Intent(this, MainActivity.class);
            String pendingNav = getIntent().getStringExtra("fcm_navigation");
            if (pendingNav != null && !pendingNav.isEmpty()) {
                mainIntent.putExtra("fcm_navigation", pendingNav);
            }
            startActivity(mainIntent);
            finish();

        } catch (Exception e) {
            e.printStackTrace();
            AndroidUtils.showAlert("Error loading dashboard", this);
        }
    }

    private void confirm_login() {
        Executor executor = Executors.newSingleThreadExecutor();
        BiometricPrompt biometricPrompt = new BiometricPrompt(LoginActivity.this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(
                            @NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        Log.d("Fingerprint_success", "Biometric authentication successful");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                SharedPreferences bioPrefs =
                                        getSharedPreferences("BIO", Context.MODE_PRIVATE);
                                bioPrefs.edit()
                                        .putBoolean("IS_Bio", true)
                                        .putBoolean("BIO_AUTH_DONE", true)
                                        .apply();

                                Constants.Biometric_checked = true;
                                Constants.is_biometric = true;

                                check_Bio_metric();
                            }
                        });
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        runOnUiThread(() ->
                                AndroidUtils.showToast(
                                        "Authentication failed. Please try again.",
                                        LoginActivity.this));
                    }

                    @Override
                    public void onAuthenticationError(int errorCode,
                                                      @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        runOnUiThread(() -> {
                            if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                                    errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                                Log.d("Fingerprint", "User cancelled biometric authentication");
                            } else {
                                AndroidUtils.showToast(
                                        "Biometric error: " + errString,
                                        LoginActivity.this);
                            }
                        });
                    }
                });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Touch ID for \"LexiZ Lawyers\"")
                .setSubtitle("Authenticate through Biometrics")
                .setDeviceCredentialAllowed(true)
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void Dashboard_data(JSONArray jsonArray_Dashboard) throws JSONException {
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

    private void checkTermsAndConditions() {
        TermsAndCondition termsAndCondition = new TermsAndCondition(this);

        termsAndCondition.setOnTermsAndConditionListener(new TermsAndCondition.OnTermsAndConditionListener() {
            @Override
            public void onTermsAccepted() {
                Log.d("TermsCheck", "Terms Accepted - proceeding to dashboard");
                saveTcAcceptanceTimestamp();
                Dashboard();
            }

            @Override
            public void onTermsDeclined() {
                Log.d("TermsCheck", "Terms Declined - returning to login screen");

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

                SharedPreferences sharedPreferences =
                        getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                sharedPreferences.edit().clear().apply();

                SharedPreferences sharedPreferences_bio =
                        getSharedPreferences("BIO", Context.MODE_PRIVATE);
                sharedPreferences_bio.edit().clear().apply();

                Constants.is_biometric = false;

                runOnUiThread(() -> {
                    isLoginMode = false;
                    toggleMode();

                    et_login_email.setText("");
                    et_full_name.setText("");
                    et_register_email.setText("");
                    et_login_password.setText("");
                });
            }

            @Override
            public void onTermsCheckComplete(boolean needsToShow) {
                Log.d("TermsCheck", "Terms check complete - needsToShow: " + needsToShow);
                if (!needsToShow) {
                    Dashboard();
                }
            }
        });

        Log.d("TermsCheck", "Checking terms - Version: " + Constants.termsVersion
                + ", RequiresAcceptance: " + Constants.requiresTermsAcceptance);
        termsAndCondition.checkTermsWithData(Constants.termsVersion, Constants.requiresTermsAcceptance);
    }

    private void saveTcAcceptanceTimestamp() {
        String timestamp = new SimpleDateFormat(
                "dd MMM yyyy, hh:mm a", Locale.getDefault()).format(new Date());

        getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                .edit()
                .putString("tc_accepted_at", timestamp)
                .apply();

        Log.d("TermsCheck", "T&C acceptance saved at: " + timestamp);
    }

    private void adjustCardForTablet() {
        if (!isTablet(this)) return;

        float density = getResources().getDisplayMetrics().density;
        int screenWidth = getResources().getDisplayMetrics().widthPixels;

        LinearLayout rootLayout = findViewById(R.id.ll_login);
        if (rootLayout != null) {
            rootLayout.setGravity(android.view.Gravity.TOP | android.view.Gravity.CENTER_HORIZONTAL);
        }

        androidx.cardview.widget.CardView cardView = findViewById(R.id.cardview);
        if (cardView != null) {
            cardView.post(() -> {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        (int) (screenWidth * 0.82f),
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.gravity = android.view.Gravity.CENTER_HORIZONTAL;
                params.topMargin = (int) (30 * density);
                params.bottomMargin = (int) (20 * density);
                params.leftMargin = 0;
                params.rightMargin = 0;
                cardView.setLayoutParams(params);
            });
        }

        if (tv_sign_in != null) {
            tv_sign_in.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 38f);
            ViewGroup.MarginLayoutParams tp =
                    (ViewGroup.MarginLayoutParams) tv_sign_in.getLayoutParams();
            if (tp != null) {
                tp.topMargin = (int) (20 * density);
                tp.bottomMargin = (int) (12 * density);
                tv_sign_in.setLayoutParams(tp);
            }
        }

        if (et_login_email != null)
            et_login_email.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 26f);
        if (et_login_password != null)
            et_login_password.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 26f);
        if (et_full_name != null)
            et_full_name.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 26f);
        if (et_register_email != null)
            et_register_email.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 26f);

        scaleAllInputLayouts(26f);

        setInputCardHeight(R.id.login_section, (int) (70 * density));
        setInputCardHeight(R.id.register_section, (int) (70 * density));

        TextView biometricLabel = findViewById(R.id.textView2_login);
        if (biometricLabel != null)
            biometricLabel.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 24f);

        androidx.cardview.widget.CardView checkboxCard =
                findViewById(R.id.checkbox_cardview_login);
        if (checkboxCard != null) {
            ViewGroup.LayoutParams cp = checkboxCard.getLayoutParams();
            cp.width = (int) (44 * density);
            cp.height = (int) (44 * density);
            checkboxCard.setLayoutParams(cp);
        }

        if (btn_send_otp != null) {
            btn_send_otp.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 24f);
            LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                    (int) (260 * density),
                    (int) (65 * density)
            );
            bp.gravity = android.view.Gravity.CENTER_HORIZONTAL;
            bp.topMargin = (int) (20 * density);
            bp.bottomMargin = (int) (20 * density);
            btn_send_otp.setLayoutParams(bp);
        }

        TextView toggleText = findViewById(R.id.tv_toggle_text);
        if (toggleText != null)
            toggleText.setTextSize(DynamicUtils.twenty);
        TextView toggleLink = findViewById(R.id.tv_toggle_link);
        if (toggleLink != null)
            toggleLink.setTextSize(DynamicUtils.twenty);

        scaleStaticTextsInRegisterSection(DynamicUtils.fifteen);
    }

    private void scaleAllInputLayouts(float sp) {
        ViewGroup root = (ViewGroup) getWindow().getDecorView()
                .findViewById(android.R.id.content);
        scaleInputsRecursive(root, sp);
    }

    private void scaleInputsRecursive(ViewGroup vg, float sp) {
        if (vg == null) return;
        for (int i = 0; i < vg.getChildCount(); i++) {
            View v = vg.getChildAt(i);
            if (v instanceof com.google.android.material.textfield.TextInputLayout) {
                com.google.android.material.textfield.TextInputLayout til =
                        (com.google.android.material.textfield.TextInputLayout) v;
                if (til.getEditText() != null)
                    til.getEditText().setTextSize(
                            android.util.TypedValue.COMPLEX_UNIT_SP, sp);
            } else if (v instanceof ViewGroup) {
                scaleInputsRecursive((ViewGroup) v, sp);
            }
        }
    }

    private void setInputCardHeight(int sectionId, int heightPx) {
        ViewGroup section = findViewById(sectionId);
        if (section == null) return;
        for (int i = 0; i < section.getChildCount(); i++) {
            View child = section.getChildAt(i);
            if (child instanceof androidx.cardview.widget.CardView) {
                ViewGroup.LayoutParams lp = child.getLayoutParams();
                lp.height = heightPx;
                child.setLayoutParams(lp);
            }
        }
    }

    private void scaleStaticTextsInRegisterSection(float sp) {
        LinearLayout registerSection = findViewById(R.id.register_section);
        if (registerSection == null) return;
        scaleStaticTextViewsRecursive(registerSection, sp);
    }

    private void scaleStaticTextViewsRecursive(ViewGroup vg, float sp) {
        for (int i = 0; i < vg.getChildCount(); i++) {
            View v = vg.getChildAt(i);
            if (v instanceof TextView
                    && !(v instanceof com.google.android.material.textfield.TextInputEditText)) {
                TextView tv = (TextView) v;
                CharSequence text = tv.getText();
                if (text != null && (text.toString().contains("By signing")
                        || text.toString().contains(" & "))) {
                    tv.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, sp);
                }
            } else if (v instanceof ViewGroup) {
                scaleStaticTextViewsRecursive((ViewGroup) v, sp);
            }
        }
    }

    private void openEmailComposer() {
        String supportEmail = "support@lexiz.ai";
        String subject = "Support Request";

        Uri mailUri = Uri.parse("mailto:" + supportEmail
                + "?subject=" + Uri.encode(subject));

        Intent intent = new Intent(Intent.ACTION_SENDTO, mailUri);

        try {
            startActivity(Intent.createChooser(intent, "Send email"));
        } catch (android.content.ActivityNotFoundException e) {
            new AlertDialog.Builder(this)
                    .setTitle("No Email App Found")
                    .setMessage("Please email us at: " + supportEmail)
                    .setPositiveButton("Copy Email", (dialog, which) -> {
                        ClipboardManager clipboard =
                                (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("email", supportEmail);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(this, "Email copied to clipboard",
                                Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("OK", null)
                    .show();
        }
    }

    @Override
    public void onClick(View view) {
    }

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
                mConnection.loginUser(LoginActivity.this, uid, Constants.TOKEN);
            } catch (Exception e) {
                Log.d("Chat Error", "Something went wrong while connecting");
                e.fillInStackTrace();
                if (chatConnectionService != null) {
                    chatConnectionService.stopSelf();
                }
            }
            return "";
        }
    }
}