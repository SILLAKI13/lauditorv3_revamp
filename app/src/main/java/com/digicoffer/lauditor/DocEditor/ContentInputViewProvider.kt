package com.digicoffer.lauditor.DocEditor

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.DescriptionValidation
import com.google.android.material.textfield.TextInputEditText

class ContentInputViewProvider(
    context: Context,
    parentContainer: ViewGroup?,
    listener: ContentActionListener?,
    docEditor: DocEditor
) {
    private val view: View = LayoutInflater.from(context).inflate(R.layout.add_doc_content, null)
    var titleTxt: String? = null
    private val tv_content: TextView = view.findViewById(R.id.tv_content)
    private val editText: TextInputEditText = view.findViewById(R.id.et_content)
    private val iv_remove: ImageView = view.findViewById(R.id.iv_remove)
    private val iv_expand: ImageView = view.findViewById(R.id.iv_expand)

    interface ContentActionListener {
        fun onRemoveClicked(view: View)
        fun onExpandClicked(titleTxt: String?, title: String?, content: String?, targetView: View)
    }

    init {
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 20)
        view.layoutParams = params
        view.isFocusable = true
        view.isFocusableInTouchMode = true
        view.requestFocus()

        view.post {
            val parent = view.parent
            if (parent is ViewGroup) {
                parent.requestChildFocus(view, view)
            }
        }

        editText.addTextChangedListener(DescriptionValidation(editText))
        val hint = "Enter Your Content"
        editText.hint = hint
        editText.requestFocus()

        editText.post {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }

        iv_remove.setOnClickListener {
            parentContainer?.removeView(view)
            listener?.onRemoveClicked(view)
            docEditor.checkConsecutivePageBreaks()
        }

        iv_expand.setOnClickListener {
            if (listener != null) {
                val tvTitle = view.findViewById<TextView>(R.id.tv_content)
                val etContent = view.findViewById<TextInputEditText>(R.id.et_content)

                val displayTitle = tvTitle.text.toString()
                val baseTitle = titleTxt
                val content = etContent.text.toString()

                listener.onExpandClicked(baseTitle, displayTitle, content, view)
            }
        }
    }

    fun AddDocContent(titleTxt: String, title: String, hintStr: String) {
        var hint = hintStr
        var displayTitle = title
        if (hint.startsWith("[[title]]")) {
            val newlineIndex = hint.indexOf("\n")
            if (newlineIndex != -1) {
                displayTitle = hint.substring(9, newlineIndex).trim()
                hint = hint.substring(newlineIndex + 1)
            }
        }

        setText(displayTitle)
        if (hint.isNotEmpty()) {
            editText.setText(hint)
        }
        this.titleTxt = titleTxt
        tv_content.tag = titleTxt
    }

    fun getView(): View {
        return view
    }

    fun getEnteredText(): String {
        return editText.text.toString()
    }

    fun setText(text: String) {
        tv_content.text = text
    }
}
