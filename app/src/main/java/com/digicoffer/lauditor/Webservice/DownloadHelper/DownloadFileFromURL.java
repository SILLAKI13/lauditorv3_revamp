package com.digicoffer.lauditor.Webservice.DownloadHelper;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadFileFromURL extends AsyncTask<String, String, HttpResultDo> {

    private ProgressDialog progressDialog;
    private final String fileName;
    private final AsyncTaskCompleteListener callback;
    private final Context activity;
    private final String requestType;

    public DownloadFileFromURL(AsyncTaskCompleteListener callback, Context activity, String requestType, String fileName) {
        this.callback = callback;
        this.activity = activity;
        this.requestType = requestType;
        this.fileName = fileName;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.progressDialog = new ProgressDialog(activity);
        this.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        this.progressDialog.setCancelable(false);
        this.progressDialog.setTitle(requestType.equals(Constants.DOWNLOAD_VIEWFILE_TAG)
                ? "Loading file..." : "Downloading...");
        this.progressDialog.show();
    }

    @Override
    protected HttpResultDo doInBackground(String... f_url) {
        HttpResultDo httpResult = new HttpResultDo();
        int count;

        try {
            URL url = new URL(f_url[0]);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                httpResult.setResponseContent("Server returned HTTP " + responseCode + ": " + connection.getResponseMessage());
                httpResult.setResult(WebServiceHelper.ServiceCallStatus.Failed);
                return httpResult;
            }

            int lengthOfFile = connection.getContentLength();

            InputStream input = new BufferedInputStream(connection.getInputStream());
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File targetDir = new File(downloadsDir, "Digicoffer");
            if (!targetDir.exists() && !targetDir.mkdirs()) {
                httpResult.setResponseContent("Failed to create download directory.");
                httpResult.setResult(WebServiceHelper.ServiceCallStatus.Failed);
                return httpResult;
            }

            File outputFile = new File(targetDir, fileName);
            OutputStream output = new FileOutputStream(outputFile);

            byte[] data = new byte[1024];
            long total = 0;
            while ((count = input.read(data)) != -1) {
                total += count;
                publishProgress("" + (int) ((total * 100) / lengthOfFile));
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();

            JSONObject json = new JSONObject();
            json.put("msg", "Downloaded to: " + outputFile.getAbsolutePath());
            json.put("path", outputFile.getAbsolutePath());

            httpResult.setResponseContent(json.toString());
            httpResult.setResult(WebServiceHelper.ServiceCallStatus.Success);

        } catch (Exception e) {
            Log.e("DownloadError", "Error downloading file", e);
            httpResult.setResponseContent("Download failed: " + e.getMessage());
            httpResult.setResult(WebServiceHelper.ServiceCallStatus.Failed);
        }

        return httpResult;
    }

    @Override
    protected void onProgressUpdate(String... progress) {
        if (progressDialog != null) {
            progressDialog.setProgress(Integer.parseInt(progress[0]));
        }
    }

    @Override
    protected void onPostExecute(HttpResultDo httpResult) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        if (httpResult != null) {
            httpResult.setRequestType(requestType);
            callback.onAsyncTaskComplete(httpResult);
        } else {
            Log.e("DownloadFile", "httpResult is null");
        }
    }
}
