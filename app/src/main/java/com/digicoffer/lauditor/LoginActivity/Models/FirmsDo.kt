package com.digicoffer.lauditor.LoginActivity.Models

class FirmsDo {
    private var name: String? = null
    var value: String? = null

    fun getName(): String? {
        return name
    }

    fun setName(name: String?): String? {
        this.name = name
        return name
    }
}
