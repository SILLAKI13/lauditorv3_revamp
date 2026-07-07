package com.digicoffer.lauditor.Groups.Models;

public class ActionModel {
    private String name;
    private boolean isEnabled;

    public ActionModel(String name) {
        this.name = name;
        this.isEnabled = true; // default to enabled
    }

    public ActionModel(String name, boolean isEnabled) {
        this.name = name;
        this.isEnabled = isEnabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
}
