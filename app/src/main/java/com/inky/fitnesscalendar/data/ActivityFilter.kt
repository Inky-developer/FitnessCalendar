package com.inky.fitnesscalendar.data

import java.util.Date


data class ActivityFilter(
    val types: List<ActivityType> = emptyList(),
    val text: String? = null,
    val startRangeDate: Date? = null,
    val endRangeDate: Date? = null
) {
    fun isEmpty() = this == ActivityFilter()
}