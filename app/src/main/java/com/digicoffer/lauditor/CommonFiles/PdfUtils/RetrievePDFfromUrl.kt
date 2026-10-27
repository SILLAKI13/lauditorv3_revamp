package com.digicoffer.lauditor.CommonFiles.PdfUtils

import android.os.AsyncTask
import android.view.View
import android.widget.ProgressBar
import com.github.barteksc.pdfviewer.PDFView
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection

open class RetrievePDFfromUrl(
    @JvmField var pdfView: PDFView?,
    @JvmField var progressBar: ProgressBar?
) : AsyncTask<String, Void, InputStream?>() {

    private var isCancelled = false

    override fun doInBackground(vararg strings: String): InputStream? {
        if (strings.isEmpty() || strings[0].isNullOrEmpty()) return null
        var currentUrl = strings[0]
        var redirectCount = 0
        try {
            while (redirectCount < 5 && !isCancelled) {
                val url = URL(currentUrl)
                val conn = url.openConnection() as? java.net.HttpURLConnection ?: return null
                conn.instanceFollowRedirects = true
                conn.connectTimeout = 15000
                conn.readTimeout = 30000
                val code = conn.responseCode
                if (code == java.net.HttpURLConnection.HTTP_MOVED_PERM ||
                    code == java.net.HttpURLConnection.HTTP_MOVED_TEMP ||
                    code == 307 || code == 308
                ) {
                    val location = conn.getHeaderField("Location") ?: break
                    currentUrl = location
                    redirectCount++
                    conn.disconnect()
                } else if (code == java.net.HttpURLConnection.HTTP_OK) {
                    return BufferedInputStream(conn.inputStream)
                } else {
                    break
                }
            }
        } catch (e: Exception) {
            e.fillInStackTrace()
            return null
        }
        return null
    }

    override fun onPostExecute(inputStream: InputStream?) {
        try {
            if (inputStream != null && pdfView != null && !isCancelled) {
                pdfView?.fromStream(inputStream)
                    ?.onLoad { progressBar?.visibility = View.GONE }
                    ?.onError { progressBar?.visibility = View.GONE }
                    ?.load()
                pdfView?.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            progressBar?.visibility = View.GONE
        }
    }

    fun cancelLoading() {
        isCancelled = true
        pdfView = null
        progressBar?.visibility = View.GONE
    }
}
