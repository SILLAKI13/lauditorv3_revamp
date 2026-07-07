package com.digicoffer.lauditor.CommonFiles.ValidationUtils;

import android.text.InputFilter;
import android.text.Spanned;

public class InputFilterValidation implements InputFilter {
    @Override
    public CharSequence filter(CharSequence source, int start, int end,
                               Spanned dest, int dstart, int dend) {
        // Build final string if source is inserted into dest
        String result = dest.subSequence(0, dstart)
                + source.toString()
                + dest.subSequence(dend, dest.length());

        // Disallow if first character is space or special character
        if (result.length() > 0 && (result.charAt(0) == ' ' || !Character.isLetterOrDigit(result.charAt(0)))) {
            return "";
        }

        // Allow only letters, digits, and common special characters after the first character
        String allowedPattern = "[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>?/~` ]*";
        if (!result.matches(allowedPattern)) {
            return "";
        }

        return null; // Accept input
    }
}

