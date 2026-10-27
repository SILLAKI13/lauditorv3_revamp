package com.digicoffer.lauditor.feature.settings.presentation.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.digicoffer.lauditor.CommonFiles.CacheUtils.AppImageCache
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.DeviceSyncManager
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.SubscriptionAccessManager
import com.digicoffer.lauditor.LoginActivity.ViewModels.LoginActivity
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.feature.settings.data.model.*
import com.digicoffer.lauditor.feature.settings.data.repository.SettingsRepository
import com.digicoffer.lauditor.feature.settings.presentation.state.SettingsUiEvent
import com.digicoffer.lauditor.feature.settings.presentation.state.SettingsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadOverview(isRefresh = false)
    }

    fun onEvent(event: SettingsUiEvent) {
        when (event) {
            is SettingsUiEvent.LoadOverview -> loadOverview(isRefresh = false)
            is SettingsUiEvent.RefreshOverview -> loadOverview(isRefresh = true)

            is SettingsUiEvent.OpenChangePasswordDialog -> {
                _uiState.update {
                    it.copy(
                        isChangePasswordOpen = true,
                        currentPassword = "",
                        newPassword = "",
                        confirmPassword = "",
                        changePasswordError = null,
                        currentPasswordVisible = false,
                        newPasswordVisible = false,
                        confirmPasswordVisible = false
                    )
                }
            }
            is SettingsUiEvent.CloseChangePasswordDialog -> {
                _uiState.update { it.copy(isChangePasswordOpen = false, changePasswordError = null) }
            }
            is SettingsUiEvent.UpdateCurrentPassword -> {
                _uiState.update { it.copy(currentPassword = event.value, changePasswordError = null) }
            }
            is SettingsUiEvent.UpdateNewPassword -> {
                _uiState.update { it.copy(newPassword = event.value, changePasswordError = null) }
            }
            is SettingsUiEvent.UpdateConfirmPassword -> {
                _uiState.update { it.copy(confirmPassword = event.value, changePasswordError = null) }
            }
            is SettingsUiEvent.ToggleCurrentPasswordVisibility -> {
                _uiState.update { it.copy(currentPasswordVisible = !it.currentPasswordVisible) }
            }
            is SettingsUiEvent.ToggleNewPasswordVisibility -> {
                _uiState.update { it.copy(newPasswordVisible = !it.newPasswordVisible) }
            }
            is SettingsUiEvent.ToggleConfirmPasswordVisibility -> {
                _uiState.update { it.copy(confirmPasswordVisible = !it.confirmPasswordVisible) }
            }
            is SettingsUiEvent.SubmitChangePassword -> submitChangePassword()

            is SettingsUiEvent.InitiateChangePlan -> initiateCheckout(isCancel = false)
            is SettingsUiEvent.CancelSubscriptionClicked -> initiateCheckout(isCancel = true)
            is SettingsUiEvent.DismissExternalCheckoutBottomSheet -> {
                _uiState.update { it.copy(isExternalCheckoutBottomSheetOpen = false, checkoutTicket = null, checkoutUrl = null) }
            }
            is SettingsUiEvent.ConfirmOpenExternalCheckout -> {
                _uiState.update { it.copy(isExternalCheckoutBottomSheetOpen = false) }
            }
            is SettingsUiEvent.DismissCancelSubscriptionBottomSheet -> {
                _uiState.update { it.copy(isCancelSubscriptionBottomSheetOpen = false) }
            }

            is SettingsUiEvent.OpenDeleteAccountConfirmation -> {
                _uiState.update {
                    it.copy(
                        isDeleteAccountConfirmationOpen = true,
                        deleteAccountConfirmationEmail = "",
                        deleteAccountError = null
                    )
                }
            }
            is SettingsUiEvent.DismissDeleteAccountConfirmation -> {
                _uiState.update {
                    it.copy(
                        isDeleteAccountConfirmationOpen = false,
                        deleteAccountConfirmationEmail = "",
                        deleteAccountError = null
                    )
                }
            }
            is SettingsUiEvent.UpdateDeleteAccountEmail -> {
                _uiState.update {
                    it.copy(
                        deleteAccountConfirmationEmail = event.value,
                        deleteAccountError = null
                    )
                }
            }
            is SettingsUiEvent.ConfirmDeleteAccount -> submitDeleteAccount()

            is SettingsUiEvent.DismissInfoDialog -> {
                val shouldLogout = _uiState.value.shouldLogoutOnDismiss
                _uiState.update {
                    it.copy(
                        isInfoDialogOpen = false,
                        dialogTitle = "",
                        dialogMessage = "",
                        shouldLogoutOnDismiss = false
                    )
                }
                if (shouldLogout) {
                    performAppLogout()
                }
            }
        }
    }

    private fun performAppLogout() {
        try {
            val ctx = getApplication<Application>().applicationContext
            val prefs = ctx.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
            Constants.TOKEN = ""
            Constants.USER_ID = ""
            Constants.UID = ""
            Constants.firm_image = ""
            Constants.dashboard_image = ""
            Constants.NAME = ""
            Constants.NAME_NEW = ""
            Constants.FIRM_NAME = ""
            Constants.ContactName = ""
            Constants.firmProfileModel = null
            AppImageCache.clearAll()
            try {
                com.bumptech.glide.Glide.get(ctx).clearMemory()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val intent = Intent(ctx, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            ctx.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadOverview(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            } else {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }

            // Ensure Android session is synced with backend
            DeviceSyncManager.syncDevice(getApplication())

            val result = repository.fetchSettingsOverview()
            if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val responseJson = JSONObject(result.responseContent ?: "{}")
                    val isError = responseJson.optBoolean("error", false)
                    if (!isError) {
                        val localDeviceId = DeviceSyncManager.getDeviceId(getApplication())
                        val parsed = SettingsOverviewResponse.fromJson(responseJson, localDeviceId)
                        val data = parsed.data

                        val devices: List<LoggedInDevice> = data?.loggedInDevices ?: emptyList()

                        // Update centralized subscription manager and Constants
                        data?.subscription?.let { sub ->
                            val isActive = sub.status.equals("Active", ignoreCase = true)
                            val subModel = sub.plan?.key?.ifEmpty { sub.plan.planId } ?: ""
                            val planName = sub.plan?.name ?: ""
                            SubscriptionAccessManager.updateSubscription(
                                isActive = isActive,
                                subModel = subModel,
                                planName = planName,
                                context = getApplication()
                            )
                        }

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                errorMessage = null,
                                security = data?.security,
                                subscription = data?.subscription,
                                terms = data?.terms,
                                loggedInDevices = devices
                            )
                        }
                    } else {
                        val msg = responseJson.optString("msg", responseJson.optString("message", "Failed to load settings"))
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                errorMessage = msg
                            )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = e.message ?: "Failed to parse settings overview"
                        )
                    }
                }
            } else {
                val errorMsg = if (result.responseContent?.isNotBlank() == true) {
                    try {
                        val j = JSONObject(result.responseContent)
                        j.optString("msg", j.optString("message", Constants.NO_INTERNET_MSG))
                    } catch (e: Exception) {
                        result.responseContent ?: Constants.NO_INTERNET_MSG
                    }
                } else {
                    Constants.NO_INTERNET_MSG
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = errorMsg
                    )
                }
            }
        }
    }

    private fun submitChangePassword() {
        val hasPassword = _uiState.value.security?.hasPassword ?: true
        val current = _uiState.value.currentPassword.trim()
        val newPass = _uiState.value.newPassword.trim()
        val confirm = _uiState.value.confirmPassword.trim()

        if (hasPassword && current.isEmpty()) {
            _uiState.update { it.copy(changePasswordError = "Please enter your current password.") }
            return
        }
        if (newPass.isEmpty()) {
            _uiState.update { it.copy(changePasswordError = "Please enter a new password.") }
            return
        }
        if (!_uiState.value.isAllPasswordRulesMet) {
            _uiState.update { it.copy(changePasswordError = "Password does not meet the required conditions.") }
            return
        }
        if (confirm.isEmpty()) {
            _uiState.update { it.copy(changePasswordError = "Please confirm your new password.") }
            return
        }
        if (newPass != confirm) {
            _uiState.update { it.copy(changePasswordError = "Confirm password mismatch.") }
            return
        }
        if (hasPassword && newPass == current) {
            _uiState.update { it.copy(changePasswordError = "New password must be different from current password.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(changePasswordLoading = true, changePasswordError = null) }
            val result = if (hasPassword) {
                repository.changePassword(current, newPass, confirm)
            } else {
                repository.setPassword(newPass, confirm)
            }
            _uiState.update { it.copy(changePasswordLoading = false) }

            if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val responseJson = JSONObject(result.responseContent ?: "{}")
                    val isError = responseJson.optBoolean("error", false)
                    if (!isError) {
                        val msg = responseJson.optString("msg", responseJson.optString("message", if (hasPassword) "Password updated successfully." else "Password set successfully."))
                        _uiState.update {
                            it.copy(
                                isChangePasswordOpen = false,
                                currentPassword = "",
                                newPassword = "",
                                confirmPassword = "",
                                changePasswordError = null,
                                isInfoDialogOpen = true,
                                dialogTitle = "Success",
                                dialogMessage = msg,
                                isSuccessMessage = true,
                                security = it.security?.copy(hasPassword = true, flow = "change")
                            )
                        }
                        // Refresh overview to sync security state
                        loadOverview(isRefresh = true)
                    } else {
                        val errorMsg = AndroidUtils.extractCleanErrorMessage(result.responseContent)
                        _uiState.update { it.copy(changePasswordError = errorMsg) }
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(changePasswordError = "Failed to update password. Please try again.") }
                }
            } else {
                val errorMsg = if (!result.responseContent.isNullOrBlank()) {
                    AndroidUtils.extractCleanErrorMessage(result.responseContent)
                } else {
                    Constants.NO_INTERNET_MSG
                }
                _uiState.update { it.copy(changePasswordError = errorMsg) }
            }
        }
    }

    private fun initiateCheckout(isCancel: Boolean = false) {
        viewModelScope.launch {
            if (isCancel) {
                _uiState.update { it.copy(cancelSubscriptionLoading = true) }
            } else {
                _uiState.update { it.copy(checkoutLoading = true) }
            }
            val result = repository.generateCheckoutTicket()
            _uiState.update { it.copy(checkoutLoading = false, cancelSubscriptionLoading = false) }

            if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val responseJson = JSONObject(result.responseContent ?: "{}")
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
                        val encodedTicket = java.net.URLEncoder.encode(ticket, "UTF-8")
                        val fullUrl = "$baseUrl$encodedTicket"
                        _uiState.update {
                            it.copy(
                                checkoutTicket = ticket,
                                checkoutUrl = fullUrl,
                                isExternalCheckoutBottomSheetOpen = true
                            )
                        }
                    } else {
                        val errorMsg = responseJson.optString("msg", responseJson.optString("message", "Unable to generate checkout ticket. Please try again."))
                        _uiState.update {
                            it.copy(
                                isInfoDialogOpen = true,
                                dialogTitle = "Subscription Checkout",
                                dialogMessage = errorMsg,
                                isSuccessMessage = false
                            )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isInfoDialogOpen = true,
                            dialogTitle = "Subscription Checkout",
                            dialogMessage = "Failed to process checkout response.",
                            isSuccessMessage = false
                        )
                    }
                }
            } else {
                val errorMsg = if (!result.responseContent.isNullOrBlank()) {
                    try {
                        val j = JSONObject(result.responseContent)
                        j.optString("msg", j.optString("message", "Unable to connect to payment service."))
                    } catch (e: Exception) {
                        result.responseContent ?: "Unable to connect to payment service."
                    }
                } else {
                    Constants.NO_INTERNET_MSG
                }
                _uiState.update {
                    it.copy(
                        isInfoDialogOpen = true,
                        dialogTitle = "Subscription Checkout",
                        dialogMessage = errorMsg,
                        isSuccessMessage = false
                    )
                }
            }
        }
    }

    private fun submitDeleteAccount() {
        val email = _uiState.value.deleteAccountConfirmationEmail.trim()
        if (email.isEmpty()) {
            _uiState.update { it.copy(deleteAccountError = "Please type in your email to confirm.") }
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(deleteAccountError = "Please enter a valid email address.") }
            return
        }

        val registeredEmail = (Constants.Email?.ifEmpty { null }
            ?: getApplication<Application>().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).getString("email", null)
            ?: "").trim().lowercase(java.util.Locale.getDefault())

        if (registeredEmail.isNotEmpty() && !email.equals(registeredEmail, ignoreCase = true)) {
            _uiState.update { it.copy(deleteAccountError = "Entered email does not match your account email.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(deleteAccountLoading = true, deleteAccountError = null) }
            val result = repository.deleteAccount()
            _uiState.update {
                it.copy(
                    deleteAccountLoading = false,
                    isDeleteAccountConfirmationOpen = false
                )
            }

            if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val responseJson = JSONObject(result.responseContent ?: "{}")
                    val isError = responseJson.optBoolean("error", false)
                    val msg = responseJson.optString(
                        "msg",
                        responseJson.optString("message", "Account deletion request submitted. Will be completed in 2 weeks.")
                    )
                    _uiState.update {
                        it.copy(
                            isInfoDialogOpen = true,
                            dialogTitle = if (!isError) "Success" else "Error",
                            dialogMessage = msg,
                            isSuccessMessage = !isError,
                            shouldLogoutOnDismiss = !isError
                        )
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isInfoDialogOpen = true,
                            dialogTitle = "Success",
                            dialogMessage = "Account deletion request submitted.",
                            isSuccessMessage = true,
                            shouldLogoutOnDismiss = true
                        )
                    }
                }
            } else {
                _uiState.update {
                    it.copy(
                        isInfoDialogOpen = true,
                        dialogTitle = "Error",
                        dialogMessage = result.responseContent ?: Constants.NO_INTERNET_MSG,
                        isSuccessMessage = false
                    )
                }
            }
        }
    }
}
