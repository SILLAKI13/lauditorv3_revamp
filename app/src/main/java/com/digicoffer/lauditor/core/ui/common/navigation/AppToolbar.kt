package com.digicoffer.lauditor.core.ui.common.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme
import com.digicoffer.lauditor.core.ui.common.foundation.AppText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppToolbar(
    title: String,
    onNavigationClick: () -> Unit,
    modifier: Modifier = Modifier,
    onNotificationClick: (() -> Unit)? = null,
    notificationCount: Int = 0,
    actions: @Composable (() -> Unit)? = null
) {
    TopAppBar(
        title = { AppText(text = title, style = LauditorTheme.typography.headerTitle, color = LauditorTheme.colors.onPrimary) },
        navigationIcon = {
            IconButton(onClick = onNavigationClick) {
                Icon(Icons.Default.Menu, contentDescription = "Open Drawer", tint = LauditorTheme.colors.onPrimary)
            }
        },
        actions = {
            if (onNotificationClick != null) {
                IconButton(onClick = onNotificationClick) {
                    BadgedBox(
                        badge = {
                            if (notificationCount > 0) {
                                Badge { Text("$notificationCount") }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = LauditorTheme.colors.onPrimary)
                    }
                }
            }
            actions?.invoke()
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = LauditorTheme.colors.primary),
        modifier = modifier
    )
}

@Preview(showBackground = true, name = "AppToolbar Preview")
@Composable
fun AppToolbarPreview() {
    LauditorTheme {
        AppToolbar(title = "App Title Header", onNavigationClick = {}, notificationCount = 3)
    }
}
