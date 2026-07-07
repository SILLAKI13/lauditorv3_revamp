package com.digicoffer.lauditor.TimeSheets.Models;

public class ProjectTMModel {
    String billableHours;
    String name;
    String nonBillablehours;
    String total;

    public String getBillableHours() {
        return billableHours;
    }

    public void setBillableHours(String billableHours) {
        this.billableHours = billableHours;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNonBillablehours() {
        return nonBillablehours;
    }

    public void setNonBillablehours(String nonBillablehours) {
        this.nonBillablehours = nonBillablehours;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }
}
