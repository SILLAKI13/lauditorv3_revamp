package com.digicoffer.lauditor.Documents.Models;

import org.json.JSONArray;
import org.json.JSONObject;


public class ViewDocumentsModel {
    String created;
    String description;
    boolean added_encryption;
    String expiration_date;
    String filename;
    String content_type;
//    String createdby;
    String id;
    boolean isdisabled;
    boolean is_disabled;
    boolean is_encrypted;
    boolean is_password;
    String name;
    String origin;
    String uploaded_by;
    String doc_type;
    String deletedBy;
    String deletedOn;
    String category;
    JSONObject tag;

    public boolean isIsdisabled() {
        return isdisabled;
    }

    public void setIsdisabled(boolean isdisabled) {
        this.isdisabled = isdisabled;
    }

    public JSONArray getTagslist() {
        return tagslist;
    }

    public void setTagslist(JSONArray tagslist) {
        this.tagslist = tagslist;
    }

    JSONArray tagslist;

    Boolean isChecked = false;

    public JSONObject getTag() {
        return tag;
    }

    public void setTag(JSONObject tagslist) {
        this.tag = tagslist;
    }

    public String getDoc_type() {
        return doc_type;
    }

    public void setDoc_type(String doc_type) {
        this.doc_type = doc_type;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    public String getDeletedOn() {
        return deletedOn;
    }

    public void setDeletedOn(String deletedOn) {
        this.deletedOn = deletedOn;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Boolean getChecked() {
        return isChecked;
    }

    public void setChecked(Boolean checked) {
        isChecked = checked;
    }

    public boolean isAdded_encryption() {
        return added_encryption;
    }

    public void setAdded_encryption(boolean added_encryption) {
        this.added_encryption = added_encryption;
    }

    public boolean getIsChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public String getContent_type() {
        return content_type;
    }

    public void setContent_type(String content_type) {
        this.content_type = content_type;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExpiration_date() {
        return expiration_date;
    }

    public void setExpiration_date(String expiration_date) {
        this.expiration_date = expiration_date;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isIs_disabled() {
        return is_disabled;
    }

    public void setIs_disabled(boolean is_disabled) {
        this.is_disabled = is_disabled;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getUploaded_by() {
        return uploaded_by;
    }

    public void setUploaded_by(String uploaded_by) {
        this.uploaded_by = uploaded_by;
    }
}


