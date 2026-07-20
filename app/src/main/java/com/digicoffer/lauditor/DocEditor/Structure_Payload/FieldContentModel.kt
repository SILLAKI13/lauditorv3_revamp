package com.digicoffer.lauditor.DocEditor.Structure_Payload

import java.util.ArrayList

class FieldContentModel {
    @JvmField
    var fieldname: String? = null
    
    @JvmField
    var title: String? = null
    
    @JvmField
    var contentText: String? = null
    
    @JvmField
    var fileName: String? = null
    
    @JvmField
    var itemList: MutableList<ItemListModel>? = ArrayList()
    
    @JvmField
    var tableItemList: MutableList<MutableList<ItemListModel>>? = ArrayList()
}
