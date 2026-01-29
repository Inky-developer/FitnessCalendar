package com.inky.fitnesscalendar.db.entities

import androidx.compose.runtime.Immutable
import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Relation
import com.inky.fitnesscalendar.data.ImageName

// The var isSynthetic should never be modified after initial creation, so the @Immutable is valid
@Immutable
data class RichActivity(
    @Embedded val activity: Activity,
    @Relation(parentColumn = "type_id", entityColumn = "uid")
    val type: ActivityType,
    @Relation(parentColumn = "place_id", entityColumn = "uid")
    val place: Place?,
    @Relation(
        parentColumn = "uid",
        entityColumn = "activity_id",
        entity = ActivityImage::class,
        projection = ["image_name"]
    )
    val images: List<ImageName>,
) {
    /// If true, this richActivity was generated in the statistics for the purpose of attributing a single
    /// activity to multiple time windows
    @Ignore
    var isSynthetic: Boolean = false

    init {
        assert(activity.typeId == type.uid) { "Inconsistent RichActivity: type is $type, but id is ${activity.typeId}" }
        assert(activity.placeId == place?.uid) { "Inconsistent RichActivity: place is $place, but id is ${activity.placeId}" }
    }

    fun clean() = copy(
        place = if (type.hasPlace) place else null,
        activity = activity.clean(type)
    )
}