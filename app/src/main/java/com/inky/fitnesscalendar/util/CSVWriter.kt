package com.inky.fitnesscalendar.util

import java.util.Date

class CSVWriter(private val headers: List<String>, elements: CSVWriterScope.() -> Unit) {
    private val rows: MutableList<String> = mutableListOf()

    init {
        val scope = CSVWriterScope()
        scope.elements()
        rows.addAll(scope.rows)
    }

    override fun toString(): String {
        val header = headers.joinToString(",")
        return listOf(header, *rows.toTypedArray()).joinToString("\n")
    }

    companion object {
        fun escapeString(value: String): String {
            val newValue =
                value.replace("\\", "\\\\").replace("\n", "\\n").replace("\"", "\\\"")
            if (newValue.contains(",")) {
                return "\"$newValue\""
            }
            return newValue
        }

        fun unescapeString(value: String): String {
            return value.removeSurrounding("\"").replace("\\\"", "\"").replace("\\n", "\n")
                .replace("\\\\", "\\")
        }
    }

    class CSVWriterScope(val rows: MutableList<String> = mutableListOf()) {
        fun <T> rows(data: List<T>, map: (T) -> List<Any?>) {
            val rows = data.map { row ->
                map(row).joinToString(",") { toCSVString(it) }
            }

            this.rows.addAll(rows)
        }

        companion object {
            fun toCSVString(element: Any?): String {
                return when (element) {
                    null -> ""
                    is String -> escapeString(element)
                    is Int -> element.toString()
                    is Long -> element.toString()
                    is Enum<*> -> element.name
                    is Date -> element.time.toString()
                    else -> throw Error("Invalid value: $element")
                }
            }
        }
    }
}