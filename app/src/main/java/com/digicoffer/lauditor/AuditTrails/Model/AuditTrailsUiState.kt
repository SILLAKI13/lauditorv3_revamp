package com.digicoffer.lauditor.AuditTrails.Model

import java.util.Date

data class AuditTrailsUiState(
    val selectedCategory: String = "",
    val searchQuery: String = "",
    val startDate: Date? = null,
    val endDate: Date? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val filteredItems: List<AuditsModel> = emptyList(),
    val pageItems: List<AuditsModel> = emptyList(),
    val isLoading: Boolean = false
)
