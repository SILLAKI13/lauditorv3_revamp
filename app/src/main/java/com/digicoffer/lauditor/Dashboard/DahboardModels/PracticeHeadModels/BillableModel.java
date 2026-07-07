package com.digicoffer.lauditor.Dashboard.DahboardModels.PracticeHeadModels;

public class BillableModel {
    String billableHours;
    String billablePercentage;

    public String getBillableHours() {
        return billableHours;
    }

    public void setBillableHours(String billableHours) {
        this.billableHours = billableHours;
    }

    public String getBillablePercentage() {
        return billablePercentage;
    }

    public void setBillablePercentage(String billablePercentage) {
        this.billablePercentage = billablePercentage;
    }

    public BillableModel(String billableHours, String billablePercentage) {
        this.billableHours = billableHours;
        this.billablePercentage = billablePercentage;
    }
}
