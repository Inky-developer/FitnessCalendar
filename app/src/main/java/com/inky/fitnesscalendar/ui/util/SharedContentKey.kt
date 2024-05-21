package com.inky.fitnesscalendar.ui.util

open class SharedContentKey {
    data class ActivityCard(val id: Int?) : SharedContentKey()

    object NewActivityFAB : SharedContentKey()

    object AppBar : SharedContentKey()
}