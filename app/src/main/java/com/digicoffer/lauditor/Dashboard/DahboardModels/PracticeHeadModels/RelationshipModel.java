package com.digicoffer.lauditor.Dashboard.DahboardModels.PracticeHeadModels;

public class RelationshipModel {
    String accepted;
    String pending;

    public RelationshipModel(String accepted, String pending) {
        this.accepted = accepted;
        this.pending = pending;
    }

    public String getAccepted() {
        return accepted;
    }

    public void setAccepted(String accepted) {
        this.accepted = accepted;
    }

    public String getPending() {
        return pending;
    }

    public void setPending(String pending) {
        this.pending = pending;
    }
}
