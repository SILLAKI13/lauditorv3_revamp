package com.digicoffer.lauditor.feature.timesheets.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.stringResource
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.TimeSheets.Models.Month_Model
import com.digicoffer.lauditor.TimeSheets.Models.TMModel
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme
import com.digicoffer.lauditor.core.designsystem.typography.FontTokens
import com.digicoffer.lauditor.core.ui.common.cards.AppCard
import com.digicoffer.lauditor.core.ui.common.feedback.AppEmptyState
import com.digicoffer.lauditor.core.ui.common.foundation.AppDivider
import com.digicoffer.lauditor.core.ui.common.foundation.AppSpacer
import com.digicoffer.lauditor.core.ui.common.foundation.AppText
import com.digicoffer.lauditor.feature.timesheets.presentation.state.TimesheetsUiState

@Composable
fun TeamMembersScreen(
    uiState: TimesheetsUiState,
    modifier: Modifier = Modifier
) {
    val isMonth = uiState.isWeek == "month"
    val query = uiState.searchQuery.trim().lowercase()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE4F2FF))
            .padding(horizontal = 15.dp, vertical = 8.dp)
    ) {
        if (isMonth) {
            val filteredMonthList = uiState.monthlyTeamMembersList.filter {
                it.name?.lowercase()?.contains(query) == true
            }

            if (filteredMonthList.isEmpty()) {
                AppEmptyState(title = "No monthly entries found")
            } else {
                LazyColumn {
                    items(filteredMonthList) { member ->
                        MonthlyTeamMemberCard(member = member, uiState = uiState)
                        AppSpacer(height = 8.dp)
                    }
                }
            }
        } else {
            val filteredWeekList = uiState.teamMembersList.filter {
                it.name?.lowercase()?.contains(query) == true
            }

            if (filteredWeekList.isEmpty()) {
                AppEmptyState(title = "No team members found")
            } else {
                LazyColumn {
                    items(filteredWeekList) { member ->
                        WeeklyTeamMemberCard(member = member)
                        AppSpacer(height = 8.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyTeamMemberCard(
    member: TMModel,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = Color.White,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Team Member Name (R.layout.title_name_layout style: blue, 18sp, regular)
            AppText(
                text = member.name ?: "",
                color = Color(0xFF004D87),
                style = TextStyle(fontSize = 18.sp, fontFamily = FontTokens.DefaultFontFamily)
            )
            AppSpacer(height = 6.dp)

            // Values Column Grid (matching WeeklyTimeSheetItem style)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Column: Billable Hours & Non-Billable Hours
                Column(
                    modifier = Modifier.wrapContentWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    AppText(
                        text = "${member.tb ?: "0"} Hour",
                        color = Color.Black,
                        style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                    )
                    AppSpacer(height = 2.dp)
                    AppText(
                        text = "${member.tnb ?: "0"} Hour",
                        color = Color.Black,
                        style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                    )
                }

                // Dotted vertical line separator
                Canvas(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .width(2.dp)
                        .height(36.dp)
                ) {
                    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    drawLine(
                        color = Color(0xFFCCCCCC),
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height),
                        strokeWidth = 2f,
                        pathEffect = pathEffect
                    )
                }

                // Right Column: Billable & Non Billable labels
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                ) {
                    AppText(
                        text = stringResource(id = R.string.billable),
                        color = Color(0xFF004D87),
                        style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                    )
                    AppSpacer(height = 2.dp)
                    AppText(
                        text = stringResource(id = R.string.non_billable),
                        color = Color(0xFF004D87),
                        style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                    )
                }
            }

            AppSpacer(height = 8.dp)

            // Total Hours Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppText(
                    text = stringResource(id = R.string.total_hours),
                    color = Color(0xFF004D87),
                    style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                )
                AppSpacer(width = 10.dp)
                AppText(
                    text = "${member.total ?: "0"} Hours",
                    color = Color.Black,
                    style = TextStyle(fontSize = 18.sp, fontFamily = FontTokens.DefaultFontFamily)
                )
            }
        }
    }
}

@Composable
fun MonthlyTeamMemberCard(
    member: Month_Model,
    uiState: TimesheetsUiState,
    modifier: Modifier = Modifier
) {
    val monthName = try {
        val parser = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.US)
        val date = parser.parse(uiState.fromDateString)
        val formatter = java.text.SimpleDateFormat("MMMM", java.util.Locale.US)
        date?.let { formatter.format(it) } ?: ""
    } catch (e: Exception) {
        ""
    }
    val fullHeaderLabel = "Month of $monthName"

    AppCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = Color.White,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Team Member Name
            AppText(
                text = member.name ?: "",
                color = Color(0xFF004D87),
                style = TextStyle(fontSize = 18.sp, fontFamily = FontTokens.DefaultFontFamily)
            )
            AppSpacer(height = 8.dp)

            // Monthly breakdown table header row (All blue, regular weight, center aligned value columns)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 2.dp)
            ) {
                AppText(
                    text = fullHeaderLabel,
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF004D87),
                    style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                )
                AppText(
                    text = stringResource(id = R.string.billable),
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF004D87),
                    textAlign = TextAlign.Center,
                    style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                )
                AppText(
                    text = "Non Billable", // no dash, matching legacy
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF004D87),
                    textAlign = TextAlign.Center,
                    style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                )
            }

            // Week rows 1 to 5 (All blue, regular, center-aligned values)
            val weeks = listOf(
                Triple("Week 1", member.billable_week_1, member.non_billable_week_1),
                Triple("Week 2", member.billable_week_2, member.non_billable_week_2),
                Triple("Week 3", member.billable_week_3, member.non_billable_week_3),
                Triple("Week 4", member.billable_week_4, member.non_billable_week_4),
                Triple("Week 5", member.billable_week_5, member.non_billable_week_5)
            )

            weeks.forEach { (wk, billable, nonbillable) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 2.dp)
                ) {
                    AppText(
                        text = wk,
                        modifier = Modifier.weight(1f),
                        color = Color(0xFF004D87),
                        style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                    )
                    AppText(
                        text = billable ?: "0:0",
                        modifier = Modifier.weight(1f),
                        color = Color(0xFF004D87),
                        textAlign = TextAlign.Center,
                        style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                    )
                    AppText(
                        text = nonbillable ?: "0:0",
                        modifier = Modifier.weight(1f),
                        color = Color(0xFF004D87),
                        textAlign = TextAlign.Center,
                        style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                    )
                }
            }

            // Totals Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 2.dp)
            ) {
                AppText(
                    text = "Total",
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF004D87),
                    style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                )
                AppText(
                    text = member.billable_week_tot ?: "0:0",
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF004D87),
                    textAlign = TextAlign.Center,
                    style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                )
                AppText(
                    text = member.non_billable_week_tot ?: "0:0",
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF004D87),
                    textAlign = TextAlign.Center,
                    style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                )
            }

            // Nested white Card container for weekly log summaries matching legacy nested CardView weekly_timesheets
            AppCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                backgroundColor = Color.White,
                shape = RoundedCornerShape(8.dp),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Column: Billable Hours & Non-Billable Hours
                        Column(
                            modifier = Modifier.wrapContentWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            AppText(
                                text = "${member.billable_week_tot ?: "0:0"} Hour",
                                color = Color.Black,
                                style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                            )
                            AppSpacer(height = 2.dp)
                            AppText(
                                text = "${member.non_billable_week_tot ?: "0:0"} Hour",
                                color = Color.Black,
                                style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                            )
                        }

                        // Dotted vertical line separator
                        Canvas(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .width(2.dp)
                                .height(36.dp)
                        ) {
                            val pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                            drawLine(
                                color = Color(0xFFCCCCCC),
                                start = Offset(0f, 0f),
                                end = Offset(0f, size.height),
                                strokeWidth = 2f,
                                pathEffect = pathEffect
                            )
                        }

                        // Right Column: Billable & Non Billable labels
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.Start
                        ) {
                            AppText(
                                text = stringResource(id = R.string.billable),
                                color = Color(0xFF004D87),
                                style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                            )
                            AppSpacer(height = 2.dp)
                            AppText(
                                text = stringResource(id = R.string.non_billable),
                                color = Color(0xFF004D87),
                                style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                            )
                        }
                    }

                    AppSpacer(height = 8.dp)

                    // Total Hours Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppText(
                            text = stringResource(id = R.string.total_hours),
                            color = Color(0xFF004D87),
                            style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                        )
                        AppSpacer(width = 10.dp)
                        AppText(
                            text = "${member.Total ?: "0:0"} Hours",
                            color = Color.Black,
                            style = TextStyle(fontSize = 18.sp, fontFamily = FontTokens.DefaultFontFamily)
                        )
                    }
                }
            }
        }
    }
}
