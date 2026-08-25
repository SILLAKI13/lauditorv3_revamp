package com.digicoffer.lauditor.feature.matter.presentation.state

import com.digicoffer.lauditor.Matter.Models.ViewMatterModel

data class MatterUiState(
    val matterList: List<ViewMatterModel> = emptyList(),
    val isLoading: Boolean = false,
    val toastMessage: String? = null,
    val nextCursor: String? = null,
    val prevCursor: String? = null,
    val searchQuery: String = ""
)
