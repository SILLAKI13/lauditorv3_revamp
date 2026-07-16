package com.digicoffer.lauditor.Relationships.Model

import org.json.JSONArray

class RelationshipsModel {
    var adminName: String? = null
    var deletedBy: String? = null

    @get:JvmName("isCanAccept")
    @set:JvmName("setCanAccept")
    var canAccept: Boolean = false

    var isExpandable: Boolean = false
    var isDisabled: Boolean = false

    @get:JvmName("isIstemp")
    @set:JvmName("setIstemp")
    var istemp: Boolean = false

    @get:JvmName("isCanAccess")
    @set:JvmName("setCanAccess")
    var canAccess: Boolean = false

    var clientType: String? = null
    var client_id: String? = null
    var consent: String? = null
    var created: String? = null
    var groups: JSONArray? = null
    var guid: String? = null
    var id: String? = null
    var isAccepted: Boolean = true
    var isClient: Boolean = false
    var isEditable: Boolean = false
    var matterList: JSONArray? = null
    var membersList: JSONArray? = null
    var name: String? = null
    var status: String? = ""
}
