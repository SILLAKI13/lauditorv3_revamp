package com.digicoffer.lauditor.core.ui.common.navigation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme
import com.digicoffer.lauditor.core.ui.common.foundation.AppText

@Composable
fun AppSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = LauditorTheme.dimensions.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppText(text = title, style = LauditorTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.weight(1f))
        if (actionText != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(actionText, color = LauditorTheme.colors.primary)
            }
        }
    }
}

@Preview(showBackground = true, name = "AppSectionHeader Preview")
@Composable
fun AppSectionHeaderPreview() {
    LauditorTheme {
        AppSectionHeader(title = "Section Header Title", actionText = "See All", onActionClick = {})
    }
}
