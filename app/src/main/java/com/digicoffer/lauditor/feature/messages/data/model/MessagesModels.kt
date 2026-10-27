package com.digicoffer.lauditor.feature.messages.data.model

enum class MessagesTab {
    CLIENTS,
    TEAMS
}

data class ConversationItem(
    val guid: String,
    val id: String = "",
    val name: String = "",
    val firmName: String? = null,
    val clientType: String = "",
    val source: String = "",
    val lastMessage: String? = null,
    val lastMessageTime: String? = null,
    val lastMessageTimestamp: Long = 0L,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val isTyping: Boolean = false,
    val isExpanded: Boolean = false,
    val isChatRestricted: Boolean = false,
    val chatRestrictionMessage: String = "Chat will be available 1 hour before your scheduled appointment",
    val appointmentFrom: String? = null,
    val members: List<ConversationItem> = emptyList()
) {
    val displayName: String
        get() = if (firmName.isNullOrEmpty()) name else "$name - $firmName"

    val avatarInitial: String
        get() = displayName.trim().take(1).uppercase().ifEmpty { "?" }
}

data class ChatMessageItem(
    val id: String = "",
    val message: String = "",
    val isSentByMe: Boolean = false,
    val senderName: String = "",
    val createdAtFormatted: String = "",
    val dateGroupHeader: String = "",
    val timeFormatted: String = "",
    val isCurrentChat: Boolean = false
)
