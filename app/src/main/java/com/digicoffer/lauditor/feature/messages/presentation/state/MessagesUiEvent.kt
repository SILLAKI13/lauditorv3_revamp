package com.digicoffer.lauditor.feature.messages.presentation.state

import android.app.Activity
import android.content.Context
import com.digicoffer.lauditor.feature.messages.data.model.ConversationItem
import com.digicoffer.lauditor.feature.messages.data.model.MessagesTab

sealed interface MessagesUiEvent {
    data class SelectTab(val tab: MessagesTab) : MessagesUiEvent
    data class SearchQueryChanged(val query: String) : MessagesUiEvent
    data class OpenConversation(val conversation: ConversationItem) : MessagesUiEvent
    object CloseConversation : MessagesUiEvent
    data class ToggleTeamExpanded(val teamId: String) : MessagesUiEvent
    data class ChatMessageInputChanged(val text: String) : MessagesUiEvent
    object SendMessage : MessagesUiEvent
    data class ChatSearchQueryChanged(val query: String) : MessagesUiEvent
    data class ToggleChatSearch(val isOpen: Boolean) : MessagesUiEvent
    object RefreshList : MessagesUiEvent
    data class LaunchVideoCall(val activity: Activity, val view: android.view.View? = null) : MessagesUiEvent
    object DismissAlert : MessagesUiEvent
}
