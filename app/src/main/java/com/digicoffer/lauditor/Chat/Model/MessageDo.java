package com.digicoffer.lauditor.Chat.Model;

public class MessageDo {
    String message;
    User sender;
    String viewType;
    String createdAt;
    String stanzaId;
    boolean iscurrentchat ;

    public boolean isIscurrentchat() {
        return iscurrentchat;
    }

    public void setIscurrentchat(boolean iscurrentchat) {
        this.iscurrentchat = iscurrentchat;
    }


    public MessageDo() {

    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    public void setStanzaId(String stanzaId){
        this.stanzaId = stanzaId;
    }
    public String getStanzaId(){
        return stanzaId;
    }

    public String getViewType() {
        return viewType;
    }

    public void setViewType(String viewType) {
        this.viewType = viewType;
    }
}
