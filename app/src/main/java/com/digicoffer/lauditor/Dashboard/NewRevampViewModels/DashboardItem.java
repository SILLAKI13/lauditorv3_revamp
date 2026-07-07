package com.digicoffer.lauditor.Dashboard.NewRevampViewModels;

import com.digicoffer.lauditor.Notifications.Models.Navigation;

public class DashboardItem {

    public static final int TYPE_MEETING           = 0;
    public static final int TYPE_APPOINTMENT       = 1;
    public static final int TYPE_MESSAGES          = 2;
    public static final int TYPE_NOTIFICATION      = 3;
    public static final int TYPE_REVENUE_TREND     = 4;
    public static final int TYPE_APPOINTMENT_TREND = 5;
    public static final int TYPE_MATTER            = 6;
    public static final int TYPE_STORAGE           = 7;
    public static final int TYPE_BILLABLE          = 8;
    public static final int TYPE_APPROX_REVENUE    = 9;
    public static final int TYPE_SUBSCRIPTION      = 10;
    public static final int TYPE_HIRING            = 11;
    public static final int TYPE_APPOINTMENT_REVENUE_TREND = 15; // or next available number

    private final int        type;
    private final Object     data;
    private       Navigation navigation;   // ← NEW

    public DashboardItem(int type, Object data) {
        this.type = type;
        this.data = data;
    }

    public int        getType()       { return type; }
    public Object     getData()       { return data; }

    // ── navigation ────────────────────────────────────────────────────
    public Navigation getNavigation() { return navigation; }
    public void setNavigation(Navigation navigation) { this.navigation = navigation; }
}