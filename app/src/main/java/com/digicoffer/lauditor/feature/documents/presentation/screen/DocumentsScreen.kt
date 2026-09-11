package com.digicoffer.lauditor.feature.documents.presentation.screen

import android.app.AlertDialog
import android.content.Context
import java.io.File
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import com.digicoffer.lauditor.core.ui.common.animation.fallDownItem
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.bumptech.glide.Glide
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Documents.Models.ClientsModel
import com.digicoffer.lauditor.Documents.Models.GroupsModel
import com.digicoffer.lauditor.Documents.Models.MattersModel
import com.digicoffer.lauditor.Documents.Models.ViewDocumentsModel
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme
import com.digicoffer.lauditor.core.ui.common.buttons.AppButton
import com.digicoffer.lauditor.core.ui.common.buttons.AppHeaderButton
import com.digicoffer.lauditor.core.ui.common.cards.AppCard
import com.digicoffer.lauditor.core.ui.common.dialogs.AppConfirmationDialog
import com.digicoffer.lauditor.core.ui.common.dialogs.AppDialog
import com.digicoffer.lauditor.core.ui.common.dropdowns.DropdownSelectorField
import com.digicoffer.lauditor.core.ui.common.search.AppSearchField
import com.digicoffer.lauditor.core.ui.common.feedback.AppEmptyState
import com.digicoffer.lauditor.core.ui.common.feedback.AppLoader
import com.digicoffer.lauditor.core.ui.common.foundation.AppDivider
import com.digicoffer.lauditor.core.ui.common.foundation.AppSpacer
import com.digicoffer.lauditor.core.ui.common.foundation.AppText
import com.digicoffer.lauditor.core.ui.common.inputs.AppTextField
import com.digicoffer.lauditor.feature.documents.presentation.state.DocumentsUiEvent
import com.digicoffer.lauditor.feature.documents.presentation.viewmodel.DocumentsViewModel
import com.github.barteksc.pdfviewer.PDFView
import java.util.Locale

private val GillSans = FontFamily(
    Font(R.font.gill_sans)
)

@Composable
fun DocumentsScreen(
    viewModel: DocumentsViewModel,
    onTitleChanged: (String) -> Unit = {},
    onBrowseClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeletedBanner by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(uiState.currentTab, uiState.isUploadMode) {
        val title = when {
            uiState.currentTab == "delete" -> "Deleted Documents"
            uiState.isUploadMode -> "Upload New"
            else -> "Document View"
        }
        onTitleChanged(title)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE6F0FA)) // Pale blue background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 15.dp, vertical = 8.dp)
        ) {
            if (uiState.currentTab != "delete") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    AppHeaderButton(
                        text = if (uiState.isUploadMode) "Document View" else "Upload New",
                        iconRes = if (uiState.isUploadMode) R.drawable.eye_icon else R.drawable.simple_plus_icon,
                        onClick = { viewModel.onEvent(DocumentsUiEvent.ToggleUploadMode(!uiState.isUploadMode)) }
                    )
                }
            }

            if (uiState.isUploadMode) {
                // RENDER UPLOAD FORM VIEW
                DocumentsUploadTab(uiState = uiState, onEvent = viewModel::onEvent, onBrowseClick = onBrowseClick)
            } else {
                // RENDER FILTER DROPDOWNS & LISTINGS VIEW
                if (uiState.currentTab == "delete" &&
                    "solo" == com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.CATEGORY &&
                    com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.ROLE == "SU" &&
                    showDeletedBanner
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF004D87))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_warning_yellow),
                                    contentDescription = "Warning",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = buildAnnotatedString {
                                        append("Documents will be permanently deleted in ")
                                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                            append("30 days")
                                        }
                                    },
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontFamily = GillSans
                                )
                            }
                            IconButton(
                                onClick = { showDeletedBanner = false },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.cancel_white_icon),
                                    contentDescription = "Close Banner",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
                // Filters block
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        // Title bar inside the Card (matching legacy cv_view_details title_bar)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val title = when (uiState.currentTab) {
                                "firm" -> "List of Firm documents"
                                "client" -> "List of Client documents"
                                "delete" -> if ("solo" == com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.CATEGORY) "Deleted Documents" else "List of Documents Pending Approval"
                                else -> "List of Matter documents"
                            }
                            Text(
                                text = title,
                                fontSize = 16.sp,
                                fontFamily = GillSans,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF004D87),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                // List Layout toggler
                                IconButton(
                                    onClick = { viewModel.onEvent(DocumentsUiEvent.ToggleGridView(false)) },
                                    modifier = Modifier
                                        .background(
                                            if (!uiState.isGridView) Color(0xFF004D87) else Color(0xFFDDDDDE),
                                            shape = RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)
                                        )
                                        .size(36.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.white_list),
                                        contentDescription = "List View",
                                        tint = if (!uiState.isGridView) Color.White else Color(0xFF585858),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // Grid Layout toggler
                                IconButton(
                                    onClick = { viewModel.onEvent(DocumentsUiEvent.ToggleGridView(true)) },
                                    modifier = Modifier
                                        .background(
                                            if (uiState.isGridView) Color(0xFF004D87) else Color(0xFFDDDDDE),
                                            shape = RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)
                                        )
                                        .size(36.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.list_view),
                                        contentDescription = "Grid View",
                                        tint = if (uiState.isGridView) Color.White else Color(0xFF585858),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        AppSpacer(height = 10.dp)

                        when (uiState.currentTab) {
                            "matter" -> {
                                Text(
                                    text = buildAnnotatedString {
                                        withStyle(SpanStyle(color = Color(0xFF004D87), fontWeight = FontWeight.Normal, fontFamily = GillSans)) {
                                            append("Matters")
                                        }
                                        withStyle(SpanStyle(color = Color.Red, fontWeight = FontWeight.Normal)) {
                                            append(" *")
                                        }
                                    },
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                DropdownSelectorField(
                                    items = uiState.mattersList,
                                    selectedItem = uiState.selectedFilterMatter,
                                    onItemSelected = { m ->
                                        viewModel.onEvent(DocumentsUiEvent.SelectFilterMatter(m))
                                    },
                                    itemToLabel = { it.name ?: "" },
                                    placeholder = "All Matters",
                                    onClearSelection = if (uiState.selectedFilterMatter != uiState.mattersList.firstOrNull()) {
                                        {
                                            uiState.mattersList.firstOrNull()?.let {
                                                viewModel.onEvent(DocumentsUiEvent.SelectFilterMatter(it))
                                            }
                                        }
                                    } else null
                                )
                            }
                            "client" -> {
                                Text(
                                    text = buildAnnotatedString {
                                        withStyle(SpanStyle(color = Color(0xFF004D87), fontWeight = FontWeight.Normal, fontFamily = GillSans)) {
                                            append("Client Name")
                                        }
                                        withStyle(SpanStyle(color = Color.Red, fontWeight = FontWeight.Normal)) {
                                            append(" *")
                                        }
                                    },
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                DropdownSelectorField(
                                    items = uiState.clientsList,
                                    selectedItem = uiState.selectedFilterClient,
                                    onItemSelected = { c ->
                                        viewModel.onEvent(DocumentsUiEvent.SelectFilterClient(c))
                                    },
                                    itemToLabel = { it.name ?: "" },
                                    placeholder = "All Clients",
                                    onClearSelection = if (uiState.selectedFilterClient != uiState.clientsList.firstOrNull()) {
                                        {
                                            uiState.clientsList.firstOrNull()?.let {
                                                viewModel.onEvent(DocumentsUiEvent.SelectFilterClient(it))
                                            }
                                        }
                                    } else null
                                )
                            }
                            "firm" -> {
                                Text(
                                    text = buildAnnotatedString {
                                        withStyle(SpanStyle(color = Color(0xFF004D87), fontWeight = FontWeight.Normal, fontFamily = GillSans)) {
                                            append("Select Group(s)")
                                        }
                                        withStyle(SpanStyle(color = Color.Red, fontWeight = FontWeight.Normal)) {
                                            append(" *")
                                        }
                                    },
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                var firmGroupsExpanded by remember { mutableStateOf(false) }

                                val selectedText = if (uiState.selectedFilterGroups.any { it.name == "All Groups" }) {
                                    "All Groups"
                                } else if (uiState.selectedFilterGroups.isNotEmpty()) {
                                    uiState.selectedFilterGroups.joinToString(", ") { it.name ?: "" }
                                } else {
                                    "All Groups"
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .background(Color(0xFFF9FAFB), RoundedCornerShape(6.dp))
                                        .border(0.5.dp, Color(0xFFC0C0C0), RoundedCornerShape(6.dp))
                                        .clickable { firmGroupsExpanded = !firmGroupsExpanded },
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedText,
                                        fontFamily = GillSans,
                                        fontSize = 15.sp,
                                        color = Color.Black,
                                        maxLines = 1,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(start = 10.dp)
                                    )
                                    Icon(
                                        painter = painterResource(id = R.drawable.drop_down_blue),
                                        contentDescription = "Dropdown Arrow",
                                        tint = Color.Unspecified,
                                        modifier = Modifier
                                            .padding(end = 15.dp)
                                            .size(12.dp)
                                    )
                                }

                                if (firmGroupsExpanded && uiState.groupsList.isNotEmpty()) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp)
                                            .heightIn(max = 160.dp)
                                            .background(Color.White, RoundedCornerShape(6.dp))
                                            .border(0.5.dp, Color(0xFFC0C0C0), RoundedCornerShape(6.dp))
                                            .padding(6.dp)
                                            .verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        val allGroupsItem = uiState.groupsList.firstOrNull { it.name == "All Groups" }
                                        uiState.groupsList.forEach { grp ->
                                            val isAllGroups = grp.name == "All Groups"
                                            val isChecked = if (isAllGroups) {
                                                uiState.selectedFilterGroups.any { it.name == "All Groups" }
                                            } else {
                                                !uiState.selectedFilterGroups.any { it.name == "All Groups" } &&
                                                        uiState.selectedFilterGroups.any { it.id == grp.id }
                                            }

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        val nextList = if (isAllGroups) {
                                                            if (isChecked) emptyList() else listOf(grp)
                                                        } else {
                                                            val cur = uiState.selectedFilterGroups.filter { it.name != "All Groups" }.toMutableList()
                                                            if (cur.any { it.id == grp.id }) {
                                                                cur.removeAll { it.id == grp.id }
                                                            } else {
                                                                cur.add(grp)
                                                            }
                                                            if (cur.isEmpty() && allGroupsItem != null) {
                                                                listOf(allGroupsItem)
                                                            } else {
                                                                cur
                                                            }
                                                        }
                                                        viewModel.onEvent(DocumentsUiEvent.SelectFilterGroups(nextList))
                                                    }
                                                    .padding(vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(
                                                    checked = isChecked,
                                                    onCheckedChange = { checked ->
                                                        val nextList = if (isAllGroups) {
                                                            if (checked) listOf(grp) else emptyList()
                                                        } else {
                                                            val cur = uiState.selectedFilterGroups.filter { it.name != "All Groups" }.toMutableList()
                                                            if (checked) {
                                                                cur.add(grp)
                                                            } else {
                                                                cur.removeAll { it.id == grp.id }
                                                            }
                                                            if (cur.isEmpty() && allGroupsItem != null) {
                                                                listOf(allGroupsItem)
                                                            } else {
                                                                cur
                                                            }
                                                        }
                                                        viewModel.onEvent(DocumentsUiEvent.SelectFilterGroups(nextList))
                                                    },
                                                    colors = CheckboxDefaults.colors(
                                                        checkedColor = Color(0xFF004D87),
                                                        checkmarkColor = Color.White
                                                    )
                                                )
                                                Text(
                                                    text = grp.name ?: "",
                                                    fontFamily = GillSans,
                                                    fontSize = 14.sp,
                                                    color = Color.Black
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            "delete" -> {
                                Text(
                                    text = "Select Document Type",
                                    fontSize = 14.sp,
                                    fontFamily = GillSans,
                                    color = Color(0xFF004D87),
                                    fontWeight = FontWeight.Normal,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                val deleteDocTypes = if ("solo" == com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.CATEGORY) {
                                    listOf("Matter", "Client")
                                } else {
                                    listOf("Matter", "Client", "Firm")
                                }
                                DropdownSelectorField(
                                    items = deleteDocTypes,
                                    selectedItem = uiState.selectedFilterDocType?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                                    onItemSelected = { name ->
                                        viewModel.onEvent(DocumentsUiEvent.SelectFilterDocType(name.lowercase(Locale.ROOT)))
                                    },
                                    itemToLabel = { it },
                                    placeholder = "Select Document Type",
                                    onClearSelection = if (uiState.selectedFilterDocType != null) {
                                        {
                                            viewModel.onEvent(DocumentsUiEvent.SelectFilterDocType(null))
                                        }
                                    } else null
                                )
                            }
                        }

                        AppSpacer(height = 10.dp)

                        // Search field (borderless card view style matching search_layout_new.xml)
                        AppSearchField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.onEvent(DocumentsUiEvent.SetSearchQuery(it)) },
                            placeholder = "Search",
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }

                AppSpacer(height = 10.dp)

                if (uiState.filteredDocumentsList.isEmpty()) {
                    // Render exact legacy empty states
                    val emptyTitle = when (uiState.currentTab) {
                        "firm" -> "No Firm Documents Yet!"
                        "client" -> "No Client Documents Yet!"
                        "delete" -> if ("solo" == com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.CATEGORY) "No Deleted Documents Yet!" else "No Documents Pending Approval Yet!"
                        else -> "No Matter Documents Yet!"
                    }
                    val emptySubtitle = when (uiState.currentTab) {
                        "delete" -> if ("solo" == com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.CATEGORY) "Deleted documents will appear here" else "Documents pending approval will appear here"
                        else -> "Secure and organize your documents by start uploading it."
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        AppEmptyState(
                            title = emptyTitle,
                            description = emptySubtitle,
                            imageRes = R.drawable.empty_doc,
                            imageSize = 120.dp,
                            titleColor = Color(0xFF004D87),
                            titleStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = GillSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            descriptionColor = Color(0xFF555555),
                            descriptionStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = GillSans,
                                fontSize = 14.sp
                            )
                        )
                        val dbg = uiState.debugInfo
                        if (uiState.currentTab == "client" && dbg != null) {
                            AppSpacer(height = 10.dp)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(8.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    Text(
                                        text = dbg,
                                        color = Color.Green,
                                        fontSize = 11.sp,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                } else {
                    val totalPages = kotlin.math.ceil(uiState.filteredDocumentsList.size.toDouble() / uiState.itemsPerPage).toInt()
                    val startIndex = (uiState.currentPage - 1) * uiState.itemsPerPage
                    val endIndex = minOf(startIndex + uiState.itemsPerPage, uiState.filteredDocumentsList.size)
                    val paginatedDocs = if (startIndex < uiState.filteredDocumentsList.size) {
                        uiState.filteredDocumentsList.subList(startIndex, endIndex)
                    } else {
                        emptyList()
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        if (uiState.isGridView) {
                            LazyVerticalStaggeredGrid(
                                columns = StaggeredGridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalItemSpacing = 8.dp,
                                contentPadding = PaddingValues(bottom = 8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                itemsIndexed(paginatedDocs, key = { _, doc -> doc.id ?: "" }) { index, doc ->
                                    ViewDocumentItemGrid(
                                        doc = doc,
                                        currentTab = uiState.currentTab,
                                        previewUrl = uiState.previewUrls[doc.id ?: ""],
                                        previewBitmap = uiState.previewBitmaps[doc.id ?: ""],
                                        isLoadingPreview = uiState.loadingPreviewIds.contains(doc.id ?: ""),
                                        isFailedPreview = uiState.failedPreviewIds.contains(doc.id ?: ""),
                                        onAction = { action ->
                                            viewModel.onEvent(DocumentsUiEvent.TriggerAction(action, doc))
                                        },
                                        onLoadPreview = {
                                            viewModel.onEvent(DocumentsUiEvent.LoadPreview(doc))
                                        },
                                        modifier = Modifier.fallDownItem(
                                            index = index,
                                            triggerKey = Pair(uiState.currentPage, uiState.isGridView)
                                        )
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(bottom = 8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                itemsIndexed(paginatedDocs, key = { _, doc -> doc.id ?: "" }) { index, doc ->
                                    ViewDocumentItemCard(
                                        doc = doc,
                                        currentTab = uiState.currentTab,
                                        onAction = { action ->
                                            viewModel.onEvent(DocumentsUiEvent.TriggerAction(action, doc))
                                        },
                                        modifier = Modifier.fallDownItem(
                                            index = index,
                                            triggerKey = Pair(uiState.currentPage, uiState.isGridView)
                                        )
                                    )
                                }
                            }
                        }
                    }

                    if (totalPages > 1) {
                        PaginationBar(
                            currentPage = uiState.currentPage,
                            totalPages = totalPages,
                            onPrev = { viewModel.onEvent(DocumentsUiEvent.PagePrev) },
                            onNext = { viewModel.onEvent(DocumentsUiEvent.PageNext) }
                        )
                    }
                }
            }
        }

        // Overlay loaders & Snackbars
        if (uiState.isLoading || uiState.isUploading) {
            AppLoader(modifier = Modifier.align(Alignment.Center))
        }
    }

    // Dialog overlays
    uiState.confirmActionType?.let { actionType ->
        AppConfirmDialog(
            actionType = actionType,
            doc = uiState.confirmDocModel,
            onConfirm = { viewModel.onEvent(DocumentsUiEvent.ExecuteConfirmAction) },
            onDismiss = { viewModel.onEvent(DocumentsUiEvent.CloseConfirmAction) }
        )
    }

    uiState.editDocModel?.let { doc ->
        EditMetadataDialog(
            doc = doc,
            initialDownloadDisabled = doc.isdisabled == true || doc.is_disabled == true,
            initialEncrypted = doc.is_encrypted == true || doc.added_encryption == true,
            isStaged = false,
            onSave = { name, desc, exp, _, _, tags ->
                viewModel.onEvent(DocumentsUiEvent.SaveMetadata(doc.id ?: "", name, desc, exp, tags))
            },
            onDismiss = { viewModel.onEvent(DocumentsUiEvent.CloseEditMetadata) }
        )
    }

    uiState.tagsDocModel?.let { doc ->
        UpdateTagsDialog(
            doc = doc,
            onSave = { name, tags ->
                viewModel.onEvent(DocumentsUiEvent.SaveTags(doc.id ?: "", name, tags))
            },
            onDismiss = { viewModel.onEvent(DocumentsUiEvent.CloseUpdateTags) }
        )
    }

    uiState.previewDocUrl?.let { url ->
        DocumentViewerDialog(
            url = url,
            docName = uiState.previewDocModel?.name ?: "Preview",
            contentType = uiState.previewDocModel?.content_type ?: "",
            onDismiss = { viewModel.onEvent(DocumentsUiEvent.ClosePreview) }
        )
    }

    if (uiState.toastMessage != null) {
        AppDialog(
            title = "Information",
            onConfirm = { viewModel.onEvent(DocumentsUiEvent.DismissToast) },
            onDismiss = { viewModel.onEvent(DocumentsUiEvent.DismissToast) },
            confirmText = "OK",
            dismissText = null,
            content = {
                Text(
                    text = uiState.toastMessage ?: "",
                    fontFamily = GillSans,
                    fontSize = 14.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }
        )
    }

    // Alert dialogs
    if (uiState.alertMessage != null) {
        AppDialog(
            title = uiState.alertTitle ?: "Alert !",
            onConfirm = { viewModel.onEvent(DocumentsUiEvent.DismissAlert) },
            onDismiss = { viewModel.onEvent(DocumentsUiEvent.DismissAlert) },
            confirmText = "OK",
            dismissText = null,
            content = {
                Text(
                    text = uiState.alertMessage ?: "",
                    fontFamily = GillSans,
                    fontSize = 15.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }
        )
    }
}

@Composable
fun DocumentActionMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    currentTab: String,
    doc: ViewDocumentsModel,
    onAction: (String) -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = Modifier
            .background(Color.White)
            .width(160.dp)
    ) {
        if (currentTab == "delete") {
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Restore",
                        fontFamily = GillSans,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                },
                onClick = {
                    onDismissRequest()
                    onAction("restore")
                }
            )
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Delete",
                        fontFamily = GillSans,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                },
                onClick = {
                    onDismissRequest()
                    onAction("deleted")
                }
            )
        } else {
            DropdownMenuItem(
                text = {
                    Text(
                        text = "View",
                        fontFamily = GillSans,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                },
                onClick = {
                    onDismissRequest()
                    onAction("View")
                }
            )
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Edit Info",
                        fontFamily = GillSans,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                },
                onClick = {
                    onDismissRequest()
                    onAction("Edit Info")
                }
            )
            val canDownload = !doc.is_disabled
            if (canDownload) {
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Download",
                            fontFamily = GillSans,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    },
                    onClick = {
                        onDismissRequest()
                        onAction("Download")
                    }
                )
            }
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Delete",
                        fontFamily = GillSans,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                },
                onClick = {
                    onDismissRequest()
                    onAction("Delete")
                }
            )
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Update Tags",
                        fontFamily = GillSans,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                },
                onClick = {
                    onDismissRequest()
                    onAction("Update Tags")
                }
            )
        }
    }
}

@Composable
fun ViewDocumentItemCard(
    doc: ViewDocumentsModel,
    currentTab: String,
    onAction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedMenu by remember { mutableStateOf(false) }
    val labelColor = Color(0xFF757575)
    val valueColor = Color.Black

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (doc.isdisabled) 0.5f else 1.0f),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = !doc.isdisabled) { onAction("View") },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.doc_pale_blue),
                        contentDescription = "Document Icon",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(40.dp)
                    )
                    AppSpacer(width = 10.dp)
                    Text(
                        text = doc.name ?: "",
                        fontSize = 14.sp,
                        fontFamily = GillSans,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }

                if (currentTab != "delete") {
                    val lockIcon = if (doc.is_encrypted || doc.added_encryption) {
                        R.drawable.close
                    } else {
                        R.drawable.unlock
                    }
                    IconButton(
                        onClick = { onAction(if (doc.is_encrypted || doc.added_encryption) "decrypt" else "encrypt") },
                        enabled = !doc.isdisabled,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = lockIcon),
                            contentDescription = "Encryption Toggle",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    AppSpacer(width = 4.dp)

                    val isDownloadEnabled = !doc.is_disabled
                    val downloadIcon = if (isDownloadEnabled) {
                        R.drawable.down_enabled
                    } else {
                        R.drawable.down_disable
                    }
                    IconButton(
                        onClick = { onAction(if (isDownloadEnabled) "disable_download" else "enable_download") },
                        enabled = !doc.isdisabled,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = downloadIcon),
                            contentDescription = "Download Status",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Box {
                    IconButton(
                        onClick = { expandedMenu = true },
                        enabled = !doc.isdisabled,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.img_17),
                            contentDescription = "Options Menu",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    DocumentActionMenu(
                        expanded = expandedMenu,
                        onDismissRequest = { expandedMenu = false },
                        currentTab = currentTab,
                        doc = doc,
                        onAction = onAction
                    )
                }
            }

            AppSpacer(height = 6.dp)

            // Nested grey info box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(0.5.dp, Color(0xFFC0C0C0)), RoundedCornerShape(6.dp))
                    .background(Color(0xFFF9FAFB), RoundedCornerShape(6.dp))
                    .padding(10.dp)
            ) {
                if (currentTab == "delete") {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Date : ",
                                fontSize = 12.sp,
                                fontFamily = GillSans,
                                color = labelColor
                            )
                            Text(
                                text = doc.created ?: "",
                                fontSize = 12.sp,
                                fontFamily = GillSans,
                                color = valueColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Deleted By : ",
                                fontSize = 12.sp,
                                fontFamily = GillSans,
                                color = labelColor
                            )
                            Text(
                                text = doc.deletedBy ?: "",
                                fontSize = 12.sp,
                                fontFamily = GillSans,
                                color = valueColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Deleted On : ",
                                fontSize = 12.sp,
                                fontFamily = GillSans,
                                color = labelColor
                            )
                            Text(
                                text = doc.deletedOn ?: "",
                                fontSize = 12.sp,
                                fontFamily = GillSans,
                                color = valueColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Document Type : ",
                                fontSize = 12.sp,
                                fontFamily = GillSans,
                                color = labelColor
                            )
                            Text(
                                text = doc.category?.ifEmpty { doc.doc_type } ?: (doc.doc_type ?: ""),
                                fontSize = 12.sp,
                                fontFamily = GillSans,
                                color = valueColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Uploaded By : ",
                                fontSize = 12.sp,
                                fontFamily = GillSans,
                                color = labelColor
                            )
                            Text(
                                text = doc.uploaded_by ?: "",
                                fontSize = 12.sp,
                                fontFamily = GillSans,
                                color = valueColor,
                                textDecoration = TextDecoration.Underline,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Uploaded On : ",
                                fontSize = 12.sp,
                                fontFamily = GillSans,
                                color = labelColor
                            )
                            Text(
                                text = doc.created ?: "",
                                fontSize = 12.sp,
                                fontFamily = GillSans,
                                color = valueColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Expiration : ",
                                fontSize = 12.sp,
                                fontFamily = GillSans,
                                color = labelColor
                            )
                            Text(
                                text = doc.expiration_date?.ifEmpty { "NA" } ?: "NA",
                                fontSize = 12.sp,
                                fontFamily = GillSans,
                                color = valueColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        val tagsList = mutableListOf<String>()
                        doc.tag?.let { t ->
                            t.keys().forEach { k ->
                                val v = t.optString(k)
                                if (v.isNotEmpty()) tagsList.add("$k-$v") else tagsList.add(k)
                            }
                        }
                        if (tagsList.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Tags : ",
                                    fontSize = 12.sp,
                                    fontFamily = GillSans,
                                    color = labelColor
                                )
                                Text(
                                    text = tagsList.joinToString(", "),
                                    fontSize = 12.sp,
                                    fontFamily = GillSans,
                                    color = valueColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ViewDocumentItemGrid(
    doc: ViewDocumentsModel,
    currentTab: String,
    previewUrl: String?,
    previewBitmap: android.graphics.Bitmap?,
    isLoadingPreview: Boolean,
    isFailedPreview: Boolean,
    onAction: (String) -> Unit,
    onLoadPreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedMenu by remember { mutableStateOf(false) }
    val labelColor = Color(0xFF757575)
    val valueColor = Color.Black

    LaunchedEffect(doc.id) {
        onLoadPreview()
    }
    
    val lockIcon = if (doc.is_encrypted || doc.added_encryption) {
        R.drawable.close
    } else {
        R.drawable.unlock
    }

    val isDownloadEnabled = !doc.is_disabled
    val downloadIcon = if (isDownloadEnabled) {
        R.drawable.down_enabled
    } else {
        R.drawable.down_disable
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .alpha(if (doc.isdisabled) 0.5f else 1.0f),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            // 1. Top bar containing Lock & Download toggle status
            if (currentTab != "delete") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .background(Color(0xFFF9F9F9))
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onAction(if (doc.is_encrypted || doc.added_encryption) "decrypt" else "encrypt") },
                        enabled = !doc.isdisabled,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = lockIcon),
                            contentDescription = "Lock",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = { onAction(if (isDownloadEnabled) "disable_download" else "enable_download") },
                        enabled = !doc.isdisabled,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = downloadIcon),
                            contentDescription = "Download Status",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // 2. Middle preview area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .background(Color(0xFFEFEFEF))
                    .clickable(enabled = !doc.isdisabled) { onAction("View") },
                contentAlignment = Alignment.Center
            ) {
                val hasPreview = previewBitmap != null || !previewUrl.isNullOrEmpty()
                val showSpinner = isLoadingPreview || (!hasPreview && !isFailedPreview)

                if (showSpinner) {
                    CircularProgressIndicator(
                        color = Color(0xFF004D87),
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else if (previewBitmap != null) {
                    Image(
                        bitmap = previewBitmap.asImageBitmap(),
                        contentDescription = "PDF Preview Page 1",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (!previewUrl.isNullOrEmpty() && (doc.content_type?.startsWith("image/", ignoreCase = true) == true || previewUrl.lowercase().contains(Regex("\\.(jpg|jpeg|png|gif|webp|bmp)")))) {
                    AndroidView(
                        factory = { context ->
                            ImageView(context).apply {
                                scaleType = ImageView.ScaleType.CENTER_CROP
                            }
                        },
                        update = { imageView ->
                            Glide.with(imageView.context)
                                .load(previewUrl)
                                .error(R.drawable.doc_pale_blue)
                                .into(imageView)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.doc_pale_blue),
                        contentDescription = "Doc",
                        modifier = Modifier.size(54.dp)
                    )
                }
            }

            // 3. Bottom area with Document Name and (Date + 3-dot Menu)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF9F9F9))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(
                    text = doc.name ?: "",
                    fontSize = 13.sp,
                    fontFamily = GillSans,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val formattedDate = formatGridDate(if (currentTab == "delete" && !doc.deletedOn.isNullOrEmpty()) doc.deletedOn else doc.created)
                    Text(
                        text = formattedDate,
                        fontSize = 11.sp,
                        fontFamily = GillSans,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Box {
                        IconButton(
                            onClick = { expandedMenu = true },
                            enabled = !doc.isdisabled,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.img_17),
                                contentDescription = "Options",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        DocumentActionMenu(
                            expanded = expandedMenu,
                            onDismissRequest = { expandedMenu = false },
                            currentTab = currentTab,
                            doc = doc,
                            onAction = onAction
                        )
                    }
                }
            }
        }
    }
}

private fun formatGridDate(createdDate: String?): String {
    if (createdDate.isNullOrEmpty()) return ""
    return if (createdDate.contains(",")) {
        val parts = createdDate.split(",")
        if (parts.size >= 2) {
            "${parts[0].trim()}, ${parts[1].trim()}"
        } else {
            createdDate
        }
    } else {
        createdDate
    }
}

@Composable
fun PaginationBar(
    currentPage: Int,
    totalPages: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (totalPages <= 1) return

    val isPrevEnabled = currentPage > 1
    val isNextEnabled = currentPage < totalPages

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 10.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous Button
        Button(
            onClick = onPrev,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF004D87),
                disabledContainerColor = Color(0xFF7A9BB8),
                contentColor = Color.White,
                disabledContentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .width(100.dp)
                .height(38.dp)
                .alpha(if (isPrevEnabled) 1.0f else 0.5f),
            enabled = isPrevEnabled
        ) {
            Text(
                text = "<< Prev",
                fontFamily = GillSans,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Next Button
        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF004D87),
                disabledContainerColor = Color(0xFF7A9BB8),
                contentColor = Color.White,
                disabledContentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .width(100.dp)
                .height(38.dp)
                .alpha(if (isNextEnabled) 1.0f else 0.5f),
            enabled = isNextEnabled
        ) {
            Text(
                text = "Next >>",
                fontFamily = GillSans,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun AppConfirmDialog(
    actionType: String,
    doc: ViewDocumentsModel?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val message = when (actionType) {
        "disable_download", "enabled" -> "Are you sure you want to disable download for this document?"
        "enable_download", "disabled" -> "Are you sure you want to enable download for this document?"
        "encrypt" -> "Are you sure you want to encrypt Document?"
        "decrypt" -> "Are you sure you want to decrypt Document?"
        "deleted" -> "Are you sure to permanently delete document?"
        "restore" -> "Are you sure to restore document?"
        "download" -> "Downloading this document will remove it from the secure system.\nDo you wish to proceed with the download?"
        else -> "Are you sure you want to delete Document?"
    }
    val title = when (actionType) {
        "encrypt", "decrypt", "restore" -> "Alert!"
        else -> "Confirmation"
    }

    AppConfirmationDialog(
        title = title,
        message = message,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Composable
fun DocumentViewerDialog(
    url: String,
    docName: String,
    contentType: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    DisposableEffect(url) {
        val activity = context as? androidx.fragment.app.FragmentActivity
        val dialog = if (activity != null) {
            val dialogBuilder = android.app.AlertDialog.Builder(activity)
            val inflater = activity.layoutInflater
            val view = inflater.inflate(R.layout.view_documents, null)
            val progressBar = view.findViewById<ProgressBar>(R.id.progress_pdf)
            val iv_image = view.findViewById<ImageView>(R.id.doc_image)
            val idPDFView = view.findViewById<com.github.barteksc.pdfviewer.PDFView>(R.id.idPDFView)
            val header = view.findViewById<android.widget.TextView>(R.id.header_name)
            val iv_close_edit_docs = view.findViewById<ImageView>(R.id.close_edit_docs)
            header.text = docName
            val dlg = dialogBuilder.create()
            val pdfTask = arrayOfNulls<com.digicoffer.lauditor.CommonFiles.PdfUtils.RetrievePDFfromUrl>(1)

            iv_close_edit_docs.setOnClickListener {
                try {
                    idPDFView?.recycle()
                    pdfTask[0]?.cancelLoading()
                    pdfTask[0]?.cancel(true)
                } catch (ignored: Exception) {
                }
                dlg.dismiss()
                onDismiss()
            }
            dlg.setOnDismissListener {
                onDismiss()
            }

            val lowerUrl = url.lowercase(java.util.Locale.getDefault())
            val cleanUrlPath = lowerUrl.substringBefore('?')
            val isImage = contentType.startsWith("image/", ignoreCase = true) ||
                    contentType.contains("image", ignoreCase = true) ||
                    lowerUrl.contains("response-content-type=image") ||
                    lowerUrl.contains("image%2f") ||
                    cleanUrlPath.endsWith(".jpg") ||
                    cleanUrlPath.endsWith(".jpeg") ||
                    cleanUrlPath.endsWith(".png") ||
                    cleanUrlPath.endsWith(".gif") ||
                    cleanUrlPath.endsWith(".webp") ||
                    cleanUrlPath.endsWith(".bmp") ||
                    cleanUrlPath.endsWith(".svg") ||
                    cleanUrlPath.endsWith(".apng") ||
                    cleanUrlPath.endsWith(".avif") ||
                    docName.endsWith(".jpg", ignoreCase = true) ||
                    docName.endsWith(".jpeg", ignoreCase = true) ||
                    docName.endsWith(".png", ignoreCase = true) ||
                    docName.endsWith(".gif", ignoreCase = true) ||
                    docName.endsWith(".webp", ignoreCase = true)

            if (isImage) {
                iv_image.visibility = android.view.View.VISIBLE
                progressBar.visibility = android.view.View.GONE
                val loadTarget: Any = if (url.startsWith("localfile://")) {
                    File(url.replace("localfile://", ""))
                } else {
                    url
                }
                com.bumptech.glide.Glide.with(context)
                    .load(loadTarget)
                    .placeholder(R.drawable.progress_animation)
                    .fitCenter()
                    .into(iv_image)
            } else {
                idPDFView.visibility = android.view.View.VISIBLE
                progressBar.visibility = android.view.View.VISIBLE
                if (url.startsWith("localfile://")) {
                    val localPath = url.replace("localfile://", "")
                    val file = File(localPath)
                    idPDFView.fromFile(file)
                        .onLoad { progressBar.visibility = android.view.View.GONE }
                        .load()
                } else {
                    pdfTask[0] = com.digicoffer.lauditor.CommonFiles.PdfUtils.RetrievePDFfromUrl(idPDFView, progressBar)
                    pdfTask[0]?.execute(url)
                }
            }
            dlg.setCancelable(false)
            dlg.setCanceledOnTouchOutside(false)
            dlg.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dlg.setView(view)
            dlg.show()
            dlg
        } else null

        onDispose {
            try {
                dialog?.dismiss()
            } catch (ignored: Exception) {
            }
        }
    }
}
