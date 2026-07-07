package com.digicoffer.lauditor.Email;

public class Header {
    String name;
    String value;
    String Headers;
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name= name;
    }
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value= value;
    }
    public String getHeaders(){
        return Headers;

    }
    public void setHeaders(String headers){
        this.Headers = Headers;
    }

}
