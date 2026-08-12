package com.digicoffer.lauditor.feature.members.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.Groups.Models.ViewGroupModel
import com.digicoffer.lauditor.Members.MembersModel
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.feature.members.data.repository.MembersRepository
import com.digicoffer.lauditor.feature.members.presentation.state.MembersUiEvent
import com.digicoffer.lauditor.feature.members.presentation.state.MembersUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class MembersViewModel(private val repository: MembersRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(MembersUiState())
    val uiState: StateFlow<MembersUiState> = _uiState.asStateFlow()

    fun onEvent(event: MembersUiEvent) {
        when (event) {
            is MembersUiEvent.LoadMembers -> loadMembers()
            is MembersUiEvent.LoadGroups -> loadGroups()
            is MembersUiEvent.DismissToast -> _uiState.update { it.copy(toastMessage = null) }
            is MembersUiEvent.CreateMember -> createMember(event)
            is androidx.compose.ui.Modifier -> {} // standard check stub
            is MembersUiEvent.UpdateMember -> updateMember(event)
            is MembersUiEvent.UpdateGroupAccess -> updateGroupAccess(event)
            is MembersUiEvent.ResetPassword -> resetPassword(event)
            is MembersUiEvent.DeleteMember -> deleteMember(event)
            is MembersUiEvent.UpgradePracticePartner -> upgradePracticePartner(event)
        }
    }

    private fun loadMembers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val httpResult = repository.fetchMembers()
            if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val result = JSONObject(httpResult.responseContent ?: "")
                    val data = result.getJSONObject("data")
                    val users = data.getJSONArray("users")
                    val totalStr = data.optString("total", "0")
                    val countStr = data.optString("count", "0")

                    val parsedMembers = ArrayList<MembersModel>()
                    for (i in 0 until users.length()) {
                        val jsonObject = users.getJSONObject(i)
                        val member = MembersModel().apply {
                            id = jsonObject.optString("id")
                            name = jsonObject.optString("name")
                            isdisabled = jsonObject.optBoolean("isdisabled", false)
                            currency = jsonObject.optString("currency")
                            defaultRate = jsonObject.optString("defaultRate")
                            designation = jsonObject.optString("designation")
                            email = jsonObject.optString("email")
                            lastLogin = jsonObject.optString("lastLogin")
                            groups = jsonObject.optJSONArray("groups") ?: JSONArray()
                        }
                        parsedMembers.add(member)
                    }

                    val subscriptionEnded = totalStr == countStr
                    Constants.isSubscriptionEnded = subscriptionEnded

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            membersList = parsedMembers,
                            licenseTotal = totalStr,
                            licenseCount = countStr,
                            isSubscriptionEnded = subscriptionEnded
                        )
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(isLoading = false, toastMessage = "Failed to parse members: ${e.localizedMessage}") }
                }
            } else {
                val errorMsg = parseErrorMessage(httpResult.responseContent)
                _uiState.update { it.copy(isLoading = false, toastMessage = errorMsg) }
            }
        }
    }

    private fun loadGroups() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val httpResult = repository.fetchGroups()
            if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val result = JSONObject(httpResult.responseContent ?: "")
                    val data = result.getJSONArray("data")

                    val parsedGroups = ArrayList<ViewGroupModel>()
                    for (i in 0 until data.length()) {
                        val jsonObject = data.getJSONObject(i)
                        val group = ViewGroupModel().apply {
                            id = jsonObject.optString("id")
                            group_id = jsonObject.optString("id")
                            name = jsonObject.optString("name")
                            group_name = jsonObject.optString("name")
                            
                            val dateStr = jsonObject.optString("created")
                            if (dateStr.contains("T")) {
                                val dateNew = AndroidUtils.stringToDateTimeDefault(dateStr, "yyyy-MM-dd'T'HH:mm:ss.SSS")
                                created = AndroidUtils.getDateToString(dateNew, "MMM dd YYYY") ?: ""
                            } else {
                                if (dateStr.isNotEmpty()) {
                                    val dateNew = AndroidUtils.stringToDateTimeDefault(dateStr, "MMM dd, yyyy, hh:mm a")
                                    created = AndroidUtils.getDateToString(dateNew, "MMM dd, yyyy | hh:mm a") ?: ""
                                }
                            }
                        }
                        parsedGroups.add(group)
                    }

                    _uiState.update { it.copy(isLoading = false, groupsList = parsedGroups) }
                } catch (e: Exception) {
                    _uiState.update { it.copy(isLoading = false, toastMessage = "Failed to parse groups: ${e.localizedMessage}") }
                }
            } else {
                val errorMsg = parseErrorMessage(httpResult.responseContent)
                _uiState.update { it.copy(isLoading = false, toastMessage = errorMsg) }
            }
        }
    }

    private fun createMember(event: MembersUiEvent.CreateMember) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val httpResult = repository.createMember(
                name = event.name,
                designation = event.designation,
                defaultRate = event.defaultRate,
                currency = event.currency,
                email = event.email,
                emailConfirm = event.emailConfirm,
                groups = event.groups
            )
            _uiState.update { it.copy(isLoading = false) }
            handleResponse(httpResult, event.onSuccess)
        }
    }

    private fun updateMember(event: MembersUiEvent.UpdateMember) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val httpResult = repository.updateMember(
                id = event.id,
                name = event.name,
                designation = event.designation,
                defaultRate = event.defaultRate,
                currency = event.currency,
                email = event.email,
                emailConfirm = event.emailConfirm
            )
            _uiState.update { it.copy(isLoading = false) }
            handleResponse(httpResult, event.onSuccess)
        }
    }

    private fun updateGroupAccess(event: MembersUiEvent.UpdateGroupAccess) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val httpResult = repository.updateGroupAccess(id = event.id, groups = event.groups)
            _uiState.update { it.copy(isLoading = false) }
            handleResponse(httpResult, event.onSuccess)
        }
    }

    private fun resetPassword(event: MembersUiEvent.ResetPassword) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val httpResult = repository.resetPassword(memberId = event.memberId)
            _uiState.update { it.copy(isLoading = false) }
            handleResponse(httpResult, event.onSuccess)
        }
    }

    private fun deleteMember(event: MembersUiEvent.DeleteMember) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val httpResult = repository.deleteMember(id = event.id)
            _uiState.update { it.copy(isLoading = false) }
            handleResponse(httpResult, event.onSuccess)
        }
    }

    private fun upgradePracticePartner(event: MembersUiEvent.UpgradePracticePartner) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val httpResult = repository.upgradePracticePartner(id = event.id)
            _uiState.update { it.copy(isLoading = false) }
            handleResponse(httpResult, event.onSuccess)
        }
    }

    private fun handleResponse(httpResult: HttpResultDo, onSuccess: () -> Unit) {
        try {
            val result = JSONObject(httpResult.responseContent ?: "")
            var msg = result.optString("msg")
            if (msg.isEmpty() && result.has("data")) {
                val dataObj = result.optJSONObject("data")
                if (dataObj != null) {
                    msg = dataObj.optString("msg")
                }
            }
            if (httpResult.requestType == "Reset Password" && msg.isEmpty()) {
                msg = "Password reset email has been sent successfully."
            }
            if (httpResult.status_code == 200) {
                _uiState.update { it.copy(toastMessage = msg) }
                onSuccess()
                loadMembers()
            } else {
                _uiState.update { it.copy(toastMessage = msg.ifEmpty { "Operation failed" }) }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(toastMessage = httpResult.responseContent ?: "Connection error") }
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
