package com.digicoffer.lauditor.Chat.Model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ClientRelationshipsDo {
    String adminName;
    String rel_id;
    boolean canAccept;
    String clientType;
    String client_id;
    String consent;
    String source = "relationship";
    private String lastMessage;
    private String lastMessageTime;
    int recyclerview_position;
    private int childCount;
    String created;
    JSONArray groups;
    String guid;
    String id;
    String unread_count;
    private boolean expanded;
    boolean isAccepted;
    boolean isClient;
    boolean isEditable;
    JSONArray matterList;
    String name;
    JSONArray Users;
    private long lastMessageTimestamp;
    ArrayList<ChildDO> childList = new ArrayList<>();
    private List<String> subItems;
    private String firmName;

    public long getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(long lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }


    public String getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(String lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public String getFirmName() {
        return firmName;
    }

    public void setFirmName(String firmName) {
        this.firmName = firmName;
    }


    public String getRel_id() {
        return rel_id;
    }

    public void setRel_id(String rel_id) {
        this.rel_id = rel_id;
    }

    public ArrayList<ChildDO> getChildList() {
        return childList;
    }

    public void setChildList(ArrayList<ChildDO> childList) {
        this.childList = childList;
    }


    public String getUnread_count() {
        return unread_count;
    }

    public void setUnread_count(String unread_count) {
        this.unread_count = unread_count;
    }

    public JSONArray getUsers() {
        return Users;
    }

    public void setUsers(JSONArray users) {
        Users = users;
    }

    public int getRecyclerview_position() {
        return recyclerview_position;
    }

    public void setRecyclerview_position(int recyclerview_position) {
        this.recyclerview_position = recyclerview_position;
    }

    public List<String> getSubItems() {
        return subItems;
    }

    public int getChildCount() {
        return childCount;
    }

    public void setChildCount(int childCount) {
        this.childCount = childCount;
    }

    public void setSubItems(List<String> subItems) {
        this.subItems = subItems;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public boolean isCanAccept() {
        return canAccept;
    }

    public void setCanAccept(boolean canAccept) {
        this.canAccept = canAccept;
    }

    public String getClientType() {
        return clientType;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getConsent() {
        return consent;
    }

    public void setConsent(String consent) {
        this.consent = consent;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public JSONArray getGroups() {
        return groups;
    }

    public void setGroups(JSONArray groups) {
        this.groups = groups;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    public void setAccepted(boolean accepted) {
        isAccepted = accepted;
    }

    public boolean isClient() {
        return isClient;
    }

    public void setClient(boolean client) {
        isClient = client;
    }

    public boolean isEditable() {
        return isEditable;
    }

    public void setEditable(boolean editable) {
        isEditable = editable;
    }

    public JSONArray getMatterList() {
        return matterList;
    }

    public void setMatterList(JSONArray matterList) {
        this.matterList = matterList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
