package com.digicoffer.lauditor.feature.matter.presentation.viewmodel

import android.app.Application
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.Matter.Models.ViewMatterModel
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.feature.matter.presentation.state.MatterUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Locale
import kotlin.coroutines.resume

class MatterViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MatterUiState())
    val uiState: StateFlow<MatterUiState> = _uiState.asStateFlow()

    init {
        fetchMatters()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun clearToastMessage() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    fun fetchMatters(navPosition: String = "", cursor: String = "") {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val httpResult = getMatters(navPosition, cursor, _uiState.value.searchQuery)
            _uiState.update { it.copy(isLoading = false) }
            android.util.Log.d("MatterViewModel", "Response content: " + httpResult.responseContent)
            if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val result = JSONObject(httpResult.responseContent ?: "")
                    val isError = result.optBoolean("error", false)
                    if (isError) {
                        val msg = result.optString("msg")
                        _uiState.update { it.copy(toastMessage = msg) }
                    } else {
                        val prevCursor = result.optString("prev_cursor")
                        val nextCursor = result.optString("next_cursor")
                        val mattersArray = result.optJSONArray("matters") ?: JSONArray()
                        
                        val parsedList = mutableListOf<ViewMatterModel>()
                        for (i in 0 until mattersArray.length()) {
                            val jsonObject = mattersArray.optJSONObject(i) ?: continue
                            val viewMatterModel = ViewMatterModel()
                            viewMatterModel.id = jsonObject.optString("id")
                            viewMatterModel.created = jsonObject.optString("created_on")
                            if (jsonObject.has("caseNumber")) {
                                viewMatterModel.caseNumber = jsonObject.optString("caseNumber")
                            }
                            if (jsonObject.has("caseType")) {
                                viewMatterModel.casetype = jsonObject.optString("caseType")
                            }
                            viewMatterModel.clients = jsonObject.optJSONArray("clients") ?: JSONArray()
                            if (jsonObject.has("corporate")) {
                                viewMatterModel.corporate = jsonObject.optJSONArray("corporate") ?: JSONArray()
                            }
                            
                            val client = viewMatterModel.clients
                            val clientNamesList = ArrayList<String>()
                            if (client.length() > 0) {
                                for (j in 0 until client.length()) {
                                    val item = client.get(j)
                                    if (item is JSONArray) {
                                        for (k in 0 until item.length()) {
                                            if (!item.isNull(k)) {
                                                val clientObj = item.optJSONObject(k)
                                                if (clientObj != null && clientObj.has("name") && clientObj.optString("name").isNotEmpty()) {
                                                    clientNamesList.add(clientObj.optString("name"))
                                                }
                                            }
                                        }
                                    } else if (item is JSONObject) {
                                        if (item.has("name") && item.optString("name").isNotEmpty()) {
                                            clientNamesList.add(item.optString("name"))
                                        }
                                    }
                                }
                            }
                            val corp = viewMatterModel.corporate
                            if (clientNamesList.isEmpty() && corp.length() > 0) {
                                for (j in 0 until corp.length()) {
                                    val corpClient = corp.optJSONObject(j)
                                    if (corpClient != null) {
                                        val corpName = corpClient.optString("name")
                                        if (corpName.isNotEmpty()) {
                                            clientNamesList.add(corpName)
                                        }
                                    }
                                }
                            }
                            viewMatterModel.client_name = clientNamesList.joinToString(", ")
                            
                            if (jsonObject.has("courtName")) {
                                viewMatterModel.courtName = jsonObject.optString("courtName")
                            }
                            if (jsonObject.has("isdisabled")) {
                                viewMatterModel.isdisabled = jsonObject.optBoolean("isdisabled")
                            }
                            if (jsonObject.has("date_of_filling")) {
                                viewMatterModel.date_of_filling = AndroidUtils.formatToMMMddYYYY(jsonObject.optString("date_of_filling"))
                            }
                            if (jsonObject.has("closedate")) {
                                viewMatterModel.closedate = AndroidUtils.formatToMMMddYYYY(jsonObject.optString("closedate"))
                            }
                            if (jsonObject.has("matterNumber")) {
                                viewMatterModel.matterNumber = jsonObject.optString("matterNumber")
                            }
                            if (jsonObject.has("matterType")) {
                                viewMatterModel.matterType = jsonObject.optString("matterType")
                            }
                            if (jsonObject.has("startdate")) {
                                viewMatterModel.startdate = AndroidUtils.formatToMMMddYYYY(jsonObject.optString("startdate"))
                            }
                            if (jsonObject.has("timesheets")) {
                                viewMatterModel.timesheets = jsonObject.optJSONArray("timesheets") ?: JSONArray()
                            }
                            if (jsonObject.has("created_date")) {
                                viewMatterModel.created_date = AndroidUtils.formatToMMMddYYYY(jsonObject.optString("created_date"))
                            }
                            if (jsonObject.has("matter_id")) {
                                viewMatterModel.matter_id = jsonObject.optString("matter_id")
                            }
                            viewMatterModel.description = jsonObject.optString("description")
                            viewMatterModel.documents = jsonObject.optJSONArray("documents") ?: JSONArray()
                            viewMatterModel.groupAcls = jsonObject.optJSONArray("groupAcls") ?: JSONArray()
                            viewMatterModel.groups = jsonObject.optJSONArray("groups") ?: JSONArray()
                            if (jsonObject.has("hearingDateDetails")) {
                                viewMatterModel.hearingDateDetails = jsonObject.optJSONObject("hearingDateDetails") ?: JSONObject()
                            }
                            viewMatterModel.is_editable = jsonObject.optBoolean("is_editable")
                            if (jsonObject.has("judges")) {
                                viewMatterModel.judges = jsonObject.optString("judges")
                            }
                            if (jsonObject.has("matterClosedDate")) {
                                viewMatterModel.matterClosedDate = jsonObject.optString("matterClosedDate")
                            }
                            viewMatterModel.members = jsonObject.optJSONArray("members") ?: JSONArray()
                            if (jsonObject.has("nextHearingDate")) {
                                viewMatterModel.nextHearingDate = jsonObject.optString("nextHearingDate")
                            }
                            if (jsonObject.has("opponentAdvocates")) {
                                viewMatterModel.opponentAdvocates = jsonObject.optJSONArray("opponentAdvocates") ?: JSONArray()
                            }
                            viewMatterModel.owner = jsonObject.optJSONObject("owner") ?: JSONObject()
                            val owner = viewMatterModel.owner
                            if (owner.length() != 0) {
                                viewMatterModel.owner_name = owner.optString("name")
                            } else {
                                viewMatterModel.owner_name = " "
                            }
                            viewMatterModel.priority = jsonObject.optString("priority")
                            viewMatterModel.status = jsonObject.optString("status")
                            
                            val tagsObject = jsonObject.optJSONObject("tags")
                            val tagsArray = JSONArray()
                            if (tagsObject != null) {
                                val keys = tagsObject.keys()
                                while (keys.hasNext()) {
                                    val key = keys.next()
                                    try {
                                        tagsArray.put(tagsObject.getString(key))
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                            viewMatterModel.tags_list = tagsArray
                            if (jsonObject.has("tempClients")) {
                                viewMatterModel.tempClients = jsonObject.optJSONArray("tempClients") ?: JSONArray()
                            }
                            if (jsonObject.has("timesheets")) {
                                viewMatterModel.timesheets = jsonObject.optJSONArray("timesheets") ?: JSONArray()
                            }
                            viewMatterModel.temporaryClients = jsonObject.optJSONArray("temporaryClients") ?: JSONArray()
                            viewMatterModel.title = jsonObject.optString("title")
                            
                            parsedList.add(viewMatterModel)
                        }
                        
                        try {
                            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)
                            parsedList.sortWith(Comparator { o1, o2 ->
                                try {
                                    val d1 = dateFormat.parse(o1.created)
                                    val d2 = dateFormat.parse(o2.created)
                                    if (d1 != null && d2 != null) {
                                        d2.compareTo(d1)
                                    } else 0
                                } catch (e: Exception) {
                                    0
                                }
                            })
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        
                        _uiState.update { it.copy(
                            matterList = parsedList,
                            prevCursor = prevCursor,
                            nextCursor = nextCursor
                        ) }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("MatterViewModel", "Exception in parsing:", e)
                    _uiState.update { it.copy(toastMessage = e.message) }
                }
            } else {
                _uiState.update { it.copy(toastMessage = "Request Failed, Try Again") }
            }
        }
    }

    private suspend fun getMatters(navPosition: String, cursor: String, searchQuery: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
        val json = JSONObject()
        val matterType = (Constants.MATTER_TYPE ?: "").lowercase(Locale.ROOT)
        val url = StringBuilder("v2/matter/").append(matterType).append("?paginate=true")
        if (searchQuery.isNotEmpty()) {
            url.append("&search=").append(searchQuery)
        }
        if (navPosition.isNotEmpty() && cursor.isNotEmpty()) {
            url.append("&").append(navPosition).append("=").append(cursor)
        }
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            getApplication(),
            WebServiceHelper.RestMethodType.GET,
            url.toString(),
            "Matter List",
            json.toString()
        )
    }

    fun closeOrReopenMatter(matterId: String, newStatus: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val httpResult = patchMatterStatus(matterId, newStatus)
            _uiState.update { it.copy(isLoading = false) }
            if (httpResult.result == WebServiceHelper.ServiceCallStatus.Success) {
                try {
                    val result = JSONObject(httpResult.responseContent ?: "")
                    val isError = result.optBoolean("error", false)
                    val msg = result.optString("msg")
                    if (!isError) {
                        onResult(true, msg)
                        fetchMatters()
                    } else {
                        onResult(false, msg)
                    }
                } catch (e: Exception) {
                    onResult(false, e.message)
                }
            } else {
                onResult(false, "Request Failed, Try Again")
            }
        }
    }

    private suspend fun patchMatterStatus(matterId: String, newStatus: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
        val json = JSONObject()
        json.put("status", newStatus)
        val matterType = (Constants.MATTER_TYPE ?: "").lowercase(Locale.ROOT)
        val url = "v2/matter/$matterType/$matterId"
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            getApplication(),
            WebServiceHelper.RestMethodType.PATCH,
            url,
            "matter_update",
            json.toString()
        )
    }
}
