package com.digicoffer.lauditor.Notifications.Models;

import org.json.JSONObject;

public class Navigation {
    private String route_name;
    private JSONObject params;   // You can parse specific params later

    public String getRoute_name() {
        return route_name;
    }

    public void setRoute_name(String route_name) {
        this.route_name = route_name;
    }

    public JSONObject getParams() {
        return params;
    }

    public void setParams(JSONObject params) {
        this.params = params;
    }
}

