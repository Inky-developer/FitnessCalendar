package com.inky.fitnesscalendar.db.entities

import androidx.room.Embedded
import androidx.room.Relation
import com.inky.fitnesscalendar.db.entities.Activity
import com.inky.fitnesscalendar.db.entities.ActivityType

data class TypeActivity(
    @Embedded val activity: Activity,
    @Relation(parentColumn = "type_id", entityColumn = "uid")
    val type: ActivityType
) {
    init {
        assert(activity.typeId == type.uid) { "Inconsistent TypeActivity" }
    }

    fun clean() = copy(type = type, activity = activity.clean(type))
}