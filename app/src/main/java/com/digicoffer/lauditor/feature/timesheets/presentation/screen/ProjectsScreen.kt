package com.digicoffer.lauditor.feature.timesheets.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.TimeSheets.Models.ProjectTMModel
import com.digicoffer.lauditor.TimeSheets.Models.ProjectsModel
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme
import com.digicoffer.lauditor.core.designsystem.typography.FontTokens
import com.digicoffer.lauditor.core.ui.common.cards.AppCard
import com.digicoffer.lauditor.core.ui.common.dropdowns.DropdownSelectorField
import com.digicoffer.lauditor.core.ui.common.feedback.AppEmptyState
import com.digicoffer.lauditor.core.ui.common.foundation.AppDivider
import com.digicoffer.lauditor.core.ui.common.foundation.AppSpacer
import com.digicoffer.lauditor.core.ui.common.foundation.AppText
import com.digicoffer.lauditor.core.ui.common.search.AppSearchField
import com.digicoffer.lauditor.feature.timesheets.presentation.state.TimesheetsUiEvent
import com.digicoffer.lauditor.feature.timesheets.presentation.state.TimesheetsUiState

@Composable
fun ProjectsScreen(
    uiState: TimesheetsUiState,
    onEvent: (TimesheetsUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE4F2FF))
            .padding(horizontal = 15.dp, vertical = 8.dp)
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // 1. Selector dropdowns
            item {
                AppCard(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    backgroundColor = Color.White,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        // Project Dropdown Label
                        AppText(
                            text = stringResource(id = R.string.project),
                            color = Color(0xFF004D87),
                            style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                        )
                        AppSpacer(height = 6.dp)
                        DropdownSelectorField(
                            items = uiState.aggregatedProjectsList,
                            selectedItem = uiState.selectedProject,
                            onItemSelected = { onEvent(TimesheetsUiEvent.AggregatedProjectSelected(it)) },
                            itemToLabel = { it.projectName ?: "" },
                            placeholder = "Search Project",
                            textStyle = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily),
                            modifier = Modifier.fillMaxWidth()
                        )

                        val teamMembers = uiState.selectedProject?.teamMembers
                        val tmsList = mutableListOf<ProjectTMModel>()
                        if (teamMembers != null) {
                            for (i in 0 until teamMembers.length()) {
                                val tObj = teamMembers.optJSONObject(i)
                                if (tObj != null) {
                                    tmsList.add(ProjectTMModel().apply {
                                        name = tObj.optString("name", "")
                                        billableHours = tObj.optString("billableHours", "0")
                                        nonBillablehours = tObj.optString("nonBillablehours", "0")
                                        total = tObj.optString("total", "0")
                                    })
                                }
                            }
                        }

                        if (uiState.selectedProject != null && tmsList.isNotEmpty()) {
                            AppSpacer(height = 10.dp)
                            // Team Members Dropdown Label
                            AppText(
                                text = stringResource(id = R.string.team_members),
                                color = Color(0xFF004D87),
                                style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                            )
                            AppSpacer(height = 6.dp)
                            DropdownSelectorField(
                                items = tmsList,
                                selectedItem = uiState.selectedProjectTeamMember,
                                onItemSelected = { onEvent(TimesheetsUiEvent.AggregatedProjectTMSelected(it)) },
                                itemToLabel = { it.name ?: "" },
                                placeholder = "Search Team Member",
                                textStyle = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        AppSpacer(height = 12.dp)

                        // Search field (magnifying glass) inside the Card
                        AppSearchField(
                            value = uiState.searchQuery,
                            onValueChange = { onEvent(TimesheetsUiEvent.SearchQueryChanged(it)) },
                            placeholder = "Search",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                AppSpacer(height = 10.dp)
            }

            // 2. Summary Card (linear_down card in legacy XML)
            item {
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = Color.White,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                AppText(
                                    text = "Non Billable",
                                    color = Color(0xFF004D87),
                                    style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                                )
                                AppSpacer(height = 2.dp)
                                AppText(
                                    text = uiState.aggregatedGrandTotalNonBillable,
                                    color = Color.Black,
                                    style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                                )
                                AppSpacer(height = 2.dp)
                                AppText(
                                    text = "Hours",
                                    color = Color(0xFF004D87),
                                    style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                AppText(
                                    text = "Billable",
                                    color = Color(0xFF004D87),
                                    style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                                )
                                AppSpacer(height = 2.dp)
                                AppText(
                                    text = uiState.aggregatedGrandTotalBillable,
                                    color = Color.Black,
                                    style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                                )
                                AppSpacer(height = 2.dp)
                                AppText(
                                    text = "Hours",
                                    color = Color(0xFF004D87),
                                    style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                                )
                            }
                        }

                        AppSpacer(height = 8.dp)
                        AppDivider()
                        AppSpacer(height = 8.dp)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppText(
                                text = stringResource(id = R.string.total_hours),
                                color = Color(0xFF004D87),
                                style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                            )
                            AppText(
                                text = "${uiState.aggregatedGrandTotalTotal} Hours",
                                color = Color(0xFF004D87),
                                style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                            )
                        }
                    }
                }
                AppSpacer(height = 10.dp)
            }

            // 3. Team Member Weekly Card Lists (Single Project card container holding nested rows)
            val selectedProject = uiState.selectedProject
            val projectsToDisplay = if (selectedProject != null) {
                listOf(selectedProject)
            } else {
                uiState.aggregatedProjectsList
            }

            if (projectsToDisplay.isEmpty()) {
                item {
                    val emptyTitle = if (selectedProject != null) "No logs for selected project" else "No projects found"
                    AppEmptyState(title = emptyTitle)
                }
            } else {
                projectsToDisplay.forEach { project ->
                    val tmsList = mutableListOf<ProjectTMModel>()
                    val teamMembers = project.teamMembers
                    if (teamMembers != null) {
                        for (i in 0 until teamMembers.length()) {
                            val tObj = teamMembers.optJSONObject(i)
                            if (tObj != null) {
                                tmsList.add(ProjectTMModel().apply {
                                    name = tObj.optString("name", "")
                                    billableHours = tObj.optString("billableHours", "0")
                                    nonBillablehours = tObj.optString("nonBillablehours", "0")
                                    total = tObj.optString("total", "0")
                                })
                            }
                        }
                    }

                    // Filter by search query if any
                    val query = uiState.searchQuery.trim().lowercase()
                    val filteredTMs = if (uiState.selectedProjectTeamMember != null) {
                        tmsList.filter { it.name == uiState.selectedProjectTeamMember.name }
                    } else {
                        tmsList
                    }.filter {
                        it.name?.lowercase()?.contains(query) == true
                    }

                    if (filteredTMs.isNotEmpty()) {
                        item {
                            AppCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                backgroundColor = Color.White,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                ) {
                                    // Project Name (R.layout.title_name_layout style: blue, 18sp, regular)
                                    AppText(
                                        text = project.projectName ?: "",
                                        color = Color(0xFF004D87),
                                        style = TextStyle(fontSize = 18.sp, fontFamily = FontTokens.DefaultFontFamily)
                                    )
                                    AppSpacer(height = 6.dp)

                                    // Case Number (R.layout.black_normal_txt style: black, 15sp, regular)
                                    AppText(
                                        text = project.caseNo ?: "",
                                        color = Color.Black,
                                        style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                                    )
                                    AppSpacer(height = 6.dp)

                                    // Clients Row (Clients label: blue, names: black)
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        AppText(
                                            text = "Clients : ",
                                            color = Color(0xFF004D87),
                                            style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                                        )
                                        val clientsArray = project.clientNames
                                        val clientsText = if (clientsArray != null && clientsArray.length() > 0) {
                                            val list = mutableListOf<String>()
                                            for (i in 0 until clientsArray.length()) {
                                                list.add(clientsArray.getString(i))
                                            }
                                            list.joinToString(", ")
                                        } else {
                                            ""
                                        }
                                        AppText(
                                            text = clientsText,
                                            color = Color.Black,
                                            style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                                        )
                                    }

                                    AppSpacer(height = 10.dp)

                                    // List of team members
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        filteredTMs.forEachIndexed { index, tmItem ->
                                            AppSpacer(height = 6.dp)
                                            // Team Member Label Row
                                            Row(modifier = Modifier.fillMaxWidth()) {
                                                AppText(
                                                    text = stringResource(id = R.string.team_members__),
                                                    color = Color(0xFF004D87),
                                                    style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                                                )
                                                AppText(
                                                    text = tmItem.name ?: "",
                                                    color = Color.Black,
                                                    style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                                                )
                                            }
                                            AppSpacer(height = 6.dp)

                                            // Values Column Grid (matching WeeklyTimeSheetItem style)
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp, horizontal = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(
                                                    modifier = Modifier.weight(1f),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    AppText(
                                                        text = "${tmItem.billableHours ?: "0"} Hour",
                                                        color = Color.Black,
                                                        style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                                                    )
                                                    AppSpacer(height = 2.dp)
                                                    AppText(
                                                        text = stringResource(id = R.string.billable),
                                                        color = Color(0xFF004D87),
                                                        style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                                                    )
                                                }

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

                                                Column(
                                                    modifier = Modifier.weight(1f),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    AppText(
                                                        text = "${tmItem.nonBillablehours ?: "0"} Hour",
                                                        color = Color.Black,
                                                        style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                                                    )
                                                    AppSpacer(height = 2.dp)
                                                    AppText(
                                                        text = "Non Billable",
                                                        color = Color(0xFF004D87),
                                                        style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                                                    )
                                                }
                                            }

                                            AppSpacer(height = 6.dp)

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
                                                    text = "${tmItem.total ?: "0"} Hours",
                                                    color = Color.Black,
                                                    style = TextStyle(fontSize = 18.sp, fontFamily = FontTokens.DefaultFontFamily)
                                                )
                                            }

                                            if (index < filteredTMs.size - 1) {
                                                AppSpacer(height = 10.dp)
                                                AppDivider()
                                                AppSpacer(height = 10.dp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
