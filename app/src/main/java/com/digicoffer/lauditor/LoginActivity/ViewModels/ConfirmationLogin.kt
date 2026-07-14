package com.digicoffer.lauditor.LoginActivity.ViewModels

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import org.json.JSONObject

class ConfirmationLogin : AppCompatActivity(), View.OnClickListener, AsyncTaskCompleteListener {
    private var btConfirm: Button? = null
    private var btCancel: Button? = null
    private var progressDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
    }

    override fun onClick(v: View) {
        // Commented out in original Java code
    }

    private fun callWebservice() {
        progressDialog = AndroidUtils.get_progress(this)
        val postData = JSONObject()
        try {
            postData.put("uid", Constants.UID)
            postData.put("status", "approved")
            WebServiceHelper.callHttpWebService(
                this,
                this,
                WebServiceHelper.RestMethodType.POST,
                "mfa-status",
                "STATUS",
                postData.toString()
            )
        } catch (e: Exception) {
            if (progressDialog != null && progressDialog!!.isShowing) {
                AndroidUtils.dismiss_dialog(progressDialog)
            }
        }
    }

    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
        if (progressDialog != null && progressDialog!!.isShowing) {
            AndroidUtils.dismiss_dialog(progressDialog)
        }
        if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val result = JSONObject(httpResult.responseContent)
                    if (!result.getBoolean("error")) {
                        onBackPressed()
                    } else {
                        AndroidUtils.showAlert(httpResult.responseContent, this)
                    }
                } catch (e: Exception) {
                    e.message
                }
            } else {
                AndroidUtils.showAlert(httpResult.responseContent, this)
            }
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        finish()
    }
}
