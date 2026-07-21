package com.digicoffer.lauditor.Matter.Models

class ClientsModel {
    var client_name: String? = null
    var client_id: String? = null
    var client_type: String? = null
    var rel_id: String? = null
    var send_request: Boolean? = null

    @get:JvmName("isSelected")
    @set:JvmName("setSelected")
    var isSelected: Boolean = false

    @get:JvmName("isChecked")
    @set:JvmName("setChecked")
    var isChecked: Boolean = false

    @get:JvmName("isIsenabled")
    @set:JvmName("setIsenabled")
    var isenabled: Boolean = false

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val that = o as ClientsModel

        return if (client_id != null) client_id == that.client_id else that.client_id == null
    }

    override fun hashCode(): Int {
        return client_id?.hashCode() ?: 0
    }
}
