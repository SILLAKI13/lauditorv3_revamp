package com.digicoffer.lauditor.CommonFiles.ValidationUtils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class ListValidation implements TextWatcher {

    private final EditText editText;

    public ListValidation(EditText editText) {
        this.editText = editText;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable editable) {
        String input = editable.toString();

        if (input.isEmpty()) return;

        StringBuilder clean = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            // Reject backslash, {, }
            if (c == '\\' || c == '{' || c == '}') continue;

            if (i == 0) {
                // ❌ No space at first index
                if (c == ' ') continue;

                // ✔️ Allow digits for numbered lists
                if (Character.isDigit(c)) {
                    clean.append(c);
                    continue;
                }

                // ✔️ Allow bullet symbols (-, *, •, etc.)
                if ("•*-‣◦".indexOf(c) >= 0) {
                    clean.append(c);
                    continue;
                }

                // ✔️ Allow letters
                if (Character.isLetter(c)) {
                    clean.append(c);
                    continue;
                }

                // ✔️ Allow punctuation at start
                if (String.valueOf(c).matches("[!@#$%^&*()_+\\-=\\[\\]';:\"|,.<>?/~]")) {
                    clean.append(c);
                    continue;
                }

                // Otherwise ignore
                continue;
            }
            else {
                // Rest of characters – same as ContentValidation's "else" block
                if (String.valueOf(c).matches("[a-zA-Z0-9 !@#$%^&*()_+\\-=\\[\\]{};':\"|,.<>?/~]")) {
                    clean.append(c);
                }
            }
        }

        String finalInput = clean.toString();

        if (!finalInput.equals(input)) {
            editText.removeTextChangedListener(this);
            editText.setText(finalInput);
            editText.setSelection(finalInput.length());
            editText.addTextChangedListener(this);
        }
    }
}
