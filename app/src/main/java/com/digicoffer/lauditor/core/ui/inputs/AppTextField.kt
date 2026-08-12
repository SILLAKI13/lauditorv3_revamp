package com.digicoffer.lauditor.core.ui.inputs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme
import com.digicoffer.lauditor.core.ui.foundation.AppText

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    helperText: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = label?.let { { Text(it) } },
            placeholder = placeholder?.let { { Text(it) } },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            isError = isError,
            enabled = enabled,
            singleLine = singleLine,
            keyboardOptions = keyboardOptions
        )
        if (isError && !errorMessage.isNullOrEmpty()) {
            AppText(
                text = errorMessage,
                style = LauditorTheme.typography.caption,
                color = LauditorTheme.colors.error,
                modifier = Modifier.padding(start = LauditorTheme.dimensions.small, top = LauditorTheme.dimensions.extraSmall)
            )
        } else if (!helperText.isNullOrEmpty()) {
            AppText(
                text = helperText,
                style = LauditorTheme.typography.caption,
                color = LauditorTheme.colors.onSurfaceVariant,
                modifier = Modifier.padding(start = LauditorTheme.dimensions.small, top = LauditorTheme.dimensions.extraSmall)
            )
        }
    }
}

@Preview(showBackground = true, name = "AppTextField Preview")
@Composable
fun AppTextFieldPreview() {
    LauditorTheme {
        AppTextField(
            value = "sample@domain.com",
            onValueChange = {},
            label = "Email Address",
            helperText = "Enter your work email"
        )
    }
}

@Preview(showBackground = true, name = "AppTextField Error Preview")
@Composable
fun AppTextFieldErrorPreview() {
    LauditorTheme {
        AppTextField(
            value = "invalid-email",
            onValueChange = {},
            label = "Email Address",
            isError = true,
            errorMessage = "Invalid email format"
        )
    }
}
