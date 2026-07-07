package com.digicoffer.lauditor.TimeSheets.Models;

import java.util.ArrayList;

public class WeekDateInfo {
    private String weekDateRange;
    private ArrayList<String> weekDates;

    public WeekDateInfo(String weekDateRange, ArrayList<String> weekDates) {
        this.weekDateRange = weekDateRange;
        this.weekDates = weekDates;
    }

    public String getWeekDateRange() {
        return weekDateRange;
    }

    public ArrayList<String> getWeekDates() {
        return weekDates;
    }
}
