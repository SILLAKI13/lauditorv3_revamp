package com.digicoffer.lauditor.Matter.Models;

public class ClientModel {
    String client_id;
    String client_name;
    String client_type;
    Boolean send_request;

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getClient_name() {
        return client_name;
    }

    public void setClient_name(String client_name) {
        this.client_name = client_name;
    }

    public String getClient_type() {
        return client_type;
    }

    public void setClient_type(String client_type) {
        this.client_type = client_type;
    }

    public Boolean getSend_request() {
        return send_request;
    }

    public void setSend_request(Boolean send_request) {
        this.send_request = send_request;
    }
}
