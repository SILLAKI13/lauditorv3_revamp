package com.digicoffer.lauditor.DocEditor.Structure_Payload

import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants

object DocumentPayloadBuilder {

    @JvmStatic
    fun createLatexPayload(fields: List<FieldContentModel>, userId: String): String {
        val doc = StringBuilder()

        // 1. Document Preamble
        doc.append("\\documentclass{article}")
        doc.append("\\usepackage{graphicx}")
        doc.append("\\usepackage{tabularx}")
        doc.append("\\usepackage{hyperref}")
        doc.append("\\usepackage{float}") // ✅ Add this line
        doc.append("\\usepackage{geometry}")
        doc.append("\\geometry{a4paper,total={170mm,257mm},left=20mm,top=20mm,}<ltk>")

        // 2. Add Title and Author
        for (field in fields) {
            if (Constants.title == field.fieldname) {
                doc.append("\\title{").append(safe(field.contentText)).append("}<ltk>")
            } else if (Constants.author == field.fieldname) {
                doc.append("\\author{").append(safe(field.contentText)).append("}<ltk>")
            }
        }

        doc.append("\\date{}<ltk>")
        doc.append("\\begin{document}<ltk>")
        doc.append("\\maketitle<ltk>")

        // 3. Add Abstract (Overview)
        for (field in fields) {
            if (Constants.overview == field.fieldname) {
                val overview = safe(field.contentText)
                if (overview.isNotEmpty()) {
                    doc.append("\\abstract ").append(overview).append("<ltk>")
                }
            }
        }

        // 4. Add Other Components
        for (field in fields) {
            if (isMetaField(field.fieldname)) continue

            val content = loadDocTextPara(field, userId)
            if (content.isNotEmpty()) {
                if (!doc.toString().endsWith("<ltk>")) {
                    doc.append("<ltk>")
                }
                doc.append(content)
            }
        }
        if (doc.toString().endsWith("<ltk>")) {
            return doc.substring(0, doc.length - 5) // remove final <ltk>
        }
        return doc.toString()
    }

    @JvmStatic
    fun loadDocTextPara(field: FieldContentModel, userId: String): String {
        when (field.fieldname) {
            Constants.docsection -> {
                return "\\section{" + safeBraces(field.title) + "}" + safe(field.contentText) + "<ltk>"
            }
            Constants.subSection -> {
                return "\\subsection{" + safeBraces(field.title) + "}" + safe(field.contentText) + "<ltk>"
            }
            Constants.subSubSection -> {
                return "\\subsubsection{" + safeBraces(field.title) + "}" + safe(field.contentText) + "<ltk>"
            }
            Constants.paragraph -> {
                return "\\paragraph{" + safeBraces(field.title) + "}" + safe(field.contentText) + "<ltk>"
            }
            Constants.pageBreak -> {
                return "\\newpage<ltk>"
            }
            Constants.orderedNumList -> {
                if (field.itemList == null || field.itemList!!.isEmpty()) return ""
                val enumList = StringBuilder()
                enumList.append("\\begin{enumerate}\n")
                for (item in field.itemList!!) {
                    enumList.append("\\item ").append(safe(item.itemText)).append("\n")
                }
                enumList.append("\\end{enumerate}<ltk>")
                return enumList.toString()
            }
            Constants.unorderedBulltedList -> {
                if (field.itemList == null || field.itemList!!.isEmpty()) return ""
                val itemList = StringBuilder()
                itemList.append("\\begin{itemize}\n")
                for (item in field.itemList!!) {
                    itemList.append("\\item ").append(safe(item.itemText)).append("\n")
                }
                itemList.append("\\end{itemize}<ltk>")
                return itemList.toString()
            }
            Constants.image -> {
                if (field.fileName == null || field.fileName!!.trim { it <= ' ' }.isEmpty()) return ""
                val imgPath = "/home/ubuntu/latekeditor_api_python/uploads/" + userId + "/" + field.fileName
                val caption = if (hasValidTitle(field.title)) "\\caption{" + field.title!!.trim { it <= ' ' } + "}" else ""
                return ("\\begin{figure}[H] \\centering" +
                        "\\includegraphics[width=0.9\\textwidth,height=0.7\\textheight,keepaspectratio]{" + imgPath + "}" +
                        caption +
                        "\\end{figure}<ltk>")
            }
            Constants.table -> {
                if (field.tableItemList == null || field.tableItemList!!.isEmpty()) return ""

                val tableBuilder = StringBuilder()
                val colCount = field.tableItemList!![0].size

                tableBuilder.append("\\begin{center}\\begin{tabularx}{1.0\\textwidth} { ");
                for (i in 0 until colCount) {
                    tableBuilder.append("<thsep>X")
                }
                tableBuilder.append(" <thsep>} \\hline ")

                for (row in field.tableItemList!!) {
                    for (i in row.indices) {
                        tableBuilder.append(safe(row[i].itemText))
                        if (i < row.size - 1) tableBuilder.append("<csep>")
                    }
                    tableBuilder.append("<rsep> \\hline ")
                }

                tableBuilder.append(" \\end{tabularx} \\end{center}<ltk>")
                return tableBuilder.toString()
            }
            else -> return ""
        }
    }

    private fun safeBraces(input: String?): String {
        if (input == null || input.trim { it <= ' ' }.isEmpty() || "null".equals(input.trim { it <= ' ' }, ignoreCase = true)) {
            return ""
        }
        return input.trim { it <= ' ' }
    }

    private fun safe(input: String?): String {
        return input ?: ""
    }

    private fun hasValidTitle(title: String?): Boolean {
        return title != null && title != "Image" && title.trim { it <= ' ' }.isNotEmpty() && !"null".equals(title.trim { it <= ' ' }, ignoreCase = true)
    }

    private fun isMetaField(fieldname: String?): Boolean {
        return (Constants.title == fieldname
                || Constants.author == fieldname
                || Constants.overview == fieldname)
    }
}
