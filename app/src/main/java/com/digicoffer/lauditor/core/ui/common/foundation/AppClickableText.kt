package com.digicoffer.lauditor.core.ui.common.foundation

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme

@Composable
fun AppClickableText(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: TextStyle = LauditorTheme.typography.bodyRegular.copy(textDecoration = TextDecoration.Underline),
    color: Color = LauditorTheme.colors.primary,
    enabled: Boolean = true
) {
    Text(
        text = text,
        modifier = modifier.clickable(enabled = enabled, onClick = onClick),
        style = style,
        color = if (enabled) color else color.copy(alpha = 0.5f)
    )
}

@Preview(showBackground = true, name = "AppClickableText Preview")
@Composable
fun AppClickableTextPreview() {
    LauditorTheme {
        AppClickableText(text = "Clickable Text Action", onClick = {})
    }
}
