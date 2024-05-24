package com.inky.fitnesscalendar.util

fun readCSV(data: String): List<Map<String, String?>> {
    val lines = data.split("\n")
    if (lines.isEmpty()) {
        return emptyList()
    }

    val header = lines[0]
    val rows = lines.drop(1)

    val headers = header.split(",")
    return rows.map { row ->
        headers.zip(row.split(",").map { if (it.isEmpty()) null else it }).toMap()
    }
}