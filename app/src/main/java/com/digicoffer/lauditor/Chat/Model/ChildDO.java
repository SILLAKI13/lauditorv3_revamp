package com.digicoffer.lauditor.Chat.Model;

import java.util.ArrayList;

public class ChildDO {
    String id;
    String name;
    String guid;
    String source = "relationship";
    int child_position;
    String client_id;
    String uid;
    String unread_count;
    String FirmName;
    private String lastMessage;
    private String lastMessageTime;
    private long lastMessageTimestamp;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public long getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(long lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
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

    public String getUnread_count() {
        return unread_count;
    }

    public String getFirmName() {
        return FirmName;
    }

    public void setFirmName(String firmName) {
        FirmName = firmName;
    }

    public void setUnread_count(String unread_count) {
        this.unread_count = unread_count;
    }


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public int getChild_position() {
        return child_position;
    }

    public void setChild_position(int child_position) {
        this.child_position = child_position;
    }

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

    public String getGuid() {
        return guid;
    }

    public String setGuid(String guid) {
        this.guid = guid;
        return guid;
    }
}
