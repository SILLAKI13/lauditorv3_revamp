package com.digicoffer.lauditor.core.ui.feedback

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme
import com.digicoffer.lauditor.core.ui.foundation.AppSpacer
import com.digicoffer.lauditor.core.ui.foundation.AppText

@Composable
fun AppEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxSize().padding(LauditorTheme.dimensions.large),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppSpacer(height = LauditorTheme.dimensions.giant)
        AppText(text = title, style = LauditorTheme.typography.headerTitle)
        if (description != null) {
            AppSpacer(height = LauditorTheme.dimensions.small)
            AppText(text = description, style = LauditorTheme.typography.bodyRegular, color = LauditorTheme.colors.onSurfaceVariant)
        }
        if (actionText != null && onActionClick != null) {
            AppSpacer(height = LauditorTheme.dimensions.large)
            AppButton(text = actionText, onClick = onActionClick)
        }
    }
}

@Preview(showBackground = true, name = "AppEmptyState Preview")
@Composable
fun AppEmptyStatePreview() {
    LauditorTheme {
        AppEmptyState(title = "No Data Found", description = "Try adjusting your filter settings.", actionText = "Refresh", onActionClick = {})
    }
}
