package com.inky.fitnesscalendar.util.decision_tree

data class Example<T>(val classification: T, val attributes: List<Any>) {
    fun withoutAttribute(index: Int) = Example(
        classification,
        attributes.slice(0..<index) + attributes.slice((index + 1)..<attributes.size)
    )
}