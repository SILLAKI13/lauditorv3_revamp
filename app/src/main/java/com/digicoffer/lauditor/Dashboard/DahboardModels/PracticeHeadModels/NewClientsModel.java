package com.digicoffer.lauditor.Dashboard.DahboardModels.PracticeHeadModels;

public class NewClientsModel {
    private String corporateType;
    private int corporateCount;
    private String criminalType;
    private int criminalCount;

    public NewClientsModel(String corporateType, int corporateCount, String criminalType, int criminalCount) {
        this.corporateType = corporateType;
        this.corporateCount = corporateCount;
        this.criminalType = criminalType;
        this.criminalCount = criminalCount;
    }

    public String getCorporateType() {
        return corporateType;
    }

    public void setCorporateType(String corporateType) {
        this.corporateType = corporateType;
    }

    public int getCorporateCount() {
        return corporateCount;
    }

    public void setCorporateCount(int corporateCount) {
        this.corporateCount = corporateCount;
    }

    public String getCriminalType() {
        return criminalType;
    }

    public void setCriminalType(String criminalType) {
        this.criminalType = criminalType;
    }

    public int getCriminalCount() {
        return criminalCount;
    }

    public void setCriminalCount(int criminalCount) {
        this.criminalCount = criminalCount;
    }
}
