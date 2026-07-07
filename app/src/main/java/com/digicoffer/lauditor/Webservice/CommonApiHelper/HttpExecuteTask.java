package com.digicoffer.lauditor.Webservice.CommonApiHelper;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

//import com.digicoffer.lauditor.LoginActivity.ViewModels.biometric_page;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.digicoffer.lauditor.CommonFiles.CacheUtils.NetworkUtils;
import com.digicoffer.lauditor.CommonFiles.TokenManagerUtils.RetryRequest;
import com.digicoffer.lauditor.CommonFiles.TokenManagerUtils.TokenRefreshHelper;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpExecuteTask extends AsyncTask<String, Integer, HttpResultDo> {

    public static AtomicInteger openCounter = new AtomicInteger(0);
    public static final int MAX_CONCURRENCY = 1;
    private HttpResultDo httpResult = null;
    private String lastBody = ""; // add as a class field
    private String requestId = null;
    private String requestType = null;
    private boolean sslFlag = false;
    WebServiceHelper.RestMethodType restMethodType = WebServiceHelper.RestMethodType.GET;
    private String URL = "";
    private AsyncTaskCompleteListener callback;
    private Context activity = null;

    public HttpExecuteTask(String requestId, boolean sslFlag, WebServiceHelper.RestMethodType restMethodType, String baseURL,
                           AsyncTaskCompleteListener callback, Context activity, String requestType) {
        super();
        this.requestId = requestId;
        this.sslFlag = sslFlag;
        this.restMethodType = restMethodType;
        this.URL = baseURL;
        this.callback = callback;
        this.activity = activity;
        this.requestType = requestType;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    protected HttpResultDo doInBackground(String... params) {
        lastBody = (params != null && params.length > 0 && params[0] != null) ? params[0] : "";
        HttpResultDo httpResult = new HttpResultDo();

        // 🔴 INTERNET CHECK (Swift equivalent)
        if (!NetworkUtils.isInternetAvailable(activity)) {
            httpResult.setStatus_code(404);
            httpResult.setResult(WebServiceHelper.ServiceCallStatus.Exception);
            httpResult.setResponseContent(Constants.NO_INTERNET_MSG);
            return httpResult;
        }
        String data = "";
        HttpURLConnection httpURLConnection = null;
        try {
            if (requestType.equals("Label")
                    || requestType.equals("auth")
                    || requestType.equals("messages_rows")
//                    || requestType.equals("REFRESH_TOKEN_INTERNAL")   // ← ADD THIS
                    || URL.contains(Constants.EMAIL_UPLOAD_URL)
                    || requestType.startsWith("messages_attachment_")
                    || requestType.equals("CREATE_MEETING_LINK")) {
                try {
                    httpURLConnection = (HttpURLConnection) new URL(URL).openConnection();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if ((requestType.equals("Get DocEditor List")) || (requestType.equals("Delete DocEditor List")) || (requestType.equals("Save Document")) || (requestType.equals("Save As Document")) || (requestType.equals("Save DocEditor")) || (requestType.equals("Open Document"))) {
                httpURLConnection = (HttpURLConnection) new URL(URL).openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setDoOutput(false);
                httpURLConnection.setDoInput(true);

                httpURLConnection.setRequestProperty("Authorization", "Bearer " + Constants.TOKEN);
                httpURLConnection.setRequestProperty("Cofferid", Constants.USER_ID);
                httpURLConnection.setRequestProperty("Accept", "application/json");
                Log.d("Token", ":" + "Bearer " + (Constants.TOKEN) + ":" + httpURLConnection);
            } else if ((requestType.equals("GET UNREAD COUNT")) || (requestType.equals("RESET COUNT")) || (requestType.equals("UPDATE COUNT") || (requestType.equals("FCM_TOKEN_LOGOUT") || (requestType.equals("FCM_TOKEN_UPDATE"))))) {
                try {
                    httpURLConnection = (HttpURLConnection) new URL(URL).openConnection();
                    // Set the specific headers for UNREAD_COUNT
                    httpURLConnection.setRequestProperty("Authorization", "Bearer " + Constants.TOKEN);
                    httpURLConnection.setRequestProperty("Content-Type", "application/json");
                    httpURLConnection.setRequestProperty("Accept", "application/json");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
//            } else if (requestType.equals("Decrypt Doc")) {
//                try {
//                    httpURLConnection = (HttpURLConnection) new URL(URL).openConnection();
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            } else if (requestType.equals("Create Matter")||requestType.equals("Case Type")) {
//                try {
//                    httpURLConnection = (HttpURLConnection) new URL(URL).openConnection();
//                    // Set the specific headers for UNREAD_COUNT
//                    httpURLConnection.setRequestProperty("Authorization", "Bearer " + Constants.TOKEN);
//                    httpURLConnection.setRequestProperty("Content-Type", "application/json");
//                    httpURLConnection.setRequestProperty("Accept", "application/json");
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
            } else {
                httpURLConnection = (HttpURLConnection) new URL(Constants.base_URL + URL).openConnection();
            }
            Log.e("URL", ":" + httpURLConnection.getURL());
            if (!requestType.equals("LOGIN") && !requestType.equals("ONBOARD") && !requestType.equals("RESEND_OTP") && !requestType.equals("SWITCH_FIRM") && !requestType.equals("SIGNUP") && !requestType.equals("UPDATE") && !requestType.equals("LOGIN_PASSWORD") && !requestType.equals("FORGET_PASSWORD") && !requestType.equals("VERIFY_TOKEN") && !requestType.equals("GET UNREAD COUNT") && !requestType.equals("REFRESH_TOKEN_INTERNAL") && !requestType.equals("RESET COUNT") && !requestType.equals("UPDATE COUNT")) {
                httpURLConnection.setRequestProperty("Authorization", "Bearer " + Constants.TOKEN);
                Log.d("Token", ":" + "Bearer " + (Constants.TOKEN) + ":" + httpURLConnection);
            }
            switch (restMethodType) {
                case GET:
                    httpURLConnection.setRequestMethod("GET");
                    if (requestType.equals("Other Doc View")) {
                        httpURLConnection = (HttpURLConnection) new URL(URL).openConnection();
                        httpURLConnection.setRequestProperty("Authorization", "Bearer " + Constants.TOKEN);
                        httpURLConnection.setRequestProperty("x-client-type", "mobile");
                    }
                    break;
                case POST:

                    if (requestType.equals("Decrypt Doc")) {
                        httpURLConnection = (HttpURLConnection) new URL(URL).openConnection();
                        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
                        httpURLConnection.setRequestMethod("POST");
                        httpURLConnection.setDoOutput(true);
                        httpURLConnection.setDoInput(true);
                        httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
                        httpURLConnection.setRequestProperty("Cache-Control", "no-cache");
                        httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                        httpURLConnection.setRequestProperty("Accept", "application/json");
                        httpURLConnection.setRequestProperty("Authorization", "Bearer " + Constants.TOKEN);
                        httpURLConnection.setRequestProperty("x-client-type", "mobile");

                        DataOutputStream requestStream = new DataOutputStream(httpURLConnection.getOutputStream());

                        // Dynamically add all fields from the JSON
                        JSONObject json = new JSONObject(params[0] != null ? params[0] : "{}");
                        Iterator<String> keys = json.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            String value = json.optString(key);

                            requestStream.writeBytes("--" + boundary + "\r\n");
                            requestStream.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"\r\n\r\n");
                            requestStream.writeBytes(value + "\r\n");
                        }

                        // Finish the multipart request
                        requestStream.writeBytes("--" + boundary + "--\r\n");
                        requestStream.flush();
                        requestStream.close();

                        Log.d("MultipartRequest", "Fields sent: " + params.toString());

                    } else {
                        // Normal JSON POST
                        httpURLConnection.setRequestMethod("POST");
                        httpURLConnection.setDoInput(true);
                        httpURLConnection.setDoOutput(true);
                        httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                        DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                        wr.write(params[0].getBytes(StandardCharsets.UTF_8));
                        wr.flush();
                        wr.close();
                    }
                    break;
                case PATCH:
                    httpURLConnection.setRequestMethod("PATCH");
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                    DataOutputStream wr_patch = new DataOutputStream(httpURLConnection.getOutputStream());
                    wr_patch.write(params[0].getBytes(StandardCharsets.UTF_8)); // Write using UTF-8 encoding
                    wr_patch.flush();
                    wr_patch.close();
                    break;
                case PUT:
                    httpURLConnection.setRequestMethod("PUT");
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                    DataOutputStream wr1 = new DataOutputStream(httpURLConnection.getOutputStream());
                    wr1.write(params[0].getBytes(StandardCharsets.UTF_8)); // Write using UTF-8 encoding
                    wr1.flush();
                    wr1.close();
                    break;
                case DELETE:
                    httpURLConnection.setRequestMethod("DELETE");
                    if (!params[0].isEmpty()) {
                        httpURLConnection.setDoInput(true);
                        httpURLConnection.setDoOutput(true);
                        httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                        DataOutputStream dos = new DataOutputStream(httpURLConnection.getOutputStream());
                        dos.write(params[0].getBytes(StandardCharsets.UTF_8)); // Write using UTF-8 encoding
                        dos.flush();
                        dos.close();
                    }
                    break;
            }


            int status_code = httpURLConnection.getResponseCode();
            Log.i("status_code:", String.valueOf(status_code));
            httpResult.setStatus_code(status_code);
            Log.e("SCode:", String.valueOf(status_code));
            if (status_code == 200) {
                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(in);
                BufferedReader r = new BufferedReader(inputStreamReader);
                StringBuilder total = new StringBuilder();
                for (String line; (line = r.readLine()) != null; ) {
                    total.append(line).append('\n');
                }
                data = total.toString();
                httpResult.setResult(WebServiceHelper.ServiceCallStatus.Success);
                httpResult.setResponseContent(data);
            } else if (status_code == 201) {
                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(in);
                BufferedReader r = new BufferedReader(inputStreamReader);
                StringBuilder total = new StringBuilder();
                for (String line; (line = r.readLine()) != null; ) {
                    total.append(line).append('\n');
                }
                data = total.toString();
                httpResult.setResult(WebServiceHelper.ServiceCallStatus.Success);
                httpResult.setResponseContent(data);
            } else if (status_code == 401) {
                try {
                    BufferedReader r = new BufferedReader(new InputStreamReader(httpURLConnection.getErrorStream()));
                    StringBuilder total = new StringBuilder();
                    for (String line; (line = r.readLine()) != null; ) {
                        total.append(line).append('\n');
                    }
                    data = total.toString();
                    httpResult.setResult(WebServiceHelper.ServiceCallStatus.Failed);
                    httpResult.setResponseContent(data);
                } catch (IOException e) {
                    e.fillInStackTrace();
                }
            } else if (status_code == 404) {
                try {
                    BufferedReader r = new BufferedReader(new InputStreamReader(httpURLConnection.getErrorStream()));
                    StringBuilder total = new StringBuilder();
                    for (String line; (line = r.readLine()) != null; ) {
                        total.append(line).append('\n');
                    }
                    data = total.toString();
                    httpResult.setResult(WebServiceHelper.ServiceCallStatus.Failed);
                    httpResult.setResponseContent(data);
                } catch (IOException e) {
                    e.fillInStackTrace();
                }
//                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
//                SharedPreferences.Editor editor = pref.edit();
//                editor.putBoolean("isLogin", false);
//                editor.commit();
//                Intent intent = new Intent(activity, LoginActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                AndroidUtils.showToast(httpResult.getResponseContent(), activity);
            } else if (status_code == 400) {
                try {
                    BufferedReader r = new BufferedReader(new InputStreamReader(httpURLConnection.getErrorStream()));
                    StringBuilder total = new StringBuilder();
                    for (String line; (line = r.readLine()) != null; ) {
                        total.append(line).append('\n');
                    }
                    data = total.toString();
                    httpResult.setResult(WebServiceHelper.ServiceCallStatus.Success);
                    httpResult.setResponseContent(data);
                } catch (IOException e) {
                    e.fillInStackTrace();
                }
            } else if (status_code == 502) {
                httpResult.setResult(WebServiceHelper.ServiceCallStatus.Pending);
                httpResult.setResponseContent("502 Bad Gateway");
            } else if (status_code == 500) {
                try {
                    BufferedReader r = new BufferedReader(new InputStreamReader(httpURLConnection.getErrorStream()));
                    StringBuilder total = new StringBuilder();
                    for (String line; (line = r.readLine()) != null; ) {
                        total.append(line).append('\n');
                    }
                    data = total.toString();
                    httpResult.setResult(WebServiceHelper.ServiceCallStatus.Failed);
                    httpResult.setResponseContent(data);
                } catch (IOException e) {
                    e.fillInStackTrace();
                }
            } else {
                httpResult.setResult(WebServiceHelper.ServiceCallStatus.Failed);
                httpResult.setResponseContent("Error connection, Please try again");
            }
        } catch (ConnectTimeoutException e) {
            httpResult.setResult(WebServiceHelper.ServiceCallStatus.Exception);
            httpResult.setErrorMessage("Exception: Connection Timeout " + e.getMessage());
        } catch (IOException e) {
            httpResult.setResult(WebServiceHelper.ServiceCallStatus.Exception);
            httpResult.setErrorMessage("Exception: " + e.getMessage());
            AndroidUtils.logMsg("WebServiceHelper.callWebService(): IO Exception " + e.getMessage());
        } catch (Exception e) {
            AndroidUtils.logMsg("HttpExecuteTask.doInBackground(): Exception " + e.getMessage());
            httpResult = new HttpResultDo();
            httpResult.setResult(WebServiceHelper.ServiceCallStatus.Exception);
            httpResult.setResponseContent(e.getMessage());
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        return httpResult;
    }

    protected void onProgressUpdate(Integer... progress) {
    }

    @Override
    protected void onPostExecute(HttpResultDo httpResult) {
        openCounter.decrementAndGet();
        httpResult.setRequestId(requestId);
        httpResult.setRequestType(requestType);
        super.onPostExecute(httpResult);

        try {
            Log.e("Response_Msg", httpResult.getResponseContent());

            // ── Skip 401 refresh for these request types ──────────────────────
            boolean skipRefresh =
                    requestType.equals("LOGIN_OTP")
                            || requestType.equals("LOGIN_PASSWORD")
                            || requestType.equals("REGISTER_OTP")
                            || requestType.equals("DEVICE_SYNC")               // ← ADD THIS
                            || requestType.equals("Label")
                            || requestType.equals("auth")
                            || requestType.equals("messages_rows")
                            || requestType.equals("REFRESH_TOKEN_INTERNAL")
                            || URL.contains(Constants.EMAIL_UPLOAD_URL);

            if (httpResult.getStatus_code() == 401 && !skipRefresh) {
                // Build a retry snapshot and delegate to TokenRefreshHelper
                RetryRequest retryRequest = new RetryRequest(
                        requestId,
                        restMethodType,
                        URL,        // already the full URL as stored in this task
                        lastBody,   // see note below
                        requestType
                );
                TokenRefreshHelper.handleUnauthorized(activity, retryRequest, callback);
                return; // don't call callback yet — it fires after retry
            }

            callback.onAsyncTaskComplete(httpResult);

        } catch (Exception e) {
            Log.d("Error_mg", Objects.requireNonNull(e.getMessage()));
        }
    }
}

