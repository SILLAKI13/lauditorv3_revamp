package com.digicoffer.lauditor.feature.groups.presentation.state

import org.json.JSONObject

sealed interface GroupsUiEvent {
    object LoadGroups : GroupsUiEvent
    object LoadMembers : GroupsUiEvent
    object DismissToast : GroupsUiEvent

    data class CreateGroup(
        val name: String,
        val description: String,
        val groupHead: String,
        val members: List<String>,
        val onSuccess: () -> Unit
    ) : GroupsUiEvent

    data class DeleteGroup(
        val id: String,
        val groupHead: String,
        val onSuccess: () -> Unit
    ) : GroupsUiEvent

    data class UpdateGroup(
        val id: String,
        val name: String,
        val description: String,
        val onSuccess: () -> Unit
    ) : GroupsUiEvent

    data class UpdateGroupMembers(
        val id: String,
        val members: List<String>,
        val onSuccess: () -> Unit
    ) : GroupsUiEvent

    data class UpdateGroupHead(
        val id: String,
        val groupHead: String,
        val onSuccess: () -> Unit
    ) : GroupsUiEvent

    data class FetchGroupCounts(
        val id: String,
        val onComplete: (JSONObject?) -> Unit
    ) : GroupsUiEvent

    data class FetchAuditLogs(
        val id: String,
        val fromDate: String,
        val toDate: String,
        val tm: String,
        val search: String
    ) : GroupsUiEvent
}
