package com.digicoffer.lauditor.core.ui.common.models

import androidx.compose.runtime.Immutable

@Immutable
data class SelectionItem<T>(
    val id: String,
    val title: String,
    val description: String? = null,
    val value: T,
    val isSelected: Boolean = false,
    val isEnabled: Boolean = true
)
