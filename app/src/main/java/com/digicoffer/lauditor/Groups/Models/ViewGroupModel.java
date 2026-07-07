package com.digicoffer.lauditor.Groups.Models;

import org.json.JSONArray;

public class ViewGroupModel {
    String id;
    String name;
    String description;
    String created;
    String User_type;
    String owner_name;
    String date;
    String group_id;
    String Group_name;
    String memberCount;
    JSONArray members;
    String group_head_id;
    String group_head_name;
    private boolean isSelected;
    boolean isChecked;
    boolean can_delete = true;
    boolean can_assign_docs = false;
    boolean isdisabled;

    public String getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(String memberCount) {
        this.memberCount = memberCount;
    }

    public boolean isIsdisabled() {
        return isdisabled;
    }

    public void setIsdisabled(boolean isdisabled) {
        this.isdisabled = isdisabled;
    }

    public boolean isCan_delete() {
        return can_delete;
    }

    public void setCan_delete(boolean can_delete) {
        this.can_delete = can_delete;
    }

    public boolean isCan_assign_docs() {
        return can_assign_docs;
    }

    public void setCan_assign_docs(boolean can_assign_docs) {
        this.can_assign_docs = can_assign_docs;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getGroup_head_id() {
        return group_head_id;
    }

    public void setGroup_head_id(String group_head_id) {
        this.group_head_id = group_head_id;
    }

    public String getGroup_head_name() {
        return group_head_name;
    }

    public void setGroup_head_name(String group_head_name) {
        this.group_head_name = group_head_name;
    }

    public JSONArray getMembers() {
        return members;
    }

    public void setMembers(JSONArray members) {
        this.members = members;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getGroup_name() {
        return Group_name;
    }

    public void setGroup_name(String group_name) {
        Group_name = group_name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }
//    public ViewGroupModel(String user_type, String owner_name, String date) {
//        User_type = user_type;
//        this.owner_name = owner_name;
//        this.date = date;
//    }

    public String getUser_type() {
        return User_type;
    }

    public void setUser_type(String user_type) {
        User_type = user_type;
    }

    public String getOwner_name() {
        return owner_name;
    }

    public void setOwner_name(String owner_name) {
        this.owner_name = owner_name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
