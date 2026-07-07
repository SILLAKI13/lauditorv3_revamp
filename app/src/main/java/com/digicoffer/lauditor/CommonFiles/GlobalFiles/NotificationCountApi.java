package com.digicoffer.lauditor.CommonFiles.GlobalFiles;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.digicoffer.lauditor.Dashboard.DahboardModels.NotificationCountModel;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class NotificationCountApi implements AsyncTaskCompleteListener {

    Context context;
    public static ArrayList<NotificationCountModel> notificationList = new ArrayList<>();
    public static int unreadNotificationCount = 0;

    public NotificationCountApi(Context context) {
        this.context = context;
    }

    @Override
    public void onClick(View v) { }

    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {
        if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                if (httpResult.getRequestType().equalsIgnoreCase("GET NOTIFICATION COUNT")) {

                    JSONObject response = new JSONObject(httpResult.getResponseContent());
                    JSONObject dataObj = response.getJSONObject("data");
                    JSONArray notifications = dataObj.getJSONArray("notifications");

                    notificationList.clear();
                    unreadNotificationCount = 0;

                    for (int i = 0; i < notifications.length(); i++) {

                        JSONObject obj = notifications.getJSONObject(i);

                        NotificationCountModel model = new NotificationCountModel();
                        model.setId(obj.getString("id"));
                        model.setMessage(obj.getString("message"));
                        model.setTimestamp(obj.getString("timestamp"));
                        model.setStatus(obj.getString("status"));

                        notificationList.add(model);

                        // 🔥 Count only unread
                        if (model.getStatus().equalsIgnoreCase("unread")) {
                            unreadNotificationCount++;
                        }
                    }

                    Log.d("NOTIFY_API", "Unread Count: " + unreadNotificationCount);

                    // update UI badge
                    if (Constants.notifyBadge != null) {
                        if (unreadNotificationCount > 0) {
                            Constants.notifyBadge.setText(String.valueOf(unreadNotificationCount));
                        } else {
                            Constants.notifyBadge.setVisibility(View.GONE);
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 🔥 Call This to Fetch Notification List
    public void fetchNotificationCount() {
        WebServiceHelper.callHttpWebService(
                this,
                context,
                WebServiceHelper.RestMethodType.GET,
                "notification",
                "GET NOTIFICATION COUNT",
                ""
        );
    }
}

