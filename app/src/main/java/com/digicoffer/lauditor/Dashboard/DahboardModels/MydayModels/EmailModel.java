package com.digicoffer.lauditor.Dashboard.DahboardModels.MydayModels;

public class EmailModel {
    String email_subject;
    String email_message;
    String email_time;
    int count;

    public EmailModel(String email_subject, String email_message, String email_time, int count) {
        this.email_subject = email_subject;
        this.email_message = email_message;
        this.email_time = email_time;
        this.count = count;
    }

    public String getEmail_subject() {
        return email_subject;
    }

    public void setEmail_subject(String email_subject) {
        this.email_subject = email_subject;
    }

    public String getEmail_message() {
        return email_message;
    }

    public void setEmail_message(String email_message) {
        this.email_message = email_message;
    }

    public String getEmail_time() {
        return email_time;
    }

    public void setEmail_time(String email_time) {
        this.email_time = email_time;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
