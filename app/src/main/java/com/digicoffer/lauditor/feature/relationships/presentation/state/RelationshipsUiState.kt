package com.digicoffer.lauditor.feature.relationships.presentation.state

import com.digicoffer.lauditor.Groups.Models.ViewGroupModel
import com.digicoffer.lauditor.Relationships.Model.RelationshipsModel
import com.digicoffer.lauditor.Relationships.Model.CountriesDO
import com.digicoffer.lauditor.Relationships.Model.SharedDocumentsDo

data class RelationshipsUiState(
    val relationshipsList: List<RelationshipsModel> = emptyList(),
    val groupsList: List<ViewGroupModel> = emptyList(),
    val membersList: List<ViewGroupModel> = emptyList(),
    val sharedDocsList: List<SharedDocumentsDo> = emptyList(),
    val decryptedDocUrl: String? = null,
    val isLoading: Boolean = false,
    val toastMessage: String? = null,
    val searchResults: List<RelationshipsModel> = emptyList(),
    val countriesList: List<CountriesDO> = emptyList(),
    val nextCursor: String? = null,
    val prevCursor: String? = null
)
