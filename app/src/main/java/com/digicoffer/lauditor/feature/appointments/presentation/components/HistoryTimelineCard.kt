package com.digicoffer.lauditor.feature.appointments.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.Appointments.Models.AppointmentModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.ui.common.badges.AppStatusBadge
import com.digicoffer.lauditor.core.ui.common.badges.AppStatusStyle
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HistoryTimelineCard(
    appointment: AppointmentModel,
    appointmentTitle: String,
    noteAddingDraft: String,
    noteEditingMap: Map<String, String>,
    noteEditingState: Map<String, Boolean>,
    isEditorExpanded: Boolean,
    onAddNoteClick: () -> Unit,
    onCancelNoteClick: () -> Unit,
    onSaveNewNote: () -> Unit,
    onSaveEditedNote: (String) -> Unit,
    onEditNoteClick: (String, String) -> Unit,
    onDeleteNoteClick: (String) -> Unit,
    onNoteDraftChanged: (String) -> Unit,
    onNoteEditDraftChanged: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Check if any note in this appointment is currently in editing state
    val editingNoteId = getEditingNoteId(appointment.notes, noteEditingState)
    val isEditing = editingNoteId != null

    val notesLength = appointment.notes.length()
    val showAddNoteButton = !isEditorExpanded && notesLength == 0
    val showNotesContainer = !isEditorExpanded && notesLength > 0

    val cleanStatus = (appointment.appointment_status ?: "").lowercase(Locale.ROOT).trim()
    val dotBrush = when (cleanStatus) {
        "completed" -> androidx.compose.ui.graphics.Brush.verticalGradient(
            listOf(Color(0xFF007705), Color(0xFF3FAF3F), Color(0xFF007705))
        )
        "cancelled", "canceled" -> androidx.compose.ui.graphics.SolidColor(Color(0xFFE53E3E))
        "upcoming", "ongoing" -> androidx.compose.ui.graphics.SolidColor(Color(0xFF004D87))
        "payment_pending", "pending" -> androidx.compose.ui.graphics.SolidColor(Color(0xFFF57C00))
        else -> androidx.compose.ui.graphics.SolidColor(Color(0xFF004D87))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = 4.dp)
    ) {
        // Timeline indicator column (left)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(end = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .size(15.dp)
                    .background(dotBrush, shape = CircleShape)
            )

            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .padding(top = 2.dp)
                    .background(Color(0xFFD2C9C9))
            )
        }

        // Content column (right)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 10.dp)
        ) {
            // Appointment Header (Title + Add Note Button)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = appointmentTitle,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF004D87),
                    fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                    modifier = Modifier.weight(1f)
                )

                if (showAddNoteButton) {
                    Button(
                        onClick = { onAddNoteClick() },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D87)),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.add_note),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily(Font(R.font.gill_sans))
                        )
                    }
                }
            }

            // Date and time
            val formattedDateTime = formatHistoryDateTime(
                appointment.appointment_from,
                appointment.appointment_to
            )
            Text(
                text = formattedDateTime,
                fontSize = 15.sp,
                color = Color(0xFF546E7A),
                fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                modifier = Modifier.padding(top = 4.dp)
            )

            // Payment info
            val symbol = appointment.payment?.symbol ?: ""
            val amount = appointment.payment?.amount_paid ?: "0"
            val paymentLabel = appointment.payment?.label?.takeIf { it.isNotBlank() } ?: "$symbol$amount"
            Text(
                text = "Payment: $paymentLabel",
                fontSize = 15.sp,
                color = Color(0xFF546E7A),
                fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                modifier = Modifier.padding(top = 4.dp)
            )

            // Status Badge
            val cleanHistoryStatus = appointment.appointment_status.lowercase(Locale.ROOT).trim()
            val (displayHistoryStatus, historyStatusStyle) = when (cleanHistoryStatus) {
                "completed" -> Pair("Completed", AppStatusStyle.SUCCESS)
                "cancelled", "canceled" -> Pair("Cancelled", AppStatusStyle.ERROR)
                "upcoming" -> Pair("Upcoming", AppStatusStyle.INFO)
                "ongoing" -> Pair("Ongoing", AppStatusStyle.INFO)
                "payment_pending", "pending" -> Pair("Payment Pending", AppStatusStyle.WARNING)
                else -> Pair(if (cleanHistoryStatus.isNotEmpty()) appointment.appointment_status.replaceFirstChar { it.uppercase() } else "Scheduled", AppStatusStyle.NEUTRAL)
            }
            AppStatusBadge(
                text = displayHistoryStatus,
                style = historyStatusStyle,
                modifier = Modifier.padding(top = 10.dp)
            )

            // Note editor box
            if (isEditorExpanded) {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        val editorText = if (isEditing) {
                            noteEditingMap[editingNoteId] ?: ""
                        } else {
                            noteAddingDraft
                        }

                        OutlinedTextField(
                            value = editorText,
                            onValueChange = {
                                if (isEditing) {
                                    onNoteEditDraftChanged(editingNoteId!!, it)
                                } else {
                                    onNoteDraftChanged(it)
                                }
                            },
                            placeholder = { Text("Notes", color = Color(0xFF546E7A)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFECEFF1),
                                unfocusedContainerColor = Color(0xFFECEFF1),
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(4.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 15.sp,
                                fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                                color = Color.Black
                            ),
                            maxLines = 10,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Controls bottom row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${editorText.length}/500",
                                fontSize = 12.sp,
                                color = Color(0xFF546E7A),
                                fontFamily = FontFamily(Font(R.font.gill_sans_regular)),
                                modifier = Modifier.weight(1f)
                            )

                            Button(
                                onClick = { onCancelNoteClick() },
                                shape = RoundedCornerShape(4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCFD8DC)),
                                modifier = Modifier.height(35.dp)
                            ) {
                                Text(
                                    text = "Cancel",
                                    color = Color.Black,
                                    fontSize = 15.sp,
                                    fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            val saveEnabled = editorText.trim().isNotEmpty()
                            Button(
                                onClick = {
                                    if (isEditing) {
                                        onSaveEditedNote(editingNoteId!!)
                                    } else {
                                        onSaveNewNote()
                                    }
                                },
                                enabled = saveEnabled,
                                shape = RoundedCornerShape(4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D87)),
                                modifier = Modifier.height(35.dp)
                            ) {
                                Text(
                                    text = "Save",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontFamily = FontFamily(Font(R.font.gill_sans_regular))
                                )
                            }
                        }
                    }
                }
            }

            // Existing notes list container
            if (showNotesContainer) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    for (i in 0 until notesLength) {
                        val noteObj = appointment.notes.optJSONObject(i) ?: continue
                        val noteId = noteObj.optString("id")
                        val noteText = noteObj.optString("note")
                        val createdOn = noteObj.optString("created_on")

                        HistoryNoteItem(
                            noteId = noteId,
                            noteText = noteText,
                            createdOn = createdOn,
                            onEditClick = { onEditNoteClick(noteId, noteText) },
                            onDeleteClick = { onDeleteNoteClick(noteId) }
                        )
                    }
                }
            }
        }
    }
}

private fun getEditingNoteId(notes: JSONArray, states: Map<String, Boolean>): String? {
    for (i in 0 until notes.length()) {
        val obj = notes.optJSONObject(i) ?: continue
        val noteId = obj.optString("id")
        if (states[noteId] == true) return noteId
    }
    return null
}

private fun formatHistoryDateTime(from: String, to: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
        val dateFrom = parser.parse(from)
        val dateTo = parser.parse(to)
        if (dateFrom != null && dateTo != null) {
            val fromTime = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(dateFrom)
            val toTime = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(dateTo)
            val day = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH).format(dateFrom)
            "$day | $fromTime - $toTime"
        } else {
            "$from - $to"
        }
    } catch (e: Exception) {
        "$from - $to"
    }
}
