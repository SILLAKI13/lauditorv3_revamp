package com.digicoffer.lauditor.DocEditor.Structure_Payload;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.digicoffer.lauditor.R;
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class LaTeXUtils {

    public static List<FieldContentModel> collectFieldContentsFromLayout(Context context, LinearLayout ll_create_document) {
        List<FieldContentModel> fields = new ArrayList<>();
        int staticViewsCount = 5;

        for (int i = staticViewsCount; i < ll_create_document.getChildCount(); i++) {
            View view = ll_create_document.getChildAt(i);
            TextView tvLabel = view.findViewById(R.id.tv_content);
            if (tvLabel == null) continue;

            String userLabel = tvLabel.getText().toString().trim(); // Modified by user
            Object tagObj = tvLabel.getTag(); // Default base label

            if (tagObj == null) continue;
            String baseLabel = tagObj.toString().trim(); // e.g., "Section"

            String fieldname = mapLabelToField(baseLabel);
            if (fieldname == null || fieldname.isEmpty()) continue;

            // ========== TABLE ============
            if ("Table".equalsIgnoreCase(baseLabel) && view.findViewById(R.id.ll_add_more) != null) {
                LinearLayout tableContainer = view.findViewById(R.id.ll_add_more);
                FieldContentModel tableModel = new FieldContentModel();
                tableModel.fieldname = Constants.table;
                tableModel.title = userLabel; // Keep user-modified label
                tableModel.tableItemList = new ArrayList<>();

                for (int j = 0; j < tableContainer.getChildCount(); j++) {
                    View rowView = tableContainer.getChildAt(j);
                    if (rowView instanceof LinearLayout) {
                        LinearLayout rowLayout = (LinearLayout) rowView;
                        List<ItemListModel> row = new ArrayList<>();

                        for (int k = 0; k < rowLayout.getChildCount(); k++) {
                            View cell = rowLayout.getChildAt(k);
                            if (cell instanceof TextInputEditText) {
                                String cellText = ((TextInputEditText) cell).getText().toString().trim();
                                row.add(new ItemListModel(cellText));
                            }
                        }

                        if (!row.isEmpty()) {
                            tableModel.tableItemList.add(row);
                        }
                    }
                }

                fields.add(tableModel);
                continue;
            }

            // ========== IMAGE ============
            if ("Image".equalsIgnoreCase(baseLabel) && view.findViewById(R.id.tv_image_name) != null) {
                FieldContentModel imageModel = new FieldContentModel();
                imageModel.fieldname = Constants.image;
                imageModel.title = userLabel;

                TextView tvImageName = view.findViewById(R.id.tv_image_name);
                if (tvImageName != null && tvImageName.getText() != null) {
                    imageModel.fileName = tvImageName.getText().toString().trim();
                }

                fields.add(imageModel);
                continue;
            }

            // ========== LISTS ============
            if ((baseLabel.equalsIgnoreCase("Numbered List") || baseLabel.equalsIgnoreCase("Bulleted List"))
                    && view.findViewById(R.id.ll_add_content) != null) {

                FieldContentModel listModel = new FieldContentModel();
                listModel.itemList = new ArrayList<>();
                listModel.fieldname = fieldname;
                listModel.title = shouldIncludeTitle(userLabel, fieldname) ? userLabel : null;

//                TextInputEditText etMainContent = view.findViewById(R.id.et_content);
//                if (etMainContent != null && etMainContent.getText() != null) {
//                    String header = etMainContent.getText().toString().trim();
//                    if (!header.isEmpty()) {
//                        listModel.itemList.add(new ItemListModel(header));
//                    }
//                }

                LinearLayout listContainer = view.findViewById(R.id.ll_add_content);
                for (int j = 0; j < listContainer.getChildCount(); j++) {
                    View itemView = listContainer.getChildAt(j);
                    if (itemView != null) {
                        TextInputEditText etItem = itemView.findViewById(R.id.et_list_item);
                        if (etItem != null) {
                            String itemText = etItem.getText().toString().trim();
                            if (!itemText.isEmpty()) {
                                listModel.itemList.add(new ItemListModel(itemText));
                            }
                        }
                    }
                }

                fields.add(listModel);
                continue;
            }

            // ========== TEXTUAL ELEMENTS ============
            TextInputEditText etContent = view.findViewById(R.id.et_content);
            FieldContentModel model = new FieldContentModel();
            model.fieldname = fieldname;
            model.title = shouldIncludeTitle(userLabel, fieldname) ? userLabel : null;

            if (etContent != null && etContent.getText() != null) {
                model.contentText = etContent.getText().toString().trim();
            }

            fields.add(model);
        }

        return fields;
    }

    public static String mapLabelToField(String label) {
        switch (label) {
            case "Overview":
                return Constants.overview;
            case "Section":
                return Constants.docsection;
            case "Sub Section":
                return Constants.subSection;
            case "Sub Sub Section":
                return Constants.subSubSection;
            case "Paragraph":
                return Constants.paragraph;
            case "Numbered List":
                return Constants.orderedNumList;
            case "Bulleted List":
                return Constants.unorderedBulltedList;
            case "Page Break":
                return Constants.pageBreak;
            case "Image":
                return Constants.image;
            case "Table":
                return Constants.table;
            default:
                return "";
        }
    }

    private static boolean shouldIncludeTitle(String userLabel, String fieldname) {
        if (userLabel == null) return false;
        userLabel = userLabel.trim().toLowerCase(); // normalize

        switch (fieldname) {
            case Constants.overview:
                return !userLabel.equals("overview");
            case Constants.docsection:
                return !userLabel.equals("section");
            case Constants.subSection:
                return !userLabel.equals("sub section");
            case Constants.subSubSection:
                return !userLabel.equals("sub sub section");
            case Constants.paragraph:
                return !userLabel.equals("paragraph");
            case Constants.orderedNumList:
                return !userLabel.equals("numbered list");
            case Constants.unorderedBulltedList:
                return !userLabel.equals("bulleted list");
            case Constants.pageBreak:
                return !userLabel.equals("page break");
            case Constants.image:
                return !userLabel.equals("image");
            case Constants.table:
                return !userLabel.equals("table");
            default:
                return true;
        }
    }

}
