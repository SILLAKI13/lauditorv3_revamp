package com.digicoffer.lauditor.feature.email.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.Documents.Models.ClientsModel
import com.digicoffer.lauditor.Documents.Models.DocumentsModel
import com.digicoffer.lauditor.Documents.Models.MattersModel
import com.digicoffer.lauditor.Documents.Models.ViewDocumentsModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.ui.common.dropdowns.DropdownSelectorField
import com.digicoffer.lauditor.core.ui.common.search.AppSearchField
import com.digicoffer.lauditor.feature.email.data.model.LauditorAttachedDoc

private val GillSans = FontFamily(Font(R.font.gill_sans))

@Composable
fun AttachDocumentDialog(
    categoryTab: String,
    isMatterTab: Boolean,
    clientsList: List<ClientsModel>,
    mattersList: List<MattersModel>,
    groupsList: List<DocumentsModel>,
    selectedClientId: String?,
    selectedClientName: String,
    selectedMatterId: String?,
    selectedMatterName: String,
    selectedGroupIds: Set<String>,
    availableDocs: List<ViewDocumentsModel>,
    selectedDocs: List<LauditorAttachedDoc>,
    searchQuery: String,
    isLoading: Boolean,
    isUSUser: Boolean = false,
    onCategoryTabChange: (String) -> Unit,
    onSubTabChange: (Boolean) -> Unit,
    onSelectClient: (ClientsModel) -> Unit,
    onSelectMatter: (MattersModel) -> Unit,
    onToggleGroup: (String) -> Unit,
    onToggleDoc: (ViewDocumentsModel, Boolean) -> Unit,
    onSearchChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val isSolo = Constants.CATEGORY == "solo"

    val filteredDocs = remember(availableDocs, searchQuery) {
        val query = searchQuery.trim()
        if (query.isEmpty()) {
            availableDocs
        } else {
            availableDocs.filter {
                (it.name ?: "").contains(query, ignoreCase = true) ||
                        (it.filename ?: "").contains(query, ignoreCase = true) ||
                        (it.description ?: "").contains(query, ignoreCase = true) ||
                        (it.uploaded_by ?: "").contains(query, ignoreCase = true)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                // Header
                Text(
                    text = "Attach Documents",
                    fontFamily = GillSans,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF004D87)
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (!isUSUser) {
                    // Category Tabs (Matter/Client vs Firm)
                    Row(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        val isClientSelected = categoryTab == "client"
                        // Left Tab: Client / Matter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                                .background(if (isClientSelected) Color(0xFF004D87) else Color(0xFFF1F5F9))
                                .clickable { onCategoryTabChange("client") }
                                .padding(horizontal = 24.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Client Documents",
                                fontFamily = GillSans,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isClientSelected) Color.White else Color(0xFF334155)
                            )
                        }

                        if (!isSolo) {
                            // Right Tab: Firm
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                                    .background(if (!isClientSelected) Color(0xFF004D87) else Color(0xFFF1F5F9))
                                    .clickable { onCategoryTabChange("firm") }
                                    .padding(horizontal = 24.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Firm Documents",
                                    fontFamily = GillSans,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (!isClientSelected) Color.White else Color(0xFF334155)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (categoryTab == "client") {
                        // Radio button for Matter vs Client
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { onSubTabChange(true) }
                                    .padding(end = 16.dp)
                            ) {
                                RadioButton(
                                    selected = isMatterTab,
                                    onClick = { onSubTabChange(true) },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF004D87))
                                )
                                Text(
                                    text = "Matter",
                                    fontFamily = GillSans,
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { onSubTabChange(false) }
                            ) {
                                RadioButton(
                                    selected = !isMatterTab,
                                    onClick = { onSubTabChange(false) },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF004D87))
                                )
                                Text(
                                    text = "Client",
                                    fontFamily = GillSans,
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (isMatterTab) {
                            // Matters dropdown
                            val selectedMatter = mattersList.find { it.id == selectedMatterId }
                            DropdownSelectorField(
                                items = mattersList,
                                selectedItem = selectedMatter,
                                onItemSelected = onSelectMatter,
                                itemToLabel = { it.title ?: "" },
                                placeholder = if (selectedMatterName.isNotEmpty()) selectedMatterName else "Select Matter",
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            // Clients dropdown
                            val selectedClient = clientsList.find { it.id == selectedClientId }
                            DropdownSelectorField(
                                items = clientsList,
                                selectedItem = selectedClient,
                                onItemSelected = onSelectClient,
                                itemToLabel = { it.name ?: "" },
                                placeholder = if (selectedClientName.isNotEmpty()) selectedClientName else "Select Client",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        // Firm: Groups selection
                        Text(
                            text = "Select Group(s):",
                            fontFamily = GillSans,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF004D87)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            groupsList.take(5).forEach { group ->
                                val gid = group.group_id ?: ""
                                val isChecked = selectedGroupIds.contains(gid)
                                FilterChip(
                                    selected = isChecked,
                                    onClick = { onToggleGroup(gid) },
                                    label = {
                                        Text(
                                            text = group.group_name ?: "",
                                            fontSize = 12.sp,
                                            fontFamily = GillSans
                                        )
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Search field for available docs
                AppSearchField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    onClearClick = { onSearchChange("") },
                    placeholder = "Search documents...",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Documents list
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                        .padding(6.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = Color(0xFF004D87)
                        )
                    } else if (filteredDocs.isEmpty()) {
                        Text(
                            text = "No documents found",
                            fontFamily = GillSans,
                            fontSize = 14.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(filteredDocs, key = { it.id?.ifEmpty { null } ?: "${it.name}_${it.filename}_${it.hashCode()}" }) { doc ->
                                val isSelected = selectedDocs.any { it.id == doc.id }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) Color(0xFFE8F6FF) else Color.White)
                                        .clickable { onToggleDoc(doc, !isSelected) }
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { checked -> onToggleDoc(doc, checked) },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = Color(0xFF004D87)
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = doc.name ?: doc.filename ?: "Document",
                                            fontFamily = GillSans,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp,
                                            color = Color(0xFF1E293B),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (!doc.uploaded_by.isNullOrEmpty() || !doc.created.isNullOrEmpty()) {
                                            Text(
                                                text = "Uploaded by: ${doc.uploaded_by.orEmpty()} • ${doc.created.orEmpty()}",
                                                fontFamily = GillSans,
                                                fontSize = 11.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Buttons: Cancel & Attach
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            fontFamily = GillSans,
                            fontSize = 14.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = onConfirm,
                        enabled = selectedDocs.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF004D87),
                            disabledContainerColor = Color(0xFF94A3B8)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text(
                            text = "Attach (${selectedDocs.size})",
                            fontFamily = GillSans,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
