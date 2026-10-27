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
                        fun clean(key: String): String {
                            if (jsonObject.isNull(key)) return ""
                            val v = jsonObject.optString(key, "").trim()
                            return if (v.equals("null", ignoreCase = true)) "" else v
                        }
                        val member = MembersModel().apply {
                            id = clean("id").ifEmpty { clean("_id") }
                            name = clean("name")
                            isdisabled = jsonObject.optBoolean("isdisabled", false)
                            val curr = clean("currency")
                            currency = if (curr.isEmpty() || curr.equals("null", ignoreCase = true)) "IndianRupee(INR)" else curr
                            defaultRate = clean("defaultRate")
                            designation = clean("designation")
                            email = clean("email")
                            val phoneVal = clean("mobile").ifEmpty {
                                clean("phone_number").ifEmpty {
                                    clean("phone").ifEmpty {
                                        clean("contact_phone")
                                    }
                                }
                            }
                            phone = phoneVal
                            mobile = phoneVal
                            lastLogin = clean("lastLogin")
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
                phone = event.phone,
                email = event.email,
                emailConfirm = event.emailConfirm,
                groups = event.groups
            )
            _uiState.update { it.copy(isLoading = false) }
            val memberName = event.name.trim()
            val defaultMsg = if (memberName.isNotEmpty()) {
                "You have successfully created member $memberName."
            } else {
                "Member created successfully."
            }
            handleResponse(httpResult, defaultMsg, event.onSuccess)
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
                phone = event.phone,
                email = event.email,
                emailConfirm = event.emailConfirm
            )
            _uiState.update { it.copy(isLoading = false) }
            val memberName = event.name.trim()
            val defaultMsg = if (memberName.isNotEmpty()) {
                "You have successfully updated member information for $memberName."
            } else {
                "You have successfully updated member information."
            }
            handleResponse(httpResult, defaultMsg, event.onSuccess)
        }
    }

    private fun updateGroupAccess(event: MembersUiEvent.UpdateGroupAccess) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val httpResult = repository.updateGroupAccess(id = event.id, groups = event.groups)
            _uiState.update { it.copy(isLoading = false) }
            val memberName = event.memberName.trim()
            val defaultMsg = if (memberName.isNotEmpty()) {
                "You have successfully updated group access for $memberName."
            } else {
                "You have successfully updated group access."
            }
            handleResponse(httpResult, defaultMsg, event.onSuccess)
        }
    }

    private fun resetPassword(event: MembersUiEvent.ResetPassword) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val httpResult = repository.resetPassword(memberId = event.memberId)
            _uiState.update { it.copy(isLoading = false) }
            val defaultMsg = "Password reset email has been sent successfully."
            handleResponse(httpResult, defaultMsg, event.onSuccess)
        }
    }

    private fun deleteMember(event: MembersUiEvent.DeleteMember) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val httpResult = repository.deleteMember(id = event.id)
            _uiState.update { it.copy(isLoading = false) }
            val memberName = event.memberName.trim()
            val defaultMsg = if (memberName.isNotEmpty()) {
                "You have successfully deleted member $memberName."
            } else {
                "Member deleted successfully."
            }
            handleResponse(httpResult, defaultMsg, event.onSuccess)
        }
    }

    private fun upgradePracticePartner(event: MembersUiEvent.UpgradePracticePartner) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val httpResult = repository.upgradePracticePartner(id = event.id, email = event.email)
            _uiState.update { it.copy(isLoading = false) }
            val memberName = event.memberName.trim()
            val defaultMsg = if (memberName.isNotEmpty()) {
                "You have successfully upgraded $memberName to Practice Partner."
            } else {
                "Member upgraded to Practice Partner successfully."
            }
            handleResponse(httpResult, defaultMsg, event.onSuccess)
        }
    }

    private fun handleResponse(httpResult: HttpResultDo, defaultSuccessMsg: String, onSuccess: () -> Unit) {
        try {
            val isSuccess = (httpResult.status_code in 200..299) ||
                    (httpResult.status_code == 0 && httpResult.result == WebServiceHelper.ServiceCallStatus.Success)

            val result = if (!httpResult.responseContent.isNullOrBlank()) {
                try { JSONObject(httpResult.responseContent) } catch (e: Exception) { null }
            } else null

            var msg = result?.optString("msg", "")?.trim() ?: ""
            if (msg.isEmpty() && result?.has("data") == true) {
                val dataObj = result.optJSONObject("data")
                if (dataObj != null) {
                    msg = dataObj.optString("msg", "").trim()
                }
            }

            if (isSuccess) {
                // If backend msg is generic, empty, or unhelpful, use action-specific Web message
                val finalMsg = if (msg.isEmpty() ||
                    msg.equals("Data updated successfully", ignoreCase = true) ||
                    msg.equals("Member updated", ignoreCase = true) ||
                    msg.equals("User updated successfully", ignoreCase = true) ||
                    msg.equals("Member updated successfully", ignoreCase = true) ||
                    msg.equals("Success", ignoreCase = true) ||
                    msg.equals("Successful", ignoreCase = true)) {
                    defaultSuccessMsg
                } else {
                    msg
                }
                _uiState.update { it.copy(toastMessage = finalMsg, isToastSuccess = true) }
                onSuccess()
                loadMembers()
            } else {
                val errorMsg = parseErrorMessage(httpResult.responseContent)
                _uiState.update { it.copy(toastMessage = errorMsg, isToastSuccess = false) }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(toastMessage = e.localizedMessage ?: "Connection error", isToastSuccess = false) }
        }
    }

    private fun parseErrorMessage(content: String?): String {
        if (content.isNullOrBlank()) return "Operation failed"
        return try {
            val obj = JSONObject(content)
            val errorsArray = obj.optJSONArray("errors")
            if (errorsArray != null && errorsArray.length() > 0) {
                val errorMessages = mutableListOf<String>()
                for (i in 0 until errorsArray.length()) {
                    val errObj = errorsArray.optJSONObject(i)
                    if (errObj != null) {
                        val field = errObj.optString("field", "")
                        val m = errObj.optString("msg", "")
                        if (field.isNotEmpty() && m.isNotEmpty()) {
                            errorMessages.add("$field: $m")
                        } else if (m.isNotEmpty()) {
                            errorMessages.add(m)
                        }
                    }
                }
                if (errorMessages.isNotEmpty()) {
                    return errorMessages.joinToString("\n")
                }
            }
            val msg = obj.optString("msg", "").trim()
            if (msg.isNotEmpty()) return msg
            val message = obj.optString("message", "").trim()
            if (message.isNotEmpty()) return message
            content
        } catch (e: Exception) {
            content
        }
    }
}

