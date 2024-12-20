package com.inky.fitnesscalendar.util.result

import com.inky.fitnesscalendar.util.result.TypedResult.Err
import com.inky.fitnesscalendar.util.result.TypedResult.Ok

sealed interface TypedResult<T, E> {
    @JvmInline
    value class Ok<T, E>(val value: T) : TypedResult<T, E> {
        override fun ok(): T? = value
        override fun err(): E? = null
    }

    @JvmInline
    value class Err<T, E>(val error: E) : TypedResult<T, E> {
        override fun ok(): T? = null
        override fun err(): E? = error
    }

    fun ok(): T?
    fun err(): E?

    fun isOk(): Boolean = this is Ok
    fun isErr(): Boolean = this is Err
}

fun <T, E> T.asOk(): TypedResult<T, E> = Ok(this)
fun <T, E> E.asErr(): TypedResult<T, E> = Err(this)

inline fun <T, E, U> TypedResult<T, E>.map(mapper: (T) -> U): TypedResult<U, E> {
    return when (this) {
        is Ok -> Ok(mapper(value))
        is Err -> Err(error)
    }
}
