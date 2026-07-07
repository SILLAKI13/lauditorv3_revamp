package com.digicoffer.lauditor.Relationships.Model;

public class SearchModel {
    private boolean error;
    private String msg;
    private String consumerID;
    private String firstName;
    private String lastName;
    private String country;
    private String name;

    public boolean getError() { return error; }
    public void setError(boolean value) { this.error = value; }

    public String getMsg() { return msg; }
    public void setMsg(String value) { this.msg = value; }

    public String getConsumerID() { return consumerID; }
    public void setConsumerID(String value) { this.consumerID = value; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String value) { this.firstName = value; }

    public String getLastName() { return lastName; }
    public void setLastName(String value) { this.lastName = value; }

    public String getCountry() { return country; }
    public void setCountry(String value) { this.country = value; }

    public String getName() { return name; }
    public void setName(String value) { this.name = value; }
}


