package com.inky.fitnesscalendar.util.decision_tree

import com.inky.fitnesscalendar.util.removedAt

data class Example<T : Any>(val classification: T?, val attributes: List<Any>) {
    fun withoutAttribute(index: Int) = Example(
        classification,
        attributes.removedAt(index)
    )
}