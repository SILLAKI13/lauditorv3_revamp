package com.digicoffer.lauditor.CommonFiles.GlobalFiles

import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.appcompat.app.AppCompatActivity
import com.digicoffer.lauditor.LoginActivity.ViewModels.LoginActivity
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

open class ForceUpdate : AppCompatActivity() {

    fun checkForUpdate(loginActivity: LoginActivity) {
        val appUpdateManager = AppUpdateManagerFactory.create(loginActivity)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            Log.d("Update", "Update info retrieved successfully.")
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE 
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                
                val launcher = activityResultLauncher
                if (launcher != null) {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        launcher,
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                    )
                }
            } else {
                Log.d("Update", "No update available or update type not allowed.")
            }
        }.addOnFailureListener { e ->
            Log.e("UpdateFailed", "Failed to retrieve update info: " + e.message)
            e.printStackTrace()
        }
    }

    companion object {
        @JvmStatic
        var activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>? = null
    }
}
