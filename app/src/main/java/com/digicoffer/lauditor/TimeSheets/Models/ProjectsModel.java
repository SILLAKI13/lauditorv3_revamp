package com.digicoffer.lauditor.TimeSheets.Models;

import org.json.JSONArray;

public class ProjectsModel {
    String caseNo;
    JSONArray clientNames;
    String matterId;
    String projectName;
    JSONArray teamMembers;

    public String getCaseNo() {
        return caseNo;
    }

    public void setCaseNo(String caseNo) {
        this.caseNo = caseNo;
    }

    public JSONArray getClientNames() {
        return clientNames;
    }

    public void setClientNames(JSONArray clientNames) {
        this.clientNames = clientNames;
    }

    public String getMatterId() {
        return matterId;
    }

    public void setMatterId(String matterId) {
        this.matterId = matterId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public JSONArray getTeamMembers() {
        return teamMembers;
    }

    public void setTeamMembers(JSONArray teamMembers) {
        this.teamMembers = teamMembers;
    }
}
