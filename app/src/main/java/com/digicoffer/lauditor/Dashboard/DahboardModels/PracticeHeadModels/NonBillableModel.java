package com.digicoffer.lauditor.Dashboard.DahboardModels.PracticeHeadModels;

public class NonBillableModel {
    String nonBillableHours;
    String nonBillablePercentage;

    public NonBillableModel(String nonBillableHours, String nonBillablePercentage) {
        this.nonBillableHours = nonBillableHours;
        this.nonBillablePercentage = nonBillablePercentage;
    }

    public String getNonBillableHours() {
        return nonBillableHours;
    }

    public void setNonBillableHours(String nonBillableHours) {
        this.nonBillableHours = nonBillableHours;
    }

    public String getNonBillablePercentage() {
        return nonBillablePercentage;
    }

    public void setNonBillablePercentage(String nonBillablePercentage) {
        this.nonBillablePercentage = nonBillablePercentage;
    }
}
