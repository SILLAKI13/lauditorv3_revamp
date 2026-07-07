package com.digicoffer.lauditor.CommonFiles.ValidationUtils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class ContentValidation implements TextWatcher {

    private EditText editText;
    private boolean isFile;

    public ContentValidation(EditText editText, boolean isFile) {
        this.editText = editText;
        this.isFile = isFile;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
        // No-op
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
        // No-op
    }

//    @Override
//    public void afterTextChanged(Editable editable) {
//        String input = editable.toString();
//
//        // If empty, return
//        if (input.isEmpty()) return;
//
//        StringBuilder cleanInput = new StringBuilder();
//
//        for (int i = 0; i < input.length(); i++) {
//            char c = input.charAt(i);
//
//            // Reject backslash, {, }
//            if (c == '\\' || c == '{' || c == '}') continue;
//
//            if (i == 0) {
//                // First character rules
//                if (c == ' ') continue; // Disallow space
//                if (Character.isDigit(c)) continue; // Disallow digit
//
//                if (isFile) {
//                    if (!Character.isLetter(c)) continue; // For file name: only letter at start
//                } else {
//                    if (c == '[' || c == ']' || c == '<' || c == '>' || c == '|') continue; // Disallowed at first pos
//                    if (!Character.isLetterOrDigit(c)) continue; // Non-alphanum disallowed at start
//                }
//
//                cleanInput.append(c);
//            } else {
//                // Remaining characters rules
//                if (isFile) {
//                    if (Character.isLetterOrDigit(c) || c == '-' || c == '_') {
//                        cleanInput.append(c);
//                    }
//                } else {
//                    // Allow common special characters (excluding restricted ones)
//                    if (String.valueOf(c).matches("[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"|,.<>?/~ ]")) {
//                        cleanInput.append(c);
//                    }
//                }
//            }
//        }
//
//        String finalInput = cleanInput.toString();
//
//        if (!finalInput.equals(input)) {
//            editText.removeTextChangedListener(this);
//            editText.setText(finalInput);
//            editText.setSelection(finalInput.length());
//            editText.addTextChangedListener(this);
//        }
//    }
    @Override
    public void afterTextChanged(Editable editable) {
        String input = editable.toString();

        // If empty, return
        if (input.isEmpty()) return;

        StringBuilder cleanInput = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            // Reject backslash, {, }
            if (c == '\\' || c == '{' || c == '}') continue;

            if (i == 0) {
                // First character rules
                if (c == ' ') continue;
                if (Character.isDigit(c)) continue;

                if (isFile) {
                    if (!Character.isLetter(c)) continue; // File name must start with letter
                } else {
                    if (c == '[' || c == ']' || c == '<' || c == '>' || c == '|') continue;
                    if (!Character.isLetterOrDigit(c)) continue; // Must start with alphanumeric
                }

                cleanInput.append(c);
            } else {
                // Remaining characters rules
                if (isFile) {
                    if (Character.isLetterOrDigit(c) || c == '-' || c == '_' || c == ' ') {
                        cleanInput.append(c);
                    }
                } else {
                    // ✅ Allow space in middle, along with other safe characters
                    if (String.valueOf(c).matches("[a-zA-Z0-9 !@#$%^&*()_+\\-=\\[\\]{};':\"|,.<>?/~]")) {
                        cleanInput.append(c);
                    }
                }
            }
        }

        String finalInput = cleanInput.toString();

        if (!finalInput.equals(input)) {
            editText.removeTextChangedListener(this);
            editText.setText(finalInput);
            editText.setSelection(finalInput.length());
            editText.addTextChangedListener(this);
        }
    }


    public static boolean isEmpty(EditText editText) {
        return editText.getText().toString().trim().isEmpty();
    }
}
