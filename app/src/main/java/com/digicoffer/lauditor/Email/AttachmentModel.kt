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

    @get:JvmName("getSize")
    @set:JvmName("setSize")
    var Size: Int = 0

    @get:JvmName("getId")
    @set:JvmName("setId")
    var Id: String? = null

    @get:JvmName("getContentType")
    @set:JvmName("setContentType")
    var ContentType: String? = null

    fun setHeaders(headers: List<Header>?) {}
    fun setBody(body: Body?) {}
}
