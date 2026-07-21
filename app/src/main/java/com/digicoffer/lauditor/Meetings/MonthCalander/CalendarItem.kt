package com.digicoffer.lauditor.Meetings.MonthCalander

import java.time.LocalDate

class CalendarItem {
    @JvmField
    val date: LocalDate?
    @JvmField
    val label: String?

    constructor(date: LocalDate?) {
        this.date = date
        this.label = null
    }

    constructor(label: String) {
        this.label = label
        this.date = null
    }

    fun isHeader(): Boolean {
        return label != null
    }
}
