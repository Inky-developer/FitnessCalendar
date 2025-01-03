package com.inky.fitnesscalendar.data.measure

import android.content.Context
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.ui.util.ContextFormat
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

@Serializable
@JvmInline
value class HeartFrequency internal constructor(val bpm: Float) : Comparable<HeartFrequency>,
    ContextFormat {
    override fun compareTo(other: HeartFrequency) = this.bpm.compareTo(other.bpm)

    override fun formatWithContext(context: Context) =
        context.getString(R.string.x_bpm, bpm.roundToInt())
}

fun Float.bpm() = HeartFrequency(bpm = this)