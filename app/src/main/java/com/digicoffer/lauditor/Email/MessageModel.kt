package com.digicoffer.lauditor.Email

class MessageModel {
    @JvmField
    var msgId: String? = null
    fun getMsgId(): String? = msgId
    fun setMsgId(msgId: String?) {
        this.msgId = msgId
    }

    @JvmField
    var subject: String? = null
    fun getSubject(): String? = subject
    fun setSubject(subject: String?) {
        this.subject = subject
    }

    private var _from: String? = null
    var from: String?
        @JvmName("getFrom")
        get() {
            val currentFrom = _from ?: return ""
            val parts = currentFrom.split("<")
            if (parts.size >= 2) {
                var namePart = parts[0].trim()
                val atIndex = namePart.indexOf('@')
                if (atIndex != -1) {
                    namePart = namePart.substring(0, atIndex).trim()
                }
                return namePart
            }
            return currentFrom
        }
        @JvmName("setFrom")
        set(value) {
            _from = value
        }

    @JvmField
    var to: String? = null
    fun getTo(): String? = to
    fun setTo(to: String?) {
        this.to = to
    }

    @JvmField
    var attachments: List<AttachmentModel>? = null
    fun getAttachments(): List<AttachmentModel>? = attachments
    fun setAttachments(attachments: List<AttachmentModel>?) {
        this.attachments = attachments
    }

    fun getMessage(): Int = 0

    fun isAttachment(): Boolean {
        return attachments?.isEmpty() ?: true
    }

    fun getAttachment(): List<AttachmentModel>? {
        return attachments
    }
}
