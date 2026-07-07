package com.digicoffer.lauditor.Dashboard.DahboardModels;

public class UnreadCountModel {
    String fromjid;
    String tojid;

    public String getFromjid() {
        return fromjid;
    }

    public void setFromjid(String fromjid) {
        this.fromjid = fromjid;
    }

    public String getTojid() {
        return tojid;
    }

    public void setTojid(String tojid) {
        this.tojid = tojid;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    String count;
}
