package com.digicoffer.lauditor.feature.email.presentation.state

import com.digicoffer.lauditor.Documents.Models.ClientsModel
import com.digicoffer.lauditor.Documents.Models.DocumentsModel
import com.digicoffer.lauditor.Documents.Models.MattersModel
import com.digicoffer.lauditor.Documents.Models.ViewDocumentsModel
import com.digicoffer.lauditor.feature.email.data.model.EmailAttachmentItem
import com.digicoffer.lauditor.feature.email.data.model.EmailLabelInfo
import com.digicoffer.lauditor.feature.email.data.model.EmailMessageItem
import com.digicoffer.lauditor.feature.email.data.model.EmailProvider
import com.digicoffer.lauditor.feature.email.data.model.LauditorAttachedDoc

data class EmailUiState(
    val provider: EmailProvider = EmailProvider.GOOGLE,
    val showProviderSelector: Boolean = false,
    val labelInfo: EmailLabelInfo? = null,
    val messages: List<EmailMessageItem> = emptyList(),
    val filteredMessages: List<EmailMessageItem> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val currentPageIndex: Int = 0,
    val pageStack: List<List<EmailMessageItem>> = emptyList(),
    val tokenStack: List<String?> = emptyList(),
    val nextPageToken: String? = null,
    val authUrlToLaunch: String? = null,
    val statusMessage: String? = null,
    val errorMessage: String? = null,

    // Compose Email
    val showComposeDialog: Boolean = false,
    val composeTo: String = "",
    val composeSubject: String = "",
    val composeBody: String = "",
    val composeAttachments: List<LauditorAttachedDoc> = emptyList(),
    val isSendingEmail: Boolean = false,
    val showNoSubjectAlert: Boolean = false,

    // Attach Lauditor Document inside Compose
    val showAttachDocDialog: Boolean = false,
    val isUSUser: Boolean = false,
    val attachCategoryTab: String = "client", // "client" | "firm"
    val attachIsMatterTab: Boolean = true, // true = Matter, false = Client
    val clientsList: List<ClientsModel> = emptyList(),
    val mattersList: List<MattersModel> = emptyList(),
    val groupsList: List<DocumentsModel> = emptyList(),
    val selectedAttachClientId: String? = null,
    val selectedAttachClientName: String = "",
    val selectedAttachMatterId: String? = null,
    val selectedAttachMatterName: String = "",
    val selectedAttachGroupIds: Set<String> = emptySet(),
    val availableAttachDocs: List<ViewDocumentsModel> = emptyList(),
    val selectedAttachDocs: List<LauditorAttachedDoc> = emptyList(),
    val attachDocSearchQuery: String = "",
    val isLoadingAttachDocs: Boolean = false,

    // Save attachment to Lauditor
    val showSaveAttachmentDialog: Boolean = false,
    val saveAttachmentTarget: EmailAttachmentItem? = null,
    val saveAttachmentMsgId: String? = null,
    val saveCategoryTab: String = "client",
    val saveIsMatterTab: Boolean = true,
    val saveSelectedClientId: String? = null,
    val saveSelectedClientName: String = "",
    val saveSelectedMatterId: String? = null,
    val saveSelectedMatterName: String = "",
    val saveSelectedGroupIds: Set<String> = emptySet(),
    val saveClientGroups: List<DocumentsModel> = emptyList(),
    val isSavingAttachment: Boolean = false
) {
    val canGoPrev: Boolean
        get() = currentPageIndex > 0

    val canGoNext: Boolean
        get() = !nextPageToken.isNullOrEmpty()
}
