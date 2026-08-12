package com.digicoffer.lauditor.core.ui.lists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme
import com.digicoffer.lauditor.core.ui.containers.AppCard
import com.digicoffer.lauditor.core.ui.foundation.AppText

@Composable
fun AppListItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = LauditorTheme.dimensions.extraSmall)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LauditorTheme.dimensions.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                AppText(text = title, style = LauditorTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                if (subtitle != null) {
                    AppText(
                        text = subtitle,
                        style = LauditorTheme.typography.bodySmall,
                        color = LauditorTheme.colors.onSurfaceVariant
                    )
                }
            }
            if (trailingContent != null) {
                Spacer(modifier = Modifier.width(LauditorTheme.dimensions.small))
                trailingContent()
            }
        }
    }
}

@Preview(showBackground = true, name = "AppListItem Preview")
@Composable
fun AppListItemPreview() {
    LauditorTheme {
        AppListItem(title = "Item Title Header", subtitle = "Secondary descriptive information")
    }
}
