package com.digicoffer.lauditor.Email

class Header {
    var name: String? = null
    var value: String? = null

    @get:JvmName("getHeaders")
    @set:JvmName("setHeaders")
    var Headers: String? = null
}
