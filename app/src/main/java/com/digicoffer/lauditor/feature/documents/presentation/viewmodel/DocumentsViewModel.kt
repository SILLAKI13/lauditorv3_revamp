package com.digicoffer.lauditor.feature.documents.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.Documents.Models.ClientsModel
import com.digicoffer.lauditor.Documents.Models.GroupsModel
import com.digicoffer.lauditor.Documents.Models.MattersModel
import com.digicoffer.lauditor.Documents.Models.ViewDocumentsModel
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.feature.documents.data.repository.DocumentsRepository
import com.digicoffer.lauditor.feature.documents.presentation.state.DocumentsUiEvent
import com.digicoffer.lauditor.feature.documents.presentation.state.DocumentsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import org.json.JSONArray
import org.json.JSONObject
import android.graphics.Bitmap
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import okhttp3.MediaType.Companion.toMediaTypeOrNull

class DocumentsViewModel(
    application: Application,
    private val repository: DocumentsRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(DocumentsUiState())
    val uiState: StateFlow<DocumentsUiState> = _uiState.asStateFlow()

    private var loadMattersJob: Job? = null
    private var loadClientsJob: Job? = null
    private var loadGroupsJob: Job? = null
    private var fetchDeletedDocumentsJob: Job? = null
    private var filterDocumentsJob: Job? = null

    init {
        // Initial setup matching bundle parameters
        val initialTab = "matter"
        switchTab(initialTab)
    }

    fun onEvent(event: DocumentsUiEvent) {
        when (event) {
            is DocumentsUiEvent.SwitchTab -> switchTab(event.tabName)
            is DocumentsUiEvent.ToggleUploadMode -> {
                _uiState.update {
                    it.copy(
                        isUploadMode = event.upload,
                        selectedUploadClient = null,
                        selectedUploadMatter = null,
                        selectedUploadGroups = emptyList(),
                        selectedUploadFiles = emptyList(),
                        clientGroupsList = emptyList()
                    )
                }
                if (event.upload) {
                    viewModelScope.launch {
                        when (_uiState.value.currentTab) {
                            "matter" -> loadMatters()
                            "client" -> loadClients()
                            "firm" -> loadGroups()
                        }
                    }
                }
            }
            is DocumentsUiEvent.ToggleGridView -> _uiState.update { it.copy(isGridView = event.grid) }
            is DocumentsUiEvent.SetSearchQuery -> {
                _uiState.update { it.copy(searchQuery = event.query) }
                applyLocalFilters()
            }
            is DocumentsUiEvent.SelectFilterClient -> {
                _uiState.update { it.copy(selectedFilterClient = event.client, selectedFilterMatter = null) }
                fetchFilteredList()
            }
            is DocumentsUiEvent.SelectFilterMatter -> {
                _uiState.update { it.copy(selectedFilterMatter = event.matter) }
                fetchFilteredList()
            }
            is DocumentsUiEvent.SelectFilterGroup -> {
                _uiState.update { it.copy(selectedFilterGroup = event.group) }
                fetchFilteredList()
            }
            is DocumentsUiEvent.SelectFilterDocType -> {
                _uiState.update { it.copy(selectedFilterDocType = event.type) }
                applyLocalFilters()
            }
            is DocumentsUiEvent.SelectUploadClient -> {
                _uiState.update { it.copy(selectedUploadClient = event.client, selectedUploadMatter = null, selectedUploadGroups = emptyList(), clientGroupsList = emptyList()) }
                loadClientGroups()
            }
            is DocumentsUiEvent.SelectUploadMatter -> {
                _uiState.update { it.copy(selectedUploadMatter = event.matter, selectedUploadGroups = emptyList(), clientGroupsList = emptyList()) }
                loadClientGroups()
            }
            is DocumentsUiEvent.SelectUploadGroups -> {
                _uiState.update { it.copy(selectedUploadGroups = event.groups) }
            }
            is DocumentsUiEvent.AddStagedFile -> addStagedFile(event.file, event.name)
            is DocumentsUiEvent.UpdateStagedFile -> updateStagedFile(
                event.index, event.name, event.desc, event.expDate,
                event.isDownloadDisabled, event.isEncrypted, event.tags
            )
            is DocumentsUiEvent.RemoveStagedFile -> removeStagedFile(event.index)
            is DocumentsUiEvent.UploadDocuments -> uploadDocuments()
            is DocumentsUiEvent.TriggerAction -> triggerDocAction(event.actionType, event.doc)
            is DocumentsUiEvent.ExecuteConfirmAction -> executeConfirmAction()
            is DocumentsUiEvent.CloseConfirmAction -> _uiState.update { it.copy(confirmDocModel = null, confirmActionType = null) }
            is DocumentsUiEvent.OpenEditMetadata -> _uiState.update { it.copy(editDocModel = event.doc) }
            is DocumentsUiEvent.CloseEditMetadata -> _uiState.update { it.copy(editDocModel = null) }
            is DocumentsUiEvent.SaveMetadata -> saveMetadata(event.docId, event.name, event.desc, event.expDate, event.tags)
            is DocumentsUiEvent.OpenUpdateTags -> _uiState.update { it.copy(tagsDocModel = event.doc) }
            is DocumentsUiEvent.CloseUpdateTags -> _uiState.update { it.copy(tagsDocModel = null) }
            is DocumentsUiEvent.SaveTags -> saveTags(event.docId, event.name, event.tags)
            is DocumentsUiEvent.ClosePreview -> _uiState.update { it.copy(previewDocUrl = null, previewDocModel = null) }
            is DocumentsUiEvent.DismissAlert -> _uiState.update { it.copy(alertTitle = null, alertMessage = null) }
            is DocumentsUiEvent.DismissToast -> _uiState.update { it.copy(toastMessage = null) }
            is DocumentsUiEvent.SelectPage -> _uiState.update { it.copy(currentPage = event.page) }
            is DocumentsUiEvent.LoadPreview -> loadGridPreview(event.doc)
        }
    }

    private fun switchTab(tabName: String) {
        loadMattersJob?.cancel()
        loadClientsJob?.cancel()
        loadGroupsJob?.cancel()
        fetchDeletedDocumentsJob?.cancel()
        filterDocumentsJob?.cancel()

        _uiState.update {
            it.copy(
                currentTab = tabName,
                isUploadMode = false,
                searchQuery = "",
                selectedFilterClient = null,
                selectedFilterMatter = null,
                selectedFilterGroup = null,
                selectedFilterDocType = null,
                selectedUploadClient = null,
                selectedUploadMatter = null,
                selectedUploadGroups = emptyList(),
                selectedUploadFiles = emptyList(),
                documentsList = emptyList(),
                filteredDocumentsList = emptyList(),
                currentPage = 1
            )
        }
        when (tabName) {
            "matter" -> {
                loadMatters()
            }
            "client" -> {
                loadClients()
            }
            "firm" -> {
                loadGroups()
            }
            "delete" -> {
                fetchDeletedDocuments()
            }
        }
    }

    private fun loadMatters() {
        loadMattersJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val res = repository.fetchMatters()
            _uiState.update { it.copy(isLoading = false) }
            if (res.result == WebServiceHelper.ServiceCallStatus.Success) {
                val json = JSONObject(res.responseContent ?: "{}")
                val mattersArr = json.optJSONArray("matters") ?: JSONArray()
                val list = mutableListOf<MattersModel>()
                for (i in 0 until mattersArr.length()) {
                    val obj = mattersArr.getJSONObject(i)
                    list.add(MattersModel().apply {
                        id = obj.optString("id")
                        title = obj.optString("title")
                        type = obj.optString("type")
                        name = obj.optString("title")
                    })
                }
                
                if (list.isNotEmpty()) {
                    val allIds = list.joinToString(",") { it.id ?: "" }
                    val allMattersModel = MattersModel().apply {
                        id = allIds
                        title = "All Matters"
                        name = "All Matters"
                        type = list[0].type
                    }
                    list.add(0, allMattersModel)
                    _uiState.update { 
                        it.copy(
                            mattersList = list,
                            selectedFilterMatter = allMattersModel
                        )
                    }
                    fetchFilteredList()
                } else {
                    _uiState.update { it.copy(mattersList = list, selectedFilterMatter = null) }
                }
            }
        }
    }

    private fun loadClients() {
        loadClientsJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val res1 = repository.fetchClients()
            val res2 = repository.fetchCorpClients()
            _uiState.update { it.copy(isLoading = false) }
            val list = mutableListOf<ClientsModel>()
            if (res1.result == WebServiceHelper.ServiceCallStatus.Success) {
                val json = JSONObject(res1.responseContent ?: "{}")
                val data = json.optJSONObject("data")
                val clientsArr = data?.optJSONArray("relationships") ?: JSONArray()
                for (i in 0 until clientsArr.length()) {
                    val obj = clientsArr.getJSONObject(i)
                    list.add(ClientsModel().apply {
                        id = obj.optString("id")
                        name = obj.optString("name")
                        type = obj.optString("type")
                    })
                }
            }
            if (res2.result == WebServiceHelper.ServiceCallStatus.Success) {
                val json = JSONObject(res2.responseContent ?: "{}")
                val corpArr = json.optJSONArray("relationships") ?: JSONArray()
                for (i in 0 until corpArr.length()) {
                    val obj = corpArr.getJSONObject(i)
                    list.add(ClientsModel().apply {
                        id = obj.optString("id")
                        name = obj.optString("name")
                        type = if (obj.optString("type") == "consumer") "consumer" else "corporate"
                    })
                }
            }
            
            if (list.isNotEmpty()) {
                val allIds = list.joinToString(",") { it.id ?: "" }
                val allClientsModel = ClientsModel().apply {
                    id = allIds
                    name = "All Clients"
                    type = list[0].type
                }
                list.add(0, allClientsModel)
                _uiState.update { 
                    it.copy(
                        clientsList = list,
                        selectedFilterClient = allClientsModel
                    )
                }
                fetchFilteredList()
            } else {
                _uiState.update { it.copy(clientsList = list, selectedFilterClient = null) }
            }
        }
    }

    private fun loadGroups() {
        loadGroupsJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val res = repository.fetchGroups()
            _uiState.update { it.copy(isLoading = false) }
            if (res.result == WebServiceHelper.ServiceCallStatus.Success) {
                val json = JSONObject(res.responseContent ?: "{}")
                val arr = json.optJSONArray("data") ?: JSONArray()
                val list = mutableListOf<GroupsModel>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val name = obj.optString("name")
                    if (name.equals("AAM", ignoreCase = true) || name.equals("SuperUser", ignoreCase = true)) {
                        continue
                    }
                    list.add(GroupsModel().apply {
                        id = obj.optString("id")
                        this.name = name
                    })
                }
                
                if (list.isNotEmpty()) {
                    val allIds = list.joinToString(",") { it.id ?: "" }
                    val allGroupsModel = GroupsModel().apply {
                        id = allIds
                        name = "All Groups"
                    }
                    list.add(0, allGroupsModel)
                    _uiState.update { 
                        it.copy(
                            groupsList = list,
                            selectedFilterGroup = allGroupsModel
                        )
                    }
                    fetchFilteredList()
                } else {
                    _uiState.update { it.copy(groupsList = list, selectedFilterGroup = null) }
                }
            }
        }
    }

    private fun loadClientGroups() {
        val tab = _uiState.value.currentTab
        val client = _uiState.value.selectedUploadClient
        val matterId = _uiState.value.selectedUploadMatter?.id ?: ""
        
        if (tab == "matter" && matterId.isEmpty()) return
        if (tab == "client" && client == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val clientsJson = if (client != null) {
                JSONArray().apply {
                    put(JSONObject().apply {
                        put("id", client.id)
                        put("type", client.type)
                    })
                }.toString()
            } else {
                "[]"
            }
            val res = repository.fetchClientGroups(clientsJson, matterId)
            _uiState.update { it.copy(isLoading = false) }
            if (res.result == WebServiceHelper.ServiceCallStatus.Success) {
                val json = JSONObject(res.responseContent ?: "{}")
                val arr = json.optJSONArray("data") ?: JSONArray()
                val list = mutableListOf<GroupsModel>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(GroupsModel().apply {
                        id = obj.optString("id")
                        name = obj.optString("name")
                    })
                }
                _uiState.update { it.copy(clientGroupsList = list) }
            }
        }
    }

    private fun fetchDeletedDocuments() {
        fetchDeletedDocumentsJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val res = repository.fetchDeletedDocuments()
            _uiState.update { it.copy(isLoading = false) }
            if (res.result == WebServiceHelper.ServiceCallStatus.Success) {
                val json = JSONObject(res.responseContent ?: "{}")
                val arr = json.optJSONArray("documents") ?: JSONArray()
                val list = parseDocumentsArray(arr)
                _uiState.update { it.copy(documentsList = list) }
                applyLocalFilters()
            }
        }
    }

    private fun fetchFilteredList() {
        val tab = _uiState.value.currentTab
        val client = _uiState.value.selectedFilterClient
        val matter = _uiState.value.selectedFilterMatter
        val group = _uiState.value.selectedFilterGroup

        filterDocumentsJob?.cancel()
        filterDocumentsJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val category = if (tab == "firm") "firm" else "client"

            // Construct clients payload
            val clientsPayload: Any = if (category == "client") {
                val clientId = client?.id ?: ""
                if (clientId.contains(",")) {
                    JSONArray().apply {
                        clientId.split(",").forEach { id ->
                            if (id.trim().isNotEmpty()) {
                                put(id.trim())
                            }
                        }
                    }
                } else {
                    clientId
                }
            } else {
                ""
            }

            // Construct matters payload
            val mattersPayload: Any? = if (category == "client") {
                val matterId = matter?.id ?: ""
                if (matterId.isEmpty()) {
                    null
                } else if (matterId.contains(",")) {
                    JSONArray().apply {
                        matterId.split(",").forEach { id ->
                            if (id.trim().isNotEmpty()) {
                                put(id.trim())
                            }
                        }
                    }
                } else {
                    matterId
                }
            } else {
                null
            }

            // Construct groups payload
            val groupsPayload: JSONArray? = if (category == "firm") {
                val groupId = group?.id ?: ""
                JSONArray().apply {
                    if (groupId.contains(",")) {
                        groupId.split(",").forEach { id ->
                            if (id.trim().isNotEmpty()) {
                                put(id.trim())
                            }
                        }
                    } else if (groupId.isNotEmpty()) {
                        put(groupId)
                    }
                }
            } else {
                null
            }

            android.util.Log.d("FILTER_DOCS", "Payload: Category=$category, Clients=$clientsPayload, Matters=$mattersPayload, Groups=$groupsPayload")
            val res = repository.filterDocuments(
                category = category,
                clients = clientsPayload,
                matters = mattersPayload,
                groups = groupsPayload
            )
            android.util.Log.d("FILTER_DOCS", "Response Status=${res.result}, ResponseContent=${res.responseContent}")
            val debugText = "Clients: $clientsPayload\nMatters: $mattersPayload\nStatus: ${res.result}\nResponse: ${res.responseContent}"
            _uiState.update { it.copy(isLoading = false, debugInfo = debugText) }
            if (res.result == WebServiceHelper.ServiceCallStatus.Success) {
                val json = JSONObject(res.responseContent ?: "{}")
                val arr = json.optJSONArray("data") ?: JSONArray()
                val list = parseDocumentsArray(arr)
                _uiState.update { it.copy(documentsList = list) }
                applyLocalFilters()
            }
        }
    }

    private fun parseDocumentsArray(arr: JSONArray): List<ViewDocumentsModel> {
        val list = mutableListOf<ViewDocumentsModel>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(ViewDocumentsModel().apply {
                id = obj.optString("id")
                name = obj.optString("name")
                filename = obj.optString("filename")
                description = obj.optString("description")
                expiration_date = obj.optString("expiration_date")
                created = obj.optString("created")
                uploaded_by = if (obj.has("createdby") && obj.optString("createdby").isNotEmpty()) {
                    obj.optString("createdby")
                } else {
                    obj.optString("uploaded_by")
                }
                content_type = obj.optString("content_type")
                isdisabled = obj.optBoolean("isdisabled", false)
                is_disabled = obj.optBoolean("is_disabled", false)
                is_encrypted = obj.optBoolean("is_encrypted", false)
                added_encryption = obj.optBoolean("added_encryption", false)
                is_password = obj.optBoolean("is_password", false)
                doc_type = if (obj.has("doctype")) obj.optString("doctype") else obj.optString("doc_type")
                deletedBy = obj.optString("deletedBy")
                deletedOn = obj.optString("deletedOn")
                tag = obj.optJSONObject("tags")
                tagslist = obj.optJSONArray("tag")
                category = obj.optString("category")
                origin = obj.optString("origin")
            })
        }
        return list
    }

    private fun applyLocalFilters() {
        val all = _uiState.value.documentsList
        val query = _uiState.value.searchQuery.lowercase(Locale.ROOT)
        val filterType = _uiState.value.selectedFilterDocType

        val filtered = all.filter { doc ->
            val matchesSearch = doc.name?.lowercase(Locale.ROOT)?.contains(query) == true
            val matchesType = when (filterType) {
                "firm" -> doc.doc_type?.lowercase(Locale.ROOT) == "firm"
                "client" -> doc.doc_type?.lowercase(Locale.ROOT) == "client"
                else -> true
            }
            matchesSearch && matchesType
        }
        _uiState.update { it.copy(filteredDocumentsList = filtered, currentPage = 1) }
    }

    private fun addStagedFile(file: File, name: String) {
        val contentString = file.name.replace(".", "/")
        val parts = contentString.split("/".toRegex()).toTypedArray()
        var docType = "pdf"
        var docname = file.name
        if (parts.size >= 2) {
            docType = parts[1]
            docname = parts[0]
        }
        val model = com.digicoffer.lauditor.Documents.Models.DocumentsModel().apply {
            this.name = docname
            this.filename = name
            this.content_type = docType
            this.description = docname
            this.file = file
            this.isIsenabled = false
            this.isencrypted = false
        }
        _uiState.update { it.copy(selectedUploadFiles = it.selectedUploadFiles + model) }
    }

    private fun updateStagedFile(
        index: Int, name: String, desc: String, expDate: String,
        isDownloadDisabled: Boolean, isEncrypted: Boolean, tags: org.json.JSONObject?
    ) {
        val currentList = _uiState.value.selectedUploadFiles.toMutableList()
        if (index in currentList.indices) {
            val updated = currentList[index]
            updated.name = name
            updated.description = desc
            updated.expiration_date = expDate
            updated.isIsenabled = isDownloadDisabled
            updated.isencrypted = isEncrypted
            updated.tags = tags
            currentList[index] = updated
            _uiState.update { it.copy(selectedUploadFiles = currentList) }
        }
    }

    private fun removeStagedFile(index: Int) {
        val currentList = _uiState.value.selectedUploadFiles.toMutableList()
        if (index in currentList.indices) {
            currentList.removeAt(index)
            _uiState.update { it.copy(selectedUploadFiles = currentList) }
        }
    }

    private fun uploadDocuments() {
        val currentTab = _uiState.value.currentTab
        val toUpload = _uiState.value.selectedUploadFiles
        if (toUpload.isEmpty()) {
            _uiState.update { it.copy(alertMessage = "Please pick at least one file to upload.") }
            return
        }
        if (currentTab == "client" && _uiState.value.selectedUploadClient == null) {
            _uiState.update { it.copy(alertMessage = "Please select a client name.") }
            return
        }
        if (currentTab == "matter" && _uiState.value.selectedUploadMatter == null) {
            _uiState.update { it.copy(alertMessage = "Please select a matter.") }
            return
        }
        if (currentTab == "firm" && _uiState.value.selectedUploadGroups.isEmpty()) {
            _uiState.update { it.copy(alertMessage = "Please select at least one group.") }
            return
        }
        if ((currentTab == "client" || currentTab == "matter") &&
            _uiState.value.clientGroupsList.isNotEmpty() &&
            _uiState.value.selectedUploadGroups.isEmpty()) {
            _uiState.update { it.copy(alertMessage = "Please select at least one group.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true) }
            var successCount = 0
            var lastErrorMessage = "Some document uploads failed. Please try again."
            for (docModel in toUpload) {
                val file = docModel.file ?: continue
                val payload = JSONObject().apply {
                    put("name", docModel.name)
                    put("description", docModel.description)
                    put("expiration_date", AndroidUtils.convertAnyDateToDDMMYYYY(docModel.expiration_date))
                    
                    // Parse filename
                    val contentString = file.name.replace(".", "/")
                    val parts = contentString.split("/".toRegex()).toTypedArray()
                    var docType = "pdf"
                    var docname = file.name
                    if (parts.size >= 2) {
                        docType = parts[1]
                        docname = parts[0]
                    }
                    put("filename", docname)

                    val category = if (currentTab == "firm") "firm" else "client"
                    put("category", category)

                    if (category == "client") {
                        if (currentTab == "client") {
                            val client = _uiState.value.selectedUploadClient
                            val clientArr = JSONArray()
                            if (client != null) {
                                clientArr.put(JSONObject().apply {
                                    put("id", client.id)
                                    put("type", client.type)
                                })
                            }
                            put("clients", clientArr)
                        } else {
                            // matter upload
                            put("clients", JSONArray()) // empty array
                            val matter = _uiState.value.selectedUploadMatter
                            if (matter != null) {
                                put("matters", JSONArray().apply { put(matter.id) })
                            }
                        }
                    } else {
                        // firm upload
                        put("clients", "")
                    }

                    // Groups
                    val groupsArr = JSONArray().apply {
                        _uiState.value.selectedUploadGroups.forEach { put(it.id) }
                    }
                    put("groups", groupsArr)
                    
                    put("downloadDisabled", docModel.isIsenabled)
                    put("custom_encrypt", docModel.isencrypted)
                    if (docModel.tags == null) {
                        put("tags", "")
                    } else {
                        put("tags", docModel.tags)
                    }

                    val contentType = if (docType.equals("apng", ignoreCase = true) || docType.equals("avif", ignoreCase = true) || docType.equals("gif", ignoreCase = true) || docType.equals("jpeg", ignoreCase = true) || docType.equals("png", ignoreCase = true) || docType.equals("svg", ignoreCase = true) || docType.equals("webp", ignoreCase = true) || docType.equals("jpg", ignoreCase = true)) {
                        "image/$docType"
                    } else {
                        "application/$docType"
                    }
                    put("content_type", contentType)
                }

                val res = repository.uploadDocument(file, payload.toString())
                if (res.result == WebServiceHelper.ServiceCallStatus.Success) {
                    val json = JSONObject(res.responseContent ?: "{}")
                    if (!json.optBoolean("error", false)) {
                        successCount++
                    } else {
                        lastErrorMessage = json.optString("msg", json.optString("message", "Upload error: ${json.toString()}"))
                    }
                } else {
                    lastErrorMessage = "Server call failed: ${res.responseContent ?: "Unknown error"}"
                }
            }

            _uiState.update { it.copy(isUploading = false) }
            if (successCount == toUpload.size) {
                _uiState.update {
                    it.copy(
                        toastMessage = "Documents uploaded successfully.",
                        isUploadMode = false,
                        selectedUploadFiles = emptyList(),
                        selectedUploadGroups = emptyList(),
                        selectedUploadClient = null,
                        selectedUploadMatter = null
                    )
                }
                switchTab(currentTab)
            } else {
                _uiState.update { it.copy(alertMessage = lastErrorMessage) }
            }
        }
    }

    private fun triggerDocAction(actionType: String, doc: ViewDocumentsModel) {
        when (actionType) {
            "View" -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isLoading = true) }
                    val isEncrypted = doc.is_encrypted == true || doc.added_encryption == true
                    val res = if (isEncrypted) {
                        repository.decryptDocument(doc.id ?: "", false)
                    } else {
                        repository.viewDocumentInfo(doc.id ?: "")
                    }
                    if (res.result == WebServiceHelper.ServiceCallStatus.Success) {
                        val json = JSONObject(res.responseContent ?: "{}")
                        val dataObj = json.optJSONObject("data")
                        val url = dataObj?.optString("url") ?: json.optString("url")
                        if (!url.isNullOrEmpty()) {
                            val contentType = doc.content_type ?: ""
                            val isImage = contentType.startsWith("image/", ignoreCase = true)
                            val isPdf = contentType.contains("pdf", ignoreCase = true) || (doc.name ?: "").endsWith(".pdf", ignoreCase = true)
                            
                            val finalUrl: String?
                            val finalDoc: ViewDocumentsModel
                            if (!isImage && !isPdf) {
                                val converted = convertDocToPdfUrl(url)
                                if (converted != null) {
                                    finalUrl = converted
                                    finalDoc = ViewDocumentsModel().apply {
                                        this.created = doc.created
                                        this.description = doc.description
                                        this.added_encryption = doc.added_encryption
                                        this.expiration_date = doc.expiration_date
                                        this.filename = doc.filename
                                        this.content_type = "application/pdf"
                                        this.id = doc.id
                                        this.isdisabled = doc.isdisabled
                                        this.is_disabled = doc.is_disabled
                                        this.is_encrypted = doc.is_encrypted
                                        this.is_password = doc.is_password
                                        this.name = doc.name
                                        this.origin = doc.origin
                                        this.uploaded_by = doc.uploaded_by
                                        this.doc_type = doc.doc_type
                                        this.deletedBy = doc.deletedBy
                                        this.deletedOn = doc.deletedOn
                                        this.category = doc.category
                                        this.tag = doc.tag
                                        this.tagslist = doc.tagslist
                                        this.isChecked = doc.isChecked
                                    }
                                } else {
                                    finalUrl = url
                                    finalDoc = doc
                                }
                            } else {
                                finalUrl = url
                                finalDoc = doc
                            }
                            _uiState.update { it.copy(isLoading = false, previewDocUrl = finalUrl, previewDocModel = finalDoc) }
                        } else {
                            _uiState.update { it.copy(isLoading = false, alertMessage = json.optString("msg", "Unable to fetch document preview link.")) }
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false, alertMessage = "API call failed. Please try again.") }
                    }
                }
            }
            "Download" -> {
                _uiState.update { it.copy(confirmDocModel = doc, confirmActionType = "download") }
            }
            "Edit Info" -> {
                _uiState.update { it.copy(editDocModel = doc) }
            }
            "Update Tags" -> {
                _uiState.update { it.copy(tagsDocModel = doc) }
            }
            else -> {
                _uiState.update { it.copy(confirmDocModel = doc, confirmActionType = actionType) }
            }
        }
    }

    private fun executeConfirmAction() {
        val doc = _uiState.value.confirmDocModel ?: return
        val type = _uiState.value.confirmActionType ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, confirmDocModel = null, confirmActionType = null) }
            val res = when (type) {
                "download" -> {
                    val isEncrypted = doc.is_encrypted == true || doc.added_encryption == true
                    val r = if (isEncrypted) {
                        repository.decryptDocument(doc.id ?: "", true)
                    } else {
                        repository.downloadDocumentInfo(doc.id ?: "")
                    }
                    if (r.result == WebServiceHelper.ServiceCallStatus.Success) {
                        val json = JSONObject(r.responseContent ?: "{}")
                        val dataObj = json.optJSONObject("data")
                        val url = dataObj?.optString("url") ?: json.optString("url")
                        if (!url.isNullOrEmpty()) {
                            var resolvedName = doc.filename ?: doc.name ?: "DownloadedFile"
                            if (!resolvedName.contains(".")) {
                                val extension = when (doc.content_type?.lowercase()) {
                                    "application/pdf" -> "pdf"
                                    "image/png" -> "png"
                                    "image/jpeg", "image/jpg" -> "jpg"
                                    "text/plain" -> "txt"
                                    "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "docx"
                                    "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> "xlsx"
                                    else -> null
                                }
                                if (extension != null) {
                                    resolvedName = "$resolvedName.$extension"
                                }
                            }
                            com.digicoffer.lauditor.CommonFiles.GlobalFiles.FileDownloader.downloadFile(
                                getApplication(), url, resolvedName, doc.content_type ?: ""
                            )
                            _uiState.update { it.copy(toastMessage = "Download started.") }
                        }
                    }
                    r
                }
                "deleted" -> {
                    repository.permanentlyDeleteDocument(doc.id ?: "", doc.doc_type ?: "client")
                }
                "restore" -> {
                    repository.restoreDeletedDocument(doc.id ?: "", doc.doc_type ?: "client")
                }
                "disabled" -> {
                    repository.enableDocDownload(doc.id ?: "", false)
                }
                "enabled" -> {
                    repository.enableDocDownload(doc.id ?: "", true)
                }
                "encrypt" -> {
                    repository.encryptDoc(doc.id ?: "")
                }
                "decrypt" -> {
                    repository.decryptDoc(doc.id ?: "")
                }
                else -> {
                    repository.deleteDocument(doc.id ?: "")
                }
            }
            _uiState.update { it.copy(isLoading = false) }
            if (res.result == WebServiceHelper.ServiceCallStatus.Success) {
                val json = JSONObject(res.responseContent ?: "{}")
                val msg = json.optString("msg", "Action completed successfully.")
                _uiState.update { it.copy(toastMessage = msg) }
                switchTab(_uiState.value.currentTab)
            } else {
                _uiState.update { it.copy(alertMessage = "Operation failed. Please try again.") }
            }
        }
    }

    private fun saveMetadata(docId: String, name: String, desc: String, expDate: String, tags: org.json.JSONObject?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, editDocModel = null) }
            val res1 = repository.updateMetadata(docId, name, desc, expDate)
            var success = res1.result == WebServiceHelper.ServiceCallStatus.Success
            if (success && tags != null) {
                val res2 = repository.updateTags(docId, name, tags, false)
                success = res2.result == WebServiceHelper.ServiceCallStatus.Success
            }
            _uiState.update { it.copy(isLoading = false) }
            if (success) {
                _uiState.update { it.copy(toastMessage = "Metadata and tags updated successfully.") }
                switchTab(_uiState.value.currentTab)
            } else {
                _uiState.update { it.copy(alertMessage = "Update failed. Please try again.") }
            }
        }
    }

    private fun saveTags(docId: String, name: String, tags: Map<String, String>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, tagsDocModel = null) }
            val tagsJson = JSONObject().apply {
                tags.forEach { (k, v) -> put(k, v) }
            }
            val res = repository.updateTags(docId, name, tagsJson, false)
            _uiState.update { it.copy(isLoading = false) }
            if (res.result == WebServiceHelper.ServiceCallStatus.Success) {
                _uiState.update { it.copy(toastMessage = "Tags updated successfully.") }
                switchTab(_uiState.value.currentTab)
            } else {
                _uiState.update { it.copy(alertMessage = "Tags update failed. Please try again.") }
            }
        }
    }

    private fun loadGridPreview(doc: ViewDocumentsModel) {
        val docId = doc.id ?: return
        if (_uiState.value.previewUrls.containsKey(docId) || _uiState.value.loadingPreviewIds.contains(docId) || _uiState.value.failedPreviewIds.contains(docId)) {
            return
        }
        
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(loadingPreviewIds = it.loadingPreviewIds + docId) }
            var success = false
            try {
                val isEncrypted = doc.is_encrypted == true || doc.added_encryption == true
                val res = if (isEncrypted) {
                    repository.decryptDocument(docId, false)
                } else {
                    repository.viewDocumentInfo(docId)
                }
                if (res.result == WebServiceHelper.ServiceCallStatus.Success) {
                    val json = JSONObject(res.responseContent ?: "{}")
                    val dataObj = json.optJSONObject("data")
                    val url = dataObj?.optString("url") ?: json.optString("url")
                    if (!url.isNullOrEmpty()) {
                        val contentType = doc.content_type ?: ""
                        val isImage = contentType.startsWith("image/", ignoreCase = true)
                        val isPdf = contentType.contains("pdf", ignoreCase = true) || url.contains(".pdf", ignoreCase = true)
                        
                        if (isPdf) {
                            _uiState.update { it.copy(previewUrls = it.previewUrls + (docId to url)) }
                            renderPdfFirstPage(docId, url)
                            success = true
                        } else if (isImage) {
                            _uiState.update { it.copy(previewUrls = it.previewUrls + (docId to url)) }
                            success = true
                        } else {
                            val converted = convertDocToPdfUrl(url)
                            if (converted != null) {
                                _uiState.update { it.copy(previewUrls = it.previewUrls + (docId to converted)) }
                                renderPdfFirstPage(docId, converted)
                                success = true
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _uiState.update { 
                    it.copy(
                        loadingPreviewIds = it.loadingPreviewIds - docId,
                        failedPreviewIds = if (success) it.failedPreviewIds else it.failedPreviewIds + docId
                    )
                }
            }
        }
    }

    private fun renderPdfFirstPage(docId: String, url: String) {
        var success = false
        try {
            val isLocal = url.startsWith("localfile://")
            val tempFile = if (isLocal) {
                File(url.replace("localfile://", ""))
            } else {
                val file = File.createTempFile("grid_prev_$docId", ".pdf", getApplication<Application>().cacheDir)
                val request = okhttp3.Request.Builder().url(url).build()
                val client = okhttp3.OkHttpClient()
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        response.body?.byteStream()?.use { input ->
                            FileOutputStream(file).use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                }
                file
            }
            
            val pfd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = android.graphics.pdf.PdfRenderer(pfd)
            if (renderer.pageCount > 0) {
                val page = renderer.openPage(0)
                val w = 600
                val h = (w * page.height) / page.width
                val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(android.graphics.Color.WHITE)
                page.render(bitmap, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                
                _uiState.update { it.copy(previewBitmaps = it.previewBitmaps + (docId to bitmap)) }
                success = true
                page.close()
            }
            renderer.close()
            pfd.close()
            if (!isLocal) {
                tempFile.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (!success) {
                _uiState.update { it.copy(failedPreviewIds = it.failedPreviewIds + docId) }
            }
        }
    }

    private suspend fun convertDocToPdfUrl(rawUrl: String): String? {
        return kotlinx.coroutines.withContext(Dispatchers.IO) {
            try {
                val doctopdfUrl = Constants.doctopdfUrl ?: return@withContext null
                val bodyJson = JSONObject().apply {
                    put("url", rawUrl)
                }
                
                val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                val body = okhttp3.RequestBody.create(
                    mediaType,
                    bodyJson.toString()
                )
                
                val request = okhttp3.Request.Builder()
                    .url(doctopdfUrl)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer ${Constants.TOKEN}")
                    .build()
                
                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val ct = response.header("Content-Type") ?: ""
                        if (ct.contains("application/pdf")) {
                            val tempFile = File.createTempFile("converted_${System.currentTimeMillis()}", ".pdf", getApplication<Application>().cacheDir)
                            response.body?.byteStream()?.use { input ->
                                FileOutputStream(tempFile).use { output ->
                                    input.copyTo(output)
                                }
                            }
                            "localfile://${tempFile.absolutePath}"
                        } else {
                            val respString = response.body?.string() ?: ""
                            val resp = JSONObject(respString)
                            if (!resp.optBoolean("error", true)) {
                                val data = resp.optJSONObject("data")
                                data?.optString("url")
                            } else {
                                null
                            }
                        }
                    } else {
                        null
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
