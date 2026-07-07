package com.digicoffer.lauditor.DocEditor;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;

import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.ContentValidation;
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.DescriptionValidation;
import com.google.android.material.textfield.TextInputEditText;

public class ExpandContentProvider extends Dialog {
    private ImageView ivCancelExpand;
    private TextView tvContentTitle, tvContentText;
    private TextInputEditText etContentTitle, etContentText;
    private AppCompatButton btnSaveContent;
    LinearLayout ll_title, ll_content;
    private String titleLabel = "Title", contentLabel = "Content", titleTxt = "";
    private String titleHint = "", contentHint = "";
    private OnSaveClickListener onSaveClickListener;
    LinearLayout linearLayout;
    Boolean isTitleVisible = true;
    Boolean isContentVisible = true;
    InputFilter[] filters, filters1;

    public ExpandContentProvider(Context context, LinearLayout llCreateView, Boolean isTitleVisible, Boolean isContentVisible) {
        super(context, androidx.appcompat.R.style.Base_Theme_AppCompat_Dialog); // You can customize style
        this.linearLayout = llCreateView;
        this.linearLayout.setAlpha(0.5f);
        this.linearLayout.setEnabled(false);
        this.isTitleVisible = isTitleVisible;
        this.isContentVisible = isContentVisible;
    }

    //    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.expand_content_layout);
//
//        ivCancelExpand = findViewById(R.id.iv_cancel_expand);
//        tvContentTitle = findViewById(R.id.tv_content_title);
//        tvContentText = findViewById(R.id.tv_content_text);
//        etContentTitle = findViewById(R.id.et_content_title);
//        etContentText = findViewById(R.id.et_content_text);
//        btnSaveContent = findViewById(R.id.btn_save_content);
//
//        tvContentTitle.setText(titleLabel);
//        tvContentText.setText(contentLabel);
//        etContentTitle.setHint(titleHint);
//        etContentText.setHint(contentHint);
//
//        ivCancelExpand.setOnClickListener(v -> dismiss());
//
//        btnSaveContent.setOnClickListener(v -> {
//            if (onSaveClickListener != null) {
//                onSaveClickListener.onSave(
//                        etContentTitle.getText() != null ? etContentTitle.getText().toString().trim() : "",
//                        etContentText.getText() != null ? etContentText.getText().toString().trim() : ""
//                );
//            }
//        });
//        if (getWindow() != null) {
//            getWindow().setLayout(
//                    ViewGroup.LayoutParams.MATCH_PARENT, // width: match parent
//                    ViewGroup.LayoutParams.WRAP_CONTENT  // height: wrap content
//            );
//        }
//    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.expand_content_layout);
        setCancelable(false); // prevent back button dismissal
        setCanceledOnTouchOutside(false);
        ivCancelExpand = findViewById(R.id.iv_cancel_expand);
        tvContentTitle = findViewById(R.id.tv_content_title);
        tvContentText = findViewById(R.id.tv_content_text);
        etContentTitle = findViewById(R.id.et_content_title);
        etContentText = findViewById(R.id.et_content_text);
        btnSaveContent = findViewById(R.id.btn_save_content);
        ll_title = findViewById(R.id.ll_title);
        ll_content = findViewById(R.id.ll_content);

        if (!titleTxt.isEmpty()) {
            if (titleTxt.equals("Image")) {
                tvContentTitle.setText("Image Caption");
                tvContentText.setText(titleTxt);
            } else {
                tvContentTitle.setText(titleTxt + " Title");
                tvContentText.setText(titleTxt + " Text");
            }
        }
        if (shouldIncludeTitle(titleLabel)) {
            etContentTitle.setText(titleLabel);
        } else {
            etContentTitle.setText(""); // default label, don't show in edit field
        }
        etContentText.setText(contentLabel);

        etContentTitle.addTextChangedListener(new ContentValidation(etContentTitle,false));
        etContentTitle.setHint(R.string.enter_title);
        etContentText.addTextChangedListener(new DescriptionValidation(etContentText));
        filters = new InputFilter[]{
                new InputFilter.LengthFilter(255)
        };
        filters1 = new InputFilter[]{
                new InputFilter.LengthFilter(5000)
        };
        etContentTitle.setFilters(filters);
        etContentText.setFilters(filters1);
        if (isTitleVisible) {
            ll_title.setVisibility(View.VISIBLE);
        } else {
            ll_title.setVisibility(View.GONE);
        }
        if (isContentVisible) {
            ll_content.setVisibility(View.VISIBLE);
        } else {
            ll_content.setVisibility(View.GONE);
        }
        ivCancelExpand.setOnClickListener(v -> {
            dismiss();
            linearLayout.setAlpha(1.0f);
            linearLayout.setEnabled(true);
        });

        btnSaveContent.setOnClickListener(v -> {
            if (onSaveClickListener != null) {
                onSaveClickListener.onSave(
                        etContentTitle.getText() != null ? etContentTitle.getText().toString().trim() : "",
                        etContentText.getText() != null ? etContentText.getText().toString().trim() : ""
                );
                linearLayout.setAlpha(1.0f);
                linearLayout.setEnabled(true);
            }
        });

        if (getWindow() != null) {
            // 👇 This removes default dialog background
            if (getWindow() != null) {
                getWindow().setBackgroundDrawable(null);

                int marginInDp = 20;
                float scale = getContext().getResources().getDisplayMetrics().density;
                int marginInPx = (int) (marginInDp * scale + 0.5f);

                int screenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
                int dialogWidth = screenWidth - (2 * marginInPx);

                getWindow().setLayout(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        }
    }

    private boolean shouldIncludeTitle(String label) {
        switch (label.trim()) {
            case "Overview":
            case "Section":
            case "Sub Section":
            case "Sub Sub Section":
            case "Paragraph":
            case "Numbered List":
            case "Bulleted List":
            case "Page Break":
            case "Image":
            case "Table":
                return false; // don't include title
            default:
                return true;  // custom titles should be shown
        }
    }


    public void setTitles(String titleTxt, String titleLabel, String contentLabel) {
        this.titleTxt = titleTxt;
        this.titleLabel = titleLabel;
        this.contentLabel = contentLabel;
    }

    public void setHints(String titleHint, String contentHint) {
        this.titleHint = titleHint;
        this.contentHint = contentHint;
    }

    public void setOnSaveClickListener(OnSaveClickListener listener) {
        this.onSaveClickListener = listener;
    }

    public interface OnSaveClickListener {
        void onSave(String title, String content);
    }
}
