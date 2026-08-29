package com.digicoffer.lauditor.feature.notifications.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.digicoffer.lauditor.core.ui.common.search.AppSearchField

/**
 * Standard search bar for notifications/lists delegating directly to canonical [AppSearchField].
 */
@Composable
fun NotificationsSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search notifications..."
) {
    AppSearchField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = placeholder,
        modifier = modifier.padding(10.dp)
    )
}
