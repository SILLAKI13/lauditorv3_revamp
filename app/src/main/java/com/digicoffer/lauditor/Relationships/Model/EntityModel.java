package com.digicoffer.lauditor.Relationships.Model;

public class EntityModel {
    private String entityID;
    private String name;
    private String contactName;

//    @JsonProperty("entityId")
    public String getEntityID() { return entityID; }
//    @JsonProperty("entityId")
    public void setEntityID(String value) { this.entityID = value; }

//    @JsonProperty("name")
    public String getName() { return name; }
//    @JsonProperty("name")
    public void setName(String value) { this.name = value; }

//    @JsonProperty("contactName")
    public String getContactName() { return contactName; }
//    @JsonProperty("contactName")
    public void setContactName(String value) { this.contactName = value; }
}


