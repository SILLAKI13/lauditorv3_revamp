package com.digicoffer.lauditor.Groups.Models

class GroupModel {
    var name: String = ""
    var id: String = ""
    var isSelected: Boolean = false
    var isChecked: Boolean = false

    @get:JvmName("isIsenabled")
    @set:JvmName("setIsenabled")
    var isenabled: Boolean = false
}
