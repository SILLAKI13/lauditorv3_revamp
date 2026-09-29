package com.digicoffer.lauditor.FirmProfile;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.regex.Pattern;

public class BankAccountDialog implements AsyncTaskCompleteListener {

    public interface OnBankAccountUpdatedListener {
        void onBankAccountSaved();
    }

    public enum Mode { ADD, VIEW, EDIT }

    private static final Pattern IFSC_PATTERN = Pattern.compile("^[A-Za-z]{4}0[A-Za-z0-9]{6}$");

    private final Activity activity;
    private final Context context;
    private final OnBankAccountUpdatedListener listener;
    private final String prefillHolderName;
    private final BankAccountModel.AccountDetails existingDetails;

    private Mode currentMode;
    private Dialog dialog;
    private Dialog progressDialog;

    private boolean isVerified = false;
    private boolean isAccountNumberVisible = false;
    private boolean isConfirmAccountNumberVisible = false;
    private String actualAccountNumber = "";

    private boolean accountNumberMaskModeActive = false;
    private boolean confirmAccountNumberMaskModeActive = false;
    private boolean editModeMaskingApplied = false;
    private boolean addModeFieldsInitialized = false;
    // Tracks which mode we last ran the "structural" UI setup for (makeEditable /
    // makeNonEditable calls, static visibility, field blanking). These calls steal
    // focus (clearFocus() + requestFocus()) which is fine ONCE when entering a mode,
    // but disastrous if re-run on every keystroke - it breaks the IME mid-type and
    // makes the field look completely uneditable. applyModeUi() runs on every
    // keystroke (via resetVerificationState()), so the structural block must only
    // fire when the mode actually changes.
    private Mode lastStructuralMode = null;

    private String snapshotHolderName = "";
    private String snapshotBankName = "";
    private String snapshotAccountNumber = "";
    private String snapshotIfsc = "";
    private String snapshotBranchName = "";

    private boolean suppressVerificationReset = false;

    private EditText etAccountHolderName, etBankName, etAccountNumber,
            etConfirmAccountNumber, etIfscCode, etBranchName;
    private TextView tvErrorAccountHolderName, tvErrorBankName, tvErrorAccountNumber,
            tvErrorConfirmAccountNumber, tvErrorIfscCode;
    private ImageView ivToggleAccountNumber, ivToggleConfirmAccountNumber, ivClose, ivEditBranchName;
    private LinearLayout llConfirmAccountNumber, llVerifiedBadge, llTerms,
            llActionButtonsEdit, llActionButtonsView, llEditBankDetails;
    private TextView tvVerifiedStatus, tvVerifiedDate, tvDialogTitle, tv_terms, tvLabelBranchName;
    private TextView tvLabelAccountHolder, tvLabelBankName, tvLabelAccountNumber,
            tvLabelConfirmAccountNumber, tvLabelIfscCode;
    private CheckBox cbTerms;
    private AppCompatButton btnVerify, btnSubmit, btnCancel, btnClose, btnEdit;

    public BankAccountDialog(Activity activity, Context context,
                             BankAccountModel.AccountDetails existingDetails,
                             String prefillHolderName,
                             OnBankAccountUpdatedListener listener) {
        this.activity = activity;
        this.context = context;
        this.existingDetails = existingDetails;
        this.prefillHolderName = prefillHolderName;
        this.listener = listener;
    }

    public void show() {
        dialog = new Dialog(activity);
        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_bank_account_details, null);
        dialog.setContentView(view);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnKeyListener((dialogInterface, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                requestClose();
                return true;
            }
            return false;
        });

        bindViews(view);
        setupListeners();

        if (existingDetails != null) {
            currentMode = Mode.VIEW;
            populateFromExistingDetails();
            applyModeUi();
        } else {
            currentMode = Mode.ADD;
            etAccountHolderName.setText(prefillHolderName != null ? prefillHolderName : "");
            applyModeUi();
        }

        dialog.show();

        if (dialog.getWindow() != null) {
            int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
            int desiredWidth = (int) (screenWidth * 0.92f);
            dialog.getWindow().setLayout(
                    desiredWidth,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setGravity(android.view.Gravity.CENTER);
        }
    }

    private void bindViews(View view) {
        tvDialogTitle = view.findViewById(R.id.tv_dialog_title);
        ivClose = view.findViewById(R.id.iv_close);

        etAccountHolderName = view.findViewById(R.id.et_account_holder_name);
        etBankName = view.findViewById(R.id.et_bank_name);
        etAccountNumber = view.findViewById(R.id.et_account_number);
        etConfirmAccountNumber = view.findViewById(R.id.et_confirm_account_number);
        etIfscCode = view.findViewById(R.id.et_ifsc_code);
        etBranchName = view.findViewById(R.id.et_branch_name);

        tvErrorAccountHolderName = view.findViewById(R.id.tv_error_account_holder_name);
        tvErrorBankName = view.findViewById(R.id.tv_error_bank_name);
        tvErrorAccountNumber = view.findViewById(R.id.tv_error_account_number);
        tvErrorConfirmAccountNumber = view.findViewById(R.id.tv_error_confirm_account_number);
        tvErrorIfscCode = view.findViewById(R.id.tv_error_ifsc_code);

        ivToggleAccountNumber = view.findViewById(R.id.iv_toggle_account_number);
        ivToggleConfirmAccountNumber = view.findViewById(R.id.iv_toggle_confirm_account_number);
        ivEditBranchName = view.findViewById(R.id.iv_edit_branch_name);
        tv_terms = view.findViewById(R.id.tv_terms);
        tvLabelBranchName = view.findViewById(R.id.tv_label_branch_name);
        llConfirmAccountNumber = view.findViewById(R.id.ll_confirm_account_number);
        llVerifiedBadge = view.findViewById(R.id.ll_verified_badge);
        llTerms = view.findViewById(R.id.ll_terms);
        llActionButtonsEdit = view.findViewById(R.id.ll_action_buttons_edit);
        llActionButtonsView = view.findViewById(R.id.ll_action_buttons_view);
        llEditBankDetails = view.findViewById(R.id.ll_edit_bank_details);

        tvVerifiedStatus = view.findViewById(R.id.tv_verified_status);
        tvVerifiedDate = view.findViewById(R.id.tv_verified_date);
        cbTerms = view.findViewById(R.id.cb_terms);

        btnVerify = view.findViewById(R.id.btn_verify);
        btnSubmit = view.findViewById(R.id.btn_submit);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnClose = view.findViewById(R.id.btn_close);
        btnEdit = view.findViewById(R.id.btn_edit);

        // Label TextViews. Their text (with/without the red mandatory asterisk) is
        // NOT set here anymore - it's mode-dependent, so it's driven entirely by
        // updateFieldLabels(), called from applyModeUi()'s isModeEntry blocks.
        tvLabelAccountHolder = view.findViewById(R.id.tv_label_account_holder);
        tvLabelBankName = view.findViewById(R.id.tv_label_bank_name);
        tvLabelAccountNumber = view.findViewById(R.id.tv_label_account_number);
        tvLabelConfirmAccountNumber = view.findViewById(R.id.tv_label_confirm_account_number);
        tvLabelIfscCode = view.findViewById(R.id.tv_label_ifsc_code);

        if (tvLabelBranchName != null) {
            tvLabelBranchName.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            tvLabelBranchName.setCompoundDrawablePadding(0);
        }
        ivEditBranchName.setVisibility(View.GONE);
        ivEditBranchName.setOnClickListener(null);
    }

    private void setRedAsteriskLabel(TextView textView, String labelText) {
        String fullText = labelText + " *";
        SpannableString spannableString = new SpannableString(fullText);
        spannableString.setSpan(new android.text.style.ForegroundColorSpan(
                        ContextCompat.getColor(context, R.color.grey_light)),
                0, fullText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new android.text.style.ForegroundColorSpan(
                        ContextCompat.getColor(context, R.color.red)),
                fullText.length() - 1, fullText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannableString);
    }

    // Mandatory-field asterisks should only be visible in ADD/EDIT mode, never in
    // VIEW mode (view mode is read-only - nothing is "required to fill in" there).
    // Called from applyModeUi()'s isModeEntry blocks so it re-runs exactly once
    // per mode switch, same pattern as the other structural UI setup in this file.
    private void updateFieldLabels(boolean showAsterisk) {
        if (showAsterisk) {
            setRedAsteriskLabel(tvLabelAccountHolder, "Account Holder Name");
            setRedAsteriskLabel(tvLabelBankName, "Bank Name");
            setRedAsteriskLabel(tvLabelAccountNumber, "Account Number");
            setRedAsteriskLabel(tvLabelConfirmAccountNumber, "Confirm Account Number");
            setRedAsteriskLabel(tvLabelIfscCode, "IFSC Code");
        } else {
            tvLabelAccountHolder.setText("Account Holder Name");
            tvLabelBankName.setText("Bank Name");
            tvLabelAccountNumber.setText("Account Number");
            tvLabelConfirmAccountNumber.setText("Confirm Account Number");
            tvLabelIfscCode.setText("IFSC Code");
        }
    }

    private void setupClickableTerms() {
        String fullText = "I have read and agree to the Terms & Conditions and Privacy Policy, and authorize LexiZ.ai to securely verify, process and store the bank account details for payment settlements.";
        SpannableString spannableString = new SpannableString(fullText);
        String termsText = "Terms & Conditions";
        String privacyText = "Privacy Policy";
        String lexiZText = "LexiZ.ai";

        int termsStart = fullText.indexOf(termsText);
        int termsEnd = termsStart + termsText.length();
        int privacyStart = fullText.indexOf(privacyText);
        int privacyEnd = privacyStart + privacyText.length();
        int lexiZStart = fullText.indexOf(lexiZText);
        int lexiZEnd = lexiZStart + lexiZText.length();

        ClickableSpan termsClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                openUrl("https://digicoffer.com/digicoffer_terms.pdf");
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
                ds.setColor(ContextCompat.getColor(context, R.color.blue));
            }
        };

        ClickableSpan privacyClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                openUrl("https://digicoffer.com/digicoffer_privacy.pdf");
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
                ds.setColor(ContextCompat.getColor(context, R.color.blue));
            }
        };

        ClickableSpan lexiZClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                openUrl("https://lexiz.ai/");
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
                ds.setColor(ContextCompat.getColor(context, R.color.blue));
            }
        };

        spannableString.setSpan(termsClickableSpan, termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(privacyClickableSpan, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(lexiZClickableSpan, lexiZStart, lexiZEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_terms.setText(spannableString);
        tv_terms.setMovementMethod(LinkMovementMethod.getInstance());
        tv_terms.setHighlightColor(Color.TRANSPARENT);
    }

    private void setupListeners() {
        ivClose.setOnClickListener(v -> requestClose());
        btnCancel.setOnClickListener(v -> requestClose());
        btnClose.setOnClickListener(v -> dismiss());

        btnEdit.setOnClickListener(v -> {
            currentMode = Mode.EDIT;
            editModeMaskingApplied = false;
            accountNumberMaskModeActive = false;
            confirmAccountNumberMaskModeActive = false;
            isAccountNumberVisible = true;
            isConfirmAccountNumberVisible = true;
            captureEditSnapshot();
            applyModeUi();
        });

        ivToggleAccountNumber.setOnClickListener(v -> toggleAccountNumberVisibility());
        ivToggleConfirmAccountNumber.setOnClickListener(v -> toggleConfirmAccountNumberVisibility());

        // FIX: this used to call resetVerificationState() (the same method the
        // keystroke watcher below calls), which blanked+re-masked the account
        // number field. Now it calls the dedicated re-edit method so the
        // "blank and re-populate from actualAccountNumber" behavior only ever
        // happens from this explicit user action, never from typing.
        llEditBankDetails.setOnClickListener(v -> resetVerificationStateForReEdit());

        cbTerms.setOnCheckedChangeListener((buttonView, isChecked) -> updateSubmitButtonState());

        btnVerify.setOnClickListener(v -> onVerifyClicked());
        btnSubmit.setOnClickListener(v -> onSubmitClicked());

        setupClickableTerms();

        TextWatcher resetVerificationWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (suppressVerificationReset) {
                    updateSubmitButtonState();
                    return;
                }
                resetVerificationState();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        etAccountNumber.addTextChangedListener(resetVerificationWatcher);
        etIfscCode.addTextChangedListener(resetVerificationWatcher);

        etAccountHolderName.addTextChangedListener(simpleWatcher());
        etConfirmAccountNumber.addTextChangedListener(simpleWatcher());
        etBankName.addTextChangedListener(simpleWatcher());
    }

    // FIX: this now runs on EVERY keystroke in etAccountNumber/etIfscCode (via the
    // TextWatcher above), so it must NOT touch etAccountNumber/etConfirmAccountNumber's
    // text or editModeMaskingApplied anymore. Previously it blanked and then
    // immediately re-populated the account number field with actualAccountNumber
    // inside the same call (via applyModeUi()'s editModeMaskingApplied-gated block),
    // which silently overwrote every character the user typed the instant they typed
    // it - making the field look completely non-editable even though it was fully
    // focusable/enabled/clickable underneath. That blank+re-populate behavior now
    // lives ONLY in resetVerificationStateForReEdit(), triggered solely by the
    // explicit "Edit" link (llEditBankDetails), which is a single deliberate user
    // action rather than something that fires on every keystroke.
    private void resetVerificationState() {
        if (isVerified) {
            isVerified = false;
            etBankName.setEnabled(true);
            suppressVerificationReset = true;
            etBranchName.setText("");
            suppressVerificationReset = false;
            llVerifiedBadge.setVisibility(View.GONE);
        }
        applyModeUi();
    }

    // Used ONLY by llEditBankDetails ("Edit" link, shown after verification) to
    // fully re-arm the account number field for fresh entry: blanks it, clears
    // editModeMaskingApplied so applyModeUi() re-populates it from
    // actualAccountNumber and refocuses it. Safe here because this fires from a
    // single explicit tap, not from every keystroke.
    private void resetVerificationStateForReEdit() {
        if (isVerified) {
            isVerified = false;
            etBankName.setEnabled(true);
            suppressVerificationReset = true;
            etBranchName.setText("");
            suppressVerificationReset = false;
            llVerifiedBadge.setVisibility(View.GONE);
        }
        if (currentMode == Mode.EDIT) {
            editModeMaskingApplied = false;
            suppressVerificationReset = true;
            etAccountNumber.setText("");
            etConfirmAccountNumber.setText("");
            suppressVerificationReset = false;
        }
        applyModeUi();
    }

    private void openUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            AndroidUtils.showAlert("Unable to open link. Please try again.", activity);
        }
    }

    private TextWatcher simpleWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSubmitButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
    }

    private void toggleAccountNumberVisibility() {
        if (currentMode != Mode.ADD && currentMode != Mode.EDIT) {
            return;
        }
        // Locked once verified - user must tap the "Edit" link first, which
        // clears isVerified and re-arms this field via applyVerifiedFieldLockState().
        if (isVerified) {
            return;
        }

        isAccountNumberVisible = !isAccountNumberVisible;

        if (currentMode == Mode.EDIT) {
            suppressVerificationReset = true;
            if (isAccountNumberVisible) {
                etAccountNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
                etAccountNumber.setText(actualAccountNumber);
                ivToggleAccountNumber.setImageResource(R.drawable.eye_open);
            } else {
                etAccountNumber.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                etAccountNumber.setText(maskAccountNumber(actualAccountNumber));
                ivToggleAccountNumber.setImageResource(R.drawable.eye_close);
            }
            // CRITICAL: Make editable after changing text
            makeEditable(etAccountNumber);
            suppressVerificationReset = false;
        } else {
            if (isAccountNumberVisible) {
                etAccountNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
                ivToggleAccountNumber.setImageResource(R.drawable.eye_open);
            } else {
                etAccountNumber.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                ivToggleAccountNumber.setImageResource(R.drawable.eye_close);
            }
            makeEditable(etAccountNumber);
        }
    }

    private void toggleConfirmAccountNumberVisibility() {
        if (currentMode != Mode.ADD && currentMode != Mode.EDIT) {
            return;
        }
        // Locked once verified - user must tap the "Edit" link first, which
        // clears isVerified and re-arms this field via applyVerifiedFieldLockState().
        if (isVerified) {
            return;
        }

        isConfirmAccountNumberVisible = !isConfirmAccountNumberVisible;

        if (currentMode == Mode.EDIT) {
            suppressVerificationReset = true;
            if (isConfirmAccountNumberVisible) {
                etConfirmAccountNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
                etConfirmAccountNumber.setText(actualAccountNumber);
                ivToggleConfirmAccountNumber.setImageResource(R.drawable.eye_open);
            } else {
                etConfirmAccountNumber.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                etConfirmAccountNumber.setText(maskAccountNumber(actualAccountNumber));
                ivToggleConfirmAccountNumber.setImageResource(R.drawable.eye_close);
            }
            makeEditable(etConfirmAccountNumber);
            suppressVerificationReset = false;
        } else {
            if (isConfirmAccountNumberVisible) {
                etConfirmAccountNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
                ivToggleConfirmAccountNumber.setImageResource(R.drawable.eye_open);
            } else {
                etConfirmAccountNumber.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                ivToggleConfirmAccountNumber.setImageResource(R.drawable.eye_close);
            }
            makeEditable(etConfirmAccountNumber);
        }
    }

    // CRITICAL METHOD: This properly resets EditText to be editable
    private void makeEditable(EditText editText) {
        if (editText == null) return;
        // Clear focus first to reset internal state
        editText.clearFocus();
        editText.setEnabled(true);
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.setClickable(true);
        editText.setBackgroundResource(R.drawable.edittext_bg);
        // FIX: reset text color back to the normal enabled color - previously this
        // was left at whatever makeNonEditable() had set it to (grey_light), which
        // made the field LOOK disabled even though it was actually focusable/editable.
        editText.setTextColor(ContextCompat.getColor(context, R.color.black));
        // Request focus after setting all properties
        editText.requestFocus();
        if (editText.getText() != null) {
            editText.setSelection(editText.getText().length());
        }
        editText.invalidate();
    }

    // Same as makeEditable(), but never touches focus. Used for general mode
    // setup (applyModeUi's structural block) where we're enabling a whole group
    // of fields at once and do NOT want to fight over / steal focus - especially
    // since this can run before the dialog window is even attached (applyModeUi()
    // is called from show() before dialog.show()), where requestFocus() is
    // unreliable and can leave the EditText showing a cursor with no real IME
    // input connection attached to it. Use makeEditable() only for deliberate,
    // single-field, user-triggered focus moves (eye-icon toggle, EDIT-mode
    // unmask-on-entry).
    private void setFieldEditableNoFocus(EditText editText) {
        if (editText == null) return;
        editText.setEnabled(true);
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.setClickable(true);
        editText.setBackgroundResource(R.drawable.edittext_bg);
        // FIX: reset text color back to the normal enabled color - previously this
        // was left at whatever makeNonEditable() had set it to (grey_light) when
        // coming from VIEW mode, which made ADD/EDIT fields LOOK disabled even
        // though they were actually focusable/editable underneath.
        editText.setTextColor(ContextCompat.getColor(context, R.color.black));
        editText.invalidate();
    }

    // CRITICAL METHOD: This properly makes EditText non-editable without breaking it
    private void makeNonEditable(EditText editText) {
        if (editText == null) return;
        // Use focusable false instead of enabled false to keep EditText working
        editText.setFocusable(false);
        editText.setFocusableInTouchMode(false);
        editText.setClickable(false);
        editText.setBackgroundResource(R.drawable.edittext_bg_disabled);
        editText.setTextColor(ContextCompat.getColor(context, R.color.grey_light));
        // Keep enabled true to maintain internal state
        editText.setEnabled(true);
    }

    private void populateFromExistingDetails() {
        if (existingDetails == null) return;

        suppressVerificationReset = true;
        etAccountHolderName.setText(existingDetails.accountHolderName);
        etBankName.setText(existingDetails.bankName);
        etIfscCode.setText(existingDetails.ifscCode);
        etBranchName.setText(existingDetails.branchName);
        actualAccountNumber = existingDetails.accountNumber;

        etAccountNumber.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        etAccountNumber.setText(maskAccountNumber(actualAccountNumber));
        etConfirmAccountNumber.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        etConfirmAccountNumber.setText(maskAccountNumber(actualAccountNumber));
        suppressVerificationReset = false;

        isVerified = existingDetails.verificationStatus;
        tvVerifiedDate.setText(!TextUtils.isEmpty(existingDetails.verifiedOn)
                ? "Verified on " + existingDetails.verifiedOn : "");
    }

    private void captureEditSnapshot() {
        snapshotHolderName = existingDetails != null && existingDetails.accountHolderName != null
                ? existingDetails.accountHolderName.trim() : "";
        snapshotBankName = existingDetails != null && existingDetails.bankName != null
                ? existingDetails.bankName.trim() : "";
        snapshotAccountNumber = actualAccountNumber != null ? actualAccountNumber.trim() : "";
        snapshotIfsc = existingDetails != null && existingDetails.ifscCode != null
                ? existingDetails.ifscCode.trim().toUpperCase() : "";
        snapshotBranchName = existingDetails != null && existingDetails.branchName != null
                ? existingDetails.branchName.trim() : "";
    }

    private boolean isEditFormDirty() {
        if (currentMode != Mode.EDIT) return false;

        String currentHolder = etAccountHolderName.getText() != null
                ? etAccountHolderName.getText().toString().trim() : "";
        String currentBank = etBankName.getText() != null
                ? etBankName.getText().toString().trim() : "";
        String currentIfsc = etIfscCode.getText() != null
                ? etIfscCode.getText().toString().trim().toUpperCase() : "";
        String currentAccountNumber = etAccountNumber.getText() != null
                ? etAccountNumber.getText().toString().trim() : "";
        String currentBranch = etBranchName.getText() != null
                ? etBranchName.getText().toString().trim() : "";

        return !currentHolder.equals(snapshotHolderName)
                || !currentBank.equals(snapshotBankName)
                || !currentIfsc.equals(snapshotIfsc)
                || !currentAccountNumber.equals(snapshotAccountNumber)
                || !currentBranch.equals(snapshotBranchName);
    }

    private void requestClose() {
        if (currentMode == Mode.EDIT && isEditFormDirty()) {
            showDiscardConfirmation();
            return;
        }
        dismiss();
    }

    private void showDiscardConfirmation() {
        new AlertDialog.Builder(activity)
                .setTitle("Discard your changes?")
                .setMessage("Your unsaved changes will be lost.")
                .setNegativeButton("Keep Editing", (d, w) -> d.dismiss())
                .setPositiveButton("Discard", (d, w) -> {
                    d.dismiss();
                    dismiss();
                })
                .setCancelable(true)
                .show();
    }

    private String maskAccountNumber(String accountNumber) {
        if (TextUtils.isEmpty(accountNumber)) return "";
        int len = accountNumber.length();
        if (len <= 4) return accountNumber;
        StringBuilder sb = new StringBuilder();
        int maskedLen = len - 4;
        for (int i = 0; i < maskedLen; i++) {
            sb.append("*");
        }
        sb.append(accountNumber.substring(maskedLen));
        return sb.toString();
    }

    private void applyModeUi() {
        // TRUE only the first time applyModeUi() runs after switching into a given
        // mode. resetVerificationState() calls applyModeUi() on every keystroke in
        // etAccountNumber/etIfscCode, so any focus-stealing setup (makeEditable /
        // makeNonEditable, which call clearFocus()+requestFocus()) must be gated
        // behind this flag - otherwise it interrupts typing on every character.
        boolean isModeEntry = (lastStructuralMode != currentMode);

        switch (currentMode) {
            case VIEW:
                tvDialogTitle.setText("Bank Account Details");

                if (isModeEntry) {
                    // Use makeNonEditable - this keeps enabled=true but removes focus
                    makeNonEditable(etAccountHolderName);
                    makeNonEditable(etBankName);
                    makeNonEditable(etAccountNumber);
                    makeNonEditable(etConfirmAccountNumber);
                    makeNonEditable(etIfscCode);
                    makeNonEditable(etBranchName);

                    // View mode is read-only, so the mandatory-field asterisks
                    // (which only make sense while filling out a form) are hidden.
                    updateFieldLabels(false);

                    llConfirmAccountNumber.setVisibility(View.GONE);
                    llTerms.setVisibility(View.GONE);
                    llActionButtonsEdit.setVisibility(View.GONE);
                    llActionButtonsView.setVisibility(View.VISIBLE);

                    ivToggleAccountNumber.setVisibility(View.GONE);
                    ivToggleConfirmAccountNumber.setVisibility(View.GONE);
                    ivEditBranchName.setVisibility(View.GONE);
                    llEditBankDetails.setVisibility(View.GONE);

                    if (!TextUtils.isEmpty(actualAccountNumber)) {
                        suppressVerificationReset = true;
                        etAccountNumber.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        etAccountNumber.setText(maskAccountNumber(actualAccountNumber));
                        etConfirmAccountNumber.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        etConfirmAccountNumber.setText(maskAccountNumber(actualAccountNumber));
                        suppressVerificationReset = false;
                    }
                }

                btnVerify.setVisibility(View.GONE);
                llVerifiedBadge.setVisibility(isVerified ? View.VISIBLE : View.GONE);

                lastStructuralMode = currentMode;
                break;

            case ADD:
                tvDialogTitle.setText("Add Bank Account Details");
                btnSubmit.setText("Submit");

                if (isModeEntry) {
                    // Make ALL fields editable (no focus stealing - user taps to focus)
                    setFieldEditableNoFocus(etAccountHolderName);
                    setFieldEditableNoFocus(etBankName);
                    setFieldEditableNoFocus(etAccountNumber);

                    setFieldEditableNoFocus(etConfirmAccountNumber);
                    setFieldEditableNoFocus(etIfscCode);
                    setFieldEditableNoFocus(etBranchName);

                    // ADD mode is a form being filled out, so the mandatory-field
                    // asterisks are shown.
                    updateFieldLabels(true);

                    ivEditBranchName.setVisibility(View.GONE);
                    setBranchNameEditable(false);

                    ivToggleAccountNumber.setVisibility(View.VISIBLE);
                    ivToggleConfirmAccountNumber.setVisibility(View.VISIBLE);

                    accountNumberMaskModeActive = false;
                    confirmAccountNumberMaskModeActive = false;
                    editModeMaskingApplied = false;

                    if (!addModeFieldsInitialized) {
                        suppressVerificationReset = true;
                        etAccountNumber.setText("");
                        etConfirmAccountNumber.setText("");
                        etAccountNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
                        etConfirmAccountNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
                        suppressVerificationReset = false;
                        addModeFieldsInitialized = true;
                    }

                    llConfirmAccountNumber.setVisibility(View.VISIBLE);
                    llTerms.setVisibility(View.VISIBLE);
                    llActionButtonsEdit.setVisibility(View.VISIBLE);
                    llActionButtonsView.setVisibility(View.GONE);
                }

                // FIX: previously these three lines were hardcoded (llVerifiedBadge always
                // GONE, btnVerify always VISIBLE, llEditBankDetails always GONE) regardless
                // of isVerified - so after a successful Verify in ADD mode, the button never
                // switched to the "Edit" link like it does in EDIT mode. Now mirrors the
                // EDIT case's isVerified-driven visibility.
                llVerifiedBadge.setVisibility(isVerified ? View.VISIBLE : View.GONE);
                btnVerify.setVisibility(isVerified ? View.GONE : View.VISIBLE);
                llEditBankDetails.setVisibility(isVerified ? View.VISIBLE : View.GONE);

                // Lock Account Number / Confirm Account Number / IFSC Code once
                // verified; unlock them again once the user taps "Edit".
                applyVerifiedFieldLockState();

                updateSubmitButtonState();

                lastStructuralMode = currentMode;
                break;

            case EDIT:
                tvDialogTitle.setText("Edit Bank Account Details");
                btnSubmit.setText("Save Changes");

                if (isModeEntry) {
                    // Make ALL fields editable (no focus stealing here - the deliberate
                    // focus move onto etAccountNumber happens below, once, when we
                    // populate it from actualAccountNumber)
                    setFieldEditableNoFocus(etAccountHolderName);
                    setFieldEditableNoFocus(etBankName);
                    setFieldEditableNoFocus(etAccountNumber);
                    setFieldEditableNoFocus(etConfirmAccountNumber);
                    setFieldEditableNoFocus(etIfscCode);
                    setFieldEditableNoFocus(etBranchName);

                    // EDIT mode is a form being filled out, so the mandatory-field
                    // asterisks are shown.
                    updateFieldLabels(true);

                    ivEditBranchName.setVisibility(View.GONE);
                    setBranchNameEditable(true);

                    ivToggleAccountNumber.setVisibility(View.VISIBLE);
                    ivToggleConfirmAccountNumber.setVisibility(View.VISIBLE);
                    ivToggleAccountNumber.setImageResource(R.drawable.eye_open);
                    ivToggleConfirmAccountNumber.setImageResource(R.drawable.eye_open);

                    llConfirmAccountNumber.setVisibility(View.VISIBLE);
                    llTerms.setVisibility(View.VISIBLE);
                    llActionButtonsEdit.setVisibility(View.VISIBLE);
                    llActionButtonsView.setVisibility(View.GONE);
                }

                // This part stays gated on editModeMaskingApplied (not isModeEntry)
                // on purpose: llEditBankDetails ("Edit" link, shown after verification)
                // now calls resetVerificationStateForReEdit(), which clears
                // editModeMaskingApplied so the account number gets re-populated
                // from actualAccountNumber here without a full mode re-entry. The
                // per-keystroke watcher no longer clears this flag, so this block
                // only runs once when entering EDIT mode (or after an explicit
                // "Edit" link tap) - never mid-typing.
                if (!TextUtils.isEmpty(actualAccountNumber) && !editModeMaskingApplied) {
                    accountNumberMaskModeActive = false;
                    confirmAccountNumberMaskModeActive = false;
                    isAccountNumberVisible = true;
                    isConfirmAccountNumberVisible = true;

                    suppressVerificationReset = true;
                    etAccountNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
                    etAccountNumber.setText(actualAccountNumber);
                    etConfirmAccountNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
                    etConfirmAccountNumber.setText(actualAccountNumber);
                    suppressVerificationReset = false;
                    editModeMaskingApplied = true;

                    // Force focus on account number field - only right after we've
                    // just (re)populated it, not on every keystroke.
                    etAccountNumber.requestFocus();
                }

                llVerifiedBadge.setVisibility(isVerified ? View.VISIBLE : View.GONE);
                btnVerify.setVisibility(isVerified ? View.GONE : View.VISIBLE);
                llEditBankDetails.setVisibility(isVerified ? View.VISIBLE : View.GONE);

                // Lock Account Number / Confirm Account Number / IFSC Code once
                // verified; unlock them again once the user taps "Edit".
                applyVerifiedFieldLockState();

                updateSubmitButtonState();

                lastStructuralMode = currentMode;
                break;
        }
    }

    private void setBranchNameEditable(boolean editable) {
        if (editable) {
            setFieldEditableNoFocus(etBranchName);
        } else {
            makeNonEditable(etBranchName);
            etBranchName.setBackgroundResource(R.drawable.edittext_bg_disabled);
            etBranchName.setTextColor(ContextCompat.getColor(context, R.color.grey_light));
        }
    }

    // Locks/unlocks Account Number, Confirm Account Number, and IFSC Code based on
    // verification state. Once isVerified is true these three fields must NOT
    // remain directly editable - the user has to tap the "Edit" link
    // (llEditBankDetails) first, which routes through
    // resetVerificationStateForReEdit() to clear isVerified and re-arm them.
    // Deliberately NOT gated behind isModeEntry (unlike the rest of the structural
    // block) because it needs to react the instant verification succeeds or is
    // reset - not just once per mode switch. setFieldEditableNoFocus()/
    // makeNonEditable() never touch focus, so calling this on every applyModeUi()
    // pass (including per-keystroke calls from resetVerificationState()) is safe
    // and won't interrupt typing. Only called from the ADD/EDIT branches - VIEW
    // mode already locks everything unconditionally via makeNonEditable().
    private void applyVerifiedFieldLockState() {
        if (isVerified) {
            makeNonEditable(etAccountNumber);
            makeNonEditable(etConfirmAccountNumber);
            makeNonEditable(etIfscCode);
            ivToggleAccountNumber.setVisibility(View.GONE);
            ivToggleConfirmAccountNumber.setVisibility(View.GONE);
        } else {
            setFieldEditableNoFocus(etAccountNumber);
            setFieldEditableNoFocus(etConfirmAccountNumber);
            setFieldEditableNoFocus(etIfscCode);
            ivToggleAccountNumber.setVisibility(View.VISIBLE);
            ivToggleConfirmAccountNumber.setVisibility(View.VISIBLE);
        }
    }

    private boolean validateAccountHolderName() {
        String v = etAccountHolderName.getText() != null ? etAccountHolderName.getText().toString().trim() : "";
        boolean valid = v.length() >= 3 && v.length() <= 100;
        tvErrorAccountHolderName.setVisibility(valid ? View.GONE : View.VISIBLE);
        return valid;
    }

    private boolean validateAccountNumber() {
        String v = etAccountNumber.getText() != null ? etAccountNumber.getText().toString().trim() : "";
        if (currentMode == Mode.EDIT && v.contains("*")) {
            v = actualAccountNumber != null ? actualAccountNumber.trim() : "";
        }
        boolean valid = v.matches("\\d{9,18}");
        tvErrorAccountNumber.setVisibility(valid ? View.GONE : View.VISIBLE);
        return valid;
    }

    private boolean validateConfirmAccountNumber() {
        String v1 = etAccountNumber.getText() != null ? etAccountNumber.getText().toString().trim() : "";
        String v2 = etConfirmAccountNumber.getText() != null ? etConfirmAccountNumber.getText().toString().trim() : "";
        if (currentMode == Mode.EDIT) {
            if (v1.contains("*")) {
                v1 = actualAccountNumber != null ? actualAccountNumber.trim() : "";
            }
            if (v2.contains("*")) {
                v2 = actualAccountNumber != null ? actualAccountNumber.trim() : "";
            }
        }
        boolean valid = !TextUtils.isEmpty(v2) && v1.equals(v2);
        tvErrorConfirmAccountNumber.setVisibility(valid ? View.GONE : View.VISIBLE);
        return valid;
    }

    private boolean validateIfscCode() {
        String v = etIfscCode.getText() != null ? etIfscCode.getText().toString().trim().toUpperCase() : "";
        boolean valid = IFSC_PATTERN.matcher(v).matches();
        tvErrorIfscCode.setVisibility(valid ? View.GONE : View.VISIBLE);
        return valid;
    }

    private boolean validateFormForVerify() {
        return validateAccountHolderName() && validateAccountNumber()
                && validateConfirmAccountNumber() && validateIfscCode();
    }

    private void updateSubmitButtonState() {
        boolean holderOk = etAccountHolderName.getText() != null
                && etAccountHolderName.getText().toString().trim().length() >= 3
                && etAccountHolderName.getText().toString().trim().length() <= 100;
        boolean bankOk = etBankName.getText() != null
                && !TextUtils.isEmpty(etBankName.getText().toString().trim());

        String accText = etAccountNumber.getText() != null ? etAccountNumber.getText().toString().trim() : "";
        if (currentMode == Mode.EDIT && accText.contains("*")) {
            accText = actualAccountNumber != null ? actualAccountNumber.trim() : "";
        }
        boolean accOk = accText.matches("\\d{9,18}");

        String confirmText = etConfirmAccountNumber.getText() != null ? etConfirmAccountNumber.getText().toString().trim() : "";
        if (currentMode == Mode.EDIT && confirmText.contains("*")) {
            confirmText = actualAccountNumber != null ? actualAccountNumber.trim() : "";
        }
        boolean confirmOk = confirmText.equals(accText);
        boolean termsOk = cbTerms.isChecked();

        boolean enable = holderOk && bankOk && accOk && confirmOk && termsOk && isVerified;
        btnSubmit.setEnabled(enable);
        btnSubmit.setAlpha(enable ? 1f : 0.5f);
    }

    private void onVerifyClicked() {
        if (!validateFormForVerify()) return;
        String ifsc = etIfscCode.getText().toString().trim().toUpperCase();
        try {
            progressDialog = AndroidUtils.get_progress(activity);
            String url = "v3/bank-account/verify-ifsc?ifsc_code=" + URLEncoder.encode(ifsc, "UTF-8");
            WebServiceHelper.callHttpWebService(
                    this,
                    context,
                    WebServiceHelper.RestMethodType.GET,
                    url,
                    "Verify_IFSC",
                    new JSONObject().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onSubmitClicked() {
        if (!isVerified) {
            AndroidUtils.showAlert("Please verify your IFSC code before submitting.", activity);
            return;
        }
        if (!validateFormForVerify()) return;
        if (!cbTerms.isChecked()) {
            AndroidUtils.showAlert("Please accept the Terms & Conditions to continue.", activity);
            return;
        }
        try {
            progressDialog = AndroidUtils.get_progress(activity);

            String accountNumberToSend;
            String accText = etAccountNumber.getText() != null ? etAccountNumber.getText().toString().trim() : "";
            if (currentMode == Mode.EDIT && accText.contains("*")) {
                accountNumberToSend = actualAccountNumber != null ? actualAccountNumber.trim() : "";
            } else {
                accountNumberToSend = accText;
            }

            JSONObject body = new JSONObject();
            body.put("account_holder_name", etAccountHolderName.getText().toString().trim());
            body.put("account_number", accountNumberToSend);
            body.put("ifsc_code", etIfscCode.getText().toString().trim().toUpperCase());
            body.put("bank_name", etBankName.getText().toString().trim());
            body.put("branch_name", etBranchName.getText().toString().trim());
            body.put("is_verified", true);

            WebServiceHelper.callHttpWebService(
                    this,
                    context,
                    WebServiceHelper.RestMethodType.POST,
                    "v3/bank-account",
                    "Save_Bank_Account",
                    body.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dismiss() {
        if (dialog != null && dialog.isShowing()) dialog.dismiss();
    }

    @Override
    public void onClick(View view) {
    }

    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {
        if (progressDialog != null && progressDialog.isShowing())
            AndroidUtils.dismiss_dialog(progressDialog);

        String type = httpResult.getRequestType();

        if (httpResult.getResult() != WebServiceHelper.ServiceCallStatus.Success) {
            AndroidUtils.showAlert("Something went wrong. Please try again.", activity);
            return;
        }

        try {
            JSONObject result = new JSONObject(httpResult.getResponseContent());
            boolean err = result.optBoolean("error", false);

            if ("Verify_IFSC".equals(type)) {
                if (!err) {
                    JSONObject data = result.optJSONObject("data");
                    String bankName = data != null ? data.optString("bank_name", "") : "";
                    String branchName = data != null ? data.optString("branch_name", "") : "";

                    suppressVerificationReset = true;
                    etBankName.setText(bankName);
                    etBranchName.setText(branchName);
                    suppressVerificationReset = false;
                    etBankName.setEnabled(true);

                    isVerified = true;
                    tvVerifiedStatus.setText("Verified");
                    tvVerifiedDate.setText("");
                    applyModeUi();
                    AndroidUtils.showAlert(result.optString("msg", "IFSC verified successfully"), activity);
                } else {
                    isVerified = false;
                    applyModeUi();
                    AndroidUtils.showAlert(result.optString("msg",
                            "Unable to verify IFSC code. Please check and try again."), activity);
                }
            }
            else if ("Save_Bank_Account".equals(type)) {
                if (!err) {
                    AndroidUtils.showAlert(result.optString("msg", "Bank account details saved successfully"), activity);
                    dismiss();
                    if (listener != null) listener.onBankAccountSaved();
                } else {
                    AndroidUtils.showAlert(result.optString("msg",
                            "Unable to save bank account details. Please try again."), activity);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}