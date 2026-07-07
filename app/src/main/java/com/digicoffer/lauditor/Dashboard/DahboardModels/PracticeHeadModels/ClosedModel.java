package com.digicoffer.lauditor.Dashboard.DahboardModels.PracticeHeadModels;

public class ClosedModel {
    String total_closed;
    String legal_type;
    int legal_count;
    String general_type;
    int general_count;

    public ClosedModel(String total_closed, String legal_type, int legal_count, String general_type, int general_count) {
        this.total_closed = total_closed;
        this.legal_type = legal_type;
        this.legal_count = legal_count;
        this.general_type = general_type;
        this.general_count = general_count;
    }

    public String getTotal_closed() {
        return total_closed;
    }

    public void setTotal_closed(String total_closed) {
        this.total_closed = total_closed;
    }

    public String getLegal_type() {
        return legal_type;
    }

    public void setLegal_type(String legal_type) {
        this.legal_type = legal_type;
    }

    public int getLegal_count() {
        return legal_count;
    }

    public void setLegal_count(int legal_count) {
        this.legal_count = legal_count;
    }

    public String getGeneral_type() {
        return general_type;
    }

    public void setGeneral_type(String general_type) {
        this.general_type = general_type;
    }

    public int getGeneral_count() {
        return general_count;
    }

    public void setGeneral_count(int general_count) {
        this.general_count = general_count;
    }
}
