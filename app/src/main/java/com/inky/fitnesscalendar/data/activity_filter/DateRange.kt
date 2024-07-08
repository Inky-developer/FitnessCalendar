package com.inky.fitnesscalendar.data.activity_filter

import com.inky.fitnesscalendar.util.DAY_START_OFFSET_HOURS
import com.inky.fitnesscalendar.util.toDate
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date

data class DateRange(val start: Date, val end: Date?) {
    companion object {
        // Let days start at 2 am
        fun atDay(offsetDays: Long): DateRange {
            val day =
                LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()).plusDays(offsetDays)
                    .minusHours(DAY_START_OFFSET_HOURS)
            val startOfToday = day.with(LocalTime.MIN).plusHours(DAY_START_OFFSET_HOURS)
            val endOfToday = day.with(LocalTime.MAX).plusHours(DAY_START_OFFSET_HOURS)

            return DateRange(
                start = startOfToday.toDate(),
                end = if (offsetDays == 0L) null else endOfToday.toDate()
            )
        }

        fun lastDays(numDays: Int): DateRange {
            val instant = Instant.now()
            return DateRange(
                start = Date.from(Instant.ofEpochMilli(instant.toEpochMilli() - ChronoUnit.DAYS.duration.toMillis() * numDays)),
                end = null
            )
        }

        fun lastDays(
            numDaysStart: Int,
            numDaysEnd: Int,
            instant: Instant = Instant.now()
        ): DateRange {
            return DateRange(
                start = Date.from(Instant.ofEpochMilli(instant.toEpochMilli() - ChronoUnit.DAYS.duration.toMillis() * numDaysStart)),
                end = Date.from(Instant.ofEpochMilli(instant.toEpochMilli() - ChronoUnit.DAYS.duration.toMillis() * numDaysEnd)),
            )
        }
    }
}