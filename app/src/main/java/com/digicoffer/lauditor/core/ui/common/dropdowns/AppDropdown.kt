package com.digicoffer.lauditor.core.ui.common.dropdowns

import com.digicoffer.lauditor.core.ui.common.search.AppSearchField

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme
import com.digicoffer.lauditor.core.ui.common.foundation.AppText
import com.digicoffer.lauditor.core.ui.common.models.DropdownItem

@Composable
fun <T> AppDropdown(
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Select Option",
    itemLabelMapper: (T) -> String = { it.toString() },
    enabled: Boolean = true,
    isSearchable: Boolean = false,
    isMultiSelect: Boolean = false,
    selectedOptions: Set<T> = emptySet(),
    onMultiOptionSelected: ((Set<T>) -> Unit)? = null,
    // Flexible overrides for inline custom drop list:
    isInline: Boolean = false,
    isExpanded: Boolean = false,
    onExpandedChange: ((Boolean) -> Unit)? = null,
    customHeader: @Composable (() -> Unit)? = null,
    customContent: @Composable (() -> Unit)? = null
) {
    if (isInline) {
        Column(modifier = modifier) {
            if (customHeader != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = enabled) { onExpandedChange?.invoke(!isExpanded) }
                ) {
                    customHeader()
                }
            }
            AnimatedVisibility(visible = isExpanded) {
                if (customContent != null) {
                    customContent()
                }
            }
        }
    } else {
        var expanded by remember { mutableStateOf(false) }
        var searchQuery by remember { mutableStateOf("") }

        val filteredOptions = if (isSearchable && searchQuery.isNotEmpty()) {
            options.filter { itemLabelMapper(it).contains(searchQuery, ignoreCase = true) }
        } else {
            options
        }

        val displayText = if (isMultiSelect) {
            if (selectedOptions.isEmpty()) "" else selectedOptions.joinToString(", ") { itemLabelMapper(it) }
        } else {
            selectedOption?.let { itemLabelMapper(it) } ?: ""
        }

        Box(modifier = modifier) {
            OutlinedTextField(
                value = displayText,
                onValueChange = {},
                readOnly = true,
                enabled = enabled,
                label = { Text(label) },
                trailingIcon = {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown menu",
                        modifier = Modifier.clickable(enabled = enabled) { expanded = !expanded }
                    )
                },
                modifier = Modifier.fillMaxWidth().clickable(enabled = enabled) { expanded = !expanded }
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.heightIn(max = 300.dp)
            ) {
                if (isSearchable) {
                    AppSearchField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        onClearClick = { searchQuery = "" },
                        placeholder = "Search options...",
                        modifier = Modifier.padding(LauditorTheme.dimensions.small)
                    )
                }
                filteredOptions.forEach { item ->
                    val isSelected = if (isMultiSelect) selectedOptions.contains(item) else item == selectedOption

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isMultiSelect) {
                                    Checkbox(checked = isSelected, onCheckedChange = null)
                                }
                                AppText(
                                    text = itemLabelMapper(item),
                                    modifier = Modifier.padding(start = LauditorTheme.dimensions.small)
                                )
                            }
                        },
                        onClick = {
                            if (isMultiSelect && onMultiOptionSelected != null) {
                                val updated = if (isSelected) selectedOptions - item else selectedOptions + item
                                onMultiOptionSelected(updated)
                            } else {
                                onOptionSelected(item)
                                expanded = false
                            }
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "AppDropdown Single Select Preview")
@Composable
fun AppDropdownPreview() {
    val options = listOf("Option 1", "Option 2", "Option 3")
    LauditorTheme {
        AppDropdown(
            options = options,
            selectedOption = "Option 1",
            onOptionSelected = {},
            label = "Category"
        )
    }
}
