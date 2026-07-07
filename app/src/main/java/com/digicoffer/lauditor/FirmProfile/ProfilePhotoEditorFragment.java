package com.digicoffer.lauditor.FirmProfile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.digicoffer.lauditor.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * ProfilePhotoEditorFragment
 *
 * A Fragment that provides a circular crop editor with:
 *  - Back button  → dismisses this fragment and re-shows the camera/gallery popup
 *  - "Choose Photo" button → re-shows the camera/gallery popup (via Callback)
 *  - Zoom In / Zoom Out buttons
 *  - SeekBar for zoom
 *  - Rotate Left / Rotate Right buttons
 *  - Cancel → pops the fragment (no result)
 *  - Save   → crops the image, writes to cache, calls Callback.onCropSaved(path)
 *
 * How to use (from PracticePartnerView or any host Fragment/Activity):
 *
 *   ProfilePhotoEditorFragment editor = ProfilePhotoEditorFragment.newInstance(imageUri);
 *   editor.setCallback(new ProfilePhotoEditorFragment.Callback() {
 *       \@Override
 *       public void onCropSaved(String croppedFilePath) {
 *           // handle the cropped file, e.g. call adapter.handleCroppedResult(path)
 *       }
 *       \@Override
 *       public void onChoosePhotoRequested() {
 *           // re-show camera/gallery popup
 *           showFileChooserPopup();
 *       }
 *   });
 *   parentFragmentManager.beginTransaction()
 *       .replace(R.id.fragment_container, editor)
 *       .addToBackStack("photo_editor")
 *       .commit();
 */
public class ProfilePhotoEditorFragment extends Fragment {

    // ── Argument key ───────────────────────────────────────────────────────
    private static final String ARG_URI = "imageUri";

    // ── Views ──────────────────────────────────────────────────────────────
    private CropImageView cropView;
    private SeekBar       seekBar;

    // ── Callback ───────────────────────────────────────────────────────────
    public interface Callback {
        /** Called when the user taps Save and the crop JPEG is written to disk. */
        void onCropSaved(String croppedFilePath);

        /**
         * Called when the user taps the back arrow or "Choose Photo" button.
         * The host should re-show the camera/gallery popup here.
         */
        void onChoosePhotoRequested();
    }

    private Callback callback;

    // ── Factory ────────────────────────────────────────────────────────────
    public static ProfilePhotoEditorFragment newInstance(Uri imageUri) {
        ProfilePhotoEditorFragment f = new ProfilePhotoEditorFragment();
        Bundle args = new Bundle();
        args.putString(ARG_URI, imageUri.toString());
        f.setArguments(args);
        return f;
    }
    // In ProfilePhotoEditorFragment.java
    public interface ProfileEditorListener {
        void onEditorClosed();
    }

    private ProfileEditorListener editorListener;

    public void setEditorListener(ProfileEditorListener listener) {
        this.editorListener = listener;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (editorListener != null) {
            editorListener.onEditorClosed();
        }
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    // ── Lifecycle ──────────────────────────────────────────────────────────
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_profile_photo_editor, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ── View references ────────────────────────────────────────────────
        cropView               = view.findViewById(R.id.cropImageView);
        seekBar                = view.findViewById(R.id.zoomSeekBar);
        TextView tvZoomIn      = view.findViewById(R.id.tvZoomIn);
        TextView tvZoomOut     = view.findViewById(R.id.tvZoomOut);
        ImageView rotate_left = view.findViewById(R.id.rotate_left);
        ImageView rotate_right =view.findViewById(R.id.rotate_right);

        TextView tvRotLeft     = view.findViewById(R.id.tvRotateLeft);
        TextView tvRotRight    = view.findViewById(R.id.tvRotateRight);
        TextView tvCancel      = view.findViewById(R.id.tvCancel);
        TextView tvSave        = view.findViewById(R.id.tvSave);
        TextView tvChoosePhoto = view.findViewById(R.id.tvChoosePhoto);

        // Back arrow in the custom title bar
        View ivBack = view.findViewById(R.id.ivBack);

        // ── Load bitmap ────────────────────────────────────────────────────
        String uriString = getArguments() != null ? getArguments().getString(ARG_URI) : null;
        if (uriString == null) {
            Toast.makeText(requireContext(), "No image provided", Toast.LENGTH_SHORT).show();
            popFragment();
            return;
        }

        try {
            Uri         uri = Uri.parse(uriString);
            InputStream is  = requireContext().getContentResolver().openInputStream(uri);
            Bitmap      bm  = BitmapFactory.decodeStream(is);
            if (is != null) is.close();

            if (bm == null) {
                Toast.makeText(requireContext(), "Cannot decode image", Toast.LENGTH_SHORT).show();
                popFragment();
                return;
            }

            cropView.setImageBitmap(bm);

        } catch (Exception e) {
            Toast.makeText(requireContext(),
                    "Cannot open image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            popFragment();
            return;
        }

        // ── SeekBar ────────────────────────────────────────────────────────
        seekBar.setMax(300);
        seekBar.setProgress(0);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (fromUser) cropView.setZoomFromSeek(progress, sb.getMax());
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb)  {}
        });

        // Sync seekBar when user pinch-zooms on canvas
        cropView.setOnZoomChangedListener(zoom -> {
            float min      = cropView.getMinScale();
            float max      = cropView.getMaxScale();
            int   progress = (int) ((zoom - min) / (max - min) * seekBar.getMax());
            seekBar.setProgress(Math.max(0, Math.min(seekBar.getMax(), progress)));
        });

        // ── Zoom In ────────────────────────────────────────────────────────
        tvZoomIn.setOnClickListener(v -> {
            int step = seekBar.getMax() / 10;
            int next = Math.min(seekBar.getMax(), seekBar.getProgress() + step);
            seekBar.setProgress(next);
            cropView.setZoomFromSeek(next, seekBar.getMax());
        });

        // ── Zoom Out ───────────────────────────────────────────────────────
        tvZoomOut.setOnClickListener(v -> {
            int step = seekBar.getMax() / 10;
            int next = Math.max(0, seekBar.getProgress() - step);
            seekBar.setProgress(next);
            cropView.setZoomFromSeek(next, seekBar.getMax());
        });

        // ── Rotate Left ────────────────────────────────────────────────────
        tvRotLeft.setOnClickListener(v -> {
            cropView.rotate(-90f);
            seekBar.setProgress(0);
        });
        rotate_left.setOnClickListener(v -> {
            cropView.rotate(-90f);
            seekBar.setProgress(0);
        });



        tvRotRight.setOnClickListener(v -> {
            cropView.rotate(90f);
            seekBar.setProgress(0);
        });
        rotate_right.setOnClickListener(v -> {
            cropView.rotate(90f);
            seekBar.setProgress(0);
        });

        if (ivBack != null) {
            ivBack.setOnClickListener(v -> {
                popFragment();
                if (callback != null) callback.onChoosePhotoRequested();
            });
        }

        // ── "Choose Photo" button: pop fragment AND re-show popup ──────────
        tvChoosePhoto.setOnClickListener(v -> {
            popFragment();
            if (callback != null) callback.onChoosePhotoRequested();
        });

        // ── Cancel: just pop (no result, no popup) ─────────────────────────
        tvCancel.setOnClickListener(v -> popFragment());

        // ── Save ───────────────────────────────────────────────────────────
        tvSave.setOnClickListener(v -> saveCrop());
    }

    // ── saveCrop ───────────────────────────────────────────────────────────
    private void saveCrop() {
        Bitmap cropped = cropView.getCroppedBitmap(512);
        if (cropped == null) {
            Toast.makeText(requireContext(), "Error creating crop", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            File outFile = new File(
                    requireContext().getCacheDir(),
                    "profile_crop_" + System.currentTimeMillis() + ".jpg");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            cropped.compress(Bitmap.CompressFormat.JPEG, 90, baos);
            cropped.recycle();
            FileOutputStream fos = new FileOutputStream(outFile);
            fos.write(baos.toByteArray());
            fos.flush();
            fos.close();
            baos.close();

            String path = outFile.getAbsolutePath();

            // Notify host before popping so it can call handleCroppedResult()
            if (callback != null) callback.onCropSaved(path);

            popFragment();

        } catch (Exception e) {
            Toast.makeText(requireContext(),
                    "Save failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // ── Helper ─────────────────────────────────────────────────────────────
    private void popFragment() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
        }
    }
}