package com.digicoffer.lauditor.feature.matter.presentation.viewmodel

import android.app.Application
import android.net.Uri
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.Matter.Models.AdvocateModel
import com.digicoffer.lauditor.Matter.Models.ClientsModel
import com.digicoffer.lauditor.Matter.Models.MatterModel
import com.digicoffer.lauditor.Matter.Models.TeamModel
import com.digicoffer.lauditor.Matter.Models.ViewMatterModel
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.feature.matter.presentation.state.MatterEditUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume

class MatterEditViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MatterEditUiState())
    val uiState: StateFlow<MatterEditUiState> = _uiState.asStateFlow()

    private var legacyModel = MatterModel()

    fun resetForm() {
        legacyModel = MatterModel()
        val matterType = Constants.MATTER_TYPE ?: "Legal"
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        val today = sdf.format(Date())
        Constants.GeneratedMatterId = ""
        Constants.Matter_id = ""
        _uiState.value = MatterEditUiState(
            matterType = matterType,
            createdDate = today,
            createdMatterId = null,
            isMatterCreated = false
        )
        viewModelScope.launch {
            fetchGeneratedMatterId()
            fetchCaseTypes()
        }
    }

    fun clearToastMessage() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    fun consumeNavigationEvents() {
        _uiState.update { it.copy(navigateToNext = false, navigateToView = false) }
    }

    private var initJob: kotlinx.coroutines.Job? = null
    private var currentInitializedMatterId: String? = null

    fun consumeUpdateSuccess() {
        _uiState.update {
            it.copy(
                showSuccessDialog = false,
                successMessage = ""
            )
        }
    }

    fun resetTransientState() {
        _uiState.update {
            it.copy(
                showSuccessDialog = false,
                successMessage = "",
                alertTitle = null,
                alertMessage = null,
                errorMessage = null,
                toastMessage = null,
                isLoading = false,
                isUploadingDocuments = false,
                previewDocUrl = null,
                previewDocModel = null,
                editMetadataFileIndex = null,
                showBrowseDialog = false,
                navigateToNext = false,
                navigateToView = false
            )
        }
    }

    fun resetEditSession() {
        initJob?.cancel()
        initJob = null
        currentInitializedMatterId = null
        resetTransientState()
    }

    fun initialize(editModel: ViewMatterModel?) {
        val targetId = editModel?.id ?: Constants.Matter_id ?: ""
        if (targetId.isNotEmpty() && targetId == currentInitializedMatterId && (initJob?.isActive == true || _uiState.value.isMatterCreated)) {
            return
        }
        currentInitializedMatterId = if (targetId.isNotEmpty()) targetId else null
        initJob?.cancel()
        initJob = viewModelScope.launch {
            resetTransientState()
            val matterType = Constants.MATTER_TYPE ?: "Legal"
            
            if (editModel != null) {
                _uiState.update { it.copy(matterType = matterType, createdMatterId = editModel.id, isMatterCreated = true) }
                populateFromEditModel(editModel)
                val matterId = editModel.id ?: Constants.Matter_id ?: ""
                if (matterId.isNotEmpty()) {
                    fetchAndPopulateMatterDetails(matterId)
                }
                if (_uiState.value.caseTypeList.isEmpty()) {
                    fetchCaseTypes()
                }
            } else {
                _uiState.update { it.copy(matterType = matterType) }
                if (_uiState.value.matterNumber.isEmpty() && !_uiState.value.isMatterCreated) {
                    fetchGeneratedMatterId()
                }
                if (_uiState.value.caseTypeList.isEmpty()) {
                    fetchCaseTypes()
                }
            }
        }
    }

    private suspend fun fetchAndPopulateMatterDetails(matterId: String) {
        _uiState.update { it.copy(isLoading = true) }
        try {
            val result = getMatterDetailsApi(matterId)
            if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                val json = JSONObject(result.responseContent ?: "{}")
                val matter = json.optJSONObject("matter")
                if (matter != null) {
                    // Parse owner
                    val ownerObj = matter.optJSONObject("owner")
                    if (ownerObj != null) {
                        Constants.owner_id = ownerObj.optString("id")
                        Constants.owner_name = ownerObj.optString("name")
                    }
                    
                    // Parse members
                    val membersList = mutableListOf<TeamModel>()
                    val membersArr = matter.optJSONArray("members") ?: JSONArray()
                    for (i in 0 until membersArr.length()) {
                        val obj = membersArr.optJSONObject(i) ?: continue
                        val member = TeamModel()
                        member.tm_id = obj.optString("id")
                        member.tm_name = obj.optString("name")
                        membersList.add(member)
                    }
                    val ownerId = Constants.owner_id ?: ""
                    val ownerName = Constants.owner_name ?: ""
                    if (ownerId.isNotEmpty() && membersList.none { it.tm_id == ownerId }) {
                        val owner = TeamModel().apply {
                            this.tm_id = ownerId
                            this.tm_name = ownerName
                        }
                        membersList.add(0, owner)
                    }

                    // Parse clients
                    val clientsList = mutableListOf<ClientsModel>()
                    val clientsArr = matter.optJSONArray("clients") ?: JSONArray()
                    for (i in 0 until clientsArr.length()) {
                        val obj = clientsArr.optJSONObject(i) ?: continue
                        val client = ClientsModel()
                        client.client_id = obj.optString("id")
                        client.client_name = obj.optString("name")
                        client.client_type = obj.optString("type", "consumer")
                        clientsList.add(client)
                    }
                    val corpArr = matter.optJSONArray("corporate") ?: JSONArray()
                    for (i in 0 until corpArr.length()) {
                        val obj = corpArr.optJSONObject(i) ?: continue
                        val client = ClientsModel()
                        client.client_id = obj.optString("id")
                        client.client_name = obj.optString("name")
                        client.client_type = "corporate"
                        clientsList.add(client)
                    }

                    // Parse existing documents with full metadata from Chosen_Documents endpoint
                    val type = _uiState.value.matterType.lowercase(Locale.ROOT).ifEmpty { "legal" }
                    val docsList = mutableListOf<com.digicoffer.lauditor.Documents.Models.DocumentsModel>()
                    val docsHttpResult = getMatterDocumentsApi(type, matterId)
                    var docsArr: JSONArray? = null
                    if (docsHttpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                        try {
                            val docsJson = JSONObject(docsHttpResult.responseContent ?: "{}")
                            if (!docsJson.optBoolean("error", false)) {
                                docsArr = docsJson.optJSONArray("documents")
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    if (docsArr == null) {
                        docsArr = matter.optJSONArray("documents") ?: JSONArray()
                    }

                    for (i in 0 until docsArr.length()) {
                        val jsonObject = docsArr.optJSONObject(i) ?: continue
                        val doc = com.digicoffer.lauditor.Documents.Models.DocumentsModel()
                        doc.docid = jsonObject.optString("docid")
                        doc.name = jsonObject.optString("name")
                        doc.description = jsonObject.optString("description")
                        doc.user_id = jsonObject.optString("user_id")
                        doc.doctype = jsonObject.optString("doctype")
                        doc.tags_list = jsonObject.optJSONObject("tags") ?: JSONObject()
                        doc.contentType = jsonObject.optString("contentType")
                        doc.filename = jsonObject.optString("filename")
                        doc.viewUrl = jsonObject.optString("viewUrl")
                        doc.isIs_encrypted = jsonObject.optBoolean("is_encrypted")
                        doc.isIs_password = jsonObject.optBoolean("is_password")
                        doc.isAdded_encryption = jsonObject.optBoolean("added_encryption")
                        docsList.add(doc)
                    }

                    // Parse opponent advocates
                    val advocatesList = mutableListOf<AdvocateModel>()
                    val advArr = matter.optJSONArray("opponentAdvocates") ?: JSONArray()
                    for (i in 0 until advArr.length()) {
                        val advObj = advArr.optJSONObject(i) ?: continue
                        val adv = AdvocateModel()
                        adv.advocate_name = advObj.optString("name")
                        adv.email = advObj.optString("email")
                        adv.number = advObj.optString("phone")
                        advocatesList.add(adv)
                    }

                    // Parse tags list
                    val tagsList = mutableListOf<String>()
                    val tagsObj = matter.optJSONObject("tags")
                    if (tagsObj != null) {
                        val keys = tagsObj.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            tagsList.add(tagsObj.optString(k))
                        }
                    }

                    _uiState.update {
                        it.copy(
                            title = matter.optString("title", it.title),
                            caseNumber = matter.optString("caseNumber", it.caseNumber),
                            matterNumber = matter.optString("matterNumber", it.matterNumber),
                            description = matter.optString("description", it.description),
                            selectedCaseType = matter.optString("caseType", it.selectedCaseType),
                            dateOfFiling = AndroidUtils.formatToMMMddYYYY(matter.optString("date_of_filling", it.dateOfFiling)),
                            startDate = AndroidUtils.formatToMMMddYYYY(matter.optString("startdate", it.startDate)),
                            endDate = AndroidUtils.formatToMMMddYYYY(matter.optString("closedate", it.endDate)),
                            courtName = matter.optString("courtName", it.courtName),
                            judges = matter.optString("judges", it.judges),
                            priority = matter.optString("priority", it.priority),
                            status = matter.optString("status", it.status),
                            tagsList = if (tagsList.isNotEmpty()) tagsList else it.tagsList,
                            advocatesList = if (advocatesList.isNotEmpty()) advocatesList else it.advocatesList,
                            selectedClients = if (clientsList.isNotEmpty()) clientsList else it.selectedClients,
                            selectedTeamMembers = membersList,
                            selectedExistingDocuments = docsList
                        )
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MatterEditViewModel", "Error fetching matter details", e)
        } finally {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun getMatterDocumentsApi(matterType: String, matterId: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
        val type = matterType.lowercase(Locale.ROOT)
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            getApplication(),
            WebServiceHelper.RestMethodType.GET,
            "matter/$type/$matterId/documents",
            "Chosen_Documents",
            JSONObject().toString()
        )
    }

    private suspend fun getMatterDetailsApi(matterId: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
        val type = _uiState.value.matterType.lowercase(Locale.ROOT)
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            getApplication(),
            WebServiceHelper.RestMethodType.GET,
            "v2/matter/$type/$matterId",
            "Edit Matter",
            ""
        )
    }

    fun initializeFromLegacy(mm: MatterModel) {
        val type = Constants.MATTER_TYPE ?: "Legal"
        
        // Parse tags list
        val parsedTags = mutableListOf<String>()
        val tagsArray = mm.tags_list
        if (tagsArray != null && tagsArray.length() > 0) {
            for (i in 0 until tagsArray.length()) {
                parsedTags.add(tagsArray.optString(i))
            }
        }

        // Parse opponent advocates
        val parsedAdvocates = mutableListOf<AdvocateModel>()
        val advocatesArray = mm.opponent_advocate ?: JSONArray()
        for (i in 0 until advocatesArray.length()) {
            val advJson = advocatesArray.optJSONObject(i) ?: continue
            val adv = AdvocateModel()
            adv.advocate_name = advJson.optString("name")
            adv.email = advJson.optString("email")
            adv.number = advJson.optString("phone")
            parsedAdvocates.add(adv)
        }

        // Parse Clients
        val selectedClients = mutableListOf<ClientsModel>()
        val clientsArr = mm.clients ?: JSONArray()
        for (i in 0 until clientsArr.length()) {
            val obj = clientsArr.optJSONObject(i) ?: continue
            val client = ClientsModel()
            client.client_id = obj.optString("id")
            client.client_name = obj.optString("name")
            client.client_type = obj.optString("type", "consumer")
            selectedClients.add(client)
        }

        // Parse Corporate Clients
        val corpArr = (mm as? ViewMatterModel)?.corporate ?: JSONArray()
        for (i in 0 until corpArr.length()) {
            val obj = corpArr.optJSONObject(i) ?: continue
            val client = ClientsModel()
            client.client_id = obj.optString("id")
            client.client_name = obj.optString("name")
            client.client_type = "corporate"
            selectedClients.add(client)
        }

        // Parse Team Members
        val selectedMembers = mutableListOf<TeamModel>()
        val membersArr = mm.members ?: JSONArray()
        for (i in 0 until membersArr.length()) {
            val obj = membersArr.optJSONObject(i) ?: continue
            val member = TeamModel()
            member.tm_id = obj.optString("id")
            member.tm_name = obj.optString("name")
            selectedMembers.add(member)
        }

        // Ensure owner is present
        val ownerId = Constants.owner_id ?: ""
        val ownerName = Constants.owner_name ?: ""
        if (ownerId.isNotEmpty() && selectedMembers.none { it.tm_id == ownerId }) {
            val owner = TeamModel()
            owner.tm_id = ownerId
            owner.tm_name = ownerName
            selectedMembers.add(0, owner)
        }

        // Parse Existing Documents
        val selectedDocs = mutableListOf<com.digicoffer.lauditor.Documents.Models.DocumentsModel>()
        val docsArr = mm.documents ?: JSONArray()
        for (i in 0 until docsArr.length()) {
            val jsonObject = docsArr.optJSONObject(i) ?: continue
            val doc = com.digicoffer.lauditor.Documents.Models.DocumentsModel()
            doc.docid = jsonObject.optString("docid")
            doc.name = jsonObject.optString("name")
            doc.user_id = jsonObject.optString("user_id")
            doc.doctype = jsonObject.optString("doctype")
            doc.tags_list = jsonObject.optJSONObject("tags") ?: JSONObject()
            doc.contentType = jsonObject.optString("contentType")
            doc.viewUrl = jsonObject.optString("viewUrl")
            doc.isIs_encrypted = jsonObject.optBoolean("is_encrypted")
            doc.isIs_password = jsonObject.optBoolean("is_password")
            doc.isAdded_encryption = jsonObject.optBoolean("added_encryption")
            selectedDocs.add(doc)
        }

        _uiState.update {
            it.copy(
                matterType = type,
                title = mm.matter_title ?: "",
                caseNumber = mm.case_number ?: "",
                matterNumber = mm.matter_id ?: "",
                createdDate = mm.created_date ?: "",
                selectedCaseType = mm.case_type ?: "",
                description = mm.description ?: "",
                tagsList = parsedTags,
                dateOfFiling = mm.date_of_filing ?: "",
                startDate = mm.start_date ?: "",
                endDate = mm.end_date ?: "",
                courtName = mm.court ?: "",
                judges = mm.judge ?: "",
                priority = mm.case_priority ?: "High",
                status = mm.status ?: "Active",
                advocatesList = parsedAdvocates,
                selectedClients = selectedClients,
                selectedTeamMembers = selectedMembers,
                selectedExistingDocuments = selectedDocs
            )
        }
    }

    private fun populateFromEditModel(vm: ViewMatterModel) {
        val type = Constants.MATTER_TYPE ?: "Legal"
        val caseNum = if (type == "Legal") vm.caseNumber ?: "" else vm.matterNumber ?: ""
        val selectedType = if (type == "Legal") vm.casetype ?: "" else vm.matterType ?: ""
        
        // Parse tags list from JSONArray
        val parsedTags = mutableListOf<String>()
        val tagsArray = vm.tags_list
        if (tagsArray != null && tagsArray.length() > 0) {
            for (i in 0 until tagsArray.length()) {
                parsedTags.add(tagsArray.optString(i))
            }
        }

        // Parse opponent advocates
        val parsedAdvocates = mutableListOf<AdvocateModel>()
        val advocatesArray = vm.opponentAdvocates ?: JSONArray()
        for (i in 0 until advocatesArray.length()) {
            val advJson = advocatesArray.optJSONObject(i) ?: continue
            val adv = AdvocateModel()
            adv.advocate_name = advJson.optString("name")
            adv.email = advJson.optString("email")
            adv.number = advJson.optString("phone")
            parsedAdvocates.add(adv)
        }

        // Parse Clients
        val selectedClients = mutableListOf<ClientsModel>()
        val clientsArr = vm.clients ?: JSONArray()
        for (i in 0 until clientsArr.length()) {
            val obj = clientsArr.optJSONObject(i) ?: continue
            val client = ClientsModel()
            client.client_id = obj.optString("id")
            client.client_name = obj.optString("name")
            client.client_type = obj.optString("type", "consumer")
            selectedClients.add(client)
        }

        // Parse Corporate Clients
        val corpArr = vm.corporate ?: JSONArray()
        for (i in 0 until corpArr.length()) {
            val obj = corpArr.optJSONObject(i) ?: continue
            val client = ClientsModel()
            client.client_id = obj.optString("id")
            client.client_name = obj.optString("name")
            client.client_type = "corporate"
            selectedClients.add(client)
        }

        // Parse Team Members
        val selectedMembers = mutableListOf<TeamModel>()
        val membersArr = vm.members ?: JSONArray()
        for (i in 0 until membersArr.length()) {
            val obj = membersArr.optJSONObject(i) ?: continue
            val member = TeamModel()
            member.tm_id = obj.optString("id")
            member.tm_name = obj.optString("name")
            selectedMembers.add(member)
        }

        // Ensure owner is present
        val ownerId = Constants.owner_id ?: ""
        val ownerName = Constants.owner_name ?: ""
        if (ownerId.isNotEmpty() && selectedMembers.none { it.tm_id == ownerId }) {
            val owner = TeamModel()
            owner.tm_id = ownerId
            owner.tm_name = ownerName
            selectedMembers.add(0, owner)
        }

        // Parse Existing Documents
        val selectedDocs = mutableListOf<com.digicoffer.lauditor.Documents.Models.DocumentsModel>()
        val docsArr = vm.documents ?: JSONArray()
        for (i in 0 until docsArr.length()) {
            val jsonObject = docsArr.optJSONObject(i) ?: continue
            val doc = com.digicoffer.lauditor.Documents.Models.DocumentsModel()
            doc.docid = jsonObject.optString("docid")
            doc.name = jsonObject.optString("name")
            doc.user_id = jsonObject.optString("user_id")
            doc.doctype = jsonObject.optString("doctype")
            doc.tags_list = jsonObject.optJSONObject("tags") ?: JSONObject()
            doc.contentType = jsonObject.optString("contentType")
            doc.viewUrl = jsonObject.optString("viewUrl")
            doc.isIs_encrypted = jsonObject.optBoolean("is_encrypted")
            doc.isIs_password = jsonObject.optBoolean("is_password")
            doc.isAdded_encryption = jsonObject.optBoolean("added_encryption")
            selectedDocs.add(doc)
        }

        _uiState.update {
            it.copy(
                title = vm.title ?: "",
                caseNumber = caseNum,
                matterNumber = vm.matter_id ?: "",
                createdDate = vm.created_date ?: vm.created ?: "",
                selectedCaseType = selectedType,
                description = vm.description ?: "",
                tagsList = parsedTags,
                dateOfFiling = vm.date_of_filling ?: "",
                startDate = vm.startdate ?: "",
                endDate = vm.closedate ?: "",
                courtName = vm.courtName ?: "",
                judges = vm.judges ?: "",
                priority = vm.priority ?: "High",
                status = vm.status ?: "Active",
                advocatesList = parsedAdvocates,
                selectedClients = selectedClients,
                selectedTeamMembers = selectedMembers,
                selectedExistingDocuments = selectedDocs
            )
        }
    }

    fun onTitleChanged(value: String) {
        _uiState.update { it.copy(title = value) }
    }

    fun onCaseNumberChanged(value: String) {
        _uiState.update { it.copy(caseNumber = value) }
    }

    fun onCaseTypeChanged(value: String) {
        _uiState.update { it.copy(selectedCaseType = value) }
    }

    fun onDescriptionChanged(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    fun onTagInputChanged(value: String) {
        _uiState.update { it.copy(tagInput = value) }
    }

    fun onAddTagClicked() {
        val tag = _uiState.value.tagInput.trim()
        if (tag.isEmpty()) {
            AndroidUtils.showToast("Enter a tag", getApplication())
            return
        }
        if (tag.length > 30) {
            AndroidUtils.showToast("Tag cannot exceed 30 characters", getApplication())
            return
        }
        if (_uiState.value.tagsList.contains(tag)) {
            AndroidUtils.showToast("Tag name already exists", getApplication())
            return
        }
        _uiState.update {
            it.copy(
                tagsList = it.tagsList + tag,
                tagInput = ""
            )
        }
    }

    fun onRemoveTagClicked(index: Int) {
        val list = _uiState.value.tagsList.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            _uiState.update { it.copy(tagsList = list) }
        }
    }

    fun onEditTagClicked(index: Int) {
        val list = _uiState.value.tagsList.toMutableList()
        if (index in list.indices) {
            val tagToEdit = list.removeAt(index)
            _uiState.update {
                it.copy(
                    tagInput = tagToEdit,
                    tagsList = list
                )
            }
        }
    }

    fun onAdditionalDetailsToggled() {
        _uiState.update { it.copy(isAdditionalDetailsExpanded = !it.isAdditionalDetailsExpanded) }
    }

    fun onCaseTypeSelected(value: String) {
        _uiState.update { it.copy(selectedCaseType = value) }
    }

    fun onDateSelected(field: String, value: String) {
        when (field) {
            "Created Date" -> _uiState.update { it.copy(createdDate = value) }
            "Date of Filing" -> _uiState.update { it.copy(dateOfFiling = value) }
            "Start Date" -> _uiState.update { it.copy(startDate = value) }
            "Close Date" -> _uiState.update { it.copy(endDate = value) }
        }
    }

    fun onCourtChanged(value: String) {
        _uiState.update { it.copy(courtName = value) }
    }

    fun onJudgesChanged(value: String) {
        _uiState.update { it.copy(judges = value) }
    }

    fun onPrioritySelected(value: String) {
        _uiState.update { it.copy(priority = value) }
    }

    fun onStatusSelected(value: String) {
        _uiState.update { it.copy(status = value) }
    }

    fun onAddAdvocateClicked() {
        _uiState.update {
            it.copy(
                isAdvocateFormVisible = true,
                advocateName = "",
                advocateEmail = "",
                advocatePhone = ""
            )
        }
    }

    fun onAdvocateFormChanged(name: String, email: String, phone: String) {
        val emailTrimmed = email.trim()
        val emailErr = if (emailTrimmed.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(emailTrimmed).matches()) {
            "Please enter a valid email address"
        } else {
            null
        }

        val phoneTrimmed = phone.trim()
        val phoneErr = if (phoneTrimmed.isNotEmpty() && phoneTrimmed.length < 10) {
            "Please enter a 10 digit valid mobile number"
        } else {
            null
        }

        _uiState.update {
            it.copy(
                advocateName = name,
                advocateEmail = email,
                advocatePhone = phone,
                advocateEmailError = emailErr,
                advocatePhoneError = phoneErr
            )
        }
    }

    fun onSaveAdvocateClicked() {
        val name = _uiState.value.advocateName.trim()
        val email = _uiState.value.advocateEmail.trim()
        val phone = _uiState.value.advocatePhone.trim()

        if (name.isEmpty() && email.isEmpty() && phone.isEmpty()) {
            _uiState.update {
                it.copy(
                    alertTitle = "Alert",
                    alertMessage = "Please enter at least one detail (Name, Email or Phone)."
                )
            }
            return
        }

        var emailErr: String? = null
        var phoneErr: String? = null

        if (email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailErr = "Please enter a valid email address"
        }
        if (phone.isNotEmpty() && phone.length < 10) {
            phoneErr = "Please enter a 10 digit valid mobile number"
        }

        if (emailErr != null || phoneErr != null) {
            _uiState.update {
                it.copy(
                    advocateEmailError = emailErr,
                    advocatePhoneError = phoneErr
                )
            }
            return
        }

        val advocate = AdvocateModel()
        advocate.advocate_name = name
        advocate.email = email
        advocate.number = phone

        _uiState.update {
            it.copy(
                advocatesList = it.advocatesList + advocate,
                isAdvocateFormVisible = false,
                advocateName = "",
                advocateEmail = "",
                advocatePhone = "",
                advocateEmailError = null,
                advocatePhoneError = null
            )
        }
    }

    fun onEditAdvocateClicked(index: Int) {
        val list = _uiState.value.advocatesList.toMutableList()
        if (index in list.indices) {
            val adv = list.removeAt(index)
            _uiState.update {
                it.copy(
                    advocatesList = list,
                    isAdvocateFormVisible = true,
                    advocateName = adv.advocate_name ?: "",
                    advocateEmail = adv.email ?: "",
                    advocatePhone = adv.number ?: "",
                    advocateEmailError = null,
                    advocatePhoneError = null
                )
            }
        }
    }

    fun onCancelAdvocateClicked() {
        _uiState.update {
            it.copy(
                isAdvocateFormVisible = false,
                advocateName = "",
                advocateEmail = "",
                advocatePhone = "",
                advocateEmailError = null,
                advocatePhoneError = null
            )
        }
    }

    fun onRemoveAdvocateClicked(index: Int) {
        val list = _uiState.value.advocatesList.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            _uiState.update { it.copy(advocatesList = list) }
        }
    }

    fun submitForm(isSaveLater: Boolean, editModel: ViewMatterModel?) {
        viewModelScope.launch {
            try {
                val title = _uiState.value.title.trim()
                if (title.isEmpty()) {
                    val msg = if (Constants.create_matter) "Please Check the Title" else "Please enter the title"
                    _uiState.update { it.copy(errorMessage = msg) }
                    return@launch
                }
                if (_uiState.value.description.length > 300) {
                    _uiState.update { it.copy(errorMessage = "Please check the description field size..") }
                    return@launch
                }

                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                // 1. Check title uniqueness if in Create mode or title changed in Edit mode
                val isTitleChanged = Constants.create_matter || (editModel != null && editModel.title != title)
                if (isTitleChanged) {
                    val uniqueResult = checkTitleUnique(title, _uiState.value.matterType)
                    if (uniqueResult.result != WebServiceHelper.ServiceCallStatus.Success) {
                        _uiState.update { it.copy(isLoading = false, errorMessage = "Connection failed, try again") }
                        return@launch
                    }

                    val uniqueJson = JSONObject(uniqueResult.responseContent ?: "")
                    if (uniqueJson.optBoolean("error", false)) {
                        val msg = uniqueJson.optString("msg")
                        _uiState.update { it.copy(isLoading = false, errorMessage = msg) }
                        return@launch
                    }
                }

                // 2. Perform Save (Create or Update)
                val isInitialCreate = Constants.create_matter && _uiState.value.createdMatterId.isNullOrEmpty()
                val saveResult = if (isInitialCreate) {
                    executeCreateMatter()
                } else {
                    executeUpdateMatter(editModel)
                }

                _uiState.update { it.copy(isLoading = false) }

                if (saveResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                    val saveJson = JSONObject(saveResult.responseContent ?: "")
                    if (!saveJson.optBoolean("error", false)) {
                        val msg = saveJson.optString("msg")
                        
                        var createdId = _uiState.value.createdMatterId
                        if (isInitialCreate) {
                            val newId = saveJson.optString("matter_id").ifEmpty {
                                saveJson.optJSONObject("data")?.optString("id") ?: ""
                            }
                            if (newId.isNotEmpty()) {
                                createdId = newId
                                Constants.Matter_id = newId
                            }
                        }

                        // Save data back to legacy coordinator memory
                        updateLegacyModel(editModel)

                        Constants.GeneratedMatterId = _uiState.value.matterNumber
                        Constants.GeneratedMatterTitle = _uiState.value.title

                        if (msg.isNotEmpty()) {
                            AndroidUtils.showToast(msg, getApplication())
                        }

                        if (isSaveLater) {
                            _uiState.update {
                                it.copy(
                                    createdMatterId = createdId,
                                    isMatterCreated = true,
                                    navigateToView = true,
                                    toastMessage = msg
                                )
                            }
                        } else {
                            _uiState.update {
                                it.copy(
                                    createdMatterId = createdId,
                                    isMatterCreated = true,
                                    navigateToNext = true,
                                    toastMessage = msg
                                )
                            }
                        }
                    } else {
                        val msg = saveJson.optString("msg")
                        _uiState.update { it.copy(errorMessage = msg, alertTitle = "Error", alertMessage = msg) }
                    }
                } else {
                    _uiState.update { it.copy(errorMessage = "Save operation failed, try again") }
                }
            } catch (e: Exception) {
                android.util.Log.e("MatterEditViewModel", "Exception in submitForm", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "An error occurred: ${e.message}") }
            }
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun dismissAlert() {
        _uiState.update { it.copy(alertTitle = null, alertMessage = null) }
    }

    private fun updateLegacyModel(editModel: ViewMatterModel?) {
        legacyModel.matter_title = _uiState.value.title
        legacyModel.case_number = _uiState.value.caseNumber
        legacyModel.case_type = _uiState.value.selectedCaseType
        legacyModel.description = _uiState.value.description
        legacyModel.date_of_filing = _uiState.value.dateOfFiling
        legacyModel.created_date = _uiState.value.createdDate
        legacyModel.start_date = _uiState.value.startDate
        legacyModel.end_date = _uiState.value.endDate
        legacyModel.court = _uiState.value.courtName
        legacyModel.judge = _uiState.value.judges
        legacyModel.case_priority = _uiState.value.priority
        legacyModel.status = _uiState.value.status
        legacyModel.matter_id = _uiState.value.matterNumber

        // Advocates list to JSONArray
        val advArr = JSONArray()
        for (adv in _uiState.value.advocatesList) {
            val obj = JSONObject()
            obj.put("name", adv.advocate_name)
            obj.put("email", adv.email)
            obj.put("phone", adv.number)
            advArr.put(obj)
        }
        legacyModel.opponent_advocate = advArr

        // Tags to JSONArray
        val tagsArr = JSONArray()
        for (tag in _uiState.value.tagsList) {
            tagsArr.put(tag)
        }
        legacyModel.tags_list = tagsArr

        // Retain legacy clients, documents, groups list metadata
        if (editModel != null) {
            legacyModel.clients = editModel.clients ?: JSONArray()
            legacyModel.group_acls = editModel.groupAcls ?: JSONArray()
            legacyModel.members = editModel.members ?: JSONArray()
            legacyModel.documents = editModel.documents ?: JSONArray()
            
            val clientIds = JSONArray()
            val clientsArray = editModel.clients ?: JSONArray()
            for (i in 0 until clientsArray.length()) {
                val clientObject = clientsArray.optJSONObject(i)
                if (clientObject != null) {
                    val clientId = clientObject.optString("id", "")
                    clientIds.put(clientId)
                }
            }
            legacyModel.clients_list = clientIds
        }

        // Update the static arrays inside parent coordinator Matter fragment
        val infoEn = Constants.matterInformation_en
        if (infoEn != null) {
            val list = infoEn.matterArraylist
            if (list != null) {
                if (list.isEmpty()) {
                    list.add(legacyModel)
                } else {
                    list[0] = legacyModel
                }
            }
        }
    }

    private suspend fun fetchGeneratedMatterId() {
        val result = getGeneratedMatterId()
        if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
            val json = JSONObject(result.responseContent ?: "")
            if (!json.optBoolean("error", false)) {
                val id = json.optString("matter_id")
                Constants.GeneratedMatterId = id
                _uiState.update { it.copy(matterNumber = id) }
            }
        }
    }

    private suspend fun fetchCaseTypes() {
        val result = getCaseTypes()
        if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
            val json = JSONObject(result.responseContent ?: "")
            if (!json.optBoolean("error", false)) {
                val arr = json.optJSONArray("data")
                    ?: json.optJSONArray("casetypes")
                    ?: json.optJSONArray("case_types")
                    ?: JSONArray()
                val list = mutableListOf<String>()
                for (i in 0 until arr.length()) {
                    val item = arr.optString(i)
                    if (item.isNotEmpty()) {
                        list.add(item)
                    }
                }
                _uiState.update {
                    it.copy(
                        caseTypeList = list,
                        selectedCaseType = it.selectedCaseType
                    )
                }
            }
        }
    }

    private suspend fun getGeneratedMatterId(): HttpResultDo = suspendCancellableCoroutine { continuation ->
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            getApplication(),
            WebServiceHelper.RestMethodType.GET,
            "v2/generate/matterid",
            "Matter Id",
            JSONObject().toString()
        )
    }

    private suspend fun getCaseTypes(): HttpResultDo = suspendCancellableCoroutine { continuation ->
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            getApplication(),
            WebServiceHelper.RestMethodType.GET,
            "v2/matter/casetypes",
            "Get Case Type",
            JSONObject().toString()
        )
    }

    private suspend fun checkTitleUnique(title: String, type: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
        val json = JSONObject()
        json.put("title", title)
        json.put("type", type.lowercase(Locale.ROOT))
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            getApplication(),
            WebServiceHelper.RestMethodType.POST,
            "matter/check/unique",
            "Unique",
            json.toString()
        )
    }

    private suspend fun executeCreateMatter(): HttpResultDo = suspendCancellableCoroutine { continuation ->
        val json = buildPayloadForSave()
        val type = _uiState.value.matterType.lowercase(Locale.ROOT)
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            getApplication(),
            WebServiceHelper.RestMethodType.POST,
            "v2/matter/$type",
            "Create Matter",
            json.toString()
        )
    }

    private suspend fun executeUpdateMatter(editModel: ViewMatterModel?): HttpResultDo = suspendCancellableCoroutine { continuation ->
        val json = buildPayloadForSave()
        val type = _uiState.value.matterType.lowercase(Locale.ROOT)
        val id = editModel?.id ?: _uiState.value.createdMatterId ?: Constants.Matter_id ?: ""
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            getApplication(),
            WebServiceHelper.RestMethodType.PATCH,
            "v2/matter/$type/$id",
            "Update Matter",
            json.toString()
        )
    }

    private fun buildPayloadForSave(): JSONObject {
        val isLegal = _uiState.value.matterType == "Legal"
        val json = JSONObject()

        val matterId = _uiState.value.matterNumber.ifEmpty { Constants.GeneratedMatterId ?: "" }
        json.put("matter_id", matterId)
        json.put("title", _uiState.value.title.trim())
        
        val createdFormatted = if (_uiState.value.createdDate.isNotEmpty()) {
            AndroidUtils.convertAnyDateToDDMMYYYY(_uiState.value.createdDate)
        } else {
            ""
        }
        json.put("created_date", createdFormatted)
        json.put("description", _uiState.value.description.trim())
        json.put("priority", _uiState.value.priority)
        json.put("status", _uiState.value.status)
        json.put("affidavit_filing_date", "")
        json.put("affidavit_isfiled", "")

        // Tags map payload (format: {"0": "tag1", "1": "tag2"})
        val tagsObject = JSONObject()
        val tagsList = _uiState.value.tagsList
        for (i in tagsList.indices) {
            tagsObject.put(i.toString(), tagsList[i])
        }
        json.put("tags", tagsObject)

        if (isLegal) {
            json.put("case_number", _uiState.value.caseNumber.trim())
            json.put("case_type", _uiState.value.selectedCaseType)
            json.put("court_name", _uiState.value.courtName)
            json.put("judges", _uiState.value.judges)

            val advocatesJsonArray = JSONArray()
            for (adv in _uiState.value.advocatesList) {
                val advJson = JSONObject()
                advJson.put("name", adv.advocate_name)
                advJson.put("email", adv.email)
                advJson.put("phone", adv.number)
                advocatesJsonArray.put(advJson)
            }
            json.put("opponent_advocates", advocatesJsonArray)

            val filingFormatted = if (_uiState.value.dateOfFiling.isNotEmpty()) {
                AndroidUtils.convertAnyDateToDDMMYYYY(_uiState.value.dateOfFiling)
            } else {
                ""
            }
            json.put("date_of_filling", filingFormatted)
        } else {
            json.put("matter_number", _uiState.value.caseNumber.trim())
            json.put("matter_type", _uiState.value.selectedCaseType)

            val startFormatted = if (_uiState.value.startDate.isNotEmpty()) {
                AndroidUtils.convertAnyDateToDDMMYYYY(_uiState.value.startDate)
            } else {
                ""
            }
            json.put("startdate", startFormatted)

            val closeFormatted = if (_uiState.value.endDate.isNotEmpty()) {
                AndroidUtils.convertAnyDateToDDMMYYYY(_uiState.value.endDate)
            } else {
                ""
            }
            json.put("closedate", closeFormatted)
        }

        return json
    }

    fun onSearchQueryChanged(value: String) {
        _uiState.update { it.copy(searchQuery = value) }
    }

    fun searchClients() {
        val query = _uiState.value.searchQuery.trim()
        if (query.isEmpty()) {
            AndroidUtils.showToast("Please enter an Entity to Search", getApplication())
            return
        }

        _uiState.update { it.copy(isSearchingClient = true, searchResults = emptyList()) }
        
        viewModelScope.launch {
            try {
                val result = searchEntityApi(query)
                if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                    val json = JSONObject(result.responseContent ?: "")
                    val clientsJson = json.optJSONArray("clients") ?: JSONArray()
                    val list = mutableListOf<ClientsModel>()
                    for (i in 0 until clientsJson.length()) {
                        val obj = clientsJson.optJSONObject(i) ?: continue
                        val model = ClientsModel()
                        model.client_id = obj.optString("client_id")
                        model.client_name = obj.optString("client_name")
                        model.client_type = obj.optString("type")
                        list.add(model)
                    }
                    if (list.isEmpty()) {
                        AndroidUtils.showToast("No clients found matching '$query'", getApplication())
                    }
                    _uiState.update { it.copy(searchResults = list, isSearchingClient = false) }
                } else {
                    _uiState.update { it.copy(isSearchingClient = false) }
                    AndroidUtils.showToast("Search failed, try again", getApplication())
                }
            } catch (e: Exception) {
                android.util.Log.e("MatterEditViewModel", "Error in searchClients", e)
                _uiState.update { it.copy(isSearchingClient = false) }
                AndroidUtils.showToast("Error searching: ${e.message}", getApplication())
            }
        }
    }

    fun loadTeamMembers() {
        if (_uiState.value.teamMembersList.isNotEmpty()) return
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val result = getMembersApi()
                if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                    val json = JSONObject(result.responseContent ?: "")
                    val data = json.optJSONObject("data") ?: JSONObject()
                    val usersJson = data.optJSONArray("users") ?: JSONArray()
                    val list = mutableListOf<TeamModel>()
                    for (i in 0 until usersJson.length()) {
                        val obj = usersJson.optJSONObject(i) ?: continue
                        val model = TeamModel()
                        model.tm_id = obj.optString("id")
                        model.tm_name = obj.optString("name")
                        list.add(model)
                    }
                    _uiState.update { it.copy(teamMembersList = list, isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                    AndroidUtils.showToast("Failed to load team members", getApplication())
                }
            } catch (e: Exception) {
                android.util.Log.e("MatterEditViewModel", "Error in loadTeamMembers", e)
                _uiState.update { it.copy(isLoading = false) }
                AndroidUtils.showToast("Error loading members: ${e.message}", getApplication())
            }
        }
    }

    fun addClient(client: ClientsModel) {
        val currentSelected = _uiState.value.selectedClients.toMutableList()
        if (currentSelected.none { it.client_id == client.client_id }) {
            currentSelected.add(client)
            _uiState.update {
                it.copy(
                    selectedClients = currentSelected,
                    searchQuery = "",
                    searchResults = emptyList()
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    searchQuery = "",
                    searchResults = emptyList()
                )
            }
        }
    }

    fun removeClient(client: ClientsModel) {
        val currentSelected = _uiState.value.selectedClients.toMutableList()
        currentSelected.removeAll { it.client_id == client.client_id }
        _uiState.update { it.copy(selectedClients = currentSelected) }
    }

    fun addTeamMember(member: TeamModel) {
        val currentSelected = _uiState.value.selectedTeamMembers.toMutableList()
        if (currentSelected.none { it.tm_id == member.tm_id }) {
            currentSelected.add(member)
            _uiState.update { it.copy(selectedTeamMembers = currentSelected, isTeamMembersDropdownExpanded = false) }
        } else {
            _uiState.update { it.copy(isTeamMembersDropdownExpanded = false) }
        }
    }

    fun removeTeamMember(member: TeamModel) {
        if (!isMemberRemovable(member)) {
            AndroidUtils.showToast("This member cannot be removed", getApplication())
            return
        }
        val currentSelected = _uiState.value.selectedTeamMembers.toMutableList()
        currentSelected.removeAll { it.tm_id == member.tm_id }
        _uiState.update { it.copy(selectedTeamMembers = currentSelected) }
    }

    fun toggleTeamMembersDropdown() {
        _uiState.update { it.copy(isTeamMembersDropdownExpanded = !it.isTeamMembersDropdownExpanded) }
    }

    fun setTeamMembersDropdownExpanded(expanded: Boolean) {
        _uiState.update { it.copy(isTeamMembersDropdownExpanded = expanded) }
    }

    fun isMemberRemovable(member: TeamModel): Boolean {
        if (Constants.create_matter) {
            return member.tm_id != Constants.owner_id
        } else {
            val isOwner = member.tm_id == Constants.owner_id
            val isCurrentUser = member.tm_id == Constants.USER_ID
            return !(isOwner || isCurrentUser)
        }
    }

    fun saveClientsAndMembers(isSaveLater: Boolean, onNavigate: () -> Unit) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val clients = JSONArray()
                var corpClientId = ""
                for (c in _uiState.value.selectedClients) {
                    if (c.client_type == "corporate") {
                        corpClientId = c.client_id ?: ""
                    } else {
                        val obj = JSONObject()
                        obj.put("id", c.client_id)
                        obj.put("type", c.client_type ?: "consumer")
                        clients.put(obj)
                    }
                }

                val members = JSONArray()
                for (m in _uiState.value.selectedTeamMembers) {
                    val obj = JSONObject()
                    obj.put("id", m.tm_id)
                    members.put(obj)
                }

                val result = saveClientsAndMembersApi(clients, members, corpClientId)
                
                _uiState.update { it.copy(isLoading = false) }
                
                if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                    val json = JSONObject(result.responseContent ?: "")
                    if (!json.optBoolean("error", false)) {
                        val msg = json.optString("msg")
                        
                        val infoEn = Constants.matterInformation_en
                        if (infoEn != null) {
                            val list = infoEn.matterArraylist
                            if (list != null && list.isNotEmpty()) {
                                val mm = list[0] as? ViewMatterModel
                                if (mm != null) {
                                    val clientsArray = JSONArray()
                                    val corporateArray = JSONArray()
                                    for (c in _uiState.value.selectedClients) {
                                        val obj = JSONObject()
                                        obj.put("id", c.client_id)
                                        obj.put("type", c.client_type ?: "consumer")
                                        obj.put("name", c.client_name)
                                        if (c.client_type == "corporate") {
                                            corporateArray.put(obj)
                                        } else {
                                            clientsArray.put(obj)
                                        }
                                    }
                                    mm.clients = clientsArray
                                    mm.corporate = corporateArray
                                    
                                    val membersArray = JSONArray()
                                    for (m in _uiState.value.selectedTeamMembers) {
                                        val obj = JSONObject()
                                        obj.put("id", m.tm_id)
                                        obj.put("name", m.tm_name)
                                        membersArray.put(obj)
                                    }
                                    mm.members = membersArray
                                }
                            }
                        }

                        if (msg.isNotEmpty()) {
                            AndroidUtils.showToast(msg, getApplication())
                            _uiState.update { it.copy(toastMessage = msg) }
                        }

                        onNavigate()
                    } else {
                        val msg = json.optString("msg")
                        _uiState.update { it.copy(errorMessage = msg, alertTitle = "Error", alertMessage = msg) }
                    }
                } else {
                    _uiState.update { it.copy(errorMessage = "Save operation failed, try again") }
                }
            } catch (e: Exception) {
                android.util.Log.e("MatterEditViewModel", "Error in saveClientsAndMembers", e)
                _uiState.update { it.copy(isLoading = false, errorMessage = "Error saving: ${e.message}") }
            }
        }
    }

    private suspend fun searchEntityApi(query: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
        val json = JSONObject()
        json.put("search", query)
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            getApplication(),
            WebServiceHelper.RestMethodType.POST,
            "v3/relationship/search/client",
            "Search Entity",
            json.toString()
        )
    }

    private suspend fun getMembersApi(): HttpResultDo = suspendCancellableCoroutine { continuation ->
        val role = Constants.ROLE ?: ""
        val url = if (role == "GH") "v3/members?module=matter" else "v3/members"
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            getApplication(),
            WebServiceHelper.RestMethodType.GET,
            url,
            "Get Members",
            JSONObject().toString()
        )
    }

    private suspend fun saveClientsAndMembersApi(
        clients: JSONArray,
        members: JSONArray,
        corpClientId: String
    ): HttpResultDo = suspendCancellableCoroutine { continuation ->
        val postdata = JSONObject()
        postdata.put("clients", clients)
        postdata.put("members", members)
        postdata.put("corporate", corpClientId)
        
        val matterType = (Constants.MATTER_TYPE ?: "").lowercase(Locale.ROOT)
        val matterId = (Constants.Matter_id ?: "")
        
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            getApplication(),
            WebServiceHelper.RestMethodType.PATCH,
            "v2/matter/$matterType/$matterId",
            "Update Matter",
            postdata.toString()
        )
    }

    // Step 3 (Documents) Methods
    fun addUploadFile(file: File, name: String = file.name) {
        val baseName = name.substringBeforeLast('.', missingDelimiterValue = name)
        val docModel = com.digicoffer.lauditor.Documents.Models.DocumentsModel().apply {
            this.name = baseName
            this.description = baseName
            this.expiration_date = ""
            this.file = file
            this.isIsenabled = true // downloadDisabled = true (Enable Download: OFF)
            this.isencrypted = false // Enable Encryption: OFF
        }
        val currentList = _uiState.value.selectedUploadFiles.toMutableList()
        currentList.add(docModel)
        _uiState.update { it.copy(selectedUploadFiles = currentList) }
    }

    fun addUploadFile(uri: Uri) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val contentResolver = context.contentResolver
                val cursor = contentResolver.query(uri, null, null, null, null)
                var displayName = "file_${System.currentTimeMillis()}"
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            displayName = it.getString(nameIndex)
                        }
                    }
                }

                // Copy stream to temp cache file
                val tempFile = File(context.cacheDir, displayName)
                contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }

                val baseName = displayName.substringBeforeLast('.', missingDelimiterValue = displayName)
                val docModel = com.digicoffer.lauditor.Documents.Models.DocumentsModel().apply {
                    this.name = baseName
                    this.description = baseName
                    this.expiration_date = ""
                    this.file = tempFile
                    this.isIsenabled = true // downloadDisabled = true (Enable Download: OFF)
                    this.isencrypted = false // Enable Encryption: OFF
                }

                val currentList = _uiState.value.selectedUploadFiles.toMutableList()
                currentList.add(docModel)
                _uiState.update { it.copy(selectedUploadFiles = currentList) }
            } catch (e: Exception) {
                android.util.Log.e("MatterEditViewModel", "Error adding upload file", e)
                AndroidUtils.showToast("Failed to select file: ${e.message}", getApplication())
            }
        }
    }

    fun removeUploadFile(index: Int) {
        val currentList = _uiState.value.selectedUploadFiles.toMutableList()
        if (index in currentList.indices) {
            currentList.removeAt(index)
            _uiState.update { it.copy(selectedUploadFiles = currentList) }
        }
    }

    fun removeExistingDocument(index: Int) {
        val currentList = _uiState.value.selectedExistingDocuments.toMutableList()
        if (index in currentList.indices) {
            currentList.removeAt(index)
            _uiState.update { it.copy(selectedExistingDocuments = currentList) }
        }
    }

    fun setEditMetadataFileIndex(index: Int?) {
        _uiState.update { it.copy(editMetadataFileIndex = index) }
    }

    fun updateUploadFileWithTags(
        index: Int,
        name: String,
        description: String,
        expDate: String,
        isDownloadDisabled: Boolean,
        isEncrypted: Boolean,
        tags: JSONObject?
    ) {
        val currentList = _uiState.value.selectedUploadFiles.toMutableList()
        if (index in currentList.indices) {
            val updated = currentList[index]
            updated.name = name
            updated.description = description
            updated.expiration_date = expDate
            updated.isIsenabled = isDownloadDisabled
            updated.isencrypted = isEncrypted
            updated.tags_list = tags ?: JSONObject()
            currentList[index] = updated
            _uiState.update { it.copy(selectedUploadFiles = currentList) }
        }
    }

    fun viewDocument(doc: com.digicoffer.lauditor.Documents.Models.DocumentsModel) {
        val docFile = doc.file
        if (docFile != null && docFile.exists()) {
            val viewDoc = com.digicoffer.lauditor.Documents.Models.ViewDocumentsModel().apply {
                this.name = doc.name
                this.content_type = doc.content_type ?: ""
            }
            _uiState.update { it.copy(previewDocUrl = "localfile://${docFile.absolutePath}", previewDocModel = viewDoc) }
            return
        }

        val docId = doc.id.ifEmpty { doc.docid }
        if (docId.isNullOrEmpty()) {
            _uiState.update { it.copy(alertTitle = "Alert", alertMessage = "Document ID not available") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val isEncrypted = (doc.isencrypted == true) || doc.is_encrypted || doc.isAdded_encryption || doc.isIs_encrypted

            // 1️⃣ Always call View Document API first (Java/XML parity: "Display Documents" / "View Doc")
            val viewResult = viewDocumentApi(docId)

            if (viewResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val json = JSONObject(viewResult.responseContent ?: "{}")
                    val isError = json.optBoolean("error", false)
                    if (isError) {
                        _uiState.update { it.copy(isLoading = false) }
                        val msg = json.optString("msg").ifEmpty { "Unable to view document" }
                        _uiState.update { it.copy(alertTitle = "Error", alertMessage = msg) }
                        return@launch
                    }

                    val dataObj = json.optJSONObject("data")
                    val rawUrl = dataObj?.optString("url") ?: json.optString("url")
                    val rawContentType = dataObj?.optString("content_type")?.ifEmpty { doc.content_type } ?: doc.content_type ?: ""
                    val rawFilename = dataObj?.optString("filename")?.ifEmpty { doc.name } ?: doc.name ?: ""

                    if (isEncrypted) {
                        // 2️⃣ Encrypted document flow: Call Decrypt Doc API (Java/XML parity: POST v3/decrypt)
                        val decryptResult = decryptDocumentApi(docId)
                        if (decryptResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                            val decryptJson = JSONObject(decryptResult.responseContent ?: "{}")
                            val isDecryptError = decryptJson.optBoolean("error", false)
                            if (!isDecryptError) {
                                val decryptData = decryptJson.optJSONObject("data")
                                val decryptedUrl = decryptData?.optString("url") ?: decryptJson.optString("url")
                                val decryptedFilename = decryptData?.optString("filename") ?: rawFilename

                                if (!decryptedUrl.isNullOrEmpty()) {
                                    val finalDoc = com.digicoffer.lauditor.Documents.Models.ViewDocumentsModel().apply {
                                        this.id = docId
                                        this.name = doc.name
                                        this.filename = decryptedFilename
                                        this.content_type = "application/pdf"
                                    }
                                    _uiState.update {
                                        it.copy(
                                            isLoading = false,
                                            previewDocUrl = decryptedUrl,
                                            previewDocModel = finalDoc
                                        )
                                    }
                                } else {
                                    _uiState.update {
                                        it.copy(
                                            isLoading = false,
                                            alertTitle = "Error",
                                            alertMessage = decryptJson.optString("msg").ifEmpty { "Unable to fetch decrypted link." }
                                        )
                                    }
                                }
                            } else {
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        alertTitle = "Error",
                                        alertMessage = decryptJson.optString("msg").ifEmpty { "Failed to decrypt document." }
                                    )
                                }
                            }
                        } else {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    alertTitle = "Error",
                                    alertMessage = "Failed to decrypt document. Please try again."
                                )
                            }
                        }
                    } else {
                        // 3️⃣ Normal non-encrypted document flow: Image / PDF / Office conversion
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

                            val isFinalPdf = finalContentType.contains("pdf", ignoreCase = true) || finalUrl.contains(".pdf", ignoreCase = true) || finalUrl.startsWith("localfile://")
                            val viewerType = when {
                                isImage -> "IMAGE"
                                isFinalPdf -> "PDF"
                                else -> "PDF"
                            }

                            android.util.Log.d("DOC_VIEW", """
                            DOC_VIEW:
                            docId = $docId
                            displayName = ${doc.name}
                            originalFilename = $rawFilename
                            contentType = $rawContentType
                            encrypted = $isEncrypted
                            view API called = true
                            decrypt API called = $isEncrypted
                            conversion API called = $conversionCalled
                            finalViewerType = $viewerType
                            finalUrl source = $finalUrl
                            """.trimIndent())

                            val finalDoc = com.digicoffer.lauditor.Documents.Models.ViewDocumentsModel().apply {
                                this.id = docId
                                this.name = doc.name
                                this.filename = rawFilename
                                this.content_type = finalContentType
                            }
                            _uiState.update { it.copy(isLoading = false, previewDocUrl = finalUrl, previewDocModel = finalDoc) }
                        } else {
                            _uiState.update { it.copy(isLoading = false) }
                            val msg = json.optString("msg").ifEmpty { "Unable to fetch document preview link." }
                            _uiState.update { it.copy(alertTitle = "Error", alertMessage = msg) }
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(isLoading = false, alertTitle = "Error", alertMessage = e.message ?: "Failed to open document") }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, alertTitle = "Error", alertMessage = "API call failed. Please try again.") }
            }
        }
    }

    private suspend fun convertDocToPdfUrl(rawUrl: String): String? {
        return withContext(Dispatchers.IO) {
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

    fun closePreview() {
        _uiState.update { it.copy(previewDocUrl = null, previewDocModel = null) }
    }

    private suspend fun viewDocumentApi(docId: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            getApplication(),
            WebServiceHelper.RestMethodType.GET,
            "v3/document/$docId/view",
            "Display Documents",
            JSONObject().toString()
        )
    }

    private suspend fun decryptDocumentApi(docId: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
        val payload = JSONObject().apply {
            put("docid", docId)
            put("download", false)
        }
        val url = Constants.decryptUrl ?: "v3/decrypt"
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            getApplication(),
            WebServiceHelper.RestMethodType.POST,
            url,
            "Decrypt Doc",
            payload.toString()
        )
    }

    fun updateUploadFileMetadata(index: Int, name: String, description: String, expDate: String, isDownloadDisabled: Boolean, isEncrypted: Boolean) {
        val currentList = _uiState.value.selectedUploadFiles.toMutableList()
        if (index in currentList.indices) {
            val updated = currentList[index]
            updated.name = name
            updated.description = description
            updated.expiration_date = expDate
            updated.isIsenabled = isDownloadDisabled
            updated.isencrypted = isEncrypted
            currentList[index] = updated
            _uiState.update { it.copy(selectedUploadFiles = currentList) }
        }
    }

    private suspend fun uploadDocumentApi(file: File, payloadString: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
        WebServiceHelper.callHttpUploadWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            getApplication(),
            WebServiceHelper.RestMethodType.POST,
            "v3/document/upload",
            "Upload Document",
            file,
            payloadString
        )
    }

    private suspend fun executeUpdateDocuments(documents: JSONArray): HttpResultDo = suspendCancellableCoroutine { continuation ->
        val payload = JSONObject()
        payload.put("documents", documents)
        val type = _uiState.value.matterType.lowercase(Locale.ROOT)
        val id = Constants.Matter_id ?: ""
        
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            getApplication(),
            WebServiceHelper.RestMethodType.PATCH,
            "v2/matter/$type/$id",
            "matter_update",
            payload.toString()
        )
    }

    fun uploadAndSaveDocuments(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingDocuments = true, errorMessage = null) }
            try {
                val uploadedDocsList = mutableListOf<com.digicoffer.lauditor.Documents.Models.DocumentsModel>()
                val toUpload = _uiState.value.selectedUploadFiles
                
                // Parallel uploads using coroutines
                val jobs = toUpload.map { docModel ->
                    val file = docModel.file ?: return@map null
                    
                    val ext = file.extension.lowercase(Locale.ROOT)
                    val docname = file.name.substringBeforeLast('.', missingDelimiterValue = file.name)
                    val contentType = when (ext) {
                        "jpg", "jpeg" -> "image/jpeg"
                        "png" -> "image/png"
                        "webp" -> "image/webp"
                        "gif" -> "image/gif"
                        "svg" -> "image/svg+xml"
                        "pdf" -> "application/pdf"
                        "xls" -> "application/vnd.ms-excel"
                        "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        "doc" -> "application/msword"
                        "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                        "ppt" -> "application/vnd.ms-powerpoint"
                        "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
                        "txt" -> "text/plain"
                        "csv" -> "text/csv"
                        else -> "application/$ext"
                    }
                    
                    val matters = JSONArray().apply { put(Constants.Matter_id) }
                    val payload = JSONObject().apply {
                        put("name", docModel.name)
                        put("description", docModel.description)
                        put("expiration_date", AndroidUtils.convertAnyDateToDDMMYYYY(docModel.expiration_date))
                        put("filename", file.name)
                        put("matters", matters)
                        put("category", "client")
                        if (Constants.clientList.length() > 0) {
                            put("clients", Constants.clientList)
                        } else {
                            put("clients", Constants.corpclientList)
                        }
                        put("groups", Constants.ex_group_attachment)
                        put("downloadDisabled", docModel.isIsenabled)
                        put("custom_encrypt", docModel.isencrypted ?: false)
                        put("tags", docModel.tags_list ?: (docModel.tags ?: ""))
                        put("content_type", contentType)
                    }
                    
                    viewModelScope.launch {
                        val result = uploadDocumentApi(file, payload.toString())
                        if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
                            val resJson = JSONObject(result.responseContent ?: "")
                            if (!resJson.optBoolean("error", false)) {
                                val docid = resJson.getString("docid")
                                val attachedDoc = com.digicoffer.lauditor.Documents.Models.DocumentsModel().apply {
                                    this.docid = docid
                                    this.name = file.name
                                    this.user_id = Constants.USER_ID ?: ""
                                    this.doctype = "general"
                                    this.description = docModel.description
                                    this.expiration_date = docModel.expiration_date
                                }
                                synchronized(uploadedDocsList) {
                                    uploadedDocsList.add(attachedDoc)
                                }
                            }
                        }
                    }
                }
                
                // Wait for all upload jobs to finish
                jobs.filterNotNull().forEach { it.join() }
                
                // If the user selected files but none uploaded successfully, trigger failure check
                if (toUpload.isNotEmpty() && uploadedDocsList.isEmpty()) {
                    _uiState.update { it.copy(isUploadingDocuments = false, errorMessage = "Document upload failed, try again.") }
                    return@launch
                }
                
                // Construct the full documents array
                val documents = JSONArray()
                // 1. Add existing documents
                for (d in _uiState.value.selectedExistingDocuments) {
                    val obj = JSONObject()
                    obj.put("docid", d.docid)
                    obj.put("doctype", d.doctype.ifEmpty { "general" })
                    obj.put("user_id", d.user_id.ifEmpty { Constants.USER_ID })
                    documents.put(obj)
                }
                // 2. Add newly uploaded documents
                for (d in uploadedDocsList) {
                    val obj = JSONObject()
                    obj.put("docid", d.docid)
                    obj.put("doctype", "general")
                    obj.put("user_id", Constants.USER_ID)
                    documents.put(obj)
                }
                
                // Call PATCH API to link documents to matter
                val patchResult = executeUpdateDocuments(documents)
                if (patchResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                    val resJson = JSONObject(patchResult.responseContent ?: "")
                    val msg = resJson.optString("msg")
                    
                    // Clear legacy/coordinator array list static structures
                    val infoEn = Constants.matterInformation_en
                    if (infoEn != null) {
                        val list = infoEn.matterArraylist
                        if (list != null && list.isNotEmpty()) {
                            val mm = list[0]
                            mm.documents = documents
                        }
                    }
                    
                    if (msg.isNotEmpty()) {
                        AndroidUtils.showToast(msg, getApplication())
                    }

                    _uiState.update {
                        it.copy(
                            isUploadingDocuments = false,
                            showSuccessDialog = true,
                            successMessage = msg,
                            toastMessage = msg,
                            selectedUploadFiles = emptyList()
                        )
                    }
                } else {
                    _uiState.update { it.copy(isUploadingDocuments = false, errorMessage = "Failed to update matter documents.") }
                }
            } catch (e: Exception) {
                android.util.Log.e("MatterEditViewModel", "Exception in uploadAndSaveDocuments", e)
                _uiState.update { it.copy(isUploadingDocuments = false, errorMessage = "An error occurred: ${e.message}") }
            }
        }
    }
}
