package com.inky.fitnesscalendar.util

import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.concurrent.TimeUnit

data class Duration(val elapsedMs: Long) {
    val elapsedSeconds
        get() = elapsedMs.toDouble() / ChronoUnit.SECONDS.duration.toMillis().toDouble()

    val elapsedHours
        get() = elapsedMs.toDouble() / ChronoUnit.HOURS.duration.toMillis().toDouble()

    fun format(): String {
        val hours = TimeUnit.MILLISECONDS.toHours(elapsedMs)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMs - TimeUnit.HOURS.toMillis(hours))
        val seconds = TimeUnit.MILLISECONDS.toSeconds(
            elapsedMs - TimeUnit.HOURS.toMillis(hours) - TimeUnit.MINUTES.toMillis(minutes)
        )
        val entries = mutableListOf<String>()
        if (hours > 0) {
            entries.add("${hours}h")
        }
        if (minutes > 0) {
            entries.add("${minutes}m")
        }
        if (hours == 0L && minutes <= 10L) {
            entries.add("${seconds}s")
        }

        return entries.joinToString(" ")
    }

    companion object {
        infix fun Date.until(end: Date): Duration {
            return Duration(end.time - this.time)
        }
    }
}

fun kotlin.time.Duration.format(): String {
    return Duration(inWholeMilliseconds).format()
}
