package com.digicoffer.lauditor.feature.members.presentation.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.ui.common.dialogs.AppDialog

private val GillSans = FontFamily(Font(R.font.gill_sans_regular))

/**
 * Standard Alert/Confirmation dialog for Member actions delegating to canonical [AppDialog].
 */
@Composable
fun MembersAlertDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    confirmLabel: String = "OK",
    showCancel: Boolean = false,
    cancelLabel: String = "Cancel",
    onCancel: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    AppDialog(
        title = title,
        confirmText = confirmLabel,
        dismissText = if (showCancel) cancelLabel else null,
        onConfirm = onConfirm,
        onDismiss = { onCancel?.invoke() ?: onDismiss() },
        content = {
            Text(
                text = message,
                color = Color(0xFF333333),
                fontSize = 15.sp,
                fontFamily = GillSans,
                textAlign = TextAlign.Center
            )
        }
    )
}
