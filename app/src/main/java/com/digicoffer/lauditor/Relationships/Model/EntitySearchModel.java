package com.digicoffer.lauditor.Relationships.Model;

public class EntitySearchModel {
    private String entityName;
    private String contactPerson;
    private String email;
    private String country;
    private String contactPhone;

//    @JsonProperty("entityName")
    public String getEntityName() { return entityName; }
//    @JsonProperty("entityName")
    public void setEntityName(String value) { this.entityName = value; }

//    @JsonProperty("contactPerson")
    public String getContactPerson() { return contactPerson; }
//    @JsonProperty("contactPerson")
    public void setContactPerson(String value) { this.contactPerson = value; }

//    @JsonProperty("email")
    public String getEmail() { return email; }
//    @JsonProperty("email")
    public void setEmail(String value) { this.email = value; }

//    @JsonProperty("country")
    public String getCountry() { return country; }
//    @JsonProperty("country")
    public void setCountry(String value) { this.country = value; }

//    @JsonProperty("contactPhone")
    public String getContactPhone() { return contactPhone; }
//    @JsonProperty("contactPhone")
    public void setContactPhone(String value) { this.contactPhone = value; }
}


