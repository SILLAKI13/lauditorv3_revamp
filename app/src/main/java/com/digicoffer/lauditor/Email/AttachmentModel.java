package com.digicoffer.lauditor.Email;

import java.util.List;

public class AttachmentModel {
    String partId;
    String mimeType;
    String filename;
    int Size;
    String Id;

    public int getSize() {
        return Size;
    }

    public void setSize(int size) {
        Size = size;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getContentType() {
        return ContentType;
    }

    public void setContentType(String contentType) {
        ContentType = contentType;
    }

    String ContentType;
    // Add more fields if needed to represent attachment data

    public String getPartId() {
        return partId;
    }

    public void setPartId(String partId) {
        this.partId = partId;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setHeaders(List<Header> headers) {
    }

    public void setBody(Body body) {
    }
}
