package com.inky.fitnesscalendar.data

import com.inky.fitnesscalendar.util.DAY_START_OFFSET_HOURS
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class EpochDay(val day: Long) {
    override fun toString(): String {
        return LocalDate.ofEpochDay(day).format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    companion object {
        fun today(offsetHours: Long = DAY_START_OFFSET_HOURS) =
            EpochDay(LocalDateTime.now().minusHours(offsetHours).toLocalDate().toEpochDay())
    }
}