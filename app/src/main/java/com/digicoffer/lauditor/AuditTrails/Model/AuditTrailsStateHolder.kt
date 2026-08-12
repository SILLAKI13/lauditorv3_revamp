package com.digicoffer.lauditor.AuditTrails.Model

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import java.util.Date

class AuditTrailsStateHolder {

    private val _uiState = mutableStateOf(AuditTrailsUiState())
    val uiState: State<AuditTrailsUiState> = _uiState

    fun updateSelectedCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category, currentPage = 1)
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query, currentPage = 1)
    }

    fun updateDateRange(start: Date?, end: Date?) {
        _uiState.value = _uiState.value.copy(startDate = start, endDate = end, currentPage = 1)
    }

    fun updateCurrentPage(page: Int) {
        _uiState.value = _uiState.value.copy(currentPage = page)
    }

    fun updateLists(filtered: List<AuditsModel>, pageItems: List<AuditsModel>, totalPages: Int) {
        _uiState.value = _uiState.value.copy(
            filteredItems = filtered,
            pageItems = pageItems,
            totalPages = totalPages
        )
    }

    fun setLoading(loading: Boolean) {
        _uiState.value = _uiState.value.copy(isLoading = loading)
    }
}
