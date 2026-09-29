package com.digicoffer.lauditor.feature.settings.presentation.screen

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.core.ui.common.foundation.AppSpacer
import com.digicoffer.lauditor.feature.settings.presentation.components.*
import com.digicoffer.lauditor.feature.settings.presentation.state.SettingsUiEvent
import com.digicoffer.lauditor.feature.settings.presentation.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF3F6FA))
    ) {
        if (state.isLoading && state.subscription == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = SettingsNavy,
                    strokeWidth = 3.dp
                )
            }
        } else if (state.errorMessage != null && state.subscription == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.errorMessage ?: "Failed to load settings.",
                        color = Color(0xFF374151),
                        fontSize = 14.sp,
                        fontFamily = GillSans
                    )
                    AppSpacer(height = 12.dp)
                    androidx.compose.material3.Button(
                        onClick = { viewModel.onEvent(SettingsUiEvent.LoadOverview) },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = SettingsNavy)
                    ) {
                        Text(
                            text = "Retry",
                            color = Color.White,
                            fontFamily = GillSansBold
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                // 1. Password Section
                PasswordCard(
                    onOpenChangePassword = {
                        viewModel.onEvent(SettingsUiEvent.OpenChangePasswordDialog)
                    }
                )

                AppSpacer(height = 12.dp)

                // 2. Subscription Section
                SubscriptionSection(
                    subscription = state.subscription,
                    terms = state.terms,
                    onInitiateChangePlan = {
                        viewModel.onEvent(SettingsUiEvent.InitiateChangePlan)
                    },
                    onCancelSubscription = {
                        viewModel.onEvent(SettingsUiEvent.CancelSubscriptionClicked)
                    },
                    checkoutLoading = state.checkoutLoading
                )

                AppSpacer(height = 12.dp)

                // 3. Logged-In Devices Section (with internal bounded scrolling)
                LoggedInDevicesSection(
                    devices = state.loggedInDevices
                )

                AppSpacer(height = 12.dp)

                // 4. Delete Account Section
                DeleteAccountSection(
                    onOpenDeleteAccountConfirmation = {
                        viewModel.onEvent(SettingsUiEvent.OpenDeleteAccountConfirmation)
                    },
                    deleteAccountLoading = state.deleteAccountLoading
                )

                AppSpacer(height = 20.dp)
            }
        }

        // ── Modals & Dialogs ────────────────────────────────────────────────
        UpdatePasswordDialog(
            state = state,
            onEvent = viewModel::onEvent
        )

        ExternalCheckoutBottomSheet(
            isOpen = state.isExternalCheckoutBottomSheetOpen,
            onDismiss = { viewModel.onEvent(SettingsUiEvent.DismissExternalCheckoutBottomSheet) },
            onContinue = {
                val url = state.checkoutUrl
                viewModel.onEvent(SettingsUiEvent.ConfirmOpenExternalCheckout)
                if (!url.isNullOrBlank()) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    } catch (ex: Exception) {
                        Toast.makeText(context, "Unable to open browser: ${ex.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )

        CancelSubscriptionBottomSheet(
            isOpen = state.isCancelSubscriptionBottomSheetOpen,
            onDismiss = { viewModel.onEvent(SettingsUiEvent.DismissCancelSubscriptionBottomSheet) }
        )

        DeleteAccountConfirmationDialog(
            isOpen = state.isDeleteAccountConfirmationOpen,
            isLoading = state.deleteAccountLoading,
            emailValue = state.deleteAccountConfirmationEmail,
            onEmailChange = { viewModel.onEvent(SettingsUiEvent.UpdateDeleteAccountEmail(it)) },
            errorMessage = state.deleteAccountError,
            onDismiss = { viewModel.onEvent(SettingsUiEvent.DismissDeleteAccountConfirmation) },
            onConfirm = { viewModel.onEvent(SettingsUiEvent.ConfirmDeleteAccount) }
        )

        SettingsFeedbackDialog(
            isOpen = state.isInfoDialogOpen,
            title = state.dialogTitle,
            message = state.dialogMessage,
            isSuccess = state.isSuccessMessage,
            onDismiss = { viewModel.onEvent(SettingsUiEvent.DismissInfoDialog) }
        )
    }
}
