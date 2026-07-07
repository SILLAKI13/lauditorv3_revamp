package com.digicoffer.lauditor.CommonFiles.FileSelection;

import static androidx.constraintlayout.widget.Constraints.TAG;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BottomSheetUploadFile extends DialogFragment implements View.OnClickListener {
    TextView tv_choose_file, tv_photo_lib, tv_cancel, tv_document_lib, tv_camera;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int CAMERA_REQUEST = 123;
    private static final int PICK_FILE_REQUEST_CODE = 1888;
    private static final int PICK_DOCFILE_REQUEST_CODE = 1999;
    private static final int PICKFILE_RESULT_CODE = 124;
    File file;
    View clDocument;
    boolean showDocSelection = true;
    OnPhotoSelectedListner onPhotoSelectedListner;

    public BottomSheetUploadFile(View clDocument) {
        this.clDocument = clDocument;
    }

    public BottomSheetUploadFile(View clDocument, boolean showDocSelection) {
        this.clDocument = clDocument;
        this.showDocSelection = showDocSelection;
    }

    public BottomSheetUploadFile() {
    }

    public interface OnPhotoSelectedListner {
        void getImagepath(File imagepath, Uri uri) throws IOException;

        void getImageBitmap(Bitmap bitmap);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable
    ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.upload_file_options, container, false);
        tv_camera = v.findViewById(R.id.tv_camera);
        tv_choose_file = v.findViewById(R.id.tv_choose_file);
        tv_choose_file.setText(R.string.please_choose);
        tv_choose_file.setTypeface(Typeface.DEFAULT_BOLD);
        tv_choose_file.setTextSize(DynamicUtils.twenty);
        tv_cancel = v.findViewById(R.id.tv_cancel);
        tv_cancel.setText(R.string.cancel);
        tv_cancel.setTypeface(Typeface.DEFAULT_BOLD);
        tv_cancel.setTextSize(DynamicUtils.twenty);
        tv_cancel.setTextColor(getContext().getColor(R.color.blue));
        tv_photo_lib = v.findViewById(R.id.tv_photo_lib);
        tv_photo_lib.setTypeface(Typeface.DEFAULT);
        tv_photo_lib.setText(R.string.photo_library);
        tv_photo_lib.setTextColor(getContext().getColor(R.color.blue));
        tv_document_lib = v.findViewById(R.id.tv_document_lib);
        tv_document_lib.setText(R.string.documents);
        tv_document_lib.setTypeface(Typeface.DEFAULT);
        tv_document_lib.setTextColor(getContext().getColor(R.color.blue));
        tv_camera.setOnClickListener(this);
        tv_cancel.setOnClickListener(this);
        tv_photo_lib.setOnClickListener(this);
        tv_document_lib.setOnClickListener(this);
        if (showDocSelection) {
            tv_document_lib.setVisibility(View.VISIBLE);
        } else {
            tv_document_lib.setVisibility(View.GONE);
        }
        getDialog().setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                clDocument.setAlpha(1.0f);
//                getDialog().dismiss();
            }
        });
        return v;
    }


    //    @Override
//    public void onClick(View v) {
//        switch (v.getId()){
//            case R.id.tv_photo_lib:
////                Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT,
////                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
////                    i.setType("*/*");
//                mGetContent.launch("*/*");
////                    startActivityForResult(i, PICK_FILE_REQUEST_CODE);
//                break;
//
//            case R.id.bt_camera:
//                if (ContextCompat.checkSelfPermission(getContext(),
//                        Manifest.permission.CAMERA)
//                        != PackageManager.PERMISSION_GRANTED) {
//                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
//                }
//                else
//                {
//                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
//                }
//                break;
//            case R.id.tv_document_lib:
////                Intent intent = new Intent();
////                intent.setType("*/*");
////                intent.setAction(Intent.ACTION_PICK);
////                startActivityForResult(Intent.createChooser(intent, "Select"), 200);
//
//                Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
//                chooseFile.setType("*/*");
//                chooseFile = Intent.createChooser(chooseFile, "Choose a file");
////                startActivity(chooseFile);
//                startActivityForResult(chooseFile, 200);

    /// /                Intent intent = new Intent();
    /// /                intent.setType("*/*");
    /// /                intent.setAction(Intent.ACTION_GET_CONTENT);
    /// /                startActivityForResult(Intent.createChooser(intent, "Choose File to Upload"), 200);
//                break;
//        }
//
//    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_photo_lib:
                if (Constants.isDocEditor) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    pickSingleImageLauncher.launch(intent);
                } else {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    pickMultipleImageLauncher.launch(intent);
                }
                break;

            case R.id.tv_camera:
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                } else {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                }
                break;

            case R.id.tv_document_lib:
                // Navigate to the document section using ACTION_OPEN_DOCUMENT
                mGetMultipleContent.launch(new String[]{
                        // 📄 Documents
                        "application/pdf",
                        "application/msword",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        "application/vnd.ms-powerpoint",
                        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                        "application/vnd.ms-excel",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        "application/rtf",
                        "text/rtf",

                        // CSV variations (important for Samsung)
                        "text/csv",
                        "application/csv",
                        "text/comma-separated-values",
                        "text/plain",

                        // 🖼️ Images
                        "image/png",
                        "image/jpeg",          // covers .jpg and .jpeg
                        "image/webp",
                        "image/heif",
                        "image/heic",
                        "image/bmp",
                        "image/svg+xml",
                        "image/tiff",
                        "image/vnd.microsoft.icon"   // correct icon type
                });

                break;

            case R.id.tv_cancel:
                dismiss();
                clDocument.setAlpha(1.0f);
                break;
        }
    }

    ActivityResultLauncher<Intent> pickSingleImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {

                    Uri uri = result.getData().getData();
                    if (uri != null) handleImageUri(uri);

                    getDialog().dismiss();
                }
            });

    ActivityResultLauncher<String[]> mGetMultipleContent =
            registerForActivityResult(new ActivityResultContracts.OpenMultipleDocuments(),
                    uris -> {
                        if (uris != null && !uris.isEmpty()) {
                            for (Uri uri : uris) {
                                String fileName = getFileNameFromUri(getContext(), uri);
                                if (!isFileAllowed(fileName)) {
                                    AndroidUtils.showToast("File type not allowed: " + fileName, getContext());
                                    continue;
                                }
                                File file = new File(uri.getPath());
                                try {
                                    onPhotoSelectedListner.getImagepath(file, uri);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            getDialog().dismiss();
                        }
                    });

    ActivityResultLauncher<Intent> pickMultipleImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {

                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {

                    Intent data = result.getData();
                    ClipData clipData = data.getClipData();

                    // MULTIPLE
                    if (clipData != null) {
                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            Uri uri = clipData.getItemAt(i).getUri();
                            handleImageUri(uri);
                        }
                    } else {
                        // SINGLE fallback (some devices)
                        Uri uri = data.getData();
                        if (uri != null) handleImageUri(uri);
                    }

                    getDialog().dismiss();
                }
            });

    private void handleImageUri(Uri uri) {
        if (uri == null) return;

        String fileName = getFileNameFromUri(getContext(), uri);
        if (!isFileAllowed(fileName)) {
            AndroidUtils.showToast("File type not allowed: " + fileName, getContext());
            return;
        }

        File file = uriToFile(getContext(), uri);

        try {
            onPhotoSelectedListner.getImagepath(file, uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    ActivityResultLauncher<String[]> mGetSingleDocument =
//            registerForActivityResult(new ActivityResultContracts.OpenDocument(),
//                    uri -> {
//                        if (uri != null) {
//                            String fileName = getFileNameFromUri(getContext(), uri);
//                            if (!isFileAllowed(fileName)) {
//                                AndroidUtils.showToast("File type not allowed: " + fileName, getContext());
//                                return;
//                            }
//                            File file = new File(uri.getPath());
//                            try {
//                                onPhotoSelectedListner.getImagepath(file, uri);
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                            getDialog().dismiss();
//                        }
//                    });


//    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
//            new ActivityResultCallback<Uri>() {
//                @Override
//                public void onActivityResult(Uri uri) {
////                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
////                    Cursor cursor = getContext().getContentResolver().query(uri,
////                            filePathColumn, null, null, null);
////                    cursor.moveToFirst();
////                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
////                    String picturePath = cursor.getString(columnIndex);
////                    cursor.close();
////                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
////                    Cursor cursor = getContext().getContentResolver().query(uri,null,
////                             null, null, null);
////                    cursor.moveToFirst();
////                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
////                    String picturePath = cursor.getString(columnIndex);
////                    cursor.close();
//
//
    /// /                    String fileName = "";
    /// /                    String scheme = uri.getScheme();
    /// /                    if (scheme.equals("file")) {
    /// /                        fileName = uri.getLastPathSegment();
    /// /                    }
    /// /                    else if (scheme.equals("content")) {
    /// /                        String[] proj = { MediaStore.Images.Media.TITLE };
    /// /                        Cursor cursor = getContext().getContentResolver().query(uri, proj, null, null, null);
    /// /                        if (cursor != null && cursor.getCount() != 0) {
    /// /                            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE);
    /// /                            cursor.moveToFirst();
    /// /                            fileName = cursor.getString(columnIndex);
    /// /                        }
    /// /                        if (cursor != null) {
    /// /                            cursor.close();
    /// /                        }
    /// /                    }
    /// /
    /// /
//                    if (uri != null) {
//                        File file = new File(uri.getPath());
//                        try {
//                            onPhotoSelectedListner.getImagepath(file, uri);
//                        } catch (IOException e) {
//                            e.fillInStackTrace();
//                        }
//                        getDialog().dismiss();
//                        // Handle the returned Uri
//                    }
//                }
//            });
    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    String fileName = getFileNameFromUri(getContext(), uri);
                    if (!isFileAllowed(fileName)) {
                        AndroidUtils.showToast("File type not allowed: " + fileName, getContext());
                        return;
                    }
                    File file = new File(uri.getPath());
                    try {
                        onPhotoSelectedListner.getImagepath(file, uri);
                    } catch (IOException e) {
                        e.fillInStackTrace();
                    }
                    getDialog().dismiss();
                }
            });


    private boolean isFileAllowed(String fileName) {
        if (fileName == null) return false;
        fileName = fileName.toLowerCase();

        // Blocked formats
        return !(fileName.endsWith(".gif") ||
                fileName.endsWith(".mp4") ||
                fileName.endsWith(".mp3"));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
//            Uri  selectedimageURI = data.getData();
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContext().getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
//            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(column_index);
            cursor.close();
            File file = new File(picturePath);
            try {
                onPhotoSelectedListner.getImagepath(file, selectedImage);
            } catch (IOException e) {
                e.fillInStackTrace();
            }
            getDialog().dismiss();
        } else if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            onPhotoSelectedListner.getImageBitmap(bitmap);
            getDialog().dismiss();
        } else if (requestCode == 200) {
            getDialog().dismiss();
            if (data == null) {
                return;
            }

//            Uri uri = data.getData();
//            String paths = FileUtils.getPath(getContext(), uri);
//            file = new File(paths);
//            onPhotoSelectedListner.getImagepath(file);
//            Log.d("File Path : ", "" + paths);
//            if (paths != null) {
//                all_file_name.setText("" + new File(paths).getName());
//            }
//            all_file_path = paths;
            try {
                Uri imageuri = data.getData();
                if (imageuri != null) {
                    try {
                        String path = FileUtils.getPath(getContext(), data.getData());
                        file = new File(path);
                        onPhotoSelectedListner.getImagepath(file, imageuri);
                    } catch (Exception e) {
                        AndroidUtils.showToast("File Access error: Please check the file name without any spaces.", getContext());

                    }
                } else {
                    AndroidUtils.showToast("File Access error: Please check the file name without any spaces.", getContext());

                }
            } catch (Exception e) {
                AndroidUtils.showToast("File Access error:Please check your file ", getContext());
            }
            getDialog().dismiss();
//            Uri path = data.getData();
//                    try {
//                        InputStream inputStream = this.getContext().getContentResolver().openInputStream(path);
//                        byte[] pdfByte = new byte[inputStream.available()];
//                        inputStream.read(pdfByte);
//                        try (FileOutputStream fos = new FileOutputStream("pathname")) {
//                            fos.write(pdfByte);
//                        }
//                    }  catch (IOException e) {
//                        e.fillInStackTrace();
//                    }
        } else {
            getDialog().dismiss();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        try {
//            BottomSheetUploadFile f = (BottomSheetUploadFile) getChildFragmentManager().findFragmentById(R.id.id_framelayoutt);
//            MainActivity activity;
//        if ((context instanceof OnPhotoSelectedListner)) {
//            activity = (MainActivity) context;
            onPhotoSelectedListner = (OnPhotoSelectedListner) getTargetFragment();
//        }
        } catch (ClassCastException e) {
            Log.e(TAG, "onAttach: ClasscatchException" + e.getMessage());
            e.fillInStackTrace();
        }
        super.onAttach(context);
    }

    public static File uriToFile(Context context, Uri uri) {
        if (uri == null) return null;

        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            // Create a temporary file in cache directory
            File file = File.createTempFile("upload_", ".jpg", context.getCacheDir());

            outputStream = new FileOutputStream(file);

            // Copy the input stream to the output stream
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getFileNameFromUri(Context context, Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = context.getContentResolver()
                    .query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

}

