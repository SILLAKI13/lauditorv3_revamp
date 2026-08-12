package com.digicoffer.lauditor.core.ui.models

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable

@Immutable
data class MenuItemModel(
    val id: String,
    val title: String,
    @param:DrawableRes val iconRes: Int? = null,
    val isSelected: Boolean = false,
    val badgeCount: Int? = null,
    val onClick: () -> Unit
)
