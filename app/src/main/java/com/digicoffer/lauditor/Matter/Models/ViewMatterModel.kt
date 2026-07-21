package com.digicoffer.lauditor.Matter.Models

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import org.json.JSONArray
import org.json.JSONObject

@SuppressLint("ParcelCreator")
class ViewMatterModel : MatterModel(), Parcelable {
    var id: String = ""
    @get:JvmName("getCorpId_property")
    @set:JvmName("setCorpId_property")
    var CorpId: String = ""
    var created: String = ""
    override var created_date: String = ""
    override var matter_id: String = ""
    override var matter_title: String = ""
    var client_name: String = ""
    var caseNumber: String = ""
    var casetype: String = ""
    var courtName: String = ""
    var date_of_filling: String = ""

    @get:JvmName("isCorp_has_value")
    @set:JvmName("setCorp_has_value")
    var corp_has_value: Boolean = false

    @get:JvmName("isIsdisabled")
    @set:JvmName("setIsdisabled")
    var isdisabled: Boolean = false

    override var description: String = ""
    override var clients: JSONArray = JSONArray()
    override var documents: JSONArray = JSONArray()
    var groupAcls: JSONArray = JSONArray()
    var groups: JSONArray = JSONArray()
    var corporate: JSONArray = JSONArray()
    var client_id: String = ""
    var client_type: String = ""
    var doc_id: String = ""
    var doc_type: String = ""
    var user_id: String = ""
    var member_id: String = ""
    var Email: String = ""
    var member_name: String = ""
    var closedate: String = ""
    var matterNumber: String = ""
    var matterType: String = ""
    var startdate: String = ""
    var timesheets: JSONArray = JSONArray()
    var hearingDateDetails: JSONObject = JSONObject()

    @get:JvmName("isIs_editable")
    @set:JvmName("setIs_editable")
    var is_editable: Boolean = false

    var judges: String = ""
    var matterClosedDate: String = ""
    override var members: JSONArray = JSONArray()
    var nextHearingDate: String = ""
    var opponentAdvocates: JSONArray = JSONArray()
    var owner: JSONObject = JSONObject()
    var owner_name: String = ""
    var client: JSONObject = JSONObject()
    var priority: String = ""
    override var status: String = ""
    override var tags_list: JSONArray = JSONArray()
    var tempClients: JSONArray = JSONArray()
    var temporaryClients: JSONArray = JSONArray()
    var title: String = ""
    var group_id: String = ""

    var Advocate_name: String = ""
    var Number: String = ""

    @get:JvmName("isCanDelete")
    @set:JvmName("setCanDelete")
    var canDelete: Boolean = false

    var can_delete: Boolean
        get() = canDelete
        set(value) { canDelete = value }

    var corpId: String
        get() = CorpId
        set(value) { CorpId = value }

    @get:JvmName("isSelected")
    @set:JvmName("setSelected")
    var isSelected: Boolean = false

    @get:JvmName("isChecked")
    @set:JvmName("setChecked")
    var isChecked: Boolean = false

    override var groups_list: JSONArray = JSONArray()
    override var clients_list: JSONArray = JSONArray()
    override var documents_list: JSONArray = JSONArray()
    override var members_list: JSONArray = JSONArray()
    override var group_acls: JSONArray = JSONArray()

    var group_name: String = ""

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
    }

    fun set(i: Int, matterModel: MatterModel) {
    }

    fun size(): Int {
        return 0
    }
}
