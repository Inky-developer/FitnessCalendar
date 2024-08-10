package com.inky.fitnesscalendar.data.activity_filter

import android.os.Parcelable
import com.inky.fitnesscalendar.data.EpochDay
import com.inky.fitnesscalendar.localization.LocalizationRepository
import com.inky.fitnesscalendar.util.DAY_START_OFFSET_HOURS
import com.inky.fitnesscalendar.util.toDate
import com.inky.fitnesscalendar.util.toLocalDate
import kotlinx.parcelize.Parcelize
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date

@Parcelize
data class DateRange(val start: Date, val end: Date?) : Parcelable {
    fun getText(): String {
        val startDateString = start.toLocalDate().format(LocalizationRepository.localDateFormatter)
        val endDateString =
            end?.toLocalDate()?.format(LocalizationRepository.localDateFormatter) ?: ""
        return "$startDateString - $endDateString"
    }

    companion object {
        // Let days start at 2 am
        fun atRelativeDay(offsetDays: Long): DateRange {
            val day =
                LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()).plusDays(offsetDays)
                    .minusHours(DAY_START_OFFSET_HOURS).toLocalDate()
            return atDay(day)
        }

        fun atDay(day: EpochDay) = atDay(day.toLocalDate())

        private fun atDay(day: LocalDate): DateRange {
            val startOfDay = day.atStartOfDay().plusHours(DAY_START_OFFSET_HOURS)
            val endOfDay = day.plusDays(1).atStartOfDay().plusHours(DAY_START_OFFSET_HOURS)

            return DateRange(
                start = startOfDay.toDate(),
                end = endOfDay.toDate()
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