package com.digicoffer.lauditor.Webservice.DownloadHelper

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class DownloadFileFromURL(
    private val callback: AsyncTaskCompleteListener,
    private val activity: Context,
    private val requestType: String,
    private val fileName: String
) : AsyncTask<String, String, HttpResultDo>() {

    private var progressDialog: ProgressDialog? = null

    override fun onPreExecute() {
        super.onPreExecute()
        progressDialog = ProgressDialog(activity).apply {
            setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            setCancelable(false)
            setTitle(
                if (requestType == Constants.DOWNLOAD_VIEWFILE_TAG) "Loading file..." else "Downloading..."
            )
            show()
        }
    }

    override fun doInBackground(vararg f_url: String?): HttpResultDo {
        val httpResult = HttpResultDo()
        var count: Int

        try {
            val url = URL(f_url[0])
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")
            connection.connect()

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                httpResult.responseContent = "Server returned HTTP " + responseCode + ": " + connection.responseMessage
                httpResult.result = WebServiceHelper.ServiceCallStatus.Failed
                return httpResult
            }

            val lengthOfFile = connection.contentLength

            val input: InputStream = BufferedInputStream(connection.inputStream)
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val targetDir = File(downloadsDir, "Digicoffer")
            if (!targetDir.exists() && !targetDir.mkdirs()) {
                httpResult.responseContent = "Failed to create download directory."
                httpResult.result = WebServiceHelper.ServiceCallStatus.Failed
                return httpResult
            }

            val outputFile = File(targetDir, fileName)
            val output: OutputStream = FileOutputStream(outputFile)

            val data = ByteArray(1024)
            var total: Long = 0
            while (input.read(data).also { count = it } != -1) {
                total += count
                publishProgress("" + ((total * 100) / lengthOfFile).toInt())
                output.write(data, 0, count)
            }

            output.flush()
            output.close()
            input.close()

            val json = JSONObject()
            json.put("msg", "Downloaded to: " + outputFile.absolutePath)
            json.put("path", outputFile.absolutePath)

            httpResult.responseContent = json.toString()
            httpResult.result = WebServiceHelper.ServiceCallStatus.Success

        } catch (e: Exception) {
            Log.e("DownloadError", "Error downloading file", e)
            httpResult.responseContent = "Download failed: " + e.message
            httpResult.result = WebServiceHelper.ServiceCallStatus.Failed
        }

        return httpResult
    }

    override fun onProgressUpdate(vararg progress: String?) {
        if (progressDialog != null) {
            progressDialog!!.setProgress(progress[0]?.toIntOrNull() ?: 0)
        }
    }

    override fun onPostExecute(httpResult: HttpResultDo) {
        if (progressDialog != null && progressDialog!!.isShowing) {
            progressDialog!!.dismiss()
        }

        httpResult.requestType = requestType
        callback.onAsyncTaskComplete(httpResult)
    }
}
