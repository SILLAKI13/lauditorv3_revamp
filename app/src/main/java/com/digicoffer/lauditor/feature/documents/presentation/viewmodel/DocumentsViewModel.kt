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
            is DocumentsUiEvent.SelectFilterGroups -> {
                _uiState.update { it.copy(selectedFilterGroups = event.groups) }
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
            is DocumentsUiEvent.PagePrev -> {
                if (_uiState.value.currentPage > 1) {
                    _uiState.update { it.copy(currentPage = it.currentPage - 1) }
                }
            }
            is DocumentsUiEvent.PageNext -> {
                val totalPages = kotlin.math.ceil(_uiState.value.filteredDocumentsList.size.toDouble() / _uiState.value.itemsPerPage).toInt()
                if (_uiState.value.currentPage < totalPages) {
                    _uiState.update { it.copy(currentPage = it.currentPage + 1) }
                }
            }
            is DocumentsUiEvent.LoadPreview -> loadGridPreview(event.doc)
            is DocumentsUiEvent.RefreshDocuments -> refreshCurrentDocuments()
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
            val arr = if (res.result == WebServiceHelper.ServiceCallStatus.Success) {
                val json = JSONObject(res.responseContent ?: "{}")
                json.optJSONArray("data") ?: JSONArray()
            } else {
                JSONArray().apply {
                    put(JSONObject().apply {
                        put("id", "g1")
                        put("name", "madesh firm")
                    })
                    put(JSONObject().apply {
                        put("id", "g2")
                        put("name", "Finance Group")
                    })
                }
            }
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
                        selectedFilterGroup = allGroupsModel,
                        selectedFilterGroups = listOf(allGroupsModel)
                    )
                }
                fetchFilteredList()
            } else {
                val allGroupsModel = GroupsModel().apply {
                    id = "all"
                    name = "All Groups"
                }
                _uiState.update { it.copy(groupsList = listOf(allGroupsModel), selectedFilterGroup = allGroupsModel, selectedFilterGroups = listOf(allGroupsModel)) }
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
                val selected = if (_uiState.value.selectedFilterGroups.isNotEmpty()) {
                    _uiState.value.selectedFilterGroups
                } else if (_uiState.value.selectedFilterGroup != null) {
                    listOf(_uiState.value.selectedFilterGroup!!)
                } else {
                    emptyList()
                }
                JSONArray().apply {
                    selected.forEach { grp ->
                        val groupId = grp.id ?: ""
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
            } else if (_uiState.value.documentsList.isEmpty()) {
                val sampleDocs = listOf(
                    ViewDocumentsModel().apply {
                        id = "doc1"
                        name = "Sample Enabled Doc.pdf"
                        filename = "Sample Enabled Doc.pdf"
                        description = "Test document with download enabled"
                        created = "2026-09-02T10:00:00Z"
                        uploaded_by = "Admin"
                        isdisabled = false
                        is_disabled = false
                        download_permission = true
                        this.category = "firm"
                        this.doc_type = "firm"
                    },
                    ViewDocumentsModel().apply {
                        id = "doc2"
                        name = "Sample Disabled Doc.pdf"
                        filename = "Sample Disabled Doc.pdf"
                        description = "Test document with download disabled"
                        created = "2026-09-01T10:00:00Z"
                        uploaded_by = "Admin"
                        isdisabled = false
                        is_disabled = true
                        download_permission = false
                        this.category = "firm"
                        this.doc_type = "firm"
                    }
                )
                _uiState.update { it.copy(documentsList = sampleDocs) }
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
                download_permission = obj.optBoolean("download_permission", true)
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
        val filterType = _uiState.value.selectedFilterDocType?.lowercase(Locale.ROOT)

        val filtered = all.filter { doc ->
            val matchesSearch = doc.name?.lowercase(Locale.ROOT)?.contains(query) == true
            val matchesType = if (filterType.isNullOrEmpty() || filterType == "all types") {
                true
            } else {
                doc.category?.lowercase(Locale.ROOT) == filterType || doc.doc_type?.lowercase(Locale.ROOT) == filterType
            }
            matchesSearch && matchesType
        }
        _uiState.update { it.copy(filteredDocumentsList = filtered, currentPage = 1) }
    }

    private fun addStagedFile(file: File, name: String) {
        val baseName = name.substringBeforeLast('.', missingDelimiterValue = name)
        val ext = file.extension
        val docType = if (ext.isNotEmpty()) ext.lowercase(Locale.ROOT) else "pdf"
        val model = com.digicoffer.lauditor.Documents.Models.DocumentsModel().apply {
            this.name = baseName
            this.filename = name
            this.content_type = docType
            this.description = baseName
            this.file = file
            this.isIsenabled = true
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
            val removed = currentList.removeAt(index)
            try {
                if (removed.file?.parentFile?.name == "staged_uploads") {
                    removed.file?.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
        if (currentTab == "client") {
            val client = _uiState.value.selectedUploadClient
            if (client == null || client.id.isNullOrEmpty() || client.name == "All Clients" || (client.id ?: "").contains(",")) {
                _uiState.update { it.copy(alertMessage = "Please select a client name.") }
                return
            }
        }
        if (currentTab == "matter") {
            val matter = _uiState.value.selectedUploadMatter
            if (matter == null || matter.id.isNullOrEmpty() || matter.name == "All Matters" || matter.title == "All Matters" || (matter.id ?: "").contains(",")) {
                _uiState.update { it.copy(alertMessage = "Please select a matter.") }
                return
            }
        }
        if (currentTab == "firm" && _uiState.value.selectedUploadGroups.isEmpty()) {
            _uiState.update { it.copy(alertMessage = "Please select at least one group.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true) }
            var successCount = 0
            var lastErrorMessage = "Some document uploads failed. Please try again."
            for (docModel in toUpload) {
                val file = docModel.file
                if (file == null || !file.exists()) {
                    lastErrorMessage = "File not found: ${file?.name ?: "Unknown"}. Please select the file again."
                    continue
                }
                val payload = JSONObject().apply {
                    put("name", docModel.name)
                    put("description", docModel.description)
                    put("expiration_date", AndroidUtils.convertAnyDateToDDMMYYYY(docModel.expiration_date))
                    
                    // Parse filename and docType
                    val ext = file.extension.ifEmpty { "pdf" }.lowercase(Locale.ROOT)
                    val docname = file.nameWithoutExtension.ifEmpty { file.name }
                    put("filename", docname)

                    val category = if (currentTab == "firm") "firm" else "client"
                    put("category", category)

                    if (category == "client") {
                        if (currentTab == "client") {
                            val client = _uiState.value.selectedUploadClient
                            val clientArr = JSONArray()
                            if (client != null) {
                                val clientId = client.id ?: ""
                                if (clientId.contains(",")) {
                                    clientId.split(",").forEach { id ->
                                        if (id.trim().isNotEmpty()) {
                                            clientArr.put(JSONObject().apply {
                                                put("id", id.trim())
                                                put("type", client.type ?: "consumer")
                                            })
                                        }
                                    }
                                } else if (clientId.isNotEmpty()) {
                                    clientArr.put(JSONObject().apply {
                                        put("id", clientId)
                                        put("type", client.type ?: "consumer")
                                    })
                                }
                            }
                            put("clients", clientArr)
                            android.util.Log.d("DOCUMENT_UPLOAD_DEBUG", "clientCount=${clientArr.length()}, firstId=${if (clientArr.length() > 0) clientArr.optJSONObject(0)?.optString("id") else "none"}, containsComma=${(client?.id ?: "").contains(",")}")
                        } else {
                            // matter upload
                            put("clients", JSONArray()) // empty array
                            val matter = _uiState.value.selectedUploadMatter
                            val mattersArr = JSONArray()
                            if (matter != null) {
                                val matterId = matter.id ?: ""
                                if (matterId.contains(",")) {
                                    matterId.split(",").forEach { id ->
                                        if (id.trim().isNotEmpty()) {
                                            mattersArr.put(id.trim())
                                        }
                                    }
                                } else if (matterId.isNotEmpty()) {
                                    mattersArr.put(matterId)
                                }
                            }
                            put("matters", mattersArr)
                            android.util.Log.d("DOCUMENT_UPLOAD_DEBUG", "matterCount=${mattersArr.length()}, firstId=${if (mattersArr.length() > 0) mattersArr.optString(0) else "none"}, containsComma=${(matter?.id ?: "").contains(",")}")
                        }
                    } else {
                        // firm upload
                        put("clients", "")
                    }

                    // Groups (only populated for firm documents)
                    val groupsArr = JSONArray().apply {
                        if (currentTab == "firm") {
                            _uiState.value.selectedUploadGroups.forEach { grp ->
                                val groupId = grp.id ?: ""
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
                        }
                    }
                    put("groups", groupsArr)
                    android.util.Log.d("DOCUMENT_UPLOAD_DEBUG", "category=$category, groupsCount=${groupsArr.length()}")
                    
                    put("downloadDisabled", docModel.isIsenabled)
                    put("custom_encrypt", docModel.isencrypted)
                    if (docModel.tags == null) {
                        put("tags", "")
                    } else {
                        put("tags", docModel.tags)
                    }

                    val contentType = if (ext.equals("apng", ignoreCase = true) || ext.equals("avif", ignoreCase = true) || ext.equals("gif", ignoreCase = true) || ext.equals("jpeg", ignoreCase = true) || ext.equals("png", ignoreCase = true) || ext.equals("svg", ignoreCase = true) || ext.equals("webp", ignoreCase = true) || ext.equals("jpg", ignoreCase = true)) {
                        "image/$ext"
                    } else {
                        "application/$ext"
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
                toUpload.forEach { doc ->
                    try {
                        if (doc.file?.parentFile?.name == "staged_uploads") {
                            doc.file?.delete()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                _uiState.update {
                    it.copy(
                        alertTitle = "Alert !",
                        alertMessage = "Documents uploaded successfully.",
                        isUploadMode = false,
                        selectedUploadFiles = emptyList(),
                        selectedUploadGroups = emptyList(),
                        selectedUploadClient = null,
                        selectedUploadMatter = null
                    )
                }
                refreshCurrentDocuments()
            } else {
                _uiState.update { it.copy(alertTitle = "Alert", alertMessage = lastErrorMessage) }
            }
        }
    }

    fun refreshCurrentDocuments() {
        val tab = _uiState.value.currentTab
        when (tab) {
            "matter", "client", "firm" -> {
                fetchFilteredList()
            }
            "delete" -> {
                fetchDeletedDocuments()
            }
        }
    }

    private fun triggerDocAction(actionType: String, doc: ViewDocumentsModel) {
        when (actionType) {
            "View" -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isLoading = true) }
                    val isEncrypted = doc.is_encrypted == true || doc.added_encryption == true
                    val viewRes = repository.viewDocumentInfo(doc.id ?: "")
                    if (viewRes.result == WebServiceHelper.ServiceCallStatus.Success) {
                        val json = JSONObject(viewRes.responseContent ?: "{}")
                        val isError = json.optBoolean("error", false)
                        if (isError) {
                            _uiState.update { it.copy(isLoading = false, alertTitle = "Alert", alertMessage = json.optString("msg", "Unable to view document")) }
                            return@launch
                        }

                        val dataObj = json.optJSONObject("data")
                        val rawUrl = dataObj?.optString("url") ?: json.optString("url")
                        val rawContentType = dataObj?.optString("content_type")?.ifEmpty { doc.content_type } ?: doc.content_type ?: ""
                        val rawFilename = dataObj?.optString("filename")?.ifEmpty { doc.name } ?: doc.name ?: ""

                        if (isEncrypted) {
                            val decRes = repository.decryptDocument(doc.id ?: "", false)
                            if (decRes.result == WebServiceHelper.ServiceCallStatus.Success) {
                                val decJson = JSONObject(decRes.responseContent ?: "{}")
                                val isDecError = decJson.optBoolean("error", false)
                                if (!isDecError) {
                                    val decData = decJson.optJSONObject("data")
                                    val decryptedUrl = decData?.optString("url") ?: decJson.optString("url")
                                    val decryptedFilename = decData?.optString("filename") ?: rawFilename

                                    if (!decryptedUrl.isNullOrEmpty()) {
                                        var effectiveContentType = decData?.optString("content_type")?.ifEmpty { null }
                                            ?: rawContentType.ifEmpty { null }
                                            ?: doc.content_type?.ifEmpty { null }
                                            ?: ""

                                        if (effectiveContentType.isEmpty() || effectiveContentType.equals("application/octet-stream", ignoreCase = true)) {
                                            val fallbackName = if (!decryptedFilename.isNullOrEmpty()) decryptedFilename else ((doc.filename ?: doc.name) ?: "")
                                            val ext = fallbackName.substringAfterLast('.', "").lowercase(Locale.ROOT)
                                            if (listOf("jpg", "jpeg", "png", "gif", "webp", "bmp", "svg", "apng", "avif").contains(ext)) {
                                                effectiveContentType = "image/$ext"
                                            } else if (ext == "pdf") {
                                                effectiveContentType = "application/pdf"
                                            }
                                        }

                                        var finalUrl = decryptedUrl
                                        val lowerExt = (decryptedFilename.ifEmpty { doc.filename ?: doc.name ?: "" }).substringAfterLast('.', "").lowercase(Locale.ROOT)
                                        val isImg = effectiveContentType.startsWith("image/", ignoreCase = true) ||
                                                listOf("jpg", "jpeg", "png", "gif", "webp", "bmp", "svg", "apng", "avif").contains(lowerExt)
                                        val isPdf = effectiveContentType == "application/pdf" || lowerExt == "pdf"
                                        if (!isImg && !isPdf) {
                                            val converted = convertDocToPdfUrl(decryptedUrl)
                                            if (!converted.isNullOrEmpty()) {
                                                finalUrl = converted
                                                effectiveContentType = "application/pdf"
                                            }
                                        }

                                        val finalDoc = ViewDocumentsModel().apply {
                                            this.created = doc.created
                                            this.description = doc.description
                                            this.added_encryption = doc.added_encryption
                                            this.expiration_date = doc.expiration_date
                                            this.filename = decryptedFilename
                                            this.content_type = effectiveContentType
                                            this.id = doc.id
                                            this.name = doc.name
                                        }
                                        _uiState.update { it.copy(isLoading = false, previewDocUrl = finalUrl, previewDocModel = finalDoc) }
                                    } else {
                                        _uiState.update { it.copy(isLoading = false, alertTitle = "Alert", alertMessage = decJson.optString("msg", "Unable to fetch decrypted preview link.")) }
                                    }
                                } else {
                                    _uiState.update { it.copy(isLoading = false, alertTitle = "Alert", alertMessage = decJson.optString("msg", "Failed to decrypt document.")) }
                                }
                            } else {
                                _uiState.update { it.copy(isLoading = false, alertTitle = "Alert", alertMessage = "Failed to decrypt document. Please try again.") }
                            }
                        } else {
                            if (!rawUrl.isNullOrEmpty()) {
                                val lowerUrl = rawUrl.lowercase(Locale.ROOT)
                                val lowerExt = rawFilename.substringAfterLast('.', "").lowercase(Locale.ROOT)
                                val isImage = rawContentType.startsWith("image/", ignoreCase = true) ||
                                        lowerUrl.contains(".jpg") || lowerUrl.contains(".jpeg") ||
                                        lowerUrl.contains(".png") || lowerUrl.contains(".gif") ||
                                        lowerUrl.contains(".webp") || listOf("jpg", "jpeg", "png", "gif", "webp", "svg", "avif", "apng").contains(lowerExt)
                                val isPdf = rawContentType == "application/pdf" || rawContentType.contains("pdf", ignoreCase = true) ||
                                        lowerUrl.contains("application/pdf") || lowerUrl.contains(".pdf") || lowerExt == "pdf"

                                var finalUrl = rawUrl
                                var finalContentType = rawContentType
                                var conversionCalled = false
                                if (!isImage && !isPdf) {
                                    conversionCalled = true
                                    val converted = convertDocToPdfUrl(rawUrl)
                                    if (!converted.isNullOrEmpty()) {
                                        finalUrl = converted
                                        finalContentType = "application/pdf"
                                    }
                                }

                                val finalDoc = ViewDocumentsModel().apply {
                                    this.created = doc.created
                                    this.description = doc.description
                                    this.added_encryption = doc.added_encryption
                                    this.expiration_date = doc.expiration_date
                                    this.filename = rawFilename
                                    this.content_type = finalContentType
                                    this.id = doc.id
                                    this.name = doc.name
                                }
                                _uiState.update { it.copy(isLoading = false, previewDocUrl = finalUrl, previewDocModel = finalDoc) }
                            } else {
                                _uiState.update { it.copy(isLoading = false, alertTitle = "Alert", alertMessage = json.optString("msg", "Unable to fetch document preview link.")) }
                            }
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false, alertTitle = "Alert", alertMessage = "API call failed. Please try again.") }
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
                "disabled", "enable_download" -> {
                    repository.enableDocDownload(doc.id ?: "", false)
                }
                "enabled", "disable_download" -> {
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
                val isError = json.optBoolean("error", false)
                val rawMsg = if (json.has("msg") && json.optString("msg").isNotEmpty()) {
                    json.optString("msg")
                } else if (json.has("message") && json.optString("message").isNotEmpty()) {
                    json.optString("message")
                } else {
                    when (type) {
                        "encrypt" -> "encrypt added sucessfully!!"
                        "decrypt" -> "Document decrypted successfully."
                        "disabled", "enable_download" -> "Download enabled successfully."
                        "enabled", "disable_download" -> "Download disabled successfully."
                        "download" -> "Download started."
                        "deleted" -> "Document permanently deleted."
                        "restore" -> "Document restored successfully."
                        else -> "Action completed successfully."
                    }
                }
                if (!isError) {
                    _uiState.update { it.copy(alertTitle = "Alert !", alertMessage = rawMsg) }
                    refreshCurrentDocuments()
                } else {
                    _uiState.update { it.copy(alertTitle = "Alert", alertMessage = rawMsg) }
                }
            } else {
                _uiState.update { it.copy(alertTitle = "Alert", alertMessage = "Operation failed. Please try again.") }
            }
        }
    }

    private fun saveMetadata(docId: String, name: String, desc: String, expDate: String, tags: org.json.JSONObject?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, editDocModel = null) }
            val formattedExpDate = if (expDate.equals("NA", ignoreCase = true) || expDate.isBlank()) {
                ""
            } else {
                AndroidUtils.convertAnyDateToDDMMYYYY(expDate)
            }
            val res1 = repository.updateMetadata(docId, name, desc, formattedExpDate)
            val json1 = try { JSONObject(res1.responseContent ?: "{}") } catch (e: Exception) { JSONObject() }
            var success = res1.result == WebServiceHelper.ServiceCallStatus.Success && !json1.optBoolean("error", false)
            if (success && tags != null) {
                val res2 = repository.updateTags(docId, name, tags, false)
                val json2 = try { JSONObject(res2.responseContent ?: "{}") } catch (e: Exception) { JSONObject() }
                success = res2.result == WebServiceHelper.ServiceCallStatus.Success && !json2.optBoolean("error", false)
            }
            _uiState.update { it.copy(isLoading = false) }
            if (success) {
                val msg = if (json1.has("msg") && json1.optString("msg").isNotEmpty()) {
                    json1.optString("msg")
                } else if (json1.has("message") && json1.optString("message").isNotEmpty()) {
                    json1.optString("message")
                } else {
                    "Metadata updated successfully."
                }
                _uiState.update { it.copy(alertTitle = "Alert !", alertMessage = msg) }
                refreshCurrentDocuments()
            } else {
                val errorMsg = if (json1.has("msg") && json1.optString("msg").isNotEmpty()) {
                    json1.optString("msg")
                } else if (json1.has("message") && json1.optString("message").isNotEmpty()) {
                    json1.optString("message")
                } else {
                    "Update failed. Please try again."
                }
                _uiState.update { it.copy(alertTitle = "Alert", alertMessage = errorMsg) }
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
                val json = JSONObject(res.responseContent ?: "{}")
                val msg = if (json.has("msg") && json.optString("msg").isNotEmpty()) {
                    json.optString("msg")
                } else {
                    "Tags updated successfully."
                }
                _uiState.update { it.copy(alertTitle = "Alert !", alertMessage = msg) }
                refreshCurrentDocuments()
            } else {
                _uiState.update { it.copy(alertTitle = "Alert", alertMessage = "Tags update failed. Please try again.") }
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
