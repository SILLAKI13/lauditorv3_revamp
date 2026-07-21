package com.digicoffer.lauditor.Webservice

import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import org.json.JSONArray
import org.json.JSONObject

class HttpResultDo {
    var requestId: String? = null
    var requestName: String?
        get() = requestId
        set(value) { requestId = value }
    var responseObject: JSONObject? = null
    var responseArray: JSONArray? = null
    var result: WebServiceHelper.ServiceCallStatus = WebServiceHelper.ServiceCallStatus.Pending
    var responseContent: String = ""
    var errorMessage: String = ""
    var requestType: String = ""
    var status_code: Int = 0

    override fun toString(): String {
        return ("HttpResultDO - requestId: " + requestId + " $$ ServiceCallStatus: " + result + " $$ errorMessage: "
                + errorMessage + " $$ responseContent: " + responseContent + " $$ responseObject: " + responseObject
                + " $$ responseArray: " + responseArray + " $$ requestType: " + requestType)
    }
}
