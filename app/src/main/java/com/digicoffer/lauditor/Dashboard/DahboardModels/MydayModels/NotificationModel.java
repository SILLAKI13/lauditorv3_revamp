package com.digicoffer.lauditor.Dashboard.DahboardModels.MydayModels;

public class NotificationModel {
    String timestamp;
    String message;
    String date;

    public NotificationModel(String timestamp, String message, String date) {
        this.timestamp = timestamp;
        this.message = message;
        this.date = date;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
