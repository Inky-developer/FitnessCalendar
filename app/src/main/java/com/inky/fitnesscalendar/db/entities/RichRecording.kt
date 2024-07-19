package com.inky.fitnesscalendar.db.entities

import androidx.room.Embedded
import androidx.room.Relation

data class RichRecording(
    @Embedded val recording: Recording,
    @Relation(parentColumn = "type_id", entityColumn = "uid")
    val type: ActivityType,
    @Relation(parentColumn = "place_id", entityColumn = "uid")
    val place: Place?,
) {
    init {
        assert(recording.typeId == type.uid) { "Inconsistent RichRecording" }
        assert(recording.placeId == place?.uid) { "Inconsistent RichRecording" }
    }
}