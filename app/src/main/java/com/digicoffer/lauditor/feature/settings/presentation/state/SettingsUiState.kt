package com.digicoffer.lauditor.feature.settings.presentation.state

import com.digicoffer.lauditor.core.ui.common.feedback.PasswordRequirementItem
import com.digicoffer.lauditor.feature.settings.data.model.LoggedInDevice
import com.digicoffer.lauditor.feature.settings.data.model.SecurityInfo
import com.digicoffer.lauditor.feature.settings.data.model.SubscriptionInfo
import com.digicoffer.lauditor.feature.settings.data.model.TermsInfo

data class SettingsUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    
    // Overview Data
    val security: SecurityInfo? = null,
    val subscription: SubscriptionInfo? = null,
    val terms: TermsInfo? = null,
    val loggedInDevices: List<LoggedInDevice> = emptyList(),

    // Change Password Form & Dialog
    val isChangePasswordOpen: Boolean = false,
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val currentPasswordVisible: Boolean = false,
    val newPasswordVisible: Boolean = false,
    val confirmPasswordVisible: Boolean = false,
    val changePasswordLoading: Boolean = false,
    val changePasswordError: String? = null,

    // External Checkout Flow (Bottom Sheet)
    val isExternalCheckoutBottomSheetOpen: Boolean = false,
    val checkoutTicket: String? = null,
    val checkoutUrl: String? = null,
    val checkoutLoading: Boolean = false,

    // Cancel Subscription Flow (Bottom Sheet)
    val isCancelSubscriptionBottomSheetOpen: Boolean = false,

    // Delete Account Flow
    val isDeleteAccountConfirmationOpen: Boolean = false,
    val deleteAccountLoading: Boolean = false,
    val deleteAccountConfirmationEmail: String = "",
    val deleteAccountError: String? = null,

    // Feedback Dialogs
    val isInfoDialogOpen: Boolean = false,
    val dialogTitle: String = "",
    val dialogMessage: String = "",
    val isSuccessMessage: Boolean = false,
    val shouldLogoutOnDismiss: Boolean = false
) {
    // Password validation rules matching ResetPasswordScreen & reset_password_file
    val isLengthMet: Boolean get() = newPassword.length in 8..15
    val isCasingMet: Boolean get() = newPassword.matches(Regex(".*[A-Z].*")) && newPassword.matches(Regex(".*[a-z].*"))
    val isNumSpecialMet: Boolean get() = newPassword.matches(Regex(".*[0-9].*")) && newPassword.matches(Regex(".*[-!@#$%&*^+=_].*"))
    val isAllPasswordRulesMet: Boolean get() = isLengthMet && isCasingMet && isNumSpecialMet

    val passwordRequirements: List<PasswordRequirementItem>
        get() = listOf(
            PasswordRequirementItem("Must be 8–15 characters long", isLengthMet),
            PasswordRequirementItem("Must include uppercase and lowercase letters", isCasingMet),
            PasswordRequirementItem("Must include a number and a special character", isNumSpecialMet)
        )
}
