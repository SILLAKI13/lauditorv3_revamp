package com.digicoffer.lauditor.core.ui.models

import androidx.compose.runtime.Immutable

@Immutable
data class DropdownItem<T>(
    val id: String,
    val label: String,
    val value: T,
    val isEnabled: Boolean = true
)
