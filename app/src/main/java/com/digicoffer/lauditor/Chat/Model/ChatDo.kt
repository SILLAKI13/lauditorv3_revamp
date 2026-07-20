package com.digicoffer.lauditor.Chat.Model

import org.json.JSONArray
import org.json.JSONObject

class ChatDo {
    @get:JvmName("getGroupName")
    @set:JvmName("setGroupName")
    var GroupName: String? = null

    var name: String? = null

    @get:JvmName("getUserList")
    @set:JvmName("setUserList")
    var userList: JSONArray? = null

    @get:JvmName("getWithUser")
    @set:JvmName("setWithUser")
    var WithUser: String? = null

    @get:JvmName("getCount")
    @set:JvmName("setCount")
    var Count: String? = null

    var guid: String? = null

    var created: String? = null

    @get:JvmName("getRelationshipConsent")
    var RelationshipConsent: String? = null

    @get:JvmName("getClientType")
    @set:JvmName("setClientType")
    var ClientType: String? = null

    @get:JvmName("getGroups")
    @set:JvmName("setGroups")
    var Groups: String? = null

    @get:JvmName("getDocuments")
    @set:JvmName("setDocuments")
    var Documents: String? = null

    @get:JvmName("getRelType")
    @set:JvmName("setRelType")
    var RelType: String? = null

    @get:JvmName("getFirst_name")
    @set:JvmName("setFirst_name")
    var first_name: String? = null

    @get:JvmName("getLast_name")
    @set:JvmName("setLast_name")
    var last_name: String? = null

    @get:JvmName("getCoffer_id")
    @set:JvmName("setCoffer_id")
    var Coffer_id: String? = null

    @get:JvmName("getClient")
    @set:JvmName("setClient")
    var Client: String? = null

    @get:JvmName("getRelationship_id")
    @set:JvmName("setRelationship_id")
    var relationship_id: String? = null

    var id: String? = null

    @get:JvmName("getBizType")
    @set:JvmName("setBizType")
    var bizType: String? = null

    @get:JvmName("isClient")
    @set:JvmName("setIsClient")
    var isClient: Boolean = false

    @get:JvmName("isAccepted")
    @set:JvmName("setIsAccepted")
    var isAccepted: Boolean = false

    @JvmField
    var documents: JSONObject? = null

    @get:JvmName("getViewDocuments")
    @set:JvmName("setViewDocuments")
    var viewDocuments: JSONObject? = null

    @get:JvmName("getUID")
    @set:JvmName("setUID")
    var uid: String? = null

    fun setDocuments(documents: JSONObject?) {
        this.documents = documents
    }
}
