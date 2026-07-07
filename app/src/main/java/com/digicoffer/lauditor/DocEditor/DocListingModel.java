package com.digicoffer.lauditor.DocEditor;

import org.json.JSONObject;

public class DocListingModel {
    String docid;
    String documentname;
    String $date;
    JSONObject updatedon;

    public String get$date() {
        return $date;
    }

    public void set$date(String $date) {
        this.$date = $date;
    }

    public String getDocid() {
        return docid;
    }

    public void setDocid(String docid) {
        this.docid = docid;
    }

    public String getDocumentname() {
        return documentname;
    }

    public void setDocumentname(String documentname) {
        this.documentname = documentname;
    }

    public JSONObject getUpdatedon() {
        return updatedon;
    }

    public void setUpdatedon(JSONObject updatedon) {
        this.updatedon = updatedon;
    }
}
