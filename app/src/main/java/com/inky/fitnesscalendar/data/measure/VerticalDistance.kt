package com.inky.fitnesscalendar.data.measure

import android.content.Context
import android.os.Parcelable
import com.inky.fitnesscalendar.R
import kotlinx.parcelize.Parcelize
import kotlin.math.roundToInt


@Parcelize
@JvmInline
value class VerticalDistance internal constructor(val meters: Double) : Parcelable, Measure {
    override fun formatWithContext(context: Context) =
        context.getString(R.string.x_m, meters.roundToInt())

    override fun isNothing() = meters == 0.0
}