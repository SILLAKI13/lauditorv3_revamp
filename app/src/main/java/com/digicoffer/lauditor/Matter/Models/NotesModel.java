package com.digicoffer.lauditor.Matter.Models;

public class NotesModel {
    private String notes;
    private String added_by;
    private String add_on;
    private String firm_name;

    public NotesModel(String notes, String added_by, String add_on, String firm_name) {
        this.notes = notes;
        this.added_by = added_by;
        this.add_on = add_on;
        this.firm_name = firm_name;
    }

    public String getNotes() { return notes; }
    public String getAdded_by() { return added_by; }
    public String getAdd_on() { return add_on; }
    public String getFirm_name() { return firm_name; }
}

