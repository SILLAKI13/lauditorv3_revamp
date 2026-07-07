package com.digicoffer.lauditor.Matter.Models;

import androidx.appcompat.widget.AppCompatButton;

import org.json.JSONArray;

public class MatterModel {

    String matter_title = "";
    String corp_client_id = "";
    String case_number = "";
    String case_type = "";
    String description = "";
    String date_of_filing = "";
    String created_date = "";
    String court = "";
    String matter_id = "";
    String judge = "";
    String start_date = "";
    String end_date = "";
    String case_priority = "";
    String status = "";

    JSONArray opponent_advocate = new JSONArray();
    JSONArray clients = new JSONArray();
    JSONArray group_acls = new JSONArray();
    JSONArray members = new JSONArray();
    JSONArray corp_clients_list = new JSONArray();
    JSONArray temp_clients_list = new JSONArray();
    JSONArray groups_list = new JSONArray();
    JSONArray clients_list = new JSONArray();
    JSONArray members_list = new JSONArray();
    JSONArray documents_list = new JSONArray();
    JSONArray tags_list = new JSONArray();
    JSONArray documents = new JSONArray();

    String tv_matter_title = "";

    public MatterModel() {
        // Default constructor (all fields are already initialized above)
    }

    // ------------------ Getters & Setters ------------------ //

    public String getMatter_id() {
        return matter_id;
    }

    public void setMatter_id(String matter_id) {
        this.matter_id = matter_id;
    }

    public JSONArray getTags_list() {
        return tags_list;
    }

    public void setTags_list(JSONArray tags_list) {
        this.tags_list = tags_list;
    }

    public String getCreated_date() {
        return created_date;
    }

    public void setCreated_date(String created_date) {
        this.created_date = created_date;
    }

    public String getTv_matter_title() {
        return tv_matter_title;
    }

    public void setTv_matter_title(String tv_matter_title) {
        this.tv_matter_title = tv_matter_title;
    }

    public String getCorp_client_id() {
        return corp_client_id;
    }

    public void setCorp_client_id(String corp_client_id) {
        this.corp_client_id = corp_client_id;
    }

    public JSONArray getTemp_clients_list() {
        return temp_clients_list;
    }

    public void setTemp_clients_list(JSONArray temp_clients_list) {
        this.temp_clients_list = temp_clients_list;
    }

    public JSONArray getCorp_clients_list() {
        return corp_clients_list;
    }

    public void setCorp_clients_list(JSONArray corp_clients_list) {
        this.corp_clients_list = corp_clients_list;
    }

    public JSONArray getDocuments_list() {
        return documents_list;
    }

    public void setDocuments_list(JSONArray documents_list) {
        this.documents_list = documents_list;
    }

    public String getStart_date() {
        return start_date;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public String getEnd_date() {
        return end_date;
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
    }

    public JSONArray getDocuments() {
        return documents;
    }

    public void setDocuments(JSONArray documents) {
        this.documents = documents;
    }

    public JSONArray getGroups_list() {
        return groups_list;
    }

    public void setGroups_list(JSONArray groups_list) {
        this.groups_list = groups_list;
    }

    public JSONArray getClients_list() {
        return clients_list;
    }

    public void setClients_list(JSONArray clients_list) {
        this.clients_list = clients_list;
    }

    public JSONArray getMembers_list() {
        return members_list;
    }

    public void setMembers_list(JSONArray members_list) {
        this.members_list = members_list;
    }

    public JSONArray getMembers() {
        return members;
    }

    public void setMembers(JSONArray members) {
        this.members = members;
    }

    public JSONArray getGroup_acls() {
        return group_acls;
    }

    public void setGroup_acls(JSONArray group_acls) {
        this.group_acls = group_acls;
    }

    public JSONArray getClients() {
        return clients;
    }

    public void setClients(JSONArray clients) {
        this.clients = clients;
    }

    public String getMatter_title() {
        return matter_title;
    }

    public void setMatter_title(String matter_title) {
        this.matter_title = matter_title;
    }

    public String getCase_number() {
        return case_number;
    }

    public void setCase_number(String case_number) {
        this.case_number = case_number;
    }

    public String getCase_type() {
        return case_type;
    }

    public void setCase_type(String case_type) {
        this.case_type = case_type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate_of_filing() {
        return date_of_filing;
    }

    public void setDate_of_filing(String date_of_filing) {
        this.date_of_filing = date_of_filing;
    }

    public String getCourt() {
        return court;
    }

    public void setCourt(String court) {
        this.court = court;
    }

    public String getJudge() {
        return judge;
    }

    public void setJudge(String judge) {
        this.judge = judge;
    }

    public String getCase_priority() {
        return case_priority;
    }

    public void setCase_priority(String case_priority) {
        this.case_priority = case_priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public JSONArray getOpponent_advocate() {
        return opponent_advocate;
    }

    public void setOpponent_advocate(JSONArray opponent_advocate) {
        this.opponent_advocate = opponent_advocate;
    }
}
