package com.digicoffer.lauditor.feature.groups.presentation.state

import com.digicoffer.lauditor.Groups.Models.GroupModel
import com.digicoffer.lauditor.Groups.Models.SearchDo
import com.digicoffer.lauditor.Groups.Models.ViewGroupModel
import org.json.JSONObject

data class GroupsUiState(
    val groupsList: List<ViewGroupModel> = emptyList(),
    val membersList: List<GroupModel> = emptyList(),
    val isLoading: Boolean = false,
    val toastMessage: String? = null,
    val counts: JSONObject? = null,
    val auditLogs: List<SearchDo> = emptyList(),
    val isSubscriptionEnded: Boolean = false
)
