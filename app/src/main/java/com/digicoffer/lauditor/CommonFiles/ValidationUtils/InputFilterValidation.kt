package com.digicoffer.lauditor.CommonFiles.ValidationUtils

import android.text.InputFilter
import android.text.Spanned

class InputFilterValidation : InputFilter {
    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        if (source == null || dest == null) return null

        // Build final string if source is inserted into dest
        val result = (dest.subSequence(0, dstart).toString()
                + source.toString()
                + dest.subSequence(dend, dest.length).toString())

        // Disallow if first character is space or special character
        if (result.isNotEmpty() && (result[0] == ' ' || !Character.isLetterOrDigit(result[0]))) {
            return ""
        }

        // Allow only letters, digits, and common special characters after the first character
        val allowedPattern = "[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>?/~` ]*"
        if (!result.matches(allowedPattern.toRegex())) {
            return ""
        }

        return null // Accept input
    }
}
