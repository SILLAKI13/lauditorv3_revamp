package com.digicoffer.lauditor.Dashboard.DahboardModels.MydayModels

class ClientChatModel(
    var time: String?,
    @get:JvmName("getClient_name") @set:JvmName("setClient_name") var client_name: String?,
    var chat_message: String?
) {
    var count: Int = 0
}
