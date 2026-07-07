package com.digicoffer.lauditor.CommonFiles.TokenManagerUtils;

import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;

/**
 * Snapshot of an API call so it can be retried after token refresh.
 */
public class RetryRequest {

    public final String requestId;
    public final WebServiceHelper.RestMethodType methodType;
    public final String fullUrl;   // complete URL as passed to HttpExecuteTask
    public final String body;      // JSON body string (empty string for GET)
    public final String requestType;

    public RetryRequest(
            String requestId,
            WebServiceHelper.RestMethodType methodType,
            String fullUrl,
            String body,
            String requestType) {
        this.requestId = requestId;
        this.methodType = methodType;
        this.fullUrl = fullUrl;
        this.body = body != null ? body : "";
        this.requestType = requestType;
    }
}
