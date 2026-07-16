package com.digicoffer.lauditor.CommonFiles.ChatService

import android.content.Context
import android.util.Log
import android.view.View
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.Webservice.HttpResultDo
import org.json.JSONException
import org.json.JSONObject

class ChatCountApi(private val context: Context) : AsyncTaskCompleteListener {

    override fun onClick(view: View) {}

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                if (httpResult.requestType.equals("UPDATE COUNT", ignoreCase = true)) {
                    val result = JSONObject(httpResult.responseContent ?: "")
                    val msg = result.getString("msg")
                    Log.d("Update count", msg)
                } else if (httpResult.requestType.equals("RESET COUNT")) {
                    val result = JSONObject(httpResult.responseContent ?: "")
                    val msg = result.getString("msg")
                    Log.d("Reset count", msg)
                }
            } catch (e: JSONException) {
                throw RuntimeException(e)
            }
        }
    }

    fun callupdatecount() {
        try {
            val jsonObject = JSONObject()
            jsonObject.put("count", 1)
            jsonObject.put("fromjid", Constants.fromjid)
            jsonObject.put("message_id", Constants.message_id)
            jsonObject.put("tojid", Constants.UID + "_" + Constants.USER_ID + Constants.VitacapeExtention)
            WebServiceHelper.callHttpWebService(
                this,
                context,
                WebServiceHelper.RestMethodType.POST,
                Constants.getunreadcountlist_URL + "updateunreadcount",
                "UPDATE COUNT",
                jsonObject.toString()
            )
            Log.d("Update_count_call", jsonObject.toString())
        } catch (e: Exception) {
            e.fillInStackTrace()
        }
    }

    fun callResetUnreadCount(fromjid: String) {
        val postData = JSONObject()
        try {
            postData.put("fromjid", fromjid + Constants.VitacapeExtention)
            postData.put("tojid", Constants.UID + "_" + Constants.USER_ID + Constants.VitacapeExtention)
            WebServiceHelper.callHttpWebService(
                this,
                context,
                WebServiceHelper.RestMethodType.POST,
                Constants.getunreadcountlist_URL + "resetunreadcount",
                "RESET COUNT",
                postData.toString()
            )
            Log.d("Reset_count_call", postData.toString())
        } catch (e: Exception) {
            // Suppressed
        }
    }
}
