package com.digicoffer.lauditor.feature.messages.presentation.state

import com.digicoffer.lauditor.feature.messages.data.model.ChatMessageItem
import com.digicoffer.lauditor.feature.messages.data.model.ConversationItem
import com.digicoffer.lauditor.feature.messages.data.model.MessagesTab

data class MessagesUiState(
    val selectedTab: MessagesTab = MessagesTab.CLIENTS,
    val isAamRole: Boolean = false,
    val isSoloCategory: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val clientsList: List<ConversationItem> = emptyList(),
    val teamsList: List<ConversationItem> = emptyList(),
    val searchQuery: String = "",
    
    // Conversation View
    val activeConversation: ConversationItem? = null,
    val isChatHistoryLoading: Boolean = false,
    val chatMessages: List<ChatMessageItem> = emptyList(),
    val chatSearchQuery: String = "",
    val isChatSearchActive: Boolean = false,
    val messageInputText: String = "",
    val isPartnerOnline: Boolean = false,
    val isPartnerTyping: Boolean = false,
    val lastSeenMessageId: String? = null,
    val isChatRestricted: Boolean = false,
    val chatRestrictionMessage: String = "Chat will be available 1 hour before your scheduled appointment"
) {
    val filteredClientsList: List<ConversationItem>
        get() {
            if (searchQuery.isBlank()) return clientsList
            val query = searchQuery.trim().lowercase()
            return clientsList.filter {
                it.displayName.lowercase().contains(query) ||
                it.clientType.lowercase().contains(query)
            }
        }

    val filteredTeamsList: List<ConversationItem>
        get() {
            if (searchQuery.isBlank()) return teamsList
            val query = searchQuery.trim().lowercase()
            return teamsList.filter {
                it.displayName.lowercase().contains(query) ||
                it.members.any { m -> m.displayName.lowercase().contains(query) }
            }
        }

    val filteredChatMessages: List<ChatMessageItem>
        get() {
            if (chatSearchQuery.isBlank()) return chatMessages
            val query = chatSearchQuery.trim().lowercase()
            return chatMessages.filter { it.message.lowercase().contains(query) }
        }
}
