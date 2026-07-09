package com.digicoffer.lauditor.Appointments.Models

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONArray
import java.util.ArrayList

class AppointmentModel : Parcelable {
    var id: String = ""
    var services_offered: JSONArray = JSONArray()
    var client_id: String = ""
    var guid: String = ""
    var client_profile_pic: String = ""
    var client_name: String = ""
    var appointment_from: String = ""
    var appointment_to: String = ""
    var consultation_mode: String = ""
    var appointment_status: String = ""
    var meeting_room_id: String = ""
    var meeting_room_expires_at: String = ""
    var rsvp_status: String = ""
    var created_at: String = ""
    var payment: PaymentModel? = PaymentModel()
    var notes: JSONArray = JSONArray()
    var appointmentHistory: ArrayList<AppointmentModel> = ArrayList()

    // Additional fields for UI state
    var isSelected: Boolean = false
    var isChecked: Boolean = false
    var isNoteExpanded: Boolean = false
    var tempNote: String = ""
    var isEditingNote: Boolean = false
    var editingNoteId: String = ""

    constructor() {
        payment = PaymentModel()
    }

    protected constructor(parcel: Parcel) {
        id = parcel.readString() ?: ""
        client_id = parcel.readString() ?: ""
        guid = parcel.readString() ?: ""
        client_name = parcel.readString() ?: ""
        appointment_from = parcel.readString() ?: ""
        appointment_to = parcel.readString() ?: ""
        consultation_mode = parcel.readString() ?: ""
        appointment_status = parcel.readString() ?: ""
        meeting_room_id = parcel.readString() ?: ""
        meeting_room_expires_at = parcel.readString() ?: ""
        rsvp_status = parcel.readString() ?: ""
        created_at = parcel.readString() ?: ""
        payment = parcel.readParcelable(PaymentModel::class.java.classLoader)
        isSelected = parcel.readByte() != 0.toByte()
        isChecked = parcel.readByte() != 0.toByte()
        isNoteExpanded = parcel.readByte() != 0.toByte()
        tempNote = parcel.readString() ?: ""
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(client_id)
        dest.writeString(client_name)
        dest.writeString(appointment_from)
        dest.writeString(appointment_to)
        dest.writeString(consultation_mode)
        dest.writeString(appointment_status)
        dest.writeString(meeting_room_id)
        dest.writeString(meeting_room_expires_at)
        dest.writeString(rsvp_status)
        dest.writeString(created_at)
        dest.writeParcelable(payment, flags)
        dest.writeByte(if (isSelected) 1.toByte() else 0.toByte())
        dest.writeByte(if (isChecked) 1.toByte() else 0.toByte())
        dest.writeByte(if (isNoteExpanded) 1.toByte() else 0.toByte())
        dest.writeString(tempNote)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AppointmentModel> {
        override fun createFromParcel(parcel: Parcel): AppointmentModel {
            return AppointmentModel(parcel)
        }

        override fun newArray(size: Int): Array<AppointmentModel?> {
            return arrayOfNulls(size)
        }
    }
}
