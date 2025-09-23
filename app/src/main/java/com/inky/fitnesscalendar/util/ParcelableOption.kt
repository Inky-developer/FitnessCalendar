package com.inky.fitnesscalendar.util

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class Option<out T : Parcelable?> : Parcelable {
    @Parcelize
    data object None : Option<Nothing>()

    @Parcelize
    data class Some<out T : Parcelable?>(val value: T) : Option<T>()

    fun unwrapOrNull(): T? = when (this) {
        is None -> null
        is Some -> value
    }

    fun or(fallback: () -> @UnsafeVariance T): T = when (this) {
        is None -> fallback()
        is Some -> value
    }
}

fun <T : Parcelable?> T.some(): Option<T> = Option.Some(this)
fun <T : Parcelable?> T?.someOrNone(): Option<T?> = this?.some() ?: Option.None
