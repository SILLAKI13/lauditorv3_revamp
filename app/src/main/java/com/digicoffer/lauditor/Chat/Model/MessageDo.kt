package com.digicoffer.lauditor.Chat.Model

class MessageDo {
    var message: String? = null
    var sender: User? = null
    var viewType: String? = null
    var createdAt: String? = null
    var stanzaId: String? = null

    @get:JvmName("isIscurrentchat")
    @set:JvmName("setIscurrentchat")
    var iscurrentchat: Boolean = false

    constructor()
}
