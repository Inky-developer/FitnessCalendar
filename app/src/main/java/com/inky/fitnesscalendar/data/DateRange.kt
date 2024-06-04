package com.inky.fitnesscalendar.data

import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date

data class DateRange(val start: Date, val end: Date?) {
    companion object {
        // Specifies by how many hours offset a day starts.
        // E.g. A value of 2 means that the day goes from 2 am to 2 am next day
        private const val DAY_START_OFFSET_HOURS = 2L

        // Let days start at 2 am
        fun atDay(offsetDays: Long): DateRange {
            val day =
                LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()).plusDays(offsetDays)
                    .minusHours(DAY_START_OFFSET_HOURS)
            val startOfToday = day.with(LocalTime.MIN).plusHours(DAY_START_OFFSET_HOURS)
            val endOfToday = day.with(LocalTime.MAX).plusHours(DAY_START_OFFSET_HOURS)

            return DateRange(
                start = Date.from(startOfToday.atZone(ZoneId.systemDefault()).toInstant()),
                end = if (offsetDays == 0L) null else Date.from(
                    endOfToday.atZone(ZoneId.systemDefault()).toInstant()
                )
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