package com.digicoffer.lauditor.Chat.Model;

import org.json.JSONArray;
import org.json.JSONObject;

public class ChatDo {
    String GroupName;
    String name;
    JSONArray userList;

    public String getWithUser() {
        return WithUser;
    }

    public void setWithUser(String withUser) {
        WithUser = withUser;
    }

    String WithUser;

    public String getCount() {
        return Count;
    }

    public void setCount(String count) {
        Count = count;
    }

    String Count;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }



    public JSONArray getUserList() {
        return userList;
    }

    public void setUserList(JSONArray userList) {
        this.userList = userList;
    }

    String guid;

    public String getGroupName() {
        return GroupName;
    }

    public void setGroupName(String groupName) {
        GroupName = groupName;
    }

    String created;
    String RelationshipConsent;
    String ClientType;
    String Groups;
    String Documents;
    String RelType;
    String first_name;
    String last_name;
    String Coffer_id;

    public String getCoffer_id() {
        return Coffer_id;
    }

    public void setCoffer_id(String coffer_id) {
        Coffer_id = coffer_id;
    }
    String Client;
    String relationship_id;
    String id;
    String bizType;

    boolean isClient;
    boolean isAccepted;
    JSONObject documents;
    JSONObject viewDocuments;
    String uid;

    public void setDocuments(JSONObject documents) {
        this.documents = documents;
    }

    public String getBizType() {
        return bizType;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public String getClient() {
        return Client;
    }

    public void setClient(String client) {
        Client = client;
    }

    public void setRelType(String relType) {
        RelType = relType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRelationship_id() {
        return relationship_id;
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    public void setIsAccepted(boolean accepted) {
        isAccepted = accepted;
    }


    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getRelationshipConsent() {
        return RelationshipConsent;
    }

    public String getClientType() {
        return ClientType;
    }

    public void setClientType(String clientType) {
        ClientType = clientType;
    }

    public String getGroups() {
        return Groups;
    }

    public void setGroups(String groups) {
        Groups = groups;
    }

    public String getDocuments() {
        return Documents;
    }

    public void setDocuments(String documents) {
        Documents = documents;
    }

    public boolean isClient() {
        return isClient;
    }

    public void setIsClient(boolean client) {
        isClient = client;
    }

    public String getUID() {
        return uid;
    }

    public void setUID(String uid) {
        this.uid = uid;
    }
}
