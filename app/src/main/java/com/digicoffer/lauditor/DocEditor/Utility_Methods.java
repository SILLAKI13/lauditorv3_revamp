package com.digicoffer.lauditor.DocEditor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utility_Methods {

    //    public static String extractBetween(String text, String start, String end) {
//        int startIndex = text.indexOf(start);
//        int endIndex = text.indexOf(end, startIndex + start.length());
//        if (startIndex != -1 && endIndex != -1) {
//            return text.substring(startIndex + start.length(), endIndex).trim();
//        }
//        return "";
//    }
    public static String extractBetween(String text, String start, String end) {
        int startIndex = text.indexOf(start);
        if (startIndex == -1) return "";

        // Move to the first '{' after start marker
        startIndex = text.indexOf("{", startIndex);
        if (startIndex == -1) return "";

        int endIndex = text.indexOf(end, startIndex + 1);
        if (endIndex == -1) return "";

        return text.substring(startIndex + 1, endIndex).trim();
    }


    public static List<String> extractListItems(String block) {
        List<String> items = new ArrayList<>();

        // Remove LaTeX list wrappers
        block = block.replace("\\begin{enumerate}", "")
                .replace("\\end{enumerate}", "")
                .replace("\\begin{itemize}", "")
                .replace("\\end{itemize}", "")
                .trim();

        String[] parts = block.split("\\\\item");
        for (String part : parts) {
            String item = part.trim();
            if (!item.isEmpty()) {
                items.add(item);
            }
        }
        return items;
    }

    public static String extractImagePath(String block) {
        Matcher matcher = Pattern.compile("\\\\includegraphics\\[.*?\\]\\{(.+?)\\}").matcher(block);
        if (matcher.find()) return matcher.group(1);
        return "";
    }

    public static String extractCaption(String block) {
        Matcher matcher = Pattern.compile("\\\\caption\\{(.*?)\\}").matcher(block);
        if (matcher.find()) {
            return matcher.group(1).trim();  // Will be "" if empty
        }
        return "";
    }


//    public static List<List<String>> extractTableData(String block) {

    /// /        block = block.replaceAll("<thsep>}", "");
//        List<List<String>> rows = new ArrayList<>();
//        String[] rowBlocks = block.split("<rsep>");
//        for (String row : rowBlocks) {
//            if (!row.contains("<csep>")) continue;
//            String[] cols = row.trim().split("<csep>");
//            List<String> cells = new ArrayList<>();
//            for (String col : cols) {
//                cells.add(col.replaceAll("\\\\hline", "").trim());
//            }
//            rows.add(cells);
//        }
//        return rows;
//    }
//    public static List<List<String>> extractTableData(String block) {
//        List<List<String>> rows = new ArrayList<>();
//
//        // Step 1: Remove column layout block like { <thsep>X<thsep>X<thsep>X <thsep> }
//        int layoutStart = block.indexOf("{ <thsep>");
//        if (layoutStart != -1) {
//            int layoutEnd = block.indexOf("}", layoutStart);
//            if (layoutEnd != -1) {
//                block = block.substring(layoutEnd + 1).trim();
//            }
//        }
//
//        // Step 2: Split rows and columns
//        String[] rowBlocks = block.split("<rsep>");
//        for (String row : rowBlocks) {
//            if (!row.contains("<csep>")) continue;
//
//            String[] cols = row.trim().split("<csep>");
//            List<String> cells = new ArrayList<>();
//            for (String col : cols) {
//                cells.add(col.replaceAll("\\\\hline", "").trim());
//            }
//            rows.add(cells);
//        }
//
//        return rows;
//    }
//    public static List<List<String>> extractTableData(String block) {
//        List<List<String>> rows = new ArrayList<>();
//
//        // Step 1: Remove the column layout block like { <thsep>X<thsep>X<thsep> }
//        int layoutStart = block.indexOf("{ <thsep>");
//        if (layoutStart != -1) {
//            int layoutEnd = block.indexOf("}", layoutStart);
//            if (layoutEnd != -1) {
//                block = block.substring(layoutEnd + 1).trim();
//            }
//        }
//
//        // Step 2: Split rows and parse columns (or single cells)
//        String[] rowBlocks = block.split("<rsep>");
//        for (String row : rowBlocks) {
//            row = row.replaceAll("\\\\hline", "").trim();
//            if (row.isEmpty()) continue;
//
//            List<String> cells = new ArrayList<>();
//            if (row.contains("<csep>")) {
//                String[] cols = row.split("<csep>");
//                for (String col : cols) {
//                    cells.add(col.trim());
//                }
//            } else {
//                cells.add(row);  // Single-column row
//            }
//            rows.add(cells);
//        }
//
//        return rows;
//    }
//    public static List<List<String>> extractTableData(String block) {
//        List<List<String>> rows = new ArrayList<>();
//
//        // Step 1: Remove the column layout block like { <thsep>X<thsep>X<thsep> }
//        int layoutStart = block.indexOf("{ <thsep>");
//        if (layoutStart != -1) {
//            int layoutEnd = block.indexOf("}", layoutStart);
//            if (layoutEnd != -1) {
//                block = block.substring(layoutEnd + 1).trim();
//            }
//        }
//
//        // Step 2: Remove any trailing LaTeX \end{...} lines
//        block = block.replaceAll("\\\\end\\{[^}]+\\}", "").trim();
//
//        // Step 3: Split rows and parse columns (or single cells)
//        String[] rowBlocks = block.split("<rsep>");
//        for (String row : rowBlocks) {
//            row = row.replaceAll("\\\\hline", "").trim();
//            if (row.isEmpty()) continue;
//
//            List<String> cells = new ArrayList<>();
//            if (row.contains("<csep>")) {
//                String[] cols = row.split("<csep>");
//                for (String col : cols) {
//                    cells.add(col.trim());
//                }
//            } else {
//                cells.add(row);  // Single-column row
//            }
//            rows.add(cells);
//        }
//
//        return rows;
//    }
    public static List<List<String>> extractTableData(String block) {
        List<List<String>> rows = new ArrayList<>();

        // Step 1: Remove the column layout block like { <thsep>X<thsep>X<thsep> }
        int layoutStart = block.indexOf("{ <thsep>");
        if (layoutStart != -1) {
            int layoutEnd = block.indexOf("}", layoutStart);
            if (layoutEnd != -1) {
                block = block.substring(layoutEnd + 1).trim();
            }
        }

        // Step 2: Remove any trailing LaTeX \end{...} lines
        block = block.replaceAll("\\\\end\\{[^}]+\\}", "").trim();

        // Step 3: Split rows and parse columns (or single cells)
        String[] rowBlocks = block.split("<rsep>");
        for (String row : rowBlocks) {
            row = row.replaceAll("\\\\hline", "").trim();
            if (row.isEmpty()) continue;

            List<String> cells = new ArrayList<>();
            if (row.contains("<csep>")) {
                // 🔥 Key fix: split with -1 to preserve trailing empty cells
                String[] cols = row.split("<csep>", -1);
                for (String col : cols) {
                    cells.add(col.trim());
                }
            } else {
                cells.add(row);  // Single-column row
            }

            rows.add(cells);
        }

        return rows;
    }
}
