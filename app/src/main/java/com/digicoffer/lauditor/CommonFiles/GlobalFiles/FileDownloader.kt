package com.digicoffer.lauditor.CommonFiles.GlobalFiles

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import java.io.UnsupportedEncodingException
import java.net.URLDecoder

class FileDownloader {
    companion object {
        @JvmStatic
        fun downloadFile(
            context: Context,
            fileUrl: String?,
            fileName: String?,
            mimeType: String?
        ) {
            try {
                var name = fileName
                if (name == null || name.trim { it <= ' ' }.isEmpty()) {
                    name = "downloaded_file"
                }
                name = name.trim { it <= ' ' }.replace("[\\\\/:*?\"<>|]".toRegex(), "_")

                val request = DownloadManager.Request(Uri.parse(fileUrl))
                request.setTitle(name)
                request.setDescription("Downloading...")

                request.setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE or
                            DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
                )

                request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS, name
                )

                request.setAllowedNetworkTypes(
                    DownloadManager.Request.NETWORK_WIFI or
                            DownloadManager.Request.NETWORK_MOBILE
                )

                request.setAllowedOverMetered(true)
                request.setAllowedOverRoaming(true)

                var resolvedMime = mimeType
                if (resolvedMime.isNullOrEmpty()) {
                    resolvedMime = guessMimeFromFileName(name)
                }
                if (!resolvedMime.isNullOrEmpty()) {
                    request.setMimeType(resolvedMime)
                }

                val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
                dm?.enqueue(request)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun downloadFile(
            context: Context,
            fileUrl: String?,
            fileName: String?
        ) {
            val extension = MimeTypeMap.getFileExtensionFromUrl(fileName)
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            downloadFile(context, fileUrl, fileName, mimeType)
        }

        private fun guessMimeFromFileName(fileName: String?): String? {
            if (fileName == null) return null
            val dot = fileName.lastIndexOf('.')
            if (dot == -1 || dot == fileName.length - 1) return null
            val ext = fileName.substring(dot + 1).lowercase().trim { it <= ' ' }
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
        }

        private fun getFileNameFromUrl(url: String?): String? {
            try {
                val uri = Uri.parse(url)
                val disposition = uri.getQueryParameter("response-content-disposition")
                if (disposition != null) {
                    val decodedDisposition = URLDecoder.decode(disposition, "UTF-8")

                    if (decodedDisposition.contains("filename*=")) {
                        val idx = decodedDisposition.indexOf("filename*=") + "filename*=".length
                        var valStr = decodedDisposition.substring(idx).trim { it <= ' ' }
                        if (valStr.contains("''")) {
                            valStr = valStr.substring(valStr.indexOf("''") + 2)
                        }
                        valStr = URLDecoder.decode(valStr.replace("\"", "").trim { it <= ' ' }, "UTF-8")
                        if (valStr.contains(";")) valStr = valStr.substring(0, valStr.indexOf(";")).trim { it <= ' ' }
                        if (valStr.isNotEmpty()) return valStr
                    }

                    if (decodedDisposition.contains("filename=")) {
                        val idx = decodedDisposition.indexOf("filename=") + "filename=".length
                        var valStr = decodedDisposition.substring(idx).replace("\"", "").trim { it <= ' ' }
                        if (valStr.contains(";")) valStr = valStr.substring(0, valStr.indexOf(";")).trim { it <= ' ' }
                        if (valStr.isNotEmpty()) return valStr
                    }
                }

                val path = uri.path
                if (path != null && path.contains("/")) {
                    val segment = path.substring(path.lastIndexOf('/') + 1)
                    if (segment.isNotEmpty()) {
                        return URLDecoder.decode(segment, "UTF-8")
                    }
                }
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
            return null
        }
    }
}
