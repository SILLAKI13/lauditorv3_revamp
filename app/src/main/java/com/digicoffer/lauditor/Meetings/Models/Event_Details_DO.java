package com.digicoffer.lauditor.Meetings.Models;

import org.json.JSONArray;

import java.util.ArrayList;

public class Event_Details_DO {
    String title;
    String event_type;
    String description;
    JSONArray team_name;
    JSONArray consumer_external;
    JSONArray corporate;
    boolean is_linked_with_timesheet = false;
    String time;
    String id;
    String format;
    String offset;
    String dialin;
    String offset_location;
    String matter_name;
    String owner_name;
    String repeat_interval;
    String team_members;
    String clients;
    boolean isRecurring;
    boolean timesheet_added;
    String from_ts;
    String to_ts;
    boolean owner;
    String display_time;
    String entity_name;
    JSONArray attachments;
    ArrayList<String> doc_id;
    String date;
    boolean all_day;
    ArrayList<String> doc_type;
    JSONArray Tm_name;
    JSONArray notifications;
    String Converted_Start_time;
    String Converted_End_time;
    String tmid;
    String matter_id;
    String matter_type;
    String meeting_link;
    String timezone_location;
    String location;


    public String getEvent_type() {
        return event_type;
    }

    public void setEvent_type(String event_type) {
        this.event_type = event_type;
    }

    public JSONArray getCorporate() {
        return corporate;
    }

    public void setCorporate(JSONArray corporate) {
        this.corporate = corporate;
    }

    public boolean isTimesheet_added() {
        return timesheet_added;
    }

    public void setTimesheet_added(boolean timesheet_added) {
        this.timesheet_added = timesheet_added;
    }

    public boolean isIs_linked_with_timesheet() {
        return is_linked_with_timesheet;
    }

    public void setIs_linked_with_timesheet(boolean is_linked_with_timesheet) {
        this.is_linked_with_timesheet = is_linked_with_timesheet;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public JSONArray getConsumer_external() {
        return consumer_external;
    }

    public void setConsumer_external(JSONArray consumer_external) {
        this.consumer_external = consumer_external;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDialin() {
        return dialin;
    }

    public void setDialin(String dialin) {
        this.dialin = dialin;
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

    public String getMeeting_link() {
        return meeting_link;
    }

    public void setMeeting_link(String meeting_link) {
        this.meeting_link = meeting_link;
    }

    public String getTimezone_location() {
        return timezone_location;
    }

    public void setTimezone_location(String timezone_location) {
        this.timezone_location = timezone_location;
    }

    public String getOwner_name() {
        return owner_name;
    }

    public void setOwner_name(String owner_name) {
        this.owner_name = owner_name;
    }

    public String getMatter_name() {
        return matter_name;
    }

    public void setMatter_name(String matter_name) {
        this.matter_name = matter_name;
    }


    public boolean isOwner() {
        return owner;
    }

    public void setOwner(boolean owner) {
        this.owner = owner;
    }

    public String getOffset_location() {
        return offset_location;
    }

    public void setOffset_location(String offset_location) {
        this.offset_location = offset_location;
    }

    public String getOffset() {
        return offset;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getDisplay_time() {
        return display_time;
    }

    public void setDisplay_time(String display_time) {
        this.display_time = display_time;
    }


    public JSONArray getTeam_name() {
        return team_name;
    }

    public void setTeam_name(JSONArray team_name) {
        this.team_name = team_name;
    }


    public boolean isAll_day() {
        return all_day;
    }

    public void setAll_day(boolean all_day) {
        this.all_day = all_day;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }


    public JSONArray getAttachments() {
        return attachments;
    }

    public void setAttachments(JSONArray attachments) {
        this.attachments = attachments;
    }


    public ArrayList<String> getDoc_id() {
        return doc_id;
    }

    public void setDoc_id(ArrayList<String> doc_id) {
        this.doc_id = doc_id;
    }

    public ArrayList<String> getDoc_type() {
        return doc_type;
    }

    public void setDoc_type(ArrayList<String> doc_type) {
        this.doc_type = doc_type;
    }


    public JSONArray getNotifications() {
        return notifications;
    }

    public void setNotifications(JSONArray notifications) {
        this.notifications = notifications;
    }


    public JSONArray getTm_name() {
        return Tm_name;
    }

    public void setTm_name(JSONArray tm_name) {
        Tm_name = tm_name;
    }


    public String getConverted_End_time() {
        return Converted_End_time;
    }

    public void setConverted_End_time(String converted_End_time) {
        Converted_End_time = converted_End_time;
    }

    public String getConverted_Start_time() {
        return Converted_Start_time;
    }

    public void setConverted_Start_time(String converted_Start_time) {
        Converted_Start_time = converted_Start_time;
    }


    public String getEntity_name() {
        return entity_name;
    }

    public void setEntity_name(String entity_name) {
        this.entity_name = entity_name;
    }

    public String getTmid() {
        return tmid;
    }

    public void setTmid(String tmid) {
        this.tmid = tmid;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRepeat_interval() {
        return repeat_interval;
    }

    public void setRepeat_interval(String repeat_interval) {
        this.repeat_interval = repeat_interval;
    }


    public String getTeam_members() {
        return team_members;
    }

    public void setTeam_members(String team_members) {
        this.team_members = team_members;
    }

    public String getClients() {
        return clients;
    }

    public void setClients(String clients) {
        this.clients = clients;
    }


    public boolean isRecurring() {
        return isRecurring;
    }

    public void setRecurring(boolean recurring) {
        isRecurring = recurring;
    }

    public String getFrom_ts() {
        return from_ts;
    }

    public void setFrom_ts(String from_ts) {
        this.from_ts = from_ts;
    }

    public String getTo_ts() {
        return to_ts;
    }

    public void setTo_ts(String to_ts) {
        this.to_ts = to_ts;
    }


}
