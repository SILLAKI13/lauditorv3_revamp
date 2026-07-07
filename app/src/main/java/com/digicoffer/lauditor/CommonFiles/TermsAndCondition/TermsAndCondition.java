package com.digicoffer.lauditor.CommonFiles.TermsAndCondition;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;

import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener;
import com.digicoffer.lauditor.Webservice.HttpResultDo;
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper;

import org.json.JSONException;
import org.json.JSONObject;

public class TermsAndCondition implements AsyncTaskCompleteListener {
    Context context;
    private Dialog progress_dialog;
    private TermsAndConditionsDialog termsDialog;
    private OnTermsAndConditionListener conditionListener;
    private String currentVersion;
    private boolean currentRequiresAcceptance;

    public interface OnTermsAndConditionListener {
        void onTermsAccepted();

        void onTermsDeclined();

        void onTermsCheckComplete(boolean needsToShow);
    }

    public TermsAndCondition(Context context) {
        this.context = context;
        this.termsDialog = TermsAndConditionsDialog.getInstance();
    }

    public void setOnTermsAndConditionListener(OnTermsAndConditionListener listener) {
        this.conditionListener = listener;
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onAsyncTaskComplete(HttpResultDo httpResult) {
        if (httpResult.getResult() == WebServiceHelper.ServiceCallStatus.Success) {
            try {
                if (progress_dialog != null && progress_dialog.isShowing()) {
                    AndroidUtils.dismiss_dialog(progress_dialog);
                }
                if (httpResult.getRequestType().equalsIgnoreCase("Get TC")) {
                    JSONObject result = new JSONObject(httpResult.getResponseContent());
                    if (!result.optBoolean("error")) {
                        JSONObject jsonObject = result.optJSONObject("data");
                        String url = jsonObject.optString("url");
                        String version = jsonObject.optString("version");
                        String msg = jsonObject.optString("msg");

                        // Check if we need to show terms based on version
                        checkAndShowTermsDialog(url, version, msg);

                    } else {
                        AndroidUtils.showValidationALert(
                                "Alert",
                                String.valueOf(result.get("msg")),
                                context
                        );
                        if (conditionListener != null) {
                            conditionListener.onTermsCheckComplete(false);
                        }
                    }
                } else if (httpResult.getRequestType().equals("Accept TC")) {
                    JSONObject result = new JSONObject(httpResult.getResponseContent());
                    String msg = result.getString("msg");
                    Log.d("Terms Accept", msg);
                    AndroidUtils.showAlert(msg, (Activity) context);

                    saveAcceptedTermsVersion(currentVersion);
                    Constants.requiresTermsAcceptance = false;

                    // ── FIX: update requiresTermsAcceptance in MyPrefs AND in Json_key ──
                    // Bio_metric_access() saves Json_key which contains requiresTermsAcceptance=true
                    // from the server. On next login restore, this overwrites our fix.
                    // We must patch Json_key directly here.
                    try {
                        SharedPreferences myPrefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                        myPrefs.edit().putBoolean("requiresTermsAcceptance", false).apply();

                        // Also patch the Json_key JSON so restore doesn't reload requiresTermsAcceptance=true
                        String savedJson = myPrefs.getString("Json_key", "");
                        if (!savedJson.isEmpty()) {
                            JSONObject jsonKey = new JSONObject(savedJson);
                            jsonKey.put("requiresTermsAcceptance", false);
                            myPrefs.edit().putString("Json_key", jsonKey.toString()).apply();
                        }
                    } catch (Exception e) {
                        Log.e("TermsAccept", "Failed to patch Json_key: " + e.getMessage());
                    }

                    if (termsDialog != null) termsDialog.dismiss();

                    if (conditionListener != null) {
                        conditionListener.onTermsAccepted();
                    }
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        } else {
            if (conditionListener != null) {
                conditionListener.onTermsCheckComplete(false);
            }
        }
    }

    private void checkAndShowTermsDialog(String pdfUrl, String newVersion, String message) {
        boolean isVersionNoneOrNull = (currentVersion == null
                || currentVersion.isEmpty()
                || currentVersion.equalsIgnoreCase("none"));

        // ── FIX: read locally saved accepted version from SharedPreferences ──
        // NOT currentVersion (which is the server version — comparing it to itself is always equal)
        String acceptedVersion = Constants.termsVersion;

        Log.d("TermsCheck", "Server version: " + newVersion);
        Log.d("TermsCheck", "Locally accepted version: " + acceptedVersion);
        Log.d("TermsCheck", "requiresTermsAcceptance: " + currentRequiresAcceptance);

        boolean needsToShow = false;

        if (isVersionNoneOrNull) {
            needsToShow = currentRequiresAcceptance;
        } else if (acceptedVersion == null || acceptedVersion.isEmpty()
                || acceptedVersion.equalsIgnoreCase("none")) {
            needsToShow = true;
        } else {
            int comparison = compareVersions(newVersion, acceptedVersion);
            if (comparison > 0) {
                needsToShow = true;
            } else if (comparison == 0) {
                needsToShow = currentRequiresAcceptance;
            } else {
                needsToShow = false;
            }
        }

        if (needsToShow) {
            showTermsDialog(pdfUrl, newVersion, message);
        } else {
            if (conditionListener != null) {
                conditionListener.onTermsCheckComplete(false);
            }
        }
    }
//    private void checkAndShowTermsDialog(String pdfUrl, String newVersion, String message) {
//        // Check if version is "none", "None", or null
//        boolean isVersionNoneOrNull = (currentVersion == null || currentVersion.isEmpty() || currentVersion.equalsIgnoreCase("none"));
//
//        Log.d("TermsCheck", "New version from server: " + newVersion);
//        Log.d("TermsCheck", "Is version none/null: " + isVersionNoneOrNull);
//        Log.d("TermsCheck", "currentRequiresAcceptance: " + currentRequiresAcceptance);
//
//        // Get stored accepted version from SharedPreferences
//        String acceptedVersion = currentVersion;
//
//        // Check if terms need to be shown
//        boolean needsToShow = false;
//
//        if (isVersionNoneOrNull) {
//            // If version is "none" or null, just check the boolean flag
//            if (currentRequiresAcceptance) {
//                needsToShow = true;
//                Log.d("TermsCheck", "Version is none/null and requiresTermsAcceptance is true - need to show terms");
//            } else {
//                needsToShow = false;
//                Log.d("TermsCheck", "Version is none/null and requiresTermsAcceptance is false - no need to show terms");
//            }
//        } else if (acceptedVersion == null || acceptedVersion.isEmpty()) {
//            // User has never accepted terms
//            needsToShow = true;
//            Log.d("TermsCheck", "No accepted version found in SharedPreferences - need to show terms");
//        } else {
//            // Handle "none" or "None" for accepted version
//            String cleanAcceptedVersion = acceptedVersion;
//            if (acceptedVersion.equalsIgnoreCase("none")) {
//                cleanAcceptedVersion = null;
//            }
//
//            if (cleanAcceptedVersion == null) {
//                needsToShow = true;
//                Log.d("TermsCheck", "Accepted version is none - need to show terms");
//            } else {
//                // Remove 'v' or 'V' prefix for comparison
//                String cleanNewVersion = newVersion.replaceAll("^[vV]", "");
//                String cleanAccepted = cleanAcceptedVersion.replaceAll("^[vV]", "");
//
//                // Compare versions
//                int comparison = compareVersions(cleanNewVersion, cleanAccepted);
//                Log.d("TermsCheck", "Version comparison result: " + comparison);
//
//                if (comparison > 0) {
//                    // New version is higher - need to show terms
//                    needsToShow = true;
//                    Log.d("TermsCheck", "New version is higher - need to show terms");
//                } else if (comparison == 0) {
//                    // Same version - check requiresTermsAcceptance flag
//                    if (currentRequiresAcceptance) {
//                        needsToShow = true;
//                        Log.d("TermsCheck", "Same version but requiresTermsAcceptance is true - need to show terms");
//                    } else {
//                        needsToShow = false;
//                        Log.d("TermsCheck", "Same version and requiresTermsAcceptance is false - no need to show terms");
//                    }
//                } else {
//                    // New version is lower (should not happen normally)
//                    needsToShow = false;
//                    Log.d("TermsCheck", "New version is lower than accepted - no need to show terms");
//                }
//            }
//        }
//
//        if (needsToShow) {
//            // Show terms dialog
//            showTermsDialog(pdfUrl, newVersion, message);
//        } else {
//            // Terms already accepted for this version
//            if (conditionListener != null) {
//                conditionListener.onTermsCheckComplete(false);
//            }
//        }
//    }

//    private void showTermsDialog(String pdfUrl, String version, String message) {
//        if (context == null || pdfUrl == null || pdfUrl.isEmpty()) {
//            AndroidUtils.showAlert("Terms and Conditions not available", (android.app.Activity) context);
//            if (conditionListener != null) {
//                conditionListener.onTermsCheckComplete(false);
//            }
//            return;
//        }
//
//        termsDialog.show(context, pdfUrl, version, message, new TermsAndConditionsDialog.OnTermsActionListener() {
//            @Override
//            public void onAccept(String versionName) {
//                // User accepted - call accept API
//                callAcceptTC(versionName);
//            }
//
//            @Override
//            public void onDecline() {
//                // User declined.
//                if (conditionListener != null) {
//                    conditionListener.onTermsDeclined();
//                }

    /// /                AndroidUtils.showAlert("You must accept the Terms and Conditions to continue using the app.",
    /// /                        (android.app.Activity) context);
//            }
//        });
//    }
    private void showTermsDialog(String pdfUrl, String version, String message) {
        if (context == null || pdfUrl == null || pdfUrl.isEmpty()) {
            if (conditionListener != null) conditionListener.onTermsCheckComplete(false);
            return;
        }
        // ── Guard against showing twice (singleton dialog) ──
        if (termsDialog != null && termsDialog.isShowing()) {
            Log.d("TermsCheck", "Dialog already visible — skipping");
            return;
        }
        termsDialog.show(context, pdfUrl, version, message, new TermsAndConditionsDialog.OnTermsActionListener() {
            @Override
            public void onAccept(String versionName) {
                callAcceptTC(versionName);
            }

            @Override
            public void onDecline() {
                if (conditionListener != null) conditionListener.onTermsDeclined();
            }
        });
    }

    private int compareVersions(String version1, String version2) {
        // Handle null or empty values
        if (version1 == null || version1.isEmpty()) version1 = "0";
        if (version2 == null || version2.isEmpty()) version2 = "0";

        if (version1.equals(version2)) return 0;
        if (version1 == null && version2 == null) return 0;
        if (version1 == null) return -1;
        if (version2 == null) return 1;

        // Remove 'v' or 'V' prefix if present
        version1 = version1.replaceAll("^[vV]", "");
        version2 = version2.replaceAll("^[vV]", "");

        // Split by dot
        String[] v1Parts = version1.split("\\.");
        String[] v2Parts = version2.split("\\.");

        int maxLength = Math.max(v1Parts.length, v2Parts.length);

        for (int i = 0; i < maxLength; i++) {
            double v1Part = 0;
            double v2Part = 0;

            if (i < v1Parts.length) {
                try {
                    v1Part = Double.parseDouble(v1Parts[i]);
                } catch (NumberFormatException e) {
                    v1Part = 0;
                }
            }

            if (i < v2Parts.length) {
                try {
                    v2Part = Double.parseDouble(v2Parts[i]);
                } catch (NumberFormatException e) {
                    v2Part = 0;
                }
            }

            if (v1Part > v2Part) return 1;
            if (v1Part < v2Part) return -1;
        }

        return 0;
    }

    private void saveAcceptedTermsVersion(String version) {
        SharedPreferences prefs = context.getSharedPreferences("terms_prefs", Context.MODE_PRIVATE);
        prefs.edit().putString("accepted_terms_version", version).apply();
    }

    private String getAcceptedTermsVersion() {
        SharedPreferences prefs = context.getSharedPreferences("terms_prefs", Context.MODE_PRIVATE);
        return prefs.getString("accepted_terms_version", null);
    }

    public void callAcceptTC(String versionName) {
        Constants.PROBIZ_TYPE = "PROFESSIONAL";
        Constants.base_URL = Constants.PROF_URL;
        JSONObject postData = new JSONObject();
        progress_dialog = AndroidUtils.get_progress((Activity) context);
        try {
            postData.put("version", versionName);
            WebServiceHelper.callHttpWebService(this, context, WebServiceHelper.RestMethodType.POST, "terms/accept", "Accept TC", postData.toString());
            Log.d("Accept_TC", postData.toString());
        } catch (Exception e) {
            e.printStackTrace();
            AndroidUtils.showToast("Error accepting terms", context);
            if (progress_dialog != null && progress_dialog.isShowing()) {
                AndroidUtils.dismiss_dialog(progress_dialog);
            }
        }
    }

    public void callGetTC() {
        try {
            Log.d("TermsAndCondition", "callGetTC - Making API call to fetch terms");
            Constants.PROBIZ_TYPE = "PROFESSIONAL";
            Constants.base_URL = Constants.PROF_URL;
            JSONObject jsonObject = new JSONObject();
            WebServiceHelper.callHttpWebService(this, context, WebServiceHelper.RestMethodType.GET, "terms", "Get TC", jsonObject.toString());
            Log.d("Get_TC", "Calling get terms API");
        } catch (Exception e) {
            e.fillInStackTrace();
            Log.e("TermsAndCondition", "Error in callGetTC: " + e.getMessage());
            if (conditionListener != null) {
                conditionListener.onTermsCheckComplete(false);
            }
        }
    }

    //    public void checkTermsWithData(String serverVersion, boolean requiresTermsAcceptance) {
//        Log.d("TermsAndCondition", "checkTermsWithData called - Version: " + serverVersion + ", Requires: " + requiresTermsAcceptance);
//
//        this.currentVersion = serverVersion;
//        this.currentRequiresAcceptance = requiresTermsAcceptance;
//
//        // Check if version is "none", "None", or null
//        boolean isVersionNoneOrNull = (serverVersion == null || serverVersion.isEmpty() || serverVersion.equalsIgnoreCase("none"));
//
//        // Get stored accepted version
//        String acceptedVersion = currentVersion;
//        Log.d("TermsAndCondition", "Accepted version: " + acceptedVersion);
//
//        // Check if terms need to be shown
//        boolean needsToShow = false;
//
//        if (isVersionNoneOrNull) {
//            // If version is "none" or null, just check the boolean flag
//            if (requiresTermsAcceptance) {
//                needsToShow = true;
//                Log.d("TermsAndCondition", "Version is none/null and requiresTermsAcceptance is true - need to show terms");
//            } else {
//                needsToShow = false;
//                Log.d("TermsAndCondition", "Version is none/null and requiresTermsAcceptance is false - no need to show terms");
//            }
//        } else if (acceptedVersion == null || acceptedVersion.isEmpty()) {
//            needsToShow = true;
//            Log.d("TermsAndCondition", "No accepted version found - need to show terms");
//        } else {
//            // Handle "none" or "None" for accepted version
//            String cleanAcceptedVersion = acceptedVersion;
//            if (acceptedVersion.equalsIgnoreCase("none")) {
//                cleanAcceptedVersion = null;
//            }
//
//            if (cleanAcceptedVersion == null) {
//                needsToShow = true;
//                Log.d("TermsAndCondition", "Accepted version is none - need to show terms");
//            } else {
//                int comparison = compareVersions(serverVersion, acceptedVersion);
//                Log.d("TermsAndCondition", "Version comparison result: " + comparison);
//
//                if (comparison > 0) {
//                    needsToShow = true;
//                    Log.d("TermsAndCondition", "New version is higher - need to show terms");
//                } else if (comparison == 0) {
//                    if (requiresTermsAcceptance) {
//                        needsToShow = true;
//                        Log.d("TermsAndCondition", "Same version but requires acceptance - need to show terms");
//                    } else {
//                        needsToShow = false;
//                        Log.d("TermsAndCondition", "Same version and no acceptance required - skip");
//                    }
//                } else {
//                    needsToShow = false;
//                    Log.d("TermsAndCondition", "Version is lower than accepted - skip");
//                }
//            }
//        }
//
//        if (needsToShow) {
//            Log.d("TermsAndCondition", "Calling callGetTC() to fetch PDF");
//            callGetTC();
//        } else {
//            Log.d("TermsAndCondition", "No need to show terms, notifying listener");
//            if (conditionListener != null) {
//                conditionListener.onTermsCheckComplete(false);
//            }
//        }
//    }
    public void checkTermsWithData(String serverVersion, boolean requiresTermsAcceptance) {
        this.currentVersion = serverVersion;
        this.currentRequiresAcceptance = requiresTermsAcceptance;

        boolean isVersionNoneOrNull = (serverVersion == null
                || serverVersion.isEmpty()
                || serverVersion.equalsIgnoreCase("none"));

        String acceptedVersion = getAcceptedTermsVersion();

        Log.d("TermsAndCondition", "Server version: " + serverVersion
                + " | Locally accepted: " + acceptedVersion
                + " | requiresAcceptance: " + requiresTermsAcceptance);

        boolean needsToShow;

        if (isVersionNoneOrNull) {
            // No version to compare against — fall back to the API flag
            needsToShow = requiresTermsAcceptance;
        } else if (acceptedVersion == null || acceptedVersion.isEmpty()
                || acceptedVersion.equalsIgnoreCase("none")) {
            // Never accepted any version on this device — must show
            needsToShow = true;
        } else {
            // We HAVE a concrete locally-accepted version — trust the version
            // comparison alone. If serverVersion <= acceptedVersion, the user
            // is current, full stop — don't let a stale requiresTermsAcceptance
            // flag from the login API re-trigger the dialog.
            int comparison = compareVersions(serverVersion, acceptedVersion);
            needsToShow = comparison > 0;
        }

        if (needsToShow) {
            callGetTC();
        } else {
            if (conditionListener != null) {
                conditionListener.onTermsCheckComplete(false);
            }
        }
    }

    public void dismissDialog() {
        if (termsDialog != null) {
            termsDialog.dismiss();
        }
    }
}