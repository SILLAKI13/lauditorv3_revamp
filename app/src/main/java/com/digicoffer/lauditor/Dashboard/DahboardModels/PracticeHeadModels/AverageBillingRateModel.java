package com.digicoffer.lauditor.Dashboard.DahboardModels.PracticeHeadModels;

public class AverageBillingRateModel {
    String averageBillingRate;
    String CurrencySymbol="";
    String CurrencyCode="";
    public AverageBillingRateModel(String averageBillingRate,String CurrencySymbol, String CurrencyCode) {
        this.averageBillingRate = averageBillingRate;
        this.CurrencySymbol=CurrencySymbol;
        this.CurrencyCode=CurrencyCode;
    }

    public String getCurrencyCode() {
        return CurrencyCode;
    }

    public String getCurrencySymbol() {
        return CurrencySymbol;
    }

    public String getAverageBillingRate() {
        return averageBillingRate;
    }

    public void setAverageBillingRate(String averageBillingRate) {
        this.averageBillingRate = averageBillingRate;
    }
}
