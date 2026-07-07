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

import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.HashMap;
import java.util.Map;

import com.digicoffer.lauditor.Webservice.core.RetrofitClient;
import com.digicoffer.lauditor.Webservice.core.ApiService;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

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

        try {
            String resolvedUrl = "";
            if (requestType.equals("Label")
                    || requestType.equals("auth")
                    || requestType.equals("messages_rows")
                    || URL.contains(Constants.EMAIL_UPLOAD_URL)
                    || requestType.startsWith("messages_attachment_")
                    || requestType.equals("CREATE_MEETING_LINK")) {
                resolvedUrl = URL;
            } else if ((requestType.equals("Get DocEditor List")) || (requestType.equals("Delete DocEditor List")) || (requestType.equals("Save Document")) || (requestType.equals("Save As Document")) || (requestType.equals("Save DocEditor")) || (requestType.equals("Open Document"))) {
                resolvedUrl = URL;
            } else if ((requestType.equals("GET UNREAD COUNT")) || (requestType.equals("RESET COUNT")) || (requestType.equals("UPDATE COUNT") || (requestType.equals("FCM_TOKEN_LOGOUT") || (requestType.equals("FCM_TOKEN_UPDATE"))))) {
                resolvedUrl = URL;
            } else if (restMethodType == WebServiceHelper.RestMethodType.GET && requestType.equals("Other Doc View")) {
                resolvedUrl = URL;
            } else {
                resolvedUrl = Constants.base_URL + URL;
            }

            Log.e("URL", ":" + resolvedUrl);

            // Build dynamic headers map
            Map<String, String> headers = new HashMap<>();
            headers.put("Accept", "application/json");

            if (!requestType.equals("LOGIN") && !requestType.equals("ONBOARD") && !requestType.equals("RESEND_OTP") && !requestType.equals("SWITCH_FIRM") && !requestType.equals("SIGNUP") && !requestType.equals("UPDATE") && !requestType.equals("LOGIN_PASSWORD") && !requestType.equals("FORGET_PASSWORD") && !requestType.equals("VERIFY_TOKEN") && !requestType.equals("GET UNREAD COUNT") && !requestType.equals("REFRESH_TOKEN_INTERNAL") && !requestType.equals("RESET COUNT") && !requestType.equals("UPDATE COUNT")) {
                headers.put("Authorization", "Bearer " + Constants.TOKEN);
                Log.d("Token", ":" + "Bearer " + (Constants.TOKEN));
            }

            if (requestType.equals("Get DocEditor List")
                    || requestType.equals("Delete DocEditor List")
                    || requestType.equals("Save Document")
                    || requestType.equals("Save As Document")
                    || requestType.equals("Save DocEditor")
                    || requestType.equals("Open Document")) {
                headers.put("Cofferid", Constants.USER_ID);
            }

            if (requestType.equals("GET UNREAD COUNT")
                    || requestType.equals("RESET COUNT")
                    || requestType.equals("UPDATE COUNT")
                    || requestType.equals("FCM_TOKEN_LOGOUT")
                    || requestType.equals("FCM_TOKEN_UPDATE")) {
                headers.put("Content-Type", "application/json");
            }

            if (requestType.equals("Other Doc View") || requestType.equals("Decrypt Doc")) {
                headers.put("x-client-type", "mobile");
            }

            // Build RequestBody
            RequestBody requestBody = null;
            if (restMethodType == WebServiceHelper.RestMethodType.POST
                    || restMethodType == WebServiceHelper.RestMethodType.PUT
                    || restMethodType == WebServiceHelper.RestMethodType.PATCH
                    || restMethodType == WebServiceHelper.RestMethodType.DELETE) {
                
                if (restMethodType == WebServiceHelper.RestMethodType.POST && requestType.equals("Decrypt Doc")) {
                    MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                    JSONObject json = new JSONObject(lastBody);
                    Iterator<String> keys = json.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        String value = json.optString(key);
                        builder.addFormDataPart(key, value);
                    }
                    requestBody = builder.build();
                } else if (restMethodType == WebServiceHelper.RestMethodType.DELETE && lastBody.isEmpty()) {
                    requestBody = null;
                } else {
                    MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
                    requestBody = RequestBody.create(mediaType, lastBody);
                }
            }

            ApiService apiService = RetrofitClient.INSTANCE.getApiService();
            Call<ResponseBody> call;

            switch (restMethodType) {
                case GET:
                    call = apiService.executeGet(resolvedUrl, headers);
                    break;
                case POST:
                    call = apiService.executePost(resolvedUrl, headers, requestBody);
                    break;
                case PUT:
                    call = apiService.executePut(resolvedUrl, headers, requestBody);
                    break;
                case PATCH:
                    call = apiService.executePatch(resolvedUrl, headers, requestBody);
                    break;
                case DELETE:
                    if (requestBody != null) {
                        call = apiService.executeDeleteWithBody(resolvedUrl, headers, requestBody);
                    } else {
                        call = apiService.executeDelete(resolvedUrl, headers);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported method: " + restMethodType);
            }

            Response<ResponseBody> response = call.execute();
            int status_code = response.code();
            Log.i("status_code:", String.valueOf(status_code));
            httpResult.setStatus_code(status_code);

            String responseContent = "";
            if (response.isSuccessful()) {
                if (response.body() != null) {
                    responseContent = response.body().string();
                }
                httpResult.setResult(WebServiceHelper.ServiceCallStatus.Success);
                httpResult.setResponseContent(responseContent);
            } else {
                if (response.errorBody() != null) {
                    responseContent = response.errorBody().string();
                }
                if (status_code == 400) {
                    httpResult.setResult(WebServiceHelper.ServiceCallStatus.Success);
                } else if (status_code == 502) {
                    httpResult.setResult(WebServiceHelper.ServiceCallStatus.Pending);
                    responseContent = "502 Bad Gateway";
                } else {
                    httpResult.setResult(WebServiceHelper.ServiceCallStatus.Failed);
                }
                httpResult.setResponseContent(responseContent);
            }

        } catch (IOException e) {
            httpResult.setResult(WebServiceHelper.ServiceCallStatus.Exception);
            httpResult.setErrorMessage("Exception: " + e.getMessage());
            AndroidUtils.logMsg("WebServiceHelper.callWebService(): IO Exception " + e.getMessage());
        } catch (Exception e) {
            AndroidUtils.logMsg("HttpExecuteTask.doInBackground(): Exception " + e.getMessage());
            httpResult.setResult(WebServiceHelper.ServiceCallStatus.Exception);
            httpResult.setResponseContent(e.getMessage());
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

