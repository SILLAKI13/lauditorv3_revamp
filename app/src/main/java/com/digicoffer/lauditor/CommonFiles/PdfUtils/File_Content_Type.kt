package com.digicoffer.lauditor.CommonFiles.PdfUtils

class File_Content_Type {

    companion object {
        @JvmStatic
        fun getContType(docType: String?): Boolean {
            if (docType == null) return true
            return when (docType) {
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/msword",
                "application/pdf",
                "application/vnd.ms-excel",
                "application/vnd.ms-powerpoint",
                "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "text/csv",
                "application/rtf",
                "text/rtf" -> true
                "image/png",
                "image/gif",
                "image/jpg",
                "image/jpeg" -> false
                else -> true
            }
        }

        @JvmStatic
        fun isExcelFile(docType: String?): Boolean {
            if (docType == null) return false
            return when (docType) {
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> true
                else -> false
            }
        }

        @JvmStatic
        fun isPDF(docType: String?): Boolean {
            return "application/pdf".equals(docType, ignoreCase = true)
        }

        @JvmStatic
        fun isImage(docType: String?): Boolean {
            if (docType == null) return false
            return when (docType.lowercase()) {
                "image/png",
                "image/jpeg",
                "image/jpg",
                "image/gif",
                "image/webp",
                "image/svg+xml" -> true
                else -> false
            }
        }
    }
}
