package com.digicoffer.lauditor.CommonFiles.ValidationUtils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

class ContentValidation(
    private val editText: EditText?,
    private val isFile: Boolean
) : TextWatcher {

    override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(editable: Editable?) {
        if (editable == null) return
        val input = editable.toString()

        // If empty, return
        if (input.isEmpty()) return

        val cleanInput = StringBuilder()

        for (i in 0 until input.length) {
            val c = input[i]

            // Reject backslash, {, }
            if (c == '\\' || c == '{' || c == '}') continue

            if (i == 0) {
                // First character rules
                if (c == ' ') continue
                if (Character.isDigit(c)) continue

                if (isFile) {
                    if (!Character.isLetter(c)) continue // File name must start with letter
                } else {
                    if (c == '[' || c == ']' || c == '<' || c == '>' || c == '|') continue
                    if (!Character.isLetterOrDigit(c)) continue // Must start with alphanumeric
                }

                cleanInput.append(c)
            } else {
                // Remaining characters rules
                if (isFile) {
                    if (Character.isLetterOrDigit(c) || c == '-' || c == '_' || c == ' ') {
                        cleanInput.append(c)
                    }
                } else {
                    // Allow space in middle, along with other safe characters
                    if (c.toString().matches("[a-zA-Z0-9 !@#$%^&*()_+\\-=\\[\\]{};':\"|,.<>?/~]".toRegex())) {
                        cleanInput.append(c)
                    }
                }
            }
        }

        val finalInput = cleanInput.toString()

        if (finalInput != input) {
            editText?.removeTextChangedListener(this)
            editText?.setText(finalInput)
            editText?.setSelection(finalInput.length)
            editText?.addTextChangedListener(this)
        }
    }

    companion object {
        @JvmStatic
        fun isEmpty(editText: EditText?): Boolean {
            return editText == null || editText.text.toString().trim().isEmpty()
        }
    }
}
