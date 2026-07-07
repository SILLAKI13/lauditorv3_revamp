package com.digicoffer.lauditor.Matter.Models;

import org.json.JSONArray;

public class HistoryModel {
    String id;
    String title;
    String to_ts;
    String notes;
    JSONArray notes_list;
    String from_ts;
    String event_type;
    String description;
    boolean allday;

    public JSONArray getNotes_list() {
        return notes_list;
    }

    public void setNotes_list(JSONArray notes_list) {
        this.notes_list = notes_list;
    }

    public boolean isAllday() {
        return allday;
    }

    public void setAllday(boolean allday) {
        this.allday = allday;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTo_ts() {
        return to_ts;
    }

    public void setTo_ts(String to_ts) {
        this.to_ts = to_ts;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getFrom_ts() {
        return from_ts;
    }

    public void setFrom_ts(String from_ts) {
        this.from_ts = from_ts;
    }

    public String getEvent_type() {
        return event_type;
    }

    public void setEvent_type(String event_type) {
        this.event_type = event_type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
