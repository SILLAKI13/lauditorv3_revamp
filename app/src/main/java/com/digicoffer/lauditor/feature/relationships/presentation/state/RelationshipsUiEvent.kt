package com.digicoffer.lauditor.feature.relationships.presentation.state

import org.json.JSONArray
import org.json.JSONObject
import com.digicoffer.lauditor.Relationships.Model.SharedDocumentsDo

sealed interface RelationshipsUiEvent {
    data class FetchRelationships(
        val tag: String,
        val navPosition: String,
        val searchQuery: String,
        val anchorId: String
    ) : RelationshipsUiEvent

    data class SearchConsumer(val searchText: String) : RelationshipsUiEvent

    data class SendRequest(val payload: JSONObject, val onResult: (Boolean, String) -> Unit) : RelationshipsUiEvent

    data class LoadProfile(val id: String, val isCorporate: Boolean, val onResult: (JSONObject?) -> Unit) : RelationshipsUiEvent

    data class LoadSharedDocuments(
        val id: String,
        val sharedTag: String,
        val isCorporate: Boolean
    ) : RelationshipsUiEvent

    data class ShareDocuments(
        val isCorporate: Boolean,
        val relId: String,
        val payload: JSONObject,
        val onResult: (Boolean, String) -> Unit
    ) : RelationshipsUiEvent

    data class UnshareDocuments(
        val isCorporate: Boolean,
        val relId: String,
        val payload: JSONObject,
        val onResult: (Boolean, String) -> Unit
    ) : RelationshipsUiEvent

    data class ConvertTempClient(
        val clientId: String,
        val clientType: String,
        val onResult: (Boolean, String) -> Unit
    ) : RelationshipsUiEvent

    data class DeleteRelationship(
        val id: String,
        val isArchive: Boolean,
        val onResult: (Boolean, String) -> Unit
    ) : RelationshipsUiEvent

    data class UpdateGroups(
        val id: String,
        val groups: JSONArray,
        val onResult: (Boolean, String) -> Unit
    ) : RelationshipsUiEvent

    data class UpdateMembers(
        val id: String,
        val isCorporate: Boolean,
        val users: JSONArray,
        val onResult: (Boolean, String) -> Unit
    ) : RelationshipsUiEvent

    data class ViewDocument(
        val docId: String,
        val sharedTag: String,
        val relId: String,
        val isCorporate: Boolean,
        val onResult: (Boolean, String?) -> Unit
    ) : RelationshipsUiEvent

    data class DecryptDocument(
        val docId: String,
        val sharedDoc: Boolean,
        val onResult: (Boolean, String?) -> Unit
    ) : RelationshipsUiEvent

    data class SearchDocuments(
        val payload: JSONObject,
        val onResult: (List<SharedDocumentsDo>) -> Unit
    ) : RelationshipsUiEvent
}
