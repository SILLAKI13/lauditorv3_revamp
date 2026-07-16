package com.digicoffer.lauditor.Relationships.Model

class SearchModel {
    var error: Boolean = false
    var msg: String? = null

    @get:JvmName("getConsumerID")
    @set:JvmName("setConsumerID")
    var consumerID: String? = null

    var firstName: String? = null
    var lastName: String? = null
    var country: String? = null
    var name: String? = null
}
