package com.digicoffer.lauditor.feature.email.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.ui.common.feedback.AppEmptyState
import com.digicoffer.lauditor.core.ui.common.feedback.AppLoader
import com.digicoffer.lauditor.core.ui.common.search.AppSearchField
import com.digicoffer.lauditor.feature.email.presentation.components.AttachDocumentDialog
import com.digicoffer.lauditor.feature.email.presentation.components.ComposeEmailDialog
import com.digicoffer.lauditor.feature.email.presentation.components.EmailCard
import com.digicoffer.lauditor.feature.email.presentation.components.EmailProviderSelector
import com.digicoffer.lauditor.feature.email.presentation.components.SaveAttachmentDialog
import com.digicoffer.lauditor.feature.email.presentation.state.EmailUiEvent
import com.digicoffer.lauditor.feature.email.presentation.state.EmailUiState
import com.digicoffer.lauditor.feature.email.presentation.viewmodel.EmailViewModel

private val GillSans = FontFamily(Font(R.font.gill_sans))

@Composable
fun EmailRoute(
    viewModel: EmailViewModel,
    onLaunchAuthUrl: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.authUrlToLaunch) {
        uiState.authUrlToLaunch?.let { url ->
            onLaunchAuthUrl(url)
            viewModel.onEvent(EmailUiEvent.ClearAuthUrl)
        }
    }

    EmailScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}

@Composable
fun EmailScreen(
    uiState: EmailUiState,
    onEvent: (EmailUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            onEvent(EmailUiEvent.DismissFeedbackMessage)
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            onEvent(EmailUiEvent.DismissFeedbackMessage)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFE8F1FA),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Provider Selection Card (if active)
            if (uiState.showProviderSelector) {
                EmailProviderSelector(
                    selectedProvider = uiState.provider,
                    onProviderSelected = { onEvent(EmailUiEvent.SelectProvider(it)) },
                    onContinueClick = { onEvent(EmailUiEvent.ConfirmProvider) }
                )
            }

            // Search Bar
            AppSearchField(
                value = uiState.searchQuery,
                onValueChange = { onEvent(EmailUiEvent.SearchQueryChanged(it)) },
                onSearchClick = { onEvent(EmailUiEvent.ExecuteSearch) },
                onClearClick = { onEvent(EmailUiEvent.ClearSearch) },
                placeholder = "Search emails...",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )

            // Actions & Pagination Ribbon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Controls: Compose Button + Inbox Count Card + First Page Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Compose icon button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .shadow(2.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { onEvent(EmailUiEvent.OpenCompose) },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.compose),
                            contentDescription = "Compose",
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Inbox Card
                    Surface(
                        modifier = Modifier.shadow(2.dp, RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.inbox),
                                contentDescription = "Inbox",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Inbox ${uiState.labelInfo?.messagesTotal ?: 0}",
                                fontFamily = GillSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF004D87)
                            )
                        }
                    }

                    // "First" button
                    Button(
                        onClick = { onEvent(EmailUiEvent.FirstPage) },
                        enabled = uiState.currentPageIndex > 0,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF28A745),
                            disabledContainerColor = Color(0xFF94A3B8)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(
                            text = "First",
                            fontFamily = GillSans,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }

                // Right Controls: Pagination Arrows
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Prev Page Arrow
                    Image(
                        painter = painterResource(id = R.drawable.left_arrow),
                        contentDescription = "Previous Page",
                        modifier = Modifier
                            .size(28.dp)
                            .alpha(if (uiState.canGoPrev) 1f else 0.3f)
                            .clickable(enabled = uiState.canGoPrev) {
                                onEvent(EmailUiEvent.PrevPage)
                            }
                    )

                    // Next Page Arrow
                    Image(
                        painter = painterResource(id = R.drawable.right_arrow),
                        contentDescription = "Next Page",
                        modifier = Modifier
                            .size(28.dp)
                            .alpha(if (uiState.canGoNext) 1f else 0.3f)
                            .clickable(enabled = uiState.canGoNext) {
                                onEvent(EmailUiEvent.NextPage)
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main Message Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    AppLoader()
                } else if (uiState.filteredMessages.isEmpty()) {
                    AppEmptyState(
                        title = "No Emails",
                        description = "There are no messages in this inbox.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(
                            items = uiState.filteredMessages,
                            key = { it.msgId }
                        ) { message ->
                            EmailCard(
                                message = message,
                                onAttachmentClick = { attachment ->
                                    onEvent(EmailUiEvent.OpenSaveAttachment(message, attachment))
                                }
                            )
                        }
                    }
                }
            }
        }

        // Compose Email Dialog
        if (uiState.showComposeDialog) {
            ComposeEmailDialog(
                to = uiState.composeTo,
                subject = uiState.composeSubject,
                body = uiState.composeBody,
                attachments = uiState.composeAttachments,
                isSending = uiState.isSendingEmail,
                onToChange = { onEvent(EmailUiEvent.ComposeToChanged(it)) },
                onSubjectChange = { onEvent(EmailUiEvent.ComposeSubjectChanged(it)) },
                onBodyChange = { onEvent(EmailUiEvent.ComposeBodyChanged(it)) },
                onRemoveAttachment = { onEvent(EmailUiEvent.RemoveComposeAttachment(it)) },
                onAttachDocClick = { onEvent(EmailUiEvent.OpenAttachDocPicker) },
                onSendClick = { onEvent(EmailUiEvent.SendEmailClicked) },
                onDismiss = { onEvent(EmailUiEvent.DismissCompose) }
            )
        }

        // Attach Document from Lauditor Picker Dialog
        if (uiState.showAttachDocDialog) {
            AttachDocumentDialog(
                categoryTab = uiState.attachCategoryTab,
                isMatterTab = uiState.attachIsMatterTab,
                clientsList = uiState.clientsList,
                mattersList = uiState.mattersList,
                groupsList = uiState.groupsList,
                selectedClientId = uiState.selectedAttachClientId,
                selectedClientName = uiState.selectedAttachClientName,
                selectedMatterId = uiState.selectedAttachMatterId,
                selectedMatterName = uiState.selectedAttachMatterName,
                selectedGroupIds = uiState.selectedAttachGroupIds,
                availableDocs = uiState.availableAttachDocs,
                selectedDocs = uiState.selectedAttachDocs,
                searchQuery = uiState.attachDocSearchQuery,
                isLoading = uiState.isLoadingAttachDocs,
                onCategoryTabChange = { onEvent(EmailUiEvent.AttachCategoryTabChanged(it)) },
                onSubTabChange = { onEvent(EmailUiEvent.AttachSubTabChanged(it)) },
                onSelectClient = { onEvent(EmailUiEvent.SelectAttachClient(it)) },
                onSelectMatter = { onEvent(EmailUiEvent.SelectAttachMatter(it)) },
                onToggleGroup = { onEvent(EmailUiEvent.ToggleAttachGroup(it)) },
                onToggleDoc = { doc, isSelected -> onEvent(EmailUiEvent.ToggleDocSelection(doc, isSelected)) },
                onSearchChange = { onEvent(EmailUiEvent.AttachDocSearchChanged(it)) },
                onConfirm = { onEvent(EmailUiEvent.ConfirmAttachDocs) },
                onDismiss = { onEvent(EmailUiEvent.DismissAttachDocPicker) }
            )
        }

        // Save Attachment to Lauditor Dialog
        if (uiState.showSaveAttachmentDialog) {
            SaveAttachmentDialog(
                targetAttachment = uiState.saveAttachmentTarget,
                categoryTab = uiState.saveCategoryTab,
                isMatterTab = uiState.saveIsMatterTab,
                clientsList = uiState.clientsList,
                mattersList = uiState.mattersList,
                groupsList = uiState.groupsList,
                clientGroups = uiState.saveClientGroups,
                selectedClientId = uiState.saveSelectedClientId,
                selectedClientName = uiState.saveSelectedClientName,
                selectedMatterId = uiState.saveSelectedMatterId,
                selectedMatterName = uiState.saveSelectedMatterName,
                selectedGroupIds = uiState.saveSelectedGroupIds,
                isSaving = uiState.isSavingAttachment,
                onCategoryTabChange = { onEvent(EmailUiEvent.SaveCategoryTabChanged(it)) },
                onSubTabChange = { onEvent(EmailUiEvent.SaveSubTabChanged(it)) },
                onSelectClient = { onEvent(EmailUiEvent.SelectSaveClient(it)) },
                onSelectMatter = { onEvent(EmailUiEvent.SelectSaveMatter(it)) },
                onToggleGroup = { onEvent(EmailUiEvent.ToggleSaveGroup(it)) },
                onConfirm = { onEvent(EmailUiEvent.ConfirmSaveAttachment) },
                onDismiss = { onEvent(EmailUiEvent.DismissSaveAttachment) }
            )
        }

        // No Subject Warning Alert
        if (uiState.showNoSubjectAlert) {
            AlertDialog(
                onDismissRequest = { onEvent(EmailUiEvent.DismissNoSubjectAlert) },
                title = {
                    Text(
                        text = "Send Without Subject?",
                        fontFamily = GillSans,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF004D87)
                    )
                },
                text = {
                    Text(
                        text = "This email does not have a subject or body. Are you sure you want to send it?",
                        fontFamily = GillSans,
                        fontSize = 14.sp,
                        color = Color(0xFF334155)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { onEvent(EmailUiEvent.ConfirmSendNoSubject) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D87))
                    ) {
                        Text("Send Anyway", color = Color.White)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { onEvent(EmailUiEvent.DismissNoSubjectAlert) }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
