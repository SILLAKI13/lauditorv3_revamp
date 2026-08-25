package com.digicoffer.lauditor.core.ui.common.inputs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme
import com.digicoffer.lauditor.core.ui.common.foundation.AppText

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
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    // Flexible style overrides:
    shape: Shape? = null,
    border: BorderStroke? = null,
    backgroundColor: Color? = null,
    textStyle: TextStyle? = null,
    height: Dp? = null,
    contentPadding: PaddingValues? = null,
    customDecorationBox: @Composable ((innerTextField: @Composable () -> Unit) -> Unit)? = null
) {
    val isCustomStyle = height != null || backgroundColor != null || border != null || customDecorationBox != null

    Column(modifier = modifier) {
        if (isCustomStyle) {
            val baseShape = shape ?: androidx.compose.foundation.shape.RoundedCornerShape(0.dp)
            var baseModifier = Modifier.fillMaxWidth()
            if (height != null) {
                baseModifier = baseModifier.height(height)
            }
            if (border != null) {
                baseModifier = baseModifier.border(border, baseShape)
            }
            if (backgroundColor != null) {
                baseModifier = baseModifier.background(backgroundColor, baseShape)
            }
            if (contentPadding != null) {
                baseModifier = baseModifier.padding(contentPadding)
            }

            Box(
                modifier = baseModifier,
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    enabled = enabled,
                    singleLine = singleLine,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    visualTransformation = visualTransformation,
                    textStyle = textStyle ?: TextStyle.Default,
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = customDecorationBox ?: { innerTextField ->
                        if (value.isEmpty() && placeholder != null) {
                            Text(
                                text = placeholder,
                                style = textStyle ?: TextStyle.Default
                            )
                        }
                        innerTextField()
                    }
                )
            }
        } else {
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
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                visualTransformation = visualTransformation
            )
        }

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
