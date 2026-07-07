package com.digicoffer.lauditor.Dashboard.DahboardModels.PracticeHeadModels;

public class ActiveModel {
    String total_active;
    String legal_type;
    int legal_count;
    String general_type;
    int general_count;
    String total_closed;
    String closed_legal_type;
    int closed_legal_count;
    String closed_general_type;
    int closed_general_count;

    public ActiveModel(String total_active, String legal_type, int legal_count, String general_type, int general_count, String total_closed, String closed_legal_type, int closed_legal_count, String closed_general_type, int closed_general_count) {
        this.total_active = total_active;
        this.legal_type = legal_type;
        this.legal_count = legal_count;
        this.general_type = general_type;
        this.general_count = general_count;
        this.total_closed = total_closed;
        this.closed_legal_type = closed_legal_type;
        this.closed_legal_count = closed_legal_count;
        this.closed_general_type = closed_general_type;
        this.closed_general_count = closed_general_count;
    }

    public String getTotal_active() {
        return total_active;
    }

    public void setTotal_active(String total_active) {
        this.total_active = total_active;
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

    public String getTotal_closed() {
        return total_closed;
    }

    public void setTotal_closed(String total_closed) {
        this.total_closed = total_closed;
    }

    public String getClosed_legal_type() {
        return closed_legal_type;
    }

    public void setClosed_legal_type(String closed_legal_type) {
        this.closed_legal_type = closed_legal_type;
    }

    public int getClosed_legal_count() {
        return closed_legal_count;
    }

    public void setClosed_legal_count(int closed_legal_count) {
        this.closed_legal_count = closed_legal_count;
    }

    public String getClosed_general_type() {
        return closed_general_type;
    }

    public void setClosed_general_type(String closed_general_type) {
        this.closed_general_type = closed_general_type;
    }

    public int getClosed_general_count() {
        return closed_general_count;
    }

    public void setClosed_general_count(int closed_general_count) {
        this.closed_general_count = closed_general_count;
    }
}
