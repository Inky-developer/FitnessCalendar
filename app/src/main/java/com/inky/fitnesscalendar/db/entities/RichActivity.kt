package com.inky.fitnesscalendar.db.entities

import androidx.room.Embedded
import androidx.room.Relation

data class RichActivity(
    @Embedded val activity: Activity,
    @Relation(parentColumn = "type_id", entityColumn = "uid")
    val type: ActivityType,
    @Relation(parentColumn = "place_id", entityColumn = "uid")
    val place: Place?
) {
    init {
        assert(activity.typeId == type.uid) { "Inconsistent RichActivity: type is $type, but id is ${activity.typeId}" }
        assert(activity.placeId == place?.uid) { "Inconsistent RichActivity: place is $place, but id is ${activity.placeId}" }
    }

    fun clean() = copy(
        type = type,
        place = if (type.hasPlace) place else null,
        activity = activity.clean(type)
    )
}