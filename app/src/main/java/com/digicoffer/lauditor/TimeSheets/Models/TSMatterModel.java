package com.digicoffer.lauditor.TimeSheets.Models;

import org.json.JSONArray;

public class TSMatterModel {
    String Mattername;
    String Matterid;
    String Matter_type;
    Boolean iseditable = true;
    JSONArray Tasks;

    public Boolean getIseditable() {
        return iseditable;
    }

    public void setIseditable(Boolean iseditable) {
        this.iseditable = iseditable;
    }

    public JSONArray getTasks() {
        return Tasks;
    }

    public void setTasks(JSONArray tasks) {
        Tasks = tasks;
    }

    public String getMatter_type() {
        return Matter_type;
    }

    public void setMatter_type(String matter_type) {
        Matter_type = matter_type;
    }

    public String getMattername() {
        return Mattername;
    }

    public void setMattername(String mattername) {
        Mattername = mattername;
    }

    public String getMatterid() {
        return Matterid;
    }

    public void setMatterid(String matterid) {
        Matterid = matterid;
    }
}
