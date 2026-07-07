package com.digicoffer.lauditor.DocEditor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.DescriptionValidation;
import com.google.android.material.textfield.TextInputEditText;

public class ContentInputViewProvider {
    private View view;
    String titleTxt;
    private TextView tv_content;
    private TextInputEditText editText;
    private ImageView iv_remove, iv_expand;

    public interface ContentActionListener {
        void onRemoveClicked(View view);

        void onExpandClicked(String titleTxt, String title, String content, View targetView);
    }


    public ContentInputViewProvider(Context context, ViewGroup parentContainer, ContentActionListener listener, DocEditor docEditor) {
        view = LayoutInflater.from(context).inflate(R.layout.add_doc_content, null);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 20);
        view.setLayoutParams(params);
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.requestFocus();

        view.post(() -> {
            ViewParent parent = view.getParent();
            if (parent instanceof ViewGroup) {
                parent.requestChildFocus(view, view);
            }
        });
        tv_content = view.findViewById(R.id.tv_content);
        editText = view.findViewById(R.id.et_content);
        editText.addTextChangedListener(new DescriptionValidation(editText));
        iv_expand = view.findViewById(R.id.iv_expand);
        iv_remove = view.findViewById(R.id.iv_remove);
        String hint = "Enter Your Content";
        editText.setHint(hint);
        editText.requestFocus(); // ✅ Move cursor to this field

        // Show keyboard
        editText.post(() -> {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            }
        });
        iv_remove.setOnClickListener(v -> {
            if (parentContainer != null) {
                parentContainer.removeView(view);
            }
            if (listener != null) {
                listener.onRemoveClicked(view);
            }
            docEditor.checkConsecutivePageBreaks();
        });

//        iv_expand.setOnClickListener(v -> {
//            if (listener != null) {
//                TextView tvTitle = view.findViewById(R.id.tv_content);
//                TextInputEditText etContent = view.findViewById(R.id.et_content);
//
//                String title = tvTitle.getText().toString();
//                String content = etContent.getText().toString();
//
//                listener.onExpandClicked(title, titleTxt, content, view);
//            }
//        });
        iv_expand.setOnClickListener(v -> {
            if (listener != null) {
                TextView tvTitle = view.findViewById(R.id.tv_content);
                TextInputEditText etContent = view.findViewById(R.id.et_content);

                String displayTitle = tvTitle.getText().toString();   // custom or original name shown in UI
                String baseTitle = titleTxt;                          // original static title, e.g., "Section"
                String content = etContent.getText().toString();

                listener.onExpandClicked(baseTitle, displayTitle, content, view);
            }
        });

    }

    //    public void AddDocContent(String titleTxt, String title, String hint)
//    public void AddDocContent(String titleTxt, String title, String hint) {
//        setText(title);
//        if (!hint.isEmpty()) {
//            editText.setText(hint);
//        }
//        this.titleTxt = titleTxt;
//        tv_content.setTag(titleTxt); // ✅ Attach original type (e.g., "Section")
//    }
    public void AddDocContent(String titleTxt, String title, String hint) {
        // Check if hint has embedded [[title]]
        String displayTitle = title;
        if (hint != null && hint.startsWith("[[title]]")) {
            int newlineIndex = hint.indexOf("\n");
            if (newlineIndex != -1) {
                displayTitle = hint.substring(9, newlineIndex).trim();  // Extract custom title
                hint = hint.substring(newlineIndex + 1);                // Remaining is content
            }
        }

        setText(displayTitle); // show "Client Info"
        if (!hint.isEmpty()) {
            editText.setText(hint); // show content
        }
        this.titleTxt = titleTxt;      // e.g., "Section"
        tv_content.setTag(titleTxt);   // important for validation
    }


    public View getView() {
        return view;
    }

    public String getEnteredText() {
        return editText.getText().toString();
    }

    public void setText(String text) {
        tv_content.setText(text);
    }
}




