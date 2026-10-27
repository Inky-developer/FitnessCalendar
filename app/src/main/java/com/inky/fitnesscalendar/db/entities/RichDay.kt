package com.inky.fitnesscalendar.db.entities

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Relation
import com.inky.fitnesscalendar.data.EpochDay

@Immutable
class RichDay(
    @ColumnInfo(name = "epoch_day") val epochDay: EpochDay,
    @Embedded private val dbDay: Day?,
    @Relation(parentColumn = "epoch_day", entityColumn = "day") val steps: List<Steps>
) {
    constructor(epochDay: EpochDay) : this(epochDay = epochDay, dbDay = null, steps = emptyList())

    @Ignore
    val day = dbDay ?: Day(epochDay)
}