package com.digicoffer.lauditor.Dashboard.DahboardModels.MydayModels;

public class ClientChatModel {
    String time;
   private String Client_name;
   String chat_message;
   int count;

    public ClientChatModel(String time, String client_name, String chat_message) {
        this.time = time;
        Client_name = client_name;
        this.chat_message = chat_message;
        this.count = count;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getClient_name() {
        return Client_name;
    }

    public void setClient_name(String client_name) {
        Client_name = client_name;
    }

    public String getChat_message() {
        return chat_message;
    }

    public void setChat_message(String chat_message) {
        this.chat_message = chat_message;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
