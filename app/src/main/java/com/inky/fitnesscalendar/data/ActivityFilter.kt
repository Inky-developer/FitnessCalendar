package com.inky.fitnesscalendar.data

import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Date


data class ActivityFilter(
    val types: List<ActivityType> = emptyList(),
    val text: String? = null,
    val startRangeDate: Date? = null,
    val endRangeDate: Date? = null
) {
    fun isEmpty() = this == ActivityFilter()

    companion object {
        fun atDay(instant: Instant): ActivityFilter {
            val day = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
            val startOfToday = day.with(LocalTime.MIN)
            val endOfToday = day.with(LocalTime.MAX)

            return ActivityFilter(
                startRangeDate = Date.from(startOfToday.atZone(ZoneId.systemDefault()).toInstant()),
                endRangeDate = Date.from(endOfToday.atZone(ZoneId.systemDefault()).toInstant())
            )
        }
    }
}