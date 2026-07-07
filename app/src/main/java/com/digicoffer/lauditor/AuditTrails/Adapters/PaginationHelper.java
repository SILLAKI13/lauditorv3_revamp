package com.digicoffer.lauditor.AuditTrails.Adapters;

public class PaginationHelper {
    public static int calculateTotalNoOfPages(int itemcount, int itemsperpage) {

        return (int) Math.ceil(((double) itemcount) / (double) (itemsperpage));
    }

    public static int startIndexForCurrentPage(int CurrentPageNo, int itemsperpage) {
        int StartIndex = (CurrentPageNo - 1) * itemsperpage;
        return StartIndex;
    }

    public static int endIndexForCurrentPage(int StartIndex, int ItemCount, int itemsperpage) {
        int EndIndex = Math.min(StartIndex + itemsperpage, ItemCount);
        return EndIndex;
    }

}