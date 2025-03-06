package com.inky.fitnesscalendar.data.measure

import android.content.Context
import com.inky.fitnesscalendar.R
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

@Serializable
@JvmInline
value class HeartFrequency internal constructor(val bpm: Double) : Comparable<HeartFrequency>,
    Measure {
    override fun compareTo(other: HeartFrequency) = this.bpm.compareTo(other.bpm)

    override fun isNothing() = bpm == 0.0

    override fun formatWithContext(context: Context) =
        context.getString(R.string.x_bpm, bpm.roundToInt())
}

fun Double.bpm() = HeartFrequency(bpm = this)