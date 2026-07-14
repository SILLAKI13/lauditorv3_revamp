package com.digicoffer.lauditor.CommonFiles.FileSelection

import android.annotation.TargetApi
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.annotation.RequiresApi
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class FileUtils {
    companion object {
        @JvmStatic
        fun getDataColumn(
            context: Context,
            uri: Uri,
            selection: String?,
            selectionArgs: Array<String>?
        ): String? {
            var cursor: Cursor? = null
            val column = "_data"
            val projection = arrayOf(column)
            try {
                cursor = context.contentResolver.query(
                    uri,
                    projection,
                    selection,
                    selectionArgs,
                    null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    val columnIndex = cursor.getColumnIndexOrThrow(column)
                    return cursor.getString(columnIndex)
                }
            } finally {
                cursor?.close()
            }
            return null
        }

        @JvmStatic
        fun isExternalStorageDocument(uri: Uri): Boolean {
            return "com.android.externalstorage.documents" == uri.authority
        }

        @JvmStatic
        fun isGoogleDrive(uri: Uri): Boolean {
            return "com.google.android.apps.docs.storage" == uri.authority
        }

        @JvmStatic
        fun isDownloadsDocument(uri: Uri): Boolean {
            return "com.android.providers.downloads.documents" == uri.authority
        }

        @JvmStatic
        fun isMediaDocument(uri: Uri): Boolean {
            return "com.android.providers.media.documents" == uri.authority
        }

        @JvmStatic
        fun isGooglePhotosUri(uri: Uri): Boolean {
            return "com.google.android.apps.photos.content" == uri.authority
        }

        @JvmStatic
        @TargetApi(Build.VERSION_CODES.KITKAT)
        fun download_path(context: Context, uri: Uri): String? {
            val fileName = getFileName(context, uri)
            val cacheDir = getDocumentCacheDir(context)
            val file = generateFileName(fileName, cacheDir)
            var destinationPath: String? = null
            if (file != null) {
                destinationPath = file.absolutePath
                saveFileFromUri(context, uri, destinationPath)
            }
            return destinationPath
        }

        @JvmStatic
        @RequiresApi(Build.VERSION_CODES.O)
        fun getPath(context: Context, uri: Uri): String? {
            val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

            // DocumentProvider
            if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    if ("primary".equals(type, ignoreCase = true)) {
                        return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    } else {
                        val fileList = File("/storage/").listFiles()
                        if (fileList != null) {
                            for (file in fileList) {
                                if (!file.absolutePath.equals(
                                        Environment.getExternalStorageDirectory().absolutePath,
                                        ignoreCase = true
                                    ) && file.isDirectory && file.canRead()
                                ) {
                                    return file.toString() + "/" + split[1]
                                }
                            }
                        }
                    }
                } else if (isGoogleDrive(uri)) {
                    val fileName = getFileName(context, uri)
                    val cacheDir = getDocumentCacheDir(context)
                    val file = generateFileName(fileName, cacheDir)
                    var destinationPath: String? = null
                    if (file != null) {
                        destinationPath = file.absolutePath
                        saveFileFromUri(context, uri, destinationPath)
                    }
                    return destinationPath
                } else if (isDownloadsDocument(uri)) {
                    val id = DocumentsContract.getDocumentId(uri)
                    if (id != null && id.startsWith("raw:")) {
                        return id.substring(4)
                    }
                    val contentUriPrefixesToTry = arrayOf(
                        "content://downloads/public_downloads",
                        "content://downloads/my_downloads",
                        "content://downloads/files_in_downloads"
                    )
                    for (contentUriPrefix in contentUriPrefixesToTry) {
                        try {
                            val contentUri = ContentUris.withAppendedId(
                                Uri.parse(contentUriPrefix),
                                java.lang.Long.valueOf(id)
                            )
                            val path = getDataColumn(context, contentUri, null, null)
                            if (path != null) {
                                return path
                            }
                        } catch (e: Exception) {
                            // Suppress and try next prefix
                        }
                    }

                    // Copy file to accessible cache using streams
                    val fileName = getFileName(context, uri)
                    val cacheDir = getDocumentCacheDir(context)
                    val file = generateFileName(fileName, cacheDir)
                    var destinationPath: String? = null
                    if (file != null) {
                        destinationPath = file.absolutePath
                        saveFileFromUri(context, uri, destinationPath)
                    }
                    return destinationPath
                } else if (isMediaDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    var contentUri: Uri? = null
                    if ("image" == type) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else if ("video" == type) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    } else if ("audio" == type) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                    val selection = "_id=?"
                    val selectionArgs = arrayOf(split[1])
                    return if (contentUri != null) {
                        getDataColumn(context, contentUri, selection, selectionArgs)
                    } else {
                        null
                    }
                }
            } else if ("content".equals(uri.scheme, ignoreCase = true)) {
                if (isGooglePhotosUri(uri)) {
                    return uri.lastPathSegment
                }
                return getDataColumn(context, uri, null, null)
            } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                return uri.path
            }
            return null
        }

        @JvmStatic
        fun getFileName(context: Context, uri: Uri): String? {
            val mimeType = context.contentResolver.getType(uri)
            var filename: String? = null
            if (mimeType == null) {
                var path: String? = null
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    path = getPath(context, uri)
                }
                filename = if (path == null) {
                    getName(uri.toString())
                } else {
                    val file = File(path)
                    file.name
                }
            } else {
                val returnCursor = context.contentResolver.query(uri, null, null, null, null)
                if (returnCursor != null) {
                    val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    returnCursor.moveToFirst()
                    filename = returnCursor.getString(nameIndex)
                    returnCursor.close()
                }
            }
            return filename
        }

        @JvmStatic
        fun getName(filename: String?): String? {
            if (filename == null) return null
            val index = filename.lastIndexOf('/')
            return filename.substring(index + 1)
        }

        @JvmStatic
        fun getDocumentCacheDir(context: Context): File {
            val dir = File(context.cacheDir, "documents")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            return dir
        }

        @JvmStatic
        fun generateFileName(name: String?, directory: File): File? {
            if (name == null) return null
            var currentName = name
            var file = File(directory, currentName)
            if (file.exists()) {
                var fileName = currentName
                var extension = ""
                val dotIndex = currentName.lastIndexOf('.')
                if (dotIndex > 0) {
                    fileName = currentName.substring(0, dotIndex)
                    extension = currentName.substring(dotIndex)
                }
                var index = 0
                while (file.exists()) {
                    index++
                    currentName = "$fileName($index)$extension"
                    file = File(directory, currentName)
                }
            }
            try {
                if (!file.createNewFile()) {
                    return null
                }
            } catch (e: IOException) {
                return null
            }
            return file
        }

        @JvmStatic
        private fun saveFileFromUri(context: Context, uri: Uri, destinationPath: String) {
            var inputStream: InputStream? = null
            var bos: BufferedOutputStream? = null
            try {
                inputStream = context.contentResolver.openInputStream(uri)
                bos = BufferedOutputStream(FileOutputStream(destinationPath, false))
                val buf = ByteArray(1024)
                var bytesRead = inputStream?.read(buf) ?: -1
                while (bytesRead != -1) {
                    bos.write(buf, 0, bytesRead)
                    bytesRead = inputStream?.read(buf) ?: -1
                }
            } catch (e: IOException) {
                e.fillInStackTrace()
            } finally {
                try {
                    inputStream?.close()
                    bos?.close()
                } catch (e: IOException) {
                    e.fillInStackTrace()
                }
            }
        }
    }
}
