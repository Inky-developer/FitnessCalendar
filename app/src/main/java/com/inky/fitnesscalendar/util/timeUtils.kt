package com.inky.fitnesscalendar.util

import android.os.Build
import com.inky.fitnesscalendar.data.EpochDay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.WeekFields
import java.util.Date
import java.util.Locale

fun LocalDateTime.toDate(zoneId: ZoneId = ZoneId.systemDefault()): Date =
    Date.from(atZone(zoneId).toInstant())

fun Date.toLocalDate(zoneId: ZoneId = ZoneId.systemDefault()): LocalDate =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        LocalDate.ofInstant(toInstant(), zoneId)
    } else {
        LocalDateTime.ofInstant(toInstant(), zoneId).toLocalDate()
    }

fun Date.toLocalDateTime(zoneId: ZoneId = ZoneId.systemDefault()): LocalDateTime =
    LocalDateTime.ofInstant(toInstant(), zoneId)

fun Date.toEpochDay(zoneId: ZoneId = ZoneId.systemDefault()): EpochDay =
    EpochDay(toLocalDate(zoneId).toEpochDay())

fun weekDays(locale: Locale): Sequence<DayOfWeek> {
    val firstDayOfWeek = WeekFields.of(locale).firstDayOfWeek
    return (0L..<7L).asSequence().map { firstDayOfWeek.plus(it) }
}