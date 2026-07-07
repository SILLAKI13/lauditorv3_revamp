package com.digicoffer.lauditor.TimeSheets.Models;

import org.json.JSONObject;

public class TimeSheetModel {
    String currentWeek;
    String nextWeek;
    String prevWeek;
    boolean isFrozen;
    JSONObject headers;

    public JSONObject getHeaders() {
        return headers;
    }

    public void setHeaders(JSONObject headers) {
        this.headers = headers;
    }

    String Fri;
    String Mon;
    String Sat;
    String Sun;
    String Thu;
    String Tue;
    String Wed;

    public String getFri() {
        return Fri;
    }

    public void setFri(String fri) {
        Fri = fri;
    }

    public String getMon() {
        return Mon;
    }

    public void setMon(String mon) {
        Mon = mon;
    }

    public String getSat() {
        return Sat;
    }

    public void setSat(String sat) {
        Sat = sat;
    }

    public String getSun() {
        return Sun;
    }

    public void setSun(String sun) {
        Sun = sun;
    }

    public String getThu() {
        return Thu;
    }

    public void setThu(String thu) {
        Thu = thu;
    }

    public String getTue() {
        return Tue;
    }

    public void setTue(String tue) {
        Tue = tue;
    }

    public String getWed() {
        return Wed;
    }

    public void setWed(String wed) {
        Wed = wed;
    }

    public String getCurrentWeek() {
        return currentWeek;
    }

    public void setCurrentWeek(String currentWeek) {
        this.currentWeek = currentWeek;
    }

    public String getNextWeek() {
        return nextWeek;
    }

    public void setNextWeek(String nextWeek) {
        this.nextWeek = nextWeek;
    }

    public String getPrevWeek() {
        return prevWeek;
    }

    public void setPrevWeek(String prevWeek) {
        this.prevWeek = prevWeek;
    }

    public boolean isFrozen() {
        return isFrozen;
    }

    public void setFrozen(boolean frozen) {
        isFrozen = frozen;
    }
}
