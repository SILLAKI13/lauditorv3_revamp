package com.digicoffer.lauditor.Meetings.Models;

import java.util.ArrayList;

public class Appointment_Do {

    private String id;
    private ArrayList<String> services_offered;

    private String client_id;
    private String client_name;

    private String appointment_from;
    private String appointment_to;

    private String consultation_mode;
    private String appointment_status;

    private String meeting_link;
    private String rsvp_status;

    private String created_at;

    private Payment_Do payment;

    // ---------------- GETTERS & SETTERS ---------------- //

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<String> getServices_offered() {
        return services_offered;
    }

    public void setServices_offered(ArrayList<String> services_offered) {
        this.services_offered = services_offered;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getClient_name() {
        return client_name;
    }

    public void setClient_name(String client_name) {
        this.client_name = client_name;
    }

    public String getAppointment_from() {
        return appointment_from;
    }

    public void setAppointment_from(String appointment_from) {
        this.appointment_from = appointment_from;
    }

    public String getAppointment_to() {
        return appointment_to;
    }

    public void setAppointment_to(String appointment_to) {
        this.appointment_to = appointment_to;
    }

    public String getConsultation_mode() {
        return consultation_mode;
    }

    public void setConsultation_mode(String consultation_mode) {
        this.consultation_mode = consultation_mode;
    }

    public String getAppointment_status() {
        return appointment_status;
    }

    public void setAppointment_status(String appointment_status) {
        this.appointment_status = appointment_status;
    }

    public String getMeeting_link() {
        return meeting_link;
    }

    public void setMeeting_link(String meeting_link) {
        this.meeting_link = meeting_link;
    }

    public String getRsvp_status() {
        return rsvp_status;
    }

    public void setRsvp_status(String rsvp_status) {
        this.rsvp_status = rsvp_status;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public Payment_Do getPayment() {
        return payment;
    }

    public void setPayment(Payment_Do payment) {
        this.payment = payment;
    }
}

