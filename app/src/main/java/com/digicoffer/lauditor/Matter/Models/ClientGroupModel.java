package com.digicoffer.lauditor.Matter.Models;

import java.util.List;

public class ClientGroupModel {
    private String id;
    private String name;
    private List<String> groups; // IDs of groups this client belongs to

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }
}
