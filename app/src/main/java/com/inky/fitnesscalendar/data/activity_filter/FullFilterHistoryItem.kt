package com.inky.fitnesscalendar.data.activity_filter

import androidx.room.Embedded
import androidx.room.Relation
import com.inky.fitnesscalendar.data.ActivityType

data class FullFilterHistoryItem(
    @Embedded val item: FilterHistoryItem,
    @Relation(parentColumn = "type_id", entityColumn = "uid")
    val type: ActivityType?
)