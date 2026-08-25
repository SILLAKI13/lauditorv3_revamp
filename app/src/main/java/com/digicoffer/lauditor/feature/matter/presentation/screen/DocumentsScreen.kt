package com.digicoffer.lauditor.feature.matter.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.feature.matter.presentation.viewmodel.MatterEditViewModel
import com.digicoffer.lauditor.core.ui.common.feedback.AppLoader
import java.io.File
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(
    viewModel: MatterEditViewModel,
    onBrowseClick: () -> Unit,
    onViewDocument: (com.digicoffer.lauditor.Documents.Models.DocumentsModel) -> Unit,
    onCancel: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    // Header title + close icon
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (Constants.create_matter) (uiState.title.ifEmpty { "Matter Documents" }) else "",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        IconButton(onClick = onCancel) {
                            Icon(
                                painter = painterResource(id = R.drawable.cancel_icon_1),
                                contentDescription = "Close",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Dashed drop zone container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
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
                            .background(Color(0xFFF8FAFC), shape = RoundedCornerShape(8.dp))
                            .clickable(onClick = onBrowseClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.cloud_icon),
                                contentDescription = "Upload Icon",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Choose the files from your device or drag\n& drop them here",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = onBrowseClick,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D87)),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Browse Files",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Section header for selected/attached files
                    if (uiState.selectedExistingDocuments.isNotEmpty() || uiState.selectedUploadFiles.isNotEmpty()) {
                        Text(
                            text = stringResource(id = R.string.selected_documents),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                        )
                    }

                    // Render existing attached documents
                    uiState.selectedExistingDocuments.forEachIndexed { index, doc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(4.dp))
                                .background(Color(0xFFF8FAFC))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = doc.name ?: "",
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f),
                                color = Color.Black
                            )
                            IconButton(
                                onClick = { onViewDocument(doc) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.eye_icon),
                                    contentDescription = "View File",
                                    tint = Color.Unspecified
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { viewModel.removeExistingDocument(index) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.cancel_red_icon),
                                    contentDescription = "Remove File",
                                    tint = Color.Unspecified
                                )
                            }
                        }
                    }

                    // Render newly added files to upload
                    uiState.selectedUploadFiles.forEachIndexed { index, doc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(4.dp))
                                .background(Color(0xFFF8FAFC))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${doc.name}.${doc.content_type}",
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f),
                                color = Color.Black
                            )
                            IconButton(
                                onClick = { viewModel.setEditMetadataFileIndex(index) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.edit__icon),
                                    contentDescription = "Edit Metadata",
                                    tint = Color.Unspecified
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { viewModel.removeUploadFile(index) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.cancel_red_icon),
                                    contentDescription = "Remove File",
                                    tint = Color.Unspecified
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onCancel,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8E8E8)),
                            border = BorderStroke(1.dp, Color(0xFF888888)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .width(135.dp)
                                .height(40.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Cancel",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        Button(
                            onClick = {
                                if (uiState.selectedUploadFiles.isNotEmpty() || uiState.selectedExistingDocuments.isNotEmpty()) {
                                    viewModel.uploadAndSaveDocuments(onSuccess = {})
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D87)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .width(135.dp)
                                .height(40.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Save",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Edit Metadata Dialog
        uiState.editMetadataFileIndex?.let { index ->
            val doc = uiState.selectedUploadFiles.getOrNull(index)
            if (doc != null) {
                var docName by remember { mutableStateOf(doc.name ?: "") }
                var description by remember { mutableStateOf(doc.description) }
                var expirationDate by remember { mutableStateOf(doc.expiration_date) }
                var enableDownload by remember { mutableStateOf(doc.isIsenabled) }
                var enableEncryption by remember { mutableStateOf(doc.isencrypted ?: false) }

                Dialog(onDismissRequest = { viewModel.setEditMetadataFileIndex(null) }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Header Bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF004D87))
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "Edit Metadata",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(
                                    onClick = { viewModel.setEditMetadataFileIndex(null) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.cancel_icon),
                                        contentDescription = "Close",
                                        tint = Color.White
                                    )
                                }
                            }

                            // Form Container
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                // Name input
                                Row {
                                    Text(
                                        text = "Document Name",
                                        fontSize = 15.sp,
                                        color = Color(0xFF004D87),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = " *",
                                        fontSize = 15.sp,
                                        color = Color.Red,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = docName,
                                    onValueChange = { if (it.length <= 50) docName = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(4.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFCBD5E1),
                                        unfocusedBorderColor = Color(0xFFCBD5E1),
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    )
                                )
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Text(
                                        text = "${docName.length}/50",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Description input
                                Row {
                                    Text(
                                        text = "Description",
                                        fontSize = 15.sp,
                                        color = Color(0xFF004D87),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = " *",
                                        fontSize = 15.sp,
                                        color = Color.Red,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = description,
                                    onValueChange = { if (it.length <= 300) description = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp),
                                    shape = RoundedCornerShape(4.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFCBD5E1),
                                        unfocusedBorderColor = Color(0xFFCBD5E1),
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    )
                                )
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Text(
                                        text = "${description.length}/300",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Expiration Date Picker
                                Text(
                                    text = "Expiration Date",
                                    fontSize = 15.sp,
                                    color = Color(0xFF004D87),
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
                                        .background(Color(0xFFF1F5F9))
                                        .clickable {
                                            val dummy = android.widget.TextView(context).apply { text = expirationDate }
                                            com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.showDatePicker(dummy, false, false, true) {
                                                expirationDate = dummy.text.toString()
                                            }
                                        }
                                        .padding(horizontal = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = expirationDate.ifEmpty { "Expiration Date" },
                                        color = if (expirationDate.isEmpty()) Color.Gray else Color.Black,
                                        fontSize = 14.sp
                                    )
                                    Icon(
                                        painter = painterResource(id = R.drawable.calendar_icon_xsmall),
                                        contentDescription = "Select Date",
                                        tint = Color.Black,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Download Toggle
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Enable Download",
                                        color = Color(0xFF004D87),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Switch(
                                        checked = enableDownload,
                                        onCheckedChange = { enableDownload = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Color(0xFF004D87)
                                        )
                                    )
                                }

                                // Encryption Toggle
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Enable Encryption",
                                        color = Color(0xFF004D87),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Switch(
                                        checked = enableEncryption,
                                        onCheckedChange = { enableEncryption = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Color(0xFF004D87)
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Dialog Cancel & Save Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.setEditMetadataFileIndex(null) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2E8F0)),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                    ) {
                                        Text(text = "Cancel", color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                    Button(
                                        onClick = {
                                            if (docName.isNotEmpty() && description.isNotEmpty()) {
                                                viewModel.updateUploadFile(
                                                    index = index,
                                                    name = docName,
                                                    description = description,
                                                    expDate = expirationDate,
                                                    isDownloadDisabled = enableDownload,
                                                    isEncrypted = enableEncryption
                                                )
                                                viewModel.setEditMetadataFileIndex(null)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D87)),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                    ) {
                                        Text(text = "Save", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (uiState.isUploadingDocuments) {
            AppLoader()
        }

        LaunchedEffect(uiState.showSuccessDialog) {
            if (uiState.showSuccessDialog) {
                val activity = context as? android.app.Activity
                com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.showAlert(
                    uiState.successMessage.ifEmpty { "You have successfully updated the matter information" },
                    activity,
                    "Success"
                ) {
                    viewModel.dismissSuccessDialog()
                    onCancel()
                }
            }
        }
    }
}
