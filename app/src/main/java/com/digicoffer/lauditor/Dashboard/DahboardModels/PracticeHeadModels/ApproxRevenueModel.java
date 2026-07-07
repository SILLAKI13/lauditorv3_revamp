package com.digicoffer.lauditor.Dashboard.DahboardModels.PracticeHeadModels;

import org.json.JSONObject;

public class ApproxRevenueModel {
    String approxRevenue;
    String CurrencySymbol="";
    String CurrencyCode="";

    public ApproxRevenueModel(String approxRevenue,String CurrencySymbol, String CurrencyCode) {
        this.approxRevenue = approxRevenue;
        this.CurrencySymbol=CurrencySymbol;
        this.CurrencyCode=CurrencyCode;
    }

    public String getCurrencyCode() {
        return CurrencyCode;
    }

    public String getCurrencySymbol() {
        return CurrencySymbol;
    }

    public String getApproxRevenue() {
        return approxRevenue;
    }

    public void setApproxRevenue(String approxRevenue) {
        this.approxRevenue = approxRevenue;
    }
}
