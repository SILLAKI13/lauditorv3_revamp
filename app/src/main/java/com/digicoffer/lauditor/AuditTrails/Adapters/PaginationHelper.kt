package com.digicoffer.lauditor.AuditTrails.Adapters

import kotlin.math.ceil

object PaginationHelper {
    @JvmStatic
    fun calculateTotalNoOfPages(itemcount: Int, itemsperpage: Int): Int {
        return ceil(itemcount.toDouble() / itemsperpage.toDouble()).toInt()
    }

    @JvmStatic
    fun startIndexForCurrentPage(CurrentPageNo: Int, itemsperpage: Int): Int {
        return (CurrentPageNo - 1) * itemsperpage
    }

    @JvmStatic
    fun endIndexForCurrentPage(StartIndex: Int, ItemCount: Int, itemsperpage: Int): Int {
        return Math.min(StartIndex + itemsperpage, ItemCount)
    }
}
