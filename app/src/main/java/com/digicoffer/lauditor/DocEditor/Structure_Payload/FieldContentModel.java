package com.digicoffer.lauditor.DocEditor.Structure_Payload;

import java.util.ArrayList;
import java.util.List;

public class FieldContentModel {
    public String fieldname;
    public String title;
    public String contentText;
    public String fileName;
    public List<ItemListModel> itemList = new ArrayList<>();
    public List<List<ItemListModel>> tableItemList = new ArrayList<>();
}

