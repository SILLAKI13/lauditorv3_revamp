package com.digicoffer.lauditor.feature.matter.presentation.state

import com.digicoffer.lauditor.Matter.Models.AdvocateModel
import com.digicoffer.lauditor.Matter.Models.ClientsModel
import com.digicoffer.lauditor.Matter.Models.TeamModel

data class MatterEditUiState(
    // Step 1 Core Fields
    val title: String = "",
    val caseNumber: String = "",
    val matterNumber: String = "",
    val matterType: String = "", // "Legal" or "General"
    val createdDate: String = "",
    val isAdditionalDetailsExpanded: Boolean = false,

    // Step 1 Additional Details Fields
    val caseTypeList: List<String> = emptyList(),
    val selectedCaseType: String = "",
    val description: String = "",
    val tagInput: String = "",
    val tagsList: List<String> = emptyList(),
    val dateOfFiling: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val courtName: String = "",
    val judges: String = "",
    val priority: String = "High", // "High", "Medium", "Low"
    val status: String = "Active", // "Active", "Pending"

    // Opponent Advocates Section
    val advocatesList: List<AdvocateModel> = emptyList(),
    val isAdvocateFormVisible: Boolean = false,
    val advocateName: String = "",
    val advocateEmail: String = "",
    val advocatePhone: String = "",
    val advocateEmailError: String? = null,
    val advocatePhoneError: String? = null,

    // UI Loading & Validation Status
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val alertTitle: String? = null,
    val alertMessage: String? = null,
    val toastMessage: String? = null,
    val navigateToNext: Boolean = false,
    val navigateToView: Boolean = false,
    val createdMatterId: String? = null,
    val isMatterCreated: Boolean = false,

    // Step 2 Fields (Client(s) & Team Member(s))
    val searchQuery: String = "",
    val searchResults: List<ClientsModel> = emptyList(),
    val isSearchingClient: Boolean = false,
    val selectedClients: List<ClientsModel> = emptyList(),
    val teamMembersList: List<TeamModel> = emptyList(),
    val selectedTeamMembers: List<TeamModel> = emptyList(),
    val isTeamMembersDropdownExpanded: Boolean = false,

    // Step 3 Fields (Document(s))
    val selectedUploadFiles: List<com.digicoffer.lauditor.Documents.Models.DocumentsModel> = emptyList(),
    val selectedExistingDocuments: List<com.digicoffer.lauditor.Documents.Models.DocumentsModel> = emptyList(),
    val isUploadingDocuments: Boolean = false,
    val showSuccessDialog: Boolean = false,
    val successMessage: String = "",
    val showBrowseDialog: Boolean = false,
    val editMetadataFileIndex: Int? = null,
    val previewDocUrl: String? = null,
    val previewDocModel: com.digicoffer.lauditor.Documents.Models.ViewDocumentsModel? = null
)
