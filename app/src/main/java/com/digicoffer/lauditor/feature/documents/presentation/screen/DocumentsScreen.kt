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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.items
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
    onBrowseClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeletedBanner by rememberSaveable { mutableStateOf(true) }

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
                                color = Color(0xFF004D87)
                            )

                            if (uiState.currentTab != "delete") {
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
                                DropdownSelectorField(
                                    items = uiState.groupsList,
                                    selectedItem = uiState.selectedFilterGroup,
                                    onItemSelected = { g ->
                                        viewModel.onEvent(DocumentsUiEvent.SelectFilterGroup(g))
                                    },
                                    itemToLabel = { it.name ?: "" },
                                    placeholder = "All Groups",
                                    onClearSelection = if (uiState.selectedFilterGroup != uiState.groupsList.firstOrNull()) {
                                        {
                                            uiState.groupsList.firstOrNull()?.let {
                                                viewModel.onEvent(DocumentsUiEvent.SelectFilterGroup(it))
                                            }
                                        }
                                    } else null
                                )
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
                                DropdownSelectorField(
                                    items = if ("solo" == com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.CATEGORY) listOf("All types", "Client") else listOf("All types", "Firm", "Client"),
                                    selectedItem = uiState.selectedFilterDocType ?: "All types",
                                    onItemSelected = { name ->
                                        val type = if (name == "All types") null else name.lowercase()
                                        viewModel.onEvent(DocumentsUiEvent.SelectFilterDocType(type))
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
                        "firm" -> "No Firm Documents"
                        "client" -> "No Client Documents"
                        "delete" -> if ("solo" == com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.CATEGORY) "No Deleted Documents Yet!" else "No Documents Pending Approval"
                        else -> "No Matter Documents"
                    }
                    val emptySubtitle = when (uiState.currentTab) {
                        "delete" -> if ("solo" == com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants.CATEGORY) "Deleted documents will appear here" else "Documents pending approval will appear here"
                        else -> "Upload documents to get started"
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
                            imageSize = 120.dp
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

                    if (uiState.isGridView) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(paginatedDocs, key = { it.id ?: "" }) { doc ->
                                ViewDocumentItemGrid(
                                    doc = doc,
                                    previewUrl = uiState.previewUrls[doc.id ?: ""],
                                    previewBitmap = uiState.previewBitmaps[doc.id ?: ""],
                                    isLoadingPreview = uiState.loadingPreviewIds.contains(doc.id ?: ""),
                                    isFailedPreview = uiState.failedPreviewIds.contains(doc.id ?: ""),
                                    onAction = { action ->
                                        viewModel.onEvent(DocumentsUiEvent.TriggerAction(action, doc))
                                    },
                                    onLoadPreview = {
                                        viewModel.onEvent(DocumentsUiEvent.LoadPreview(doc))
                                    }
                                )
                            }
                            if (totalPages > 1) {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    PaginationBar(
                                        currentPage = uiState.currentPage,
                                        totalPages = totalPages,
                                        onPageSelected = { viewModel.onEvent(DocumentsUiEvent.SelectPage(it)) }
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(paginatedDocs, key = { it.id ?: "" }) { doc ->
                                ViewDocumentItemCard(doc = doc, currentTab = uiState.currentTab, onAction = { action ->
                                    viewModel.onEvent(DocumentsUiEvent.TriggerAction(action, doc))
                                })
                            }
                            if (totalPages > 1) {
                                item {
                                    PaginationBar(
                                        currentPage = uiState.currentPage,
                                        totalPages = totalPages,
                                        onPageSelected = { viewModel.onEvent(DocumentsUiEvent.SelectPage(it)) }
                                    )
                                }
                            }
                        }
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
            title = uiState.alertTitle ?: "Alert",
            onConfirm = { viewModel.onEvent(DocumentsUiEvent.DismissAlert) },
            onDismiss = { viewModel.onEvent(DocumentsUiEvent.DismissAlert) },
            confirmText = "OK",
            dismissText = null,
            content = {
                Text(
                    text = uiState.alertMessage ?: "",
                    fontFamily = GillSans,
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
        )
    }
}

@Composable
fun ViewDocumentItemCard(
    doc: ViewDocumentsModel,
    currentTab: String,
    onAction: (String) -> Unit
) {
    var expandedMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
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
                    // Lock icon toggle (Clicking toggles encrypt/decrypt state)
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

                    // Download icon status (Clicking toggles disable/enable download state)
                    val isDownloadDisabled = doc.is_disabled
                    val downloadIcon = if (isDownloadDisabled) {
                        R.drawable.down_enabled
                    } else {
                        R.drawable.down_disable
                    }
                    IconButton(
                        onClick = { onAction(if (isDownloadDisabled) "disabled" else "enabled") },
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

                // 3-dot spinner options menu
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
                    DropdownMenu(
                        expanded = expandedMenu,
                        onDismissRequest = { expandedMenu = false },
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
                                    expandedMenu = false
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
                                    expandedMenu = false
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
                                    expandedMenu = false
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
                                    expandedMenu = false
                                    onAction("Edit Info")
                                }
                            )
                            if (doc.isdisabled != true && doc.is_disabled != true) {
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
                                        expandedMenu = false
                                        onAction("Download")
                                    }
                                )
                            }
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
                                    expandedMenu = false
                                    onAction("Update Tags")
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
                                    expandedMenu = false
                                    onAction("Delete")
                                }
                            )
                        }
                    }
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
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row {
                        Text(
                            text = "Uploaded By : ",
                            fontSize = 12.sp,
                            fontFamily = GillSans,
                            color = Color.Black
                        )
                        Text(
                            text = doc.uploaded_by ?: "",
                            fontSize = 12.sp,
                            fontFamily = GillSans,
                            color = Color.Black
                        )
                    }
                    Row {
                        Text(
                            text = "Uploaded On : ",
                            fontSize = 12.sp,
                            fontFamily = GillSans,
                            color = Color.Black
                        )
                        Text(
                            text = doc.created ?: "",
                            fontSize = 12.sp,
                            fontFamily = GillSans,
                            color = Color.Black
                        )
                    }
                    Row {
                        Text(
                            text = "Expiration : ",
                            fontSize = 12.sp,
                            fontFamily = GillSans,
                            color = Color.Black
                        )
                        Text(
                            text = doc.expiration_date?.ifEmpty { "NA" } ?: "NA",
                            fontSize = 12.sp,
                            fontFamily = GillSans,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ViewDocumentItemGrid(
    doc: ViewDocumentsModel,
    previewUrl: String?,
    previewBitmap: android.graphics.Bitmap?,
    isLoadingPreview: Boolean,
    isFailedPreview: Boolean,
    onAction: (String) -> Unit,
    onLoadPreview: () -> Unit
) {
    var expandedMenu by remember { mutableStateOf(false) }

    LaunchedEffect(doc.id) {
        onLoadPreview()
    }
    
    val lockIcon = if (doc.is_encrypted || doc.added_encryption) {
        R.drawable.close
    } else {
        R.drawable.unlock
    }

    val isDownloadDisabled = doc.is_disabled
    val downloadIcon = if (isDownloadDisabled) {
        R.drawable.down_enabled
    } else {
        R.drawable.down_disable
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .alpha(if (doc.isdisabled) 0.5f else 1.0f),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Top bar containing Lock & Download toggle status
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
                    onClick = { onAction(if (isDownloadDisabled) "disabled" else "enabled") },
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

            // 3. Bottom area with Details & Menu trigger
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = doc.name ?: "",
                        fontSize = 12.sp,
                        fontFamily = GillSans,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = doc.created ?: "",
                        fontSize = 10.sp,
                        fontFamily = GillSans,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }

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
                    DropdownMenu(
                        expanded = expandedMenu,
                        onDismissRequest = { expandedMenu = false },
                        modifier = Modifier
                            .background(Color.White)
                            .width(160.dp)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "View",
                                    fontFamily = GillSans,
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                            },
                            onClick = { expandedMenu = false; onAction("View") }
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
                            onClick = { expandedMenu = false; onAction("Edit Info") }
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
                            onClick = { expandedMenu = false; onAction("Delete") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PaginationBar(
    currentPage: Int,
    totalPages: Int,
    onPageSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (totalPages <= 1) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val leftEnabled = currentPage > 1
        Box(
            modifier = Modifier
                .size(32.dp)
                .clickable(enabled = leftEnabled) { onPageSelected(currentPage - 1) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "<",
                fontFamily = GillSans,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (leftEnabled) Color(0xFF004D87) else Color.LightGray
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        val blueGradient = androidx.compose.ui.graphics.Brush.linearGradient(
            colors = listOf(Color(0xFF0073C6), Color(0xFF004D87))
        )

        for (page in 1..totalPages) {
            val isActive = page == currentPage
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        brush = if (isActive) blueGradient else androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onPageSelected(page) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = page.toString(),
                    fontFamily = GillSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isActive) Color.White else Color(0xFF004D87)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
        }

        Spacer(modifier = Modifier.width(4.dp))

        val rightEnabled = currentPage < totalPages
        Box(
            modifier = Modifier
                .size(32.dp)
                .clickable(enabled = rightEnabled) { onPageSelected(currentPage + 1) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = ">",
                fontFamily = GillSans,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (rightEnabled) Color(0xFF004D87) else Color.LightGray
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
        "disabled" -> "Are you sure you want to enable download for this document?"
        "enabled" -> "Are you sure you want to disable download for this document?"
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

    AppDialog(
        title = title,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        confirmText = "Yes",
        dismissText = "No"
    ) {
        Text(
            text = message,
            fontFamily = GillSans,
            fontSize = 15.sp,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun DocumentViewerDialog(
    url: String,
    docName: String,
    contentType: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF004D87))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = docName, color = Color.White, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(
                            painter = painterResource(id = R.drawable.cancel_white_icon),
                            contentDescription = "Close Preview",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    val lowerUrl = url.lowercase(java.util.Locale.ROOT)
                    val isImage = contentType.startsWith("image/", ignoreCase = true) ||
                            lowerUrl.endsWith(".jpg") ||
                            lowerUrl.endsWith(".jpeg") ||
                            lowerUrl.endsWith(".png") ||
                            lowerUrl.endsWith(".gif") ||
                            lowerUrl.endsWith(".webp") ||
                            lowerUrl.endsWith(".bmp")

                    if (isImage) {
                        AndroidView(
                            factory = { ctx ->
                                val pBar = ProgressBar(ctx)
                                val imgView = ImageView(ctx).apply {
                                    scaleType = ImageView.ScaleType.FIT_CENTER
                                    adjustViewBounds = true
                                    layoutParams = FrameLayout.LayoutParams(
                                        FrameLayout.LayoutParams.MATCH_PARENT,
                                        FrameLayout.LayoutParams.MATCH_PARENT,
                                        android.view.Gravity.CENTER
                                    )
                                }
                                val frameLayout = FrameLayout(ctx).apply {
                                    layoutParams = FrameLayout.LayoutParams(
                                        FrameLayout.LayoutParams.MATCH_PARENT,
                                        FrameLayout.LayoutParams.MATCH_PARENT
                                    )
                                    addView(imgView)
                                    addView(pBar, FrameLayout.LayoutParams(
                                        FrameLayout.LayoutParams.WRAP_CONTENT,
                                        FrameLayout.LayoutParams.WRAP_CONTENT,
                                        android.view.Gravity.CENTER
                                    ))
                                }
                                val loadTarget: Any = if (url.startsWith("localfile://")) {
                                    File(url.replace("localfile://", ""))
                                } else {
                                    url
                                }
                                com.bumptech.glide.Glide.with(ctx)
                                    .load(loadTarget)
                                    .fitCenter()
                                    .listener(object : com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable> {
                                        override fun onLoadFailed(
                                            e: com.bumptech.glide.load.engine.GlideException?,
                                            model: Any?,
                                            target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>?,
                                            isFirstResource: Boolean
                                        ): Boolean {
                                            pBar.visibility = android.view.View.GONE
                                            return false
                                        }

                                        override fun onResourceReady(
                                            resource: android.graphics.drawable.Drawable?,
                                            model: Any?,
                                            target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>?,
                                            dataSource: com.bumptech.glide.load.DataSource?,
                                            isFirstResource: Boolean
                                        ): Boolean {
                                            pBar.visibility = android.view.View.GONE
                                            return false
                                        }
                                    })
                                    .into(imgView)
                                frameLayout
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // PDF & converted documents (XLS, XLSX, DOC, DOCX, etc.) render in PDFView
                        AndroidView(
                            factory = { ctx ->
                                val pBar = ProgressBar(ctx)
                                val pView = com.github.barteksc.pdfviewer.PDFView(ctx, null).apply {
                                    layoutParams = FrameLayout.LayoutParams(
                                        FrameLayout.LayoutParams.MATCH_PARENT,
                                        FrameLayout.LayoutParams.MATCH_PARENT,
                                        android.view.Gravity.CENTER
                                    )
                                }
                                val frameLayout = FrameLayout(ctx).apply {
                                    layoutParams = FrameLayout.LayoutParams(
                                        FrameLayout.LayoutParams.MATCH_PARENT,
                                        FrameLayout.LayoutParams.MATCH_PARENT
                                    )
                                    addView(pView)
                                    addView(pBar, FrameLayout.LayoutParams(
                                        FrameLayout.LayoutParams.WRAP_CONTENT,
                                        FrameLayout.LayoutParams.WRAP_CONTENT,
                                        android.view.Gravity.CENTER
                                    ))
                                }
                                if (url.startsWith("localfile://")) {
                                    try {
                                        val filePath = url.replace("localfile://", "")
                                        val file = File(filePath)
                                        pView.fromFile(file)
                                            .onLoad { pBar.visibility = android.view.View.GONE }
                                            .load()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        pBar.visibility = android.view.View.GONE
                                    }
                                } else {
                                    com.digicoffer.lauditor.CommonFiles.PdfUtils.RetrievePDFfromUrl(pView, pBar).execute(url)
                                }
                                frameLayout
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
