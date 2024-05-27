package com.inky.fitnesscalendar.util

import java.util.Date
import java.util.concurrent.TimeUnit

data class Duration(val elapsedMs: Long) {
    fun format(): String {
        val hours = TimeUnit.MILLISECONDS.toHours(elapsedMs)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMs - TimeUnit.HOURS.toMillis(hours))
        val seconds = TimeUnit.MILLISECONDS.toSeconds(
            elapsedMs - TimeUnit.HOURS.toMillis(hours) - TimeUnit.MINUTES.toMillis(minutes)
        )
        val builder = StringBuilder()
        if (hours > 0) {
            builder.append("${hours}h ")
        }
        if (minutes > 0) {
            builder.append("${minutes}m ")
        }
        if (hours == 0L && minutes <= 10L) {
            builder.append("${seconds}s ")
        }


        return builder.toString()
    }

    companion object {
        infix fun Date.until(end: Date): Duration {
            return Duration(end.time - this.time)
        }
    }
}
