package com.inky.fitnesscalendar.localization

import android.content.Context
import android.text.format.DateFormat
import androidx.compose.runtime.Immutable
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.measure.Duration.Companion.until
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Immutable
@Singleton
class LocalizationRepository @Inject constructor(@ApplicationContext private val context: Context) {
    val timeFormatter: java.text.DateFormat = DateFormat.getTimeFormat(context)
    val dateFormatter: java.text.DateFormat = DateFormat.getMediumDateFormat(context)

    fun formatDuration(date: Date, now: Date = Date.from(Instant.now())): String {
        val duration = date until now

        if (duration.elapsedMs < ChronoUnit.DAYS.duration.toMillis()) {
            return duration.format()
        }

        return formatRelativeDate(date, now)
    }

    fun formatRelativeDate(date: Date, now: Date = Date.from(Instant.now())): String {
        val duration = date until now

        if (duration.elapsedMs < ChronoUnit.DAYS.duration.toMillis()) {
            return timeFormatter.format(date)
        }

        if (duration.elapsedMs < ChronoUnit.WEEKS.duration.toMillis()) {
            return Calendar.getInstance().apply {
                time = date
            }.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()) ?: "-"
        }

        // For farther in the past, just format the date itself
        return dateFormatter.format(date.toInstant().toEpochMilli())
    }

    fun formatRelativeLocalDate(localDate: LocalDate): String {
        val daysDiff = localDate.until(LocalDate.now(), ChronoUnit.DAYS)
        if (daysDiff == 0L) {
            return context.getString(R.string.today)
        }

        if (daysDiff == 1L) {
            return context.getString(R.string.yesterday)
        }

        if (daysDiff < 7) {
            return localDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
        }

        return localDate.format(localDateFormatter)
    }

    companion object {
        val localDateFormatter: DateTimeFormatter by lazy {
            DateTimeFormatter.ofLocalizedDate(
                FormatStyle.FULL
            )
        }
    }
}