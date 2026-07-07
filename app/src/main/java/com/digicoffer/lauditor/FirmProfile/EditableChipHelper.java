package com.digicoffer.lauditor.FirmProfile;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.digicoffer.lauditor.R;

/**
 * Utility for adding editable chips (text + edit icon + remove icon)
 * to any ViewGroup (FlexboxLayout, LinearLayout, ChipGroup wrapper, etc.)
 *
 * Usage:
 *   EditableChipHelper.addChip(context, container, "Some text",
 *       newText -> { /* on edit save *\/ },
 *       () -> { /* optional on-remove callback *\/ });
 */
public class EditableChipHelper {

    public interface OnEditSaved {
        void onSaved(String newText);
    }

    public interface OnRemoved {
        void onRemoved();
    }
    public static View addChip(
            Context context,
            ViewGroup parent,
            String text,
            boolean showEditIcon,
            OnEditSaved onEdit,
            OnRemoved onRemove
    ) {
        View chip = LayoutInflater.from(context)
                .inflate(R.layout.chip_editable_item, parent, false);
        TextView tvText   = chip.findViewById(R.id.tv_chip_text);
        ImageView ivEdit  = chip.findViewById(R.id.iv_chip_edit);
        ImageView ivClose = chip.findViewById(R.id.iv_chip_remove);

        if (ivEdit != null) {
            ivEdit.setVisibility(showEditIcon ? View.VISIBLE : View.GONE);
        }
        tvText.setText(text);

        // ── Edit: populate the input field, remove this chip ─────────────
        ivEdit.setOnClickListener(v -> {
            String currentText = tvText.getText().toString();
            parent.removeView(chip);          // remove old chip
            if (onEdit != null) onEdit.onSaved(currentText);  // caller populates etCases
        });

        // ── Remove ────────────────────────────────────────────────────────
        ivClose.setOnClickListener(v -> {
            parent.removeView(chip);
            if (onRemove != null) onRemove.onRemoved();
        });

        parent.addView(chip);
        return chip;
    }
//    public static View addChip(
//            Context context,
//            ViewGroup parent,
//            String text,
//            boolean showEditIcon,
//            OnEditSaved onEdit,
//            OnRemoved onRemove
//    ) {
//        View chip = LayoutInflater.from(context)
//                .inflate(R.layout.chip_editable_item, parent, false);
//        TextView tvText   = chip.findViewById(R.id.tv_chip_text);
//        ImageView ivEdit  = chip.findViewById(R.id.iv_chip_edit);
//        ImageView ivClose = chip.findViewById(R.id.iv_chip_remove);
//        if (ivEdit != null) {
//            ivEdit.setVisibility(showEditIcon ? View.VISIBLE : View.GONE);
//        }
//        tvText.setText(text);
//
//        // ── Edit ──────────────────────────────────────────────────────────
//        ivEdit.setOnClickListener(v -> showEditDialog(context, tvText, onEdit));
//
//        // ── Remove ────────────────────────────────────────────────────────
//        ivClose.setOnClickListener(v -> {
//            parent.removeView(chip);
//            if (onRemove != null) onRemove.onRemoved();
//        });
//
//        parent.addView(chip);
//        return chip;
//    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private static void showEditDialog(
            Context context,
            TextView tvChipText,
            OnEditSaved onEdit
    ) {
        EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setText(tvChipText.getText());
        input.selectAll();

        int pad = (int) (context.getResources().getDisplayMetrics().density * 16);
        input.setPadding(pad, pad, pad, pad);

        new AlertDialog.Builder(context)
                .setTitle("Edit")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newText = input.getText().toString().trim();
                    if (!newText.isEmpty()) {
                        tvChipText.setText(newText);   // update label
                        if (onEdit != null) onEdit.onSaved(newText);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
