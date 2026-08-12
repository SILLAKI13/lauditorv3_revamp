package com.digicoffer.lauditor.feature.members.presentation.state

import com.digicoffer.lauditor.Groups.Models.ViewGroupModel
import com.digicoffer.lauditor.Members.MembersModel

data class MembersUiState(
    val isLoading: Boolean = false,
    val toastMessage: String? = null,
    
    // License slot state
    val licenseTotal: String = "0",
    val licenseCount: String = "0",
    val isSubscriptionEnded: Boolean = false,

    // Data lists
    val membersList: List<MembersModel> = emptyList(),
    val groupsList: List<ViewGroupModel> = emptyList()
)
