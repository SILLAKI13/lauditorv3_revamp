package com.digicoffer.lauditor.core.ui.containers

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme
import com.digicoffer.lauditor.core.ui.foundation.AppText

@Composable
fun AppDialog(
    title: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmText: String = "OK",
    dismissText: String? = "Cancel",
    content: @Composable (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { AppText(text = title, style = LauditorTheme.typography.titleMedium) },
        text = content,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText)
            }
        },
        dismissButton = dismissText?.let {
            {
                TextButton(onClick = onDismiss) {
                    Text(it)
                }
            }
        },
        modifier = modifier
    )
}

@Preview(showBackground = true, name = "AppDialog Preview")
@Composable
fun AppDialogPreview() {
    LauditorTheme {
        AppDialog(title = "Dialog Title Modal", onConfirm = {}, onDismiss = {})
    }
}
