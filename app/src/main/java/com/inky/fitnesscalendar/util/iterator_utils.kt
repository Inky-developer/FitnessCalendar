package com.inky.fitnesscalendar.util

fun <T, R : Comparable<R>> Iterable<T>.filteredMaxByOrNull(selector: (T) -> R?): T? {
    return map { it to selector(it) }
        .filter { it.second != null }
        .maxByOrNull { it.second!! }
        ?.first
}