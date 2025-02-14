package com.inky.fitnesscalendar.data.measure

import com.inky.fitnesscalendar.ui.util.ContextFormat

interface Measure : ContextFormat {
    fun isNothing(): Boolean
}

fun <T : Measure> T.takeIfNotNothing(): T? = takeIf { !isNothing() }