package com.inky.fitnesscalendar.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlin.math.roundToLong


@Parcelize
data class Distance(val meters: Long) : Parcelable {
    constructor(kilometers: Double) : this(meters = (kilometers * 1000).roundToLong())

    val kilometers
        get() = meters / 1000.0
}