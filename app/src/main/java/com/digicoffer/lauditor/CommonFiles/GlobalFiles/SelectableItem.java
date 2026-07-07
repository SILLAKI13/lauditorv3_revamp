package com.digicoffer.lauditor.CommonFiles.GlobalFiles;


public interface SelectableItem {
    String getId();
    String getName();
    boolean isSelected();
    void setSelected(boolean selected);
}
