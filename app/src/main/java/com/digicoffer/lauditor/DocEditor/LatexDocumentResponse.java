package com.digicoffer.lauditor.DocEditor;

import java.util.Date;

public class LatexDocumentResponse {
    private String createdon;
    private String docid;
    private String document;
    private int page;
    private String pageid;
    private String updatedon;
    private String userid;

    // Getters and setters
    public String getCreatedon() {
        return createdon;
    }

    public void setCreatedon(String createdon) {
        this.createdon = createdon;
    }

    public String getDocid() {
        return docid;
    }

    public void setDocid(String docid) {
        this.docid = docid;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String getPageid() {
        return pageid;
    }

    public void setPageid(String pageid) {
        this.pageid = pageid;
    }

    public String getUpdatedon() {
        return updatedon;
    }

    public void setUpdatedon(String updatedon) {
        this.updatedon = updatedon;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public static class DateWrapper {
        private String $date;

        public String get$date() {
            return $date;
        }

        public void set$date(String $date) {
            this.$date = $date;
        }

//        public Date toJavaDate() {
//            try {
//                return javax.xml.bind.DatatypeConverter.parseDateTime($date).getTime();
//            } catch (Exception e) {
//                return null;
//            }
//        }
    }
}

