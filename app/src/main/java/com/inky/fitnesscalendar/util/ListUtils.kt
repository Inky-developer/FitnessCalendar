package com.inky.fitnesscalendar.util

fun <T> List<T>.removedAt(index: Int): List<T> {
    return slice(0..<index) + slice((index + 1)..<size)
}