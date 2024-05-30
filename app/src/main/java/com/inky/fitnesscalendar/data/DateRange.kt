package com.inky.fitnesscalendar.data

import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date

data class DateRange(val start: Date, val end: Date) {
    companion object {
        // Let days start at 2 am
        fun atDay(offsetDays: Long, instant: Instant = Instant.now()): DateRange {
            val day = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).plusDays(offsetDays)
            val startOfToday = day.with(LocalTime.MIN).plusHours(2)
            val endOfToday = day.with(LocalTime.MAX).plusHours(2)

            return DateRange(
                start = Date.from(startOfToday.atZone(ZoneId.systemDefault()).toInstant()),
                end = Date.from(endOfToday.atZone(ZoneId.systemDefault()).toInstant())
            )
        }

        fun lastDays(numDays: Int, instant: Instant = Instant.now()): DateRange {
            return DateRange(
                start = Date.from(Instant.ofEpochMilli(instant.toEpochMilli() - ChronoUnit.DAYS.duration.toMillis() * numDays)),
                end = Date.from(instant)
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