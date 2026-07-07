package com.digicoffer.lauditor.TimeSheets.Models;

import org.json.JSONArray;
import org.json.JSONObject;

public class EventsModel {
    String billing;
    String taskName;
    String total;
    Boolean is_editable=true;
    JSONObject mon;
    JSONObject tue;
    JSONObject wed;
    JSONObject thu;
    JSONObject fri;
    JSONObject sat;
    JSONObject sun;
    String matter_type;
    String matter_name;
    String matter_id;

    public Boolean getIs_editable() {
        return is_editable;
    }

    public void setIs_editable(Boolean is_editable) {
        this.is_editable = is_editable;
    }

    public String getMatter_type() {
        return matter_type;
    }

    public void setMatter_type(String matter_type) {
        this.matter_type = matter_type;
    }

    public String getMatter_name() {
        return matter_name;
    }

    public void setMatter_name(String matter_name) {
        this.matter_name = matter_name;
    }

    public String getMatter_id() {
        return matter_id;
    }

    public void setMatter_id(String matter_id) {
        this.matter_id = matter_id;
    }

    public String getBilling() {
        return billing;
    }

    public void setBilling(String billing) {
        this.billing = billing;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public JSONObject getMon() {
        return mon;
    }

    public void setMon(JSONObject mon) {
        this.mon = mon;
    }

    public JSONObject getTue() {
        return tue;
    }

    public void setTue(JSONObject tue) {
        this.tue = tue;
    }

    public JSONObject getWed() {
        return wed;
    }

    public void setWed(JSONObject wed) {
        this.wed = wed;
    }

    public JSONObject getThu() {
        return thu;
    }

    public void setThu(JSONObject thu) {
        this.thu = thu;
    }

    public JSONObject getFri() {
        return fri;
    }

    public void setFri(JSONObject fri) {
        this.fri = fri;
    }

    public JSONObject getSat() {
        return sat;
    }

    public void setSat(JSONObject sat) {
        this.sat = sat;
    }

    public JSONObject getSun() {
        return sun;
    }

    public void setSun(JSONObject sun) {
        this.sun = sun;
    }
}
