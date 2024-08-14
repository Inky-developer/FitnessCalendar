package com.inky.fitnesscalendar.db.entities

import androidx.room.Embedded
import androidx.room.Relation

data class RichActivityTypeName(
    @Embedded val typeName: ActivityTypeName,
    @Relation(parentColumn = "type_id", entityColumn = "uid")
    val type: ActivityType
) {
    init {
        assert(typeName.typeId == type.uid) { "Invalid RichActivityTypeName $this" }
    }
}
