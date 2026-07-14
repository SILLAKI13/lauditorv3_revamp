package com.digicoffer.lauditor.CommonFiles.TokenManagerUtils

import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper

/**
 * Snapshot of an API call so it can be retried after token refresh.
 */
class RetryRequest(
    @JvmField val requestId: String?,
    @JvmField val methodType: WebServiceHelper.RestMethodType,
    @JvmField val fullUrl: String?,
    body: String?,
    @JvmField val requestType: String?
) {
    @JvmField val body: String = body ?: ""
}
