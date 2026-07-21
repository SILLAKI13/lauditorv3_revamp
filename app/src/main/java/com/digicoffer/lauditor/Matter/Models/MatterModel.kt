package com.digicoffer.lauditor.Matter.Models

import org.json.JSONArray

open class MatterModel {
    open var matter_title: String = ""
    open var corp_client_id: String = ""
    open var case_number: String = ""
    open var case_type: String = ""
    open var description: String = ""
    open var date_of_filing: String = ""
    open var created_date: String = ""
    open var court: String = ""
    open var matter_id: String = ""
    open var judge: String = ""
    open var start_date: String = ""
    open var end_date: String = ""
    open var case_priority: String = ""
    open var status: String = ""

    open var opponent_advocate: JSONArray = JSONArray()
    open var clients: JSONArray = JSONArray()
    open var group_acls: JSONArray = JSONArray()
    open var members: JSONArray = JSONArray()
    open var corp_clients_list: JSONArray = JSONArray()
    open var temp_clients_list: JSONArray = JSONArray()
    open var groups_list: JSONArray = JSONArray()
    open var clients_list: JSONArray = JSONArray()
    open var members_list: JSONArray = JSONArray()
    open var documents_list: JSONArray = JSONArray()
    open var tags_list: JSONArray = JSONArray()
    open var documents: JSONArray = JSONArray()

    open var tv_matter_title: String = ""
}
