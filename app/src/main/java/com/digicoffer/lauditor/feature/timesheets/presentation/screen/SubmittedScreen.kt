package com.digicoffer.lauditor.feature.timesheets.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils
import com.digicoffer.lauditor.core.designsystem.typography.FontTokens
import com.digicoffer.lauditor.core.ui.common.feedback.AppEmptyState
import com.digicoffer.lauditor.core.ui.common.foundation.AppSpacer
import com.digicoffer.lauditor.feature.timesheets.presentation.components.DailyTimeSheetBlock
import com.digicoffer.lauditor.feature.timesheets.presentation.components.TimeSheetEntryUiModel
import com.digicoffer.lauditor.feature.timesheets.presentation.state.TimesheetsUiState
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun SubmittedScreen(
    uiState: TimesheetsUiState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE4F2FF))
            .padding(horizontal = 15.dp, vertical = 8.dp)
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
                val lDate = log.date ?: ""
                lDate.startsWith(day) || (dateVal.isNotEmpty() && lDate.contains(dateVal))
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
                    isEditable = false,
                    isSubmitted = true
                )
            }
            Triple(formattedDateHeader, dailyTotalFormatted, uiEntries)
        }.filter { it.third.isNotEmpty() }

        if (dailyGroups.isEmpty()) {
            item {
                AppSpacer(height = 20.dp)
                AppEmptyState(
                    title = if (!uiState.isFrozen) "Timesheet Not Submitted Please Select Other Week" else "No Timesheet entries for this week",
                    description = null,
                    imageRes = R.drawable.empty_appointments,
                    imageSize = 130.dp,
                    titleStyle = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontTokens.DefaultFontFamily),
                    titleColor = Color.Black
                )
            }
        } else {
            dailyGroups.forEach { (header, totalHrs, entries) ->
                item {
                    DailyTimeSheetBlock(
                        dateText = header,
                        totalHoursText = totalHrs,
                        entriesList = entries,
                        onEditClick = {},
                        onDeleteClick = {}
                    )
                    AppSpacer(height = 6.dp)
                }
            }
        }
    }
}
