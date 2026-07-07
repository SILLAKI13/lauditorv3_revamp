package com.digicoffer.lauditor.Meetings.Models;

import org.json.JSONArray;

import java.util.ArrayList;

public class Events_Do {
    String Event_Name;
    String Converted_Start_time;
    String COnverted_End_time;
    String Event_id;
    String converted_date = "";
    String Event_description;
    String Event_repetetion;
    String Event_Notifications;
    String Event_clients;
    String Event_team;
    boolean is_linked_with_timesheet = false;
    boolean all_day;
    String description;
    String dialin;
    String event_type;
    JSONArray invitees_consumer_external;
    JSONArray invitees_external;
    JSONArray invitees_internal;
    boolean isrecurring;
    String location;
    String matter_id;
    String matter_type;
    String matter_name;
    String meeting_link;
    String notes;
    JSONArray notifications;
    boolean owner;
    String owner_name;
    String repeat_interval;
    String timezone_location;
    String timezone_offset;
    String title;
    JSONArray attachments;
    private boolean isExpanded = false;
    private String userRsvp = ""; // store Yes/No/Maybe

    public boolean isIs_linked_with_timesheet() {
        return is_linked_with_timesheet;
    }

    public void setIs_linked_with_timesheet(boolean is_linked_with_timesheet) {
        this.is_linked_with_timesheet = is_linked_with_timesheet;
    }

    // getters & setters
    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        this.isExpanded = expanded;
    }

    public String getUserRsvp() {
        return userRsvp;
    }

    public void setUserRsvp(String rsvp) {
        this.userRsvp = rsvp;
    }

    public String getTimezone_offset() {
        return timezone_offset;
    }

    public void setTimezone_offset(String timezone_offset) {
        this.timezone_offset = timezone_offset;
    }

    public JSONArray getNotifications() {
        return notifications;
    }

    public JSONArray getAttachments() {
        return attachments;
    }

    public void setAttachments(JSONArray attachments) {
        this.attachments = attachments;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDialin() {
        return dialin;
    }

    public void setDialin(String dialin) {
        this.dialin = dialin;
    }

    public String getEvent_type() {
        return event_type;
    }

    public void setEvent_type(String event_type) {
        this.event_type = event_type;
    }

    public JSONArray getInvitees_consumer_external() {
        return invitees_consumer_external;
    }

    public void setInvitees_consumer_external(JSONArray invitees_consumer_external) {
        this.invitees_consumer_external = invitees_consumer_external;
    }

    public String getConverted_date() {
        return converted_date;
    }

    public void setConverted_date(String converted_date) {
        this.converted_date = converted_date;
    }

    public JSONArray getInvitees_external() {
        return invitees_external;
    }

    public void setInvitees_external(JSONArray invitees_external) {
        this.invitees_external = invitees_external;
    }

    public JSONArray getInvitees_internal() {
        return invitees_internal;
    }

    public void setInvitees_internal(JSONArray invitees_internal) {
        this.invitees_internal = invitees_internal;
    }

    public boolean isIsrecurring() {
        return isrecurring;
    }

    public void setIsrecurring(boolean isrecurring) {
        this.isrecurring = isrecurring;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getMatter_id() {
        return matter_id;
    }

    public void setMatter_id(String matter_id) {
        this.matter_id = matter_id;
    }

    public String getMatter_type() {
        return matter_type;
    }

    public void setMatter_type(String matter_type) {
        this.matter_type = matter_type;
    }

    public String getMatter_name() {
        return matter_name;
    }

    public void setMatter_name(String matter_name) {
        this.matter_name = matter_name;
    }

    public String getMeeting_link() {
        return meeting_link;
    }

    public void setMeeting_link(String meeting_link) {
        this.meeting_link = meeting_link;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setNotifications(JSONArray notifications) {
        this.notifications = notifications;
    }

    public boolean isOwner() {
        return owner;
    }

    public void setOwner(boolean owner) {
        this.owner = owner;
    }

    public String getOwner_name() {
        return owner_name;
    }

    public void setOwner_name(String owner_name) {
        this.owner_name = owner_name;
    }

    public String getRepeat_interval() {
        return repeat_interval;
    }

    public void setRepeat_interval(String repeat_interval) {
        this.repeat_interval = repeat_interval;
    }

    public String getTimezone_location() {
        return timezone_location;
    }

    public void setTimezone_location(String timezone_location) {
        this.timezone_location = timezone_location;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isAll_day() {
        return all_day;
    }

    public void setAll_day(boolean all_day) {
        this.all_day = all_day;
    }

    //    ArrayList<String> notifications;
    ArrayList<String> Team_name;
    ArrayList<String> Tm_name;

    public ArrayList<String> getTeam_name() {
        return Team_name;
    }

    public void setTeam_name(ArrayList<String> team_name) {
        Team_name = team_name;
    }

    public ArrayList<String> getTm_name() {
        return Tm_name;
    }

    public void setTm_name(ArrayList<String> tm_name) {
        Tm_name = tm_name;
    }

//    public ArrayList<String> getNotifications() {
//        return notifications;
//    }

//    public void setNotifications(ArrayList<String> notifications) {
//        this.notifications = notifications;
//    }

    boolean isRecurring;
    ArrayList<String> attachment_name;

    public ArrayList<String> getAttachment_name() {
        return attachment_name;
    }

    public void setAttachment_name(ArrayList<String> attachment_name) {
        this.attachment_name = attachment_name;
    }

    public boolean isRecurring() {
        return isRecurring;
    }

    public void setRecurring(boolean recurring) {
        isRecurring = recurring;
    }
//    boolean isRecurring;
//
//    public boolean isRecurring() {
//        return isRecurring;
//    }
//
//    public void setRecurring(boolean recurring) {
//        isRecurring = recurring;
//    }

    public String getEvent_description() {
        return Event_description;
    }

    public void setEvent_description(String event_description) {
        Event_description = event_description;
    }

    public String getEvent_repetetion() {
        return Event_repetetion;
    }

    public void setEvent_repetetion(String event_repetetion) {
        Event_repetetion = event_repetetion;
    }

    public String getEvent_Notifications() {
        return Event_Notifications;
    }

    public void setEvent_Notifications(String event_Notifications) {
        Event_Notifications = event_Notifications;
    }

    public String getEvent_clients() {
        return Event_clients;
    }

    public void setEvent_clients(String event_clients) {
        Event_clients = event_clients;
    }

    public String getEvent_team() {
        return Event_team;
    }

    public void setEvent_team(String event_team) {
        Event_team = event_team;
    }

    public String getEvent_id() {
        return Event_id;
    }

    public void setEvent_id(String event_id) {
        Event_id = event_id;
    }

    public String getConverted_Start_time() {
        return Converted_Start_time;
    }

    public void setConverted_Start_time(String converted_Start_time) {
        Converted_Start_time = converted_Start_time;
    }

    public String getCOnverted_End_time() {
        return COnverted_End_time;
    }

    public void setCOnverted_End_time(String COnverted_End_time) {
        this.COnverted_End_time = COnverted_End_time;
    }

    public String getEvent_Name() {
        return Event_Name;
    }

    public void setEvent_Name(String event_Name) {
        Event_Name = event_Name;
    }

    String Event_start_time;
    String Event_end_time;

    public String getEvent_start_time() {
        return Event_start_time;
    }

    public void setEvent_start_time(String event_start_time) {
        Event_start_time = event_start_time;
    }

    public String getEvent_end_time() {
        return Event_end_time;
    }

    public void setEvent_end_time(String event_end_time) {
        Event_end_time = event_end_time;
    }


}
