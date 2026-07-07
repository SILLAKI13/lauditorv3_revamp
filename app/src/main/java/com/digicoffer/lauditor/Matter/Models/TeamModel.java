package com.digicoffer.lauditor.Matter.Models;

import org.json.JSONArray;

public class TeamModel {
    String tm_id;
    String tm_name;
    String user_id;
    private boolean isSelected;
    boolean isChecked;
    boolean isenabled;
    JSONArray groups;

    public JSONArray getGroups() {
        return groups;
    }

    public void setGroups(JSONArray groups) {
        this.groups = groups;
    }

    public String getTm_id() {
        return tm_id;
    }

    public void setTm_id(String tm_id) {
        this.tm_id = tm_id;
    }

    public String getTm_name() {
        return tm_name;
    }

    public void setTm_name(String tm_name) {
        this.tm_name = tm_name;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
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

    public boolean isIsenabled() {
        return isenabled;
    }

    public void setIsenabled(boolean isenabled) {
        this.isenabled = isenabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TeamModel that = (TeamModel) o;

        return tm_id != null ? tm_id.equals(that.tm_id) : that.tm_id == null;
    }

    @Override
    public int hashCode() {
        return tm_id != null ? tm_id.hashCode() : 0;
    }
}
