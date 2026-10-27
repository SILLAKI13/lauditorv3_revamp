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
        fun fromJson(json: JSONObject, localDeviceId: String = ""): SettingsOverviewResponse {
            val error = json.optBoolean("error", false)
            val msg = json.optNullableString("msg") ?: json.optNullableString("message")
            val dataObj = json.optJSONObject("data")
            val data = if (dataObj != null) SettingsOverviewData.fromJson(dataObj, localDeviceId) else null
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
        fun fromJson(json: JSONObject, localDeviceId: String = ""): SettingsOverviewData {
            val secObj = json.optJSONObject("security")
            val subObj = json.optJSONObject("subscription")
            val termsObj = json.optJSONObject("terms")
            val devArr = json.optJSONArray("logged_in_devices")
                ?: json.optJSONArray("loggedInDevices")
                ?: json.optJSONArray("devices")
                ?: json.optJSONArray("active_devices")
                ?: json.optJSONArray("activeDevices")
                ?: json.optJSONArray("sessions")
                ?: secObj?.optJSONArray("logged_in_devices")
                ?: secObj?.optJSONArray("devices")
            val curDevObj = json.optJSONObject("current_device")
                ?: json.optJSONObject("currentDevice")
                ?: secObj?.optJSONObject("current_device")

            val devicesList = mutableListOf<LoggedInDevice>()
            if (devArr != null) {
                for (i in 0 until devArr.length()) {
                    val d = devArr.optJSONObject(i)
                    if (d != null) {
                        devicesList.add(LoggedInDevice.fromJson(d, localDeviceId))
                    }
                }
            }

            val parsedCurDev = if (curDevObj != null) LoggedInDevice.fromJson(curDevObj, localDeviceId).copy(isCurrent = true, currentDevice = true) else null
            if (parsedCurDev != null) {
                val existsIndex = devicesList.indexOfFirst {
                    (it.deviceId.isNotEmpty() && it.deviceId == parsedCurDev.deviceId) ||
                    (it.deviceName.isNotEmpty() && it.deviceName == parsedCurDev.deviceName && it.deviceType == parsedCurDev.deviceType)
                }
                if (existsIndex >= 0) {
                    devicesList[existsIndex] = devicesList[existsIndex].copy(isCurrent = true, currentDevice = true)
                } else {
                    devicesList.add(0, parsedCurDev)
                }
            }

            // Current device should be sorted first (iOS parity)
            val sortedList = devicesList.sortedByDescending { it.isCurrent || it.currentDevice }

            val securityInfo = if (secObj != null) {
                SecurityInfo.fromJson(secObj)
            } else if (json.has("has_password") || json.has("hasPassword") || json.has("password_set")) {
                SecurityInfo.fromJson(json)
            } else null

            return SettingsOverviewData(
                security = securityInfo,
                subscription = if (subObj != null) SubscriptionInfo.fromJson(subObj) else null,
                terms = if (termsObj != null) TermsInfo.fromJson(termsObj) else null,
                loggedInDevices = sortedList,
                currentDevice = parsedCurDev
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
            val flowVal = json.optNullableString("flow")
                ?: json.optNullableString("action")
                ?: json.optNullableString("status")

            val hasPass = when {
                json.has("has_password") -> json.optBoolean("has_password")
                json.has("hasPassword") -> json.optBoolean("hasPassword")
                json.has("password_set") -> json.optBoolean("password_set")
                json.has("is_password_set") -> json.optBoolean("is_password_set")
                flowVal?.equals("set", ignoreCase = true) == true || flowVal?.equals("setup", ignoreCase = true) == true -> false
                flowVal?.equals("change", ignoreCase = true) == true -> true
                else -> true
            }

            val resolvedFlow = when {
                !flowVal.isNullOrBlank() -> flowVal
                !hasPass -> "setup"
                else -> "change"
            }

            return SecurityInfo(
                hasPassword = hasPass,
                flow = resolvedFlow,
                lastChangedOn = json.optNullableString("lastChangedOn") ?: json.optNullableString("last_changed_on"),
                lastChangedAt = json.optNullableString("lastChangedAt") ?: json.optNullableString("last_changed_at")
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
        fun fromJson(json: JSONObject, localDeviceId: String = ""): LoggedInDevice {
            val devId = json.optNullableString("device_id")
                ?: json.optNullableString("deviceId")
                ?: json.optNullableString("id")
                ?: json.optNullableString("session_id")
                ?: ""

            val devName = json.optNullableString("device_name")
                ?: json.optNullableString("deviceName")
                ?: json.optNullableString("name")
                ?: json.optNullableString("device")
                ?: json.optNullableString("browser")
                ?: json.optNullableString("model")
                ?: json.optNullableString("client_name")
                ?: "Unknown Device"

            val os = json.optNullableString("os")
                ?: json.optNullableString("platform")
                ?: json.optNullableString("operating_system")
            val osVersion = json.optNullableString("os_version")
                ?: json.optNullableString("osVersion")
                ?: json.optNullableString("version")
            val rawType = json.optNullableString("device_type")
                ?: json.optNullableString("deviceType")
                ?: json.optNullableString("type")

            val devType = when {
                !os.isNullOrBlank() && !osVersion.isNullOrBlank() -> "$os $osVersion"
                !os.isNullOrBlank() -> os
                !rawType.isNullOrBlank() -> rawType
                else -> ""
            }

            val rawLastActive = json.optNullableString("last_active")
                ?: json.optNullableString("lastActive")
                ?: json.optNullableString("last_activity")
                ?: json.optNullableString("lastActivity")
                ?: json.optNullableString("last_login")
                ?: json.optNullableString("lastLogin")
                ?: json.optNullableString("updated_at")
                ?: json.optNullableString("timestamp")
                ?: ""

            val formattedLastActive = formatRelativeTime(rawLastActive)

            val isCur = json.optBoolean("is_current", false)
                || json.optBoolean("isCurrent", false)
                || json.optBoolean("current_device", false)
                || json.optBoolean("currentDevice", false)
                || json.optBoolean("current", false)
                || json.optBoolean("is_current_device", false)
                || (localDeviceId.isNotBlank() && devId.isNotBlank() && devId.equals(localDeviceId, ignoreCase = true))

            return LoggedInDevice(
                deviceId = devId,
                deviceName = devName,
                deviceType = devType,
                lastActive = formattedLastActive,
                isCurrent = isCur,
                currentDevice = isCur
            )
        }

        fun formatRelativeTime(raw: String): String {
            if (raw.isBlank()) return "Recently active"
            if (raw.endsWith("ago", ignoreCase = true) || raw.endsWith("ago.", ignoreCase = true) || raw.equals("just now", ignoreCase = true)) {
                return if (raw.endsWith(".")) raw else "$raw."
            }
            return try {
                val patterns = listOf(
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                    "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
                    "yyyy-MM-dd'T'HH:mm:ss'Z'",
                    "yyyy-MM-dd'T'HH:mm:ss",
                    "yyyy-MM-dd HH:mm:ss",
                    "yyyy-MM-dd"
                )
                var parsedDate: java.util.Date? = null
                for (pattern in patterns) {
                    try {
                        val sdf = java.text.SimpleDateFormat(pattern, java.util.Locale.ROOT)
                        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                        parsedDate = sdf.parse(raw)
                        if (parsedDate != null) break
                    } catch (_: Exception) {}
                }
                if (parsedDate == null) {
                    val epoch = raw.toLongOrNull()
                    if (epoch != null) {
                        parsedDate = if (epoch < 10000000000L) java.util.Date(epoch * 1000) else java.util.Date(epoch)
                    }
                }
                if (parsedDate != null) {
                    val diff = System.currentTimeMillis() - parsedDate.time
                    val seconds = (diff / 1000).coerceAtLeast(0)
                    val minutes = seconds / 60
                    val hours = minutes / 60
                    val days = hours / 24
                    val weeks = days / 7
                    when {
                        seconds < 60 -> if (seconds <= 5) "4 seconds ago." else "$seconds seconds ago."
                        minutes < 60 -> "$minutes min${if (minutes > 1) "s" else ""} ago."
                        hours < 24 -> "$hours hour${if (hours > 1) "s" else ""} ago."
                        days < 7 -> "$days day${if (days > 1) "s" else ""} ago."
                        weeks < 5 -> "$weeks week${if (weeks > 1) "s" else ""} ago."
                        else -> {
                            val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.ROOT)
                            sdf.format(parsedDate)
                        }
                    }
                } else {
                    raw
                }
            } catch (e: Exception) {
                raw
            }
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
            val error = json.optBoolean("error", false)
            val msg = json.optNullableString("msg") ?: json.optNullableString("message")
            val dataObj = json.optJSONObject("data")
            val ticket = json.optNullableString("ticket")
                ?: json.optNullableString("checkout_ticket")
                ?: dataObj?.optNullableString("ticket")
                ?: dataObj?.optNullableString("checkout_ticket")
            return CheckoutTicketResponse(
                error = error,
                msg = msg,
                ticket = ticket
            )
        }
    }
}
