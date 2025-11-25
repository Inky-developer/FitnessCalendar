package com.inky.fitnesscalendar.ui.util

import androidx.compose.ui.Modifier

inline fun Modifier.applyIf(condition: Boolean, block: Modifier.() -> Modifier): Modifier {
    return if (condition) {
        block()
    } else {
        this
    }
}