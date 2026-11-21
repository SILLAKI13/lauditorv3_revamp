package com.digicoffer.lauditor.feature.email.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.Documents.Models.ClientsModel
import com.digicoffer.lauditor.Documents.Models.MattersModel
import com.digicoffer.lauditor.Documents.Models.ViewDocumentsModel
import com.digicoffer.lauditor.feature.email.data.model.EmailAttachmentItem
import com.digicoffer.lauditor.feature.email.data.model.EmailMessageItem
import com.digicoffer.lauditor.feature.email.data.model.EmailProvider
import com.digicoffer.lauditor.feature.email.data.model.LauditorAttachedDoc
import com.digicoffer.lauditor.feature.email.data.repository.EmailRepository
import com.digicoffer.lauditor.feature.email.presentation.state.EmailUiEvent
import com.digicoffer.lauditor.feature.email.presentation.state.EmailUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EmailViewModel(
    private val repository: EmailRepository
) : ViewModel() {

    private val isIndiaAccount: Boolean
        get() = Constants.isIndiaCountry(Constants.Country) ||
                Constants.CurrencySymbol == "₹" ||
                Constants.CountryCode == "+91"

    private val isUSUser: Boolean
        get() = Constants.isUSUser() || !isIndiaAccount

    private val _uiState = MutableStateFlow(EmailUiState())
    val uiState: StateFlow<EmailUiState> = _uiState.asStateFlow()

    init {
        Constants.isGmail = true
        Constants.check_url()
        _uiState.update { it.copy(provider = EmailProvider.GOOGLE, showProviderSelector = false, isUSUser = isUSUser) }
        loadInitialEmailData()
    }

    private fun loadInitialEmailData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // 1. Trigger GET https://apidev2.digicoffer.com/professional/v2/matter/list
            launch {
                val mattersResult = repository.fetchMatters()
                mattersResult.onSuccess { list ->
                    _uiState.update { it.copy(mattersList = list) }
                }
            }

            // 2. Trigger GET https://dev.utils.mail.digicoffer.com/api/v1/mail/status
            val statusResult = repository.checkMailStatus()

            statusResult.fold(
                onSuccess = { json ->
                    val dataObj = json.optJSONObject("data")
                    val isGmailAuth = json.optBoolean("gmail", false) ||
                            (dataObj?.optBoolean("gmail", false) ?: false)

                    if (isGmailAuth) {
                        // Once user is authenticated in Google, automatically list mails without selecting Google and clicking Continue
                        _uiState.update { it.copy(showProviderSelector = false, provider = EmailProvider.GOOGLE) }
                        Constants.isGmail = true
                        Constants.check_url()
                        loadInboxData()
                    } else {
                        // User not authenticated: show provider selector (with Google only)
                        _uiState.update { it.copy(isLoading = false, showProviderSelector = true, provider = EmailProvider.GOOGLE) }
                    }
                },
                onFailure = {
                    // Fall back to showing provider selector on failure
                    _uiState.update { it.copy(isLoading = false, showProviderSelector = true, provider = EmailProvider.GOOGLE) }
                }
            )
        }
    }

    fun onEvent(event: EmailUiEvent) {
        when (event) {
            is EmailUiEvent.SelectProvider -> {
                _uiState.update { it.copy(provider = event.provider) }
                Constants.isGmail = (event.provider == EmailProvider.GOOGLE)
                Constants.check_url()
            }
            is EmailUiEvent.ConfirmProvider -> confirmProvider()
            is EmailUiEvent.CheckAuthReturn -> checkAuthReturn()
            is EmailUiEvent.ClearAuthUrl -> _uiState.update { it.copy(authUrlToLaunch = null) }

            is EmailUiEvent.SearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
                if (event.query.isEmpty()) {
                    _uiState.update { it.copy(filteredMessages = it.messages) }
                }
            }
            is EmailUiEvent.ExecuteSearch -> executeSearch()
            is EmailUiEvent.ClearSearch -> {
                _uiState.update { it.copy(searchQuery = "", filteredMessages = it.messages) }
            }
            is EmailUiEvent.NextPage -> nextPage()
            is EmailUiEvent.PrevPage -> prevPage()
            is EmailUiEvent.FirstPage -> firstPage()

            is EmailUiEvent.OpenCompose -> {
                _uiState.update {
                    it.copy(
                        showComposeDialog = true,
                        composeTo = "",
                        composeSubject = "",
                        composeBody = "",
                        composeAttachments = emptyList(),
                        showNoSubjectAlert = false
                    )
                }
            }
            is EmailUiEvent.DismissCompose -> {
                _uiState.update { it.copy(showComposeDialog = false, showNoSubjectAlert = false) }
            }
            is EmailUiEvent.ComposeToChanged -> _uiState.update { it.copy(composeTo = event.value) }
            is EmailUiEvent.ComposeSubjectChanged -> _uiState.update { it.copy(composeSubject = event.value) }
            is EmailUiEvent.ComposeBodyChanged -> _uiState.update { it.copy(composeBody = event.value) }
            is EmailUiEvent.RemoveComposeAttachment -> {
                _uiState.update { state ->
                    state.copy(composeAttachments = state.composeAttachments.filterNot { it.id == event.docId })
                }
            }
            is EmailUiEvent.SendEmailClicked -> sendEmailClicked()
            is EmailUiEvent.ConfirmSendNoSubject -> {
                _uiState.update { it.copy(showNoSubjectAlert = false) }
                sendEmail()
            }
            is EmailUiEvent.DismissNoSubjectAlert -> _uiState.update { it.copy(showNoSubjectAlert = false) }

            is EmailUiEvent.OpenAttachDocPicker -> openAttachDocPicker()
            is EmailUiEvent.DismissAttachDocPicker -> _uiState.update { it.copy(showAttachDocDialog = false) }
            is EmailUiEvent.AttachCategoryTabChanged -> attachCategoryTabChanged(event.category)
            is EmailUiEvent.AttachSubTabChanged -> attachSubTabChanged(event.isMatter)
            is EmailUiEvent.SelectAttachClient -> selectAttachClient(event.client)
            is EmailUiEvent.SelectAttachMatter -> selectAttachMatter(event.matter)
            is EmailUiEvent.ToggleAttachGroup -> toggleAttachGroup(event.groupId)
            is EmailUiEvent.ToggleDocSelection -> toggleDocSelection(event.doc, event.isSelected)
            is EmailUiEvent.AttachDocSearchChanged -> _uiState.update { it.copy(attachDocSearchQuery = event.query) }
            is EmailUiEvent.ConfirmAttachDocs -> confirmAttachDocs()

            is EmailUiEvent.OpenSaveAttachment -> openSaveAttachment(event.message, event.attachment)
            is EmailUiEvent.DismissSaveAttachment -> _uiState.update { it.copy(showSaveAttachmentDialog = false) }
            is EmailUiEvent.SaveCategoryTabChanged -> saveCategoryTabChanged(event.category)
            is EmailUiEvent.SaveSubTabChanged -> saveSubTabChanged(event.isMatter)
            is EmailUiEvent.SelectSaveClient -> selectSaveClient(event.client)
            is EmailUiEvent.SelectSaveMatter -> selectSaveMatter(event.matter)
            is EmailUiEvent.ToggleSaveGroup -> toggleSaveGroup(event.groupId)
            is EmailUiEvent.ConfirmSaveAttachment -> confirmSaveAttachment()

            is EmailUiEvent.DismissFeedbackMessage -> {
                _uiState.update { it.copy(statusMessage = null, errorMessage = null) }
            }
        }
    }

    private fun confirmProvider() {
        Constants.isGmail = (_uiState.value.provider == EmailProvider.GOOGLE)
        Constants.check_url()
        _uiState.update { it.copy(showProviderSelector = false) }
        loadInboxData()
    }

    private fun checkAuthReturn() {
        _uiState.update { it.copy(showProviderSelector = false) }
        loadInboxData()
    }

    private fun loadInboxData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val isGmail = (_uiState.value.provider == EmailProvider.GOOGLE)
            val labelResult = repository.fetchLabel(isGmail)

            if (labelResult.isSuccess) {
                val labelInfo = labelResult.getOrNull()
                _uiState.update { it.copy(labelInfo = labelInfo) }
                loadInitialMessages(labelInfo?.id ?: "INBOX")
            } else {
                // Check if need auth
                val authResult = repository.fetchAuthUrl(isGmail)
                _uiState.update { it.copy(isLoading = false) }
                if (authResult.isSuccess) {
                    val authUrl = authResult.getOrNull()
                    _uiState.update { it.copy(authUrlToLaunch = authUrl) }
                } else {
                    _uiState.update {
                        it.copy(errorMessage = labelResult.exceptionOrNull()?.message ?: "Failed to connect to email provider")
                    }
                }
            }
        }
    }

    private fun loadInitialMessages(labelId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val isGmail = (_uiState.value.provider == EmailProvider.GOOGLE)
            val result = repository.fetchMessages(isGmail, labelId, null)
            _uiState.update { it.copy(isLoading = false) }

            if (result.isSuccess) {
                val (msgList, nextToken) = result.getOrThrow()
                _uiState.update {
                    it.copy(
                        messages = msgList,
                        filteredMessages = msgList,
                        currentPageIndex = 0,
                        pageStack = listOf(msgList),
                        tokenStack = listOf(null),
                        nextPageToken = nextToken
                    )
                }
            } else {
                _uiState.update {
                    it.copy(errorMessage = result.exceptionOrNull()?.message ?: "Failed to load messages")
                }
            }
        }
    }

    private fun nextPage() {
        val state = _uiState.value
        val nextIdx = state.currentPageIndex + 1
        if (nextIdx < state.pageStack.size) {
            val page = state.pageStack[nextIdx]
            _uiState.update {
                it.copy(
                    currentPageIndex = nextIdx,
                    messages = page,
                    filteredMessages = page
                )
            }
            return
        }

        val token = state.nextPageToken
        if (token.isNullOrEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val isGmail = (state.provider == EmailProvider.GOOGLE)
            val labelId = state.labelInfo?.id ?: "INBOX"
            val result = repository.fetchMessages(isGmail, labelId, token)
            _uiState.update { it.copy(isLoading = false) }

            if (result.isSuccess) {
                val (msgList, newNextToken) = result.getOrThrow()
                val updatedStack = state.pageStack + listOf(msgList)
                val updatedTokenStack = state.tokenStack + listOf(token)
                _uiState.update {
                    it.copy(
                        messages = msgList,
                        filteredMessages = msgList,
                        currentPageIndex = nextIdx,
                        pageStack = updatedStack,
                        tokenStack = updatedTokenStack,
                        nextPageToken = newNextToken
                    )
                }
            } else {
                _uiState.update {
                    it.copy(errorMessage = result.exceptionOrNull()?.message ?: "No more pages available")
                }
            }
        }
    }

    private fun prevPage() {
        val state = _uiState.value
        if (state.currentPageIndex > 0) {
            val prevIdx = state.currentPageIndex - 1
            val page = state.pageStack[prevIdx]
            _uiState.update {
                it.copy(
                    currentPageIndex = prevIdx,
                    messages = page,
                    filteredMessages = page
                )
            }
        }
    }

    private fun firstPage() {
        val state = _uiState.value
        if (state.pageStack.isNotEmpty()) {
            val page = state.pageStack[0]
            _uiState.update {
                it.copy(
                    currentPageIndex = 0,
                    messages = page,
                    filteredMessages = page
                )
            }
        }
    }

    private fun executeSearch() {
        val query = _uiState.value.searchQuery.trim().lowercase()
        if (query.isEmpty()) {
            _uiState.update { it.copy(filteredMessages = it.messages) }
        } else {
            val filtered = _uiState.value.messages.filter { msg ->
                msg.from.lowercase().contains(query) ||
                        msg.subject.lowercase().contains(query)
            }
            _uiState.update { it.copy(filteredMessages = filtered) }
        }
    }

    private fun sendEmailClicked() {
        val toStr = _uiState.value.composeTo.trim()
        val emails = toStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (emails.isEmpty() || !emails.all { AndroidUtils.isValidEmail(it) }) {
            _uiState.update { it.copy(errorMessage = "Please enter valid recipient email address(es)") }
            return
        }

        if (_uiState.value.composeSubject.trim().isEmpty() && _uiState.value.composeBody.trim().isEmpty()) {
            _uiState.update { it.copy(showNoSubjectAlert = true) }
        } else {
            sendEmail()
        }
    }

    private fun sendEmail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSendingEmail = true, errorMessage = null) }
            val state = _uiState.value
            val isGmail = (state.provider == EmailProvider.GOOGLE)
            val emails = state.composeTo.split(",").map { it.trim() }.filter { it.isNotEmpty() }

            val result = repository.sendEmail(
                isGmail = isGmail,
                toEmails = emails,
                subject = state.composeSubject.trim(),
                body = state.composeBody.trim(),
                documents = state.composeAttachments
            )

            _uiState.update { it.copy(isSendingEmail = false) }
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        showComposeDialog = false,
                        statusMessage = result.getOrNull() ?: "Email sent successfully"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(errorMessage = result.exceptionOrNull()?.message ?: "Failed to send email")
                }
            }
        }
    }

    // Attach Document from Lauditor Picker
    private fun openAttachDocPicker() {
        val isUS = isUSUser
        _uiState.update {
            it.copy(
                showAttachDocDialog = true,
                isUSUser = isUS,
                attachCategoryTab = "client",
                attachIsMatterTab = true,
                selectedAttachClientId = null,
                selectedAttachClientName = "",
                selectedAttachMatterId = null,
                selectedAttachMatterName = "",
                selectedAttachGroupIds = emptySet(),
                availableAttachDocs = emptyList(),
                selectedAttachDocs = it.composeAttachments,
                attachDocSearchQuery = ""
            )
        }
        if (isUS) {
            loadAllDocumentsForAttach()
        } else {
            loadMetadataForAttach()
        }
    }

    private fun loadAllDocumentsForAttach() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingAttachDocs = true) }
            val result = repository.fetchAllDocuments()
            _uiState.update {
                it.copy(
                    availableAttachDocs = result.getOrDefault(emptyList()),
                    isLoadingAttachDocs = false
                )
            }
        }
    }

    private fun loadMetadataForAttach() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingAttachDocs = true) }
            val clientsResult = repository.fetchClients()
            val mattersResult = repository.fetchMatters()
            val groupsResult = repository.fetchGroups()
            _uiState.update {
                it.copy(
                    clientsList = clientsResult.getOrDefault(emptyList()),
                    mattersList = mattersResult.getOrDefault(emptyList()),
                    groupsList = groupsResult.getOrDefault(emptyList()),
                    isLoadingAttachDocs = false
                )
            }
            if (_uiState.value.attachCategoryTab == "client" && _uiState.value.attachIsMatterTab) {
                // If matters exist, can query initial docs or wait for matter selection
            }
        }
    }

    private fun attachCategoryTabChanged(category: String) {
        _uiState.update {
            it.copy(
                attachCategoryTab = category,
                availableAttachDocs = emptyList()
            )
        }
        if (category == "firm") {
            queryAttachDocuments()
        }
    }

    private fun attachSubTabChanged(isMatter: Boolean) {
        _uiState.update {
            it.copy(
                attachIsMatterTab = isMatter,
                availableAttachDocs = emptyList()
            )
        }
    }

    private fun selectAttachClient(client: ClientsModel) {
        _uiState.update {
            it.copy(
                selectedAttachClientId = client.id,
                selectedAttachClientName = client.name ?: ""
            )
        }
        queryAttachDocuments()
    }

    private fun selectAttachMatter(matter: MattersModel) {
        _uiState.update {
            it.copy(
                selectedAttachMatterId = matter.id,
                selectedAttachMatterName = matter.title ?: ""
            )
        }
        queryAttachDocuments()
    }

    private fun toggleAttachGroup(groupId: String) {
        val current = _uiState.value.selectedAttachGroupIds.toMutableSet()
        if (current.contains(groupId)) {
            current.remove(groupId)
        } else {
            current.add(groupId)
        }
        _uiState.update { it.copy(selectedAttachGroupIds = current) }
        queryAttachDocuments()
    }

    private fun queryAttachDocuments() {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update { it.copy(isLoadingAttachDocs = true) }

            val result = if (state.attachCategoryTab == "client") {
                val clientId = if (!state.attachIsMatterTab) state.selectedAttachClientId else null
                val matterId = if (state.attachIsMatterTab) state.selectedAttachMatterId else null
                repository.filterDocuments("client", clientId, matterId, null)
            } else {
                repository.filterDocuments("firm", null, null, state.selectedAttachGroupIds.toList())
            }

            _uiState.update {
                it.copy(
                    availableAttachDocs = result.getOrDefault(emptyList()),
                    isLoadingAttachDocs = false
                )
            }
        }
    }

    private fun toggleDocSelection(doc: ViewDocumentsModel, isSelected: Boolean) {
        val docId = doc.id ?: return
        val current = _uiState.value.selectedAttachDocs.toMutableList()
        if (isSelected) {
            viewModelScope.launch {
                val viewUrlResult = repository.fetchDocumentViewUrl(docId)
                val url = viewUrlResult.getOrDefault("")
                current.removeAll { it.id == docId }
                current.add(
                    LauditorAttachedDoc(
                        id = docId,
                        name = doc.name ?: doc.filename ?: "Document",
                        filename = doc.filename ?: doc.name ?: "document.pdf",
                        pathUrl = url
                    )
                )
                _uiState.update { it.copy(selectedAttachDocs = current) }
            }
        } else {
            current.removeAll { it.id == docId }
            _uiState.update { it.copy(selectedAttachDocs = current) }
        }
    }

    private fun confirmAttachDocs() {
        _uiState.update {
            it.copy(
                composeAttachments = it.selectedAttachDocs,
                showAttachDocDialog = false
            )
        }
    }

    // Save Attachment into Lauditor Documents
    private fun openSaveAttachment(message: EmailMessageItem, attachment: EmailAttachmentItem) {
        _uiState.update {
            it.copy(
                showSaveAttachmentDialog = true,
                saveAttachmentTarget = attachment,
                saveAttachmentMsgId = message.msgId,
                saveCategoryTab = "client",
                saveIsMatterTab = true,
                saveSelectedClientId = null,
                saveSelectedClientName = "",
                saveSelectedMatterId = null,
                saveSelectedMatterName = "",
                saveSelectedGroupIds = emptySet(),
                saveClientGroups = emptyList()
            )
        }
        loadMetadataForSave()
    }

    private fun loadMetadataForSave() {
        viewModelScope.launch {
            val clientsResult = repository.fetchClients()
            val mattersResult = repository.fetchMatters()
            val groupsResult = repository.fetchGroups()
            _uiState.update {
                it.copy(
                    clientsList = clientsResult.getOrDefault(emptyList()),
                    mattersList = mattersResult.getOrDefault(emptyList()),
                    groupsList = groupsResult.getOrDefault(emptyList())
                )
            }
        }
    }

    private fun saveCategoryTabChanged(category: String) {
        _uiState.update { it.copy(saveCategoryTab = category) }
    }

    private fun saveSubTabChanged(isMatter: Boolean) {
        _uiState.update { it.copy(saveIsMatterTab = isMatter) }
    }

    private fun selectSaveClient(client: ClientsModel) {
        _uiState.update {
            it.copy(
                saveSelectedClientId = client.id,
                saveSelectedClientName = client.name ?: ""
            )
        }
        loadClientGroupsForSave()
    }

    private fun selectSaveMatter(matter: MattersModel) {
        _uiState.update {
            it.copy(
                saveSelectedMatterId = matter.id,
                saveSelectedMatterName = matter.title ?: ""
            )
        }
    }

    private fun toggleSaveGroup(groupId: String) {
        val current = _uiState.value.saveSelectedGroupIds.toMutableSet()
        if (current.contains(groupId)) {
            current.remove(groupId)
        } else {
            current.add(groupId)
        }
        _uiState.update { it.copy(saveSelectedGroupIds = current) }
    }

    private fun loadClientGroupsForSave() {
        val state = _uiState.value
        val clientId = state.saveSelectedClientId ?: return
        val client = state.clientsList.find { it.id == clientId }
        val type = client?.type ?: "individual"
        viewModelScope.launch {
            val res = repository.fetchClientGroups(clientId, type, state.saveSelectedMatterId)
            val groups = res.getOrDefault(emptyList())
            _uiState.update {
                it.copy(
                    saveClientGroups = groups,
                    saveSelectedGroupIds = groups.mapNotNull { g -> g.group_id }.toSet()
                )
            }
        }
    }

    private fun confirmSaveAttachment() {
        val state = _uiState.value
        val msgId = state.saveAttachmentMsgId ?: return
        val partId = state.saveAttachmentTarget?.partId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingAttachment = true, errorMessage = null) }

            val clientIds = if (state.saveCategoryTab == "client" && !state.saveSelectedClientId.isNullOrEmpty()) {
                listOf(state.saveSelectedClientId)
            } else emptyList()

            val matterId = if (state.saveCategoryTab == "client") state.saveSelectedMatterId else null
            val groupIds = state.saveSelectedGroupIds.toList()

            val result = repository.uploadAttachmentToLauditor(
                category = state.saveCategoryTab,
                clientIds = clientIds,
                matterId = matterId,
                groupIds = groupIds,
                msgId = msgId,
                partId = partId
            )

            _uiState.update { it.copy(isSavingAttachment = false) }
            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        showSaveAttachmentDialog = false,
                        statusMessage = result.getOrNull() ?: "Attachment saved successfully"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(errorMessage = result.exceptionOrNull()?.message ?: "Failed to save attachment")
                }
            }
        }
    }
}
