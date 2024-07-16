package com.inky.fitnesscalendar.util

import android.os.Build
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

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