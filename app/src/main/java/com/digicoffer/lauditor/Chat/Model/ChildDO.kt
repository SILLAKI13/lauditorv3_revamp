package com.digicoffer.lauditor.Chat.Model

class ChildDO {
    var id: String? = null
    var name: String? = null

    var guid: String? = null
        private set

    var source: String? = "relationship"

    @get:JvmName("getChild_position")
    @set:JvmName("setChild_position")
    var child_position: Int = 0

    @get:JvmName("getClient_id")
    @set:JvmName("setClient_id")
    var client_id: String? = null

    var uid: String? = null

    @get:JvmName("getUnread_count")
    @set:JvmName("setUnread_count")
    var unread_count: String? = null

    @get:JvmName("getFirmName")
    @set:JvmName("setFirmName")
    var FirmName: String? = null

    @get:JvmName("getLastMessage")
    @set:JvmName("setLastMessage")
    var lastMessage: String? = null

    @get:JvmName("getLastMessageTime")
    @set:JvmName("setLastMessageTime")
    var lastMessageTime: String? = null

    @get:JvmName("getLastMessageTimestamp")
    @set:JvmName("setLastMessageTimestamp")
    var lastMessageTimestamp: Long = 0

    fun setGuid(guid: String?): String? {
        this.guid = guid
        return guid
    }
}
