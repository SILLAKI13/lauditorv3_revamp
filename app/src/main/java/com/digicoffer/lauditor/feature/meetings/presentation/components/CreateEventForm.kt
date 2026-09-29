package com.digicoffer.lauditor.feature.meetings.presentation.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.TimeZonesDO
import com.digicoffer.lauditor.Matter.Models.ViewMatterModel
import com.digicoffer.lauditor.Meetings.Models.DocumentsDo
import com.digicoffer.lauditor.Meetings.Models.RelationshipsDO
import com.digicoffer.lauditor.Meetings.Models.TeamDo
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.feature.meetings.presentation.state.MeetingsUiEvent
import com.digicoffer.lauditor.feature.meetings.presentation.state.MeetingsUiState
import com.digicoffer.lauditor.feature.meetings.presentation.viewmodel.MeetingsViewModel
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val GillSansFont = FontFamily(Font(R.font.gill_sans_regular))

@Composable
private fun FormLabel(text: String, isMandatory: Boolean = false) {
    Text(
        text = buildAnnotatedString {
            append(text)
            if (isMandatory) {
                withStyle(style = SpanStyle(color = Color.Red)) {
                    append(" *")
                }
            }
        },
        color = Color(0xFF004D87),
        fontFamily = GillSansFont,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp
    )
}

@Composable
private fun FormCustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    height: Dp = 44.dp,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val inputBorderColor = Color(0xFFC0C0C0)
    val inputBgColor = if (enabled) Color(0xFFF9FAFB) else Color(0xFFEEEEEE)
    val textStyle = TextStyle(
        fontSize = 15.sp,
        fontFamily = GillSansFont,
        color = Color.Black
    )

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = textStyle,
        singleLine = singleLine,
        maxLines = maxLines,
        enabled = enabled,
        keyboardOptions = keyboardOptions,
        cursorBrush = SolidColor(Color.Black),
        modifier = modifier
            .fillMaxWidth()
            .then(if (height > 0.dp) Modifier.height(height) else Modifier)
            .background(inputBgColor, shape = RoundedCornerShape(6.dp))
            .border(width = 0.5.dp, color = inputBorderColor, shape = RoundedCornerShape(6.dp)),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = textStyle.copy(color = Color(0xFFA0A0A0))
                        )
                    }
                    innerTextField()
                }
                trailingIcon?.invoke()
            }
        }
    )
}

/**
 * Single-select dropdown with in-place expandable list, search filter, and inner scroll.
 */
@Composable
private fun FormCustomDropdown(
    selectedText: String,
    placeholder: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    onClear: (() -> Unit)? = null,
    searchPlaceholder: String = "",
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val inputBorderColor = Color(0xFFC0C0C0)
    val inputBgColor = if (enabled) Color(0xFFF9FAFB) else Color(0xFFEEEEEE)
    val activeBlue = Color(0xFF004D87)

    val filteredOptions = remember(options, searchQuery) {
        if (searchQuery.isBlank()) options
        else options.filter { it.contains(searchQuery, ignoreCase = true) }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(inputBgColor, shape = RoundedCornerShape(6.dp))
                .border(width = 0.5.dp, color = inputBorderColor, shape = RoundedCornerShape(6.dp))
                .clickable(enabled = enabled) {
                    searchQuery = ""
                    expanded = !expanded
                }
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = selectedText.ifEmpty { placeholder },
                color = if (selectedText.isNotEmpty()) Color.Black else Color(0xFFA0A0A0),
                fontFamily = GillSansFont,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (selectedText.isNotEmpty() && onClear != null) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_close),
                    contentDescription = "Clear",
                    tint = activeBlue,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onClear() }
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.drop_down_blue),
                    contentDescription = "Dropdown icon",
                    tint = activeBlue,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(if (expanded) 180f else 0f)
                )
            }
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 0.5.dp, color = inputBorderColor, shape = RoundedCornerShape(6.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(6.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp)
                ) {
                    if (searchPlaceholder.isNotEmpty() || options.size > 4) {
                        FormCustomTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = if (searchPlaceholder.isNotEmpty()) searchPlaceholder else "Search",
                            height = 38.dp,
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_baseline_search_24),
                                    contentDescription = "Search",
                                    tint = activeBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.5.dp)
                    }

                    if (filteredOptions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No options found",
                                fontFamily = GillSansFont,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                        ) {
                            items(filteredOptions) { option ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            expanded = false
                                            searchQuery = ""
                                            onSelect(option)
                                        }
                                        .padding(horizontal = 8.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = option,
                                        fontFamily = GillSansFont,
                                        fontSize = 14.sp,
                                        color = if (option == selectedText) activeBlue else Color.Black,
                                        fontWeight = if (option == selectedText) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                                HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Multi-select dropdown for Attendees & Attachments matching exact Java XML structure.
 * Features:
 * - Placeholder / trigger field with dropdown icon
 * - In-place expanded card with search input, "Select All" checkbox, and inner-scrolling list
 * - Selected item chips with delete icon below the field
 */
@Composable
private fun <T> AttendeesMultiSelectDropdown(
    label: String,
    placeholder: String,
    searchPlaceholder: String,
    items: List<T>,
    selectedItems: List<T>,
    itemLabel: (T) -> String,
    itemId: (T) -> String,
    onSelectionChanged: (List<T>) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val activeBlue = Color(0xFF004D87)
    val inputBorderColor = Color(0xFFC0C0C0)

    val filteredItems = remember(items, searchQuery) {
        if (searchQuery.isBlank()) items
        else items.filter { itemLabel(it).contains(searchQuery, ignoreCase = true) }
    }

    val isAllSelected = filteredItems.isNotEmpty() && filteredItems.all { item ->
        selectedItems.any { itemId(it) == itemId(item) }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        FormLabel(text = label)
        Spacer(modifier = Modifier.height(4.dp))

        // Selector Input Box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(Color(0xFFF9FAFB), shape = RoundedCornerShape(6.dp))
                .border(width = 0.5.dp, color = inputBorderColor, shape = RoundedCornerShape(6.dp))
                .clickable {
                    expanded = !expanded
                    if (!expanded) searchQuery = ""
                }
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = placeholder,
                color = Color(0xFFA0A0A0),
                fontFamily = GillSansFont,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Icon(
                painter = painterResource(id = R.drawable.drop_down_blue),
                contentDescription = "Dropdown",
                tint = activeBlue,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(if (expanded) 180f else 0f)
            )
        }

        // Expanded Multi-Select Dropdown Card
        if (expanded) {
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 0.5.dp, color = inputBorderColor, shape = RoundedCornerShape(6.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(6.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    // Search Bar
                    FormCustomTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = searchPlaceholder,
                        height = 38.dp,
                        trailingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_baseline_search_24),
                                contentDescription = "Search",
                                tint = activeBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Select All Checkbox
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isAllSelected) {
                                    val idsToRemove = filteredItems.map { itemId(it) }.toSet()
                                    onSelectionChanged(selectedItems.filter { itemId(it) !in idsToRemove })
                                } else {
                                    val existingIds = selectedItems.map { itemId(it) }.toSet()
                                    val newItemsToAdd = filteredItems.filter { itemId(it) !in existingIds }
                                    onSelectionChanged(selectedItems + newItemsToAdd)
                                }
                            }
                            .padding(vertical = 4.dp, horizontal = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isAllSelected,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    val existingIds = selectedItems.map { itemId(it) }.toSet()
                                    val newItemsToAdd = filteredItems.filter { itemId(it) !in existingIds }
                                    onSelectionChanged(selectedItems + newItemsToAdd)
                                } else {
                                    val idsToRemove = filteredItems.map { itemId(it) }.toSet()
                                    onSelectionChanged(selectedItems.filter { itemId(it) !in idsToRemove })
                                }
                            },
                            colors = CheckboxDefaults.colors(checkedColor = activeBlue)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Select All",
                            color = activeBlue,
                            fontFamily = GillSansFont,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 0.5.dp)

                    // Inner Scroll List
                    if (filteredItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No items found",
                                color = Color.Gray,
                                fontFamily = GillSansFont,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 180.dp)
                        ) {
                            items(filteredItems) { item ->
                                val id = itemId(item)
                                val isChecked = selectedItems.any { itemId(it) == id }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isChecked) {
                                                onSelectionChanged(selectedItems.filter { itemId(it) != id })
                                            } else {
                                                onSelectionChanged(selectedItems + item)
                                            }
                                        }
                                        .padding(vertical = 4.dp, horizontal = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            if (checked) {
                                                onSelectionChanged(selectedItems + item)
                                            } else {
                                                onSelectionChanged(selectedItems.filter { itemId(it) != id })
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = activeBlue)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = itemLabel(item),
                                        color = Color.Black,
                                        fontFamily = GillSansFont,
                                        fontSize = 14.sp
                                    )
                                }
                                HorizontalDivider(color = Color(0xFFF8F8F8), thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        }

        // Selected Chips below the field with red delete icon
        if (selectedItems.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(selectedItems) { item ->
                    AssistChip(
                        onClick = {
                            val id = itemId(item)
                            onSelectionChanged(selectedItems.filter { itemId(it) != id })
                        },
                        label = {
                            Text(
                                text = itemLabel(item),
                                fontSize = 12.sp,
                                fontFamily = GillSansFont,
                                color = Color.Black
                            )
                        },
                        trailingIcon = {
                            Image(
                                painter = painterResource(id = R.drawable.delete_de),
                                contentDescription = "Delete",
                                modifier = Modifier.size(14.dp),
                                colorFilter = ColorFilter.tint(Color.Red)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFFF3F3F3)),
                        border = BorderStroke(0.5.dp, Color(0xFFDDDDDE)),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventForm(
    uiState: MeetingsUiState,
    viewModel: MeetingsViewModel,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeBlue = Color(0xFF004D87)
    val inputBorderColor = Color(0xFFC0C0C0)
    val isEditMode = uiState.editingEvent != null
    val editing = uiState.editingEvent

    // Form fields
    var eventType by remember {
        mutableStateOf(
            when (editing?.event_type?.lowercase(Locale.ROOT)) {
                "legal" -> "Legal Matter"
                "general" -> "General Matter"
                "overhead" -> "Overhead"
                "others" -> "Others"
                "reminders" -> "Reminders"
                else -> ""
            }
        )
    }

    var selectedMatter by remember { mutableStateOf<ViewMatterModel?>(null) }
    var taskName by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date())) }
    var startTime by remember { mutableStateOf("10:00") }
    var endTime by remember { mutableStateOf("10:30") }
    var durationText by remember { mutableStateOf("Duration: 30 min") }
    var isAllDay by remember { mutableStateOf(false) }
    var selectedTimezone by remember { mutableStateOf<TimeZonesDO?>(null) }
    var repetition by remember { mutableStateOf("None") }

    var meetingRoomId by remember { mutableStateOf("") }
    var meetingLink by remember { mutableStateOf("") }
    var dialinNumber by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isAddTimesheet by remember { mutableStateOf(true) }

    // Attendees & Attachments selections
    var selectedTeamMembers by remember { mutableStateOf<List<TeamDo>>(emptyList()) }
    var selectedIndividuals by remember { mutableStateOf<List<RelationshipsDO>>(emptyList()) }
    var selectedDocuments by remember { mutableStateOf<List<DocumentsDo>>(emptyList()) }

    // Notifications: Pair of (Unit, Value) e.g. ("Minutes", "10")
    var notificationsList by remember { mutableStateOf(listOf(Pair("Minutes", "10"))) }

    var showEditRecurrenceDialog by remember { mutableStateOf(false) }

    // Dynamic Meeting Link updater
    fun updateMeetingLink(roomId: String = meetingRoomId) {
        if (roomId.isEmpty()) return
        try {
            val dateObj = AndroidUtils.stringToDateTimeDefault(selectedDate, "MMM dd, yyyy")
            val dateStr = AndroidUtils.getDateToString(dateObj, "yyyy-MM-dd") ?: ""
            meetingLink = AndroidUtils.getAVChatUrl(roomId, startTime, endTime, dateStr, Constants.NAME)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Prepopulate on Edit
    LaunchedEffect(editing) {
        if (editing != null) {
            val type = editing.event_type?.lowercase(Locale.ROOT) ?: ""
            eventType = when (type) {
                "legal" -> "Legal Matter"
                "general" -> "General Matter"
                "overhead" -> "Overhead"
                "others" -> "Others"
                "reminders" -> "Reminders"
                else -> ""
            }

            val title = editing.title.orEmpty()
            if (title.contains(" - ")) {
                val parts = title.split(" - ")
                taskName = if (parts.size > 1) parts[1] else title
            } else {
                taskName = title
            }

            if (!editing.date.isNullOrEmpty()) {
                val parsed = AndroidUtils.stringToDateTimeDefault(editing.date, "dd-MM-yyyy")
                selectedDate = AndroidUtils.getDateToString(parsed, "MMM dd, yyyy").orEmpty()
            } else if (!editing.from_ts.isNullOrEmpty()) {
                val parsed = AndroidUtils.stringToDateTimeDefault(editing.from_ts, "yyyy-MM-dd'T'HH:mm:ss")
                selectedDate = AndroidUtils.getDateToString(parsed, "MMM dd, yyyy").orEmpty()
            }

            if (!editing.from_ts.isNullOrEmpty()) {
                val parsed = AndroidUtils.stringToDateTimeDefault(editing.from_ts, "yyyy-MM-dd'T'HH:mm:ss")
                startTime = AndroidUtils.getDateToString(parsed, "HH:mm").orEmpty()
            }
            if (!editing.to_ts.isNullOrEmpty()) {
                val parsed = AndroidUtils.stringToDateTimeDefault(editing.to_ts, "yyyy-MM-dd'T'HH:mm:ss")
                endTime = AndroidUtils.getDateToString(parsed, "HH:mm").orEmpty()
            }

            isAllDay = editing.all_day
            val rep = editing.repeat_interval.orEmpty().lowercase(Locale.ROOT)
            repetition = when (rep) {
                "daily" -> "Daily"
                "weekly" -> "Weekly"
                "biweekly", "bi-weekly" -> "Bi-Weekly"
                "monthly" -> "Monthly"
                "yearly" -> "Yearly"
                else -> "None"
            }

            meetingLink = editing.meeting_link.orEmpty()
            dialinNumber = editing.dialin.orEmpty()
            location = editing.location.orEmpty()
            description = editing.description.orEmpty()
            isAddTimesheet = editing.timesheet_added || editing.is_linked_with_timesheet

            // Notifications
            val notifs = editing.notifications
            if (notifs != null && notifs.length() > 0) {
                val list = mutableListOf<Pair<String, String>>()
                for (i in 0 until notifs.length()) {
                    val raw = notifs.optString(i)
                    if (raw.contains("-")) {
                        val split = raw.split("-")
                        val num = split[0]
                        val unit = split[1].replaceFirstChar { it.uppercase() }
                        list.add(Pair(unit, num))
                    }
                }
                if (list.isNotEmpty()) notificationsList = list
            }
        }
    }

    // Generated meeting room ID update
    LaunchedEffect(uiState.meetingRoomId) {
        if (!uiState.meetingRoomId.isNullOrEmpty()) {
            meetingRoomId = uiState.meetingRoomId.orEmpty()
            updateMeetingLink(uiState.meetingRoomId.orEmpty())
        }
    }

    // Generated meeting link update (fallback if full url is set)
    LaunchedEffect(uiState.generatedMeetingLink) {
        if (!uiState.generatedMeetingLink.isNullOrEmpty()) {
            meetingLink = uiState.generatedMeetingLink.orEmpty()
        }
    }

    // Timezone default selection
    LaunchedEffect(uiState.timezones) {
        if (selectedTimezone == null && uiState.timezones.isNotEmpty()) {
            val matching = uiState.timezones.firstOrNull {
                it.NAME?.contains("Kolkata", ignoreCase = true) == true || it.GMT?.contains("+05:30") == true
            }
            selectedTimezone = matching ?: uiState.timezones.first()
        }
    }

    // Matter list update when matter is selected or edited
    LaunchedEffect(uiState.matters) {
        if (isEditMode && editing?.matter_id?.isNotEmpty() == true) {
            val found = uiState.matters.firstOrNull { it.id == editing.matter_id }
            if (found != null) {
                selectedMatter = found
            }
        }
    }

    // Recalculate duration
    fun updateDuration() {
        try {
            val sdf = SimpleDateFormat("HH:mm", Locale.US)
            val start = sdf.parse(startTime)
            val end = sdf.parse(endTime)
            if (start != null && end != null) {
                val diff = end.time - start.time
                if (diff > 0) {
                    val mins = (diff / (1000 * 60)) % 60
                    val hours = (diff / (1000 * 60 * 60)) % 24
                    durationText = if (hours > 0) "Duration: $hours hr $mins min" else "Duration: $mins min"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Function to build and submit payload
    fun buildAndSubmitPayload(recurringChoice: String? = null, updateScope: String? = null) {
        val matterLegal = when (eventType) {
            "Legal Matter" -> "legal"
            "General Matter" -> "general"
            "Overhead" -> "overhead"
            "Others" -> "others"
            "Reminders" -> "reminders"
            else -> ""
        }

        if (matterLegal.isEmpty()) {
            Toast.makeText(context, "Please select an Event Type", Toast.LENGTH_SHORT).show()
            return
        }

        if ((matterLegal == "legal" || matterLegal == "general") && selectedMatter == null && editing?.matter_id.isNullOrEmpty()) {
            Toast.makeText(context, "Please select a Matter", Toast.LENGTH_SHORT).show()
            return
        }

        if (taskName.trim().isEmpty() && matterLegal != "reminders") {
            Toast.makeText(context, "Please select a Task", Toast.LENGTH_SHORT).show()
            return
        }

        if (matterLegal == "reminders" && description.trim().isEmpty()) {
            Toast.makeText(context, "Please enter a Reminder Message", Toast.LENGTH_SHORT).show()
            return
        }

        val postData = JSONObject()
        val dateObj = AndroidUtils.stringToDateTimeDefault(selectedDate, "MMM dd, yyyy")
        val eventCreationDate = AndroidUtils.getDateToString(dateObj, "yyyy-MM-dd") ?: ""

        val eventStartingDate = if (isAllDay) "${eventCreationDate}T00:00:00" else "${eventCreationDate}T$startTime:00"
        val eventEndDate = if (isAllDay) "${eventCreationDate}T23:59:59" else "${eventCreationDate}T$endTime:00"

        val selectedTmsArray = JSONArray()
        selectedTeamMembers.forEach { selectedTmsArray.put(it.id) }

        val selectedClientsArray = JSONArray()
        val selectedCorpArray = JSONArray()
        selectedIndividuals.forEach { ind ->
            if (ind.type == "corporate" || ind.type == "corporate_tm") {
                selectedCorpArray.put(ind.id)
            } else {
                selectedClientsArray.put(ind.id)
            }
        }

        val attachmentsArray = JSONArray()
        selectedDocuments.forEach { doc ->
            val dObj = JSONObject()
            dObj.put("doctype", doc.doctype)
            dObj.put("docid", doc.docid)
            attachmentsArray.put(dObj)
        }

        val notifArray = JSONArray()
        notificationsList.forEach { pair ->
            val unitStr = pair.first.lowercase(Locale.ROOT)
            val num = pair.second.ifEmpty { "10" }
            notifArray.put("$num-$unitStr")
        }

        val tzLocation = selectedTimezone?.NAME ?: "(GMT+05:30) India Standard Time - Kolkata"
        val gmtStr = selectedTimezone?.GMT ?: "330"
        val offsetMinutes = if (gmtStr.contains(":")) {
            try {
                val sign = if (gmtStr.startsWith("-")) -1 else 1
                val clean = gmtStr.replace("+", "").replace("-", "")
                val parts = clean.split(":")
                val h = parts[0].toIntOrNull() ?: 5
                val m = if (parts.size > 1) parts[1].toIntOrNull() ?: 30 else 0
                sign * (h * 60 + m)
            } catch (e: Exception) { 330 }
        } else {
            gmtStr.toIntOrNull() ?: 330
        }
        val multipliedOffset = -1 * offsetMinutes

        var repVal = repetition.lowercase(Locale.ROOT)
        if (repVal == "bi-weekly" || repVal == "biweekly") repVal = "biweekly"
        if (repVal == "none") repVal = ""

        // UTC Date string (yyyy-MM-dd'T'HH:mm:ss.SSS'Z')
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        inputFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val parsedDate = try {
            inputFormat.parse(eventCreationDate)
        } catch (e: Exception) {
            Date()
        }
        val cal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        if (parsedDate != null) cal.time = parsedDate
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val utcFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        utcFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val finalDate = utcFormat.format(cal.time)

        postData.put("date", finalDate)
        postData.put("invitees_internal", selectedTmsArray)
        postData.put("invitees_external", selectedClientsArray)
        postData.put("invitees_corporate", selectedCorpArray)
        postData.put("invitees_consumer_external", selectedClientsArray)
        postData.put("attachments", attachmentsArray)
        postData.put("notifications", notifArray)
        postData.put("timezone_location", tzLocation)
        postData.put("timezone_offset", multipliedOffset)
        postData.put("repeat_interval", repVal)
        postData.put("meeting_link", meetingLink)
        postData.put("allday", isAllDay)
        postData.put("from_ts", eventStartingDate)
        postData.put("to_ts", eventEndDate)
        postData.put("event_type", matterLegal)

        val fullTitle = if (matterLegal == "legal" || matterLegal == "general") {
            val mName = selectedMatter?.title ?: editing?.matter_name ?: ""
            if (mName.isNotEmpty()) "$mName - $taskName" else taskName
        } else if (matterLegal == "reminders") {
            "Reminders"
        } else {
            taskName
        }
        postData.put("title", fullTitle)

        // Duration calculation for timesheet formatted as "HH:mm" (e.g. "00:30")
        var durationTimesheet = "00:30"
        try {
            val sdf = SimpleDateFormat("HH:mm", Locale.US)
            val stDate = sdf.parse(startTime)
            val enDate = sdf.parse(endTime)
            if (stDate != null && enDate != null) {
                val diffMs = Math.abs(stDate.time - enDate.time)
                val diffHours = (diffMs / (60 * 60 * 1000)) % 24
                val diffMins = (diffMs / (60 * 1000)) % 60
                durationTimesheet = String.format(Locale.US, "%02d:%02d", diffHours, diffMins)
            }
        } catch (e: Exception) {
            durationTimesheet = "00:30"
        }

        if (matterLegal != "reminders") {
            postData.put("dialin", dialinNumber)
            postData.put("location", location)
            postData.put("addtimesheet", isAddTimesheet)
            postData.put("description", description)

            if (isAddTimesheet) {
                val timesheetsArray = JSONArray()
                val tsObj = JSONObject()
                tsObj.put("date", eventCreationDate)
                tsObj.put("duration", if (isAllDay) "23:59" else durationTimesheet)
                tsObj.put("eventtitle", taskName)
                tsObj.put("addedby", Constants.NAME)
                tsObj.put("user_id", Constants.USER_ID)
                if (matterLegal != "legal" && matterLegal != "general") {
                    tsObj.put("matter_id", eventType)
                    tsObj.put("matter_type", matterLegal)
                }
                timesheetsArray.put(tsObj)
                postData.put("timesheets", timesheetsArray)
            }
        } else {
            postData.put("description", description)
        }

        if (matterLegal == "legal" || matterLegal == "general") {
            val mId = selectedMatter?.id ?: editing?.matter_id ?: ""
            postData.put("matter_type", matterLegal)
            postData.put("matter_id", mId)
        }

        if (isEditMode && editing != null) {
            viewModel.onEvent(
                MeetingsUiEvent.SubmitEditEvent(
                    eventId = editing.id.orEmpty(),
                    payload = postData,
                    recurringChoice = recurringChoice,
                    updateScope = updateScope
                )
            )
        } else {
            viewModel.onEvent(MeetingsUiEvent.SubmitCreateEvent(payload = postData))
        }
    }

    // Dynamic available items based on selected matter or firm-wide lists
    val isMatterType = eventType == "Legal Matter" || eventType == "General Matter"
    val isMatterSelected = selectedMatter != null || (isEditMode && !editing?.matter_id.isNullOrEmpty())

    val availableTeamMembers = remember(selectedMatter, eventType, uiState.teamMembers) {
        if (isMatterType && selectedMatter != null) {
            val membersArray = selectedMatter?.members ?: JSONArray()
            val list = mutableListOf<TeamDo>()
            for (i in 0 until membersArray.length()) {
                val obj = membersArray.optJSONObject(i) ?: continue
                list.add(TeamDo().apply {
                    id = obj.optString("id")
                    name = obj.optString("name")
                })
            }
            list
        } else if (!isMatterType) {
            uiState.teamMembers
        } else {
            emptyList()
        }
    }

    val availableIndividuals = remember(selectedMatter, eventType, uiState.clients, uiState.corporateClients) {
        if (isMatterType && selectedMatter != null) {
            val list = mutableListOf<RelationshipsDO>()
            val clientsArray = selectedMatter?.clients ?: JSONArray()
            for (i in 0 until clientsArray.length()) {
                val obj = clientsArray.optJSONObject(i) ?: continue
                list.add(RelationshipsDO().apply {
                    id = obj.optString("id")
                    name = obj.optString("name")
                    type = obj.optString("type", "client")
                })
            }
            val corpArray = selectedMatter?.corporate ?: JSONArray()
            for (i in 0 until corpArray.length()) {
                val obj = corpArray.optJSONObject(i) ?: continue
                list.add(RelationshipsDO().apply {
                    id = obj.optString("id")
                    name = obj.optString("name")
                    type = obj.optString("type", "corporate")
                })
            }
            list
        } else if (!isMatterType) {
            uiState.clients + uiState.corporateClients
        } else {
            emptyList()
        }
    }

    val availableDocuments = remember(selectedMatter, eventType) {
        if (isMatterType && selectedMatter != null) {
            val docsArray = selectedMatter?.documents ?: JSONArray()
            val list = mutableListOf<DocumentsDo>()
            for (i in 0 until docsArray.length()) {
                val obj = docsArray.optJSONObject(i) ?: continue
                list.add(DocumentsDo().apply {
                    docid = obj.optString("docid")
                    doctype = obj.optString("doctype")
                    name = obj.optString("name")
                })
            }
            list
        } else {
            emptyList()
        }
    }

    val isEventTypeSelected = eventType.isNotEmpty()

    // Attendees card visibility matching exact Java XML parity
    val isAttendeesCardVisible = if (isMatterType) {
        isMatterSelected && (availableTeamMembers.isNotEmpty() || availableIndividuals.isNotEmpty())
    } else {
        isEventTypeSelected && (availableTeamMembers.isNotEmpty() || availableIndividuals.isNotEmpty())
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 1. Event Information Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 0.5.dp, color = Color(0xFFDDDDDE), shape = RoundedCornerShape(8.dp)),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Event Type Dropdown
                val eventTypeOptions = if (Constants.ROLE == "AAM") {
                    listOf("Overhead", "Others", "Reminders")
                } else {
                    listOf("Legal Matter", "General Matter", "Overhead", "Others", "Reminders")
                }

                FormLabel(text = "Event Type", isMandatory = true)
                Spacer(modifier = Modifier.height(4.dp))
                FormCustomDropdown(
                    selectedText = eventType,
                    placeholder = "Select Event Type",
                    searchPlaceholder = "Search Event Type",
                    options = eventTypeOptions,
                    onSelect = { selectedType ->
                        eventType = selectedType
                        selectedMatter = null
                        taskName = ""
                        selectedTeamMembers = emptyList()
                        selectedIndividuals = emptyList()
                        selectedDocuments = emptyList()
                        if (selectedType == "Legal Matter") {
                            viewModel.onEvent(MeetingsUiEvent.LoadMatters("legal"))
                        } else if (selectedType == "General Matter") {
                            viewModel.onEvent(MeetingsUiEvent.LoadMatters("general"))
                        }
                    },
                    onClear = {
                        eventType = ""
                        selectedMatter = null
                        taskName = ""
                        selectedTeamMembers = emptyList()
                        selectedIndividuals = emptyList()
                        selectedDocuments = emptyList()
                    }
                )

                // Matter Selector (if Legal or General)
                if (isMatterType) {
                    Spacer(modifier = Modifier.height(14.dp))
                    FormLabel(text = "Matter Name", isMandatory = true)
                    Spacer(modifier = Modifier.height(4.dp))
                    FormCustomDropdown(
                        selectedText = selectedMatter?.title ?: editing?.matter_name.orEmpty(),
                        placeholder = "Select Matter Name",
                        searchPlaceholder = "Search Matter",
                        options = uiState.matters.map { it.title.orEmpty() },
                        onSelect = { matterTitle ->
                            selectedMatter = uiState.matters.firstOrNull { it.title == matterTitle }
                            selectedTeamMembers = emptyList()
                            selectedIndividuals = emptyList()
                            selectedDocuments = emptyList()
                        },
                        onClear = {
                            selectedMatter = null
                            selectedTeamMembers = emptyList()
                            selectedIndividuals = emptyList()
                            selectedDocuments = emptyList()
                        }
                    )

                    // Add to Timesheet checkbox right after Matter Name
                    if (Constants.ROLE != "AAM") {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isAddTimesheet = !isAddTimesheet },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isAddTimesheet,
                                onCheckedChange = { isAddTimesheet = it },
                                colors = CheckboxDefaults.colors(checkedColor = activeBlue)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Add to Timesheet",
                                color = activeBlue,
                                fontSize = 14.sp,
                                fontFamily = GillSansFont
                            )
                        }
                    }
                }

                // Task Selector (if Legal, General, Overhead, Others)
                if (eventType.isNotEmpty() && eventType != "Reminders") {
                    val taskOptions = when (eventType) {
                        "Legal Matter" -> listOf("Case Filling", "Consultation", "Creating Legal Briefs", "Meeting with client", "Hearing")
                        "General Matter" -> listOf("Consultation", "Draft agreements", "Filling with authorities", "Meeting with client", "Prepare annual fillings")
                        "Overhead" -> listOf("Conference", "Holidays", "Research", "Training", "Vacation")
                        "Others" -> listOf("Business Development", "Personal", "Doctor Appointment", "Lunch/Dinner", "Misc")
                        else -> emptyList()
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    FormLabel(text = "Task", isMandatory = true)
                    Spacer(modifier = Modifier.height(4.dp))
                    FormCustomDropdown(
                        selectedText = taskName,
                        placeholder = "Select Task",
                        searchPlaceholder = "Search Task",
                        options = taskOptions,
                        onSelect = { taskName = it },
                        onClear = { taskName = "" }
                    )
                }

                // Message Field (if Reminders)
                if (eventType == "Reminders") {
                    Spacer(modifier = Modifier.height(14.dp))
                    FormLabel(text = "Message", isMandatory = true)
                    Spacer(modifier = Modifier.height(4.dp))
                    FormCustomTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Enter message",
                        singleLine = false,
                        height = 70.dp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Date & Time Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 0.5.dp, color = Color(0xFFDDDDDE), shape = RoundedCornerShape(8.dp)),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Date Picker Field
                FormLabel(text = "Date", isMandatory = true)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .background(Color(0xFFF9FAFB), shape = RoundedCornerShape(6.dp))
                        .border(width = 0.5.dp, color = inputBorderColor, shape = RoundedCornerShape(6.dp))
                        .clickable {
                            val cal = Calendar.getInstance()
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    cal.set(year, month, dayOfMonth)
                                    selectedDate = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(cal.time)
                                    updateMeetingLink()
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = selectedDate,
                        fontSize = 15.sp,
                        color = Color.Black,
                        fontFamily = GillSansFont
                    )
                    Image(
                        painter = painterResource(id = R.drawable.ic_time_blue),
                        contentDescription = "Select Date",
                        modifier = Modifier.size(20.dp),
                        colorFilter = ColorFilter.tint(activeBlue)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Time Pickers (Start / End)
                FormLabel(text = "Time", isMandatory = true)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Start Time
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .background(if (!isAllDay) Color(0xFFF9FAFB) else Color(0xFFEEEEEE), shape = RoundedCornerShape(6.dp))
                            .border(width = 0.5.dp, color = inputBorderColor, shape = RoundedCornerShape(6.dp))
                            .clickable(enabled = !isAllDay) {
                                val cal = Calendar.getInstance()
                                TimePickerDialog(
                                    context,
                                    { _, hour, min ->
                                        startTime = String.format(Locale.US, "%02d:%02d", hour, min)
                                        val startCal = Calendar.getInstance().apply {
                                            set(Calendar.HOUR_OF_DAY, hour)
                                            set(Calendar.MINUTE, min)
                                        }
                                        val endCal = (startCal.clone() as Calendar).apply {
                                            add(Calendar.MINUTE, 30)
                                        }
                                        endTime = String.format(Locale.US, "%02d:%02d", endCal.get(Calendar.HOUR_OF_DAY), endCal.get(Calendar.MINUTE))
                                        updateDuration()
                                        updateMeetingLink()
                                    },
                                    cal.get(Calendar.HOUR_OF_DAY),
                                    cal.get(Calendar.MINUTE),
                                    true
                                ).show()
                            }
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = startTime,
                            fontSize = 15.sp,
                            color = if (isAllDay) Color.Gray else Color.Black,
                            fontFamily = GillSansFont
                        )
                        Image(
                            painter = painterResource(id = R.drawable.ic_time_blue),
                            contentDescription = "Start Time",
                            modifier = Modifier.size(20.dp),
                            colorFilter = ColorFilter.tint(activeBlue)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // End Time
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .background(if (!isAllDay) Color(0xFFF9FAFB) else Color(0xFFEEEEEE), shape = RoundedCornerShape(6.dp))
                            .border(width = 0.5.dp, color = inputBorderColor, shape = RoundedCornerShape(6.dp))
                            .clickable(enabled = !isAllDay) {
                                val cal = Calendar.getInstance()
                                TimePickerDialog(
                                    context,
                                    { _, hour, min ->
                                        endTime = String.format(Locale.US, "%02d:%02d", hour, min)
                                        updateDuration()
                                        updateMeetingLink()
                                    },
                                    cal.get(Calendar.HOUR_OF_DAY),
                                    cal.get(Calendar.MINUTE),
                                    true
                                ).show()
                            }
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = endTime,
                            fontSize = 15.sp,
                            color = if (isAllDay) Color.Gray else Color.Black,
                            fontFamily = GillSansFont
                        )
                        Image(
                            painter = painterResource(id = R.drawable.ic_time_blue),
                            contentDescription = "End Time",
                            modifier = Modifier.size(20.dp),
                            colorFilter = ColorFilter.tint(activeBlue)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Duration & All Day toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isAllDay) "Duration: 24 hours" else durationText,
                        color = activeBlue,
                        fontSize = 13.sp,
                        fontFamily = GillSansFont
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "All Day",
                            color = activeBlue,
                            fontSize = 13.sp,
                            fontFamily = GillSansFont,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Checkbox(
                            checked = isAllDay,
                            onCheckedChange = { isAllDay = it },
                            colors = CheckboxDefaults.colors(checkedColor = activeBlue)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Timezone Dropdown
                val timezoneFormattedOptions = uiState.timezones.map { tz ->
                    "(${tz.GMT}) ${tz.NAME}"
                }
                val currentTzFormatted = selectedTimezone?.let { tz ->
                    "(${tz.GMT}) ${tz.NAME}"
                } ?: "(GMT+05:30) India Standard Time - Kolkata"

                FormLabel(text = "Time Zone", isMandatory = true)
                Spacer(modifier = Modifier.height(4.dp))
                FormCustomDropdown(
                    selectedText = currentTzFormatted,
                    placeholder = "Select Time Zone",
                    searchPlaceholder = "Search Time Zone",
                    options = if (timezoneFormattedOptions.isNotEmpty()) timezoneFormattedOptions else listOf(currentTzFormatted),
                    onSelect = { selectedFormatted ->
                        selectedTimezone = uiState.timezones.firstOrNull { tz ->
                            "(${tz.GMT}) ${tz.NAME}" == selectedFormatted
                        }
                    },
                    onClear = {
                        selectedTimezone = null
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Repetition Dropdown
                FormLabel(text = "Repetition", isMandatory = false)
                Spacer(modifier = Modifier.height(4.dp))
                FormCustomDropdown(
                    selectedText = repetition,
                    placeholder = "Select Repetition",
                    searchPlaceholder = "Search Repetition",
                    options = listOf("None", "Daily", "Weekly", "Bi-Weekly", "Monthly", "Yearly"),
                    onSelect = { repetition = it },
                    onClear = { repetition = "None" }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Meeting Details Card (Hidden for Reminders)
        if (eventType != "Reminders") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 0.5.dp, color = Color(0xFFDDDDDE), shape = RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Title with Meeting Icon
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.videocall_icon),
                            contentDescription = "Meeting details",
                            modifier = Modifier.size(20.dp),
                            colorFilter = ColorFilter.tint(activeBlue)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Meeting Details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = activeBlue,
                            fontFamily = GillSansFont
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Meeting Link + Generate Link Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FormLabel(text = "Meeting Link", isMandatory = false)
                        Text(
                            text = "+ Generate Link",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = activeBlue,
                            fontFamily = GillSansFont,
                            modifier = Modifier
                                .clickable { viewModel.onEvent(MeetingsUiEvent.GenerateMeetingLink) }
                                .padding(4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    FormCustomTextField(
                        value = meetingLink,
                        onValueChange = { meetingLink = it },
                        placeholder = "Meeting Link",
                        height = 44.dp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dial-in Number
                    FormLabel(text = "Dial-in Number", isMandatory = false)
                    Spacer(modifier = Modifier.height(4.dp))
                    FormCustomTextField(
                        value = dialinNumber,
                        onValueChange = { dialinNumber = it },
                        placeholder = "Dial-in Number",
                        height = 44.dp,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Location
                    FormLabel(text = "Location", isMandatory = false)
                    Spacer(modifier = Modifier.height(4.dp))
                    FormCustomTextField(
                        value = location,
                        onValueChange = { location = it },
                        placeholder = "Location",
                        height = 44.dp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Description / Agenda
                    FormLabel(text = "Meeting Agenda / Description", isMandatory = false)
                    Spacer(modifier = Modifier.height(4.dp))
                    FormCustomTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Enter description or agenda",
                        height = 100.dp,
                        singleLine = false,
                        maxLines = 4
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 4. Attendees Card (Exact Java XML Parity: Unified Card with Team Members, Individuals & Documents)
        if (isAttendeesCardVisible) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 0.5.dp, color = Color(0xFFDDDDDE), shape = RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Header: Icon + Title
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.three_grp_icon),
                            contentDescription = "Attendees",
                            modifier = Modifier.size(20.dp),
                            colorFilter = ColorFilter.tint(activeBlue)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Attendees",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = activeBlue,
                            fontFamily = GillSansFont
                        )
                    }

                    // 1. Add Team Member(s)
                    if (Constants.CATEGORY != "solo" && availableTeamMembers.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        AttendeesMultiSelectDropdown(
                            label = "Add Team Member(s)",
                            placeholder = "Select Team Member(s)",
                            searchPlaceholder = "Search Team Members",
                            items = availableTeamMembers,
                            selectedItems = selectedTeamMembers,
                            itemLabel = { it.name.orEmpty() },
                            itemId = { it.id.orEmpty() },
                            onSelectionChanged = { selectedTeamMembers = it }
                        )
                    }

                    // 2. Add Individuals
                    if (Constants.ROLE != "AAM" && availableIndividuals.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        AttendeesMultiSelectDropdown(
                            label = "Add Individuals",
                            placeholder = "Select Individuals",
                            searchPlaceholder = "Search Individuals",
                            items = availableIndividuals,
                            selectedItems = selectedIndividuals,
                            itemLabel = { it.name.orEmpty() },
                            itemId = { it.id.orEmpty() },
                            onSelectionChanged = { selectedIndividuals = it }
                        )
                    }

                    // 3. Add Document(s) (Visible if Matter type and documents are available)
                    if (isMatterType && availableDocuments.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        AttendeesMultiSelectDropdown(
                            label = "Add Document(s)",
                            placeholder = "Select Document(s)",
                            searchPlaceholder = "Search Documents",
                            items = availableDocuments,
                            selectedItems = selectedDocuments,
                            itemLabel = { it.name.orEmpty() },
                            itemId = { it.docid.orEmpty() },
                            onSelectionChanged = { selectedDocuments = it }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // 5. Notify Me Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 0.5.dp, color = Color(0xFFDDDDDE), shape = RoundedCornerShape(8.dp)),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.notifications),
                            contentDescription = "Notify icon",
                            modifier = Modifier.size(20.dp),
                            colorFilter = ColorFilter.tint(activeBlue)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Notify Me",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = activeBlue,
                            fontFamily = GillSansFont
                        )
                    }

                    Button(
                        onClick = {
                            notificationsList = notificationsList + Pair("Minutes", "10")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = activeBlue),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            "+ Add Notification",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontFamily = GillSansFont
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                notificationsList.forEachIndexed { index, notifyRow ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Unit dropdown box
                        Box(modifier = Modifier.weight(1.2f)) {
                            FormCustomDropdown(
                                selectedText = notifyRow.first,
                                placeholder = "Select Unit",
                                options = listOf("Minutes", "Hours", "Days", "Weeks"),
                                onSelect = { selectedUnit ->
                                    notificationsList = notificationsList.mapIndexed { idx, pair ->
                                        if (idx == index) Pair(selectedUnit, pair.second) else pair
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Number input
                        FormCustomTextField(
                            value = notifyRow.second,
                            onValueChange = { newVal ->
                                val filtered = newVal.filter { it.isDigit() }
                                notificationsList = notificationsList.mapIndexed { idx, pair ->
                                    if (idx == index) Pair(pair.first, filtered) else pair
                                }
                            },
                            placeholder = "10",
                            height = 44.dp,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(0.8f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                notificationsList = notificationsList.filterIndexed { idx, _ -> idx != index }
                            }
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.delete_de),
                                contentDescription = "Delete",
                                modifier = Modifier.size(20.dp),
                                colorFilter = ColorFilter.tint(Color.Red)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 6. Save & Cancel Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(
                onClick = onCancelClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEEEEE)),
                border = BorderStroke(1.dp, Color(0xFFDDDDDE)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .width(120.dp)
                    .height(40.dp)
            ) {
                Text(
                    text = "Cancel",
                    color = Color.Black,
                    fontFamily = GillSansFont,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            Button(
                onClick = {
                    if (isEditMode && editing?.isRecurring == true) {
                        showEditRecurrenceDialog = true
                    } else {
                        buildAndSubmitPayload()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = activeBlue),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .width(120.dp)
                    .height(40.dp)
            ) {
                Text(
                    text = if (isEditMode) "Update" else "Save",
                    color = Color.White,
                    fontFamily = GillSansFont,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Recurrence Edit Choice Dialog
    if (showEditRecurrenceDialog) {
        RecurrenceChoiceDialog(
            onDismiss = { showEditRecurrenceDialog = false },
            onConfirm = { choice ->
                showEditRecurrenceDialog = false
                val mappedChoice = when (choice) {
                    "Only this event" -> "this"
                    "This and following events" -> "following"
                    else -> "all"
                }
                buildAndSubmitPayload(recurringChoice = mappedChoice)
            }
        )
    }
}
