package com.digicoffer.lauditor.CommonFiles.GlobalFiles

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.digicoffer.lauditor.MainActivity
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import org.json.JSONObject
import java.net.URLEncoder

sealed class SubscriptionAccessResult {
    object Allowed : SubscriptionAccessResult()
    data class Restricted(
        val title: String,
        val message: String,
        val actionLabel: String,
        val isExpired: Boolean
    ) : SubscriptionAccessResult()
    data class CategoryRestricted(
        val title: String,
        val message: String
    ) : SubscriptionAccessResult()
}

enum class SubscriptionPlanType {
    FREE_TRIAL,
    FREE,
    STARTER,
    PROFESSIONAL,
    PREMIUM
}

object SubscriptionAccessManager {

    private const val TAG = "SubAccessManager"
    private var activeDialog: AlertDialog? = null

    /**
     * Resolves and normalizes plan identifiers:
     * Free Trial: "three_months_plan", "three_month_free", "3_months_plan", "free trial"
     * Free Plan: "free", "free_plan", "the free"
     * Starter: "starter", Rs. 999
     * Professional: "professional", Rs. 1299
     * Premium: "premium", Rs. 1499
     */
    fun resolvePlan(model: String?, name: String?): SubscriptionPlanType {
        val m = (model ?: "").trim().lowercase()
        val n = (name ?: "").trim().lowercase()

        return when {
            m.contains("three_month") || m.contains("three_months") ||
            m.contains("3_month") || n.contains("free trial") || n.contains("trial") -> SubscriptionPlanType.FREE_TRIAL

            m == "free" || m == "free_plan" || m == "the free" ||
            n == "free" || n == "free plan" -> SubscriptionPlanType.FREE

            m.contains("starter") || n.contains("starter") || m.contains("999") -> SubscriptionPlanType.STARTER

            m.contains("professional") || n.contains("professional") || m.contains("1299") -> SubscriptionPlanType.PROFESSIONAL

            m.contains("premium") || n.contains("premium") || m.contains("1499") -> SubscriptionPlanType.PREMIUM

            else -> SubscriptionPlanType.FREE_TRIAL // Safe default for new accounts
        }
    }

    /**
     * Checks subscription plan access according to iOS parity rules & Plan Matrix specification:
     *
     * 1. Profile is ALWAYS exempt (CommonWords.profile exemption)
     * 2. Free Trial ("three_month_free", "three_months_plan"): Active by default, trial features allowed
     * 3. Free Plan: Profile allowed; all action buttons show "This option is unavailable on The Free Plan. Click here to upgrade."
     * 4. Starter Plan: Profile & Appointments allowed; others show Starter upgrade message
     * 5. Professional Plan: Profile, Appointments & Instant Appointments allowed; others show Professional upgrade message
     * 6. Premium Plan: Full access per matrix
     * 7. Expired Subscription: Shows "Subscription Expired" / "Renew Now"
     */
    fun checkAccess(moduleName: String = "", action: String = ""): SubscriptionAccessResult {
        val mod = moduleName.trim().lowercase()
        val act = action.trim().lowercase()
        val isSolo = "solo".equals(Constants.CATEGORY, ignoreCase = true)

        // 1. Profile / Settings exemption (matches iOS `if moduleName == CommonWords.profile { return false }`)
        if (mod == "profile" || mod == "firm_profile" || mod == "firmprofile" ||
            mod == "settings" || mod == "profile_info" || mod == "settlement_details" ||
            mod == "extended_profile" || mod == "profile_listing") {
            if ((mod == "firm_profile" || mod == "firmprofile") && isSolo) {
                return SubscriptionAccessResult.CategoryRestricted(
                    title = "Not Available",
                    message = "Firm Profile is not available for Solo accounts."
                )
            }
            return SubscriptionAccessResult.Allowed
        }

        // Resolve current plan
        val plan = resolvePlan(Constants.SUB_MODEL, Constants.PLAN_NAME)

        // Determine if subscription is truly expired:
        // A Free Trial is NEVER expired merely because is_active was defaulted to false.
        // It is only expired if explicitly confirmed expired by the backend.
        val isTrial = (plan == SubscriptionPlanType.FREE_TRIAL)
        val isExplicitlyExpired = !Constants.is_active && !isTrial

        if (isExplicitlyExpired) {
            return SubscriptionAccessResult.Restricted(
                title = "Subscription Expired",
                message = "Your subscription has expired. Renew now to keep your account active and access all features.",
                actionLabel = "Renew Now",
                isExpired = true
            )
        }

        // Apply plan-based matrix rules
        return when (plan) {
            SubscriptionPlanType.FREE_TRIAL -> {
                when {
                    mod == "instant_online_appointments" || mod == "instant_appointment" ||
                    (mod == "appointments" && act == "instant_online") -> {
                        SubscriptionAccessResult.Restricted(
                            title = "Subscription Upgrade",
                            message = "Instant online appointments is unavailable on The Free Trial.\nClick here to upgrade.",
                            actionLabel = "Upgrade",
                            isExpired = false
                        )
                    }
                    mod == "doc_editor" || mod == "doceditor" -> {
                        SubscriptionAccessResult.Restricted(
                            title = "Subscription Upgrade",
                            message = "This option is unavailable.\nClick here to upgrade.",
                            actionLabel = "Upgrade",
                            isExpired = false
                        )
                    }
                    // Solo category restrictions
                    isSolo && (mod == "firm_doc_upload" || mod == "team_chat" ||
                              mod == "aggregated_timesheet" || mod == "groups" || mod == "team_members") -> {
                        SubscriptionAccessResult.CategoryRestricted(
                            title = "Not Available",
                            message = "This feature is not available for Solo accounts."
                        )
                    }
                    else -> SubscriptionAccessResult.Allowed
                }
            }

            SubscriptionPlanType.FREE -> {
                SubscriptionAccessResult.Restricted(
                    title = "Subscription Upgrade",
                    message = "This option is unavailable on The Free Plan.\nClick here to upgrade.",
                    actionLabel = "Upgrade",
                    isExpired = false
                )
            }

            SubscriptionPlanType.STARTER -> {
                when {
                    (mod == "appointment" || mod == "appointments") &&
                    act != "instant_online" && act != "instant" && act != "create" && act != "action" -> {
                        SubscriptionAccessResult.Allowed
                    }
                    else -> {
                        SubscriptionAccessResult.Restricted(
                            title = "Subscription Upgrade",
                            message = "This option is unavailable on The Starter Plan.\nClick here to upgrade.",
                            actionLabel = "Upgrade",
                            isExpired = false
                        )
                    }
                }
            }

            SubscriptionPlanType.PROFESSIONAL -> {
                when {
                    (mod == "appointment" || mod == "appointments" ||
                     mod == "instant_online_appointments" || mod == "instant_appointment" ||
                     act == "instant_online") && act != "create" && act != "action" -> {
                        SubscriptionAccessResult.Allowed
                    }
                    else -> {
                        SubscriptionAccessResult.Restricted(
                            title = "Subscription Upgrade",
                            message = "This option is unavailable on The Professional Plan.\nClick here to upgrade.",
                            actionLabel = "Upgrade",
                            isExpired = false
                        )
                    }
                }
            }

            SubscriptionPlanType.PREMIUM -> {
                when {
                    mod == "doc_editor" || mod == "doceditor" -> {
                        SubscriptionAccessResult.Restricted(
                            title = "Subscription Upgrade",
                            message = "This option is unavailable.\nClick here to upgrade.",
                            actionLabel = "Upgrade",
                            isExpired = false
                        )
                    }
                    isSolo && (mod == "firm_doc_upload" || mod == "team_chat" ||
                              mod == "aggregated_timesheet" || mod == "groups" || mod == "team_members") -> {
                        SubscriptionAccessResult.CategoryRestricted(
                            title = "Not Available",
                            message = "This feature is not available for Solo accounts."
                        )
                    }
                    else -> SubscriptionAccessResult.Allowed
                }
            }
        }
    }

    /**
     * Helper that returns true if restricted and displays the popup.
     * Matches iOS:
     * if checkSubPlanValidation() {
     *     AppCSModel.shared.subcribPopup = true
     *     return
     * }
     */
    fun checkAndShowPopup(activity: Activity?, moduleName: String = "", action: String = ""): Boolean {
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            return false
        }
        val result = checkAccess(moduleName, action)
        when (result) {
            is SubscriptionAccessResult.Allowed -> return false
            is SubscriptionAccessResult.Restricted -> {
                showSubscriptionWarningDialog(activity, result)
                return true
            }
            is SubscriptionAccessResult.CategoryRestricted -> {
                AndroidUtils.showAlert(result.message, activity, result.title)
                return true
            }
        }
    }

    /**
     * Displays the subscription warning popup matching iOS.
     */
    fun showSubscriptionWarningDialog(activity: Activity, restricted: SubscriptionAccessResult.Restricted) {
        try {
            if (activity.isFinishing || activity.isDestroyed) return

            // Dismiss any existing popup to avoid duplicate dialogs
            activeDialog?.let {
                if (it.isShowing) {
                    it.dismiss()
                }
            }

            val dialogBuilder = AlertDialog.Builder(activity)
            val inflater = activity.layoutInflater
            val dialogLayout = inflater.inflate(R.layout.reneval_popup, null)

            val tvConfirmation = dialogLayout.findViewById<TextView>(R.id.tv_confirmation)
            val tvConfirmContent = dialogLayout.findViewById<TextView>(R.id.tv_confirmContent)
            val ivClose = dialogLayout.findViewById<View>(R.id.iv_close)
            val btnSave = dialogLayout.findViewById<View>(R.id.btnSave)

            tvConfirmation?.text = restricted.title
            tvConfirmContent?.text = restricted.message

            if (btnSave is TextView) {
                btnSave.text = restricted.actionLabel
            } else if (btnSave is androidx.appcompat.widget.AppCompatButton) {
                btnSave.text = restricted.actionLabel
            }
            btnSave?.visibility = View.VISIBLE

            val dialog = dialogBuilder.create()
            dialog.setView(dialogLayout)
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()

            val window = dialog.window
            if (window != null) {
                window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                val marginInDp = 20
                val scale = activity.resources.displayMetrics.density
                val marginInPx = (marginInDp * scale + 0.5f).toInt()
                val screenWidth = activity.resources.displayMetrics.widthPixels
                val dialogWidth = screenWidth - (2 * marginInPx)
                window.setLayout(dialogWidth, WindowManager.LayoutParams.WRAP_CONTENT)
                val layoutParams = window.attributes
                layoutParams.gravity = Gravity.CENTER
                window.attributes = layoutParams
            }

            btnSave?.setOnClickListener {
                dialog.dismiss()
                launchCheckout(activity)
            }

            ivClose?.setOnClickListener {
                dialog.dismiss()
            }

            activeDialog = dialog
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying subscription popup: ${e.message}", e)
        }
    }

    /**
     * Launches the checkout flow using POST /professional/v3/generate/checkout/ticket.
     * Environment mapping:
     * DEV: https://dev2.payment.lexiz.ai/plans?ticket=
     * STAGING: https://staging2.payment.lexiz.ai/plans?ticket=
     * PROD: https://payment.lexiz.ai/plans?ticket=
     */
    fun launchCheckout(activity: Activity) {
        try {
            Constants.check_url()
            if (Constants.base_URL.isNullOrEmpty() && !Constants.PROF_URL.isNullOrEmpty()) {
                Constants.base_URL = Constants.PROF_URL
            }

            val progress = AndroidUtils.get_progress(activity)

            WebServiceHelper.callHttpWebService(
                object : AsyncTaskCompleteListener {
                    override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                        try {
                            AndroidUtils.dismiss_dialog(progress)
                        } catch (ignored: Exception) {}

                        if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                            try {
                                val responseJson = JSONObject(httpResult.responseContent ?: "{}")
                                val isError = responseJson.optBoolean("error", false)
                                val dataObj = responseJson.optJSONObject("data")
                                val ticket = responseJson.optString("ticket").ifEmpty {
                                    responseJson.optString("checkout_ticket").ifEmpty {
                                        dataObj?.optString("ticket")?.ifEmpty {
                                            dataObj?.optString("checkout_ticket")
                                        } ?: ""
                                    }
                                }

                                if (!isError && ticket.isNotBlank()) {
                                    val baseUrl = when {
                                        Constants.ISPRODUCTION -> "https://payment.lexiz.ai/plans?ticket="
                                        Constants.IS_STAGING -> "https://staging2.payment.lexiz.ai/plans?ticket="
                                        else -> "https://dev2.payment.lexiz.ai/plans?ticket="
                                    }
                                    val encodedTicket = URLEncoder.encode(ticket, "UTF-8")
                                    val fullUrl = "$baseUrl$encodedTicket"

                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl))
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    activity.startActivity(intent)
                                    return
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing checkout ticket: ${e.message}")
                            }
                        }

                        // Fallback: Navigate to Settings screen
                        val mainActivity = (activity as? MainActivity) ?: Constants.mainActivity
                        mainActivity?.navigateToSettings()
                    }

                    override fun onClick(view: View) {}
                },
                activity,
                WebServiceHelper.RestMethodType.POST,
                "v3/generate/checkout/ticket",
                "GENERATE_CHECKOUT_TICKET",
                JSONObject().toString()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error initiating checkout: ${e.message}")
            val mainActivity = (activity as? MainActivity) ?: Constants.mainActivity
            mainActivity?.navigateToSettings()
        }
    }

    /**
     * Updates and persists subscription state across memory and SharedPreferences.
     */
    fun updateSubscription(
        isActive: Boolean,
        subModel: String?,
        planName: String?,
        context: Context? = null
    ) {
        val plan = resolvePlan(subModel, planName)
        val isTrial = (plan == SubscriptionPlanType.FREE_TRIAL)
        val effectiveIsActive = if (isTrial) true else isActive

        Constants.is_active = effectiveIsActive
        if (!subModel.isNullOrBlank()) {
            Constants.SUB_MODEL = subModel
        }
        if (!planName.isNullOrBlank()) {
            Constants.PLAN_NAME = planName
        }

        val ctx = context ?: Constants.mainActivity
        ctx?.let { c ->
            try {
                val prefs = c.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                val savedJsonStr = prefs.getString("Json_key", null)
                val userJson = if (!savedJsonStr.isNullOrBlank()) JSONObject(savedJsonStr) else JSONObject()
                val subObj = userJson.optJSONObject("subscription") ?: JSONObject()
                subObj.put("is_active", effectiveIsActive)
                if (!subModel.isNullOrBlank()) subObj.put("sub_model", subModel)
                if (!planName.isNullOrBlank()) subObj.put("plan_name", planName)
                userJson.put("subscription", subObj)
                prefs.edit().putString("Json_key", userJson.toString()).apply()
            } catch (e: Exception) {
                Log.e(TAG, "Error persisting subscription state: ${e.message}")
            }
        }
    }
}
