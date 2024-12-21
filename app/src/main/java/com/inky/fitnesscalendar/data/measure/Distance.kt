package com.inky.fitnesscalendar.data.measure

import android.content.Context
import android.os.Parcelable
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.ui.util.ContextFormat
import kotlinx.parcelize.Parcelize
import kotlin.math.roundToLong


@Parcelize
@JvmInline
value class Distance(val meters: Long) : Parcelable, ContextFormat {
    constructor(kilometers: Double) : this(meters = (kilometers * 1000).roundToLong())

    override fun formatWithContext(context: Context) = if (kilometers >= 1) {
        context.getString(R.string.x_km, "%.1f".format(kilometers))
    } else {
        context.getString(R.string.x_m, meters)
    }

    val kilometers
        get() = meters / 1000.0
}