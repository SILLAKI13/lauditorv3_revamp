package com.digicoffer.lauditor.CommonFiles.GlobalFiles

import android.content.Context
import android.util.Log
import android.view.View
import com.digicoffer.lauditor.Dashboard.DahboardModels.NotificationCountModel
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.Webservice.HttpResultDo
import org.json.JSONArray
import org.json.JSONObject
import java.util.ArrayList

class NotificationCountApi(private val context: Context) : AsyncTaskCompleteListener {

    override fun onClick(v: View) {}

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                if (httpResult.requestType.equals("GET NOTIFICATION COUNT", ignoreCase = true)) {
                    val response = JSONObject(httpResult.responseContent ?: "")
                    val dataObj = response.getJSONObject("data")
                    val notifications = dataObj.getJSONArray("notifications")

                    notificationList.clear()
                    unreadNotificationCount = 0

                    for (i in 0 until notifications.length()) {
                        val obj = notifications.getJSONObject(i)

                        val model = NotificationCountModel()
                        model.id = obj.getString("id")
                        model.message = obj.getString("message")
                        model.timestamp = obj.getString("timestamp")
                        model.status = obj.getString("status")

                        notificationList.add(model)

                        if (model.status.equals("unread", ignoreCase = true)) {
                            unreadNotificationCount++
                        }
                    }

                    Log.d("NOTIFY_API", "Unread Count: $unreadNotificationCount")

                    val badge = Constants.notifyBadge
                    if (badge != null) {
                        if (unreadNotificationCount > 0) {
                            badge.text = unreadNotificationCount.toString()
                        } else {
                            badge.visibility = View.GONE
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fetchNotificationCount() {
        WebServiceHelper.callHttpWebService(
            this,
            context,
            WebServiceHelper.RestMethodType.GET,
            "notification",
            "GET NOTIFICATION COUNT",
            ""
        )
    }

    companion object {
        @JvmField
        val notificationList = ArrayList<NotificationCountModel>()

        @JvmField
        var unreadNotificationCount = 0
    }
}
