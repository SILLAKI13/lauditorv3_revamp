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
        var inputStream: InputStream? = null
        try {
            val url = URL(strings[0])
            val urlConnection = url.openConnection() as HttpsURLConnection
            if (urlConnection.responseCode == 200) {
                inputStream = BufferedInputStream(urlConnection.inputStream)
            }
        } catch (e: IOException) {
            e.fillInStackTrace()
            return null
        }
        return inputStream
    }

    override fun onPostExecute(inputStream: InputStream?) {
        pdfView?.fromStream(inputStream)?.load()
        progressBar?.visibility = View.GONE
        pdfView?.visibility = View.VISIBLE
    }

    fun cancelLoading() {
        isCancelled = true
        pdfView = null
        progressBar?.visibility = View.GONE
    }
}
