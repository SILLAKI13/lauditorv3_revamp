package com.digicoffer.lauditor.feature.groups.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.Groups.Models.GroupModel
import com.digicoffer.lauditor.Groups.Models.SearchDo
import com.digicoffer.lauditor.Groups.Models.ViewGroupModel
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.feature.groups.data.repository.GroupsRepository
import com.digicoffer.lauditor.feature.groups.presentation.state.GroupsUiEvent
import com.digicoffer.lauditor.feature.groups.presentation.state.GroupsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.Objects

class GroupsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GroupsRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(GroupsUiState())
    val uiState: StateFlow<GroupsUiState> = _uiState.asStateFlow()

    fun onEvent(event: GroupsUiEvent) {
        when (event) {
            is GroupsUiEvent.LoadGroups -> loadGroups()
            is GroupsUiEvent.LoadMembers -> loadMembers()
            is GroupsUiEvent.DismissToast -> _uiState.update { it.copy(toastMessage = null) }
            is GroupsUiEvent.CreateGroup -> createGroup(event)
            is GroupsUiEvent.DeleteGroup -> deleteGroup(event)
            is GroupsUiEvent.UpdateGroup -> updateGroup(event)
            is GroupsUiEvent.UpdateGroupMembers -> updateGroupMembers(event)
            is GroupsUiEvent.UpdateGroupHead -> updateGroupHead(event)
            is GroupsUiEvent.FetchGroupCounts -> fetchGroupCounts(event)
            is GroupsUiEvent.FetchAuditLogs -> fetchAuditLogs(event)
        }
    }

    private fun loadGroups() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val httpResult = repository.fetchGroups()
            _uiState.update { it.copy(isLoading = false) }
            if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val result = JSONObject(httpResult.responseContent ?: "")
                    val data = result.getJSONArray("data")
                    val parsedList = ArrayList<ViewGroupModel>()
                    for (i in 0 until data.length()) {
                        val jsonObject = data.getJSONObject(i)
                        val viewGroupModel = ViewGroupModel().apply {
                            id = jsonObject.optString("id")
                            val dateStr = jsonObject.optString("created")
                            isIsdisabled = jsonObject.optBoolean("isdisabled")
                            if (dateStr.isNotEmpty()) {
                                val date_new = AndroidUtils.stringToDateTimeDefault(dateStr, "MMM dd, yyyy, hh:mm a")
                                created = AndroidUtils.getDateToString(date_new, "MMM dd, yyyy | hh:mm a")
                            } else {
                                created = ""
                            }
                            members = jsonObject.optJSONArray("members")
                            memberCount = jsonObject.optString("memberCount")
                            description = jsonObject.optString("description")
                            name = jsonObject.optString("name")
                            val group_head_obj = jsonObject.optJSONObject("groupHead")
                            if (group_head_obj != null) {
                                group_head_id = group_head_obj.optString("id")
                                group_head_name = group_head_obj.optString("name")
                                owner_name = group_head_obj.optString("name")
                            }
                        }
                        parsedList.add(viewGroupModel)
                    }
                    // Sort according to legacy sorting rules
                    parsedList.sortWith(Comparator { a, b ->
                        val nameA = a.name ?: ""
                        val nameB = b.name ?: ""
                        when {
                            nameA.equals("AAM", ignoreCase = true) -> -1
                            nameB.equals("AAM", ignoreCase = true) -> 1
                            nameA.equals("SuperUser", ignoreCase = true) -> -1
                            nameB.equals("SuperUser", ignoreCase = true) -> 1
                            else -> nameA.compareTo(nameB, ignoreCase = true)
                        }
                    })
                    _uiState.update { it.copy(groupsList = parsedList) }
                } catch (e: Exception) {
                    _uiState.update { it.copy(toastMessage = "Failed to parse groups: ${e.localizedMessage}") }
                }
            } else {
                _uiState.update { it.copy(toastMessage = parseErrorMessage(httpResult.responseContent)) }
            }
        }
    }

    private fun loadMembers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val httpResult = repository.fetchMembers()
            _uiState.update { it.copy(isLoading = false) }
            if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val result = JSONObject(httpResult.responseContent ?: "")
                    val data = result.getJSONObject("data")
                    val users = data.getJSONArray("users")
                    val parsedList = ArrayList<GroupModel>()
                    for (i in 0 until users.length()) {
                        val jsonObject = users.getJSONObject(i)
                        val groupModel = GroupModel().apply {
                            id = jsonObject.getString("id")
                            name = jsonObject.getString("name")
                            isenabled = true
                        }
                        parsedList.add(groupModel)
                    }
                    parsedList.sortBy { it.name }
                    _uiState.update { it.copy(membersList = parsedList) }
                } catch (e: Exception) {
                    _uiState.update { it.copy(toastMessage = "Failed to parse members: ${e.localizedMessage}") }
                }
            } else {
                _uiState.update { it.copy(toastMessage = parseErrorMessage(httpResult.responseContent)) }
            }
        }
    }

    private fun createGroup(event: GroupsUiEvent.CreateGroup) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val membersArray = JSONArray().apply {
                event.members.forEach { put(it) }
            }
            val httpResult = repository.createGroup(
                name = event.name,
                description = event.description,
                groupHead = event.groupHead,
                members = membersArray
            )
            _uiState.update { it.copy(isLoading = false) }
            handleResponse(httpResult, event.onSuccess)
        }
    }

    private fun deleteGroup(event: GroupsUiEvent.DeleteGroup) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val httpResult = repository.deleteGroup(id = event.id, groupHead = event.groupHead)
            _uiState.update { it.copy(isLoading = false) }
            handleResponse(httpResult, event.onSuccess)
        }
    }

    private fun updateGroup(event: GroupsUiEvent.UpdateGroup) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val httpResult = repository.updateGroup(id = event.id, name = event.name, description = event.description)
            _uiState.update { it.copy(isLoading = false) }
            handleResponse(httpResult, event.onSuccess)
        }
    }

    private fun updateGroupMembers(event: GroupsUiEvent.UpdateGroupMembers) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val membersArray = JSONArray().apply {
                event.members.forEach { put(it) }
            }
            val httpResult = repository.updateGroupMembers(id = event.id, members = membersArray)
            _uiState.update { it.copy(isLoading = false) }
            handleResponse(httpResult, event.onSuccess)
        }
    }

    private fun updateGroupHead(event: GroupsUiEvent.UpdateGroupHead) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val httpResult = repository.updateGroupHead(id = event.id, groupHead = event.groupHead)
            _uiState.update { it.copy(isLoading = false) }
            handleResponse(httpResult, event.onSuccess)
        }
    }

    private fun fetchGroupCounts(event: GroupsUiEvent.FetchGroupCounts) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val httpResult = repository.fetchGroupCounts(id = event.id)
            _uiState.update { it.copy(isLoading = false) }
            if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val result = JSONObject(httpResult.responseContent ?: "")
                    val counts = result.optJSONObject("counts")
                    _uiState.update { it.copy(counts = counts) }
                    event.onComplete(counts)
                } catch (e: Exception) {
                    _uiState.update { it.copy(toastMessage = "Failed to parse counts: ${e.localizedMessage}") }
                    event.onComplete(null)
                }
            } else {
                _uiState.update { it.copy(toastMessage = parseErrorMessage(httpResult.responseContent)) }
                event.onComplete(null)
            }
        }
    }

    private fun fetchAuditLogs(event: GroupsUiEvent.FetchAuditLogs) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val httpResult = repository.fetchAuditLogs(
                id = event.id,
                fromDate = event.fromDate,
                toDate = event.toDate,
                tm = event.tm,
                search = event.search
            )
            _uiState.update { it.copy(isLoading = false) }
            if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val result = JSONObject(httpResult.responseContent ?: "")
                    val data = result.getJSONArray("data")
                    val list = ArrayList<SearchDo>()
                    for (i in 0 until data.length()) {
                        val json = data.getJSONObject(i)
                        val log = SearchDo().apply {
                            msg = json.optString("msg")
                            timestamp = json.optString("timestamp")
                        }
                        list.add(log)
                    }
                    _uiState.update { it.copy(auditLogs = list) }
                } catch (e: Exception) {
                    _uiState.update { it.copy(toastMessage = "Failed to parse audit logs: ${e.localizedMessage}") }
                }
            } else {
                _uiState.update { it.copy(toastMessage = parseErrorMessage(httpResult.responseContent)) }
            }
        }
    }

    private fun handleResponse(httpResult: HttpResultDo, onSuccess: () -> Unit) {
        try {
            if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                val result = JSONObject(httpResult.responseContent ?: "")
                if (result.has("errors")) {
                    val jsonArray = result.getJSONArray("errors")
                    val iserror = jsonArray.getJSONObject(0)
                    val msg = iserror.getString("msg")
                    _uiState.update { it.copy(toastMessage = msg) }
                } else {
                    val msg = result.optString("msg").ifEmpty { "Operation completed successfully" }
                    _uiState.update { it.copy(toastMessage = msg) }
                    onSuccess()
                    loadGroups()
                }
            } else {
                val errMsg = parseErrorMessage(httpResult.responseContent)
                _uiState.update { it.copy(toastMessage = errMsg) }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(toastMessage = e.message ?: "Connection error") }
        }
    }

    private fun parseErrorMessage(content: String?): String {
        return try {
            val obj = JSONObject(content ?: "")
            obj.optString("msg", "Request failed")
        } catch (e: Exception) {
            content ?: "Connection error"
        }
    }
}
