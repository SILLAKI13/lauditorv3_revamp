package com.digicoffer.lauditor.Relationships.Model;

import org.json.JSONArray;
import org.json.JSONObject;

public class ProfileDo {
    String first_name;
    String last_name;
    String contact_person;
    String contact_phone;
    String house_flat_no;
    String street;
    String city_town;
    String state;
    String zipcode;
    String website;
    JSONObject address;

    public JSONObject getAddress() {
        return address;
    }
    public String getWebsite(){
        return website;
    }
    public void setWebsite(String website)
    {
        this.website = website;
    }

    public void setAddress(JSONObject address) {
        this.address = address;
    }

    public String getContact_phone() {
        return contact_phone;
    }

    public void setContact_phone(String contact_phone) {
        this.contact_phone = contact_phone;
    }

    public String getHouse_flat_no() {
        return house_flat_no;
    }

    public void setHouse_flat_no(String house_flat_no) {
        this.house_flat_no = house_flat_no;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity_town() {
        return city_town;
    }

    public void setCity_town(String city_town) {
        this.city_town = city_town;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getContact_person() {
        return contact_person;
    }

    public void setContact_person(String contact_person) {
        this.contact_person = contact_person;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    String middle_name;
    String dob;
    String mobile;
    String fullname;
    String uid;

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    String email;
    JSONArray citizen;
    String index;
    String country;
    String affiliation_type;
    String work_address;
    String home_address;
    String work_phone;
    String alt_phone;
    String joined;

    public String getAffiliation_type() {
        return affiliation_type;
    }

    public void setAffiliation_type(String affiliation_type) {
        this.affiliation_type = affiliation_type;
    }

    public String getWork_address() {
        return work_address;
    }

    public void setWork_address(String work_address) {
        this.work_address = work_address;
    }

    public String getHome_address() {
        return home_address;
    }

    public void setHome_address(String home_address) {
        this.home_address = home_address;
    }

    public String getWork_phone() {
        return work_phone;
    }

    public void setWork_phone(String work_phone) {
        this.work_phone = work_phone;
    }

    public String getAlt_phone() {
        return alt_phone;
    }

    public void setAlt_phone(String alt_phone) {
        this.alt_phone = alt_phone;
    }

    public String getJoined() {
        return joined;
    }

    public void setJoined(String joined) {
        this.joined = joined;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getMiddle_name() {
        return middle_name;
    }

    public void setMiddle_name(String middle_name) {
        this.middle_name = middle_name;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public JSONArray getCitizen() {
        return citizen;
    }

    public void setCitizen(JSONArray citizen) {
        this.citizen = citizen;
    }
}
