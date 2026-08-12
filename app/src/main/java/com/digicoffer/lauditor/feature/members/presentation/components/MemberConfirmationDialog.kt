package com.digicoffer.lauditor.feature.members.presentation.components

import androidx.compose.runtime.Composable

@Composable
fun MemberConfirmationDialog(
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    MembersAlertDialog(
        title = "Confirmation",
        message = message,
        showCancel = true,
        confirmLabel = "Yes",
        cancelLabel = "No",
        onConfirm = onConfirm,
        onCancel = onDismiss,
        onDismiss = onDismiss
    )
}
