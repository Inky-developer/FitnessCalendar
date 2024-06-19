package com.inky.fitnesscalendar.data

import androidx.room.Embedded
import androidx.room.Relation

data class TypeRecording(
    @Embedded val recording: Recording,
    @Relation(parentColumn = "type_id", entityColumn = "uid")
    val type: ActivityType
) {
    init {
        assert(recording.typeId == type.uid) { "Inconsistent TypeActivity" }
    }
}