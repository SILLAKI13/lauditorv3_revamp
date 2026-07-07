package com.digicoffer.lauditor.Dashboard.NewRevampViewModels;

import java.util.ArrayList;
import java.util.List;

/**
 * One horizontally-scrolling section row.
 * Badge count = number of cards in THIS section only (items.size()).
 */
public class DashboardSection {

    public static final int SECTION_TODAY     = 0;
    public static final int SECTION_ANALYTICS = 1;
    public static final int SECTION_METRICS   = 2;

    public final int                 sectionId;
    public final String              title;
    public final List<DashboardItem> items;

    public DashboardSection(int sectionId, String title) {
        this.sectionId = sectionId;
        this.title     = title;
        this.items     = new ArrayList<>();
    }

    /**
     * Returns the count of cards present in this section.
     * This is what is shown in the section header badge (e.g., "4").
     */
    public int cardCount() {
        return items.size();
    }
}