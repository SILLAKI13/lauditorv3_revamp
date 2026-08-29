package com.digicoffer.lauditor.feature.relationships.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.activity.compose.BackHandler
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Relationships.Model.RelationshipsModel
import com.digicoffer.lauditor.feature.groups.presentation.components.CustomTextField
import com.digicoffer.lauditor.feature.members.presentation.components.MembersAlertDialog
import com.digicoffer.lauditor.core.ui.common.buttons.AppHeaderButton
import com.digicoffer.lauditor.core.ui.common.feedback.AppLoader
import com.digicoffer.lauditor.core.ui.common.search.AppSearchField
import com.digicoffer.lauditor.feature.notifications.presentation.components.NotificationsSearchBar
import com.digicoffer.lauditor.feature.relationships.presentation.components.ExchangeInfoDialog
import com.digicoffer.lauditor.feature.relationships.presentation.components.RelationshipCardItem
import com.digicoffer.lauditor.feature.relationships.presentation.components.RelationshipFormCard
import com.digicoffer.lauditor.feature.relationships.presentation.components.ShareDocsDialog
import com.digicoffer.lauditor.feature.relationships.presentation.state.RelationshipsUiEvent
import com.digicoffer.lauditor.feature.relationships.presentation.viewmodel.RelationshipsViewModel
import com.digicoffer.lauditor.Relationships.Model.SharedDocumentsDo
import androidx.compose.ui.platform.LocalContext
import org.json.JSONArray
import org.json.JSONObject

import kotlinx.coroutines.launch
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants

enum class ScreenMode {
    LIST,
    CREATE,
    EXCHANGE_INFO,
    SHARE_DOCS,
    MEMBER_ASSIGNMENT,
    GROUP_ASSIGNMENT,
    DELETE
}

private val GillSans = FontFamily(
    Font(R.font.gill_sans)
)

@Composable
fun RelationshipsScreen(
    viewModel: RelationshipsViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    onTitleChange: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    var screenMode by remember { mutableStateOf(ScreenMode.LIST) }

    BackHandler(enabled = screenMode != ScreenMode.LIST) {
        if (screenMode == ScreenMode.SHARE_DOCS) {
            screenMode = ScreenMode.EXCHANGE_INFO
        } else {
            screenMode = ScreenMode.LIST
        }
    }

    val context = LocalContext.current
    var shareCategory by remember { mutableStateOf("client") }

    // Read category from Constants.Rel_Type
    val currentRelType = remember { Constants.Rel_Type ?: "Individual" }

    // Search query on main directory
    var directorySearchQuery by remember { mutableStateOf("") }

    // Dialog state targets
    var activeRelationModel by remember { mutableStateOf<RelationshipsModel?>(null) }
    var validationAlertMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    val handleViewDoc: (SharedDocumentsDo, String) -> Unit = { doc, sharedTag ->
        val effContentType = getEffectiveContentType(doc)
        val isPdf = effContentType.equals("application/pdf", ignoreCase = true)
        val isImg = effContentType.startsWith("image/", ignoreCase = true)
        val isEncrypted = doc.added_encryption || doc.is_encrypted
        if (isEncrypted) {
            viewModel.onEvent(
                RelationshipsUiEvent.DecryptDocument(
                    docId = doc.id ?: "",
                    sharedDoc = (sharedTag == "withme")
                ) { success, decryptedUrl ->
                    if (success && decryptedUrl != null) {
                        coroutineScope.launch {
                            var finalUrl = decryptedUrl
                            if (!isPdf && !isImg) {
                                viewModel.onEvent(RelationshipsUiEvent.SetLoading(true))
                                val converted = callDoc2PdfApi(decryptedUrl, context)
                                viewModel.onEvent(RelationshipsUiEvent.SetLoading(false))
                                if (converted != null) {
                                    finalUrl = converted
                                }
                            }
                            displayDocument(context, doc, finalUrl)
                        }
                    }
                }
            )
        } else {
            val effectiveTag = if (!isPdf && !isImg) "byme" else sharedTag

            activeRelationModel?.let { model ->
                viewModel.onEvent(
                    RelationshipsUiEvent.ViewDocument(
                        docId = doc.id ?: "",
                        sharedTag = effectiveTag,
                        relId = model.id ?: "",
                        isCorporate = (currentRelType == "Corporate" || currentRelType == "Entity")
                    ) { success, viewUrl ->
                        if (success && viewUrl != null) {
                            coroutineScope.launch {
                                var finalUrl = viewUrl
                                if (!isPdf && !isImg) {
                                    viewModel.onEvent(RelationshipsUiEvent.SetLoading(true))
                                    val converted = callDoc2PdfApi(viewUrl, context)
                                    viewModel.onEvent(RelationshipsUiEvent.SetLoading(false))
                                    if (converted != null) {
                                        finalUrl = converted
                                    }
                                }
                                displayDocument(context, doc, finalUrl)
                            }
                        }
                    }
                )
            }
        }
    }
    
    // Member access checklist search
    var memberSearchQuery by remember { mutableStateOf("") }
    val selectedMemberIds = remember { mutableStateListOf<String>() }

    // Group access checklist search
    var groupSearchQuery by remember { mutableStateOf("") }
    val selectedGroupIds = remember { mutableStateListOf<String>() }

    LaunchedEffect(screenMode, currentRelType, directorySearchQuery) {
        if (screenMode == ScreenMode.LIST) {
            onTitleChange("View Relationships")
            viewModel.onEvent(
                RelationshipsUiEvent.FetchRelationships(
                    tag = currentRelType,
                    navPosition = "",
                    searchQuery = directorySearchQuery,
                    anchorId = ""
                )
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE4F2FF))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            if (screenMode == ScreenMode.LIST || screenMode == ScreenMode.CREATE) {
                Spacer(modifier = Modifier.height(12.dp))
            }

            when (screenMode) {
                ScreenMode.LIST -> {
                    val subheadTitle = when (currentRelType) {
                        "Individual" -> "View Relationships - Individual"
                        "Entity" -> "View Relationships - Business"
                        "Corporate" -> "View Relationships - Corporate"
                        "Deleted" -> "View Relationships - Deleted"
                        else -> "View Relationships - Individual"
                    }

                    // Subheader row: Title and Add Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = subheadTitle,
                            color = Color(0xFF004D87),
                            fontFamily = GillSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            lineHeight = 22.sp
                        )

                        AppHeaderButton(
                            text = "Add Relationships",
                            iconRes = R.drawable.simple_plus_icon,
                            iconContentDescription = "Add",
                            onClick = { screenMode = ScreenMode.CREATE }
                        )
                    }

                    // Unified Search Bar layout matching screenshot
                    AppSearchField(
                        value = directorySearchQuery,
                        onValueChange = { directorySearchQuery = it },
                        onSearchClick = { /* search query matches fetch */ },
                        placeholder = "Search Relationship",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
                    )

                    // Main Relationships directory list
                    if (uiState.relationshipsList.isEmpty() && !uiState.isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.empty_relationship),
                                    contentDescription = "No relationships",
                                    modifier = Modifier.size(130.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No Relationships Yet!",
                                    fontFamily = GillSans,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "There are no Relationships under this category.",
                                    fontFamily = GillSans,
                                    fontSize = 14.sp,
                                    color = Color(0xFF707070),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(uiState.relationshipsList) { model ->
                                RelationshipCardItem(
                                    model = model,
                                    onActionClick = { action, targetModel ->
                                        activeRelationModel = targetModel
                                        when (action) {
                                            "Exchange Information" -> screenMode = ScreenMode.EXCHANGE_INFO
                                            "Manage Groups" -> {
                                                selectedGroupIds.clear()
                                                targetModel.groups?.let { groupsArray ->
                                                    for (i in 0 until groupsArray.length()) {
                                                        val groupObj = groupsArray.optJSONObject(i)
                                                        if (groupObj != null) {
                                                            selectedGroupIds.add(groupObj.optString("id"))
                                                        } else {
                                                            val groupId = groupsArray.optString(i)
                                                            if (groupId.isNotEmpty()) {
                                                                selectedGroupIds.add(groupId)
                                                            }
                                                        }
                                                    }
                                                }
                                                screenMode = ScreenMode.GROUP_ASSIGNMENT
                                            }
                                            "Manage Team Members" -> {
                                                selectedMemberIds.clear()
                                                targetModel.membersList?.let { usersArray ->
                                                    for (i in 0 until usersArray.length()) {
                                                        selectedMemberIds.add(usersArray.optString(i))
                                                    }
                                                }
                                                screenMode = ScreenMode.MEMBER_ASSIGNMENT
                                            }
                                            "Delete Relationship" -> screenMode = ScreenMode.DELETE
                                            "Activate Relationship" -> {
                                                viewModel.onEvent(RelationshipsUiEvent.ActivateRelationship(targetModel.id ?: "") { success, msg ->
                                                    validationAlertMessage = msg
                                                    if (success) {
                                                        viewModel.onEvent(RelationshipsUiEvent.FetchRelationships(currentRelType, "", "", ""))
                                                    }
                                                })
                                            }
                                        }
                                    },
                                    onCardClick = { targetModel ->
                                        if (currentRelType.lowercase() != "deleted") {
                                            activeRelationModel = targetModel
                                            screenMode = ScreenMode.EXCHANGE_INFO
                                        }
                                    }
                                )
                            }

                            // Bottom Pagination Row as part of list scroll
                            if (uiState.nextCursor != null || uiState.prevCursor != null) {
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.White)
                                            .padding(horizontal = 10.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Button(
                                            onClick = {
                                                uiState.prevCursor?.let { cursor ->
                                                    viewModel.onEvent(
                                                        RelationshipsUiEvent.FetchRelationships(
                                                            tag = currentRelType,
                                                            navPosition = "prev",
                                                            searchQuery = directorySearchQuery,
                                                            anchorId = cursor
                                                        )
                                                    )
                                                }
                                            },
                                            enabled = uiState.prevCursor != null,
                                            modifier = Modifier.width(110.dp).height(40.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF004D87),
                                                disabledContainerColor = Color.LightGray
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text(
                                                text = "<< Prev",
                                                color = Color.White,
                                                fontFamily = GillSans,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Button(
                                            onClick = {
                                                uiState.nextCursor?.let { cursor ->
                                                    viewModel.onEvent(
                                                        RelationshipsUiEvent.FetchRelationships(
                                                            tag = currentRelType,
                                                            navPosition = "next",
                                                            searchQuery = directorySearchQuery,
                                                            anchorId = cursor
                                                        )
                                                    )
                                                }
                                            },
                                            enabled = uiState.nextCursor != null,
                                            modifier = Modifier.width(110.dp).height(40.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF004D87),
                                                disabledContainerColor = Color.LightGray
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text(
                                                text = "Next >>",
                                                color = Color.White,
                                                fontFamily = GillSans,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                ScreenMode.CREATE -> {
                    RelationshipFormCard(
                        countriesList = uiState.countriesList,
                        onCancel = { screenMode = ScreenMode.LIST },
                        onSubmitRequest = { payload, onResult ->
                            viewModel.onEvent(
                                RelationshipsUiEvent.SendRequest(payload) { success, msg ->
                                    onResult(success, msg)
                                }
                            )
                        },
                        onValidationError = { msg ->
                            validationAlertMessage = msg
                        },
                        onTitleChange = onTitleChange
                    )
                }

                ScreenMode.EXCHANGE_INFO -> {
                    activeRelationModel?.let { model ->
                        ExchangeInfoDialog(
                            model = model,
                            sharedDocs = uiState.sharedDocsList,
                            isLoading = uiState.isLoading,
                            isCorporate = (currentRelType == "Corporate" || currentRelType == "Entity"),
                            onDismiss = { screenMode = ScreenMode.LIST },
                            onLoadProfile = { onResult ->
                                viewModel.onEvent(
                                    RelationshipsUiEvent.LoadProfile(
                                        id = model.id ?: "",
                                        isCorporate = (currentRelType == "Corporate" || currentRelType == "Entity"),
                                        onResult = onResult
                                    )
                                )
                            },
                            onLoadDocs = { sharedTag ->
                                viewModel.onEvent(
                                    RelationshipsUiEvent.LoadSharedDocuments(
                                        id = model.id ?: "",
                                        sharedTag = sharedTag,
                                        isCorporate = (currentRelType == "Corporate" || currentRelType == "Entity")
                                    )
                                )
                            },
                            onUnshareDocs = { payload ->
                                viewModel.onEvent(
                                    RelationshipsUiEvent.UnshareDocuments(
                                        isCorporate = (currentRelType == "Corporate" || currentRelType == "Entity"),
                                        relId = model.id ?: "",
                                        payload = payload
                                    ) { success, msg ->
                                        validationAlertMessage = msg
                                        // Refresh list
                                        viewModel.onEvent(
                                            RelationshipsUiEvent.LoadSharedDocuments(
                                                id = model.id ?: "",
                                                sharedTag = "byme",
                                                isCorporate = (currentRelType == "Corporate" || currentRelType == "Entity")
                                            )
                                        )
                                    }
                                )
                            },
                            onShareClick = { category ->
                                shareCategory = category
                                screenMode = ScreenMode.SHARE_DOCS
                            },
                            onSearchDocs = { payload, onResult ->
                                viewModel.onEvent(
                                    RelationshipsUiEvent.SearchDocuments(payload) { results ->
                                        onResult(results)
                                    }
                                )
                            },
                            onShareDocs = { payload ->
                                viewModel.onEvent(
                                    RelationshipsUiEvent.ShareDocuments(
                                        isCorporate = (currentRelType == "Corporate" || currentRelType == "Entity"),
                                        relId = model.id ?: "",
                                        payload = payload
                                    ) { success, msg ->
                                        validationAlertMessage = msg
                                        // Refresh list
                                        viewModel.onEvent(
                                            RelationshipsUiEvent.LoadSharedDocuments(
                                                id = model.id ?: "",
                                                sharedTag = "byme",
                                                isCorporate = (currentRelType == "Corporate" || currentRelType == "Entity")
                                            )
                                        )
                                    }
                                )
                            },
                            onViewDoc = { doc, sharedTag ->
                                handleViewDoc(doc, sharedTag)
                            }
                        )
                    }
                }

                ScreenMode.SHARE_DOCS -> {
                    activeRelationModel?.let { model ->
                        ShareDocsDialog(
                            model = model,
                            isCorporate = (currentRelType == "Corporate" || currentRelType == "Entity"),
                            initialCategory = shareCategory,
                            onDismiss = { screenMode = ScreenMode.EXCHANGE_INFO },
                            onSearchDocs = { payload, onResult ->
                                viewModel.onEvent(
                                    RelationshipsUiEvent.SearchDocuments(payload) { results ->
                                        onResult(results)
                                    }
                                )
                            },
                            onShareDocs = { payload ->
                                viewModel.onEvent(
                                    RelationshipsUiEvent.ShareDocuments(
                                        isCorporate = (currentRelType == "Corporate" || currentRelType == "Entity"),
                                        relId = model.id ?: "",
                                        payload = payload
                                    ) { success, msg ->
                                        screenMode = ScreenMode.EXCHANGE_INFO
                                        validationAlertMessage = msg
                                    }
                                )
                            },
                            onViewDoc = { doc ->
                                handleViewDoc(doc, "byme")
                            }
                        )
                    }
                }

                else -> {}
            }
        }

        // Overlay dialogs
        if (screenMode == ScreenMode.MEMBER_ASSIGNMENT) {
            activeRelationModel?.let { model ->
                Dialog(onDismissRequest = { screenMode = ScreenMode.LIST }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .wrapContentHeight()
                            .border(width = 0.5.dp, color = Color(0xFFDDDDDE), shape = RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Update Member Access - ${model.name}",
                                    color = Color(0xFF004D87),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                IconButton(onClick = { screenMode = ScreenMode.LIST }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.outline_close),
                                        contentDescription = "Close",
                                        tint = Color.Black
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            CustomTextField(
                                value = memberSearchQuery,
                                onValueChange = { memberSearchQuery = it },
                                placeholder = "Search Member(S)"
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Checklist rows
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                val filteredMembers = uiState.groupsList.filter {
                                    memberSearchQuery.isEmpty() || it.name?.lowercase()?.contains(memberSearchQuery.lowercase()) == true
                                }
                                filteredMembers.forEach { member ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp)
                                            .border(
                                                width = 0.5.dp,
                                                color = Color(0xFFDDDDDE),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                if (selectedMemberIds.contains(member.id)) {
                                                    selectedMemberIds.remove(member.id)
                                                } else {
                                                    selectedMemberIds.add(member.id ?: "")
                                                }
                                            }
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = member.name ?: "", color = Color.Black, fontSize = 14.sp)
                                        Checkbox(
                                            checked = selectedMemberIds.contains(member.id),
                                            onCheckedChange = null,
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = Color(0xFF004D87),
                                                uncheckedColor = Color.Gray
                                            ),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Button(
                                    onClick = { screenMode = ScreenMode.LIST },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.width(100.dp)
                                ) {
                                    Text(text = "Cancel", color = Color.Black)
                                }

                                Button(
                                    onClick = {
                                        val usersArray = JSONArray()
                                        for (id in selectedMemberIds) {
                                            usersArray.put(id)
                                        }
                                        viewModel.onEvent(
                                            RelationshipsUiEvent.UpdateMembers(
                                                id = model.id ?: "",
                                                isCorporate = currentRelType == "Corporate",
                                                users = usersArray
                                            ) { success, msg ->
                                                screenMode = ScreenMode.LIST
                                                validationAlertMessage = msg
                                                // Refresh list
                                                viewModel.onEvent(
                                                    RelationshipsUiEvent.FetchRelationships(
                                                        tag = currentRelType,
                                                        navPosition = "",
                                                        searchQuery = directorySearchQuery,
                                                        anchorId = ""
                                                    )
                                                )
                                            }
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D87)),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.width(100.dp)
                                ) {
                                    Text(text = "Save", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (screenMode == ScreenMode.GROUP_ASSIGNMENT) {
            activeRelationModel?.let { model ->
                Dialog(onDismissRequest = { screenMode = ScreenMode.LIST }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .wrapContentHeight()
                            .border(width = 0.5.dp, color = Color(0xFFDDDDDE), shape = RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Modify Group Access - ${model.name}",
                                    color = Color(0xFF004D87),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                IconButton(onClick = { screenMode = ScreenMode.LIST }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.outline_close),
                                        contentDescription = "Close",
                                        tint = Color.Black
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            CustomTextField(
                                value = groupSearchQuery,
                                onValueChange = { groupSearchQuery = it },
                                placeholder = "Search Group(S)"
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Checklist rows
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                val filteredGroups = uiState.groupsList.filter {
                                    groupSearchQuery.isEmpty() || it.name?.lowercase()?.contains(groupSearchQuery.lowercase()) == true
                                }
                                filteredGroups.forEach { group ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp)
                                            .border(
                                                width = 0.5.dp,
                                                color = Color(0xFFDDDDDE),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                if (selectedGroupIds.contains(group.id)) {
                                                    selectedGroupIds.remove(group.id)
                                                } else {
                                                    selectedGroupIds.add(group.id ?: "")
                                                }
                                            }
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = group.name ?: "", color = Color.Black, fontSize = 14.sp)
                                        Checkbox(
                                            checked = selectedGroupIds.contains(group.id),
                                            onCheckedChange = null,
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = Color(0xFF004D87),
                                                uncheckedColor = Color.Gray
                                            ),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Button(
                                    onClick = { screenMode = ScreenMode.LIST },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.width(100.dp)
                                ) {
                                    Text(text = "Cancel", color = Color.Black)
                                }

                                Button(
                                    onClick = {
                                        val groupsArray = JSONArray()
                                        for (id in selectedGroupIds) {
                                            groupsArray.put(id)
                                        }
                                        viewModel.onEvent(
                                            RelationshipsUiEvent.UpdateGroups(
                                                id = model.id ?: "",
                                                groups = groupsArray
                                            ) { success, msg ->
                                                screenMode = ScreenMode.LIST
                                                validationAlertMessage = msg
                                                // Refresh list
                                                viewModel.onEvent(
                                                    RelationshipsUiEvent.FetchRelationships(
                                                        tag = currentRelType,
                                                        navPosition = "",
                                                        searchQuery = directorySearchQuery,
                                                        anchorId = ""
                                                    )
                                                )
                                            }
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D87)),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.width(100.dp)
                                ) {
                                    Text(text = "Save", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (screenMode == ScreenMode.DELETE) {
            activeRelationModel?.let { model ->
                Dialog(onDismissRequest = { screenMode = ScreenMode.LIST }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .wrapContentHeight()
                            .border(width = 0.5.dp, color = Color(0xFFDDDDDE), shape = RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Confirmation",
                                    color = Color(0xFF004D87),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                IconButton(
                                    onClick = { screenMode = ScreenMode.LIST },
                                    modifier = Modifier.align(Alignment.CenterEnd)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.outline_close),
                                        contentDescription = "Close",
                                        tint = Color.Black
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            val warningText = androidx.compose.ui.text.buildAnnotatedString {
                                append("Are you sure you want to delete the relationship request sent to ")
                                withStyle(style = androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(model.name ?: "")
                                }
                                append("?")
                            }
                            Text(
                                text = warningText,
                                color = Color.Black,
                                fontSize = 15.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Button(
                                    onClick = { screenMode = ScreenMode.LIST },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D87)),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.width(100.dp)
                                ) {
                                    Text(text = "No", color = Color.White)
                                }

                                Button(
                                    onClick = {
                                        viewModel.onEvent(
                                            RelationshipsUiEvent.DeleteRelationship(
                                                id = model.id ?: "",
                                                isArchive = currentRelType == "Corporate"
                                            ) { success, msg ->
                                                screenMode = ScreenMode.LIST
                                                validationAlertMessage = msg
                                                // Refresh directory
                                                viewModel.onEvent(
                                                    RelationshipsUiEvent.FetchRelationships(
                                                        tag = currentRelType,
                                                        navPosition = "",
                                                        searchQuery = directorySearchQuery,
                                                        anchorId = ""
                                                    )
                                                )
                                            }
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.width(100.dp)
                                ) {
                                    Text(text = "Yes", color = Color.Black)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Standard Alert dialogue warning messages
        validationAlertMessage?.let { msg ->
            MembersAlertDialog(
                title = "Alert !",
                message = msg,
                onConfirm = { validationAlertMessage = null },
                onDismiss = { validationAlertMessage = null }
            )
        }

        // Circular Loading Overlay
        if (uiState.isLoading) {
            AppLoader()
        }
    }
}

fun getEffectiveContentType(doc: SharedDocumentsDo): String {
    val filename = doc.filename ?: ""
    val name = doc.name ?: ""
    val extension = if (filename.contains('.')) {
        filename.substringAfterLast('.', "").lowercase(java.util.Locale.getDefault())
    } else if (name.contains('.')) {
        name.substringAfterLast('.', "").lowercase(java.util.Locale.getDefault())
    } else {
        ""
    }
    if (extension.isNotEmpty()) {
        val imgExts = listOf("apng", "avif", "gif", "jpeg", "png", "svg", "webp", "jpg")
        return if (imgExts.contains(extension)) {
            "image/$extension"
        } else if (extension == "pdf") {
            "application/pdf"
        } else {
            "application/$extension"
        }
    }
    return doc.content_type ?: ""
}

fun displayDocument(context: android.content.Context, doc: SharedDocumentsDo, url: String) {
    val activity = context as? androidx.fragment.app.FragmentActivity ?: return
    val dialogBuilder = android.app.AlertDialog.Builder(activity)
    val inflater = activity.layoutInflater
    val view = inflater.inflate(R.layout.view_documents, null)
    val progressBar = view.findViewById<android.widget.ProgressBar>(R.id.progress_pdf)
    val iv_image = view.findViewById<android.widget.ImageView>(R.id.doc_image)
    val idPDFView = view.findViewById<com.github.barteksc.pdfviewer.PDFView>(R.id.idPDFView)
    val header = view.findViewById<android.widget.TextView>(R.id.header_name)
    val iv_close_edit_docs = view.findViewById<android.widget.ImageView>(R.id.close_edit_docs)
    header.text = doc.name
    val dialog = dialogBuilder.create()
    val pdfTask = arrayOfNulls<com.digicoffer.lauditor.CommonFiles.PdfUtils.RetrievePDFfromUrl>(1)

    iv_close_edit_docs.setOnClickListener {
        try {
            idPDFView?.recycle()
            pdfTask[0]?.cancelLoading()
            pdfTask[0]?.cancel(true)
        } catch (ignored: Exception) {
        }
        dialog.dismiss()
    }
    val lowerUrl = url.lowercase(java.util.Locale.getDefault())
    val urlIsPDF = lowerUrl.contains("application/pdf") || lowerUrl.contains(".pdf") || url.startsWith("localfile://")
    val effContentType = getEffectiveContentType(doc)
    val isImage = effContentType.startsWith("image/", ignoreCase = true)
    if (urlIsPDF) {
        idPDFView.visibility = android.view.View.VISIBLE
        progressBar.visibility = android.view.View.VISIBLE
        if (url.startsWith("localfile://")) {
            val localPath = url.replace("localfile://", "")
            idPDFView.fromFile(java.io.File(localPath)).load()
            progressBar.visibility = android.view.View.GONE
        } else {
            pdfTask[0] = com.digicoffer.lauditor.CommonFiles.PdfUtils.RetrievePDFfromUrl(idPDFView, progressBar)
            pdfTask[0]?.execute(url)
        }
    } else {
        if (isImage) {
            iv_image.visibility = android.view.View.VISIBLE
            com.bumptech.glide.Glide.with(context)
                .load(url)
                .placeholder(R.drawable.progress_animation)
                .centerCrop()
                .into(iv_image)
        } else {
            idPDFView.visibility = android.view.View.VISIBLE
            progressBar.visibility = android.view.View.VISIBLE
            pdfTask[0] = com.digicoffer.lauditor.CommonFiles.PdfUtils.RetrievePDFfromUrl(idPDFView, progressBar)
            pdfTask[0]?.execute(url)
        }
    }
    dialog.setCancelable(false)
    dialog.setCanceledOnTouchOutside(false)
    dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    dialog.setView(view)
    dialog.show()
}

suspend fun callDoc2PdfApi(fileUrl: String, context: android.content.Context): String? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
    try {
        val apiUrl = java.net.URL(com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.doctopdfUrl)
        val conn = apiUrl.openConnection() as java.net.HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "Bearer " + com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.TOKEN)
        conn.doOutput = true
        conn.connectTimeout = 15000
        conn.readTimeout = 60000

        val body = org.json.JSONObject()
        body.put("url", fileUrl)
        val input = body.toString().toByteArray(charset("utf-8"))
        conn.outputStream.write(input, 0, input.size)
        conn.connect()

        val code = conn.responseCode
        val ctHdr = conn.contentType
        if (code == 200) {
            if (ctHdr != null && ctHdr.contains("application/pdf")) {
                val pdfFile = java.io.File.createTempFile("doc2pdf_" + System.currentTimeMillis(), ".pdf", context.cacheDir)
                java.io.BufferedInputStream(conn.inputStream).use { `in` ->
                    java.io.FileOutputStream(pdfFile).use { fo ->
                        val buf = ByteArray(4096)
                        var n: Int
                        while (`in`.read(buf).also { n = it } != -1) {
                            fo.write(buf, 0, n)
                        }
                    }
                }
                return@withContext "localfile://" + pdfFile.absolutePath
            } else {
                val reader = java.io.BufferedReader(java.io.InputStreamReader(conn.inputStream))
                val sb = java.lang.StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                reader.close()
                val resp = org.json.JSONObject(sb.toString())
                if (!resp.optBoolean("error", true)) {
                    val data = resp.optJSONObject("data")
                    if (data != null) return@withContext data.optString("url")
                }
            }
        }
    } catch (e: Exception) {
        android.util.Log.e("FetchUrl", "doc2pdf failed: " + e.message)
    }
    return@withContext null
}

