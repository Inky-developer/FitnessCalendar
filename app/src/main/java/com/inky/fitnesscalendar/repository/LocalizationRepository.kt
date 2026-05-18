package com.inky.fitnesscalendar.repository

import android.content.Context
import android.text.format.DateFormat
import androidx.compose.runtime.Immutable
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.measure.Duration.Companion.until
import com.inky.fitnesscalendar.util.DAY_START_OFFSET_HOURS
import com.inky.fitnesscalendar.util.toLocalDate
import com.inky.fitnesscalendar.util.toLocalDateTime
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Immutable
interface LocalizationRepository {
    val timeFormatter: java.text.DateFormat
    val dateFormatter: java.text.DateFormat

    fun formatDuration(date: Date, now: Date = Date.from(Instant.now())): String
    fun formatRelativeDate(date: Date, now: LocalDateTime = LocalDateTime.now()): String
    fun formatRelativeLocalDate(localDate: LocalDate): String

    companion object {
        val localDateFormatter: DateTimeFormatter by lazy {
            DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)
        }

        val shortLocalDateFormatter: DateTimeFormatter by lazy {
            DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
        }
    }
}

@Immutable
@Singleton
class LocalizationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : LocalizationRepository {
    override val timeFormatter: java.text.DateFormat = DateFormat.getTimeFormat(context)
    override val dateFormatter: java.text.DateFormat = DateFormat.getMediumDateFormat(context)

    override fun formatDuration(date: Date, now: Date): String {
        val duration = date until now

        if (duration.elapsedMs < ChronoUnit.DAYS.duration.toMillis()) {
            return duration.format()
        }

        return formatRelativeDate(date, now.toLocalDateTime())
    }

    override fun formatRelativeDate(date: Date, now: LocalDateTime): String {
        val localDate = date.toLocalDate()
        val daysDiff = localDate.until(
            now.minusHours(DAY_START_OFFSET_HOURS).toLocalDate(),
            ChronoUnit.DAYS
        )

        if (daysDiff == 0L) {
            return timeFormatter.format(date)
        }

        if (daysDiff < 7) {
            return localDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
        }

        return dateFormatter.format(date.toInstant().toEpochMilli())
    }

    override fun formatRelativeLocalDate(localDate: LocalDate): String {
        val daysDiff = localDate.until(
            LocalDateTime.now().minusHours(DAY_START_OFFSET_HOURS).toLocalDate(),
            ChronoUnit.DAYS
        )
        if (daysDiff == 0L) {
            return context.getString(R.string.today)
        }

        if (daysDiff == 1L) {
            return context.getString(R.string.yesterday)
        }

        if (daysDiff < 7) {
            return localDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
        }

        return localDate.format(LocalizationRepository.localDateFormatter)
    }
}

@InstallIn(SingletonComponent::class)
@Module
abstract class LocalizationModule {
    @Binds
    @Singleton
    abstract fun bindLocalizationRepository(
        impl: LocalizationRepositoryImpl
    ): LocalizationRepository
}
