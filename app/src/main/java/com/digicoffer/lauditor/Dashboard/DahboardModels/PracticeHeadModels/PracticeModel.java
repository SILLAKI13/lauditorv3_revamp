package com.digicoffer.lauditor.Dashboard.DahboardModels.PracticeHeadModels;

public class PracticeModel {
    int hours;
    int percentage;
    int submitted_members;

    public PracticeModel(int hours, int percentage, int submitted_members) {
        this.hours = hours;
        this.percentage = percentage;
        this.submitted_members = submitted_members;
    }

    public int getSubmitted_members() {
        return submitted_members;
    }

    public void setSubmitted_members(int submitted_members) {
        this.submitted_members = submitted_members;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public int getPercentage() {
        return percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }
}
