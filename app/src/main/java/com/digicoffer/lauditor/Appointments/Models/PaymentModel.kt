package com.digicoffer.lauditor.Appointments.Models

import android.os.Parcel
import android.os.Parcelable

class PaymentModel : Parcelable {
    var status: String = ""
    var amount_paid: String = ""
    var currency: String = ""
    var symbol: String = ""
    var label: String = ""

    constructor()

    protected constructor(parcel: Parcel) {
        status = parcel.readString() ?: ""
        amount_paid = parcel.readString() ?: ""
        currency = parcel.readString() ?: ""
        symbol = parcel.readString() ?: ""
        label = parcel.readString() ?: ""
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(status)
        parcel.writeString(amount_paid)
        parcel.writeString(currency)
        parcel.writeString(symbol)
        parcel.writeString(label)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PaymentModel> {
        override fun createFromParcel(parcel: Parcel): PaymentModel {
            return PaymentModel(parcel)
        }

        override fun newArray(size: Int): Array<PaymentModel?> {
            return arrayOfNulls(size)
        }
    }
}
