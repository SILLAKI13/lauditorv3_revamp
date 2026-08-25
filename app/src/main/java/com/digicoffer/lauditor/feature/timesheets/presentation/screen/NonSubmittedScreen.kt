package com.digicoffer.lauditor.feature.timesheets.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme
import com.digicoffer.lauditor.core.designsystem.typography.FontTokens
import com.digicoffer.lauditor.core.ui.common.buttons.AppButton
import com.digicoffer.lauditor.core.ui.common.cards.AppCard
import com.digicoffer.lauditor.core.ui.common.dropdowns.DropdownSelectorField
import com.digicoffer.lauditor.core.ui.common.feedback.AppEmptyState
import com.digicoffer.lauditor.core.ui.common.foundation.AppSpacer
import com.digicoffer.lauditor.core.ui.common.foundation.AppText
import com.digicoffer.lauditor.feature.timesheets.presentation.components.DailyTimeSheetBlock
import com.digicoffer.lauditor.feature.timesheets.presentation.components.TimeSheetEntryUiModel
import com.digicoffer.lauditor.feature.timesheets.presentation.state.TimesheetsUiEvent
import com.digicoffer.lauditor.feature.timesheets.presentation.state.TimesheetsUiState
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun NonSubmittedScreen(
    uiState: TimesheetsUiState,
    onEvent: (TimesheetsUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val datesList = uiState.weekDateInfo?.weekDates ?: emptyList()
    val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    val dailyGroups = daysOfWeek.mapIndexed { idx, day ->
        val dateVal = if (idx < datesList.size) datesList[idx] else ""
        val formattedDateHeader = try {
            val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
            val outputFormat = SimpleDateFormat("EEE MMM d, yyyy", Locale.US)
            val parsed = inputFormat.parse(dateVal)
            if (parsed != null) outputFormat.format(parsed) else "$day $dateVal"
        } catch (e: Exception) {
            "$day $dateVal"
        }
        
        val dayLogs = uiState.timesheetsList.filter { log ->
            log.date?.contains(dateVal) == true || log.date?.startsWith(day) == true
        }

        val totalMins = dayLogs.sumOf { (it.hours?.toIntOrNull() ?: 0) * 60 + (it.minutes?.toIntOrNull() ?: 0) }
        val dailyTotalFormatted = "${totalMins / 60}:${String.format("%02d", totalMins % 60)} Hours"

        val uiEntries = dayLogs.map { log ->
            TimeSheetEntryUiModel(
                id = log.taskid ?: "",
                matterName = log.Task_matter_name ?: "",
                hours = "${log.hours ?: "0"} Hour",
                minutes = "${log.minutes ?: "0"} Min",
                billableStatus = AndroidUtils.CapitalizeFirstLetter(log.Task_billing ?: "") ?: "",
                taskName = log.Task_name ?: "",
                isEditable = log.is_editable == true,
                isSubmitted = uiState.isFrozen
            )
        }
        Triple(formattedDateHeader, dailyTotalFormatted, uiEntries)
    }.filter { it.third.isNotEmpty() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE4F2FF))
            .padding(15.dp) // 15dp margins on all sides matching legacy layout
    ) {
        item {
            // Level 1: Outer Card Container (Matches cv_details_activity_log)
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.White,
                shape = RoundedCornerShape(8.dp),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    if (!uiState.isFrozen) {
                        // Project Dropdown Spinner (Using standard reusable DropdownSelectorField)
                        LabelWithAsterisk(text = "Project")
                        AppSpacer(height = 4.dp)
                        DropdownSelectorField(
                            items = uiState.activeProjectsList,
                            selectedItem = uiState.selectedMatter,
                            onItemSelected = { onEvent(TimesheetsUiEvent.MatterSelected(it)) },
                            itemToLabel = { it.mattername ?: "" },
                            placeholder = "Search Project",
                            textStyle = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily),
                            enabled = !uiState.isEditMode,
                            modifier = Modifier.fillMaxWidth()
                        )
                        AppSpacer(height = 12.dp)

                        // Task Dropdown Spinner (Only visible once a Project is selected)
                        if (uiState.selectedMatter != null) {
                            LabelWithAsterisk(text = "Task")
                            AppSpacer(height = 4.dp)
                            DropdownSelectorField(
                                items = uiState.taskList,
                                selectedItem = uiState.selectedTask,
                                onItemSelected = { onEvent(TimesheetsUiEvent.TaskSelected(it)) },
                                itemToLabel = { it.displayValue ?: "" },
                                placeholder = "Search Task",
                                textStyle = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily),
                                modifier = Modifier.fillMaxWidth()
                            )
                            AppSpacer(height = 12.dp)
                        }

                        // Status Dropdown Spinner
                        LabelWithAsterisk(text = "Status")
                        AppSpacer(height = 4.dp)
                        DropdownSelectorField(
                            items = listOf("Billable", "Non-Billable"),
                            selectedItem = uiState.selectedStatus,
                            onItemSelected = { onEvent(TimesheetsUiEvent.StatusSelected(it)) },
                            itemToLabel = { it },
                            placeholder = "Search Status",
                            textStyle = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily),
                            modifier = Modifier.fillMaxWidth()
                        )
                        AppSpacer(height = 12.dp)

                        // Date Dropdown Spinner
                        LabelWithAsterisk(text = "Date")
                        AppSpacer(height = 4.dp)
                        val formattedDates = datesList.map { dateVal ->
                            val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                            val idx = datesList.indexOf(dateVal)
                            if (idx in 0..6) "${days[idx]} $dateVal" else dateVal
                        }
                        DropdownSelectorField(
                            items = formattedDates,
                            selectedItem = uiState.selectedDate,
                            onItemSelected = { onEvent(TimesheetsUiEvent.LogDateSelected(it)) },
                            itemToLabel = { it },
                            placeholder = "Search Date",
                            textStyle = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily),
                            enabled = !uiState.isEditMode,
                            modifier = Modifier.fillMaxWidth()
                        )
                        AppSpacer(height = 14.dp)

                        // Duration Section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Left Col: Hours input field
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 4.dp)
                            ) {
                                BlackLabelWithAsterisk(text = "Hours")
                                AppSpacer(height = 4.dp)
                                OutlinedTextField(
                                    value = uiState.hours,
                                    onValueChange = { onEvent(TimesheetsUiEvent.HoursChanged(it)) },
                                    placeholder = { Text("Hours", style = TextStyle(fontSize = 15.sp, color = Color.Gray)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(6.dp),
                                    singleLine = true,
                                    textStyle = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFCCCCCC),
                                        unfocusedBorderColor = Color(0xFFCCCCCC)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(32.dp) // Set to 32dp to visually match legacy Dropdown selector fields
                                )
                            }

                            // Center Col: Minutes Selector Buttons (Separate rounded-corner button cards with spacing gap)
                            Column(
                                modifier = Modifier
                                    .weight(1.3f)
                                    .padding(horizontal = 4.dp)
                            ) {
                                BlackLabelWithAsterisk(text = "Minutes")
                                AppSpacer(height = 4.dp)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(32.dp), // Set height to 32dp to match Hours field exactly
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("15", "30", "45").forEach { min ->
                                        val isSelected = uiState.minutes == min
                                        val bg = if (isSelected) Color(0xFF004D87) else Color(0xFFEFEFEF)
                                        val txt = if (isSelected) Color.White else Color.Black
                                        val borderCol = if (isSelected) Color(0xFF004D87) else Color(0xFFCCCCCC)

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                                .background(bg, RoundedCornerShape(6.dp))
                                                .border(1.dp, borderCol, RoundedCornerShape(6.dp))
                                                .clickable {
                                                    val nextMin = if (isSelected) "" else min
                                                    onEvent(TimesheetsUiEvent.MinutesChanged(nextMin))
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = min, color = txt, fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                                        }
                                    }
                                }
                            }

                            // Right Col: Total Hours (Read-Only)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 4.dp)
                            ) {
                                BlackLabelWithAsterisk(text = "Total Hours")
                                AppSpacer(height = 4.dp)
                                val hrsVal = uiState.hours.trim().toIntOrNull() ?: 0
                                val minsVal = uiState.minutes.toIntOrNull() ?: 0
                                val totalHrsText = if (uiState.hours.isEmpty() && uiState.minutes.isEmpty()) {
                                    ""
                                } else {
                                    val formattedMins = String.format("%02d", minsVal)
                                    "$hrsVal:$formattedMins"
                                }

                                OutlinedTextField(
                                    value = totalHrsText,
                                    onValueChange = {},
                                    placeholder = { Text("Total..", style = TextStyle(fontSize = 15.sp, color = Color.Gray)) },
                                    readOnly = true,
                                    enabled = false,
                                    shape = RoundedCornerShape(6.dp),
                                    singleLine = true,
                                    textStyle = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledBorderColor = Color(0xFFCCCCCC),
                                        disabledContainerColor = Color(0xFFFAFAFA),
                                        disabledTextColor = Color.Black
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(32.dp) // Set height to 32dp to match selectors exactly
                                )
                            }
                        }
                        AppSpacer(height = 18.dp)

                        // Form Action Buttons (Equal width weight(1f), 40dp height, matching layout)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            // Cancel Button
                            AppButton(
                                text = "Cancel",
                                onClick = { onEvent(TimesheetsUiEvent.ClearForm) },
                                containerColor = Color(0xFFE5E5E5),
                                contentColor = Color.Black,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(40.dp)
                            )
                            AppSpacer(width = 16.dp)
                            // Save Button
                            AppButton(
                                text = "Save",
                                onClick = { onEvent(TimesheetsUiEvent.SaveTimesheet) },
                                containerColor = Color(0xFF004D87),
                                contentColor = Color.White,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(40.dp)
                            )
                        }
                    }

                    // Separation gap before logs list
                    AppSpacer(height = 16.dp)

                    if (dailyGroups.isEmpty()) {
                        AppSpacer(height = 20.dp)
                        AppEmptyState(
                            title = "No Timesheet entries for this week",
                            description = "Add tasks or track time to begin.",
                            imageRes = R.drawable.empty_appointments,
                            imageSize = 130.dp,
                            titleStyle = TextStyle(fontSize = 18.sp, fontFamily = FontTokens.DefaultFontFamily),
                            descriptionStyle = TextStyle(fontSize = 14.sp, fontFamily = FontTokens.DefaultFontFamily),
                            titleColor = Color.Black,
                            descriptionColor = Color(0xFF666666)
                        )
                    } else {
                        dailyGroups.forEach { (header, totalHrs, entries) ->
                            DailyTimeSheetBlock(
                                dateText = header,
                                totalHoursText = totalHrs,
                                entriesList = entries,
                                onEditClick = { entry ->
                                    val originalLog = uiState.timesheetsList.find { it.taskid == entry.id }
                                    originalLog?.let { onEvent(TimesheetsUiEvent.EditTimesheet(it)) }
                                },
                                onDeleteClick = { entry ->
                                    val originalLog = uiState.timesheetsList.find { it.taskid == entry.id }
                                    originalLog?.let { onEvent(TimesheetsUiEvent.DeleteTimesheet(it)) }
                                }
                            )
                            AppSpacer(height = 10.dp)
                        }

                        // 3. Submit Weekly Logs Button (Positioned at the very bottom, rectangular with 8.dp corner shape, width 110dp, blue, matching layout)
                        if (!uiState.isFrozen && uiState.timesheetsList.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                AppButton(
                                    text = stringResource(id = R.string.submit),
                                    onClick = { onEvent(TimesheetsUiEvent.SubmitTimesheets) },
                                    containerColor = Color(0xFF004D87),
                                    contentColor = Color.White,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.width(110.dp).height(40.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LabelWithAsterisk(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AppText(
            text = text,
            color = Color(0xFF004D87),
            style = LauditorTheme.typography.bodyLarge.copy(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
        )
        AppText(
            text = " *",
            color = Color.Red,
            style = LauditorTheme.typography.bodyLarge.copy(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
        )
    }
}

@Composable
fun BlackLabelWithAsterisk(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AppText(
            text = text,
            color = Color.Black,
            style = LauditorTheme.typography.bodySmall.copy(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
        )
        AppText(
            text = " *",
            color = Color.Red,
            style = LauditorTheme.typography.bodySmall.copy(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
        )
    }
}
