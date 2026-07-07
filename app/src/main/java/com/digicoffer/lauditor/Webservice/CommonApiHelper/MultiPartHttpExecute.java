package com.digicoffer.lauditor.Webservice.CommonApiHelper;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.digicoffer.lauditor.LoginActivity.ViewModels.LoginActivity;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MultiPartHttpExecute extends AsyncTask<String, Integer, HttpResultDo> {

    public static AtomicInteger openCounter = new AtomicInteger(0);
    public static final int MAX_CONCURRENCY = 1;
    //    private HttpResultDo httpResult = null;
    private String requestId = null;
    private String requestType = null;
    private boolean sslFlag = false;
    WebServiceHelper.RestMethodType restMethodType = WebServiceHelper.RestMethodType.GET;
    private String URL = "";
    private AsyncTaskCompleteListener callback;
    private Context activity = null;

    HttpResultDo httpResult = new HttpResultDo();

    public MultiPartHttpExecute(String requestId, boolean sslFlag, WebServiceHelper.RestMethodType restMethodType, String baseURL,
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
        String data = "";
        String fullUrl = "";


        try {
            if (requestType.equals("Label") || (requestType.equals("auth")) || (requestType.equals("messages_rows")) || (URL.contains(Constants.EMAIL_UPLOAD_URL))) {
                fullUrl = URL;
            } else {
                fullUrl = Constants.base_URL + URL;
            }
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = null;

            if (URL.equals("v3/decrypt")) {
                // Convert string to JSONObject
                JSONObject jsonObject = new JSONObject(params[0]);
                MultipartBody.Builder bodyBuilder = new MultipartBody.Builder();
                MultipartBody.Builder builder = bodyBuilder.setType(MultipartBody.FORM);

                for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
                    Object key = it.next();
                    String key_String = key.toString();
                    String key_value = jsonObject.getString(key_String);

                    builder.addFormDataPart(key_String, key_value);
                }
                body = builder.build();
            } else {
                body = RequestBody.create(mediaType, params[0]);
            }
            Request request = new Request.Builder()
                    .url(fullUrl)
                    .method(this.restMethodType.toString(), body)
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + Constants.TOKEN)
                    .build();
            // Perform the request asynchronously
            Response response = client.newCall(request).execute();

            int status_code = response.code();
            Log.i("status_code:", String.valueOf(status_code));
            httpResult.setStatus_code(status_code);
            // Check if the response is successful
            if (response.isSuccessful()) {
                // Handle successful response
                assert response.body() != null;
                String responseData = response.body().string();
                Log.d("Response", responseData);
                httpResult.setResult(WebServiceHelper.ServiceCallStatus.Success);
                httpResult.setResponseContent(responseData);
                return httpResult;
            } else {
                // Handle unsuccessful response
                assert response.body() != null;
                String responseData = response.body().string();
                Log.e("Response", "Error: " + response.code());
                Log.e("Response", "Body: " + responseData);
                httpResult.setResult(WebServiceHelper.ServiceCallStatus.Failed);
                httpResult.setResponseContent(responseData);
                return httpResult;
            }

        } catch (Exception e) {
            AndroidUtils.logMsg("HttpExecuteTask.doInBackground(): Exception " + e.getMessage());
            httpResult = new HttpResultDo();
            httpResult.setResult(WebServiceHelper.ServiceCallStatus.Exception);
            httpResult.setResponseContent(e.getMessage());
            return httpResult;
        } finally {

        }
    }

    protected void onPostExecute(HttpResultDo httpResult) {
        openCounter.decrementAndGet();
        httpResult.setRequestId(requestId);
        httpResult.setRequestType(requestType);
//        AndroidUtils.logMsg("HttpExecuteTask.onPostExecute() " + httpResult);
        super.onPostExecute(httpResult);
        try {
            Log.e("Response_Msg", httpResult.getResponseContent());
            if (httpResult.getStatus_code() == 401 && !(requestType.equals("Label")) && !(requestType.equals("auth"))
                    && !(requestType.equals("messages_rows")) && !(URL.contains(Constants.EMAIL_UPLOAD_URL)) && !(requestType.equals("Dashboard"))) {
                Intent in = new Intent(activity, LoginActivity.class);
                in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(in);
                AndroidUtils.showToast("Session expired, Login again", activity);
            } else if ((requestType.equals("Dashboard")) && (httpResult.getStatus_code() == 401)) {
                Constants.Valid_Token = false;
//                if (!token.isEmpty()) {
//                    Constants.loginActivity.Dashboard();
//                } else if (!email.isEmpty() && !password.isEmpty()) {
//                if (!Constants.Email.isEmpty() && !Constants.OLD_PASSWORD.isEmpty()) {
//                    Constants.loginActivity.Login(Constants.Email, Constants.OLD_PASSWORD);
//                } else {
                    Log.d("Session", "Session expired, Login again");
                    Intent nav_bio = new Intent(activity, LoginActivity.class);
                    activity.startActivity(nav_bio);
//                    AndroidUtils.showToast("Session expired, Login again", activity);
//                    Intent nav_bio = new Intent(activity, LoginActivity.class);
//                    activity.startActivity(nav_bio);
//                }
//                }
//                Intent nav_bio;
//                if (Constants.Biometric_checked) {
//                    nav_bio = new Intent(activity, biometric_page.class);
//                } else {
//                    nav_bio = new Intent(activity, LoginActivity.class);
//                }
//                activity.startActivity(nav_bio);
            } else
                Constants.Valid_Token = true;
            callback.onAsyncTaskComplete(httpResult);
        } catch (Exception e) {
            Log.d("Error_mg", Objects.requireNonNull(e.getMessage()));
            AndroidUtils.logMsg("HttpExecuteTask.onPostExecute() : Exception " + e.getMessage());
        }
    }
}
