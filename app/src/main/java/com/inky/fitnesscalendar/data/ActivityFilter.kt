package com.inky.fitnesscalendar.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Date


@Parcelize
data class ActivityFilter(
    val types: List<ActivityType> = emptyList(),
    val categories: List<ActivityCategory> = emptyList(),
    val text: String? = null,
    val startRangeDate: Date? = null,
    val endRangeDate: Date? = null
) : Parcelable {
    fun isEmpty() = this == ActivityFilter()

    companion object {
        // Let days start at 2 am
        fun atDay(instant: Instant): ActivityFilter {
            val day = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
            val startOfToday = day.with(LocalTime.MIN).plusHours(2)
            val endOfToday = day.with(LocalTime.MAX).plusHours(2)

            return ActivityFilter(
                startRangeDate = Date.from(startOfToday.atZone(ZoneId.systemDefault()).toInstant()),
                endRangeDate = Date.from(endOfToday.atZone(ZoneId.systemDefault()).toInstant())
            )
        }
    }
}