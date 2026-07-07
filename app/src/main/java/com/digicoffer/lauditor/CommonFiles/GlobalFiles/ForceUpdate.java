package com.digicoffer.lauditor.CommonFiles.GlobalFiles;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import androidx.activity.result.ActivityResultLauncher;

import com.digicoffer.lauditor.LoginActivity.ViewModels.LoginActivity;
import com.google.android.gms.tasks.Task;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.appupdate.AppUpdateOptions;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;

public class ForceUpdate extends AppCompatActivity {
    private static ActivityResultLauncher activityResultLauncher;

//    public void checkForUpdate(LoginActivity loginActivity) {
//        // Check if there is an update available
//        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(loginActivity);
//
//// Returns an intent object that you use to check for an update.
//        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
//
//// Checks that the platform will allow the specified type of update.
//        try {
//            appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
//                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
//                    appUpdateManager.startUpdateFlowForResult(
//                            // Pass the intent that is returned by 'getAppUpdateInfo()'.
//                            appUpdateInfo,
//                            // an activity result launcher registered via registerForActivityResult
//                            activityResultLauncher,
//                            // Or pass 'AppUpdateType.FLEXIBLE' to newBuilder() for
//                            // flexible updates.
//                            AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build());
//                    // Request the update.
//                }
//            });
//            activityResultLauncher = registerForActivityResult(
//                    new ActivityResultContracts.StartIntentSenderForResult(),
//                    new ActivityResultCallback<ActivityResult>() {
//                        @Override
//                        public void onActivityResult(ActivityResult result) {
//                            // handle callback
//                            if (result.getResultCode() != RESULT_OK) {
//                                Log.d("Update", "Update flow failed! Result code: " + result.getResultCode());
//                                // If the update is canceled or fails,
//                                // you can request to start the update again.
//                            }
//                        }
//                    });
//        } catch (Exception e) {
//            e.fillInStackTrace();
//            Log.d("exception...",e.getMessage());
//        }
//    }
public void checkForUpdate(LoginActivity loginActivity) {
    AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(loginActivity);
    Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

    appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
        Log.d("Update", "Update info retrieved successfully.");
        if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
            appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    activityResultLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build());
        } else {
            Log.d("Update", "No update available or update type not allowed.");
        }
    }).addOnFailureListener(e -> {
        Log.e("UpdateFailed", "Failed to retrieve update info: " + e.getMessage());
        e.printStackTrace();  // Print the stack trace for more details
    });
}

}
