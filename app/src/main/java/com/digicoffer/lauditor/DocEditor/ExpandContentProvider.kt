package com.digicoffer.lauditor.DocEditor

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.ContentValidation
import com.digicoffer.lauditor.CommonFiles.ValidationUtils.DescriptionValidation
import com.google.android.material.textfield.TextInputEditText

class ExpandContentProvider(
    context: Context,
    private val linearLayout: LinearLayout,
    private val isTitleVisible: Boolean,
    private val isContentVisible: Boolean
) : Dialog(context, androidx.appcompat.R.style.Base_Theme_AppCompat_Dialog) {

    private var ivCancelExpand: ImageView? = null
    private var tvContentTitle: TextView? = null
    private var tvContentText: TextView? = null
    private var etContentTitle: TextInputEditText? = null
    private var etContentText: TextInputEditText? = null
    private var btnSaveContent: AppCompatButton? = null
    private var ll_title: LinearLayout? = null
    private var ll_content: LinearLayout? = null

    private var titleLabel = "Title"
    private var contentLabel = "Content"
    private var titleTxt = ""
    private var titleHint = ""
    private var contentHint = ""
    private var onSaveClickListener: OnSaveClickListener? = null

    interface OnSaveClickListener {
        fun onSave(title: String, content: String)
    }

    init {
        this.linearLayout.alpha = 0.5f
        this.linearLayout.isEnabled = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.expand_content_layout)
        setCancelable(false)
        setCanceledOnTouchOutside(false)

        ivCancelExpand = findViewById(R.id.iv_cancel_expand)
        tvContentTitle = findViewById(R.id.tv_content_title)
        tvContentText = findViewById(R.id.tv_content_text)
        etContentTitle = findViewById(R.id.et_content_title)
        etContentText = findViewById(R.id.et_content_text)
        btnSaveContent = findViewById(R.id.btn_save_content)
        ll_title = findViewById(R.id.ll_title)
        ll_content = findViewById(R.id.ll_content)

        if (titleTxt.isNotEmpty()) {
            if (titleTxt == "Image") {
                tvContentTitle?.text = "Image Caption"
                tvContentText?.text = titleTxt
            } else {
                tvContentTitle?.text = "$titleTxt Title"
                tvContentText?.text = "$titleTxt Text"
            }
        }

        if (shouldIncludeTitle(titleLabel)) {
            etContentTitle?.setText(titleLabel)
        } else {
            etContentTitle?.setText("")
        }
        etContentText?.setText(contentLabel)

        etContentTitle?.let { etContentTitle ->
            etContentTitle.addTextChangedListener(ContentValidation(etContentTitle, false))
            etContentTitle.setHint(R.string.enter_title)
            val filters = arrayOf<InputFilter>(InputFilter.LengthFilter(255))
            etContentTitle.filters = filters
        }

        etContentText?.let { etContentText ->
            etContentText.addTextChangedListener(DescriptionValidation(etContentText))
            val filters1 = arrayOf<InputFilter>(InputFilter.LengthFilter(5000))
            etContentText.filters = filters1
        }

        ll_title?.visibility = if (isTitleVisible) View.VISIBLE else View.GONE
        ll_content?.visibility = if (isContentVisible) View.VISIBLE else View.GONE

        ivCancelExpand?.setOnClickListener {
            dismiss()
            linearLayout.alpha = 1.0f
            linearLayout.isEnabled = true
        }

        btnSaveContent?.setOnClickListener {
            if (onSaveClickListener != null) {
                val title = etContentTitle?.text?.toString()?.trim() ?: ""
                val content = etContentText?.text?.toString()?.trim() ?: ""
                onSaveClickListener?.onSave(title, content)
                linearLayout.alpha = 1.0f
                linearLayout.isEnabled = true
            }
        }

        window?.let { window ->
            window.setBackgroundDrawable(null)

            val marginInDp = 20
            val scale = context.resources.displayMetrics.density
            val marginInPx = (marginInDp * scale + 0.5f).toInt()

            val screenWidth = context.resources.displayMetrics.widthPixels
            val dialogWidth = screenWidth - (2 * marginInPx)

            window.setLayout(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    private fun shouldIncludeTitle(label: String): Boolean {
        return when (label.trim()) {
            "Overview", "Section", "Sub Section", "Sub Sub Section",
            "Paragraph", "Numbered List", "Bulleted List", "Page Break",
            "Image", "Table" -> false
            else -> true
        }
    }

    fun setTitles(titleTxt: String, titleLabel: String, contentLabel: String) {
        this.titleTxt = titleTxt
        this.titleLabel = titleLabel
        this.contentLabel = contentLabel
    }

    fun setHints(titleHint: String, contentHint: String) {
        this.titleHint = titleHint
        this.contentHint = contentHint
    }

    fun setOnSaveClickListener(listener: OnSaveClickListener?) {
        this.onSaveClickListener = listener
    }
}
