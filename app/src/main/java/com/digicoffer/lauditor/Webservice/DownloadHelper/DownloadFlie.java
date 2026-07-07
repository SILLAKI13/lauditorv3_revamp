package com.digicoffer.lauditor.Webservice.DownloadHelper;

import static androidx.constraintlayout.widget.Constraints.TAG;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DownloadFlie extends AsyncTask<String , String, HttpResultDo> {

    private ProgressDialog progressDialog;
    private String fileName;
    private String folder;
    private AsyncTaskCompleteListener callback;
    private Context activity = null;
    private boolean isDownloaded;
    private String requestType = null;

    public DownloadFlie(AsyncTaskCompleteListener callback, Context activity, String requestType, String fileName){

        this.callback = callback;
        this.activity = activity;
        this.requestType = requestType;
        this.fileName = fileName;
    }

    /**
     * Before starting background thread
     * Show Progress Bar Dialog
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.progressDialog = new ProgressDialog(activity);
        this.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        this.progressDialog.setCancelable(false);
        this.progressDialog.setTitle(requestType.equals(Constants.DOWNLOAD_VIEWFILE_TAG) ? "Loading file....." : "Downloading....");
        this.progressDialog.show();
    }

    /**
     * Downloading file in background thread
     */
    @Override
    protected HttpResultDo doInBackground(String... f_url) {
        int count = 0;
        HttpResultDo httpResult = new HttpResultDo();
        String folder_name = "Digicoffer";
        try {

            String timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
            //External directory path to save file
            folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + File.separator + folder_name;
            //Create androiddeft folder if it does not exist
//            folder = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + folder_name;
            File directory = new File(folder);

            if (!directory.isDirectory() || !directory.exists()) {
                directory.mkdirs();
            }
            if (requestType.equals(Constants.DOWNLOAD_VIEWFILE_TAG))
                fileName = "temporaryFile.pdf";
//            else
//                fileName = conn{
//                getHeaderField("Content-Disposition").split("=")[1];
//        }
            File file = new File(folder,fileName);
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.fillInStackTrace();
            }
//            file.createNewFile();
//            String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
//
//            File myDir = new File(root + "/saved_images");
//            if (!myDir.exists()) {
//                myDir.mkdirs();
//            }
//            Random generator = new Random();
//            int n = 10000;
//            n = generator.nextInt(n);
//            String fname = "Image-"+ n +".jpg";
//            File file = new File (myDir, fname);
//            if (file.exists ())
//                file.delete ();
//            if (file.exists ()) file.delete ();
//            file.createNewFile();
//            try {
//            // Output stream to write file
//            Reader pr = new FileReader(file);
//            String line = "";
//            int data = pr.read();
//            while (data != -1) {
//                line += (char) data;
//                data = pr.read();
//            }
//            pr.close();
            try {
                URL url = new URL(f_url[0]);
                URLConnection connection = url.openConnection();
                connection.connect();
                // getting file length

                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(url.openStream(), 8192);
                FileOutputStream output = new FileOutputStream(file);
                int lengthOfFile = connection.getContentLength();
                byte[] data = new byte[1024];
                long total = 0;
                while ((count = input.read(data)) >0) {
//                    total += count;
//                    // publishing the progress....
//                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lengthOfFile));
                    Log.d(TAG, "Progress: " + (int) ((total * 100) / lengthOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

//             closing streams
                output.close();
            } catch (IOException e) {
                e.fillInStackTrace();
            }
//            input.close();
//            MediaScannerConnection.scanFile(progressDialog.getContext(), new String[] { file.toString() }, null,
//                    new MediaScannerConnection.OnScanCompletedListener() {
//                        public void onScanCompleted(String path, Uri uri) {
//                            Log.i("ExternalStorage", "Scanned " + path + ":");
//                            Log.i("ExternalStorage", "-> uri=" + uri);
//                        }
//                    });
                httpResult.setResult(WebServiceHelper.ServiceCallStatus.Success);
                JSONObject object = new JSONObject();
                object.put("msg", "Downloaded at: " + folder_name + "/" + fileName);
                object.put("path", folder_name + "/" + fileName);
                httpResult.setResponseContent(object.toString());
                return httpResult;
            } catch (JSONException e) {
                e.fillInStackTrace();
            }
//        } catch (Exception e) {
//            Log.e("Error: ", e.getMessage());
//        }
        httpResult.setResponseContent("Something went wrong");
        httpResult.setResult(WebServiceHelper.ServiceCallStatus.Failed);
        return httpResult;
    }

    /**
     * Updating progress bar
     */
    protected void onProgressUpdate(String... progress) {
        // setting progress percentage
        progressDialog.setProgress(Integer.parseInt(progress[0]));
    }


    @Override
    protected void onPostExecute(HttpResultDo httpResult) {
        // dismiss the dialog after the file was downloaded
        this.progressDialog.dismiss();

        // Display File path after downloading
        httpResult.setRequestType(requestType);
        callback.onAsyncTaskComplete(httpResult);
    }


}
