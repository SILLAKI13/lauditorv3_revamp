package com.digicoffer.lauditor.feature.appointments.presentation.screen

import com.digicoffer.lauditor.core.ui.common.animation.fallDownItem
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import java.util.Locale
import com.digicoffer.lauditor.Appointments.Models.AppointmentModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.ui.common.dialogs.AppConfirmationDialog
import com.digicoffer.lauditor.core.ui.common.dialogs.AppDialog
import com.digicoffer.lauditor.core.ui.common.feedback.AppLoader
import com.digicoffer.lauditor.feature.appointments.presentation.components.AppointmentCardItem
import com.digicoffer.lauditor.feature.appointments.presentation.components.HistoryOverlayScreen
import com.digicoffer.lauditor.feature.appointments.presentation.components.SettlementHistoryScreen
import com.digicoffer.lauditor.feature.appointments.presentation.components.formatAppointmentDateTime
import com.digicoffer.lauditor.feature.appointments.presentation.state.AppointmentsUiEvent
import com.digicoffer.lauditor.feature.appointments.presentation.state.AppointmentsUiState
import com.digicoffer.lauditor.feature.appointments.presentation.viewmodel.AppointmentsViewModel
import com.digicoffer.lauditor.core.ui.common.search.AppSearchField

@Composable
fun AppointmentsRoute(
    viewModel: AppointmentsViewModel,
    onChatClick: (AppointmentModel) -> Unit,
    onVideoCallClick: (AppointmentModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    AppointmentsScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onChatClick = onChatClick,
        onVideoCallClick = onVideoCallClick,
        modifier = modifier
    )
}

@Composable
fun AppointmentsScreen(
    uiState: AppointmentsUiState,
    onEvent: (AppointmentsUiEvent) -> Unit,
    onChatClick: (AppointmentModel) -> Unit,
    onVideoCallClick: (AppointmentModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Local Dialog States
    var pendingCancelAppointment by remember { mutableStateOf<AppointmentModel?>(null) }
    var pendingDeleteAppointment by remember { mutableStateOf<AppointmentModel?>(null) }
    var pendingDeleteNote by remember { mutableStateOf<Pair<String, String>?>(null) } // appointmentId to noteId

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE4F2FF))
    ) {
        // Main Content Switch: Settlement History vs Client History vs Main Listing
        if (uiState.settlementClientId.isNotEmpty()) {
            SettlementHistoryScreen(
                uiState = uiState,
                onEvent = onEvent
            )
        } else if (uiState.historyClientId.isNotEmpty()) {
            HistoryOverlayScreen(
                clientName = uiState.historyClientName,
                clientProfilePic = uiState.historyClientProfilePic,
                historyList = uiState.historyList,
                noteAddingMap = uiState.noteAddingMap,
                noteEditingMap = uiState.noteEditingMap,
                noteEditingState = uiState.noteEditingState,
                noteExpandedState = uiState.noteExpandedState,
                onCloseClick = { onEvent(AppointmentsUiEvent.CloseHistory) },
                onAddNoteClick = { aptId ->
                    onEvent(AppointmentsUiEvent.NoteAddingDraftChanged(aptId, ""))
                    onEvent(AppointmentsUiEvent.ToggleNotesExpanded(aptId))
                },
                onCancelNoteClick = { aptId ->
                    val apt = uiState.historyList.firstOrNull { it.id == aptId }
                    var editingNoteId: String? = null
                    if (apt != null) {
                        for (i in 0 until apt.notes.length()) {
                            val nId = apt.notes.optJSONObject(i)?.optString("id") ?: ""
                            if (uiState.noteEditingState[nId] == true) {
                                editingNoteId = nId
                                break
                            }
                        }
                    }
                    if (editingNoteId != null) {
                        onEvent(AppointmentsUiEvent.CancelEditingNote(aptId, editingNoteId))
                    } else {
                        onEvent(AppointmentsUiEvent.ToggleNotesExpanded(aptId))
                    }
                },
                onSaveNewNote = { aptId ->
                    onEvent(AppointmentsUiEvent.SaveNewNote(aptId))
                },
                onSaveEditedNote = { aptId, noteId ->
                    onEvent(AppointmentsUiEvent.SaveEditedNote(aptId, noteId))
                },
                onEditNoteClick = { noteId, noteText ->
                    val appointment = uiState.historyList.firstOrNull { apt ->
                        for (i in 0 until apt.notes.length()) {
                            if (apt.notes.optJSONObject(i)?.optString("id") == noteId) return@firstOrNull true
                        }
                        false
                    }
                    appointment?.let {
                        onEvent(AppointmentsUiEvent.StartEditingNote(it.id, noteId, noteText))
                    }
                },
                onDeleteNoteClick = { aptId, noteId ->
                    pendingDeleteNote = Pair(aptId, noteId)
                },
                onNoteDraftChanged = { aptId, text ->
                    onEvent(AppointmentsUiEvent.NoteAddingDraftChanged(aptId, text))
                },
                onNoteEditDraftChanged = { noteId, text ->
                    onEvent(AppointmentsUiEvent.NoteEditingDraftChanged(noteId, text))
                }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            ) {
                // Search Bar
                AppSearchField(
                    value = uiState.searchQuery,
                    onValueChange = { onEvent(AppointmentsUiEvent.SearchQueryChanged(it)) },
                    placeholder = stringResource(id = R.string.search_appointments),
                    onClearClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        onEvent(AppointmentsUiEvent.SearchQueryChanged(""))
                    },
                    onSearchKeyboardAction = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Empty State vs Listing
                if (uiState.filteredList.isEmpty()) {
                    val isSearching = uiState.searchQuery.isNotEmpty()
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(32.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.empty_appointments),
                            contentDescription = "No appointments",
                            modifier = Modifier.size(if (isSearching) 100.dp else 120.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (isSearching) "No matching appointments" else "No Appointments Yet!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF004D87),
                            fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (isSearching) "Try adjusting your search terms" else "There are no Appointment(s)under here.",
                            fontSize = 14.sp,
                            color = Color(0xFF757575),
                            fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // Lazy List of Appointments
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        itemsIndexed(
                            items = uiState.currentPageList,
                            key = { _, item -> item.id }
                        ) { index, item ->
                            AppointmentCardItem(
                                appointment = item,
                                isMenuExpanded = (index == uiState.expandedCardPosition),
                                onMenuToggle = { onEvent(AppointmentsUiEvent.ToggleActionMenu(index)) },
                                onHistoryClick = { onEvent(AppointmentsUiEvent.OpenHistory(item)) },
                                onSettlementHistoryClick = { onEvent(AppointmentsUiEvent.OpenSettlementHistory(item)) },
                                onCancelClick = { pendingCancelAppointment = item },
                                onDeleteClick = { pendingDeleteAppointment = item },
                                onVideoCallClick = { onVideoCallClick(item) },
                                onChatClick = { onChatClick(item) },
                                modifier = Modifier.fallDownItem(index = index, triggerKey = uiState.currentPage)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Bottom Pagination Buttons
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        val isPrevEnabled = uiState.currentPage > 0
                        val isNextEnabled = uiState.currentPage < uiState.totalPages - 1

                        Button(
                            onClick = { onEvent(AppointmentsUiEvent.PagePrev) },
                            enabled = isPrevEnabled,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF004D87),
                                disabledContainerColor = Color(0xFF004D87)
                            ),
                            modifier = Modifier
                                .height(40.dp)
                                .alpha(if (isPrevEnabled) 1.0f else 0.5f)
                        ) {
                            Text(
                                text = stringResource(id = R.string.prev_),
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily(Font(R.font.gill_sans))
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Button(
                            onClick = { onEvent(AppointmentsUiEvent.PageNext) },
                            enabled = isNextEnabled,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF004D87),
                                disabledContainerColor = Color(0xFF004D87)
                            ),
                            modifier = Modifier
                                .height(40.dp)
                                .alpha(if (isNextEnabled) 1.0f else 0.5f)
                        ) {
                            Text(
                                text = stringResource(id = R.string.next_),
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily(Font(R.font.gill_sans))
                            )
                        }
                    }
                }
            }
        }

        // Action Loader Overlay
        if (uiState.isLoading) {
            AppLoader()
        }

        // ==================== LOCAL DIALOGS ====================

        // 1. Cancel Appointment Alert
        pendingCancelAppointment?.let { appointment ->
            AppConfirmationDialog(
                title = "Cancel Appointment",
                message = "Are you sure you want to cancel this appointment? This action cannot be undone.",
                onConfirm = {
                    onEvent(AppointmentsUiEvent.CancelAppointment(appointment))
                    pendingCancelAppointment = null
                },
                onDismiss = { pendingCancelAppointment = null }
            )
        }

        // 2. Delete Appointment Alert
        pendingDeleteAppointment?.let { appointment ->
            AppConfirmationDialog(
                title = "Confirmation",
                message = "Are you sure you want to delete this appointment? This action cannot be undone.",
                onConfirm = {
                    onEvent(AppointmentsUiEvent.DeleteAppointment(appointment))
                    pendingDeleteAppointment = null
                },
                onDismiss = { pendingDeleteAppointment = null }
            )
        }

        // 3. Delete Note Alert
        pendingDeleteNote?.let { pair ->
            AppConfirmationDialog(
                title = "Delete Note",
                message = "Are you sure you want to delete this note?",
                onConfirm = {
                    onEvent(AppointmentsUiEvent.DeleteNote(pair.first, pair.second))
                    pendingDeleteNote = null
                },
                onDismiss = { pendingDeleteNote = null }
            )
        }

        // 4. API Feedback Alert Dialog (Success / Error)
        if (!uiState.alertMessage.isNullOrBlank()) {
            AppDialog(
                title = uiState.alertTitle ?: "Alert",
                confirmText = "OK",
                onConfirm = { onEvent(AppointmentsUiEvent.DismissDialogs) },
                onDismiss = { onEvent(AppointmentsUiEvent.DismissDialogs) },
                content = {
                    Text(
                        text = uiState.alertMessage,
                        fontSize = 15.sp,
                        color = Color.Black,
                        fontFamily = FontFamily(Font(R.font.gill_sans)),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .fillMaxWidth()
                    )
                }
            )
        }
    }
}
