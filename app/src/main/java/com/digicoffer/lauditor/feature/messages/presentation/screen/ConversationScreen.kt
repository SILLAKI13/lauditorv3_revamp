package com.digicoffer.lauditor.feature.messages.presentation.screen

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import com.digicoffer.lauditor.core.ui.common.feedback.AppLoader
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.feature.messages.presentation.components.ChatComposerBar
import com.digicoffer.lauditor.feature.messages.presentation.components.ChatHeaderBar
import com.digicoffer.lauditor.feature.messages.presentation.components.ChatSearchBox
import com.digicoffer.lauditor.feature.messages.presentation.components.MessageBubble
import com.digicoffer.lauditor.feature.messages.presentation.components.NavyBluePrimary
import com.digicoffer.lauditor.feature.messages.presentation.components.TypingIndicatorBubble
import com.digicoffer.lauditor.feature.messages.presentation.state.MessagesUiEvent
import com.digicoffer.lauditor.feature.messages.presentation.state.MessagesUiState

@Composable
fun ConversationScreen(
    state: MessagesUiState,
    onEvent: (MessagesUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeConversation = state.activeConversation ?: return
    val context = LocalContext.current
    val activity = context as? Activity
    val listState = rememberLazyListState()

    BackHandler {
        onEvent(MessagesUiEvent.CloseConversation)
    }

    LaunchedEffect(state.filteredChatMessages.size, state.isPartnerTyping) {
        val totalItems = state.filteredChatMessages.size + if (state.isPartnerTyping) 1 else 0
        if (totalItems > 0) {
            listState.animateScrollToItem(totalItems - 1)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                ChatHeaderBar(
                    contact = activeConversation,
                    isOnline = state.isPartnerOnline,
                    onBackClick = { onEvent(MessagesUiEvent.CloseConversation) },
                    onToggleSearch = {
                        onEvent(MessagesUiEvent.ToggleChatSearch(!state.isChatSearchActive))
                    }
                )

                if (state.isChatSearchActive) {
                    ChatSearchBox(
                        query = state.chatSearchQuery,
                        onQueryChanged = { onEvent(MessagesUiEvent.ChatSearchQueryChanged(it)) },
                        onClose = {
                            onEvent(MessagesUiEvent.ToggleChatSearch(false))
                        },
                        placeholder = "Search in chat...",
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        },
        bottomBar = {
            if (state.isChatRestricted || activeConversation.isChatRestricted) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Surface(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                        color = Color(0xFFE8F1FB),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF90CAF9)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        androidx.compose.material3.Text(
                            text = state.chatRestrictionMessage.ifEmpty { 
                                activeConversation.chatRestrictionMessage 
                            },
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
                                color = NavyBluePrimary,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        )
                    }
                }
            } else {
                ChatComposerBar(
                    text = state.messageInputText,
                    onTextChanged = { onEvent(MessagesUiEvent.ChatMessageInputChanged(it)) },
                    onSendClick = {
                        if (!com.digicoffer.lauditor.CommonFiles.GlobalFiles.AndroidUtils.checkSubPlanValidation(activity, "messages")) {
                            onEvent(MessagesUiEvent.SendMessage)
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Background legal/chat doodle pattern
            Image(
                painter = painterResource(id = R.drawable.chat_background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            if (state.isChatHistoryLoading && state.chatMessages.isEmpty()) {
                AppLoader()
            } else {
                val messages = state.filteredChatMessages
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    itemsIndexed(
                        items = messages,
                        key = { index, item -> "${item.id}_${item.createdAtFormatted}_$index" }
                    ) { index, msg ->
                        val showHeader = if (index == 0) {
                            msg.dateGroupHeader.isNotEmpty()
                        } else {
                            msg.dateGroupHeader != messages[index - 1].dateGroupHeader
                        }

                        MessageBubble(
                            message = msg,
                            showDateHeader = showHeader
                        )
                    }

                    if (state.isPartnerTyping) {
                        item(key = "typing_indicator") {
                            TypingIndicatorBubble()
                        }
                    }
                }
            }
        }
    }
}
