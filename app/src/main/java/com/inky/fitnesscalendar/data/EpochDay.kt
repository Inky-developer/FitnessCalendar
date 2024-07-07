package com.inky.fitnesscalendar.data

import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class EpochDay(val day: Long) {
    override fun toString(): String {
        return LocalDate.ofEpochDay(day).format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    companion object {
        fun today() = EpochDay(LocalDate.now().toEpochDay())
    }
}