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
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL

class DownloadFlie(
    private val callback: AsyncTaskCompleteListener,
    private val activity: Context,
    private val requestType: String,
    private var fileName: String
) : AsyncTask<String, String, HttpResultDo>() {

    private var progressDialog: ProgressDialog? = null
    private var folder: String = ""

    override fun onPreExecute() {
        super.onPreExecute()
        progressDialog = ProgressDialog(activity).apply {
            setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            setCancelable(false)
            setTitle(
                if (requestType == Constants.DOWNLOAD_VIEWFILE_TAG) "Loading file....." else "Downloading...."
            )
            show()
        }
    }

    override fun doInBackground(vararg f_url: String?): HttpResultDo {
        var count = 0
        val httpResult = HttpResultDo()
        val folder_name = "Digicoffer"

        try {
            folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + folder_name
            val directory = File(folder)

            if (!directory.isDirectory || !directory.exists()) {
                directory.mkdirs()
            }

            if (requestType == Constants.DOWNLOAD_VIEWFILE_TAG) {
                fileName = "temporaryFile.pdf"
            }

            val file = File(folder, fileName)
            try {
                file.createNewFile()
            } catch (e: IOException) {
                e.fillInStackTrace()
            }

            try {
                val url = URL(f_url[0])
                val connection = url.openConnection()
                connection.connect()

                val input: InputStream = BufferedInputStream(url.openStream(), 8192)
                val output = FileOutputStream(file)
                val lengthOfFile = connection.contentLength
                val data = ByteArray(1024)
                val total: Long = 0

                while (input.read(data).also { count = it } > 0) {
                    // Legay behavior: total progress accumulation is commented out
                    publishProgress("" + ((total * 100) / lengthOfFile).toInt())
                    Log.d("Constraints", "Progress: " + ((total * 100) / lengthOfFile).toInt())

                    output.write(data, 0, count)
                }

                output.flush()
                output.close()
            } catch (e: IOException) {
                e.fillInStackTrace()
            }

            httpResult.result = WebServiceHelper.ServiceCallStatus.Success
            val `object` = JSONObject()
            `object`.put("msg", "Downloaded at: $folder_name/$fileName")
            `object`.put("path", "$folder_name/$fileName")
            httpResult.responseContent = `object`.toString()
            return httpResult

        } catch (e: JSONException) {
            e.fillInStackTrace()
        }

        httpResult.responseContent = "Something went wrong"
        httpResult.result = WebServiceHelper.ServiceCallStatus.Failed
        return httpResult
    }

    override fun onProgressUpdate(vararg progress: String?) {
        if (progressDialog != null) {
            progressDialog!!.setProgress(progress[0]?.toIntOrNull() ?: 0)
        }
    }

    override fun onPostExecute(httpResult: HttpResultDo) {
        if (progressDialog != null) {
            progressDialog!!.dismiss()
        }

        httpResult.requestType = requestType
        callback.onAsyncTaskComplete(httpResult)
    }
}
