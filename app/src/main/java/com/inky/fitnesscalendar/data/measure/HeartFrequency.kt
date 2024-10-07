package com.inky.fitnesscalendar.data.measure

import android.content.Context
import com.inky.fitnesscalendar.R
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

@Serializable
@JvmInline
value class HeartFrequency(val bpm: Float) : Comparable<HeartFrequency> {
    override fun compareTo(other: HeartFrequency) = this.bpm.compareTo(other.bpm)

    fun format(context: Context) = context.getString(R.string.x_bpm, bpm.roundToInt())
}