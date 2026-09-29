package com.digicoffer.lauditor.feature.settings.presentation.state

sealed interface SettingsUiEvent {
    object LoadOverview : SettingsUiEvent
    object RefreshOverview : SettingsUiEvent

    // Change Password
    object OpenChangePasswordDialog : SettingsUiEvent
    object CloseChangePasswordDialog : SettingsUiEvent
    data class UpdateCurrentPassword(val value: String) : SettingsUiEvent
    data class UpdateNewPassword(val value: String) : SettingsUiEvent
    data class UpdateConfirmPassword(val value: String) : SettingsUiEvent
    object ToggleCurrentPasswordVisibility : SettingsUiEvent
    object ToggleNewPasswordVisibility : SettingsUiEvent
    object ToggleConfirmPasswordVisibility : SettingsUiEvent
    object SubmitChangePassword : SettingsUiEvent

    // Subscription & Checkout
    object InitiateChangePlan : SettingsUiEvent
    object CancelSubscriptionClicked : SettingsUiEvent
    object DismissExternalCheckoutBottomSheet : SettingsUiEvent
    object ConfirmOpenExternalCheckout : SettingsUiEvent
    object DismissCancelSubscriptionBottomSheet : SettingsUiEvent

    // Delete Account
    object OpenDeleteAccountConfirmation : SettingsUiEvent
    object DismissDeleteAccountConfirmation : SettingsUiEvent
    data class UpdateDeleteAccountEmail(val value: String) : SettingsUiEvent
    object ConfirmDeleteAccount : SettingsUiEvent

    // Feedback Dialogs
    object DismissInfoDialog : SettingsUiEvent
}
