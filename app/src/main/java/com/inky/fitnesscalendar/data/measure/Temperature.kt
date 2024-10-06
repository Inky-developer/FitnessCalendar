package com.inky.fitnesscalendar.data.measure

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class Temperature(val celsius: Float)