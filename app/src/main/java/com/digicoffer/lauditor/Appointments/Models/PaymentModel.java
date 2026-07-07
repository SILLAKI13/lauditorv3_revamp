package com.digicoffer.lauditor.Appointments.Models;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

public class PaymentModel implements Parcelable {
    private String status = "";
    private String amount_paid = "";
    private String currency = "";
    private String symbol = "";
    private String label = "";

    public PaymentModel() {
    }

    protected PaymentModel(Parcel in) {
        status = in.readString();
        amount_paid = in.readString();
        currency = in.readString();
        symbol = in.readString();
        label = in.readString();
    }

    public static final Creator<PaymentModel> CREATOR = new Creator<PaymentModel>() {
        @Override
        public PaymentModel createFromParcel(Parcel in) {
            return new PaymentModel(in);
        }

        @Override
        public PaymentModel[] newArray(int size) {
            return new PaymentModel[size];
        }
    };

    // Getters
    public String getStatus() {
        return status;
    }

    public String getAmount_paid() {
        return amount_paid;
    }

    public String getCurrency() {
        return currency;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getLabel() {
        return label;
    }

    // Setters
    public void setStatus(String status) {
        this.status = status;
    }

    public void setAmount_paid(String amount_paid) {
        this.amount_paid = amount_paid;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(status);
        dest.writeString(amount_paid);
        dest.writeString(currency);
        dest.writeString(symbol);
        dest.writeString(label);
    }
}
