package com.digicoffer.lauditor.CommonFiles.TermsAndCondition;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.CommonFiles.PdfUtils.RetrievePDFfromUrl;
import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.material.button.MaterialButton;

import java.io.InputStream;

public class TermsAndConditionsDialog {

    private static final String TAG = "TermsDialog";
    private static TermsAndConditionsDialog instance;
    private Dialog dialog;
    private PDFView pdfView;
    private ProgressBar progressBar;
    private TextView tvError;
    private TextView tvTerms;
    private CheckBox cbAgree;
    private MaterialButton btnAccept, btnDecline;
    private RetrievePDFfromUrl pdfLoader;

    public interface OnTermsActionListener {
        void onAccept(String version);
        void onDecline();
    }

    private TermsAndConditionsDialog() {}

    public static synchronized TermsAndConditionsDialog getInstance() {
        if (instance == null) {
            instance = new TermsAndConditionsDialog();
        }
        return instance;
    }

    public void show(Context context, String pdfUrl, String version, String message, OnTermsActionListener listener) {
        Log.d(TAG, "show() called");

        if (context instanceof android.app.Activity) {
            ((android.app.Activity) context).runOnUiThread(() -> {
                Log.d(TAG, "Running on UI thread");
                showDialogInternal(context, pdfUrl, version, message, listener);
            });
        } else {
            Log.d(TAG, "Not an Activity context");
            showDialogInternal(context, pdfUrl, version, message, listener);
        }
    }

    private void showDialogInternal(Context context, String pdfUrl, String version, String message, OnTermsActionListener listener) {
        Log.d(TAG, "showDialogInternal started");

        if (!(context instanceof android.app.Activity)) {
            Log.e(TAG, "Context is not an Activity");
            return;
        }

        android.app.Activity activity = (android.app.Activity) context;
        if (activity.isFinishing() || activity.isDestroyed()) {
            Log.e(TAG, "Activity is finishing or destroyed");
            return;
        }

        if (pdfLoader != null) {
            pdfLoader.cancelLoading();
            pdfLoader.cancel(true);
            pdfLoader = null;
        }

        dismiss();

        try {
            dialog = new Dialog(activity);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

            LayoutInflater inflater = LayoutInflater.from(activity);
            View view = inflater.inflate(R.layout.terms_condition_layout, null);
            dialog.setContentView(view);

            if (dialog.getWindow() != null) {
                dialog.getWindow().setLayout(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT
                );
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }

            ImageView ivClose = view.findViewById(R.id.iv_close);
            pdfView = view.findViewById(R.id.pdfView);
            progressBar = view.findViewById(R.id.progressBar);
            tvError = view.findViewById(R.id.tv_error);
            tvTerms = view.findViewById(R.id.tv_terms);
            cbAgree = view.findViewById(R.id.cb_agree);
            btnAccept = view.findViewById(R.id.btn_accept);
            btnDecline = view.findViewById(R.id.btn_decline);

            ivClose.setOnClickListener(v -> {
                if (listener != null) listener.onDecline();
                dismiss();
            });

            setupTermsText(activity);

            cbAgree.setOnCheckedChangeListener((buttonView, isChecked) -> {
                updateAcceptButtonState();
            });

            btnAccept.setEnabled(false);
            btnAccept.setAlpha(0.5f);

            btnAccept.setOnClickListener(v -> {
                if (cbAgree.isChecked()) {
                    Log.d(TAG, "Accept button clicked");
                    if (listener != null) listener.onAccept(version);
//                    dismiss();
                } else {
                    android.widget.Toast.makeText(context, "Please agree to the terms to continue", android.widget.Toast.LENGTH_SHORT).show();
                }
            });

            btnDecline.setOnClickListener(v -> {
                Log.d(TAG, "Decline button clicked");
                if (listener != null) listener.onDecline();
                dismiss();
            });

            dialog.show();
            Log.d(TAG, "Dialog shown successfully");

            if (pdfUrl != null && !pdfUrl.isEmpty()) {
                Log.d(TAG, "Loading PDF from URL: " + pdfUrl);
                progressBar.setVisibility(View.VISIBLE);
                pdfView.setVisibility(View.INVISIBLE);
                tvError.setVisibility(View.GONE);

                pdfLoader = new RetrievePDFfromUrl(pdfView, progressBar) {
                    @Override
                    protected void onPostExecute(InputStream inputStream) {
                        if (inputStream == null) {
                            activity.runOnUiThread(() -> showError("Failed to load PDF"));
                            return;
                        }

                        pdfView.fromStream(inputStream)
                                .onLoad(totalPages -> {
                                    activity.runOnUiThread(() -> {
                                        Log.d(TAG, "PDF loaded, total pages: " + totalPages);
                                        progressBar.setVisibility(View.GONE);
                                        pdfView.setVisibility(View.VISIBLE);
                                        updateAcceptButtonState();
                                    });
                                })
                                .onError(error -> {
                                    activity.runOnUiThread(() -> {
                                        Log.e(TAG, "PDF error: " + error.getMessage());
                                        showError("Failed to load PDF");
                                    });
                                })
                                .load();
                    }
                };
                pdfLoader.execute(pdfUrl);
            } else {
                Log.e(TAG, "PDF URL is null or empty");
                showError("No PDF available");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error showing dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupTermsText(android.app.Activity activity) {
        String fullText = "By clicking this, you agree to the Privacy Policy and Cookies Policy, along with the Terms & Conditions.";
        SpannableString spannable = new SpannableString(fullText);

        // Privacy Policy link
        int privacyStart = fullText.indexOf("Privacy Policy");
        int privacyEnd = privacyStart + "Privacy Policy".length();
        spannable.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                showPdfDialog(activity, "Privacy Policy", "https://digicoffer.com/digicoffer_privacy.pdf");
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setColor(activity.getResources().getColor(R.color.blue));
                ds.setUnderlineText(true);
            }
        }, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Cookies Policy link
        int cookiesStart = fullText.indexOf("Cookies Policy");
        int cookiesEnd = cookiesStart + "Cookies Policy".length();
        spannable.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                showPdfDialog(activity, "Cookies Policy", "https://digicoffer.com/digicoffer_cookie.pdf");
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setColor(activity.getResources().getColor(R.color.blue));
                ds.setUnderlineText(true);
            }
        }, cookiesStart, cookiesEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

//        // Terms & Conditions link
//        int termsStart = fullText.indexOf("Terms & Conditions");
//        int termsEnd = termsStart + "Terms & Conditions".length();
//        spannable.setSpan(new ClickableSpan() {
//            @Override
//            public void onClick(@NonNull View widget) {
//                showPdfDialog(activity, "Terms & Conditions", "https://digicoffer.com/digicoffer_terms.pdf");
//            }
//
//            @Override
//            public void updateDrawState(@NonNull TextPaint ds) {
//                ds.setColor(activity.getResources().getColor(R.color.blue));
//                ds.setUnderlineText(true);
//            }
//        }, termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvTerms.setText(spannable);
        tvTerms.setMovementMethod(LinkMovementMethod.getInstance());
        tvTerms.setHighlightColor(Color.TRANSPARENT);
    }

    private void showPdfDialog(android.app.Activity activity, String title, String pdfUrl) {
        Log.d(TAG, "showPdfDialog called - Title: " + title + ", URL: " + pdfUrl);

        Dialog dialog = new Dialog(activity);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.dialog_webview, null);
        dialog.setContentView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
            );
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        PDFView pdfView = view.findViewById(R.id.pdfView);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        TextView tvError = view.findViewById(R.id.tv_error);
        ImageView ivClose = view.findViewById(R.id.iv_close);
        TextView tvTitle = view.findViewById(R.id.tv_title);

        // Set the title
        tvTitle.setText(title);
        Log.d(TAG, "Title set to: " + tvTitle.getText().toString());

        // Reset UI state
        progressBar.setVisibility(View.VISIBLE);
        pdfView.setVisibility(View.GONE);
        tvError.setVisibility(View.GONE);

        // Create a new instance for this dialog
        dialog.show();
        Log.d(TAG, "Dialog shown successfully");
try{
        if (pdfUrl != null && !pdfUrl.isEmpty()) {
            Log.d(TAG, "Loading PDF from URL: " + pdfUrl);
            progressBar.setVisibility(View.VISIBLE);
            pdfView.setVisibility(View.INVISIBLE);
            tvError.setVisibility(View.GONE);

            pdfLoader = new RetrievePDFfromUrl(pdfView, progressBar) {
                @Override
                protected void onPostExecute(InputStream inputStream) {
                    if (inputStream == null) {
                        activity.runOnUiThread(() -> showError("Failed to load PDF"));
                        return;
                    }

                    pdfView.fromStream(inputStream)
                            .onLoad(totalPages -> {
                                activity.runOnUiThread(() -> {
                                    Log.d(TAG, "PDF loaded, total pages: " + totalPages);
                                    progressBar.setVisibility(View.GONE);
                                    pdfView.setVisibility(View.VISIBLE);
                                    updateAcceptButtonState();
                                });
                            })
                            .onError(error -> {
                                activity.runOnUiThread(() -> {
                                    Log.e(TAG, "PDF error: " + error.getMessage());
                                    showError("Failed to load PDF");
                                });
                            })
                            .load();
                }
            };
            pdfLoader.execute(pdfUrl);
        } else {
            Log.e(TAG, "PDF URL is null or empty");
            showError("No PDF available");
        }

    } catch (Exception e) {
        Log.e(TAG, "Error showing dialog: " + e.getMessage());
        e.printStackTrace();
    }

        ivClose.setOnClickListener(v -> {
            pdfLoader.cancelLoading();
            pdfLoader.cancel(true);
            dialog.dismiss();
        });
    }

    private void updateAcceptButtonState() {
        if (btnAccept != null) {
            boolean isChecked = cbAgree != null && cbAgree.isChecked();
            boolean isPdfVisible = pdfView != null && pdfView.getVisibility() == View.VISIBLE;
            boolean enable = isChecked && isPdfVisible;
            btnAccept.setEnabled(enable);
            btnAccept.setAlpha(enable ? 1.0f : 0.5f);
            Log.d(TAG, "Accept button enabled: " + enable);
        }
    }

    private void enableAcceptButton(boolean enable) {
        if (btnAccept != null) {
            boolean isChecked = cbAgree != null && cbAgree.isChecked();
            btnAccept.setEnabled(enable && isChecked);
            btnAccept.setAlpha((enable && isChecked) ? 1.0f : 0.5f);
        }
    }

    private void showError(String errorMessage) {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (pdfView != null) pdfView.setVisibility(View.GONE);
        if (tvError != null) {
            tvError.setVisibility(View.VISIBLE);
            tvError.setText(errorMessage);
        }
        enableAcceptButton(false);
    }

    public void dismiss() {
        Log.d(TAG, "dismiss() called");
        if (pdfLoader != null) {
            pdfLoader.cancelLoading();
            pdfLoader.cancel(true);
            pdfLoader = null;
        }

        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
            Log.d(TAG, "Dialog dismissed");
        }
    }

    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }
}