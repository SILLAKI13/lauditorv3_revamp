package com.digicoffer.lauditor.feature.documents.presentation.state

import com.digicoffer.lauditor.Documents.Models.ClientsModel
import com.digicoffer.lauditor.Documents.Models.GroupsModel
import com.digicoffer.lauditor.Documents.Models.MattersModel
import com.digicoffer.lauditor.Documents.Models.ViewDocumentsModel
import java.io.File

sealed interface DocumentsUiEvent {
    data class SwitchTab(val tabName: String) : DocumentsUiEvent
    data class ToggleUploadMode(val upload: Boolean) : DocumentsUiEvent
    data class ToggleGridView(val grid: Boolean) : DocumentsUiEvent
    data class SetSearchQuery(val query: String) : DocumentsUiEvent

    // Filter selectors
    data class SelectFilterClient(val client: ClientsModel?) : DocumentsUiEvent
    data class SelectFilterMatter(val matter: MattersModel?) : DocumentsUiEvent
    data class SelectFilterGroup(val group: GroupsModel?) : DocumentsUiEvent
    data class SelectFilterDocType(val type: String?) : DocumentsUiEvent

    // Upload selectors
    data class SelectUploadClient(val client: ClientsModel?) : DocumentsUiEvent
    data class SelectUploadMatter(val matter: MattersModel?) : DocumentsUiEvent
    data class SelectUploadGroups(val groups: List<GroupsModel>) : DocumentsUiEvent

    // Staged files management
    data class AddStagedFile(val file: File, val name: String) : DocumentsUiEvent
    data class UpdateStagedFile(
        val index: Int,
        val name: String,
        val desc: String,
        val expDate: String,
        val isDownloadDisabled: Boolean,
        val isEncrypted: Boolean,
        val tags: org.json.JSONObject?
    ) : DocumentsUiEvent
    data class RemoveStagedFile(val index: Int) : DocumentsUiEvent
    object UploadDocuments : DocumentsUiEvent

    // Core actions
    data class TriggerAction(val actionType: String, val doc: ViewDocumentsModel) : DocumentsUiEvent
    object ExecuteConfirmAction : DocumentsUiEvent
    object CloseConfirmAction : DocumentsUiEvent

    data class OpenEditMetadata(val doc: ViewDocumentsModel) : DocumentsUiEvent
    object CloseEditMetadata : DocumentsUiEvent
    data class SaveMetadata(val docId: String, val name: String, val desc: String, val expDate: String, val tags: org.json.JSONObject?) : DocumentsUiEvent

    data class OpenUpdateTags(val doc: ViewDocumentsModel) : DocumentsUiEvent
    object CloseUpdateTags : DocumentsUiEvent
    data class SaveTags(val docId: String, val name: String, val tags: Map<String, String>) : DocumentsUiEvent

    object ClosePreview : DocumentsUiEvent
    object DismissAlert : DocumentsUiEvent
    object DismissToast : DocumentsUiEvent
    data class SelectPage(val page: Int) : DocumentsUiEvent
    data class LoadPreview(val doc: ViewDocumentsModel) : DocumentsUiEvent
}
