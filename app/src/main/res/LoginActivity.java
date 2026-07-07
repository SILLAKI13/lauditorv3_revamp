package com.digicoffer.lauditor.LoginActivity;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatSpinner;

import com.digicoffer.lauditor.LoginActivity.Models.FirmsDo;
import com.digicoffer.lauditor.MainActivity;
import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;
import com.digicoffer.lauditor.CommonFiles.ChatService.ChatConnection;
import com.digicoffer.lauditor.CommonFiles.ChatService.ChatConnectionService;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.digicoffer.lauditor.CommonFiles.CommonAdapters.CommonSpinnerAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity implements AsyncTaskCompleteListener {

    TextInputEditText tet_email,tet_password;
    AppCompatButton bt_login;
    boolean isAllFieldsChecked = false;
    Dialog progress_dialog;
    Dialog ad_dialog;
    private static ChatConnection mConnection;
   private static ChatConnectionService chatConnectionService;
    TextView tv_forgot_password;
    boolean isRecursionEnable = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        tet_email = findViewById(R.id.et_login_email);
        tet_password = findViewById(R.id.et_login_password);
        tv_forgot_password = findViewById(R.id.tv_forgotPassword);
       bt_login = findViewById(R.id.bt_login);
//       mConnection = (ChatConnection) getCallingActivity()
//       chatConnectionService = (ChatConnectionService) getApplicationContext();
        tet_email.setText(Constants.email);
        tet_password.setText(Constants.password);
//        Login();
       bt_login.setPressed(true);
        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    isAllFieldsChecked = Validate();
                    if (isAllFieldsChecked){
                        Login();
                    }
                } catch (Exception e) {
                    e.fillInStackTrace();
                }
            }
        });

//        getActionBar().hide();
    }

    private void Login() {
        try {
            Constants.PROBIZ_TYPE = "PROFESSIONAL";
            Constants.base_URL = Constants.PROF_URL;
            JSONObject postData = new JSONObject();

            progress_dialog = AndroidUtils.get_progress(com.digicoffer.lauditor.LoginActivity.ViewModels.LoginActivity.this);
            postData.put("email", tet_email.getText().toString());
            postData.put("password", tet_password.getText().toString());
            WebServiceHelper.callHttpWebService(com.digicoffer.lauditor.LoginActivity.ViewModels.LoginActivity.this, com.digicoffer.lauditor.LoginActivity.ViewModels.LoginActivity.this, WebServiceHelper.RestMethodType.POST, "login", "LOGIN", postData.toString());
        } catch (Exception e) {
            if (progress_dialog != null && progress_dialog.isShowing())
                AndroidUtils.dismiss_dialog(progress_dialog);
        }
    }

    private boolean Validate(){
        if (tet_email.getText().toString().trim().length()==0){
            tet_email.setError("Email is required");
            return false;
        }
         if (tet_password.getText().toString().trim().length()==0){
            tet_password.setError("Password is required");
            return false;
        }
        return true;
    }


    @Override
    public void onClick(View view) {

    }
    private void firm_login(final ArrayList<FirmsDo> list) {
        MaterialAlertDialogBuilder builder = new  MaterialAlertDialogBuilder(com.digicoffer.lauditor.LoginActivity.ViewModels.LoginActivity.this,R.style.MaterialAlertDialog_Rounded);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogLayout = inflater.inflate(R.layout.firm_login, null);
        final AppCompatSpinner sp_firm =  dialogLayout.findViewById(R.id.sp_firm);
        CommonSpinnerAdapter<FirmsDo> adapter = new CommonSpinnerAdapter<>(this, list);
        sp_firm.setAdapter(adapter);
        final TextInputEditText et_firm_password = (TextInputEditText) dialogLayout.findViewById(R.id.et_login_password);
        Button bt_submit = (Button) dialogLayout.findViewById(R.id.bt_submit_firm_login);
//        Button bt_cancel = (Button) dialogLayout.findViewById(R.id.btn_cancel);
        final androidx.appcompat.app.AlertDialog dialog = builder.create();
        ad_dialog = dialog;
//        bt_cancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//            }
//        });
        bt_submit.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                JSONObject postData = new JSONObject();
                try {
                    if (Objects.requireNonNull(et_firm_password.getText()).toString().trim().equals("")) {
                        AndroidUtils.showToast("Password is mandatory", com.digicoffer.lauditor.LoginActivity.ViewModels.LoginActivity.this);
                    }
                    else {
                        progress_dialog = AndroidUtils.get_progress(com.digicoffer.lauditor.LoginActivity.ViewModels.LoginActivity.this);
                        int firm_position = sp_firm.getSelectedItemPosition();
                        postData.put("email", Objects.requireNonNull(tet_email.getText()).toString());
                        postData.put("userid", list.get(firm_position).getValue());
                        postData.put("password", et_firm_password.getText().toString());
                        WebServiceHelper.callHttpWebService(com.digicoffer.lauditor.LoginActivity.ViewModels.LoginActivity.this, com.digicoffer.lauditor.LoginActivity.ViewModels.LoginActivity.this, WebServiceHelper.RestMethodType.POST, "login", "LOGIN", postData.toString());
                    }
                } catch (Exception e) {
                    if (progress_dialog != null && progress_dialog.isShowing())
                        AndroidUtils.dismiss_dialog(progress_dialog);
                }
            }
        });

        dialog.setView(dialogLayout);
        dialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void save_xmpp_preference() {
//        String email = Objects.requireNonNull(et_Prof_Biz.getText()).toString().trim();

        SharedPreferences prefs = getDefaultSharedPreferences(getApplicationContext());
        String uid = Constants.UID;
        if (!Constants.ROLE.equalsIgnoreCase("admin"))
        {
            uid = uid + "_" + Constants.USER_ID;
        }

//        String existing_xmpp_jid = AndroidUtils.getSharedPreferenceStringData("xmpp_jid", this);
//        if (existing_xmpp_jid != null && !existing_xmpp_jid.equals(uid)) {
//            Intent i1 = new Intent(getApplicationContext(), ChatConnectionService.class);
//            stopService(i1);
//        }
        prefs.edit()
                .putString("xmpp_jid", uid)
                .putString("xmpp_password", Constants.TOKEN)
                .putBoolean("xmpp_logged_in", true)
                .apply();
//
        if (mConnection == null) {
            mConnection = new ChatConnection(this);
        }
        if (chatConnectionService == null) {
            chatConnectionService = new ChatConnectionService();
        }
        new JsonTask().execute(Constants.base_URL + "user/create/");

    }

    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {
        if (progress_dialog != null && progress_dialog.isShowing())
            AndroidUtils.dismiss_dialog(progress_dialog);
        if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                JSONObject result = new JSONObject(httpResult.getResponseContent());
                if (httpResult.getRequestType().equals("LOGIN")) {
                    if (!result.getBoolean("error")) {
                        JSONObject probiz_data = new JSONObject(result.getString("data"));
                        if (!probiz_data.getString("plan").toLowerCase().equals("lauditor")) {
//                            AndroidUtils.showToast("Account not found", this);
                           AndroidUtils.showToast("Account not found", com.digicoffer.lauditor.LoginActivity.ViewModels.LoginActivity.this);
                            return;
                        }
                        String email = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                            email = Objects.requireNonNull(tet_email.getText()).toString().trim();
                        }
                        String password = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                            password = Objects.requireNonNull(tet_password.getText()).toString().trim();
                        }
                        SharedPreferences prefs = getDefaultSharedPreferences(getApplicationContext());
                        prefs.edit()
                                .putString("email", email)
                                .putString("password", password)
                                .putBoolean("isLogin", true)
                                .putString("proBizType", Constants.PROBIZ_TYPE)
                                .apply();


                        Intent in = new Intent();
//                        in.putExtra("email", probiz_data.getString("email"));
//                        Constants.TOKEN = result.getString("token");
                        Constants.NAME = probiz_data.getString("name");
                        Constants.termsVersion = probiz_data.getString("termsVersion");
                        Constants.requiresTermsAcceptance = probiz_data.optBoolean("requiresTermsAcceptance");
                        Constants.USER_ID = probiz_data.getString("user_id");
                        Constants.UID = probiz_data.getString("uid");
                        Constants.IS_ADMIN = probiz_data.getBoolean("admin");
                        Constants.FIRM_NAME = probiz_data.getString("firm_name");
                        Constants.ROLE = probiz_data.getString("role");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            save_xmpp_preference();
                        }
                        Log.e("Constants.TOKEN",Constants.TOKEN);
                        AndroidUtils.showToast("Login Successful", this);
                        startActivity(new Intent(this, MainActivity.class));
                        if (ad_dialog != null && ad_dialog.isShowing())
                            ad_dialog.dismiss();

                    } else {
                        if (result.has("firms")) {
                            ArrayList<FirmsDo> list = new ArrayList<>();
                            FirmsDo firmsDo;
                            JSONObject firms = result.getJSONObject("firms");
                            JSONArray adminJsonArray = firms.getJSONArray("lauditor");
                            if (adminJsonArray.length() == 0) {
                                String msg = "Account not found";
                               AndroidUtils.showToast(msg, com.digicoffer.lauditor.LoginActivity.ViewModels.LoginActivity.this);
                            } else if (adminJsonArray.length() > 1) {
                                for (int i = 0; i < adminJsonArray.length(); i++) {
                                    JSONObject obj = adminJsonArray.getJSONObject(i);
                                    String userid = obj.getString("id");
                                    firmsDo = new FirmsDo();
                                    firmsDo.setName(obj.getString("firmName"));
                                    firmsDo.setValue(obj.getString("id"));
                                    list.add(firmsDo);
                                }
                                firm_login(list);
                            } else {
                                JSONObject obj = adminJsonArray.getJSONObject(0);
                                String user = obj.getString("id");
                                Iterator<String> iter = obj.keys();
                                JSONObject postData = new JSONObject();
                                postData.put("email", tet_email.getText().toString());
                                postData.put("userid", user);
                                postData.put("password", tet_password.getText().toString());
                                WebServiceHelper.callHttpWebService(com.digicoffer.lauditor.LoginActivity.ViewModels.LoginActivity.this, com.digicoffer.lauditor.LoginActivity.ViewModels.LoginActivity.this, WebServiceHelper.RestMethodType.POST, "login", "LOGIN", postData.toString());
                            }
                        } else {
                            String error_msg = result.has("plan") && result.getString("plan").equals("lauditor") ? String.valueOf(result.get("msg")) : "Account not found";
                            AndroidUtils.showToast(error_msg, com.digicoffer.lauditor.LoginActivity.ViewModels.LoginActivity.this);
//                            ((TextView) findViewById(R.id.tv_response)).setText(error_msg);
//                            AndroidUtils.showToast(error_msg, this);
                        }
                    }
                }

            } catch (Exception e) {
               AndroidUtils.showToast(e.getMessage(), com.digicoffer.lauditor.LoginActivity.ViewModels.LoginActivity.this);
            }
        } else {
            AndroidUtils.showToast(httpResult.getResponseContent(), com.digicoffer.lauditor.LoginActivity.ViewModels.LoginActivity.this);
//            ((TextView) findViewById(R.id.tv_response)).setText((httpResult.getResponseContent()));
        }
    }


    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                mConnection.connect();

            } catch (IOException | SmackException | XMPPException e) {
                Log.d("Chat Error", "Something went wrong while connecting ,make sure the credentials are right and try again");
                e.fillInStackTrace();
                //Stop the service all together.
                chatConnectionService.stopSelf();
            }
            return "";
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}