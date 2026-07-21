package com.digicoffer.lauditor.Dashboard.DahboardModels

class Item {
    var type: Int = 0
    var `object`: Any? = null
    var viewtype: String? = null

    constructor(type: Int, viewtype: String?, `object`: Any?) {
        this.type = type
        this.viewtype = viewtype
        this.`object` = `object`
    }

    constructor(type: Int, `object`: Any?) {
        this.type = type
        this.`object` = `object`
    }
}
