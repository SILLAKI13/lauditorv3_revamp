package com.digicoffer.lauditor.core.ui.common.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
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
    shape: Shape? = null,
    containerColor: Color? = null,
    contentColor: Color? = null,
    border: BorderStroke? = null,
    elevation: ButtonElevation? = null,
    contentPadding: PaddingValues? = null,
    fontFamily: FontFamily? = null,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    textStyle: TextStyle? = null,
    icon: @Composable (() -> Unit)? = null
) {
    val baseContainerColor = containerColor ?: when (variant) {
        ButtonVariant.Primary -> LauditorTheme.colors.primary
        ButtonVariant.Secondary -> LauditorTheme.colors.secondary
        ButtonVariant.Outline -> Color.Transparent
        ButtonVariant.Danger -> LauditorTheme.colors.error
    }

    val baseContentColor = contentColor ?: when (variant) {
        ButtonVariant.Outline -> LauditorTheme.colors.primary
        else -> LauditorTheme.colors.onPrimary
    }
    
    val baseShape = shape ?: when (variant) {
        ButtonVariant.Outline -> ButtonDefaults.outlinedShape
        else -> ButtonDefaults.shape
    }

    if (variant == ButtonVariant.Outline || border != null) {
        val baseBorder = border ?: BorderStroke(
            1.dp,
            LauditorTheme.colors.primary
        )
        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled && !isLoading,
            shape = baseShape,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = baseContainerColor,
                contentColor = baseContentColor
            ),
            border = baseBorder,
            elevation = elevation,
            contentPadding = contentPadding ?: ButtonDefaults.ContentPadding
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = baseContentColor)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (icon != null) {
                        icon()
                        Spacer(modifier = Modifier.width(LauditorTheme.dimensions.small))
                    }
                    Text(
                        text = text,
                        color = baseContentColor,
                        fontFamily = fontFamily,
                        fontSize = fontSize,
                        fontWeight = fontWeight,
                        style = textStyle ?: TextStyle.Default
                    )
                }
            }
        }
    } else {
        Button(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled && !isLoading,
            shape = baseShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = baseContainerColor,
                contentColor = baseContentColor
            ),
            elevation = elevation,
            contentPadding = contentPadding ?: ButtonDefaults.ContentPadding
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = baseContentColor)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (icon != null) {
                        icon()
                        Spacer(modifier = Modifier.width(LauditorTheme.dimensions.small))
                    }
                    Text(
                        text = text,
                        color = baseContentColor,
                        fontFamily = fontFamily,
                        fontSize = fontSize,
                        fontWeight = fontWeight,
                        style = textStyle ?: TextStyle.Default
                    )
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
