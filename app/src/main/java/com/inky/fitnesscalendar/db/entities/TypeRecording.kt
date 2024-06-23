package com.inky.fitnesscalendar.db.entities

import androidx.room.Embedded
import androidx.room.Relation
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.db.entities.Recording

data class TypeRecording(
    @Embedded val recording: Recording,
    @Relation(parentColumn = "type_id", entityColumn = "uid")
    val type: ActivityType
) {
    init {
        assert(recording.typeId == type.uid) { "Inconsistent TypeActivity" }
    }
}