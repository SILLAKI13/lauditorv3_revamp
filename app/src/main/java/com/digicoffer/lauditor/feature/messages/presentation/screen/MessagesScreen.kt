package com.digicoffer.lauditor.feature.messages.presentation.screen

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.digicoffer.lauditor.core.ui.common.feedback.AppLoader
import com.digicoffer.lauditor.feature.messages.data.model.MessagesTab
import com.digicoffer.lauditor.feature.messages.presentation.components.ConversationItemCard
import com.digicoffer.lauditor.feature.messages.presentation.components.MessagesEmptyState
import com.digicoffer.lauditor.feature.messages.presentation.components.NavyBluePrimary
import com.digicoffer.lauditor.feature.messages.presentation.components.PageBgLightBlue
import com.digicoffer.lauditor.feature.messages.presentation.components.SearchBox
import com.digicoffer.lauditor.feature.messages.presentation.components.TeamAccordionCard
import com.digicoffer.lauditor.feature.messages.presentation.state.MessagesUiEvent
import com.digicoffer.lauditor.feature.messages.presentation.viewmodel.MessagesViewModel

@Composable
fun MessagesScreen(
    viewModel: MessagesViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.onEvent(MessagesUiEvent.DismissAlert)
        }
    }

    if (state.activeConversation != null) {
        ConversationScreen(
            state = state,
            onEvent = viewModel::onEvent
        )
    } else {
        BackHandler {
            onNavigateBack()
        }

        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = PageBgLightBlue,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Section Title
                Text(
                    text = if (state.selectedTab == MessagesTab.CLIENTS) "List Of Clients" else "List Of Team Members",
                    color = NavyBluePrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp)
                )

                // Rounded Pill Search Bar
                SearchBox(
                    query = state.searchQuery,
                    onQueryChanged = { viewModel.onEvent(MessagesUiEvent.SearchQueryChanged(it)) },
                    placeholder = "Type to search"
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(modifier = Modifier.fillMaxSize()) {
                    if (state.isLoading) {
                        AppLoader()
                    } else if (state.selectedTab == MessagesTab.CLIENTS) {
                        val clients = state.filteredClientsList
                        if (clients.isEmpty()) {
                            MessagesEmptyState(
                                message = "No Messages Yet!",
                                subtitle = "There are no clients under here."
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = 16.dp)
                            ) {
                                items(
                                    items = clients,
                                    key = { it.guid }
                                ) { client ->
                                    ConversationItemCard(
                                        item = client,
                                        onClick = {
                                            viewModel.onEvent(MessagesUiEvent.OpenConversation(client))
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        val teams = state.filteredTeamsList
                        if (teams.isEmpty()) {
                            MessagesEmptyState(
                                message = "No Messages Yet!",
                                subtitle = "There are no teams under here."
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = 16.dp)
                            ) {
                                items(
                                    items = teams,
                                    key = { it.guid.ifEmpty { it.id } }
                                ) { teamItem ->
                                    if (teamItem.members.isNotEmpty()) {
                                        TeamAccordionCard(
                                            team = teamItem,
                                            onToggleExpand = {
                                                viewModel.onEvent(MessagesUiEvent.ToggleTeamExpanded(teamItem.guid.ifEmpty { teamItem.id }))
                                            },
                                            onMemberClick = { member ->
                                                viewModel.onEvent(MessagesUiEvent.OpenConversation(member))
                                            }
                                        )
                                    } else {
                                        ConversationItemCard(
                                            item = teamItem,
                                            onClick = {
                                                viewModel.onEvent(MessagesUiEvent.OpenConversation(teamItem))
                                            }
                                        )
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
