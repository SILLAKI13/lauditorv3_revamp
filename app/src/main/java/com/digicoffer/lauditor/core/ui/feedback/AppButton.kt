package com.digicoffer.lauditor.core.ui.feedback

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme

enum class ButtonVariant { Primary, Secondary, Outline, Danger }

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Primary,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: @Composable (() -> Unit)? = null
) {
    val containerColor = when (variant) {
        ButtonVariant.Primary -> LauditorTheme.colors.primary
        ButtonVariant.Secondary -> LauditorTheme.colors.secondary
        ButtonVariant.Outline -> Color.Transparent
        ButtonVariant.Danger -> LauditorTheme.colors.error
    }

    val contentColor = when (variant) {
        ButtonVariant.Outline -> LauditorTheme.colors.primary
        else -> LauditorTheme.colors.onPrimary
    }

    if (variant == ButtonVariant.Outline) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (icon != null) {
                        icon()
                        Spacer(modifier = Modifier.width(LauditorTheme.dimensions.small))
                    }
                    Text(text = text, color = contentColor)
                }
            }
        }
    } else {
        Button(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled && !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = containerColor)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = contentColor)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (icon != null) {
                        icon()
                        Spacer(modifier = Modifier.width(LauditorTheme.dimensions.small))
                    }
                    Text(text = text, color = contentColor)
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "AppButton Primary Preview")
@Composable
fun AppButtonPreview() {
    LauditorTheme {
        AppButton(text = "Submit Action", onClick = {})
    }
}

@Preview(showBackground = true, name = "AppButton Loading Preview")
@Composable
fun AppButtonLoadingPreview() {
    LauditorTheme {
        AppButton(text = "Submitting...", onClick = {}, isLoading = true)
    }
}
