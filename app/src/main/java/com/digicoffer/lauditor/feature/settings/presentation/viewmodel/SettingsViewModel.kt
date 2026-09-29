package com.digicoffer.lauditor.feature.settings.presentation.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.digicoffer.lauditor.CommonFiles.CacheUtils.AppImageCache
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
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

            is SettingsUiEvent.InitiateChangePlan -> initiateCheckout()
            is SettingsUiEvent.CancelSubscriptionClicked -> {
                _uiState.update { it.copy(isCancelSubscriptionBottomSheetOpen = true) }
            }
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

    private fun loadOverview(isRefresh: Boolean) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            } else {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }

            val result = repository.fetchSettingsOverview()
            if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val responseJson = JSONObject(result.responseContent ?: "{}")
                    val isError = responseJson.optBoolean("error", false)
                    if (!isError) {
                        val parsed = SettingsOverviewResponse.fromJson(responseJson)
                        val data = parsed.data

                        // Process logged in devices: ensure the first device is marked as current if none is explicitly marked
                        val devices: List<LoggedInDevice> = data?.loggedInDevices ?: emptyList()
                        val hasExplicitCurrent = devices.any { it.isCurrent || it.currentDevice }
                        val updatedDevices = devices.mapIndexed { index: Int, dev: LoggedInDevice ->
                            if (index == 0 && !hasExplicitCurrent) {
                                dev.copy(isCurrent = true)
                            } else {
                                dev
                            }
                        }

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                errorMessage = null,
                                security = data?.security,
                                subscription = data?.subscription,
                                terms = data?.terms,
                                loggedInDevices = updatedDevices
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
        val current = _uiState.value.currentPassword.trim()
        val newPass = _uiState.value.newPassword.trim()
        val confirm = _uiState.value.confirmPassword.trim()

        if (current.isEmpty()) {
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
        if (newPass == current) {
            _uiState.update { it.copy(changePasswordError = "New password must be different from current password.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(changePasswordLoading = true, changePasswordError = null) }
            val result = repository.changePassword(current, newPass, confirm)
            _uiState.update { it.copy(changePasswordLoading = false) }

            if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val responseJson = JSONObject(result.responseContent ?: "{}")
                    val isError = responseJson.optBoolean("error", false)
                    if (!isError) {
                        val msg = responseJson.optString("msg", responseJson.optString("message", "Password updated successfully."))
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
                                isSuccessMessage = true
                            )
                        }
                    } else {
                        val errorsArr = responseJson.optJSONArray("errors")
                        val errorMsg = if (errorsArr != null && errorsArr.length() > 0) {
                            val firstErr = errorsArr.optJSONObject(0)
                            firstErr?.optString("msg", firstErr.optString("message", "Failed to update password.")) ?: "Failed to update password."
                        } else {
                            responseJson.optString("msg", responseJson.optString("message", "Failed to update password."))
                        }
                        _uiState.update { it.copy(changePasswordError = errorMsg) }
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(changePasswordError = "Failed to update password. Please try again.") }
                }
            } else {
                val errorMsg = if (!result.responseContent.isNullOrBlank()) {
                    try {
                        val responseJson = JSONObject(result.responseContent)
                        val errorsArr = responseJson.optJSONArray("errors")
                        if (errorsArr != null && errorsArr.length() > 0) {
                            val firstErr = errorsArr.optJSONObject(0)
                            firstErr?.optString("msg", firstErr.optString("message", "Failed to update password.")) ?: "Failed to update password."
                        } else {
                            responseJson.optString("msg", responseJson.optString("message", "Failed to update password."))
                        }
                    } catch (e: Exception) {
                        result.responseContent ?: "Failed to update password."
                    }
                } else {
                    Constants.NO_INTERNET_MSG
                }
                _uiState.update { it.copy(changePasswordError = errorMsg) }
            }
        }
    }

    private fun initiateCheckout() {
        viewModelScope.launch {
            _uiState.update { it.copy(checkoutLoading = true) }
            val result = repository.generateCheckoutTicket()
            _uiState.update { it.copy(checkoutLoading = false) }

            if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val responseJson = JSONObject(result.responseContent ?: "{}")
                    val isError = responseJson.optBoolean("error", false)
                    val ticket = responseJson.optString("ticket", "")
                    if (!isError && ticket.isNotBlank()) {
                        val baseUrl = when {
                            Constants.ISPRODUCTION -> "https://payment.lexiz.ai/plans?ticket="
                            Constants.IS_STAGING -> "https://staging2.payment.lexiz.ai/plans?ticket="
                            else -> "https://dev2.payment.lexiz.ai/plans?ticket="
                        }
                        val fullUrl = "$baseUrl$ticket"
                        _uiState.update {
                            it.copy(
                                checkoutTicket = ticket,
                                checkoutUrl = fullUrl,
                                isExternalCheckoutBottomSheetOpen = true
                            )
                        }
                    } else {
                        val msg = responseJson.optString("msg", responseJson.optString("message", "Failed to initiate checkout."))
                        _uiState.update {
                            it.copy(
                                isInfoDialogOpen = true,
                                dialogTitle = "Error",
                                dialogMessage = msg,
                                isSuccessMessage = false
                            )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isInfoDialogOpen = true,
                            dialogTitle = "Error",
                            dialogMessage = "Failed to generate checkout ticket.",
                            isSuccessMessage = false
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

    private fun submitDeleteAccount() {
        val email = _uiState.value.deleteAccountConfirmationEmail.trim()
        if (email.isEmpty()) {
            _uiState.update { it.copy(deleteAccountError = "Please enter your email to confirm.") }
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(deleteAccountError = "Please enter a valid email address.") }
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
                            dialogTitle = "Success",
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
