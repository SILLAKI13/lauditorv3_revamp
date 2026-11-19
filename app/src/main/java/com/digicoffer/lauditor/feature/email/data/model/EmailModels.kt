package com.digicoffer.lauditor.feature.email.data.model

import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants

enum class EmailProvider {
    GOOGLE,
    OUTLOOK
}

data class EmailMessageItem(
    val msgId: String,
    val from: String,
    val to: String,
    val subject: String,
    val attachments: List<EmailAttachmentItem> = emptyList(),
    val body: String = ""
)

data class EmailAttachmentItem(
    val partId: String,
    val filename: String,
    val mimeType: String = "",
    val size: Int = 0
)

data class EmailLabelInfo(
    val id: String,
    val name: String,
    val type: String = "",
    val messagesTotal: Int = 0,
    val messagesUnread: Int = 0,
    val threadsTotal: Int = 0,
    val threadsUnread: Int = 0
)

data class LauditorAttachedDoc(
    val id: String,
    val name: String,
    val filename: String,
    var pathUrl: String = ""
)
