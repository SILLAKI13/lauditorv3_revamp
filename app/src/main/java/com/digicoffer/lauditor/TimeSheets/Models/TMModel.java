package com.digicoffer.lauditor.TimeSheets.Models;

public class TMModel {
    String id;
    String name;
    String tb;
    String tnb;
    String total;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTb() {
        return tb;
    }

    public void setTb(String tb) {
        this.tb = tb;
    }

    public String getTnb() {
        return tnb;
    }

    public void setTnb(String tnb) {
        this.tnb = tnb;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }
}
