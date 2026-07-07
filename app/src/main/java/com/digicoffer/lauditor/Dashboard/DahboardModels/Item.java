package com.digicoffer.lauditor.Dashboard.DahboardModels;

public class Item {
    private int type;
    private Object object;
    private String Viewtype;

    public Item(int type, String viewtype, Object object) {
        this.type = type;
        this.Viewtype = viewtype;
        this.object = object;
    }

    public String getViewtype() {
        return Viewtype;
    }

    public void setViewtype(String viewtype) {
        Viewtype = viewtype;
    }

    public Item(int type, Object object) {
        this.type = type;
        this.object = object;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
