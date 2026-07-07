package com.digicoffer.lauditor.Dashboard.DahboardModels.MydayModels;

public class RelationshipRequestModel {

    String client_name;
    String time;
    int count;

    public RelationshipRequestModel(String client_name, String time, int count) {
        this.client_name = client_name;
        this.time = time;
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getClient_name() {
        return client_name;
    }

    public void setClient_name(String client_name) {
        this.client_name = client_name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
