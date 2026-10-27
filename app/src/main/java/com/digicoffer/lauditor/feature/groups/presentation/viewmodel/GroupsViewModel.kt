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
                            val ts = jsonObject.opt("timestamp")
                            timestamp = when (ts) {
                                is Number -> ts.toLong()
                                is String -> ts.toLongOrNull() ?: 0L
                                else -> 0L
                            }
                        }
                        parsedList.add(viewGroupModel)
                    }
                    // Sort descending by timestamp (latest created first). Old system groups without timestamp will naturally sit at the end.
                    parsedList.sortWith(compareByDescending { it.timestamp })
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
                    val data = result.optJSONObject("data")
                    val users = data?.optJSONArray("users") ?: result.optJSONArray("users") ?: JSONArray()
                    val parsedList = ArrayList<GroupModel>()
                    for (i in 0 until users.length()) {
                        val jsonObject = users.optJSONObject(i) ?: continue
                        val memberGroups = jsonObject.optJSONArray("groups")
                        var isSystemRestricted = false
                        var reason = ""
                        if (memberGroups != null) {
                            for (g in 0 until memberGroups.length()) {
                                val gObj = memberGroups.optJSONObject(g)
                                val gName = gObj?.optString("name").orEmpty()
                                if (gName.equals("AAM", ignoreCase = true) || gName.equals("SuperUser", ignoreCase = true)) {
                                    isSystemRestricted = true
                                    reason = "System generated user - $gName"
                                    break
                                }
                            }
                        }
                        val groupModel = GroupModel().apply {
                            id = jsonObject.optString("id")
                            name = jsonObject.optString("name")
                            isenabled = true
                            isRestricted = isSystemRestricted
                            restrictedReason = reason
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
            handleResponse(
                httpResult = httpResult,
                defaultSuccessMsg = "You have successfully created ${event.name}.",
                onSuccess = event.onSuccess
            )
        }
    }

    private fun deleteGroup(event: GroupsUiEvent.DeleteGroup) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val httpResult = repository.deleteGroup(id = event.id, groupHead = event.groupHead)
            _uiState.update { it.copy(isLoading = false) }
            handleResponse(
                httpResult = httpResult,
                defaultSuccessMsg = "You have successfully deleted the group.",
                onSuccess = event.onSuccess
            )
        }
    }

    private fun updateGroup(event: GroupsUiEvent.UpdateGroup) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val httpResult = repository.updateGroup(id = event.id, name = event.name, description = event.description)
            _uiState.update { it.copy(isLoading = false) }
            handleResponse(
                httpResult = httpResult,
                defaultSuccessMsg = "You have successfully updated the group information.",
                onSuccess = event.onSuccess
            )
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
            val successMsg = if (event.groupName.isNotBlank()) {
                "Congratulations!!\nYou have successfully updated group members list of ${event.groupName} group."
            } else {
                "You have successfully updated group members list."
            }
            handleResponse(
                httpResult = httpResult,
                defaultSuccessMsg = successMsg,
                onSuccess = event.onSuccess
            )
        }
    }

    private fun updateGroupHead(event: GroupsUiEvent.UpdateGroupHead) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val httpResult = repository.updateGroupHead(id = event.id, groupHead = event.groupHead)
            _uiState.update { it.copy(isLoading = false) }
            handleResponse(
                httpResult = httpResult,
                defaultSuccessMsg = "You have successfully updated the group head.",
                onSuccess = event.onSuccess
            )
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
                category = event.category,
                client = event.client,
                fromDate = event.fromDate,
                toDate = event.toDate,
                tm = event.tm,
                search = event.search
            )
            _uiState.update { it.copy(isLoading = false) }
            if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val result = JSONObject(httpResult.responseContent ?: "")
                    val data = result.optJSONArray("data") ?: JSONArray()
                    val list = ArrayList<SearchDo>()
                    for (i in 0 until data.length()) {
                        val json = data.getJSONObject(i)
                        val log = SearchDo().apply {
                            category = json.optString("category")
                            msg = json.optString("msg")
                            val rawTs = json.optString("timestamp")
                            timestamp = if (rawTs.isNotEmpty()) {
                                try {
                                    val date = AndroidUtils.stringToDateTimeDefault(rawTs, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                                        ?: AndroidUtils.stringToDateTimeDefault(rawTs, "yyyy-MM-dd'T'HH:mm:ss'Z'")
                                        ?: AndroidUtils.stringToDateTimeDefault(rawTs, "yyyy-MM-dd'T'HH:mm:ss")
                                    if (date != null) {
                                        AndroidUtils.getDateToString(date, "MMM dd, yyyy hh:mm a")
                                    } else {
                                        rawTs
                                    }
                                } catch (e: Exception) {
                                    rawTs
                                }
                            } else {
                                ""
                            }
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

    private fun handleResponse(
        httpResult: HttpResultDo,
        defaultSuccessMsg: String = "Operation completed successfully",
        onSuccess: () -> Unit
    ) {
        try {
            val statusCode = httpResult.status_code
            val isSuccessStatus = (statusCode in 200..299) || (statusCode == 0 && httpResult.result == WebServiceHelper.ServiceCallStatus.Success)

            if (isSuccessStatus) {
                val result = JSONObject(httpResult.responseContent ?: "{}")
                if (result.has("errors")) {
                    val jsonArray = result.getJSONArray("errors")
                    val iserror = jsonArray.getJSONObject(0)
                    val msg = iserror.getString("msg")
                    _uiState.update { it.copy(toastMessage = msg) }
                } else if (result.optBoolean("error", false)) {
                    val msg = result.optString("msg").ifEmpty { "Operation failed" }
                    _uiState.update { it.copy(toastMessage = msg) }
                } else {
                    val serverMsg = result.optString("msg")
                    val msg = if (defaultSuccessMsg != "Operation completed successfully") {
                        defaultSuccessMsg
                    } else {
                        serverMsg.ifEmpty { defaultSuccessMsg }
                    }
                    _uiState.update { it.copy(toastMessage = msg) }
                    onSuccess()
                    loadGroups()
                }
            } else {
                val errMsg = parseErrorMessage(httpResult.responseContent)
                _uiState.update { it.copy(toastMessage = errMsg) }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(toastMessage = parseErrorMessage(httpResult.responseContent)) }
        }
    }

    private fun parseErrorMessage(content: String?): String {
        return try {
            val trimmed = content?.trim().orEmpty()
            if (trimmed.startsWith("{")) {
                val obj = JSONObject(trimmed)
                if (obj.has("errors")) {
                    val arr = obj.optJSONArray("errors")
                    if (arr != null && arr.length() > 0) {
                        return arr.getJSONObject(0).optString("msg", "Request failed")
                    }
                }
                obj.optString("msg", "Request failed")
            } else if (trimmed.isNotEmpty()) {
                trimmed
            } else {
                "Request failed"
            }
        } catch (e: Exception) {
            content?.takeIf { it.isNotBlank() } ?: "Connection error"
        }
    }
}
