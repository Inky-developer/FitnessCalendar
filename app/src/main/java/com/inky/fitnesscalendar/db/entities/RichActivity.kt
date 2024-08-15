package com.inky.fitnesscalendar.db.entities

import androidx.room.Embedded
import androidx.room.Relation

data class RichActivity(
    @Embedded val activity: Activity,
    @Relation(parentColumn = "type_id", entityColumn = "uid")
    val type: ActivityType,
    @Relation(parentColumn = "place_id", entityColumn = "uid")
    val place: Place?,
    @Relation(parentColumn = "uid", entityColumn = "activity_id")
    val track: Track?
) {
    init {
        assert(activity.typeId == type.uid) { "Inconsistent RichActivity: type is $type, but id is ${activity.typeId}" }
        assert(activity.placeId == place?.uid) { "Inconsistent RichActivity: place is $place, but id is ${activity.placeId}" }
        assert(track?.activityId == activity.uid) { "Inconsistent RichActivity: track is $track, but id is ${activity.uid}" }
    }

    fun clean() = copy(
        place = if (type.hasPlace) place else null,
        activity = activity.clean(type)
    )
}