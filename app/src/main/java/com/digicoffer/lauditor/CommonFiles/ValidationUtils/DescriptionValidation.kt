package com.digicoffer.lauditor.CommonFiles.ValidationUtils

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView

class DescriptionValidation(private val editText: EditText?) : TextWatcher {

    init {
        if (editText != null) {
            editText.setMaxLines(Integer.MAX_VALUE)
            editText.setHorizontallyScrolling(false)
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

                        // Clear focus to prevent keyboard from reopening
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
        var input = editable.toString()

        if (input.isEmpty()) return

        val cleanInput: String

        // Handle first character restrictions
        val firstChar = input[0]
        if (firstChar == ' ' || firstChar == '[' || firstChar == ']' || firstChar == '|' ||
            firstChar == '<' || firstChar == '>' || (!Character.isLetterOrDigit(firstChar) && firstChar != '\n')
        ) {
            input = input.replaceFirst("^[ \\[\\]\\|<>\\W]+".toRegex(), "")
        }

        // Remove globally restricted characters
        var temp = input.replace("[{}]".toRegex(), "") // Note: backslash is handled as part of regex escape or separately. In Java it was: [{}\\\\]
        temp = temp.replace("\\\\".toRegex(), "")
        cleanInput = temp.replace("[^a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\];':\"|,.<>?/~` \\n]".toRegex(), "")

        if (cleanInput != editable.toString()) {
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
