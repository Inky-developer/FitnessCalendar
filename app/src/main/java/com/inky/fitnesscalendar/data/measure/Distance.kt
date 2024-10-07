package com.inky.fitnesscalendar.data.measure

import android.content.Context
import android.os.Parcelable
import com.inky.fitnesscalendar.R
import kotlinx.parcelize.Parcelize
import kotlin.math.roundToLong


@Parcelize
data class Distance(val meters: Long) : Parcelable {
    constructor(kilometers: Double) : this(meters = (kilometers * 1000).roundToLong())

    fun format(context: Context) = context.getString(R.string.x_km, "%.1f".format(kilometers))

    val kilometers
        get() = meters / 1000.0
}