package com.inky.fitnesscalendar.util

fun readCSV(data: String): List<Map<String, String?>> {
    val lines = data.replace("\r\n", "\n").split("\n")
    if (lines.isEmpty()) {
        return emptyList()
    }

    val header = lines[0]
    val rows = lines.drop(1)

    val headers = splitRow(header)
    return rows.map { row ->
        headers.zip(splitRow(row).map { CSVWriter.unescapeString(it).ifEmpty { null } }).toMap()
    }
}

internal fun splitRow(row: String): List<String> {
    val result = mutableListOf<String>()
    val currentEntry = StringBuilder()
    var inString = false

    for ((index, char) in row.withIndex()) {
        val prevChar = row.getOrNull(index - 1)
        when {
            char == '"' && prevChar != '\\' -> inString = !inString
            char == ',' && !inString -> {
                result.add(currentEntry.toString())
                currentEntry.clear()
            }

            else -> {
                currentEntry.append(char)
            }
        }
    }
    result.add(currentEntry.toString())

    return result
}