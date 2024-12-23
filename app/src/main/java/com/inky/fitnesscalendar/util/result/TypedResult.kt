package com.inky.fitnesscalendar.util.result

import com.inky.fitnesscalendar.util.result.TypedResult.Err
import com.inky.fitnesscalendar.util.result.TypedResult.Ok
import kotlinx.coroutines.CancellationException

sealed interface TypedResult<out T, out E> {
    @JvmInline
    value class Ok<T>(val value: T) : TypedResult<T, Nothing> {
        override fun ok(): T? = value
        override fun err(): Nothing? = null
    }

    @JvmInline
    value class Err<E>(val error: E) : TypedResult<Nothing, E> {
        override fun ok(): Nothing? = null
        override fun err(): E? = error
    }

    fun ok(): T?
    fun err(): E?
}

fun <T, E> TypedResult<T, E>.isOk(): Boolean = this is Ok
fun <T, E> TypedResult<T, E>.isErr(): Boolean = this is Err

fun <T> T.asOk(): TypedResult<T, Nothing> = Ok(this)
fun <E> E.asErr(): TypedResult<Nothing, E> = Err(this)

inline fun <T, E, U> TypedResult<T, E>.map(mapper: (T) -> U): TypedResult<U, E> {
    return when (this) {
        is Ok -> Ok(mapper(value))
        is Err -> Err(error)
    }
}

interface Raises<in E> {
    fun raise(error: E): Nothing

    /**
     * Either returns back the OK value or immediately returns from the block with the error
     */
    fun <T> TypedResult<T, E>.unwrap(): T = when (this) {
        is Ok -> value
        is Err -> raise(error)
    }
}

/**
 * Internal class. Do not construct manually
 */
class TryScope<E : Any> : Raises<E> {
    var error: E? = null

    override fun raise(error: E): Nothing {
        this.error = error
        throw TryScopeCancellation()
    }
}

class TryScopeCancellation : CancellationException()

/**
 * Tries to run the block and returns a result of either its return value or the raised error
 */
inline fun <T, E : Any> tryScope(block: Raises<E>.() -> T): TypedResult<T, E> {
    val scope = TryScope<E>()
    try {
        return Ok(scope.block())
    } catch (_: TryScopeCancellation) {
        val error = scope.error
        assert(error != null)
        return Err(error as E)
    }
}
