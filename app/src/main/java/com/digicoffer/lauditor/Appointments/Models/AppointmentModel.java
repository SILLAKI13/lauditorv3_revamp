package com.digicoffer.lauditor.Appointments.Models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.json.JSONArray;

import java.util.ArrayList;

public class AppointmentModel implements Parcelable {
    private String id = "";
    private JSONArray services_offered = new JSONArray();
    private String client_id = "";
    private String guid = "";
    private String client_profile_pic="";
    private String client_name = "";
    private String appointment_from = "";
    private String appointment_to = "";
    private String consultation_mode = "";
    private String appointment_status = "";
    private String meeting_room_id = "";
    private String meeting_room_expires_at = "";
    private String rsvp_status = "";
    private String created_at = "";
    private PaymentModel payment;
    private JSONArray notes = new JSONArray();
    private ArrayList<AppointmentModel> appointmentHistory = new ArrayList<>();

    // Additional fields for UI state
    private boolean isSelected = false;
    private boolean isChecked = false;
    private boolean isNoteExpanded = false;
    private String tempNote = "";
    private boolean isEditingNote = false;
    private String editingNoteId = "";

    public AppointmentModel() {
        payment = new PaymentModel();
    }

    protected AppointmentModel(Parcel in) {
        id = in.readString();
        client_id = in.readString();
        guid = in.readString();
        client_name = in.readString();
        appointment_from = in.readString();
        appointment_to = in.readString();
        consultation_mode = in.readString();
        appointment_status = in.readString();
        meeting_room_id = in.readString();
        meeting_room_expires_at = in.readString();
        rsvp_status = in.readString();
        created_at = in.readString();
        payment = in.readParcelable(PaymentModel.class.getClassLoader());
        isSelected = in.readByte() != 0;
        isChecked = in.readByte() != 0;
        isNoteExpanded = in.readByte() != 0;
        tempNote = in.readString();
    }

    public static final Creator<AppointmentModel> CREATOR = new Creator<AppointmentModel>() {
        @Override
        public AppointmentModel createFromParcel(Parcel in) {
            return new AppointmentModel(in);
        }

        @Override
        public AppointmentModel[] newArray(int size) {
            return new AppointmentModel[size];
        }
    };

    // Getters
    public String getClient_profile_pic() {
        return client_profile_pic;
    }

    public void setClient_profile_pic(String client_profile_pic) {
        this.client_profile_pic = client_profile_pic;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getId() {
        return id;
    }

    public JSONArray getServices_offered() {
        return services_offered;
    }

    public String getClient_id() {
        return client_id;
    }

    public String getClient_name() {
        return client_name;
    }

    public String getAppointment_from() {
        return appointment_from;
    }

    public String getAppointment_to() {
        return appointment_to;
    }

    public String getConsultation_mode() {
        return consultation_mode;
    }

    public String getAppointment_status() {
        return appointment_status;
    }

    public String getMeeting_room_id() {
        return meeting_room_id;
    }

    public String getMeeting_room_expires_at() {
        return meeting_room_expires_at;
    }

    public String getRsvp_status() {
        return rsvp_status;
    }

    public String getCreated_at() {
        return created_at;
    }

    public PaymentModel getPayment() {
        return payment;
    }

    public JSONArray getNotes() {
        return notes;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public boolean isNoteExpanded() {
        return isNoteExpanded;
    }

    public String getTempNote() {
        return tempNote;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setServices_offered(JSONArray services_offered) {
        this.services_offered = services_offered;
    }

    public boolean isEditingNote() {
        return isEditingNote;
    }

    public void setEditingNote(boolean editingNote) {
        isEditingNote = editingNote;
    }

    public String getEditingNoteId() {
        return editingNoteId;
    }

    public void setEditingNoteId(String editingNoteId) {
        this.editingNoteId = editingNoteId;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public void setClient_name(String client_name) {
        this.client_name = client_name;
    }

    public void setAppointment_from(String appointment_from) {
        this.appointment_from = appointment_from;
    }

    public void setAppointment_to(String appointment_to) {
        this.appointment_to = appointment_to;
    }

    public void setConsultation_mode(String consultation_mode) {
        this.consultation_mode = consultation_mode;
    }

    public void setAppointment_status(String appointment_status) {
        this.appointment_status = appointment_status;
    }

    public void setMeeting_room_id(String meeting_room_id) {
        this.meeting_room_id = meeting_room_id;
    }

    public void setMeeting_room_expires_at(String meeting_room_expires_at) {
        this.meeting_room_expires_at = meeting_room_expires_at;
    }

    public void setRsvp_status(String rsvp_status) {
        this.rsvp_status = rsvp_status;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public void setPayment(PaymentModel payment) {
        this.payment = payment;
    }

    public void setNotes(JSONArray notes) {
        this.notes = notes;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public void setNoteExpanded(boolean noteExpanded) {
        isNoteExpanded = noteExpanded;
    }

    public void setTempNote(String tempNote) {
        this.tempNote = tempNote;
    }

    public ArrayList<AppointmentModel> getAppointmentHistory() {
        return appointmentHistory;
    }

    public void setAppointmentHistory(ArrayList<AppointmentModel> appointmentHistory) {
        this.appointmentHistory = appointmentHistory;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(client_id);
        dest.writeString(client_name);
        dest.writeString(appointment_from);
        dest.writeString(appointment_to);
        dest.writeString(consultation_mode);
        dest.writeString(appointment_status);
        dest.writeString(meeting_room_id);
        dest.writeString(meeting_room_expires_at);
        dest.writeString(rsvp_status);
        dest.writeString(created_at);
        dest.writeParcelable(payment, flags);
        dest.writeByte((byte) (isSelected ? 1 : 0));
        dest.writeByte((byte) (isChecked ? 1 : 0));
        dest.writeByte((byte) (isNoteExpanded ? 1 : 0));
        dest.writeString(tempNote);
    }
}