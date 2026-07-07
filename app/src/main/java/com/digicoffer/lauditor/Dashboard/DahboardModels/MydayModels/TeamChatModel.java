package com.digicoffer.lauditor.Dashboard.DahboardModels.MydayModels;

public class TeamChatModel {

    String time;
   private String tm_name;
   String tm_message;
   int count;

    public TeamChatModel(String time, String tm_name, String tm_message) {
        this.time = time;
        this.tm_name = tm_name;
        this.tm_message = tm_message;
        this.count = count;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTm_name() {
        return tm_name;
    }

    public void setTm_name(String tm_name) {
        this.tm_name = tm_name;
    }

    public String getTm_message() {
        return tm_message;
    }

    public void setTm_message(String tm_message) {
        this.tm_message = tm_message;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
