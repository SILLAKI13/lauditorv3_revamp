package com.digicoffer.lauditor.CommonFiles.ValidationUtils

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView

class Validation(private val editText: EditText?) : TextWatcher {

    init {
        if (editText != null) {
            // Restrict Enter key and handle "Done" action
            editText.setSingleLine(true)
            editText.imeOptions = EditorInfo.IME_ACTION_DONE
            editText.setOnEditorActionListener(object : TextView.OnEditorActionListener {
                override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                    if (actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER
                                && event.action == KeyEvent.ACTION_DOWN)
                    ) {
                        // Hide keyboard
                        val imm = editText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                        imm?.hideSoftInputFromWindow(editText.windowToken, 0)

                        // Clear focus so keyboard stays hidden
                        editText.clearFocus()
                        return true // consume the event
                    }
                    return false
                }
            })
        }
    }

    override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(editable: Editable?) {
        if (editable == null) return
        val input = editable.toString()

        if (input.isEmpty()) return

        val cleanInput: String
        val firstChar = input[0]

        if (firstChar == ' ' || !Character.isLetterOrDigit(firstChar)) {
            cleanInput = input.replaceFirst("^[^a-zA-Z0-9]".toRegex(), "")
        } else {
            cleanInput = input.replace("[^a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>?/~` ]".toRegex(), "")
        }

        if (cleanInput != input) {
            editText?.removeTextChangedListener(this)
            editText?.setText(cleanInput)
            editText?.setSelection(cleanInput.length)
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
