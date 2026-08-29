package com.digicoffer.lauditor.feature.relationships.data.repository

import android.content.Context
import android.view.View
import com.digicoffer.lauditor.Webservice.AsyncTaskCompleteListener
import com.digicoffer.lauditor.Webservice.HttpResultDo
import com.digicoffer.lauditor.Webservice.CommonApiHelper.WebServiceHelper
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume

class RelationshipsRepository(private val context: Context) {

    suspend fun getRelationships(
        tag: String,
        navPosition: String,
        searchQuery: String,
        anchorId: String
    ): HttpResultDo = suspendCancellableCoroutine { continuation ->
        val json = JSONObject()
        val paramName = when (navPosition) {
            "prev", "before" -> "before"
            "next", "after" -> "after"
            else -> ""
        }
        val endpoint = when (tag.lowercase()) {
            "corporate" -> {
                if (paramName.isNotEmpty() && anchorId.isNotEmpty()) {
                    if (searchQuery.isNotEmpty()) {
                        "v3/corporate?$paramName=$anchorId&paginate=true&search=$searchQuery"
                    } else {
                        "v3/corporate?$paramName=$anchorId&paginate=true"
                    }
                } else {
                    if (searchQuery.isNotEmpty()) {
                        "v3/corporate?paginate=true&search=$searchQuery"
                    } else if (anchorId.isNotEmpty()) {
                        "v3/corporate?paginate=true&anchor_id=$anchorId"
                    } else {
                        "v3/corporate?paginate=true"
                    }
                }
            }
            "tempclient", "temp" -> {
                if (paramName.isNotEmpty() && anchorId.isNotEmpty()) {
                    if (searchQuery.isNotEmpty()) {
                        "v3/tempclient?$paramName=$anchorId&paginate=true&search=$searchQuery"
                    } else {
                        "v3/tempclient?$paramName=$anchorId&paginate=true"
                    }
                } else {
                    if (searchQuery.isNotEmpty()) {
                        "v3/tempclient?paginate=true&search=$searchQuery"
                    } else if (anchorId.isNotEmpty()) {
                        "v3/tempclient?paginate=true&anchor_id=$anchorId"
                    } else {
                        "v3/tempclient?paginate=true"
                    }
                }
            }
            "deleted" -> {
                "v2/relationship/delete/list"
            }
            else -> {
                val relType = if (tag == "Individual") "individuals" else if (tag == "Entity") "business" else tag
                if (paramName.isNotEmpty() && anchorId.isNotEmpty()) {
                    if (searchQuery.isNotEmpty()) {
                        "v2/relationship/$relType?$paramName=$anchorId&paginate=true&search=$searchQuery"
                    } else {
                        "v2/relationship/$relType?$paramName=$anchorId&paginate=true"
                    }
                } else {
                    if (searchQuery.isNotEmpty()) {
                        "v2/relationship/$relType?paginate=true&search=$searchQuery"
                    } else if (anchorId.isNotEmpty()) {
                        "v2/relationship/$relType?paginate=true&anchor_id=$anchorId"
                    } else {
                        "v2/relationship/$relType?paginate=true"
                    }
                }
            }
        }

        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            context,
            WebServiceHelper.RestMethodType.GET,
            endpoint,
            "Get Relationships",
            json.toString()
        )
    }

    suspend fun getCountries(): HttpResultDo = suspendCancellableCoroutine { continuation ->
        val json = JSONObject()
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            context,
            WebServiceHelper.RestMethodType.GET,
            "countries",
            "COUNTRIES",
            json.toString()
        )
    }

    suspend fun getGroups(): HttpResultDo = suspendCancellableCoroutine { continuation ->
        val json = JSONObject()
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            context,
            WebServiceHelper.RestMethodType.GET,
            "v3/groups",
            "Get Groups",
            json.toString()
        )
    }

    suspend fun searchConsumer(searchText: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
        val json = JSONObject()
        json.put("searchText", searchText)
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            context,
            WebServiceHelper.RestMethodType.POST,
            "v3/relationship/search/consumer",
            "Search Consumer",
            json.toString()
        )
    }

    suspend fun sendRelationshipRequest(payload: JSONObject): HttpResultDo = suspendCancellableCoroutine { continuation ->
        val clientType = payload.optString("client_type")
        val isExisting = payload.has("consumerId") || payload.has("entityId")
        
        val url = when (clientType) {
            "individual" -> {
                if (isExisting) "v2/relationship/request/consumer" else "v2/relationship/invite/consumer"
            }
            "entity" -> {
                if (isExisting) "v2/relationship/request/entity" else "v2/relationship/invite/entity"
            }
            "corporate" -> {
                if (isExisting) "v3/corporate" else "v2/relationship/invite/entity"
            }
            else -> "v3/relationship/"
        }
        
        val cleanPayload = JSONObject()
        if (isExisting) {
            if (clientType == "individual") {
                cleanPayload.put("consumerId", payload.optString("consumerId"))
            } else {
                cleanPayload.put("entityId", payload.optString("entityId"))
            }
            cleanPayload.put("description", "Description")
        } else {
            cleanPayload.put("country", payload.optString("country"))
            cleanPayload.put("email", payload.optString("email"))
            if (clientType == "individual") {
                cleanPayload.put("first_name", payload.optString("first_name"))
                cleanPayload.put("last_name", payload.optString("last_name"))
                cleanPayload.put("mobile", payload.optString("mobile"))
            } else {
                cleanPayload.put("fullname", payload.optString("fullname"))
                cleanPayload.put("contact_person", payload.optString("contact_person"))
                cleanPayload.put("contact_phone", payload.optString("contact_phone"))
            }
        }

        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            context,
            WebServiceHelper.RestMethodType.POST,
            url,
            "Send Request",
            cleanPayload.toString()
        )
    }

    suspend fun getProfile(id: String, isCorporate: Boolean): HttpResultDo = suspendCancellableCoroutine { continuation ->
        val json = JSONObject()
        val endpoint = if (isCorporate) "v3/profile/$id" else "v2/relationship/$id/profile"
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            context,
            WebServiceHelper.RestMethodType.GET,
            endpoint,
            "Profile",
            json.toString()
        )
    }

    suspend fun getSharedDocuments(id: String, sharedTag: String, isCorporate: Boolean): HttpResultDo = suspendCancellableCoroutine { continuation ->
        val json = JSONObject()
        val endpoint = if (isCorporate) {
            "v3/share/$id/$sharedTag"
        } else {
            "v2/relationship/$id/docs/shared/$sharedTag"
        }
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            context,
            WebServiceHelper.RestMethodType.GET,
            endpoint,
            "Shared Documents",
            json.toString()
        )
    }

    suspend fun getFilterDocuments(payload: JSONObject): HttpResultDo = suspendCancellableCoroutine { continuation ->
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            context,
            WebServiceHelper.RestMethodType.PUT,
            "v3/document/filter",
            "Existing Documents",
            payload.toString()
        )
    }

    suspend fun shareDocuments(isCorporate: Boolean, relId: String, payload: JSONObject): HttpResultDo = suspendCancellableCoroutine { continuation ->
        val endpoint = if (isCorporate) "v3/share" else "v2/relationship/$relId/docs/share"
        val method = if (isCorporate) WebServiceHelper.RestMethodType.POST else WebServiceHelper.RestMethodType.PUT
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            context,
            method,
            endpoint,
            "ShareDocuments",
            payload.toString()
        )
    }

    suspend fun convertTempClient(clientId: String, clientType: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
        val json = JSONObject()
        json.put("client_id", clientId)
        json.put("client_type", clientType)
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            context,
            WebServiceHelper.RestMethodType.POST,
            "v3/convert-temp-clients",
            "Invite Temp Clients",
            json.toString()
        )
    }

    suspend fun deleteRelationship(id: String, isArchive: Boolean): HttpResultDo = suspendCancellableCoroutine { continuation ->
        val json = JSONObject()
        val endpoint = if (isArchive) {
            "v2/relationship/$id/archive"
        } else {
            "v2/relationship/$id/delete"
        }
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            context,
            WebServiceHelper.RestMethodType.DELETE,
            endpoint,
            "Delete_Relationship",
            json.toString()
        )
    }

    suspend fun updateGroups(id: String, groups: JSONArray): HttpResultDo = suspendCancellableCoroutine { continuation ->
        val json = JSONObject()
        json.put("groups", groups)
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            context,
            WebServiceHelper.RestMethodType.PATCH,
            "v3/relationship/groups/$id",
            "Update Groups",
            json.toString()
        )
    }

    suspend fun updateMembers(id: String, isCorporate: Boolean, users: JSONArray): HttpResultDo = suspendCancellableCoroutine { continuation ->
        val json = JSONObject()
        val endpoint: String
        val method: WebServiceHelper.RestMethodType
        if (isCorporate) {
            json.put("users", users)
            endpoint = "v3/relationship/$id/members"
            method = WebServiceHelper.RestMethodType.PATCH
        } else {
            json.put("members", users)
            endpoint = "v2/relationship/$id/members"
            method = WebServiceHelper.RestMethodType.PUT
        }
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            context,
            method,
            endpoint,
            "Update Members",
            json.toString()
        )
    }

    suspend fun restoreRelationship(id: String): HttpResultDo = suspendCancellableCoroutine { continuation ->
        val json = JSONObject()
        WebServiceHelper.callHttpWebService(
            object : AsyncTaskCompleteListener {
                override fun onAsyncTaskComplete(httpResult: HttpResultDo) {
                    continuation.resume(httpResult)
                }
                override fun onClick(view: View) {}
            },
            context,
            WebServiceHelper.RestMethodType.POST,
            "v2/relationship/$id/terminate/restore",
            "Archive Relationship",
            json.toString()
        )
    }
}
