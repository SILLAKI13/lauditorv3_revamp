package com.digicoffer.lauditor.feature.documents.presentation.state

import com.digicoffer.lauditor.Documents.Models.ClientsModel
import com.digicoffer.lauditor.Documents.Models.GroupsModel
import com.digicoffer.lauditor.Documents.Models.MattersModel
import com.digicoffer.lauditor.Documents.Models.ViewDocumentsModel

data class DocumentsUiState(
    val currentTab: String = "matter", // "matter", "client", "firm", "delete"
    val isUploadMode: Boolean = false,
    val isGridView: Boolean = false,
    
    // Lists from server
    val documentsList: List<ViewDocumentsModel> = emptyList(),
    val filteredDocumentsList: List<ViewDocumentsModel> = emptyList(),
    val groupsList: List<GroupsModel> = emptyList(),
    val clientsList: List<ClientsModel> = emptyList(),
    val mattersList: List<MattersModel> = emptyList(),
    val clientGroupsList: List<GroupsModel> = emptyList(),

    // Selected items for view filter
    val selectedFilterClient: ClientsModel? = null,
    val selectedFilterMatter: MattersModel? = null,
    val selectedFilterGroup: GroupsModel? = null,
    val selectedFilterDocType: String? = null, // Deleted flow: "firm" or "client"

    // Search
    val searchQuery: String = "",

    // Upload Mode Staged Fields
    val selectedUploadClient: ClientsModel? = null,
    val selectedUploadMatter: MattersModel? = null,
    val selectedUploadGroups: List<GroupsModel> = emptyList(),
    val selectedUploadFiles: List<com.digicoffer.lauditor.Documents.Models.DocumentsModel> = emptyList(),

    // Loading & Popups
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val alertTitle: String? = null,
    val alertMessage: String? = null,
    val toastMessage: String? = null,

    // Dialog controllers
    val editDocModel: ViewDocumentsModel? = null,
    val tagsDocModel: ViewDocumentsModel? = null,
    val confirmDocModel: ViewDocumentsModel? = null,
    val confirmActionType: String? = null, // "disabled", "enabled", "encrypt", "decrypt", "deleted", "download", "restore"
    val previewDocUrl: String? = null,
    val previewDocModel: ViewDocumentsModel? = null,
    val debugInfo: String? = null,
    
    // Pagination and Previews
    val currentPage: Int = 1,
    val itemsPerPage: Int = 10,
    val previewUrls: Map<String, String> = emptyMap(),
    val previewBitmaps: Map<String, android.graphics.Bitmap> = emptyMap(),
    val loadingPreviewIds: Set<String> = emptySet(),
    val failedPreviewIds: Set<String> = emptySet()
)
