package com.digicoffer.lauditor.CommonFiles.ValidationUtils;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

public class Validation implements TextWatcher {

    private EditText editText;

    public Validation(EditText editText) {
        this.editText = editText;

        // Restrict Enter key and handle "Done" action
        editText.setSingleLine(true);
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                                && event.getAction() == KeyEvent.ACTION_DOWN)) {

                    // Hide keyboard
                    InputMethodManager imm = (InputMethodManager) editText.getContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                    }

                    // Clear focus so keyboard stays hidden
                    editText.clearFocus();
                    return true; // consume the event
                }
                return false;
            }
        });
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) { }

    @Override
    public void onTextChanged(CharSequence charSequence, int start, int before, int count) { }

    @Override
    public void afterTextChanged(Editable editable) {
        String input = editable.toString();

        if (input.isEmpty()) return;

        String cleanInput;
        char firstChar = input.charAt(0);

        if (firstChar == ' ' || !Character.isLetterOrDigit(firstChar)) {
            cleanInput = input.replaceAll("^[^a-zA-Z0-9]", "");
        } else {
            cleanInput = input.replaceAll("[^a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>?/~` ]", "");
        }

        if (!cleanInput.equals(input)) {
            editText.removeTextChangedListener(this);
            editText.setText(cleanInput);
            editText.setSelection(cleanInput.length());
            editText.addTextChangedListener(this);
        }
    }

    public static boolean isEmpty(EditText editText) {
        return editText.getText().toString().trim().isEmpty();
    }
}
