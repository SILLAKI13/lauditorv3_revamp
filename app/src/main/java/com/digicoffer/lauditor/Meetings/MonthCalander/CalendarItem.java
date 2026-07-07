package com.digicoffer.lauditor.Meetings.MonthCalander;

import java.time.LocalDate;

public class CalendarItem {
    public final LocalDate date;
    public final String label;

    public CalendarItem(LocalDate date) {
        this.date = date;
        this.label = null;
    }

    public CalendarItem(String label) {
        this.label = label;
        this.date = null;
    }

    public boolean isHeader() {
        return label != null;
    }
}
