package com.digicoffer.lauditor.feature.notifications.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.Notifications.Models.Navigation
import com.digicoffer.lauditor.Notifications.Models.NotificationsDo
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.core.ui.common.inputs.AppCircleCheckbox
import com.digicoffer.lauditor.core.ui.common.badges.AppPillBadge
import com.digicoffer.lauditor.core.designsystem.theme.LauditorTheme
import com.digicoffer.lauditor.core.ui.common.feedback.AppLoader
import com.digicoffer.lauditor.feature.notifications.presentation.components.DateGroupCard
import com.digicoffer.lauditor.feature.notifications.presentation.components.NotificationsSearchBar
import com.digicoffer.lauditor.feature.notifications.presentation.state.NotificationsUiEvent
import com.digicoffer.lauditor.feature.notifications.presentation.state.NotificationsUiState
import com.digicoffer.lauditor.feature.notifications.presentation.viewmodel.NotificationsViewModel

private val GillSansRegular = FontFamily(Font(R.font.gill_sans_regular))

@Composable
fun NotificationsRoute(
    viewModel: NotificationsViewModel,
    onNavigate: (Navigation?) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.pendingNavigation) {
        val nav = uiState.pendingNavigation
        if (nav != null) {
            onNavigate(nav)
            viewModel.onEvent(NotificationsUiEvent.NavigationCompleted)
        }
    }

    NotificationsScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}

@Composable
fun NotificationsScreen(
    uiState: NotificationsUiState,
    onEvent: (NotificationsUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE4F2FF)) // @color/dashboard_background_color
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Header Row (Title + Badge Count + Action Buttons)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp) // @dimen/Fifteen_dp
                    .padding(top = 15.dp)       // @dimen/Fifteen_dp
            ) {
                // Title
                Text(
                    text = "List of Notifications",
                    style = TextStyle(
                        fontFamily = GillSansRegular,
                        fontSize = 18.sp,       // @dimen/eighteen_sp
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF004D87) // @color/blue
                    )
                )
                
                // Count badge
                AppPillBadge(
                    text = uiState.notificationList.size.toString(),
                    modifier = Modifier.padding(start = 8.dp) // layout_marginStart 8dp
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Action Buttons Enabled Status
                val buttonsAlpha = if (uiState.hasSelection) 1.0f else 0.4f
                
                // Envelope "Read All" Button
                Image(
                    painter = painterResource(id = R.drawable.ic_check),
                    contentDescription = "Mark Read",
                    modifier = Modifier
                        .alpha(buttonsAlpha)
                        .size(35.dp) // @dimen/thirty_five_dp
                        .background(Color.White, CircleShape)
                        .border(0.5.dp, Color(0xFFCCCCCC), CircleShape) // circle_b
                        .padding(8.dp)
                        .clickable(enabled = uiState.hasSelection) {
                            onEvent(NotificationsUiEvent.MarkSelectedAsRead)
                        }
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Trash "Delete All" Button
                Image(
                    painter = painterResource(id = R.drawable.ic_delete),
                    contentDescription = "Delete Selected",
                    modifier = Modifier
                        .alpha(buttonsAlpha)
                        .size(35.dp) // @dimen/thirty_five_dp
                        .background(Color.White, CircleShape)
                        .border(0.5.dp, Color(0xFFCCCCCC), CircleShape) // circle_b
                        .padding(8.dp)
                        .clickable(enabled = uiState.hasSelection) {
                            onEvent(NotificationsUiEvent.RequestDeleteSelected)
                        }
                )
            }
            
            // Search Bar Component
            NotificationsSearchBar(
                query = uiState.searchQuery,
                onQueryChange = { onEvent(NotificationsUiEvent.SearchQueryChanged(it)) }
            )
            
            // Select All Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp, end = 12.dp, bottom = 8.dp)
            ) {
                Text(
                    text = "Select All",
                    style = TextStyle(
                        fontFamily = GillSansRegular,
                        fontSize = 15.sp, // @dimen/Fifteen_dp
                        color = Color.Black
                    ),
                    modifier = Modifier.weight(1f)
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(end = 10.dp) // @dimen/ten_dp
                ) {
                    AppCircleCheckbox(
                        checked = uiState.isAllSelected,
                        onCheckedChange = { onEvent(NotificationsUiEvent.ToggleSelectAll(it)) },
                        size = 25.dp // @dimen/twenty_five
                    )
                }
            }
            
            // Main Content Area: Grouped Lists or Empty State
            if (uiState.filteredList.isEmpty()) {
                // Empty State Illustration Panel
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.empty_notification),
                        contentDescription = "No Notifications",
                        modifier = Modifier.size(130.dp)
                    )
                    Text(
                        text = "No Notifications Yet!",
                        style = TextStyle(
                            fontFamily = GillSansRegular,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        ),
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    Text(
                        text = "There are no notifications under here.",
                        style = TextStyle(
                            fontFamily = GillSansRegular,
                            fontSize = 14.sp,
                            color = Color(0xFF666666) // @color/dark_grey_new
                        ),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            } else {
                // Grouped Notification List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    items(uiState.groupedItems.toList()) { (dateKey, items) ->
                        DateGroupCard(
                            dateKey = dateKey,
                            notifications = items,
                            onCheckedChange = { notification, isChecked ->
                                onEvent(NotificationsUiEvent.NotificationCheckedChange(notification, isChecked))
                            },
                            onRowClick = { notification ->
                                onEvent(NotificationsUiEvent.ReadSingleNotification(notification))
                            },
                            highlightIds = uiState.activeHighlightIds,
                            onHighlightDismiss = { ids ->
                                onEvent(NotificationsUiEvent.HighlightCardDismissed(ids))
                            }
                        )
                    }
                    
                    // List Bottom Footer Spacer to prevent clipping R.dimen.twentyeight_dp
                    item {
                        Spacer(modifier = Modifier.height(28.dp))
                    }
                }
            }
        }
    }
    
    // Custom Confirmation & Alert Dialog Handling
    if (uiState.alertTitle != null && uiState.alertMessage != null) {
        AlertDialog(
            onDismissRequest = { onEvent(NotificationsUiEvent.DismissDialogs) },
            title = {
                Text(
                    text = uiState.alertTitle,
                    fontFamily = GillSansRegular,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
            },
            text = {
                Text(
                    text = uiState.alertMessage,
                    fontFamily = GillSansRegular,
                    fontSize = 14.sp,
                    color = Color.Black
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (uiState.alertTitle == "Confirmation") {
                            onEvent(NotificationsUiEvent.ConfirmDeleteSelected)
                        }
                        onEvent(NotificationsUiEvent.DismissDialogs)
                    }
                ) {
                    Text(
                        text = if (uiState.alertTitle == "Confirmation") "Yes" else "Ok",
                        fontFamily = GillSansRegular,
                        color = Color(0xFF004D87),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = if (uiState.alertTitle == "Confirmation") {
                {
                    TextButton(onClick = { onEvent(NotificationsUiEvent.DismissDialogs) }) {
                        Text(
                            text = "No",
                            fontFamily = GillSansRegular,
                            color = Color(0xFF666666),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else null
        )
    }
    
    // API Progress Loader Overlay
    if (uiState.isLoading) {
        AppLoader()
    }
}

@Preview(showBackground = true, name = "NotificationsScreen Loaded List Preview")
@Composable
fun NotificationsScreenPreview() {
    LauditorTheme {
        val testList = listOf(
            NotificationsDo().apply {
                id = "1"
                message = "New event notification update."
                timestamp = "2026-07-29T10:00:00.000Z"
                status = "unread"
            }
        )
        val state = NotificationsUiState(
            notificationList = testList,
            filteredList = testList,
            groupedItems = mapOf("Jul 29, 2026" to testList),
            hasSelection = true,
            isAllSelected = false
        )
        NotificationsScreen(uiState = state, onEvent = {})
    }
}
