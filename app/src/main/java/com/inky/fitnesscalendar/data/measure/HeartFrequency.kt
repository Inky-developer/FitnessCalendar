package com.inky.fitnesscalendar.data.measure

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class HeartFrequency(val bpm: Float): Comparable<HeartFrequency> {
    override fun compareTo(other: HeartFrequency) = this.bpm.compareTo(other.bpm)
}