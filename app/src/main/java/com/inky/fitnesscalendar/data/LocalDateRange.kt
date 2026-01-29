package com.inky.fitnesscalendar.data

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.WeekFields
import java.util.Locale

@JvmInline
value class LocalDateRange(val range: OpenEndRange<LocalDateTime>) {
    val start get() = range.start
    val endExclusive get() = range.endExclusive

    companion object Companion {
        fun dayOf(date: LocalDate): LocalDateRange {
            val start = date.atStartOfDay()
            val end = date.plusDays(1).atStartOfDay()
            return LocalDateRange(start..<end)
        }

        fun weekOf(date: LocalDate, locale: Locale = Locale.getDefault()): LocalDateRange {
            val dayOfWeekField = WeekFields.of(locale).dayOfWeek()
            val start = date.atStartOfDay().with(dayOfWeekField, dayOfWeekField.range().minimum)
            val end =
                date.atStartOfDay().with(dayOfWeekField, dayOfWeekField.range().maximum).plusDays(1)
            return LocalDateRange(start..<end)
        }

        fun monthOf(date: LocalDate): LocalDateRange {
            val start = date.withDayOfMonth(1).atStartOfDay()
            val end = date.withDayOfMonth(1).plusMonths(1).atStartOfDay()
            return LocalDateRange(start..<end)
        }

        fun yearOf(date: LocalDate): LocalDateRange {
            val start = date.withDayOfYear(1).atStartOfDay()
            val end = date.withDayOfYear(1).plusYears(1).atStartOfDay()
            return LocalDateRange(start..<end)
        }

        val COMPARATOR: Comparator<LocalDateRange> = Comparator.comparing(LocalDateRange::start)
    }
}
