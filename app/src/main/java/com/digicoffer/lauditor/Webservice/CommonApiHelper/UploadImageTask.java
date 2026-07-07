package com.digicoffer.lauditor.Webservice.CommonApiHelper;

import android.content.Context;
import android.os.AsyncTask;
import android.webkit.MimeTypeMap;

import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

public class UploadImageTask extends AsyncTask<String, String, HttpResultDo> {

    private File uploadFile;
    private boolean sslFlag;
    private WebServiceHelper.RestMethodType restMethodType;
    private String baseURL;
    private AsyncTaskCompleteListener callback;
    private Context activity;
    private String requestType;

    public UploadImageTask(File uploadFile, boolean sslFlag, WebServiceHelper.RestMethodType restMethodType,
                           String baseURL, AsyncTaskCompleteListener callback, Context activity, String requestType) {
        this.uploadFile = uploadFile;
        this.sslFlag = sslFlag;
        this.restMethodType = restMethodType;
        this.baseURL = baseURL;
        this.callback = callback;
        this.activity = activity;
        this.requestType = requestType;
    }

    @Override
    protected HttpResultDo doInBackground(String... params) {
        HttpResultDo httpResult = new HttpResultDo();
        String boundary = "WebKitFormBoundary" + System.currentTimeMillis();
        String LINE_FEED = "\r\n";
        HttpURLConnection httpURLConnection = null;

        try {
            URL url;
            if (requestType.equals("Upload DocEditor File")) {
                url = new URL(baseURL);
            } else {
                url = new URL(Constants.base_URL + baseURL);
            }

            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Cofferid", Constants.USER_ID);
            httpURLConnection.setRequestProperty("Authorization", "Bearer " + Constants.TOKEN);
            httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            DataOutputStream requestStream = new DataOutputStream(httpURLConnection.getOutputStream());

            JSONObject json = new JSONObject(params[0] != null ? params[0] : "{}");
            Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                String value = json.optString(key);

                requestStream.writeBytes("--" + boundary + LINE_FEED);
                requestStream.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"" + LINE_FEED);
                requestStream.writeBytes("Content-Type: text/plain; charset=UTF-8" + LINE_FEED);
                requestStream.writeBytes(LINE_FEED);
                requestStream.writeBytes(value + LINE_FEED);
            }

            // Add content_type
            String contentType = getMimeType(uploadFile.getAbsolutePath());
            if (contentType == null) contentType = "application/octet-stream";

            requestStream.writeBytes("--" + boundary + LINE_FEED);
            requestStream.writeBytes("Content-Disposition: form-data; name=\"content_type\"" + LINE_FEED);
            requestStream.writeBytes("Content-Type: text/plain; charset=UTF-8" + LINE_FEED);
            requestStream.writeBytes(LINE_FEED);
            requestStream.writeBytes(contentType + LINE_FEED);

            // File part
            requestStream.writeBytes("--" + boundary + LINE_FEED);
            requestStream.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + uploadFile.getName() + "\"" + LINE_FEED);
            requestStream.writeBytes("Content-Type: " + contentType + LINE_FEED);
            requestStream.writeBytes("Content-Transfer-Encoding: binary" + LINE_FEED);
            requestStream.writeBytes(LINE_FEED);

            FileInputStream inputStream = new FileInputStream(uploadFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                requestStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            requestStream.writeBytes(LINE_FEED);

            requestStream.writeBytes("--" + boundary + "--" + LINE_FEED);
            requestStream.flush();
            requestStream.close();

            int status = httpURLConnection.getResponseCode();
            BufferedReader reader;
            if (status >= 200 && status < 300) {
                httpResult.setResult(WebServiceHelper.ServiceCallStatus.Success);
                reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            } else {
                httpResult.setResult(WebServiceHelper.ServiceCallStatus.Failed);
                reader = new BufferedReader(new InputStreamReader(httpURLConnection.getErrorStream()));
            }

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }
            reader.close();
            httpResult.setResponseContent(response.toString());

        } catch (ConnectTimeoutException e) {
            httpResult.setResult(WebServiceHelper.ServiceCallStatus.Exception);
            httpResult.setResponseContent("Exception: Connection Timeout " + e.getMessage());
        } catch (IOException e) {
            httpResult.setResult(WebServiceHelper.ServiceCallStatus.Exception);
            httpResult.setResponseContent("IOException: " + e.getMessage());
            AndroidUtils.logMsg("UploadImageTask: IO Exception " + e.getMessage());
        } catch (Exception e) {
            httpResult.setResult(WebServiceHelper.ServiceCallStatus.Exception);
            httpResult.setResponseContent(e.getMessage() != null ? e.getMessage() : "Unknown error");
            AndroidUtils.logMsg("UploadImageTask: Exception " + e.getMessage());
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }

        return httpResult;
    }

    @Override
    protected void onPostExecute(HttpResultDo httpResult) {
        httpResult.setRequestType(requestType);
        super.onPostExecute(httpResult);
        try {
            callback.onAsyncTaskComplete(httpResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getMimeType(String filePath) {
        String extension = "";
        int index = filePath.lastIndexOf(".");
        if (index > 0) {
            extension = filePath.substring(index + 1).toLowerCase();
        }

        // Manual strong mapping
        switch (extension) {
            case "pdf":
                return "application/pdf";

            case "doc":
                return "application/msword";

            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

            case "xls":
                return "application/vnd.ms-excel";

            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

            case "ppt":
                return "application/vnd.ms-powerpoint";

            case "pptx":
                return "application/vnd.openxmlformats-officedocument.presentationml.presentation";

            case "jpg":
            case "jpeg":
                return "image/jpeg";

            case "png":
                return "image/png";

            case "txt":
                return "text/plain";

            case "csv":
                return "text/csv";

            case "zip":
                return "application/zip";
        }

        // Secondary fallback
        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        return mime != null ? mime : "application/";
    }

}
