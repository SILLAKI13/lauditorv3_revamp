package com.digicoffer.lauditor.Matter.Models;

public class ClientsModel {
    String client_name;
    String client_id;
    String client_type;
    String rel_id;
    Boolean send_request;
    private boolean isSelected;
    boolean isChecked;
    boolean isenabled;

    public Boolean getSend_request() {
        return send_request;
    }

    public void setSend_request(Boolean send_request) {
        this.send_request = send_request;
    }

    public String getRel_id() {
        return rel_id;
    }

    public void setRel_id(String rel_id) {
        this.rel_id = rel_id;
    }

    public String getClient_name() {
        return client_name;
    }

    public void setClient_name(String client_name) {
        this.client_name = client_name;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getClient_type() {
        return client_type;
    }

    public void setClient_type(String client_type) {
        this.client_type = client_type;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public boolean isIsenabled() {
        return isenabled;
    }

    public void setIsenabled(boolean isenabled) {
        this.isenabled = isenabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClientsModel that = (ClientsModel) o;

        return client_id != null ? client_id.equals(that.client_id) : that.client_id == null;
    }

    @Override
    public int hashCode() {
        return client_id != null ? client_id.hashCode() : 0;
    }
}
