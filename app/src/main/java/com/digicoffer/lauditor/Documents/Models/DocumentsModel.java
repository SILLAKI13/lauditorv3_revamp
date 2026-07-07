package com.digicoffer.lauditor.Documents.Models;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

public class DocumentsModel {
    String name;

    public String getContent_type() {
        return Content_type;
    }

    public void setContent_type(String content_type) {
        Content_type = content_type;
    }

    String id = "";
    String Content_type;
    boolean isencrypted = false;
    String filename = "";
    String expiration_date = "";
    String group_id;
    String group_name;
    private boolean isSelected;
    boolean isChecked;
    boolean isenabled = false;
    File file;
    String tag_type;
    String tag_name;
    JSONObject tags;
    String description = "";
    JSONArray groups;
    private boolean isGroupSelected;
    boolean isGroupChecked;
    boolean isGroupenabled;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public boolean isGroupSelected() {
        return isGroupSelected;
    }

    public void setGroupSelected(boolean groupSelected) {
        isGroupSelected = groupSelected;
    }

    public boolean isGroupChecked() {
        return isGroupChecked;
    }

    public void setGroupChecked(boolean groupChecked) {
        isGroupChecked = groupChecked;
    }

    public boolean isGroupenabled() {
        return isGroupenabled;
    }

    public void setGroupenabled(boolean groupenabled) {
        isGroupenabled = groupenabled;
    }

    public String getGroup_id() {
        return group_id;
    }

    public Boolean getIsencrypted() {
        return isencrypted;
    }

    public void setIsencrypted(Boolean isencrypted) {
        this.isencrypted = isencrypted;
    }

    public String getExpiration_date() {
        return expiration_date;
    }

    public void setExpiration_date(String expiration_date) {
        this.expiration_date = expiration_date;
    }

    public String group_id() {
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

    public JSONArray getGroups() {
        return groups;
    }

    public void setGroups(JSONArray groups) {
        this.groups = groups;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public JSONObject getTags() {
        return tags;
    }

    public void setTags(JSONObject tags) {
        this.tags = tags;
    }

    public String getTag_type() {
        return tag_type;
    }

    public void setTag_type(String tag_type) {
        this.tag_type = tag_type;
    }

    public String getTag_name() {
        return tag_name;
    }

    public void setTag_name(String tag_name) {
        this.tag_name = tag_name;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

        DocumentsModel that = (DocumentsModel) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
