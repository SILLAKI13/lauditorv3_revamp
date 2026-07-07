package com.digicoffer.lauditor.DocEditor.Structure_Payload;

import com.digicoffer.lauditor.CommonFiles.GlobalFiles.Constants;

import java.util.List;

public class DocumentPayloadBuilder {

    public static String createLatexPayload(List<FieldContentModel> fields, String userId) {
        StringBuilder doc = new StringBuilder();

        // 1. Document Preamble
        doc.append("\\documentclass{article}");
        doc.append("\\usepackage{graphicx}");
        doc.append("\\usepackage{tabularx}");
        doc.append("\\usepackage{hyperref}");
        doc.append("\\usepackage{float}");  // ✅ Add this line
        doc.append("\\usepackage{geometry}");
        doc.append("\\geometry{a4paper,total={170mm,257mm},left=20mm,top=20mm,}<ltk>");

        // 2. Add Title and Author
        for (FieldContentModel field : fields) {
            if (Constants.title.equals(field.fieldname)) {
                doc.append("\\title{").append(safe(field.contentText)).append("}<ltk>");
            } else if (Constants.author.equals(field.fieldname)) {
                doc.append("\\author{").append(safe(field.contentText)).append("}<ltk>");
            }
        }

        doc.append("\\date{}<ltk>");
        doc.append("\\begin{document}<ltk>");
        doc.append("\\maketitle<ltk>");

        // 3. Add Abstract (Overview)
        for (FieldContentModel field : fields) {
            if (Constants.overview.equals(field.fieldname)) {
                String overview = safe(field.contentText);
                if (!overview.isEmpty()) {
                    doc.append("\\abstract ").append(overview).append("<ltk>");
                }
            }
        }

        // 4. Add Other Components
        for (FieldContentModel field : fields) {
            if (isMetaField(field.fieldname)) continue;

            String content = loadDocTextPara(field, userId);
            if (!content.isEmpty()) {
                if (!doc.toString().endsWith("<ltk>")) {
                    doc.append("<ltk>");
                }
                doc.append(content);
            }
        }
        if (doc.toString().endsWith("<ltk>")) {
            return doc.substring(0, doc.length() - 5); // remove final <ltk>
        }
        return doc.toString();
    }

    //    public static String loadDocTextPara(FieldContentModel field, String userId) {
//        switch (field.fieldname) {
//            case Constants.docsection:
//                return hasValidTitle(field.title) ?
//                        "\\section{" + field.title.trim() + "}" + safe(field.contentText) + "<ltk>" : "";
//
//            case Constants.subSection:
//                return hasValidTitle(field.title) ?
//                        "\\subsection{" + field.title.trim() + "}" + safe(field.contentText) + "<ltk>" : "";
//
//            case Constants.subSubSection:
//                return hasValidTitle(field.title) ?
//                        "\\subsubsection{" + field.title.trim() + "}" + safe(field.contentText) + "<ltk>" : "";
//
//            case Constants.paragraph:
//                return safe(field.contentText).isEmpty() ? "" : safe(field.contentText) + "<ltk>";
//
//            case Constants.pageBreak:
//                return "\\newpage<ltk>";
//
//            case Constants.orderedNumList: {
//                if (field.itemList == null || field.itemList.isEmpty()) return "";
//                StringBuilder enumList = new StringBuilder();
//                enumList.append("\\begin{enumerate}\n");
//                for (ItemListModel item : field.itemList) {
//                    enumList.append("\\item ").append(safe(item.itemText)).append("\n");
//                }
//                enumList.append("\\end{enumerate}<ltk>");
//                return enumList.toString();
//            }
//
//            case Constants.unorderedBulltedList: {
//                if (field.itemList == null || field.itemList.isEmpty()) return "";
//                StringBuilder itemList = new StringBuilder();
//                itemList.append("\\begin{itemize}\n");
//                for (ItemListModel item : field.itemList) {
//                    itemList.append("\\item ").append(safe(item.itemText)).append("\n");
//                }
//                itemList.append("\\end{itemize}<ltk>");
//                return itemList.toString();
//            }
//
//            case Constants.image: {
//                if (field.fileName == null || field.fileName.trim().isEmpty()) return "";
//                String imgPath = "/home/ubuntu/latekeditor_api_python/uploads/" + userId + "/" + field.fileName;
//                String caption = hasValidTitle(field.title) ? "\\caption{" + field.title.trim() + "}" : "";
//                return "\\begin{figure}[h] \\centering" +
//                        "\\includegraphics[width=0.9\\textwidth]{" + imgPath + "}" +
//                        caption +
//                        "\\end{figure}<ltk>";
//            }
//
//            case Constants.table: {
//                if (field.tableItemList == null || field.tableItemList.isEmpty()) return "";
//
//                StringBuilder tableBuilder = new StringBuilder();
//                int colCount = field.tableItemList.get(0).size();
//
//                tableBuilder.append("\\begin{center}\\begin{tabularx}{1.0\\textwidth} { ");
//                for (int i = 0; i < colCount; i++) {
//                    tableBuilder.append("<thsep>X");
//                }
//                tableBuilder.append(" <thsep>} \\hline ");
//
//                for (List<ItemListModel> row : field.tableItemList) {
//                    for (int i = 0; i < row.size(); i++) {
//                        tableBuilder.append(safe(row.get(i).itemText));
//                        if (i < row.size() - 1) tableBuilder.append("<csep>");
//                    }
//                    tableBuilder.append("<rsep> \\hline ");
//                }
//
//                tableBuilder.append(" \\end{tabularx} \\end{center}<ltk>");
//                return tableBuilder.toString();
//            }
//
//            default:
//                return "";
//        }
//    }
    public static String loadDocTextPara(FieldContentModel field, String userId) {
        switch (field.fieldname) {
            case Constants.docsection:
                return "\\section{" + safeBraces(field.title) + "}" + safe(field.contentText) + "<ltk>";

            case Constants.subSection:
                return "\\subsection{" + safeBraces(field.title) + "}" + safe(field.contentText) + "<ltk>";

            case Constants.subSubSection:
                return "\\subsubsection{" + safeBraces(field.title) + "}" + safe(field.contentText) + "<ltk>";

            case Constants.paragraph:
                return "\\paragraph{" + safeBraces(field.title) + "}" + safe(field.contentText) + "<ltk>";

            case Constants.pageBreak:
                return "\\newpage<ltk>";

            case Constants.orderedNumList: {
                if (field.itemList == null || field.itemList.isEmpty()) return "";
                StringBuilder enumList = new StringBuilder();
                enumList.append("\\begin{enumerate}\n");
                for (ItemListModel item : field.itemList) {
                    enumList.append("\\item ").append(safe(item.itemText)).append("\n");
                }
                enumList.append("\\end{enumerate}<ltk>");
                return enumList.toString();
            }

            case Constants.unorderedBulltedList: {
                if (field.itemList == null || field.itemList.isEmpty()) return "";
                StringBuilder itemList = new StringBuilder();
                itemList.append("\\begin{itemize}\n");
                for (ItemListModel item : field.itemList) {
                    itemList.append("\\item ").append(safe(item.itemText)).append("\n");
                }
                itemList.append("\\end{itemize}<ltk>");
                return itemList.toString();
            }

            case Constants.image: {
                if (field.fileName == null || field.fileName.trim().isEmpty()) return "";
                String imgPath = "/home/ubuntu/latekeditor_api_python/uploads/" + userId + "/" + field.fileName;
                String caption = hasValidTitle(field.title) ? "\\caption{" + field.title.trim() + "}" : "";
                return "\\begin{figure}[H] \\centering" +
                        "\\includegraphics[width=0.9\\textwidth,height=0.7\\textheight,keepaspectratio]{" + imgPath + "}" +
                        caption +
                        "\\end{figure}<ltk>";
            }

            case Constants.table: {
                if (field.tableItemList == null || field.tableItemList.isEmpty()) return "";

                StringBuilder tableBuilder = new StringBuilder();
                int colCount = field.tableItemList.get(0).size();

                tableBuilder.append("\\begin{center}\\begin{tabularx}{1.0\\textwidth} { ");
                for (int i = 0; i < colCount; i++) {
                    tableBuilder.append("<thsep>X");
                }
                tableBuilder.append(" <thsep>} \\hline ");

                for (List<ItemListModel> row : field.tableItemList) {
                    for (int i = 0; i < row.size(); i++) {
                        tableBuilder.append(safe(row.get(i).itemText));
                        if (i < row.size() - 1) tableBuilder.append("<csep>");
                    }
                    tableBuilder.append("<rsep> \\hline ");
                }

                tableBuilder.append(" \\end{tabularx} \\end{center}<ltk>");
                return tableBuilder.toString();
            }

            default:
                return "";
        }
    }

    private static String safeBraces(String input) {
        if (input == null || input.trim().isEmpty() || "null".equalsIgnoreCase(input.trim())) {
            return "";
        }
        return input.trim();
    }

    private static String safe(String input) {
        return input == null ? "" : input;
    }

    private static boolean hasValidTitle(String title) {
        return title != null && !title.equals("Image") && !title.trim().isEmpty() && !"null".equalsIgnoreCase(title.trim());
    }

    private static boolean isMetaField(String fieldname) {
        return Constants.title.equals(fieldname)
                || Constants.author.equals(fieldname)
                || Constants.overview.equals(fieldname);
    }
}
