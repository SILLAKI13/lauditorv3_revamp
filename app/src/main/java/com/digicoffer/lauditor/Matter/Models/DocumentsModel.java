package com.digicoffer.lauditor.Matter.Models;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;

public class DocumentsModel {
    String description;
    String docid;
    String doctype;
    String contentType;
    String expiration_date;
    boolean is_encrypted;
    boolean is_password;
    boolean added_encryption;
    String viewUrl;
    String name;
    String user_id;
    private boolean isSelected;
    boolean isChecked;
    boolean isenabled;
    String tag_type;
    String tag_name;
    JSONObject tags_list = new JSONObject();

    public String getExpiration_date() {
        return expiration_date;
    }

    public void setExpiration_date(String expiration_date) {
        this.expiration_date = expiration_date;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public boolean isIs_encrypted() {
        return is_encrypted;
    }

    public void setIs_encrypted(boolean is_encrypted) {
        this.is_encrypted = is_encrypted;
    }

    public boolean isIs_password() {
        return is_password;
    }

    public void setIs_password(boolean is_password) {
        this.is_password = is_password;
    }

    public boolean isAdded_encryption() {
        return added_encryption;
    }

    public void setAdded_encryption(boolean added_encryption) {
        this.added_encryption = added_encryption;
    }

    public String getViewUrl() {
        return viewUrl;
    }

    public void setViewUrl(String viewUrl) {
        this.viewUrl = viewUrl;
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

    public JSONObject getTags_list() {
        return tags_list;
    }

    public void setTags_list(JSONObject tags_list) {
        this.tags_list = tags_list;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    File file;


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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDocid() {
        return docid;
    }

    public void setDocid(String docid) {
        this.docid = docid;
    }

    public String getDoctype() {
        return doctype;
    }

    public void setDoctype(String doctype) {
        this.doctype = doctype;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DocumentsModel d = (DocumentsModel) o;

        return docid != null ? docid.equals(d.docid) : d.docid == null;
    }

    @Override
    public int hashCode() {
        return docid != null ? docid.hashCode() : 0;
    }
}
