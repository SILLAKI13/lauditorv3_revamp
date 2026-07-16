package com.digicoffer.lauditor.FirmProfile

import android.app.AlertDialog
import android.content.Context
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.digicoffer.lauditor.R

class EditableChipHelper {
    fun interface OnEditSaved {
        fun onSaved(newText: String)
    }

    fun interface OnRemoved {
        fun onRemoved()
    }

    companion object {
        @JvmStatic
        fun addChip(
            context: Context,
            parent: ViewGroup,
            text: String,
            showEditIcon: Boolean,
            onEdit: OnEditSaved?,
            onRemove: OnRemoved?
        ): View {
            val chip = LayoutInflater.from(context)
                .inflate(R.layout.chip_editable_item, parent, false)
            val tvText = chip.findViewById<TextView>(R.id.tv_chip_text)
            val ivEdit = chip.findViewById<ImageView>(R.id.iv_chip_edit)
            val ivClose = chip.findViewById<ImageView>(R.id.iv_chip_remove)

            ivEdit?.visibility = if (showEditIcon) View.VISIBLE else View.GONE
            tvText?.text = text

            ivEdit?.setOnClickListener {
                val currentText = tvText?.text?.toString() ?: ""
                parent.removeView(chip)
                onEdit?.onSaved(currentText)
            }

            ivClose?.setOnClickListener {
                parent.removeView(chip)
                onRemove?.onRemoved()
            }

            parent.addView(chip)
            return chip
        }

        @JvmStatic
        private fun showEditDialog(
            context: Context,
            tvChipText: TextView,
            onEdit: OnEditSaved?
        ) {
            val input = EditText(context)
            input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            input.setText(tvChipText.text)
            input.selectAll()

            val pad = (context.resources.displayMetrics.density * 16).toInt()
            input.setPadding(pad, pad, pad, pad)

            AlertDialog.Builder(context)
                .setTitle("Edit")
                .setView(input)
                .setPositiveButton("Save") { _, _ ->
                    val newText = input.text.toString().trim()
                    if (newText.isNotEmpty()) {
                        tvChipText.text = newText
                        onEdit?.onSaved(newText)
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}
