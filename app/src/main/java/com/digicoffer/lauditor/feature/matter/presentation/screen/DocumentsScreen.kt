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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.Documents.Models.ViewDocumentsModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.designsystem.colors.ColorTokens
import com.digicoffer.lauditor.core.ui.common.dialogs.AppDialog
import com.digicoffer.lauditor.core.ui.common.feedback.AppLoader
import com.digicoffer.lauditor.feature.documents.presentation.screen.EditMetadataDialog
import com.digicoffer.lauditor.feature.matter.presentation.viewmodel.MatterEditViewModel
import org.json.JSONObject

private val GillSans = FontFamily(Font(R.font.gill_sans))
private val GillSansBold = FontFamily(Font(R.font.gill_sans_bold))

@Composable
fun DocumentsScreen(
    viewModel: MatterEditViewModel,
    onBrowseClick: () -> Unit,
    onViewDocument: (com.digicoffer.lauditor.Documents.Models.DocumentsModel) -> Unit,
    onCancel: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorTokens.LightBlueBg)
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    // Header title + close icon
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.title.ifEmpty { "Matter Documents" },
                            fontSize = 20.sp,
                            fontFamily = GillSansBold,
                            fontWeight = FontWeight.Bold,
                            color = ColorTokens.BluePrimary
                        )
                        IconButton(onClick = {
                            viewModel.consumeUpdateSuccess()
                            viewModel.resetTransientState()
                            onCancel()
                        }) {
                            Icon(
                                painter = painterResource(id = R.drawable.cancel_icon_1),
                                contentDescription = "Close",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Section header for selected/attached files (shown above Browse Files)
                    if (uiState.selectedExistingDocuments.isNotEmpty() || uiState.selectedUploadFiles.isNotEmpty()) {
                        Text(
                            text = stringResource(id = R.string.selected_documents),
                            fontSize = 16.sp,
                            fontFamily = GillSansBold,
                            fontWeight = FontWeight.Bold,
                            color = ColorTokens.BluePrimary,
                            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                        )
                    }

                    // Render existing attached documents (Loaded from matter details)
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
                                fontFamily = GillSans,
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
                                text = doc.name ?: "",
                                fontSize = 14.sp,
                                fontFamily = GillSans,
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
                                fontFamily = GillSans,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = onBrowseClick,
                                colors = ButtonDefaults.buttonColors(containerColor = ColorTokens.BluePrimary),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Browse Files",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontFamily = GillSansBold,
                                    fontWeight = FontWeight.Bold
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
                                fontFamily = GillSansBold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        Button(
                            onClick = {
                                viewModel.uploadAndSaveDocuments(onSuccess = {})
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ColorTokens.BluePrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .width(135.dp)
                                .height(40.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Save",
                                fontFamily = GillSansBold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Full Edit Metadata Dialog with Add Tag support reused from Documents module
        uiState.editMetadataFileIndex?.let { index ->
            val doc = uiState.selectedUploadFiles.getOrNull(index)
            if (doc != null) {
                val viewDoc = ViewDocumentsModel().apply {
                    this.name = doc.name
                    this.description = doc.description
                    this.expiration_date = doc.expiration_date
                    this.tag = doc.tags_list ?: JSONObject()
                }

                EditMetadataDialog(
                    doc = viewDoc,
                    initialDownloadDisabled = doc.isIsenabled,
                    initialEncrypted = doc.isencrypted ?: false,
                    isStaged = true,
                    onSave = { updatedName, updatedDesc, updatedExp, isDownloadDisabled, isEncrypted, tagsObj ->
                        viewModel.updateUploadFileWithTags(
                            index = index,
                            name = updatedName,
                            description = updatedDesc,
                            expDate = updatedExp,
                            isDownloadDisabled = isDownloadDisabled,
                            isEncrypted = isEncrypted,
                            tags = tagsObj
                        )
                        viewModel.setEditMetadataFileIndex(null)
                    },
                    onDismiss = { viewModel.setEditMetadataFileIndex(null) }
                )
            }
        }

        if (uiState.showSuccessDialog) {
            AppDialog(
                title = "Success",
                onDismiss = {
                    viewModel.consumeUpdateSuccess()
                    onCancel()
                },
                onConfirm = {
                    viewModel.consumeUpdateSuccess()
                    onCancel()
                },
                confirmText = "OK"
            ) {
                Text(
                    text = uiState.successMessage.ifEmpty { "Documents saved successfully." },
                    fontFamily = GillSans,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Black
                )
            }
        }

        if (uiState.alertMessage != null) {
            AppDialog(
                title = uiState.alertTitle ?: "Alert",
                onDismiss = { viewModel.dismissAlert() },
                onConfirm = { viewModel.dismissAlert() },
                confirmText = "OK"
            ) {
                Text(
                    text = uiState.alertMessage ?: "",
                    fontFamily = GillSans,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Black
                )
            }
        }

        // Document Preview Dialog
        uiState.previewDocUrl?.let { url ->
            com.digicoffer.lauditor.feature.documents.presentation.screen.DocumentViewerDialog(
                url = url,
                docName = uiState.previewDocModel?.name ?: "Preview",
                contentType = uiState.previewDocModel?.content_type ?: "",
                onDismiss = { viewModel.closePreview() }
            )
        }

        if (uiState.isUploadingDocuments || uiState.isLoading) {
            AppLoader()
        }

        // In-App Toast with App Logo
        com.digicoffer.lauditor.core.ui.common.feedback.AppToast(
            message = uiState.toastMessage,
            onDismiss = { viewModel.clearToastMessage() }
        )
    }
}
