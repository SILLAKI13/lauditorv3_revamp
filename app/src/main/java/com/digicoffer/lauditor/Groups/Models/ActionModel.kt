package com.digicoffer.lauditor.Groups.Models

class ActionModel {
    var name: String = ""
    var isEnabled: Boolean = true

    constructor(name: String) {
        this.name = name
        this.isEnabled = true
    }

    constructor(name: String, isEnabled: Boolean) {
        this.name = name
        this.isEnabled = isEnabled
    }
}
