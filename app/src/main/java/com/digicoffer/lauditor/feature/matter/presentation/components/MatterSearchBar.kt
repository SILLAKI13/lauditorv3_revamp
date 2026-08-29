package com.digicoffer.lauditor.feature.matter.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.digicoffer.lauditor.core.ui.common.search.AppSearchField

/**
 * Matter search bar delegating directly to canonical [AppSearchField].
 */
@Composable
fun MatterSearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    onSearchClick: () -> Unit,
    placeholderHint: String = "Search Matter",
    modifier: Modifier = Modifier
) {
    AppSearchField(
        value = query,
        onValueChange = onQueryChanged,
        onSearchClick = onSearchClick,
        placeholder = placeholderHint,
        modifier = modifier.padding(10.dp)
    )
}
