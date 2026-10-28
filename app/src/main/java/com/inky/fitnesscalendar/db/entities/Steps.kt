package com.inky.fitnesscalendar.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.inky.fitnesscalendar.data.EpochDay
import java.time.LocalDateTime
import java.time.LocalTime

@Entity(
    primaryKeys = ["day", "frame"],
)
data class Steps(
    @ColumnInfo(name = "day") val day: EpochDay,
    @ColumnInfo(name = "frame") val frame: Int,
    @ColumnInfo(name = "steps") val steps: Int,
) {
    constructor(dateTime: LocalDateTime, steps: Int) : this(
        day = EpochDay(dateTime.toLocalDate().toEpochDay()),
        frame = frameAt(dateTime.toLocalTime()),
        steps = steps
    )

    val time get(): LocalTime = LocalTime.ofSecondOfDay((frame * SECONDS_PER_FRAME).toLong())

    companion object {
        // Changing this value requires a database migration
        const val FRAMES_PER_DAY = 24
        const val SECONDS_PER_DAY = 86_400
        const val SECONDS_PER_FRAME = SECONDS_PER_DAY / FRAMES_PER_DAY

        private fun frameAt(time: LocalTime): Int = time.toSecondOfDay().floorDiv(SECONDS_PER_FRAME)
    }
}

fun List<Steps>.totalSteps() = sumOf { it.steps }