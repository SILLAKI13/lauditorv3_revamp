package com.digicoffer.lauditor.Meetings.Models;

import java.util.Objects;

public class RelationshipsDO {
    String id;
    String name;
    String type;
    String entity_id;
    private boolean isSelected;
    boolean isChecked;
    boolean isenabled;

    public String getEntity_id() {
        return entity_id;
    }

    public void setEntity_id(String entity_id) {
        this.entity_id = entity_id;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    // Override equals to compare objects based on the ID field
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RelationshipsDO that = (RelationshipsDO) o;
        return Objects.equals(id, that.id);
    }

    // Override hashCode to compute hash based on the ID field
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
