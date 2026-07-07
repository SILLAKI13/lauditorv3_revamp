package com.digicoffer.lauditor.Dashboard.DahboardModels.PracticeHeadModels;

public class TimeSheetModel {
    String submitted_start_date;
    String submitted_end_date;
    String pending_start_date;
    String pending_end_date;

    public TimeSheetModel(String submitted_start_date, String submitted_end_date, String pending_start_date, String pending_end_date) {
        this.submitted_start_date = submitted_start_date;
        this.submitted_end_date = submitted_end_date;
        this.pending_start_date = pending_start_date;
        this.pending_end_date = pending_end_date;
    }

    public String getSubmitted_start_date() {
        return submitted_start_date;
    }

    public void setSubmitted_start_date(String submitted_start_date) {
        this.submitted_start_date = submitted_start_date;
    }

    public String getSubmitted_end_date() {
        return submitted_end_date;
    }

    public void setSubmitted_end_date(String submitted_end_date) {
        this.submitted_end_date = submitted_end_date;
    }

    public String getPending_start_date() {
        return pending_start_date;
    }

    public void setPending_start_date(String pending_start_date) {
        this.pending_start_date = pending_start_date;
    }

    public String getPending_end_date() {
        return pending_end_date;
    }

    public void setPending_end_date(String pending_end_date) {
        this.pending_end_date = pending_end_date;
    }
}
