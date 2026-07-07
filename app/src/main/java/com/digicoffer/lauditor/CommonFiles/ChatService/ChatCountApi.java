package com.digicoffer.lauditor.CommonFiles.ChatService;

import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.VitacapeExtention;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.fromjid;
import static com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.getunreadcountlist_URL;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Write-only now. All read-side unread-count / last-message logic has moved
 * to ConversationMetaApi, which hits the single GET /conversation/meta
 * endpoint instead of this class's old POST getunreadcountlist + per-row
 * MAM queries.
 * <p>
 * resetunreadcount and updateunreadcount stay here as POSTs because
 * /conversation/meta is read-only (GET) and has no write/reset action.
 */
public class ChatCountApi implements AsyncTaskCompleteListener {

    Context context;

    public ChatCountApi(Context context) {
        this.context = context;
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {
        if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                if (httpResult.getRequestType().equalsIgnoreCase("UPDATE COUNT")) {
                    JSONObject result = new JSONObject(httpResult.getResponseContent());
                    String msg = result.getString("msg");
                    Log.d("Update count", msg);
                    // Read-side refresh now goes through ConversationMetaApi from
                    // wherever the adapter/screen needs updated counts.
                } else if (httpResult.getRequestType().equals("RESET COUNT")) {
                    JSONObject result = new JSONObject(httpResult.getResponseContent());
                    String msg = result.getString("msg");
                    Log.d("Reset count", msg);
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void callupdatecount() {
        // https://dev.utils.chat.digicoffer.com/api/v1/updateunreadcount
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("count", 1);
            jsonObject.put("fromjid", fromjid);
            jsonObject.put("message_id", Constants.message_id);
            jsonObject.put("tojid", Constants.UID + "_" + Constants.USER_ID + VitacapeExtention);
            WebServiceHelper.callHttpWebService(this, context, WebServiceHelper.RestMethodType.POST, getunreadcountlist_URL + "updateunreadcount", "UPDATE COUNT", jsonObject.toString());
            Log.d("Update_count_call", jsonObject.toString());
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    public void callResetUnreadCount(String fromjid) {
        // https://dev.utils.chat.digicoffer.com/api/v1/resetunreadcount
        // Left on the original POST endpoint - /conversation/meta is read-only
        // (GET) and has no reset/write action.
        JSONObject postData = new JSONObject();
        try {
            postData.put("fromjid", fromjid + VitacapeExtention);
            postData.put("tojid", Constants.UID + "_" + Constants.USER_ID + VitacapeExtention);
            WebServiceHelper.callHttpWebService(this, context, WebServiceHelper.RestMethodType.POST, Constants.getunreadcountlist_URL + "resetunreadcount", "RESET COUNT", postData.toString());
            Log.d("Reset_count_call", postData.toString());
        } catch (Exception e) {
            // if (progress_dialog != null && progress_dialog.isShowing())
            //     AndroidUtils.dismiss_dialog(progress_dialog);
        }
    }
}