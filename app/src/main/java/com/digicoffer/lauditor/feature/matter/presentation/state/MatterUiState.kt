package com.digicoffer.lauditor.feature.matter.presentation.state

import com.digicoffer.lauditor.Matter.Models.ViewMatterModel

data class MatterUiState(
    val matterList: List<ViewMatterModel> = emptyList(),
    val filteredMatters: List<ViewMatterModel> = emptyList(),
    val currentPageList: List<ViewMatterModel> = emptyList(),
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val isLoading: Boolean = false,
    val toastMessage: String? = null,
    val alertTitle: String? = null,
    val alertMessage: String? = null,
    val nextCursor: String? = null,
    val prevCursor: String? = null,
    val hasPrev: Boolean = false,
    val hasNext: Boolean = false,
    val searchQuery: String = ""
)
