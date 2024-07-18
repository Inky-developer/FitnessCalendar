package com.inky.fitnesscalendar.db.entities

import androidx.room.Embedded
import androidx.room.Relation

data class RichFilterHistoryItem(
    @Embedded val item: FilterHistoryItem,
    @Relation(parentColumn = "type_id", entityColumn = "uid")
    val type: ActivityType?,
    @Relation(parentColumn = "place_id", entityColumn = "uid")
    val place: Place?,
)