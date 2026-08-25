package com.digicoffer.lauditor.feature.appointments.presentation.screen

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
import com.digicoffer.lauditor.Appointments.Models.AppointmentModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.ui.common.feedback.AppLoader
import com.digicoffer.lauditor.feature.appointments.presentation.components.AppointmentCardItem
import com.digicoffer.lauditor.feature.appointments.presentation.components.HistoryOverlayScreen
import com.digicoffer.lauditor.feature.appointments.presentation.state.AppointmentsUiEvent
import com.digicoffer.lauditor.feature.appointments.presentation.state.AppointmentsUiState
import com.digicoffer.lauditor.feature.appointments.presentation.viewmodel.AppointmentsViewModel
import com.digicoffer.lauditor.feature.notifications.presentation.components.NotificationsSearchBar

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
    // Local Dialog States
    var pendingCancelAppointment by remember { mutableStateOf<AppointmentModel?>(null) }
    var pendingDeleteAppointment by remember { mutableStateOf<AppointmentModel?>(null) }
    var pendingDeleteNote by remember { mutableStateOf<Pair<String, String>?>(null) } // appointmentId to noteId

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE4F2FF))
    ) {
        // Main Content Switch: Overlay vs Main Listing
        if (uiState.historyClientId.isNotEmpty()) {
            HistoryOverlayScreen(
                clientName = uiState.historyClientName,
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
                    onEvent(AppointmentsUiEvent.ToggleNotesExpanded(aptId))
                },
                onSaveNewNote = { aptId ->
                    onEvent(AppointmentsUiEvent.SaveNewNote(aptId))
                },
                onSaveEditedNote = { aptId, noteId ->
                    onEvent(AppointmentsUiEvent.SaveEditedNote(aptId, noteId))
                },
                onEditNoteClick = { noteId, noteText ->
                    // Set note editing draft, editing mode, and expand textfield notes editor
                    onEvent(AppointmentsUiEvent.StartEditingNote(noteId, noteText))
                    // Loop through list to find which appointment note belongs to and expand it
                    val appointment = uiState.historyList.firstOrNull { apt ->
                        for (i in 0 until apt.notes.length()) {
                            if (apt.notes.optJSONObject(i)?.optString("id") == noteId) return@firstOrNull true
                        }
                        false
                    }
                    appointment?.let { onEvent(AppointmentsUiEvent.ToggleNotesExpanded(it.id)) }
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
                NotificationsSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = { onEvent(AppointmentsUiEvent.SearchQueryChanged(it)) },
                    placeholder = stringResource(id = R.string.search_appointments),
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
                                onCancelClick = { pendingCancelAppointment = item },
                                onDeleteClick = { pendingDeleteAppointment = item },
                                onVideoCallClick = { onVideoCallClick(item) },
                                onChatClick = { onChatClick(item) }
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
            CustomConfirmationDialog(
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
            CustomConfirmationDialog(
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
            CustomConfirmationDialog(
                title = "Delete Note",
                message = "Are you sure you want to delete this note?",
                onConfirm = {
                    onEvent(AppointmentsUiEvent.DeleteNote(pair.first, pair.second))
                    pendingDeleteNote = null
                },
                onDismiss = { pendingDeleteNote = null }
            )
        }
    }
}

@Composable
private fun CustomConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            shape = RoundedCornerShape(6.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                // Close button at top right
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.simple_cancel),
                        contentDescription = "Cancel",
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(30.dp)
                            .clickable { onDismiss() }
                    )
                }

                // Title
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF004D87), // @color/blue
                    fontFamily = FontFamily(Font(R.font.gill_sans_bold)),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Message text
                Text(
                    text = message,
                    fontSize = 17.sp,
                    color = Color.Black,
                    fontFamily = FontFamily(Font(R.font.gill_sans)),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(15.dp))

                // Buttons row (No on left, Yes on right)
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    // No Button
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEEEEE)), // @color/grey_text_color
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDDDDE)), // @color/dark_grey
                        modifier = Modifier
                            .width(80.dp)
                            .height(40.dp)
                    ) {
                        Text(
                            text = "No",
                            color = Color.Black,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily(Font(R.font.gill_sans_bold))
                        )
                    }

                    Spacer(modifier = Modifier.width(30.dp))

                    // Yes Button
                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D87)), // @color/blue
                        modifier = Modifier
                            .width(80.dp)
                            .height(40.dp)
                    ) {
                        Text(
                            text = "Yes",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily(Font(R.font.gill_sans_bold))
                        )
                    }
                }
            }
        }
    }
}
