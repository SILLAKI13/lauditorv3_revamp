package com.digicoffer.lauditor.feature.email.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.ui.common.dropdowns.DropdownSelectorField
import com.digicoffer.lauditor.feature.email.data.model.EmailAttachmentItem

private val GillSans = FontFamily(Font(R.font.gill_sans))

@Composable
fun SaveAttachmentDialog(
    targetAttachment: EmailAttachmentItem?,
    categoryTab: String,
    isMatterTab: Boolean,
    clientsList: List<ClientsModel>,
    mattersList: List<MattersModel>,
    groupsList: List<DocumentsModel>,
    clientGroups: List<DocumentsModel>,
    selectedClientId: String?,
    selectedClientName: String,
    selectedMatterId: String?,
    selectedMatterName: String,
    selectedGroupIds: Set<String>,
    isSaving: Boolean,
    onCategoryTabChange: (String) -> Unit,
    onSubTabChange: (Boolean) -> Unit,
    onSelectClient: (ClientsModel) -> Unit,
    onSelectMatter: (MattersModel) -> Unit,
    onToggleGroup: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val isSolo = Constants.CATEGORY == "solo"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
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
                    text = "Upload to Lauditor Documents",
                    fontFamily = GillSans,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF004D87)
                )

                if (targetAttachment != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "File: ${targetAttachment.filename}",
                        fontFamily = GillSans,
                        fontSize = 13.sp,
                        color = Color(0xFF64748B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tabs: Client vs Firm
                Row(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    val isClientSelected = categoryTab == "client"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                            .background(if (isClientSelected) Color(0xFF004D87) else Color(0xFFF1F5F9))
                            .clickable { onCategoryTabChange("client") }
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Client",
                            fontFamily = GillSans,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isClientSelected) Color.White else Color(0xFF334155)
                        )
                    }

                    if (!isSolo) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                                .background(if (!isClientSelected) Color(0xFF004D87) else Color(0xFFF1F5F9))
                                .clickable { onCategoryTabChange("firm") }
                                .padding(horizontal = 24.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Firm",
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
                    // Radio options: Matter vs Client
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

                    // Display client groups if available
                    val displayGroups = if (clientGroups.isNotEmpty()) clientGroups else groupsList
                    if (displayGroups.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Groups:",
                            fontFamily = GillSans,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF004D87)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                                .padding(4.dp)
                        ) {
                            items(displayGroups, key = { it.group_id ?: "" }) { grp ->
                                val gid = grp.group_id ?: ""
                                val isChecked = selectedGroupIds.contains(gid)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onToggleGroup(gid) }
                                        .padding(horizontal = 6.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { onToggleGroup(gid) },
                                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF004D87))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = grp.group_name ?: "",
                                        fontFamily = GillSans,
                                        fontSize = 13.sp,
                                        color = Color(0xFF1E293B)
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
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
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                            .padding(4.dp)
                    ) {
                        items(groupsList, key = { it.group_id ?: "" }) { grp ->
                            val gid = grp.group_id ?: ""
                            val isChecked = selectedGroupIds.contains(gid)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onToggleGroup(gid) }
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { onToggleGroup(gid) },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF004D87))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = grp.group_name ?: "",
                                    fontFamily = GillSans,
                                    fontSize = 13.sp,
                                    color = Color(0xFF1E293B)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action Buttons
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

                    val canUpload = if (categoryTab == "client") {
                        if (isMatterTab) !selectedMatterId.isNullOrEmpty() else !selectedClientId.isNullOrEmpty()
                    } else {
                        selectedGroupIds.isNotEmpty()
                    }

                    Button(
                        onClick = onConfirm,
                        enabled = !isSaving && canUpload,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF004D87),
                            disabledContainerColor = Color(0xFF94A3B8)
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Upload",
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
}
