package com.inky.fitnesscalendar.util

fun <T> List<T>.removedAt(index: Int): List<T> {
    return slice(0..<index) + slice((index + 1)..<size)
}

/**
 * Returns a new list with the element added to the list if it does not already exist in the list
 */
fun <T> List<T>.added(value: T): List<T> {
    val newSelection = filter { it != value }.toMutableList()
    newSelection.add(value)
    return newSelection
}