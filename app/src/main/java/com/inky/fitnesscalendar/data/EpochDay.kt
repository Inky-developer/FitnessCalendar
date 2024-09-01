package com.inky.fitnesscalendar.data

import android.os.Parcelable
import com.inky.fitnesscalendar.util.DAY_START_OFFSET_HOURS
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Parcelize
@JvmInline
value class EpochDay(val day: Long) : Parcelable, Comparable<EpochDay> {
    fun toLocalDate(): LocalDate = LocalDate.ofEpochDay(day)

    override fun toString(): String {
        return LocalDate.ofEpochDay(day).format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    override fun compareTo(other: EpochDay) = day.compareTo(other.day)

    companion object {
        fun today(offsetHours: Long = DAY_START_OFFSET_HOURS) =
            EpochDay(LocalDateTime.now().minusHours(offsetHours).toLocalDate().toEpochDay())
    }
}