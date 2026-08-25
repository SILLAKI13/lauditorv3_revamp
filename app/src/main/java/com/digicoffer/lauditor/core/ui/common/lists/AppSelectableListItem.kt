package com.digicoffer.lauditor.core.ui.common.lists

import androidx.compose.material3.Checkbox
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme

@Composable
fun AppSelectableListItem(
    title: String,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    isMultiSelect: Boolean = true
) {
    AppListItem(
        title = title,
        subtitle = subtitle,
        modifier = modifier,
        onClick = { onSelectionChange(!isSelected) },
        trailingContent = {
            if (isMultiSelect) {
                Checkbox(checked = isSelected, onCheckedChange = onSelectionChange)
            } else {
                RadioButton(selected = isSelected, onClick = { onSelectionChange(!isSelected) })
            }
        }
    )
}

@Preview(showBackground = true, name = "AppSelectableListItem Preview")
@Composable
fun AppSelectableListItemPreview() {
    LauditorTheme {
        AppSelectableListItem(title = "Selectable Row Item", isSelected = true, onSelectionChange = {})
    }
}
