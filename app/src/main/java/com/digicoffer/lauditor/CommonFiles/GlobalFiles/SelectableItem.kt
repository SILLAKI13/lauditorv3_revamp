package com.digicoffer.lauditor.CommonFiles.GlobalFiles

interface SelectableItem {
    fun getId(): String?
    fun getName(): String?
    fun isSelected(): Boolean
    fun setSelected(selected: Boolean)
}
