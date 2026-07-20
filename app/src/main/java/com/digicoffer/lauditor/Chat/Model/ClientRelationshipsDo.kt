package com.digicoffer.lauditor.Chat.Model

import org.json.JSONArray
import java.util.ArrayList

class ClientRelationshipsDo {
    @get:JvmName("getAdminName")
    @set:JvmName("setAdminName")
    var adminName: String? = null

    @get:JvmName("getRel_id")
    @set:JvmName("setRel_id")
    var rel_id: String? = null

    @get:JvmName("isCanAccept")
    @set:JvmName("setCanAccept")
    var canAccept: Boolean = false

    @get:JvmName("getClientType")
    @set:JvmName("setClientType")
    var clientType: String? = null

    @get:JvmName("getClient_id")
    @set:JvmName("setClient_id")
    var client_id: String? = null

    @get:JvmName("getConsent")
    @set:JvmName("setConsent")
    var consent: String? = null

    @get:JvmName("getSource")
    @set:JvmName("setSource")
    var source: String? = "relationship"

    @get:JvmName("getLastMessage")
    @set:JvmName("setLastMessage")
    var lastMessage: String? = null

    @get:JvmName("getLastMessageTime")
    @set:JvmName("setLastMessageTime")
    var lastMessageTime: String? = null

    @get:JvmName("getRecyclerview_position")
    @set:JvmName("setRecyclerview_position")
    var recyclerview_position: Int = 0

    @get:JvmName("getChildCount")
    @set:JvmName("setChildCount")
    var childCount: Int = 0

    @get:JvmName("getCreated")
    @set:JvmName("setCreated")
    var created: String? = null

    @get:JvmName("getGroups")
    @set:JvmName("setGroups")
    var groups: JSONArray? = null

    @get:JvmName("getGuid")
    @set:JvmName("setGuid")
    var guid: String? = null

    @get:JvmName("getId")
    @set:JvmName("setId")
    var id: String? = null

    @get:JvmName("getUnread_count")
    @set:JvmName("setUnread_count")
    var unread_count: String? = null

    @get:JvmName("isExpanded")
    @set:JvmName("setExpanded")
    var expanded: Boolean = false

    @get:JvmName("isAccepted")
    @set:JvmName("setAccepted")
    var isAccepted: Boolean = false

    @get:JvmName("isClient")
    @set:JvmName("setClient")
    var isClient: Boolean = false

    @get:JvmName("isEditable")
    @set:JvmName("setEditable")
    var isEditable: Boolean = false

    @get:JvmName("getMatterList")
    @set:JvmName("setMatterList")
    var matterList: JSONArray? = null

    @get:JvmName("getName")
    @set:JvmName("setName")
    var name: String? = null

    @get:JvmName("getUsers")
    @set:JvmName("setUsers")
    var Users: JSONArray? = null

    @get:JvmName("getLastMessageTimestamp")
    @set:JvmName("setLastMessageTimestamp")
    var lastMessageTimestamp: Long = 0

    @get:JvmName("getChildList")
    @set:JvmName("setChildList")
    var childList = ArrayList<ChildDO>()

    @get:JvmName("getSubItems")
    @set:JvmName("setSubItems")
    var subItems: List<String>? = null

    @get:JvmName("getFirmName")
    @set:JvmName("setFirmName")
    var firmName: String? = null
}
