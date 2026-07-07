package com.digicoffer.lauditor.FirmProfile;

import java.util.ArrayList;

public class CourtData {
    private String id;
    private String name;
    private String court_type;
    private String city;
    private String state;
    private ArrayList<String> jurisdiction;
    private ArrayList<String> benches;

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCourt_type() { return court_type; }
    public void setCourt_type(String court_type) { this.court_type = court_type; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public ArrayList<String> getJurisdiction() { return jurisdiction; }
    public void setJurisdiction(ArrayList<String> jurisdiction) { this.jurisdiction = jurisdiction; }
    public ArrayList<String> getBenches() { return benches; }
    public void setBenches(ArrayList<String> benches) { this.benches = benches; }
}
