package com.digicoffer.lauditor.CommonFiles.PdfUtils;

public class File_Content_Type {

    public static boolean getContType(String docType) {
        switch (docType) {
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document": // docx
            case "application/msword": // doc
            case "application/pdf": // pdf
            case "application/vnd.ms-excel": // xls
            case "application/vnd.ms-powerpoint": // ppt
            case "application/vnd.openxmlformats-officedocument.presentationml.presentation": // pptx
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet": // xlsx
            case "text/csv":
            case "application/rtf":
            case "text/rtf":
                return true; // treat as "document" type
            case "image/png":
            case "image/gif":
            case "image/jpg":
            case "image/jpeg":
                return false; // treat as image
            default:
                return true;
        }
    }

    public static boolean isExcelFile(String docType) {
        if (docType == null) return false;
        switch (docType) {
            case "application/vnd.ms-excel": // .xls
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet": // .xlsx
                return true;
            default:
                return false;
        }
    }

    // ✅ New method to check PDF
    public static boolean isPDF(String docType) {
        return "application/pdf".equalsIgnoreCase(docType);
    }

    // ✅ Optional: new method to check if Image
    public static boolean isImage(String docType) {
        if (docType == null) return false;
        switch (docType.toLowerCase()) {
            case "image/png":
            case "image/jpeg":
            case "image/jpg":
            case "image/gif":
            case "image/webp":
            case "image/svg+xml":
                return true;
            default:
                return false;
        }
    }
}
