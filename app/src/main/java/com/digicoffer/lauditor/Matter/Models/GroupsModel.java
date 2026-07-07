package com.digicoffer.lauditor.Matter.Models;

import java.util.List;

public class GroupsModel {
    String group_id;
    String group_name;
    private boolean isSelected;
    boolean isChecked;
    boolean isenabled;
    List<ClientGroupModel>clientGroupModelList;

    public List<ClientGroupModel> getClientGroupModelList() {
        return clientGroupModelList;
    }

    public void setClientGroupModelList(List<ClientGroupModel> clientGroupModelList) {
        this.clientGroupModelList = clientGroupModelList;
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

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GroupsModel that = (GroupsModel) o;

        return group_id != null ? group_id.equals(that.group_id) : that.group_id == null;
    }

    @Override
    public int hashCode() {
        return group_id != null ? group_id.hashCode() : 0;
    }
}
