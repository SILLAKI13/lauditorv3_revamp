package com.digicoffer.lauditor.feature.appointments.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.Appointments.Models.AppointmentModel
import com.digicoffer.lauditor.R
import java.util.Locale

@Composable
fun HistoryOverlayScreen(
    clientName: String,
    historyList: List<AppointmentModel>,
    noteAddingMap: Map<String, String>,
    noteEditingMap: Map<String, String>,
    noteEditingState: Map<String, Boolean>,
    noteExpandedState: Map<String, Boolean>,
    onCloseClick: () -> Unit,
    onAddNoteClick: (String) -> Unit,
    onCancelNoteClick: (String) -> Unit,
    onSaveNewNote: (String) -> Unit,
    onSaveEditedNote: (String, String) -> Unit,
    onEditNoteClick: (String, String) -> Unit,
    onDeleteNoteClick: (String, String) -> Unit,
    onNoteDraftChanged: (String, String) -> Unit,
    onNoteEditDraftChanged: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE4F2FF))
            .padding(10.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.back_arrow),
                contentDescription = "Close History",
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color(0xFF004D87)),
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onCloseClick() }
                    .padding(10.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "Appointment History",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF004D87),
                fontFamily = FontFamily(Font(R.font.gill_sans_regular))
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Client initials profile avatar
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(30.dp)
                    .background(Color(0xFF004D87), shape = CircleShape)
            ) {
                val initials = if (clientName.isNotEmpty()) {
                    clientName.take(1).uppercase(Locale.ROOT)
                } else {
                    "C"
                }
                Text(
                    text = initials,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = clientName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF004D87),
                fontFamily = FontFamily(Font(R.font.gill_sans_regular))
            )
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = Color(0xFFB0BEC5),
            modifier = Modifier.padding(top = 6.dp, bottom = 6.dp)
        )

        // Timeline LazyColumn list view
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 10.dp)
        ) {
            itemsIndexed(
                items = historyList,
                key = { _, item -> item.id }
            ) { index, item ->
                // Calculate ordinal title e.g. "1st Appointment"
                val appointmentNumber = historyList.size - index
                val title = "${getOrdinal(appointmentNumber)} Appointment"

                HistoryTimelineCard(
                    appointment = item,
                    appointmentTitle = title,
                    noteAddingDraft = noteAddingMap[item.id] ?: "",
                    noteEditingMap = noteEditingMap,
                    noteEditingState = noteEditingState,
                    isEditorExpanded = noteExpandedState[item.id] ?: false,
                    onAddNoteClick = { onAddNoteClick(item.id) },
                    onCancelNoteClick = { onCancelNoteClick(item.id) },
                    onSaveNewNote = { onSaveNewNote(item.id) },
                    onSaveEditedNote = { noteId -> onSaveEditedNote(item.id, noteId) },
                    onEditNoteClick = { noteId, noteText -> onEditNoteClick(noteId, noteText) },
                    onDeleteNoteClick = { noteId -> onDeleteNoteClick(item.id, noteId) },
                    onNoteDraftChanged = { text -> onNoteDraftChanged(item.id, text) },
                    onNoteEditDraftChanged = { noteId, text -> onNoteEditDraftChanged(noteId, text) }
                )
            }
        }
    }
}

private fun getOrdinal(i: Int): String {
    val suffixes = arrayOf("th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th")
    return when (i % 100) {
        11, 12, 13 -> "${i}th"
        else -> "$i${suffixes[i % 10]}"
    }
}
