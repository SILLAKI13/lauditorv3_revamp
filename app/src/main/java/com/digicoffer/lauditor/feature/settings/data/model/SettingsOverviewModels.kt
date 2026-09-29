package com.digicoffer.lauditor.feature.settings.data.model

import org.json.JSONArray
import org.json.JSONObject

private fun JSONObject.optNullableString(name: String): String? {
    if (!has(name) || isNull(name)) return null
    val v = optString(name, "")
    return if (v.isBlank() || v.equals("null", ignoreCase = true)) null else v
}

data class SettingsOverviewResponse(
    val error: Boolean = false,
    val msg: String? = null,
    val data: SettingsOverviewData? = null
) {
    companion object {
        fun fromJson(json: JSONObject): SettingsOverviewResponse {
            val error = json.optBoolean("error", false)
            val msg = json.optNullableString("msg") ?: json.optNullableString("message")
            val dataObj = json.optJSONObject("data")
            val data = if (dataObj != null) SettingsOverviewData.fromJson(dataObj) else null
            return SettingsOverviewResponse(error = error, msg = msg, data = data)
        }
    }
}

data class SettingsOverviewData(
    val security: SecurityInfo? = null,
    val subscription: SubscriptionInfo? = null,
    val terms: TermsInfo? = null,
    val loggedInDevices: List<LoggedInDevice> = emptyList(),
    val currentDevice: LoggedInDevice? = null
) {
    companion object {
        fun fromJson(json: JSONObject): SettingsOverviewData {
            val secObj = json.optJSONObject("security")
            val subObj = json.optJSONObject("subscription")
            val termsObj = json.optJSONObject("terms")
            val devArr = json.optJSONArray("logged_in_devices")
            val curDevObj = json.optJSONObject("current_device")

            val devicesList = mutableListOf<LoggedInDevice>()
            if (devArr != null) {
                for (i in 0 until devArr.length()) {
                    val d = devArr.optJSONObject(i)
                    if (d != null) {
                        devicesList.add(LoggedInDevice.fromJson(d))
                    }
                }
            }

            return SettingsOverviewData(
                security = if (secObj != null) SecurityInfo.fromJson(secObj) else null,
                subscription = if (subObj != null) SubscriptionInfo.fromJson(subObj) else null,
                terms = if (termsObj != null) TermsInfo.fromJson(termsObj) else null,
                loggedInDevices = devicesList,
                currentDevice = if (curDevObj != null) LoggedInDevice.fromJson(curDevObj) else null
            )
        }
    }
}

data class SecurityInfo(
    val hasPassword: Boolean = true,
    val flow: String? = "change",
    val lastChangedOn: String? = null,
    val lastChangedAt: String? = null
) {
    companion object {
        fun fromJson(json: JSONObject): SecurityInfo {
            return SecurityInfo(
                hasPassword = json.optBoolean("hasPassword", true),
                flow = json.optNullableString("flow") ?: "change",
                lastChangedOn = json.optNullableString("lastChangedOn"),
                lastChangedAt = json.optNullableString("lastChangedAt")
            )
        }
    }
}

data class SubscriptionInfo(
    val id: String? = null,
    val status: String? = "Active",
    val displayStatus: DisplayStatus? = null,
    val activatedOn: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val nextBillingDate: String? = null,
    val validity: PlanValidity? = null,
    val plan: SubscriptionPlan? = null,
    val payment: PaymentInfo? = null,
    val pendingPlan: SubscriptionPlan? = null,
    val actions: SubscriptionActions? = null,
    val emandate: EmandateInfo? = null
) {
    companion object {
        fun fromJson(json: JSONObject): SubscriptionInfo {
            val dispObj = json.optJSONObject("displayStatus")
            val valObj = json.optJSONObject("validity")
            val planObj = json.optJSONObject("plan")
            val payObj = json.optJSONObject("payment")
            val pendObj = json.optJSONObject("pendingPlan")
            val actObj = json.optJSONObject("actions")
            val emObj = json.optJSONObject("emandate")

            return SubscriptionInfo(
                id = json.optNullableString("id"),
                status = json.optNullableString("status") ?: "Active",
                displayStatus = if (dispObj != null) DisplayStatus.fromJson(dispObj) else null,
                activatedOn = json.optNullableString("activatedOn"),
                startDate = json.optNullableString("startDate"),
                endDate = json.optNullableString("endDate"),
                nextBillingDate = json.optNullableString("nextBillingDate"),
                validity = if (valObj != null) PlanValidity.fromJson(valObj) else null,
                plan = if (planObj != null) SubscriptionPlan.fromJson(planObj) else null,
                payment = if (payObj != null) PaymentInfo.fromJson(payObj) else null,
                pendingPlan = if (pendObj != null) SubscriptionPlan.fromJson(pendObj) else null,
                actions = if (actObj != null) SubscriptionActions.fromJson(actObj) else null,
                emandate = if (emObj != null) EmandateInfo.fromJson(emObj) else null
            )
        }
    }
}

data class DisplayStatus(
    val text: String? = null,
    val color: String? = null
) {
    companion object {
        fun fromJson(json: JSONObject): DisplayStatus {
            return DisplayStatus(
                text = json.optNullableString("text"),
                color = json.optNullableString("color")
            )
        }
    }
}

data class PlanValidity(
    val daysLeft: Any? = null,
    val label: String? = null
) {
    fun getFormattedValidity(): String {
        return when {
            daysLeft != null && daysLeft.toString().isNotBlank() && daysLeft.toString() != "null" -> {
                val days = daysLeft.toString()
                if (days == "1") "1 day left" else "$days days left"
            }
            !label.isNullOrBlank() -> label
            else -> "Unlimited"
        }
    }

    companion object {
        fun fromJson(json: JSONObject): PlanValidity {
            return PlanValidity(
                daysLeft = if (json.has("daysLeft") && !json.isNull("daysLeft")) json.opt("daysLeft") else null,
                label = json.optNullableString("label")
            )
        }
    }
}

data class SubscriptionPlan(
    val key: String? = null,
    val planId: String? = null,
    val name: String? = null,
    val amount: Double? = 0.0,
    val billingCycle: String? = null,
    val currencyCode: String? = "INR",
    val currencySymbol: String? = "₹"
) {
    companion object {
        fun fromJson(json: JSONObject): SubscriptionPlan {
            return SubscriptionPlan(
                key = json.optNullableString("key"),
                planId = json.optNullableString("plan_id"),
                name = json.optNullableString("name"),
                amount = json.optDouble("amount", 0.0),
                billingCycle = json.optNullableString("billingCycle"),
                currencyCode = json.optNullableString("currencyCode") ?: "INR",
                currencySymbol = json.optNullableString("currencySymbol") ?: "₹"
            )
        }
    }
}

data class PaymentInfo(
    val method: String? = null,
    val maskedId: String? = null,
    val isAutoPayEnabled: Boolean = false
) {
    companion object {
        fun fromJson(json: JSONObject): PaymentInfo {
            return PaymentInfo(
                method = json.optNullableString("method"),
                maskedId = json.optNullableString("maskedId"),
                isAutoPayEnabled = json.optBoolean("isAutoPayEnabled", false)
            )
        }
    }
}

data class SubscriptionActions(
    val primary: SubscriptionActionItem? = null,
    val secondary: SubscriptionActionItem? = null
) {
    companion object {
        fun fromJson(json: JSONObject): SubscriptionActions {
            val prim = json.optJSONObject("primary")
            val sec = json.optJSONObject("secondary")
            return SubscriptionActions(
                primary = if (prim != null) SubscriptionActionItem.fromJson(prim) else null,
                secondary = if (sec != null) SubscriptionActionItem.fromJson(sec) else null
            )
        }
    }
}

data class SubscriptionActionItem(
    val type: String? = null,
    val label: String? = null
) {
    companion object {
        fun fromJson(json: JSONObject): SubscriptionActionItem {
            return SubscriptionActionItem(
                type = json.optNullableString("type"),
                label = json.optNullableString("label")
            )
        }
    }
}

data class EmandateInfo(
    val status: String? = null,
    val directCheckout: Boolean = false,
    val checkoutOptions: Any? = null
) {
    companion object {
        fun fromJson(json: JSONObject): EmandateInfo {
            return EmandateInfo(
                status = json.optNullableString("status"),
                directCheckout = json.optBoolean("direct_checkout", false),
                checkoutOptions = if (json.has("checkout_options") && !json.isNull("checkout_options")) json.opt("checkout_options") else null
            )
        }
    }
}

data class TermsInfo(
    val accepted: Boolean = false,
    val acceptedAt: String? = null,
    val version: String? = null
) {
    companion object {
        fun fromJson(json: JSONObject): TermsInfo {
            return TermsInfo(
                accepted = json.optBoolean("accepted", false),
                acceptedAt = json.optNullableString("accepted_at"),
                version = json.optNullableString("version")
            )
        }
    }
}

data class LoggedInDevice(
    val deviceId: String = "",
    val deviceName: String = "",
    val deviceType: String = "",
    val lastActive: String = "",
    val isCurrent: Boolean = false,
    val currentDevice: Boolean = false
) {
    companion object {
        fun fromJson(json: JSONObject): LoggedInDevice {
            return LoggedInDevice(
                deviceId = json.optNullableString("device_id") ?: "",
                deviceName = json.optNullableString("device_name") ?: "",
                deviceType = json.optNullableString("device_type") ?: "",
                lastActive = json.optNullableString("last_active") ?: "",
                isCurrent = json.optBoolean("is_current", false),
                currentDevice = json.optBoolean("current_device", false)
            )
        }
    }
}

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String
)

data class CheckoutTicketResponse(
    val error: Boolean = false,
    val msg: String? = null,
    val ticket: String? = null
) {
    companion object {
        fun fromJson(json: JSONObject): CheckoutTicketResponse {
            return CheckoutTicketResponse(
                error = json.optBoolean("error", false),
                msg = json.optNullableString("msg") ?: json.optNullableString("message"),
                ticket = json.optNullableString("ticket")
            )
        }
    }
}
