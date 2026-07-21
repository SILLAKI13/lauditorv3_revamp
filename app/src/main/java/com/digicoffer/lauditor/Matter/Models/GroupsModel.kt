package com.digicoffer.lauditor.Matter.Models

class GroupsModel {
    var group_id: String? = null
    var group_name: String? = null

    @get:JvmName("isSelected")
    @set:JvmName("setSelected")
    var isSelected: Boolean = false

    @get:JvmName("isChecked")
    @set:JvmName("setChecked")
    var isChecked: Boolean = false

    @get:JvmName("isIsenabled")
    @set:JvmName("setIsenabled")
    var isenabled: Boolean = false

    var clientGroupModelList: List<ClientGroupModel>? = null

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val that = o as GroupsModel

        return if (group_id != null) group_id == that.group_id else that.group_id == null
    }

    override fun hashCode(): Int {
        return group_id?.hashCode() ?: 0
    }
}
