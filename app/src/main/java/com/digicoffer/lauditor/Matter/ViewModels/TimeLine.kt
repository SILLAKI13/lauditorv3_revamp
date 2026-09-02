package com.digicoffer.lauditor.Matter.ViewModels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.Matter.Models.HistoryModel
import com.digicoffer.lauditor.Matter.Models.ViewMatterModel
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.designsystem.colors.ColorTokens
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Locale

private val GillSans = FontFamily(androidx.compose.ui.text.font.Font(R.font.gill_sans))

class TimeLine() : Fragment() {
    var historyList = ArrayList<HistoryModel>()
    var viewMatter = ViewMatter()
    var header_name = ""
    var matter: Matter? = null
    var viewMatterModel: ViewMatterModel? = null

    constructor(
        historyList1: ArrayList<HistoryModel>,
        viewMatter1: ViewMatter,
        header_name1: String,
        matter1: Matter?,
        viewMatterModel: ViewMatterModel?
    ) : this() {
        historyList = historyList1
        viewMatter = viewMatter1
        header_name = header_name1
        matter = matter1
        this.viewMatterModel = viewMatterModel
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme {
                    TimeLineScreen()
                }
            }
        }
    }

    @Composable
    fun TimeLineScreen() {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F7F7))
                .padding(horizontal = 10.dp)
        ) {
            itemsIndexed(historyList) { index, history ->
                TimelineItemRow(index, history)
            }
        }
    }

    @Composable
    fun TimelineItemRow(index: Int, history: HistoryModel) {
        val corp = viewMatterModel?.corporate
        val showCorporateTab = corp != null && corp.length() > 0 && history.from_ts != history.to_ts
        
        var isLauditorSelected by remember { mutableStateOf(true) }

        // States for notes display/edit/add
        var showEditArea by remember { mutableStateOf(false) }
        var showViewArea by remember { mutableStateOf(false) }
        var showAddArea by remember { mutableStateOf(false) }
        
        var noteInputText by remember { mutableStateOf("") }

        val notesText = history.notes
        val hasNotes = notesText != null && notesText.isNotEmpty() && notesText != "null"

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Event Title
                Text(
                    text = history.title ?: "",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Event Date
                Text(
                    text = formatEventDate(history.from_ts),
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Tabs Row (if Corporate is applicable)
                if (showCorporateTab) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { isLauditorSelected = true }
                                .padding(end = 16.dp)
                        ) {
                            RadioButton(
                                selected = isLauditorSelected,
                                onClick = { isLauditorSelected = true },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFF00E1FF),
                                    unselectedColor = Color.Black
                                )
                            )
                            Text(
                                text = "Lauditor Notes",
                                fontSize = 14.sp,
                                color = if (isLauditorSelected) Color(0xFF00E1FF) else Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { isLauditorSelected = false }
                        ) {
                            RadioButton(
                                selected = !isLauditorSelected,
                                onClick = { isLauditorSelected = false },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFF00E1FF),
                                    unselectedColor = Color.Black
                                )
                            )
                            Text(
                                text = "Corporate Notes",
                                fontSize = 14.sp,
                                color = if (!isLauditorSelected) Color(0xFF00E1FF) else Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Tab Content
                if (!showCorporateTab || isLauditorSelected) {
                    // LAUDITOR NOTES
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (hasNotes) {
                            if (!showEditArea && !showViewArea) {
                                // Default preview
                                Text(
                                    text = "${notesText}....",
                                    fontSize = 14.sp,
                                    color = Color.Black,
                                    textDecoration = TextDecoration.Underline,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Edit & View Icon Buttons Row
                                if (!history.allday) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Start
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.edit__icon),
                                            contentDescription = "Edit Note",
                                            tint = Color.Unspecified,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clickable {
                                                    noteInputText = notesText ?: ""
                                                    showEditArea = true
                                                    showViewArea = false
                                                }
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Icon(
                                            painter = painterResource(id = R.drawable.eye_icon),
                                            contentDescription = "View Note",
                                            tint = Color.Unspecified,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clickable {
                                                    showViewArea = true
                                                    showEditArea = false
                                                }
                                        )
                                    }
                                }
                            }

                            // View note state
                            if (showViewArea) {
                                OutlinedTextField(
                                    value = notesText ?: "",
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.LightGray,
                                        unfocusedBorderColor = Color.LightGray
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = { showViewArea = false },
                                        colors = ButtonDefaults.buttonColors(containerColor = ColorTokens.BluePrimary),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(text = "Close", color = Color.White)
                                    }
                                }
                            }

                            // Edit Note state
                            if (showEditArea) {
                                OutlinedTextField(
                                    value = noteInputText,
                                    onValueChange = { noteInputText = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text(text = "Notes") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ColorTokens.BluePrimary,
                                        unfocusedBorderColor = Color.LightGray
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Button(
                                        onClick = { showEditArea = false },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8E8E8)),
                                        border = BorderStroke(1.dp, Color(0xFF888888)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.width(120.dp)
                                    ) {
                                        Text(text = "Cancel", color = Color.Black)
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Button(
                                        onClick = {
                                            if (noteInputText.isNotEmpty()) {
                                                showEditArea = false
                                                viewMatter.callEditNotesWebservice(history.id ?: "", noteInputText.trim())
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = ColorTokens.BluePrimary),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.width(120.dp),
                                        enabled = noteInputText.isNotEmpty()
                                    ) {
                                        Text(text = "Save", color = Color.White)
                                    }
                                }
                            }

                        } else {
                            // No Notes State
                            if (!showAddArea) {
                                Text(
                                    text = "....",
                                    fontSize = 14.sp,
                                    color = Color.Black,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                
                                if (history.from_ts != history.to_ts) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Icon(
                                        painter = painterResource(id = R.drawable.simple_plus),
                                        contentDescription = "Add Note",
                                        tint = Color.Unspecified,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clickable {
                                                noteInputText = ""
                                                showAddArea = true
                                            }
                                    )
                                }
                            }

                            if (showAddArea) {
                                OutlinedTextField(
                                    value = noteInputText,
                                    onValueChange = { if (it.length <= 150) noteInputText = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 90.dp),
                                    minLines = 3,
                                    maxLines = 5,
                                    placeholder = { Text(text = "Notes", color = Color.Gray, fontFamily = GillSans) },
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        fontFamily = GillSans,
                                        fontSize = 15.sp,
                                        color = Color.Black
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ColorTokens.BluePrimary,
                                        unfocusedBorderColor = Color(0xFFDDDDDE),
                                        focusedContainerColor = Color(0xFFEEEEEE),
                                        unfocusedContainerColor = Color(0xFFEEEEEE)
                                    ),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                Text(
                                    text = "${noteInputText.length}/150",
                                    fontFamily = GillSans,
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    modifier = Modifier
                                        .align(Alignment.End)
                                        .padding(top = 2.dp, bottom = 4.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Button(
                                        onClick = { showAddArea = false },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8E8E8)),
                                        border = BorderStroke(1.dp, Color(0xFF888888)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.width(120.dp)
                                    ) {
                                        Text(text = "Cancel", color = Color.Black)
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Button(
                                        onClick = {
                                            if (noteInputText.isNotEmpty()) {
                                                showAddArea = false
                                                viewMatter.callEditNotesWebservice(history.id ?: "", noteInputText.trim())
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = ColorTokens.BluePrimary),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.width(120.dp),
                                        enabled = noteInputText.isNotEmpty()
                                    ) {
                                        Text(text = "Create", color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // CORPORATE NOTES
                    val notesList = history.notes_list
                    if (notesList != null && notesList.length() > 0) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            for (j in 0 until notesList.length()) {
                                val noteObj = notesList.optJSONObject(j) ?: continue
                                val noteText = noteObj.optString("notes", "")
                                val addedBy = noteObj.optString("added_by", "")
                                val addOn = noteObj.optString("add_on", "")
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    // Avatar Circle
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(ColorTokens.BluePrimary, shape = CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (addedBy.isNotEmpty()) addedBy.substring(0, 1).uppercase() else "",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.width(10.dp))
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = addedBy,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color.Black
                                        )
                                        Text(
                                            text = addOn,
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = noteText,
                                            fontSize = 13.sp,
                                            color = Color.DarkGray
                                        )
                                    }
                                }
                                if (j < notesList.length() - 1) {
                                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.5.dp)
                                }
                            }
                        }
                    } else {
                        Text(text = "No Corporate Notes available.", fontSize = 13.sp, color = Color.Gray)
                    }
                }
            }
        }
    }

    private fun formatEventDate(dateStr: String?): String {
        if (dateStr.isNullOrEmpty()) return ""
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
            val outputFormat = SimpleDateFormat("MMMM dd, yyyy | hh:mm a", Locale.ENGLISH)
            val date = inputFormat.parse(dateStr)
            if (date != null) outputFormat.format(date) else dateStr ?: ""
        } catch (e: Exception) {
            dateStr ?: ""
        }
    }
}
