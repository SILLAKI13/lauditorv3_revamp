package com.digicoffer.lauditor.DocEditor.Structure_Payload

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.digicoffer.lauditor.R
import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants
import com.google.android.material.textfield.TextInputEditText
import java.util.ArrayList

object LaTeXUtils {

    @JvmStatic
    fun collectFieldContentsFromLayout(context: Context?, ll_create_document: LinearLayout): List<FieldContentModel> {
        val fields: MutableList<FieldContentModel> = ArrayList()
        val staticViewsCount = 5

        for (i in staticViewsCount until ll_create_document.childCount) {
            val view = ll_create_document.getChildAt(i)
            val tvLabel = view.findViewById<TextView>(R.id.tv_content) ?: continue

            val userLabel = tvLabel.text.toString().trim { it <= ' ' } // Modified by user
            val tagObj = tvLabel.tag // Default base label

            if (tagObj == null) continue
            val baseLabel = tagObj.toString().trim { it <= ' ' } // e.g., "Section"

            val fieldname = mapLabelToField(baseLabel)
            if (fieldname.isEmpty()) continue

            // ========== TABLE ============
            if ("Table".equals(baseLabel, ignoreCase = true) && view.findViewById<View>(R.id.ll_add_more) != null) {
                val tableContainer = view.findViewById<LinearLayout>(R.id.ll_add_more)
                val tableModel = FieldContentModel()
                tableModel.fieldname = Constants.table
                tableModel.title = userLabel // Keep user-modified label
                tableModel.tableItemList = ArrayList()

                for (j in 0 until tableContainer.childCount) {
                    val rowView = tableContainer.getChildAt(j)
                    if (rowView is LinearLayout) {
                        val rowLayout = rowView
                        val row: MutableList<ItemListModel> = ArrayList()

                        for (k in 0 until rowLayout.childCount) {
                            val cell = rowLayout.getChildAt(k)
                            if (cell is TextInputEditText) {
                                val cellText = cell.text.toString().trim { it <= ' ' }
                                row.add(ItemListModel(cellText))
                            }
                        }

                        if (row.isNotEmpty()) {
                            tableModel.tableItemList!!.add(row)
                        }
                    }
                }

                fields.add(tableModel)
                continue
            }

            // ========== IMAGE ============
            if ("Image".equals(baseLabel, ignoreCase = true) && view.findViewById<View>(R.id.tv_image_name) != null) {
                val imageModel = FieldContentModel()
                imageModel.fieldname = Constants.image
                imageModel.title = userLabel

                val tvImageName = view.findViewById<TextView>(R.id.tv_image_name)
                if (tvImageName?.text != null) {
                    imageModel.fileName = tvImageName.text.toString().trim { it <= ' ' }
                }

                fields.add(imageModel)
                continue
            }

            // ========== LISTS ============
            if ((baseLabel.equals("Numbered List", ignoreCase = true) || baseLabel.equals("Bulleted List", ignoreCase = true))
                && view.findViewById<View>(R.id.ll_add_content) != null
            ) {
                val listModel = FieldContentModel()
                listModel.itemList = ArrayList()
                listModel.fieldname = fieldname
                listModel.title = if (shouldIncludeTitle(userLabel, fieldname)) userLabel else null

                val listContainer = view.findViewById<LinearLayout>(R.id.ll_add_content)
                for (j in 0 until listContainer.childCount) {
                    val itemView = listContainer.getChildAt(j)
                    if (itemView != null) {
                        val etItem = itemView.findViewById<TextInputEditText>(R.id.et_list_item)
                        if (etItem != null) {
                            val itemText = etItem.text.toString().trim { it <= ' ' }
                            if (itemText.isNotEmpty()) {
                                listModel.itemList!!.add(ItemListModel(itemText))
                            }
                        }
                    }
                }

                fields.add(listModel)
                continue
            }

            // ========== TEXTUAL ELEMENTS ============
            val etContent = view.findViewById<TextInputEditText>(R.id.et_content)
            val model = FieldContentModel()
            model.fieldname = fieldname
            model.title = if (shouldIncludeTitle(userLabel, fieldname)) userLabel else null

            if (etContent?.text != null) {
                model.contentText = etContent.text.toString().trim { it <= ' ' }
            }

            fields.add(model)
        }

        return fields
    }

    @JvmStatic
    fun mapLabelToField(label: String): String {
        return when (label) {
            "Overview" -> Constants.overview
            "Section" -> Constants.docsection
            "Sub Section" -> Constants.subSection
            "Sub Sub Section" -> Constants.subSubSection
            "Paragraph" -> Constants.paragraph
            "Numbered List" -> Constants.orderedNumList
            "Bulleted List" -> Constants.unorderedBulltedList
            "Page Break" -> Constants.pageBreak
            "Image" -> Constants.image
            "Table" -> Constants.table
            else -> ""
        }
    }

    private fun shouldIncludeTitle(userLabel: String?, fieldname: String?): Boolean {
        if (userLabel == null) return false
        val normalizedLabel = userLabel.trim { it <= ' ' }.lowercase()

        return when (fieldname) {
            Constants.overview -> normalizedLabel != "overview"
            Constants.docsection -> normalizedLabel != "section"
            Constants.subSection -> normalizedLabel != "sub section"
            Constants.subSubSection -> normalizedLabel != "sub sub section"
            Constants.paragraph -> normalizedLabel != "paragraph"
            Constants.orderedNumList -> normalizedLabel != "numbered list"
            Constants.unorderedBulltedList -> normalizedLabel != "bulleted list"
            Constants.pageBreak -> normalizedLabel != "page break"
            Constants.image -> normalizedLabel != "image"
            Constants.table -> normalizedLabel != "table"
            else -> true
        }
    }
}
