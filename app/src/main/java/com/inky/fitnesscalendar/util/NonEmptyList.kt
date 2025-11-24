package com.inky.fitnesscalendar.util

interface NonEmptyList<T> : List<T>

@JvmInline
@Suppress("JavaDefaultMethodsNotOverriddenByDelegation")
private value class NonEmptyListImpl<T>(val data: List<T>) : List<T> by data, NonEmptyList<T> {
    init {
        require(data.isNotEmpty()) { "List must not be empty" }
    }
}


fun <T> List<T>.asNonEmptyOrNull(): NonEmptyList<T>? {
    if (this.isEmpty()) {
        return null
    }
    return NonEmptyListImpl(this)
}