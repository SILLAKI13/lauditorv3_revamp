package com.digicoffer.lauditor.CommonFiles.FileSelection

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.digicoffer.lauditor.R

class Filecosen(private val activity: Activity, private val callback: FileChooserCallback) {

    private var dialog: Dialog? = null
    private val context: Context = activity

    interface FileChooserCallback {
        fun openCamera()
        fun openGallery()
    }

    fun show() {
        val currentDialog = Dialog(activity)
        dialog = currentDialog
        currentDialog.setContentView(R.layout.dialog_file_chosen)
        currentDialog.setCancelable(true)

        currentDialog.window?.let { window ->
            window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window.setGravity(Gravity.BOTTOM)
        }

        val llCamera = currentDialog.findViewById<LinearLayout>(R.id.llCamera)
        val llGallery = currentDialog.findViewById<LinearLayout>(R.id.llGallery)
        val tvCancel = currentDialog.findViewById<TextView>(R.id.tvCancel)

        llCamera.setOnClickListener {
            callback.openCamera()
            currentDialog.dismiss()
        }

        llGallery.setOnClickListener {
            callback.openGallery()
            currentDialog.dismiss()
        }

        tvCancel.setOnClickListener { currentDialog.dismiss() }

        currentDialog.show()
    }
}
