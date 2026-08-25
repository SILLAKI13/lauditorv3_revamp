package com.digicoffer.lauditor.feature.timesheets.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.designsystem.typography.FontTokens
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme
import com.digicoffer.lauditor.core.ui.common.cards.AppCard
import com.digicoffer.lauditor.core.ui.common.feedback.AppLoader
import com.digicoffer.lauditor.core.ui.common.foundation.AppSpacer
import com.digicoffer.lauditor.core.ui.common.foundation.AppText
import com.digicoffer.lauditor.feature.notifications.presentation.components.NotificationsSearchBar
import com.digicoffer.lauditor.feature.timesheets.presentation.state.TimesheetsUiEvent
import com.digicoffer.lauditor.feature.timesheets.presentation.state.TimesheetsUiState
import com.digicoffer.lauditor.feature.timesheets.presentation.viewmodel.TimesheetsViewModel

@Composable
fun TimesheetsRoute(
    viewModel: TimesheetsViewModel,
    onDatePickerClick: () -> Unit,
    showAggregatedTabs: Boolean = false,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    TimesheetsScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onDatePickerClick = onDatePickerClick,
        showAggregatedTabs = showAggregatedTabs,
        modifier = modifier
    )
}

@Composable
fun TimesheetsScreen(
    uiState: TimesheetsUiState,
    onEvent: (TimesheetsUiEvent) -> Unit,
    onDatePickerClick: () -> Unit,
    showAggregatedTabs: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFE4F2FF))
            .padding(top = 10.dp, bottom = 10.dp)
    ) {
        // 1. Primary Tabs (Aggregated vs My Timesheets)
        // REMOVED: Top tab-switching row is no longer displayed, per User instructions.
        // The screen follows the sidebar menu navigation flow directly, based on uiState.mainTab.

        // 2. Secondary Tabs (Not Submitted vs Submitted / Team Members vs Projects)
        val leftTabLabel = if (uiState.mainTab == "Aggregated") {
            stringResource(id = R.string.team_members)
        } else {
            stringResource(id = R.string.not_submitted)
        }

        val rightTabLabel = if (uiState.mainTab == "Aggregated") {
            stringResource(id = R.string.projects)
        } else {
            stringResource(id = R.string.submitted)
        }

        val leftTabValue = if (uiState.mainTab == "Aggregated") "TM" else "NS"
        val rightTabValue = if (uiState.mainTab == "Aggregated") "Project" else "Submitted"

        Row(
            modifier = Modifier
                .wrapContentWidth()
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 15.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            TabButton(
                text = leftTabLabel,
                isSelected = uiState.subTab == leftTabValue,
                onClick = { onEvent(TimesheetsUiEvent.SubTabSelected(leftTabValue)) },
                isLeft = true
            )
            TabButton(
                text = rightTabLabel,
                isSelected = uiState.subTab == rightTabValue,
                onClick = { onEvent(TimesheetsUiEvent.SubTabSelected(rightTabValue)) },
                isLeft = false
            )
        }

        AppSpacer(height = 10.dp)

        // 3. Date Navigation Card Layout (Matches legacy week_month_card design with 15dp padding margins)
        AppCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp), // 15dp margins on all sides!
            backgroundColor = Color.White,
            shape = RoundedCornerShape(8.dp),
            elevation = 4.dp // Matches elevation 4dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp) // padding 10dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Range button with 6.dp rounded corners & border
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFEFEFEF), RoundedCornerShape(6.dp))
                            .border(0.5.dp, Color.LightGray, RoundedCornerShape(6.dp))
                            .clickable { onEvent(TimesheetsUiEvent.PreviousRange) },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.baseline_arrow_back_ios_new_24),
                            contentDescription = "Previous",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Date range labels click targets (size 15sp, bold, start aligned)
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            horizontalAlignment = Alignment.Start, // Corrected label start-alignment
                            modifier = Modifier
                                .clickable(enabled = uiState.mainTab == "MyTimeSheets") { onDatePickerClick() }
                        ) {
                            AppText(
                                text = stringResource(id = R.string.from),
                                color = Color(0xFF004D87),
                                style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                            )
                            AppSpacer(height = 2.dp)
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFEFEFEF), RoundedCornerShape(6.dp))
                                    .border(0.5.dp, Color(0xFFCCCCCC), RoundedCornerShape(6.dp))
                                    .padding(vertical = 4.dp, horizontal = 8.dp)
                            ) {
                                AppText(
                                    text = uiState.fromDateString,
                                    color = Color.Black,
                                    style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                                )
                            }
                        }

                        AppSpacer(width = 16.dp)

                        Column(
                            horizontalAlignment = Alignment.Start, // Corrected label start-alignment
                            modifier = Modifier
                                .clickable(enabled = uiState.mainTab == "MyTimeSheets") { onDatePickerClick() }
                        ) {
                            AppText(
                                text = stringResource(id = R.string.to),
                                color = Color(0xFF004D87),
                                style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                            )
                            AppSpacer(height = 2.dp)
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFEFEFEF), RoundedCornerShape(6.dp))
                                    .border(0.5.dp, Color(0xFFCCCCCC), RoundedCornerShape(6.dp))
                                    .padding(vertical = 4.dp, horizontal = 8.dp)
                            ) {
                                AppText(
                                    text = uiState.toDateString,
                                    color = Color.Black,
                                    style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily)
                                )
                            }
                        }
                    }

                    // Next Range button with 6.dp rounded corners & border
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFEFEFEF), RoundedCornerShape(6.dp))
                            .border(0.5.dp, Color.LightGray, RoundedCornerShape(6.dp))
                            .clickable { onEvent(TimesheetsUiEvent.NextRange) },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.baseline_arrow_forward_ios_24),
                            contentDescription = "Next",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                if (uiState.mainTab == "Aggregated") {
                    AppSpacer(height = 12.dp)

                    // Bottom Week/Month Selector inside Date Card (size 15sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TabButton(
                            text = stringResource(id = R.string.week),
                            isSelected = uiState.isWeek == "week",
                            onClick = { onEvent(TimesheetsUiEvent.DateRangeTypeChanged("week")) },
                            isLeft = true
                        )
                        TabButton(
                            text = stringResource(id = R.string.month),
                            isSelected = uiState.isWeek == "month",
                            onClick = { onEvent(TimesheetsUiEvent.DateRangeTypeChanged("month")) },
                            isLeft = false
                        )
                    }
                }
            }
        }

        AppSpacer(height = 10.dp)

        // 4. Search bar (Visible only in Team Members aggregated view)
        if (uiState.mainTab == "Aggregated" && uiState.subTab == "TM") {
            Box(modifier = Modifier.padding(horizontal = 15.dp)) {
                NotificationsSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = { onEvent(TimesheetsUiEvent.SearchQueryChanged(it)) },
                    placeholder = stringResource(id = R.string.type_to_search),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            AppSpacer(height = 10.dp)
        }

        // 5. Scrollable content screen section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (uiState.mainTab == "Aggregated") {
                if (uiState.subTab == "TM") {
                    TeamMembersScreen(uiState = uiState)
                } else {
                    ProjectsScreen(uiState = uiState, onEvent = onEvent)
                }
            } else {
                if (uiState.subTab == "NS") {
                    NonSubmittedScreen(uiState = uiState, onEvent = onEvent)
                } else {
                    SubmittedScreen(uiState = uiState)
                }
            }
        }

        // Standard progress indicator loader overlays
        if (uiState.isLoading) {
            AppLoader()
        }
    }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLeft: Boolean = true
) {
    val backgroundColor = if (isSelected) Color(0xFF004D87) else Color.White
    val textColor = if (isSelected) Color.White else Color.Black
    val shape = if (isLeft) {
        RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp)
    } else {
        RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp)
    }

    Box(
        modifier = modifier
            .width(130.dp) // Set fixed width to make it compact & equal, matching wrap_content gravity layout
            .height(40.dp) // Height 40dp
            .background(backgroundColor, shape)
            .border(0.5.dp, Color(0xFFCCCCCC), shape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        AppText(
            text = text,
            color = textColor,
            style = TextStyle(fontSize = 15.sp, fontFamily = FontTokens.DefaultFontFamily),
            textAlign = TextAlign.Center
        )
    }
}
