package com.inky.fitnesscalendar.db.entities

import androidx.room.Embedded
import androidx.room.Relation

data class RichActivity(
    @Embedded val activity: Activity,
    @Relation(parentColumn = "type_id", entityColumn = "uid")
    val type: ActivityType
) {
    init {
        assert(activity.typeId == type.uid) { "Inconsistent TypeActivity: type is $type, but id is ${activity.typeId}" }
    }

    fun clean() = copy(type = type, activity = activity.clean(type))
}