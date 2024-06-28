package com.inky.fitnesscalendar

import com.inky.fitnesscalendar.util.CSVWriter
import com.inky.fitnesscalendar.util.readCSV
import com.inky.fitnesscalendar.util.splitRow
import org.junit.Assert.assertEquals
import org.junit.Test

class CSVTest {
    private val inputStrings = listOf(
        "Simple String.",
        "String, with, commas",
        "String with a \nnewline",
        "String with commas, \"quotes, and \nnewlines\""
    )

    @Test
    fun escapingWorks() {
        for (string in inputStrings) {
            val escaped = CSVWriter.escapeString(string)
            println("escaped: $escaped")
            val unescaped = CSVWriter.unescapeString(escaped)
            assertEquals(string, unescaped)
        }
    }

    @Test
    fun testSplitRow() {
        val expectedResults = listOf(
            listOf("Simple String."),
            listOf("String", " with", " commas"),
            listOf("String with a \nnewline"),
            listOf("String with commas", " quotes, and \nnewlines")
        )
        for ((inputString, expectedResult) in inputStrings.zip(expectedResults)) {
            assertEquals(expectedResult, splitRow(inputString))
        }
    }

    @Test
    fun readingAndWritingWorks() {
        val data = listOf(
            listOf("A", "B", "C"),
            listOf("\"A\"", "B, B", "C\nC"),
            listOf("\"A,\n\"", "B,\nB", "\"C\nC,C,c\n\"")
        )

        val csv = CSVWriter(listOf("A", "B", "C")) {
            rows(data) { it }
        }.toString()

        val parsedRows = readCSV(csv).map { row ->
            listOf(
                row["A"],
                row["B"],
                row["C"]
            )
        }

        for ((expected, parsed) in data.zip(parsedRows)) {
            assertEquals(expected, parsed)
        }
    }
}