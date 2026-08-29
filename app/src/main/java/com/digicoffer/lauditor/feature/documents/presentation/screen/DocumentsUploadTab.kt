package com.digicoffer.lauditor.feature.documents.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.Documents.Models.ClientsModel
import com.digicoffer.lauditor.Documents.Models.GroupsModel
import com.digicoffer.lauditor.Documents.Models.MattersModel
import com.digicoffer.lauditor.Documents.Models.ViewDocumentsModel
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme
import com.digicoffer.lauditor.core.ui.common.buttons.AppButton
import com.digicoffer.lauditor.core.ui.common.cards.AppCard
import com.digicoffer.lauditor.core.ui.common.dropdowns.DropdownSelectorField
import com.digicoffer.lauditor.core.ui.common.foundation.AppSpacer
import com.digicoffer.lauditor.core.ui.common.foundation.AppText
import com.digicoffer.lauditor.core.ui.common.inputs.AppCheckbox
import com.digicoffer.lauditor.feature.documents.presentation.state.DocumentsUiEvent
import com.digicoffer.lauditor.feature.documents.presentation.state.DocumentsUiState

private val GillSans = FontFamily(
    Font(R.font.gill_sans)
)

@Composable
fun DocumentsUploadTab(
    uiState: DocumentsUiState,
    onEvent: (DocumentsUiEvent) -> Unit,
    onBrowseClick: () -> Unit
) {
    var editingStagedIndex by remember { mutableStateOf(-1) }
    var groupsExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Selector Field Area (Matter or Client dropdowns)
                if (uiState.currentTab == "client") {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = Color(0xFF004D87), fontWeight = FontWeight.Normal, fontFamily = GillSans)) {
                                append("Select Client Name")
                            }
                            withStyle(SpanStyle(color = Color.Red, fontWeight = FontWeight.Normal)) {
                                append(" *")
                            }
                        },
                        fontSize = 14.sp
                    )
                    DropdownSelectorField(
                        items = uiState.clientsList,
                        selectedItem = uiState.selectedUploadClient,
                        onItemSelected = { c ->
                            onEvent(DocumentsUiEvent.SelectUploadClient(c))
                        },
                        itemToLabel = { it.name ?: "" },
                        placeholder = "Select Client Name",
                        showSearch = true
                    )
                }

                if (uiState.currentTab == "matter") {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(color = Color(0xFF004D87), fontWeight = FontWeight.Normal, fontFamily = GillSans)) {
                                append("Matters")
                            }
                            withStyle(SpanStyle(color = Color.Red, fontWeight = FontWeight.Normal)) {
                                append(" *")
                            }
                        },
                        fontSize = 14.sp
                    )
                    DropdownSelectorField(
                        items = uiState.mattersList,
                        selectedItem = uiState.selectedUploadMatter,
                        onItemSelected = { m ->
                            onEvent(DocumentsUiEvent.SelectUploadMatter(m))
                        },
                        itemToLabel = { it.name ?: "" },
                        placeholder = "Select Matter",
                        showSearch = true
                    )
                }

                // 2. Select Groups (Clickable Field toggling checklist visibility below it)
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Color(0xFF004D87), fontWeight = FontWeight.Normal, fontFamily = GillSans)) {
                            append("Select Group(s)")
                        }
                        withStyle(SpanStyle(color = Color.Red, fontWeight = FontWeight.Normal)) {
                            append(" *")
                        }
                    },
                    fontSize = 14.sp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                        .height(40.dp)
                        .background(Color(0xFFF9FAFB), RoundedCornerShape(6.dp))
                        .border(0.5.dp, Color(0xFFC0C0C0), RoundedCornerShape(6.dp))
                        .clickable { groupsExpanded = !groupsExpanded },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val selectedGroupsText = if (uiState.selectedUploadGroups.isNotEmpty()) {
                        uiState.selectedUploadGroups.joinToString { it.name ?: "" }
                    } else {
                        "Select Group(s)"
                    }
                    Text(
                        text = selectedGroupsText,
                        fontFamily = GillSans,
                        fontSize = 15.sp,
                        color = if (uiState.selectedUploadGroups.isNotEmpty()) Color.Black else Color(0xFFA0A0A0),
                        maxLines = 1,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 10.dp)
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.drop_down_blue),
                        contentDescription = "Dropdown Arrow",
                        tint = Color.Unspecified,
                        modifier = Modifier.padding(end = 15.dp).size(12.dp)
                    )
                }

                val activeGroupsList = if (uiState.currentTab == "firm") uiState.groupsList else uiState.clientGroupsList
                if (groupsExpanded && activeGroupsList.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                            .heightIn(max = 150.dp)
                            .background(Color.White, RoundedCornerShape(6.dp))
                            .border(0.5.dp, Color(0xFFC0C0C0), RoundedCornerShape(6.dp))
                            .padding(6.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        activeGroupsList.forEach { grp ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val current = uiState.selectedUploadGroups.toMutableList()
                                        val contains = current.any { it.id == grp.id }
                                        if (!contains) {
                                            current.add(grp)
                                        } else {
                                            current.removeAll { it.id == grp.id }
                                        }
                                        onEvent(DocumentsUiEvent.SelectUploadGroups(current))
                                    }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = uiState.selectedUploadGroups.any { it.id == grp.id },
                                    onCheckedChange = { checked ->
                                        val current = uiState.selectedUploadGroups.toMutableList()
                                        if (checked) {
                                            current.add(grp)
                                        } else {
                                            current.removeAll { it.id == grp.id }
                                        }
                                        onEvent(DocumentsUiEvent.SelectUploadGroups(current))
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color(0xFF004D87),
                                        checkmarkColor = Color.White
                                    )
                                )
                                Text(
                                    text = grp.name ?: "",
                                    color = Color.Black,
                                    fontFamily = GillSans,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }

                // 3. Select Document(s) Section (dotted browse box)
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Color(0xFF004D87), fontWeight = FontWeight.Normal, fontFamily = GillSans)) {
                            append("Select Document(s)")
                        }
                        withStyle(SpanStyle(color = Color.Red, fontWeight = FontWeight.Normal)) {
                            append(" *")
                        }
                    },
                    fontSize = 14.sp
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .drawBehind {
                            val stroke = Stroke(
                                width = 2f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )
                            drawRoundRect(
                                color = Color.LightGray,
                                style = stroke,
                                cornerRadius = CornerRadius(8.dp.toPx())
                            )
                        }
                        .background(Color(0xFFF9FAFB), shape = RoundedCornerShape(8.dp))
                        .clickable(onClick = onBrowseClick),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.cloud_icon),
                            contentDescription = "Upload Icon",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(36.dp)
                        )
                        AppSpacer(height = 6.dp)
                        Text(
                            text = "Choose the files from your device or drag\n& drop them here",
                            color = Color.Gray,
                            fontFamily = GillSans,
                            fontSize = 11.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        AppSpacer(height = 6.dp)
                        Button(
                            onClick = onBrowseClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D87)),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Browse",
                                color = Color.White,
                                fontFamily = GillSans,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 4. Staged Upload Files Queue List
                if (uiState.selectedUploadFiles.isNotEmpty()) {
                    Text(
                        text = "Selected Documents",
                        fontFamily = GillSans,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        uiState.selectedUploadFiles.forEachIndexed { idx, docModel ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(BorderStroke(0.5.dp, Color(0xFFC0C0C0)), RoundedCornerShape(6.dp))
                                    .background(Color(0xFFF9FAFB), RoundedCornerShape(6.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = docModel.filename ?: "",
                                    fontSize = 13.sp,
                                    fontFamily = GillSans,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier.weight(1f)
                                )

                                IconButton(
                                    onClick = { editingStagedIndex = idx },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.edit__icon),
                                        contentDescription = "Edit file metadata details",
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = { onEvent(DocumentsUiEvent.RemoveStagedFile(idx)) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.cancel_red_icon),
                                        contentDescription = "Remove file from queue",
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. Action Buttons (Cancel / Upload)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onEvent(DocumentsUiEvent.ToggleUploadMode(false)) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFEFEF)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Text("Cancel", color = Color.Black, fontFamily = GillSans, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onEvent(DocumentsUiEvent.UploadDocuments) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D87)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Text("Upload", color = Color.White, fontFamily = GillSans, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (editingStagedIndex != -1) {
        val editingDoc = uiState.selectedUploadFiles.getOrNull(editingStagedIndex)
        if (editingDoc != null) {
            val legacyWrapper = ViewDocumentsModel()
            legacyWrapper.name = editingDoc.name
            legacyWrapper.description = editingDoc.description
            legacyWrapper.expiration_date = editingDoc.expiration_date
            
            EditMetadataDialog(
                doc = legacyWrapper,
                initialDownloadDisabled = editingDoc.isIsenabled,
                initialEncrypted = editingDoc.isencrypted ?: false,
                onSave = { name, desc, exp, downloadDisabled, encrypted, tags ->
                    onEvent(
                        DocumentsUiEvent.UpdateStagedFile(
                            editingStagedIndex, name, desc, exp,
                            downloadDisabled, encrypted, tags
                        )
                    )
                    editingStagedIndex = -1
                },
                onDismiss = { editingStagedIndex = -1 }
            )
        }
    }
}
