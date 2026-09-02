package com.digicoffer.lauditor.CommonFiles.FileSelection

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DynamicUtils
import com.digicoffer.lauditor.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class BottomSheetUploadFile : DialogFragment, View.OnClickListener {
    private var tv_choose_file: TextView? = null
    private var tv_photo_lib: TextView? = null
    private var tv_cancel: TextView? = null
    private var tv_document_lib: TextView? = null
    private var tv_camera: TextView? = null
    private var clDocument: View? = null
    private var showDocSelection = true
    private var onPhotoSelectedListner: OnPhotoSelectedListner? = null

    companion object {
        private const val TAG = "BottomSheetUploadFile"
        private const val MY_CAMERA_PERMISSION_CODE = 100
        private const val CAMERA_REQUEST = 123
        private const val PICK_FILE_REQUEST_CODE = 1888

        @JvmStatic
        @JvmOverloads
        fun uriToFile(context: Context, uri: Uri?, fileName: String? = null): File? {
            if (uri == null) return null
            var inputStream: InputStream? = null
            var outputStream: FileOutputStream? = null
            try {
                inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream == null) return null
                val name = fileName ?: "upload_${System.currentTimeMillis()}"
                val file = File(context.cacheDir, name)
                outputStream = FileOutputStream(file)
                val buffer = ByteArray(4096)
                var length = inputStream.read(buffer)
                while (length > 0) {
                    outputStream.write(buffer, 0, length)
                    length = inputStream.read(buffer)
                }
                return file
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            } finally {
                try {
                    inputStream?.close()
                    outputStream?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    constructor() : super()

    constructor(clDocument: View?) : this() {
        this.clDocument = clDocument
    }

    constructor(clDocument: View?, showDocSelection: Boolean) : this() {
        this.clDocument = clDocument
        this.showDocSelection = showDocSelection
    }

    interface OnPhotoSelectedListner {
        @Throws(IOException::class)
        fun getImagepath(imagepath: File?, uri: Uri?)
        fun getImageBitmap(bitmap: Bitmap?)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.upload_file_options, container, false)
        tv_camera = v.findViewById(R.id.tv_camera)
        tv_choose_file = v.findViewById(R.id.tv_choose_file)
        tv_choose_file?.text = getString(R.string.please_choose)
        tv_choose_file?.typeface = Typeface.DEFAULT_BOLD
        tv_choose_file?.textSize = DynamicUtils.twenty.toFloat()
        tv_cancel = v.findViewById(R.id.tv_cancel)
        tv_cancel?.text = getString(R.string.cancel)
        tv_cancel?.typeface = Typeface.DEFAULT_BOLD
        tv_cancel?.textSize = DynamicUtils.twenty.toFloat()
        context?.let { ctx ->
            tv_cancel?.setTextColor(ctx.getColor(R.color.blue))
        }
        tv_photo_lib = v.findViewById(R.id.tv_photo_lib)
        tv_photo_lib?.typeface = Typeface.DEFAULT
        tv_photo_lib?.text = getString(R.string.photo_library)
        context?.let { ctx ->
            tv_photo_lib?.setTextColor(ctx.getColor(R.color.blue))
        }
        tv_document_lib = v.findViewById(R.id.tv_document_lib)
        tv_document_lib?.text = getString(R.string.documents)
        tv_document_lib?.typeface = Typeface.DEFAULT
        context?.let { ctx ->
            tv_document_lib?.setTextColor(ctx.getColor(R.color.blue))
        }
        tv_camera?.setOnClickListener(this)
        tv_cancel?.setOnClickListener(this)
        tv_photo_lib?.setOnClickListener(this)
        tv_document_lib?.setOnClickListener(this)
        if (showDocSelection) {
            tv_document_lib?.visibility = View.VISIBLE
        } else {
            tv_document_lib?.visibility = View.GONE
        }
        dialog?.setOnDismissListener {
            clDocument?.alpha = 1.0f
        }
        return v
    }

    override fun onClick(v: View?) {
        val id = v?.id ?: return
        when (id) {
            R.id.tv_photo_lib -> {
                if (Constants.isDocEditor) {
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    intent.type = "image/*"
                    pickSingleImageLauncher.launch(intent)
                } else {
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    intent.type = "image/*"
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    pickMultipleImageLauncher.launch(intent)
                }
            }

            R.id.tv_camera -> {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(Manifest.permission.CAMERA), MY_CAMERA_PERMISSION_CODE)
                } else {
                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(cameraIntent, CAMERA_REQUEST)
                }
            }

            R.id.tv_document_lib -> {
                mGetMultipleContent.launch(
                    arrayOf(
                        "application/pdf",
                        "application/msword",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        "application/vnd.ms-powerpoint",
                        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                        "application/vnd.ms-excel",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        "application/rtf",
                        "text/rtf",
                        "text/csv",
                        "application/csv",
                        "text/comma-separated-values",
                        "text/plain",
                        "image/png",
                        "image/jpeg",
                        "image/webp",
                        "image/heif",
                        "image/heic",
                        "image/bmp",
                        "image/svg+xml",
                        "image/tiff",
                        "image/vnd.microsoft.icon"
                    )
                )
            }

            R.id.tv_cancel -> {
                dismiss()
                clDocument?.alpha = 1.0f
            }
        }
    }

    private val pickSingleImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val uri = result.data?.data
                if (uri != null) handleImageUri(uri)
                dialog?.dismiss()
            }
        }

    private val mGetMultipleContent =
        registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            if (!uris.isNullOrEmpty()) {
                for (uri in uris) {
                    val fileName = getFileNameFromUri(requireContext(), uri)
                    if (!isFileAllowed(fileName)) {
                        AndroidUtils.showToast("File type not allowed: $fileName", context)
                        continue
                    }
                    val file = uriToFile(requireContext(), uri, fileName)
                    if (file != null) {
                        try {
                            onPhotoSelectedListner?.getImagepath(file, uri)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
                dialog?.dismiss()
            }
        }

    private val pickMultipleImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val data = result.data
                val clipData = data?.clipData
                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        val uri = clipData.getItemAt(i).uri
                        handleImageUri(uri)
                    }
                } else {
                    val uri = data?.data
                    if (uri != null) handleImageUri(uri)
                }
                dialog?.dismiss()
            }
        }

    private fun handleImageUri(uri: Uri) {
        val fileName = getFileNameFromUri(requireContext(), uri)
        if (!isFileAllowed(fileName)) {
            AndroidUtils.showToast("File type not allowed: $fileName", context)
            return
        }
        val file = uriToFile(requireContext(), uri, fileName)
        if (file != null) {
            try {
                onPhotoSelectedListner?.getImagepath(file, uri)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private val mGetContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                val fileName = getFileNameFromUri(requireContext(), uri)
                if (!isFileAllowed(fileName)) {
                    AndroidUtils.showToast("File type not allowed: $fileName", context)
                    return@registerForActivityResult
                }
                val file = uriToFile(requireContext(), uri, fileName)
                if (file != null) {
                    try {
                        onPhotoSelectedListner?.getImagepath(file, uri)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                dialog?.dismiss()
            }
        }

    private fun isFileAllowed(fileName: String?): Boolean {
        if (fileName == null) return false
        val lowerName = fileName.lowercase()
        return !(lowerName.endsWith(".gif") ||
                lowerName.endsWith(".mp4") ||
                lowerName.endsWith(".mp3"))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImage = data.data ?: return
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = requireContext().contentResolver.query(
                selectedImage,
                filePathColumn, null, null, null
            )
            if (cursor != null) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                cursor.moveToFirst()
                val picturePath = cursor.getString(columnIndex)
                cursor.close()
                val file = File(picturePath)
                try {
                    onPhotoSelectedListner?.getImagepath(file, selectedImage)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            dialog?.dismiss()
        } else if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            val bitmap = data?.extras?.get("data") as? Bitmap
            onPhotoSelectedListner?.getImageBitmap(bitmap)
            dialog?.dismiss()
        } else if (requestCode == 200) {
            dialog?.dismiss()
            if (data == null) return
            try {
                val imageuri = data.data
                if (imageuri != null) {
                    try {
                        val path = FileUtils.getPath(requireContext(), imageuri)
                        if (path != null) {
                            val file = File(path)
                            onPhotoSelectedListner?.getImagepath(file, imageuri)
                        } else {
                            AndroidUtils.showToast(
                                "File Access error: Please check the file name without any spaces.",
                                context
                            )
                        }
                    } catch (e: Exception) {
                        AndroidUtils.showToast(
                            "File Access error: Please check the file name without any spaces.",
                            context
                        )
                    }
                } else {
                    AndroidUtils.showToast(
                        "File Access error: Please check the file name without any spaces.",
                        context
                    )
                }
            } catch (e: Exception) {
                AndroidUtils.showToast("File Access error:Please check your file ", context)
            }
            dialog?.dismiss()
        } else {
            dialog?.dismiss()
        }
    }

    override fun onAttach(context: Context) {
        try {
            onPhotoSelectedListner = targetFragment as? OnPhotoSelectedListner
        } catch (e: ClassCastException) {
            Log.e(TAG, "onAttach: ClasscatchException" + e.message)
            e.printStackTrace()
        }
        super.onAttach(context)
    }

    private fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var result: String? = null
        if ("content" == uri.scheme) {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME))
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.lastPathSegment
        }
        return result
    }
}
