package com.digicoffer.lauditor.CommonFiles.ValidationUtils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

class ListValidation(private val editText: EditText?) : TextWatcher {

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(editable: Editable?) {
        if (editable == null) return
        val input = editable.toString()

        if (input.isEmpty()) return

        val clean = StringBuilder()

        for (i in 0 until input.length) {
            val c = input[i]

            // Reject backslash, {, }
            if (c == '\\' || c == '{' || c == '}') continue

            if (i == 0) {
                // No space at first index
                if (c == ' ') continue

                // Allow digits for numbered lists
                if (Character.isDigit(c)) {
                    clean.append(c)
                    continue
                }

                // Allow bullet symbols (-, *, •, etc.)
                if ("•*-‣◦".indexOf(c) >= 0) {
                    clean.append(c)
                    continue
                }

                // Allow letters
                if (Character.isLetter(c)) {
                    clean.append(c)
                    continue
                }

                // Allow punctuation at start
                if (c.toString().matches("[!@#$%^&*()_+\\-=\\[\\]';:\"|,.<>?/~]".toRegex())) {
                    clean.append(c)
                    continue
                }

                // Otherwise ignore
                continue
            } else {
                // Rest of characters – same as ContentValidation's "else" block
                if (c.toString().matches("[a-zA-Z0-9 !@#$%^&*()_+\\-=\\[\\]{};':\"|,.<>?/~]".toRegex())) {
                    clean.append(c)
                }
            }
        }

        val finalInput = clean.toString()

        if (finalInput != input) {
            editText?.removeTextChangedListener(this)
            editText?.setText(finalInput)
            editText?.setSelection(finalInput.length)
            editText?.addTextChangedListener(this)
        }
    }
}
