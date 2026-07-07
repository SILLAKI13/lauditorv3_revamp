package com.digicoffer.lauditor.Webservice.CommonApiHelper;

import android.content.Context;

import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.DownloadHelper.DownloadFileFromURL;
import com.digicoffer.lauditor.Webservice.DownloadHelper.DownloadFlie;

import java.io.File;

public class WebServiceHelper  {

    public enum ServiceCallStatus {
        Pending, Sent, Success, Failed, Exception, ConcurrencyError
    }

    public enum RestMethodType {
        GET, POST, PUT, DELETE,PATCH
    }


    public static String callHttpWebService(AsyncTaskCompleteListener callback, Context activity, RestMethodType restMethodType, String url, String requestType, String... formParams) {
        String requestId = "";
        new HttpExecuteTask(requestId, false, restMethodType, url, callback, activity, requestType).execute(formParams);
        return requestId;
    }

    public static String callEmailHttpWebService(AsyncTaskCompleteListener callback, Context activity, RestMethodType restMethodType, String url, String requestType, String... formParams) {
        String requestId = "";
        new MultiPartHttpExecute(requestId, false, restMethodType, url, callback, activity, requestType).execute(formParams);
        return requestId;
    }

    public static String callHttpUploadWebService(AsyncTaskCompleteListener callback, Context activity, RestMethodType restMethodType, String url, String requestType, File file, String... formParams) {
        String requestId = "";
        new UploadImageTask(file, false, restMethodType, url, callback, activity, requestType).execute(formParams);
        return requestId;
    }
//
    public static String callHttpDownloadFileWebService(AsyncTaskCompleteListener callback, Context activity, String requestType, String url, String fileName) {
        String requestId = "";
        new DownloadFlie(callback, activity, requestType, fileName).execute(url);
        return requestId;
    }
    public static String callHttpDownloadNewFileWebService(AsyncTaskCompleteListener callback, Context activity, String requestType, String url, String fileName) {
        String requestId = "";
        new DownloadFileFromURL(callback, activity, requestType, fileName).execute(url);
        return requestId;
    }

//    public static String callHttpRequestHeaders(AsyncTaskCompleteListener callback, Context activity, String url) {
//        String requestId = "";
//        new GetHeadersValues(callback, activity, url).execute(url);
//        return requestId;
//    }

}
