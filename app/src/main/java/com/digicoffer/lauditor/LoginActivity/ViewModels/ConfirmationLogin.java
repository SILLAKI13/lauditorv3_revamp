package com.digicoffer.lauditor.LoginActivity.ViewModels;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
//

import org.json.JSONObject;

public class ConfirmationLogin extends AppCompatActivity implements View.OnClickListener, AsyncTaskCompleteListener {
    Button bt_Confirm, bt_Cancel;
    Dialog progress_dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
//        updateXML();
    }

//    public void updateXML() {
//        bt_Confirm = (Button) findViewById(R.id.but_confirm);
//        bt_Confirm.setOnClickListener(this);
//        bt_Cancel = (Button) findViewById(R.id.but_cancel);
//        bt_Cancel.setOnClickListener(this);
//    }

    @Override
    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.but_confirm:
//                callWebservice();
//                break;
//            case R.id.but_cancel:
//                onBackPressed();
//                break;
//        }

    }

    private void callWebservice() {
        progress_dialog = AndroidUtils.get_progress(this);
        JSONObject postData = new JSONObject();
        try {
            postData.put("uid", Constants.UID);
            postData.put("status", "approved");
            WebServiceHelper.callHttpWebService(this, this, WebServiceHelper.RestMethodType.POST, "mfa-status", "STATUS", postData.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
        }
    }

    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {
        if (progress_dialog != null && progress_dialog.isShowing())
            AndroidUtils.dismiss_dialog(progress_dialog);
        if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                JSONObject result = new JSONObject(httpResult.getResponseContent());
                if (!result.getBoolean("error")) {
                    onBackPressed();
                } else {
                    AndroidUtils.showAlert(httpResult.getResponseContent(), this);
                }
            } catch (Exception e) {
                e.getMessage();
            }

        } else {
            AndroidUtils.showAlert(httpResult.getResponseContent(), this);
        }

    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
