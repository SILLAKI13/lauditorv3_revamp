package com.digicoffer.lauditor.Dashboard.NewRevampViewModels

import java.util.ArrayList

class DashboardSection(val sectionId: Int, val title: String) {
    val items: MutableList<DashboardItem> = ArrayList()

    fun cardCount(): Int = items.size

    companion object {
        const val SECTION_TODAY = 0
        const val SECTION_ANALYTICS = 1
        const val SECTION_METRICS = 2
    }
}
