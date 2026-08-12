package com.digicoffer.lauditor.feature.members.presentation.state

sealed interface MembersUiEvent {
    object LoadMembers : MembersUiEvent
    object LoadGroups : MembersUiEvent
    object DismissToast : MembersUiEvent

    data class CreateMember(
        val name: String,
        val designation: String,
        val defaultRate: String,
        val currency: String,
        val email: String,
        val emailConfirm: String,
        val groups: List<String>,
        val onSuccess: () -> Unit
    ) : MembersUiEvent

    data class UpdateMember(
        val id: String,
        val name: String,
        val designation: String,
        val defaultRate: String,
        val currency: String,
        val email: String,
        val emailConfirm: String,
        val onSuccess: () -> Unit
    ) : MembersUiEvent

    data class UpdateGroupAccess(
        val id: String,
        val groups: List<String>,
        val onSuccess: () -> Unit
    ) : MembersUiEvent

    data class ResetPassword(
        val memberId: String,
        val onSuccess: () -> Unit
    ) : MembersUiEvent

    data class DeleteMember(
        val id: String,
        val onSuccess: () -> Unit
    ) : MembersUiEvent

    data class UpgradePracticePartner(
        val id: String,
        val onSuccess: () -> Unit
    ) : MembersUiEvent
}
