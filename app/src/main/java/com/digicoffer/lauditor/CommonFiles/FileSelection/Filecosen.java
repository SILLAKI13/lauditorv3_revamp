package com.digicoffer.lauditor.CommonFiles.FileSelection;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.digicoffer.lauditor.R;

public class Filecosen {

    private final Activity activity;
    private Dialog dialog;
    private Context context;

    public interface FileChooserCallback {
        void openCamera();
        void openGallery();
    }

    private FileChooserCallback callback;

    public Filecosen(Activity activity, FileChooserCallback callback) {
        this.activity = activity;
        this.context = activity;
        this.callback = callback;
    }

    public void show() {
        dialog = new Dialog(activity);
        dialog.setContentView(R.layout.dialog_file_chosen);
        dialog.setCancelable(true);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setGravity(Gravity.BOTTOM);
        }

        LinearLayout llCamera = dialog.findViewById(R.id.llCamera);
        LinearLayout llGallery = dialog.findViewById(R.id.llGallery);
        TextView tvCancel = dialog.findViewById(R.id.tvCancel);

        llCamera.setOnClickListener(v -> {
            callback.openCamera();
            dialog.dismiss();
        });

        llGallery.setOnClickListener(v -> {
            callback.openGallery();
            dialog.dismiss();
        });

        tvCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
}
