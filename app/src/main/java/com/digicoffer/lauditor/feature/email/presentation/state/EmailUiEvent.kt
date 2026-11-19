package com.digicoffer.lauditor.feature.email.presentation.state

import com.digicoffer.lauditor.Documents.Models.ClientsModel
import com.digicoffer.lauditor.Documents.Models.MattersModel
import com.digicoffer.lauditor.Documents.Models.ViewDocumentsModel
import com.digicoffer.lauditor.feature.email.data.model.EmailAttachmentItem
import com.digicoffer.lauditor.feature.email.data.model.EmailMessageItem
import com.digicoffer.lauditor.feature.email.data.model.EmailProvider

sealed interface EmailUiEvent {
    // Provider & Init
    data class SelectProvider(val provider: EmailProvider) : EmailUiEvent
    object ConfirmProvider : EmailUiEvent
    object CheckAuthReturn : EmailUiEvent
    object ClearAuthUrl : EmailUiEvent

    // Search & Pagination
    data class SearchQueryChanged(val query: String) : EmailUiEvent
    object ExecuteSearch : EmailUiEvent
    object ClearSearch : EmailUiEvent
    object NextPage : EmailUiEvent
    object PrevPage : EmailUiEvent
    object FirstPage : EmailUiEvent

    // Compose
    object OpenCompose : EmailUiEvent
    object DismissCompose : EmailUiEvent
    data class ComposeToChanged(val value: String) : EmailUiEvent
    data class ComposeSubjectChanged(val value: String) : EmailUiEvent
    data class ComposeBodyChanged(val value: String) : EmailUiEvent
    data class RemoveComposeAttachment(val docId: String) : EmailUiEvent
    object SendEmailClicked : EmailUiEvent
    object ConfirmSendNoSubject : EmailUiEvent
    object DismissNoSubjectAlert : EmailUiEvent

    // Attach Document from Lauditor (Inside Compose)
    object OpenAttachDocPicker : EmailUiEvent
    object DismissAttachDocPicker : EmailUiEvent
    data class AttachCategoryTabChanged(val category: String) : EmailUiEvent
    data class AttachSubTabChanged(val isMatter: Boolean) : EmailUiEvent
    data class SelectAttachClient(val client: ClientsModel) : EmailUiEvent
    data class SelectAttachMatter(val matter: MattersModel) : EmailUiEvent
    data class ToggleAttachGroup(val groupId: String) : EmailUiEvent
    data class ToggleDocSelection(val doc: ViewDocumentsModel, val isSelected: Boolean) : EmailUiEvent
    data class AttachDocSearchChanged(val query: String) : EmailUiEvent
    object ConfirmAttachDocs : EmailUiEvent

    // Save attachment from email to Lauditor documents
    data class OpenSaveAttachment(val message: EmailMessageItem, val attachment: EmailAttachmentItem) : EmailUiEvent
    object DismissSaveAttachment : EmailUiEvent
    data class SaveCategoryTabChanged(val category: String) : EmailUiEvent
    data class SaveSubTabChanged(val isMatter: Boolean) : EmailUiEvent
    data class SelectSaveClient(val client: ClientsModel) : EmailUiEvent
    data class SelectSaveMatter(val matter: MattersModel) : EmailUiEvent
    data class ToggleSaveGroup(val groupId: String) : EmailUiEvent
    object ConfirmSaveAttachment : EmailUiEvent

    // General feedback
    object DismissFeedbackMessage : EmailUiEvent
}
