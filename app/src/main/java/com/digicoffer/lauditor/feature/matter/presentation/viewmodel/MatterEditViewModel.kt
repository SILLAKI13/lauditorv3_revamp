package com.digicoffer.lauditor.feature.matter.presentation.viewmodel

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.Matter.Models.AdvocateModel
import com.digicoffer.lauditor.Matter.Models.MatterModel
import com.digicoffer.lauditor.Matter.Models.ViewMatterModel
import com.digicoffer.lauditor.Matter.Models.ClientsModel
import com.digicoffer.lauditor.Matter.Models.TeamModel
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
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.io.File
import kotlin.coroutines.resume

class MatterEditViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MatterEditUiState())
    val uiState: StateFlow<MatterEditUiState> = _uiState.asStateFlow()

    private var legacyModel = MatterModel()

    fun initialize(editModel: ViewMatterModel?) {
        viewModelScope.launch {
            val isCreate = Constants.create_matter
            val matterType = Constants.MATTER_TYPE ?: "Legal"
            
            _uiState.update { it.copy(matterType = matterType) }

            if (isCreate) {
                // Set default created date to today
                val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.US)
                val today = sdf.format(Date())
                _uiState.update { it.copy(createdDate = today) }
                
                // Fetch generated matter ID
                fetchGeneratedMatterId()
            } else {
                if (editModel != null) {
                    populateFromEditModel(editModel)
                }
            }
            
            // Load case types list
            fetchCaseTypes()
        }
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
        _uiState.update {
            it.copy(
                advocateName = name,
                advocateEmail = email,
                advocatePhone = phone
            )
        }
    }

    fun onSaveAdvocateClicked() {
        val name = _uiState.value.advocateName.trim()
        val email = _uiState.value.advocateEmail.trim()
        val phone = _uiState.value.advocatePhone.trim()

        if (name.isEmpty()) {
            AndroidUtils.showToast("Please enter the name.", getApplication())
            return
        }
        if (email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            AndroidUtils.showToast("Please enter a valid email address", getApplication())
            return
        }
        if (phone.isNotEmpty() && !phone.matches(Regex("^\\d{10}$"))) {
            AndroidUtils.showToast("Please enter a 10 digit valid mobile number", getApplication())
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
                advocatePhone = ""
            )
        }
    }

    fun onCancelAdvocateClicked() {
        _uiState.update { it.copy(isAdvocateFormVisible = false) }
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

                // 1. Check title uniqueness
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

                // 2. Perform Save (Create or Update)
                val saveResult = if (Constants.create_matter) {
                    executeCreateMatter()
                } else {
                    executeUpdateMatter(editModel)
                }

                _uiState.update { it.copy(isLoading = false) }

                if (saveResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                    val saveJson = JSONObject(saveResult.responseContent ?: "")
                    if (!saveJson.optBoolean("error", false)) {
                        val msg = saveJson.optString("msg")
                        AndroidUtils.showToast(msg, getApplication())
                        
                        if (Constants.create_matter) {
                            Constants.Matter_id = saveJson.optString("matter_id")
                        }

                        // Save data back to legacy coordinator memory
                        updateLegacyModel(editModel)

                        Constants.GeneratedMatterId = _uiState.value.matterNumber
                        Constants.GeneratedMatterTitle = _uiState.value.title

                        if (isSaveLater) {
                            _uiState.update { it.copy(navigateToView = true) }
                        } else {
                            _uiState.update { it.copy(navigateToNext = true) }
                        }
                    } else {
                        val msg = saveJson.optString("msg")
                        _uiState.update { it.copy(errorMessage = msg) }
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
                _uiState.update { it.copy(matterNumber = id) }
            }
        }
    }

    private suspend fun fetchCaseTypes() {
        val result = getCaseTypes()
        if (result.result == WebServiceHelper.ServiceCallStatus.Success) {
            val json = JSONObject(result.responseContent ?: "")
            if (!json.optBoolean("error", false)) {
                val arr = json.optJSONArray("data") ?: JSONArray()
                val list = mutableListOf<String>()
                for (i in 0 until arr.length()) {
                    list.add(arr.optString(i))
                }
                _uiState.update { it.copy(caseTypeList = list) }
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
            "Generate Matter ID",
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
            "Get Case Types",
            JSONObject().toString()
        )
    }

    private suspend fun checkTitleUnique(title: String, type: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
        val json = JSONObject()
        if (!Constants.Matter_id.isNullOrEmpty()) {
            json.put("matter_id", Constants.Matter_id)
        }
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
            "Check Unique",
            json.toString()
        )
    }

    private suspend fun executeCreateMatter(): HttpResultDo = suspendCancellableCoroutine { continuation ->
        val payload = buildPayload()
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
            payload.toString()
        )
    }

    private suspend fun executeUpdateMatter(editModel: ViewMatterModel?): HttpResultDo = suspendCancellableCoroutine { continuation ->
        val payload = buildPayload()
        val type = _uiState.value.matterType.lowercase(Locale.ROOT)
        val id = Constants.Matter_id ?: ""
        
        // When updating, we need to pass back the existing clients, groupAcls, members etc.
        if (editModel != null) {
            val clients = JSONArray()
            val editClients = editModel.clients ?: JSONArray()
            for (i in 0 until editClients.length()) {
                val clientObject = editClients.getJSONObject(i)
                val obj = JSONObject()
                obj.put("id", clientObject.getString("id"))
                obj.put("type", clientObject.getString("type"))
                clients.put(obj)
            }
            val corpId = editModel.corpId ?: ""
            if (corpId.isNotEmpty()) {
                val obj = JSONObject()
                obj.put("id", corpId)
                obj.put("type", "corporate")
                clients.put(obj)
            }
            payload.put("clients", clients)

            val groupAcls = editModel.groupAcls ?: JSONArray()
            payload.put("group_acls", groupAcls)

            val members = JSONArray()
            val editMembers = editModel.members ?: JSONArray()
            for (i in 0 until editMembers.length()) {
                val obj = JSONObject()
                obj.put("id", editMembers.getJSONObject(i).getString("id"))
                members.put(obj)
            }
            payload.put("members", members)
        }

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
            payload.toString()
        )
    }

    private fun buildPayload(): JSONObject {
        val json = JSONObject()
        val isLegal = _uiState.value.matterType == "Legal"

        json.put("title", _uiState.value.title)
        json.put("matter_id", _uiState.value.matterNumber)
        json.put("description", _uiState.value.description)
        json.put("priority", _uiState.value.priority)
        json.put("status", _uiState.value.status)
        
        val createdFormatted = AndroidUtils.convertAnyDateToDDMMYYYY(_uiState.value.createdDate)
        json.put("created_date", createdFormatted)

        // Tags to object map
        val tagsObj = JSONObject()
        _uiState.value.tagsList.forEachIndexed { index, tag ->
            tagsObj.put(index.toString(), tag)
        }
        json.put("tags", tagsObj)

        if (isLegal) {
            json.put("case_number", _uiState.value.caseNumber)
            json.put("judges", _uiState.value.judges)
            json.put("case_type", _uiState.value.selectedCaseType)
            json.put("court_name", _uiState.value.courtName)

            val dofFormatted = AndroidUtils.convertAnyDateToDDMMYYYY(_uiState.value.dateOfFiling)
            json.put("date_of_filling", dofFormatted)

            val advArr = JSONArray()
            _uiState.value.advocatesList.forEach { adv ->
                val advJson = JSONObject()
                advJson.put("name", adv.advocate_name)
                advJson.put("email", adv.email)
                advJson.put("phone", adv.number)
                advArr.put(advJson)
            }
            json.put("opponent_advocates", advArr)
            json.put("affidavit_filing_date", "")
            json.put("affidavit_isfiled", "")
        } else {
            json.put("matter_number", _uiState.value.caseNumber)
            json.put("matter_type", _uiState.value.selectedCaseType)

            val startFormatted = AndroidUtils.convertAnyDateToDDMMYYYY(_uiState.value.startDate)
            json.put("startdate", startFormatted)

            val closeFormatted = AndroidUtils.convertAnyDateToDDMMYYYY(_uiState.value.endDate)
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
            _uiState.update { it.copy(selectedClients = currentSelected) }
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
            _uiState.update { it.copy(selectedTeamMembers = currentSelected) }
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
                        AndroidUtils.showToast(msg, getApplication())
                        
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

                        onNavigate()
                    } else {
                        val msg = json.optString("msg")
                        _uiState.update { it.copy(errorMessage = msg) }
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

    // Step 3 (Document(s)) Methods
    fun toggleBrowseDialog(show: Boolean) {
        _uiState.update { it.copy(showBrowseDialog = show) }
    }

    fun setEditMetadataFileIndex(index: Int?) {
        _uiState.update { it.copy(editMetadataFileIndex = index) }
    }

    fun addUploadFile(file: File, name: String) {
        val contentString = name.replace(".", "/")
        val parts = contentString.split("/".toRegex()).toTypedArray()
        var docType = "pdf"
        var docname = name
        if (parts.size >= 2) {
            docType = parts[1]
            docname = parts[0]
        }
        val documentModel = com.digicoffer.lauditor.Documents.Models.DocumentsModel().apply {
            this.name = docname
            this.filename = name
            this.content_type = docType
            this.description = docname
            this.file = file
            this.isIsenabled = false
            this.isencrypted = false
        }
        _uiState.update {
            it.copy(selectedUploadFiles = it.selectedUploadFiles + documentModel)
        }
    }

    fun updateUploadFile(index: Int, name: String, description: String, expDate: String, isDownloadDisabled: Boolean, isEncrypted: Boolean) {
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

    fun dismissSuccessDialog() {
        _uiState.update { it.copy(showSuccessDialog = false) }
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
                    
                    val contentString = file.name.replace(".", "/")
                    val parts = contentString.split("/".toRegex()).toTypedArray()
                    var docType = "pdf"
                    var docname = file.name
                    if (parts.size >= 2) {
                        docType = parts[1]
                        docname = parts[0]
                    }
                    
                    val contentType = if (docType.equals("apng", ignoreCase = true) || docType.equals("avif", ignoreCase = true) || docType.equals("gif", ignoreCase = true) || docType.equals("jpeg", ignoreCase = true) || docType.equals("png", ignoreCase = true) || docType.equals("svg", ignoreCase = true) || docType.equals("webp", ignoreCase = true) || docType.equals("jpg", ignoreCase = true)) {
                        "image/$docType"
                    } else {
                        "application/$docType"
                    }
                    
                    val matters = JSONArray().apply { put(Constants.Matter_id) }
                    val payload = JSONObject().apply {
                        put("name", docModel.name)
                        put("description", docModel.description)
                        put("expiration_date", AndroidUtils.convertAnyDateToDDMMYYYY(docModel.expiration_date))
                        put("filename", docname)
                        put("matters", matters)
                        put("category", "client")
                        if (Constants.clientList.length() > 0) {
                            put("clients", Constants.clientList)
                        } else {
                            put("clients", Constants.corpclientList)
                        }
                        put("groups", Constants.ex_group_attachment)
                        put("downloadDisabled", docModel.isIsenabled)
                        put("custom_encrypt", docModel.isencrypted)
                        put("tags", docModel.tags ?: "")
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
                    
                    _uiState.update {
                        it.copy(
                            isUploadingDocuments = false,
                            showSuccessDialog = true,
                            successMessage = msg,
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
