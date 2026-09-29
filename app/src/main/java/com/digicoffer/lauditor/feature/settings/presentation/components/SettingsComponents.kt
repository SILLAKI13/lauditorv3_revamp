package com.digicoffer.lauditor.feature.settings.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.ui.common.feedback.AppPasswordRequirements
import com.digicoffer.lauditor.core.ui.common.foundation.AppSpacer
import com.digicoffer.lauditor.feature.settings.data.model.LoggedInDevice
import com.digicoffer.lauditor.feature.settings.data.model.SubscriptionInfo
import com.digicoffer.lauditor.feature.settings.data.model.TermsInfo
import com.digicoffer.lauditor.feature.settings.presentation.state.SettingsUiEvent
import com.digicoffer.lauditor.feature.settings.presentation.state.SettingsUiState
import java.text.SimpleDateFormat
import java.util.Locale

val GillSans = FontFamily(Font(R.font.gill_sans))
val GillSansBold = FontFamily(Font(R.font.gill_sans_bold))

val SettingsNavy = Color(0xFF004D87)
val SettingsCardBorder = Color(0xFFE5E7EB)
val SettingsTextDark = Color(0xFF111827)
val SettingsTextMuted = Color(0xFF6B7280)
val SettingsGreenBg = Color(0xFFDCFCE7)
val SettingsGreenText = Color(0xFF166534)
val SettingsGreenBorder = Color(0xFF86EFAC)
val SettingsRed = Color(0xFFDC2626)
val SettingsRedBg = Color(0xFFFEE2E2)
val SettingsRedBorder = Color(0xFFFCA5A5)
val SettingsBlueIconBg = Color(0xFFEFF6FF)

// ═════════════════════════════════════════════════════════════════════════════
// 1. PASSWORD CARD
// ═════════════════════════════════════════════════════════════════════════════
@Composable
fun PasswordCard(
    onOpenChangePassword: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, SettingsCardBorder),
        shadowElevation = 0.5.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Lock Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(SettingsBlueIconBg, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Password",
                    tint = SettingsNavy,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Center Text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Password",
                    color = SettingsNavy,
                    fontSize = 14.5.sp,
                    fontFamily = GillSansBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Update your password to keep your account secure.",
                    color = SettingsTextMuted,
                    fontSize = 11.5.sp,
                    fontFamily = GillSans,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right Button
            Surface(
                onClick = onOpenChangePassword,
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, SettingsNavy),
                modifier = Modifier.height(32.dp)
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Change Password",
                        color = SettingsNavy,
                        fontSize = 11.5.sp,
                        fontFamily = GillSansBold
                    )
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 2. UPDATE PASSWORD MODAL / DIALOG
// ═════════════════════════════════════════════════════════════════════════════
@Composable
fun UpdatePasswordDialog(
    state: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit
) {
    if (!state.isChangePasswordOpen) return

    Dialog(
        onDismissRequest = {
            if (!state.changePasswordLoading) {
                onEvent(SettingsUiEvent.CloseChangePasswordDialog)
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(enabled = !state.changePasswordLoading) {
                    onEvent(SettingsUiEvent.CloseChangePasswordDialog)
                },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Update Password",
                            color = SettingsNavy,
                            fontSize = 17.sp,
                            fontFamily = GillSansBold
                        )
                        IconButton(
                            onClick = { onEvent(SettingsUiEvent.CloseChangePasswordDialog) },
                            enabled = !state.changePasswordLoading,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color(0xFF6B7280)
                            )
                        }
                    }

                    AppSpacer(height = 14.dp)

                    // Error Banner if present
                    if (!state.changePasswordError.isNullOrBlank()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = SettingsRedBg,
                            border = BorderStroke(1.dp, SettingsRedBorder)
                        ) {
                            Text(
                                text = state.changePasswordError,
                                color = SettingsRed,
                                fontSize = 12.sp,
                                fontFamily = GillSans,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        AppSpacer(height = 10.dp)
                    }

                    // Field 1: Current Password
                    PasswordInputField(
                        label = "Current Password",
                        value = state.currentPassword,
                        onValueChange = { onEvent(SettingsUiEvent.UpdateCurrentPassword(it)) },
                        placeholder = "Enter your current password",
                        isVisible = state.currentPasswordVisible,
                        onToggleVisibility = { onEvent(SettingsUiEvent.ToggleCurrentPasswordVisibility) },
                        enabled = !state.changePasswordLoading
                    )

                    AppSpacer(height = 12.dp)

                    // Field 2: New Password
                    PasswordInputField(
                        label = "New Password",
                        value = state.newPassword,
                        onValueChange = { onEvent(SettingsUiEvent.UpdateNewPassword(it)) },
                        placeholder = "Enter your new password",
                        isVisible = state.newPasswordVisible,
                        onToggleVisibility = { onEvent(SettingsUiEvent.ToggleNewPasswordVisibility) },
                        enabled = !state.changePasswordLoading
                    )

                    // Password Requirements checklist (matching Login Reset Password flow)
                    if (state.newPassword.isNotEmpty()) {
                        AppSpacer(height = 6.dp)
                        AppPasswordRequirements(
                            requirements = state.passwordRequirements,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }

                    AppSpacer(height = 12.dp)

                    // Field 3: Confirm Password
                    PasswordInputField(
                        label = "Confirm Password",
                        value = state.confirmPassword,
                        onValueChange = { onEvent(SettingsUiEvent.UpdateConfirmPassword(it)) },
                        placeholder = "Re-enter your new password",
                        isVisible = state.confirmPasswordVisible,
                        onToggleVisibility = { onEvent(SettingsUiEvent.ToggleConfirmPasswordVisibility) },
                        enabled = !state.changePasswordLoading
                    )

                    AppSpacer(height = 18.dp)

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Cancel Button
                        Surface(
                            onClick = { onEvent(SettingsUiEvent.CloseChangePasswordDialog) },
                            enabled = !state.changePasswordLoading,
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE5E7EB),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Cancel",
                                    color = Color(0xFF374151),
                                    fontSize = 13.5.sp,
                                    fontFamily = GillSansBold
                                )
                            }
                        }

                        // Save Password Button
                        Surface(
                            onClick = { onEvent(SettingsUiEvent.SubmitChangePassword) },
                            enabled = !state.changePasswordLoading,
                            shape = RoundedCornerShape(8.dp),
                            color = SettingsNavy,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (state.changePasswordLoading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = "Save Password",
                                        color = Color.White,
                                        fontSize = 13.5.sp,
                                        fontFamily = GillSansBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PasswordInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isVisible: Boolean,
    onToggleVisibility: () -> Unit,
    enabled: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = SettingsNavy,
            fontSize = 12.5.sp,
            fontFamily = GillSansBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp))
                .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                singleLine = true,
                visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                textStyle = TextStyle(
                    fontSize = 13.5.sp,
                    fontFamily = GillSans,
                    color = Color.Black
                ),
                cursorBrush = SolidColor(SettingsNavy),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = 12.5.sp,
                            fontFamily = GillSans,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                    innerTextField()
                },
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onToggleVisibility,
                enabled = enabled,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    painter = painterResource(if (isVisible) R.drawable.ic_visibility else R.drawable.ic_visibility_off),
                    contentDescription = "Toggle password visibility",
                    tint = Color(0xFF6B7280),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 3. SUBSCRIPTION SECTION (ENCLOSED IN WHITE CARD LIKE iOS)
// ═════════════════════════════════════════════════════════════════════════════
@Composable
fun SubscriptionSection(
    subscription: SubscriptionInfo?,
    terms: TermsInfo?,
    onInitiateChangePlan: () -> Unit,
    onCancelSubscription: () -> Unit,
    checkoutLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, SettingsCardBorder),
        shadowElevation = 0.5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Subscription",
                    color = SettingsNavy,
                    fontSize = 16.5.sp,
                    fontFamily = GillSansBold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Change Plan / Subscribe Button
                    val planActionText = subscription?.actions?.primary?.label ?: "Change Plan"
                    Surface(
                        onClick = onInitiateChangePlan,
                        enabled = !checkoutLoading,
                        shape = RoundedCornerShape(8.dp),
                        color = SettingsNavy,
                        modifier = Modifier.height(30.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (checkoutLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = planActionText,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontFamily = GillSansBold
                                )
                            }
                        }
                    }

                    // Cancel Subscription Button
                    val secondaryAction = subscription?.actions?.secondary
                    val showCancel = secondaryAction != null || (subscription?.plan?.key != "free" && subscription?.plan?.amount != 0.0)
                    if (showCancel) {
                        val cancelText = secondaryAction?.label ?: "Cancel Subscription"
                        Surface(
                            onClick = onCancelSubscription,
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, SettingsRed),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cancelText,
                                    color = SettingsRed,
                                    fontSize = 12.sp,
                                    fontFamily = GillSansBold
                                )
                            }
                        }
                    }
                }
            }

            AppSpacer(height = 10.dp)

            // Current Plan Banner Card
            CurrentPlanBannerCard(subscription = subscription)

            AppSpacer(height = 10.dp)

            // 2-Column Grid of Detail Tiles
            val planName = subscription?.plan?.name ?: "Free Plan"
            val billingCycle = subscription?.plan?.billingCycle ?: "Monthly"
            val statusText = subscription?.status ?: "Active"
            val startDate = subscription?.startDate ?: "N/A"
            val validityText = subscription?.validity?.getFormattedValidity() ?: "Unlimited"
            val nextBillingDate = subscription?.nextBillingDate ?: "N/A"
            val paymentMethod = subscription?.payment?.method?.lowercase(Locale.ROOT) ?: "N/A"
            val activatedOn = subscription?.activatedOn ?: "N/A"
            val tcAcceptedOn = formatTermsDate(terms?.acceptedAt)

            val detailTiles = listOf(
                DetailTileData("Plan Details", planName, iconRes = R.drawable.ic_briefcase),
                DetailTileData("Billing Cycle", billingCycle, iconVector = Icons.Default.Refresh),
                DetailTileData("Status", statusText, iconVector = Icons.Default.CheckCircle, isStatusGreen = true),
                DetailTileData("Subscription Start Date", startDate, iconVector = Icons.Default.DateRange),
                DetailTileData("Plan Validity", validityText, iconRes = R.drawable.ic_hourglass),
                DetailTileData("Next Billing Date", nextBillingDate, iconVector = Icons.Default.DateRange),
                DetailTileData("Mode of Payment", paymentMethod, iconRes = R.drawable.ic_credit_card),
                DetailTileData("Account Activated On", activatedOn, iconVector = Icons.Default.DateRange),
                DetailTileData("T&C Accepted On", tcAcceptedOn, iconRes = R.drawable.ic_terms_doc)
            )

            // Render tiles in 2-column rows
            for (i in detailTiles.indices step 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SubscriptionDetailTile(
                        data = detailTiles[i],
                        modifier = Modifier.weight(1f)
                    )
                    if (i + 1 < detailTiles.size) {
                        SubscriptionDetailTile(
                            data = detailTiles[i + 1],
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                if (i + 2 < detailTiles.size) {
                    AppSpacer(height = 8.dp)
                }
            }
        }
    }
}

@Composable
private fun CurrentPlanBannerCard(subscription: SubscriptionInfo?) {
    val planName = subscription?.plan?.name ?: "Premium"
    val statusText = subscription?.status ?: "Active"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = BorderStroke(1.dp, SettingsGreenBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Green Briefcase Icon in Pale Green Box
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(SettingsGreenBg, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_briefcase),
                    contentDescription = "Plan",
                    tint = SettingsGreenText,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Current Plan Pill Badge
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SettingsGreenBg
            ) {
                Text(
                    text = "Current Plan",
                    color = SettingsGreenText,
                    fontSize = 11.sp,
                    fontFamily = GillSansBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Plan Title
            Text(
                text = planName,
                color = SettingsTextDark,
                fontSize = 18.sp,
                fontFamily = GillSansBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Active Pill Badge
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SettingsGreenBg
            ) {
                Text(
                    text = statusText,
                    color = SettingsGreenText,
                    fontSize = 11.sp,
                    fontFamily = GillSansBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Description
            Text(
                text = "Everything you need to run your practice efficiently.",
                color = Color(0xFF4B5563),
                fontSize = 12.sp,
                fontFamily = GillSans,
                textAlign = TextAlign.Center
            )
        }
    }
}

data class DetailTileData(
    val label: String,
    val value: String,
    val iconVector: ImageVector? = null,
    val iconRes: Int? = null,
    val isStatusGreen: Boolean = false
)

@Composable
private fun SubscriptionDetailTile(
    data: DetailTileData,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.heightIn(min = 54.dp),
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        border = BorderStroke(1.dp, SettingsCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(SettingsBlueIconBg, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (data.iconVector != null) {
                    Icon(
                        imageVector = data.iconVector,
                        contentDescription = data.label,
                        tint = SettingsNavy,
                        modifier = Modifier.size(16.dp)
                    )
                } else if (data.iconRes != null) {
                    Icon(
                        painter = painterResource(data.iconRes),
                        contentDescription = data.label,
                        tint = SettingsNavy,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = data.label,
                    color = SettingsTextMuted,
                    fontSize = 10.5.sp,
                    fontFamily = GillSans,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = data.value.ifBlank { "N/A" },
                    color = if (data.isStatusGreen && data.value.equals("Active", ignoreCase = true)) SettingsGreenText else SettingsTextDark,
                    fontSize = 12.5.sp,
                    fontFamily = GillSansBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 4. LOGGED-IN DEVICES SECTION (ENCLOSED IN WHITE CARD LIKE iOS)
// ═════════════════════════════════════════════════════════════════════════════
@Composable
fun LoggedInDevicesSection(
    devices: List<LoggedInDevice>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, SettingsCardBorder),
        shadowElevation = 0.5.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = "Logged-In Devices",
                color = SettingsNavy,
                fontSize = 16.5.sp,
                fontFamily = GillSansBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "These devices currently have access to your account.",
                color = SettingsTextMuted,
                fontSize = 11.5.sp,
                fontFamily = GillSans
            )

            AppSpacer(height = 10.dp)

            if (devices.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF9FAFB),
                    border = BorderStroke(1.dp, SettingsCardBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No devices currently logged in.",
                            color = SettingsTextMuted,
                            fontSize = 12.5.sp,
                            fontFamily = GillSans
                        )
                    }
                }
            } else {
                // Bounded inner scroll area so that the whole screen doesn't have to scroll endlessly
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        devices.forEachIndexed { index, device ->
                            LoggedInDeviceCard(device = device)
                            if (index < devices.size - 1) {
                                AppSpacer(height = 6.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoggedInDeviceCard(
    device: LoggedInDevice,
    modifier: Modifier = Modifier
) {
    val isCurrent = device.isCurrent || device.currentDevice
    val iconRes = getDeviceIconRes(device.deviceName, device.deviceType)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        border = BorderStroke(1.dp, SettingsCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Device Icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(SettingsBlueIconBg, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = device.deviceName,
                    tint = SettingsNavy,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Name + Current Device Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = device.deviceName.ifBlank { "Unknown Device" },
                        color = SettingsTextDark,
                        fontSize = 13.sp,
                        fontFamily = GillSansBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (isCurrent) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SettingsGreenBg
                        ) {
                            Text(
                                text = "Current Device",
                                color = SettingsGreenText,
                                fontSize = 10.sp,
                                fontFamily = GillSansBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Platform / Type
                Text(
                    text = device.deviceType.ifBlank { "Operating System" },
                    color = SettingsTextMuted,
                    fontSize = 11.sp,
                    fontFamily = GillSans
                )

                Spacer(modifier = Modifier.height(1.dp))

                // Last Active
                Text(
                    text = device.lastActive.ifBlank { "Recently active" },
                    color = Color(0xFF374151),
                    fontSize = 11.sp,
                    fontFamily = GillSans
                )
            }
        }
    }
}

private fun getDeviceIconRes(name: String, type: String): Int {
    val combined = "$name $type".lowercase(Locale.ROOT)
    return when {
        combined.contains("iphone") || combined.contains("ios") || combined.contains("android") || combined.contains("samsung") || combined.contains("vivo") || combined.contains("phone") -> R.drawable.ic_phone
        combined.contains("windows") || combined.contains("mac") || combined.contains("desktop") -> R.drawable.ic_computer
        combined.contains("linux") || combined.contains("chrome") || combined.contains("firefox") || combined.contains("safari") -> R.drawable.ic_globe
        else -> R.drawable.ic_computer
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 5. DELETE ACCOUNT SECTION
// ═════════════════════════════════════════════════════════════════════════════
@Composable
fun DeleteAccountSection(
    onOpenDeleteAccountConfirmation: () -> Unit,
    deleteAccountLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, SettingsRedBorder),
        shadowElevation = 0.5.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Red Trash Icon in Light Pink Box
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(SettingsRedBg, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Account",
                    tint = SettingsRed,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Center Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Delete Account",
                    color = SettingsRed,
                    fontSize = 14.5.sp,
                    fontFamily = GillSansBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Are you sure to permanently delete account?",
                    color = Color(0xFF374151),
                    fontSize = 11.5.sp,
                    fontFamily = GillSans,
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = "This action cannot be undone.",
                    color = SettingsRed,
                    fontSize = 11.sp,
                    fontFamily = GillSans
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Delete Account Button
            Surface(
                onClick = onOpenDeleteAccountConfirmation,
                enabled = !deleteAccountLoading,
                shape = RoundedCornerShape(8.dp),
                color = SettingsRed,
                modifier = Modifier.height(32.dp)
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (deleteAccountLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Delete Account",
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontFamily = GillSansBold
                        )
                    }
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 6. EXTERNAL CHECKOUT BOTTOM SHEET (iOS-Style ModalBottomSheet)
// ═════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExternalCheckoutBottomSheet(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onContinue: () -> Unit
) {
    if (!isOpen) return

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Leave Application",
                color = SettingsNavy,
                fontSize = 18.sp,
                fontFamily = GillSansBold
            )

            AppSpacer(height = 10.dp)

            Text(
                text = "You are about to leave the app and go to an external website.",
                color = SettingsTextDark,
                fontSize = 14.5.sp,
                fontFamily = GillSansBold,
                lineHeight = 20.sp
            )

            AppSpacer(height = 8.dp)

            Text(
                text = "Any accounts or purchases made outside of this app will be managed by the developer Lex-Z Lawyers. Your account, stored payment method and related features, such as subscription management and refund requests, will be available on the website.",
                color = Color(0xFF4B5563),
                fontSize = 12.5.sp,
                fontFamily = GillSans,
                lineHeight = 17.sp
            )

            AppSpacer(height = 20.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFE5E7EB),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Cancel",
                            color = Color(0xFF374151),
                            fontSize = 13.5.sp,
                            fontFamily = GillSansBold
                        )
                    }
                }

                Surface(
                    onClick = onContinue,
                    shape = RoundedCornerShape(20.dp),
                    color = SettingsNavy,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Continue",
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontFamily = GillSansBold
                        )
                    }
                }
            }

            AppSpacer(height = 16.dp)
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 7. CANCEL SUBSCRIPTION BOTTOM SHEET (iOS-Style ModalBottomSheet)
// ═════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CancelSubscriptionBottomSheet(
    isOpen: Boolean,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Cancel Subscription",
                color = SettingsNavy,
                fontSize = 18.sp,
                fontFamily = GillSansBold
            )

            AppSpacer(height = 10.dp)

            Text(
                text = "To cancel or modify your active subscription plan, please visit our web portal or contact customer support at support@lexiz.ai.",
                color = Color(0xFF374151),
                fontSize = 13.sp,
                fontFamily = GillSans,
                lineHeight = 18.sp
            )

            AppSpacer(height = 8.dp)

            Text(
                text = "Your current plan benefits will remain active until the end of the current billing cycle.",
                color = SettingsTextMuted,
                fontSize = 12.sp,
                fontFamily = GillSans,
                lineHeight = 16.sp
            )

            AppSpacer(height = 20.dp)

            Surface(
                onClick = onDismiss,
                shape = RoundedCornerShape(20.dp),
                color = SettingsNavy,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Got It",
                        color = Color.White,
                        fontSize = 13.5.sp,
                        fontFamily = GillSansBold
                    )
                }
            }

            AppSpacer(height = 16.dp)
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 8. DELETE ACCOUNT CONFIRMATION DIALOG (iOS-STYLE DESIGN)
// ═════════════════════════════════════════════════════════════════════════════
@Composable
fun DeleteAccountConfirmationDialog(
    isOpen: Boolean,
    isLoading: Boolean,
    emailValue: String,
    onEmailChange: (String) -> Unit,
    errorMessage: String? = null,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (!isOpen) return

    Dialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top-right Close (X) button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Centered Circular Red Warning Badge
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(Color(0xFFFFD5D5), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = Color(0xFFE53935),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title
                Text(
                    text = "Delete Your Account Permanently?",
                    color = Color(0xFF111827),
                    fontSize = 17.5.sp,
                    fontFamily = GillSansBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                Text(
                    text = "This action cannot be undone. Your account will be permanently deleted, and you will no longer be able to access it.",
                    color = Color(0xFF374151),
                    fontSize = 13.sp,
                    fontFamily = GillSans,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Email Prompt Label
                Text(
                    text = "Please type in your email to confirm.",
                    color = SettingsNavy,
                    fontSize = 12.5.sp,
                    fontFamily = GillSans,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 2.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Email Input Field
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .background(Color(0xFFF9FAFB), RoundedCornerShape(8.dp))
                        .border(
                            1.dp,
                            if (errorMessage != null) Color(0xFFEF4444) else Color(0xFFD1D5DB),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = emailValue,
                        onValueChange = onEmailChange,
                        enabled = !isLoading,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        textStyle = TextStyle(
                            fontSize = 13.5.sp,
                            fontFamily = GillSans,
                            color = Color.Black
                        ),
                        cursorBrush = SolidColor(SettingsNavy),
                        decorationBox = { innerTextField ->
                            if (emailValue.isEmpty()) {
                                Text(
                                    text = "Enter your email",
                                    fontSize = 13.sp,
                                    fontFamily = GillSans,
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Error text if present
                if (!errorMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = errorMessage,
                        color = Color(0xFFDC2626),
                        fontSize = 11.5.sp,
                        fontFamily = GillSans,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Buttons: "No, Keep it" & "Yes, Delete Account"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // "No, Keep it" Button
                    Surface(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "No, Keep it",
                                color = Color(0xFF111827),
                                fontSize = 13.5.sp,
                                fontFamily = GillSansBold
                            )
                        }
                    }

                    // "Yes, Delete Account" Button
                    Surface(
                        onClick = onConfirm,
                        enabled = !isLoading,
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE53935),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Yes, Delete Account",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontFamily = GillSansBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// 9. GENERAL FEEDBACK / ALERT DIALOG
// ═════════════════════════════════════════════════════════════════════════════
@Composable
fun SettingsFeedbackDialog(
    isOpen: Boolean,
    title: String,
    message: String,
    isSuccess: Boolean,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.9f),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title.ifBlank { if (isSuccess) "Success" else "Confirmation" },
                    color = if (isSuccess) SettingsNavy else SettingsTextDark,
                    fontSize = 16.5.sp,
                    fontFamily = GillSansBold,
                    textAlign = TextAlign.Center
                )

                AppSpacer(height = 10.dp)

                Text(
                    text = message,
                    color = Color(0xFF4B5563),
                    fontSize = 13.sp,
                    fontFamily = GillSans,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                AppSpacer(height = 16.dp)

                Surface(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(8.dp),
                    color = SettingsNavy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "OK",
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontFamily = GillSansBold
                        )
                    }
                }
            }
        }
    }
}

private fun formatTermsDate(dateStr: String?): String {
    if (dateStr.isNullOrBlank()) return "N/A"
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val parsed = inputFormat.parse(dateStr)
        if (parsed != null) outputFormat.format(parsed) else dateStr
    } catch (e: Exception) {
        dateStr
    }
}
