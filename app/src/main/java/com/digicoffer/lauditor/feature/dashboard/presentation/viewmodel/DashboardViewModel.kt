package com.digicoffer.lauditor.feature.dashboard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.feature.dashboard.data.repository.DashboardRepository
import com.digicoffer.lauditor.feature.dashboard.presentation.state.DashboardUiEvent
import com.digicoffer.lauditor.feature.dashboard.presentation.state.DashboardUiState
import com.digicoffer.lauditor.feature.dashboard.presentation.state.PendingCardNav
import com.digicoffer.lauditor.feature.dashboard.presentation.state.PendingSubscriptionPay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        updateRoleAndCategory()
    }

    private fun updateRoleAndCategory() {
        _uiState.update {
            it.copy(
                userRole = Constants.ROLE,
                userCategory = Constants.CATEGORY,
                currentDateInfo = DashboardUiState.getCurrentDateInfo(),
                clientChatCount = Constants.clientChat_count.toString().toIntOrNull() ?: 0,
                teamChatCount = Constants.teamChat_count.toString().toIntOrNull() ?: 0
            )
        }
    }

    fun onEvent(event: DashboardUiEvent) {
        when (event) {
            is DashboardUiEvent.LoadDashboard -> loadDashboard(event.isRefresh)
            is DashboardUiEvent.RefreshChatCounts -> refreshChatCounts()
            is DashboardUiEvent.OnCardClick -> {
                if (event.navigation != null && !event.navigation.route_name.isNullOrEmpty()) {
                    _uiState.update { it.copy(pendingNavigation = event.navigation) }
                } else {
                    _uiState.update {
                        it.copy(
                            pendingCardTypeNavigation = PendingCardNav(
                                cardType = event.cardType,
                                navigation = event.navigation,
                                subType = event.subType
                            )
                        )
                    }
                }
            }
            is DashboardUiEvent.OnPaySubscription -> {
                _uiState.update {
                    it.copy(
                        pendingSubscriptionPay = PendingSubscriptionPay(
                            planLabel = event.planLabel,
                            validityText = event.validityText
                        )
                    )
                }
            }
            is DashboardUiEvent.SetClientChatActive -> {
                Constants.isClient_chat = event.isClient
            }
            is DashboardUiEvent.DismissAlert -> {
                _uiState.update {
                    it.copy(
                        alertTitle = null,
                        alertMessage = null,
                        isNoInternet = false
                    )
                }
            }
            is DashboardUiEvent.NavigationHandled -> {
                _uiState.update {
                    it.copy(
                        pendingNavigation = null,
                        pendingCardTypeNavigation = null,
                        pendingSubscriptionPay = null
                    )
                }
            }
        }
    }

    private fun loadDashboard(isRefresh: Boolean) {
        viewModelScope.launch {
            updateRoleAndCategory()
            _uiState.update {
                if (isRefresh) it.copy(isRefreshing = true) else it.copy(isLoading = true)
            }

            when (val result = repository.fetchDashboard()) {
                is DashboardRepository.Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            sections = result.data,
                            currentDateInfo = DashboardUiState.getCurrentDateInfo()
                        )
                    }
                    // Fetch chat counts in background
                    fetchChatCounts()
                }
                is DashboardRepository.Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            alertTitle = if (result.isNoInternet) "No Internet Connection" else "Alert",
                            alertMessage = result.message,
                            isNoInternet = result.isNoInternet
                        )
                    }
                }
            }
        }
    }

    private fun fetchChatCounts() {
        viewModelScope.launch {
            repository.fetchChatList()
            _uiState.update {
                it.copy(
                    clientChatCount = Constants.clientChat_count.toString().toIntOrNull() ?: 0,
                    teamChatCount = Constants.teamChat_count.toString().toIntOrNull() ?: 0
                )
            }
        }
    }

    private fun refreshChatCounts() {
        viewModelScope.launch {
            repository.fetchChatList()
            _uiState.update {
                it.copy(
                    clientChatCount = Constants.clientChat_count.toString().toIntOrNull() ?: 0,
                    teamChatCount = Constants.teamChat_count.toString().toIntOrNull() ?: 0
                )
            }
        }
    }
}
