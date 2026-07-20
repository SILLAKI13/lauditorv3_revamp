package com.digicoffer.lauditor.DocEditor

import java.util.ArrayList
import java.util.regex.Pattern

object Utility_Methods {

    @JvmStatic
    fun extractBetween(text: String, start: String, end: String): String {
        var startIndex = text.indexOf(start)
        if (startIndex == -1) return ""

        // Move to the first '{' after start marker
        startIndex = text.indexOf("{", startIndex)
        if (startIndex == -1) return ""

        val endIndex = text.indexOf(end, startIndex + 1)
        if (endIndex == -1) return ""

        return text.substring(startIndex + 1, endIndex).trim { it <= ' ' }
    }

    @JvmStatic
    fun extractListItems(blockStr: String): List<String> {
        var block = blockStr
        val items = ArrayList<String>()

        // Remove LaTeX list wrappers
        block = block.replace("\\begin{enumerate}", "")
            .replace("\\end{enumerate}", "")
            .replace("\\begin{itemize}", "")
            .replace("\\end{itemize}", "")
            .trim { it <= ' ' }

        val parts = block.split("\\\\item".toRegex()).toTypedArray()
        for (part in parts) {
            val item = part.trim { it <= ' ' }
            if (item.isNotEmpty()) {
                items.add(item)
            }
        }
        return items
    }

    @JvmStatic
    fun extractImagePath(block: String): String {
        val matcher = Pattern.compile("\\\\includegraphics\\[.*?\\]\\{(.+?)\\}").matcher(block)
        return if (matcher.find()) matcher.group(1) ?: "" else ""
    }

    @JvmStatic
    fun extractCaption(block: String): String {
        val matcher = Pattern.compile("\\\\caption\\{(.*?)\\}").matcher(block)
        return if (matcher.find()) {
            matcher.group(1)?.trim { it <= ' ' } ?: ""
        } else ""
    }

    @JvmStatic
    fun extractTableData(blockStr: String): List<List<String>> {
        var block = blockStr
        val rows = ArrayList<List<String>>()

        // Step 1: Remove the column layout block like { <thsep>X<thsep>X<thsep> }
        val layoutStart = block.indexOf("{ <thsep>")
        if (layoutStart != -1) {
            val layoutEnd = block.indexOf("}", layoutStart)
            if (layoutEnd != -1) {
                block = block.substring(layoutEnd + 1).trim { it <= ' ' }
            }
        }

        // Step 2: Remove any trailing LaTeX \end{...} lines
        block = block.replace("\\\\end\\{[^}]+\\}".toRegex(), "").trim { it <= ' ' }

        // Step 3: Split rows and parse columns (or single cells)
        val rowBlocks = block.split("<rsep>".toRegex()).toTypedArray()
        for (rowBlock in rowBlocks) {
            val row = rowBlock.replace("\\\\hline".toRegex(), "").trim { it <= ' ' }
            if (row.isEmpty()) continue

            val cells = ArrayList<String>()
            if (row.contains("<csep>")) {
                val cols = row.split("<csep>".toRegex(), -1)
                for (col in cols) {
                    cells.add(col.trim { it <= ' ' })
                }
            } else {
                cells.add(row) // Single-column row
            }

            rows.add(cells)
        }

        return rows
    }
}
