package com.inky.fitnesscalendar.util

import android.util.Log

@Deprecated(
    message = "Debug calls should not be comitted.",
    replaceWith = ReplaceWith("println(this)")
)
inline fun <reified T> T.debug(): T {
    val className = T::class.simpleName
    Log.w("debug_utils", "<$className>: $this")
    return this
}