package com.digicoffer.lauditor.Email

class AttachmentModel {
    @JvmField
    var partId: String? = null
    fun getPartId(): String? = partId
    fun setPartId(partId: String?) {
        this.partId = partId
    }

    @JvmField
    var mimeType: String? = null
    fun getMimeType(): String? = mimeType
    fun setMimeType(mimeType: String?) {
        this.mimeType = mimeType
    }

    @JvmField
    var filename: String? = null
    fun getFilename(): String? = filename
    fun setFilename(filename: String?) {
        this.filename = filename
    }

    @JvmField
    var size: Int = 0
    fun getSize(): Int = size
    fun setSize(size: Int) {
        this.size = size
    }

    @JvmField
    var id: String? = null
    fun getId(): String? = id
    fun setId(id: String?) {
        this.id = id
    }

    @JvmField
    var contentType: String? = null
    fun getContentType(): String? = contentType
    fun setContentType(contentType: String?) {
        this.contentType = contentType
    }

    fun setHeaders(headers: List<Header>?) {}
    fun setBody(body: Body?) {}
}
