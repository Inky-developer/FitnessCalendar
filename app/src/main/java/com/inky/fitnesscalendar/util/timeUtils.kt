package com.inky.fitnesscalendar.util

import android.os.Build
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

fun LocalDateTime.toDate(): Date = Date.from(atZone(ZoneId.systemDefault()).toInstant())

fun Date.toLocalDate(zoneId: ZoneId = ZoneId.systemDefault()): LocalDate =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        LocalDate.ofInstant(toInstant(), zoneId)
    } else {
        LocalDateTime.ofInstant(toInstant(), zoneId).toLocalDate()
    }