package com.digicoffer.lauditor.CommonFiles.TermsAndCondition

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.digicoffer.lauditor.CommonFiles.PdfUtils.RetrievePDFfromUrl
import com.digicoffer.lauditor.R
import com.github.barteksc.pdfviewer.PDFView
import com.google.android.material.button.MaterialButton
import java.io.InputStream

class TermsAndConditionsDialog private constructor() {

    private var dialog: Dialog? = null
    private var pdfView: PDFView? = null
    private var progressBar: ProgressBar? = null
    private var tvError: TextView? = null
    private var tvTerms: TextView? = null
    private var cbAgree: CheckBox? = null
    private var btnAccept: MaterialButton? = null
    private var btnDecline: MaterialButton? = null
    private var pdfLoader: RetrievePDFfromUrl? = null

    interface OnTermsActionListener {
        fun onAccept(version: String)
        fun onDecline()
    }

    fun show(context: Context, pdfUrl: String?, version: String, message: String?, listener: OnTermsActionListener?) {
        Log.d(TAG, "show() called")

        if (context is Activity) {
            context.runOnUiThread {
                Log.d(TAG, "Running on UI thread")
                showDialogInternal(context, pdfUrl, version, message, listener)
            }
        } else {
            Log.d(TAG, "Not an Activity context")
            showDialogInternal(context, pdfUrl, version, message, listener)
        }
    }

    private fun showDialogInternal(context: Context, pdfUrl: String?, version: String, message: String?, listener: OnTermsActionListener?) {
        Log.d(TAG, "showDialogInternal started")

        if (context !is Activity) {
            Log.e(TAG, "Context is not an Activity")
            return
        }

        val activity = context
        if (activity.isFinishing || activity.isDestroyed) {
            Log.e(TAG, "Activity is finishing or destroyed")
            return
        }

        pdfLoader?.let {
            it.cancelLoading()
            it.cancel(true)
            pdfLoader = null
        }

        dismiss()

        try {
            dialog = Dialog(activity)
            dialog?.let {
                it.setCancelable(false)
                it.setCanceledOnTouchOutside(false)
                it.requestWindowFeature(Window.FEATURE_NO_TITLE)

                val inflater = LayoutInflater.from(activity)
                val view = inflater.inflate(R.layout.terms_condition_layout, null)
                it.setContentView(view)

                it.window?.let { window ->
                    window.setLayout(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT
                    )
                    window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                }

                val ivClose = view.findViewById<ImageView>(R.id.iv_close)
                pdfView = view.findViewById(R.id.pdfView)
                progressBar = view.findViewById(R.id.progressBar)
                tvError = view.findViewById(R.id.tv_error)
                tvTerms = view.findViewById(R.id.tv_terms)
                cbAgree = view.findViewById(R.id.cb_agree)
                btnAccept = view.findViewById(R.id.btn_accept)
                btnDecline = view.findViewById(R.id.btn_decline)

                ivClose.setOnClickListener {
                    listener?.onDecline()
                    dismiss()
                }

                setupTermsText(activity)

                cbAgree?.setOnCheckedChangeListener { _, _ ->
                    updateAcceptButtonState()
                }

                btnAccept?.isEnabled = false
                btnAccept?.alpha = 0.5f

                btnAccept?.setOnClickListener {
                    if (cbAgree?.isChecked == true) {
                        Log.d(TAG, "Accept button clicked")
                        listener?.onAccept(version)
                    } else {
                        Toast.makeText(context, "Please agree to the terms to continue", Toast.LENGTH_SHORT).show()
                    }
                }

                btnDecline?.setOnClickListener {
                    Log.d(TAG, "Decline button clicked")
                    listener?.onDecline()
                    dismiss()
                }

                it.show()
                Log.d(TAG, "Dialog shown successfully")

                if (!pdfUrl.isNullOrEmpty()) {
                    Log.d(TAG, "Loading PDF from URL: $pdfUrl")
                    progressBar?.visibility = View.VISIBLE
                    pdfView?.visibility = View.INVISIBLE
                    tvError?.visibility = View.GONE

                    pdfLoader = object : RetrievePDFfromUrl(pdfView, progressBar) {
                        override fun onPostExecute(inputStream: InputStream?) {
                            if (inputStream == null) {
                                activity.runOnUiThread { showError("Failed to load PDF") }
                                return
                            }

                            pdfView?.fromStream(inputStream)
                                ?.onLoad { totalPages ->
                                    activity.runOnUiThread {
                                        Log.d(TAG, "PDF loaded, total pages: $totalPages")
                                        progressBar?.visibility = View.GONE
                                        pdfView?.visibility = View.VISIBLE
                                        updateAcceptButtonState()
                                    }
                                }
                                ?.onError { error ->
                                    activity.runOnUiThread {
                                        Log.e(TAG, "PDF error: ${error.message}")
                                        showError("Failed to load PDF")
                                    }
                                }
                                ?.load()
                        }
                    }
                    pdfLoader?.execute(pdfUrl)
                } else {
                    Log.e(TAG, "PDF URL is null or empty")
                    showError("No PDF available")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing dialog: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun setupTermsText(activity: Activity) {
        val fullText = "By clicking this, you agree to the Privacy Policy and Cookies Policy, along with the Terms & Conditions."
        val spannable = SpannableString(fullText)

        // Privacy Policy link
        val privacyStart = fullText.indexOf("Privacy Policy")
        val privacyEnd = privacyStart + "Privacy Policy".length
        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                showPdfDialog(activity, "Privacy Policy", "https://digicoffer.com/digicoffer_privacy.pdf")
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.color = activity.resources.getColor(R.color.blue)
                ds.isUnderlineText = true
            }
        }, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Cookies Policy link
        val cookiesStart = fullText.indexOf("Cookies Policy")
        val cookiesEnd = cookiesStart + "Cookies Policy".length
        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                showPdfDialog(activity, "Cookies Policy", "https://digicoffer.com/digicoffer_cookie.pdf")
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.color = activity.resources.getColor(R.color.blue)
                ds.isUnderlineText = true
            }
        }, cookiesStart, cookiesEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        tvTerms?.text = spannable
        tvTerms?.movementMethod = LinkMovementMethod.getInstance()
        tvTerms?.highlightColor = Color.TRANSPARENT
    }

    private fun showPdfDialog(activity: Activity, title: String, pdfUrl: String?) {
        Log.d(TAG, "showPdfDialog called - Title: $title, URL: $pdfUrl")

        val innerDialog = Dialog(activity)
        innerDialog.setCancelable(false)
        innerDialog.setCanceledOnTouchOutside(false)
        innerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val inflater = LayoutInflater.from(activity)
        val view = inflater.inflate(R.layout.dialog_webview, null)
        innerDialog.setContentView(view)

        innerDialog.window?.let { window ->
            window.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        val innerPdfView = view.findViewById<PDFView>(R.id.pdfView)
        val innerProgressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val innerTvError = view.findViewById<TextView>(R.id.tv_error)
        val ivClose = view.findViewById<ImageView>(R.id.iv_close)
        val tvTitle = view.findViewById<TextView>(R.id.tv_title)

        tvTitle.text = title
        Log.d(TAG, "Title set to: ${tvTitle.text}")

        innerProgressBar.visibility = View.VISIBLE
        innerPdfView.visibility = View.GONE
        innerTvError.visibility = View.GONE

        innerDialog.show()
        Log.d(TAG, "Dialog shown successfully")

        try {
            if (!pdfUrl.isNullOrEmpty()) {
                Log.d(TAG, "Loading PDF from URL: $pdfUrl")
                innerProgressBar.visibility = View.VISIBLE
                innerPdfView.visibility = View.INVISIBLE
                innerTvError.visibility = View.GONE

                pdfLoader = object : RetrievePDFfromUrl(innerPdfView, innerProgressBar) {
                    override fun onPostExecute(inputStream: InputStream?) {
                        if (inputStream == null) {
                            activity.runOnUiThread {
                                innerProgressBar.visibility = View.GONE
                                innerPdfView.visibility = View.GONE
                                innerTvError.visibility = View.VISIBLE
                                innerTvError.text = "Failed to load PDF"
                            }
                            return
                        }

                        innerPdfView.fromStream(inputStream)
                            .onLoad { totalPages ->
                                activity.runOnUiThread {
                                    Log.d(TAG, "PDF loaded, total pages: $totalPages")
                                    innerProgressBar.visibility = View.GONE
                                    innerPdfView.visibility = View.VISIBLE
                                }
                            }
                            .onError { error ->
                                activity.runOnUiThread {
                                    Log.e(TAG, "PDF error: ${error.message}")
                                    innerProgressBar.visibility = View.GONE
                                    innerPdfView.visibility = View.GONE
                                    innerTvError.visibility = View.VISIBLE
                                    innerTvError.text = "Failed to load PDF"
                                }
                            }
                            .load()
                    }
                }
                pdfLoader?.execute(pdfUrl)
            } else {
                Log.e(TAG, "PDF URL is null or empty")
                innerProgressBar.visibility = View.GONE
                innerPdfView.visibility = View.GONE
                innerTvError.visibility = View.VISIBLE
                innerTvError.text = "No PDF available"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing dialog: ${e.message}")
            e.printStackTrace()
        }

        ivClose.setOnClickListener {
            pdfLoader?.let {
                it.cancelLoading()
                it.cancel(true)
            }
            innerDialog.dismiss()
        }
    }

    private fun updateAcceptButtonState() {
        btnAccept?.let {
            val isChecked = cbAgree?.isChecked == true
            val isPdfVisible = pdfView?.visibility == View.VISIBLE
            val enable = isChecked && isPdfVisible
            it.isEnabled = enable
            it.alpha = if (enable) 1.0f else 0.5f
            Log.d(TAG, "Accept button enabled: $enable")
        }
    }

    private fun enableAcceptButton(enable: Boolean) {
        btnAccept?.let {
            val isChecked = cbAgree?.isChecked == true
            it.isEnabled = enable && isChecked
            it.alpha = if (enable && isChecked) 1.0f else 0.5f
        }
    }

    private fun showError(errorMessage: String) {
        progressBar?.visibility = View.GONE
        pdfView?.visibility = View.GONE
        tvError?.let {
            it.visibility = View.VISIBLE
            it.text = errorMessage
        }
        enableAcceptButton(false)
    }

    fun dismiss() {
        Log.d(TAG, "dismiss() called")
        pdfLoader?.let {
            it.cancelLoading()
            it.cancel(true)
            pdfLoader = null
        }

        dialog?.let {
            if (it.isShowing) {
                it.dismiss()
                Log.d(TAG, "Dialog dismissed")
            }
            dialog = null
        }
    }

    fun isShowing(): Boolean {
        return dialog?.isShowing == true
    }

    companion object {
        private const val TAG = "TermsDialog"
        private var instance: TermsAndConditionsDialog? = null

        @JvmStatic
        @Synchronized
        fun getInstance(): TermsAndConditionsDialog {
            if (instance == null) {
                instance = TermsAndConditionsDialog()
            }
            return instance!!
        }
    }
}
