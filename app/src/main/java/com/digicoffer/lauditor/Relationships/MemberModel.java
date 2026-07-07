package com.digicoffer.lauditor.Relationships;

public class MemberModel {
    private String id;
    private String name;
    private boolean isChecked = false; // ✅ Added for checkbox state tracking

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

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
