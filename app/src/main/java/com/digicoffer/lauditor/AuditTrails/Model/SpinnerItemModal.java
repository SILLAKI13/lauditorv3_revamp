package com.digicoffer.lauditor.AuditTrails.Model;

public class SpinnerItemModal {
    String name;

    public SpinnerItemModal(String category_name) {
        this.name = category_name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
