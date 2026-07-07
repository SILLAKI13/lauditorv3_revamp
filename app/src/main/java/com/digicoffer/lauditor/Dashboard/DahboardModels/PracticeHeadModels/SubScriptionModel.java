package com.digicoffer.lauditor.Dashboard.DahboardModels.PracticeHeadModels;

public class SubScriptionModel {
    boolean active_pay_button;
    boolean is_active_sub;
    boolean is_paid_sub;
    String user_allowed;
    String message;
    String month;
    String email;

//    public SubScriptionModel(boolean active_pay_button, boolean is_active_sub, boolean is_paid_sub, String email, String user_allowed, String message, String month) {
//        this.active_pay_button = active_pay_button;
//        this.is_active_sub = is_active_sub;
//        this.is_paid_sub = is_paid_sub;
//    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUser_allowed() {
        return user_allowed;
    }

    public void setUser_allowed(String user_allowed) {
        this.user_allowed = user_allowed;
    }

    public boolean isActive_pay_button() {
        return active_pay_button;
    }

    public void setActive_pay_button(boolean active_pay_button) {
        this.active_pay_button = active_pay_button;
    }

    public boolean isIs_active_sub() {
        return is_active_sub;
    }

    public void setIs_active_sub(boolean is_active_sub) {
        this.is_active_sub = is_active_sub;
    }

    public boolean isIs_paid_sub() {
        return is_paid_sub;
    }

    public void setIs_paid_sub(boolean is_paid_sub) {
        this.is_paid_sub = is_paid_sub;
    }
}
