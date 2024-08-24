package com.inky.fitnesscalendar.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.inky.fitnesscalendar.data.ActivityCategory
import com.inky.fitnesscalendar.data.Vehicle
import com.inky.fitnesscalendar.data.activity_filter.AttributeFilter
import com.inky.fitnesscalendar.data.activity_filter.DateRangeOption
import java.time.Instant
import java.util.Date

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = ActivityType::class,
            parentColumns = arrayOf("uid"),
            childColumns = arrayOf("type_id"),
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Place::class,
            parentColumns = arrayOf("uid"),
            childColumns = arrayOf("place_id"),
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("last_updated"), Index("type_id"), Index("place_id")]
)
data class FilterHistoryItem(
    @ColumnInfo(name = "uid") @PrimaryKey val uid: Int? = null,
    @ColumnInfo(name = "type") val type: ItemType,
    @ColumnInfo(name = "text") val text: String? = null,
    @ColumnInfo(name = "date_range_start") val dateRangeStart: Date? = null,
    @ColumnInfo(name = "date_range_end") val dateRangeEnd: Date? = null,
    @ColumnInfo(name = "date_range_name") val dateRangeName: DateRangeOption.DateRangeName? = null,
    @ColumnInfo(name = "category") val category: ActivityCategory? = null,
    @ColumnInfo(name = "type_id") val typeId: Int? = null,
    @ColumnInfo(name = "place_id") val placeId: Int? = null,
    @ColumnInfo(name = "vehicle") val vehicle: Vehicle? = null,
    @ColumnInfo(name = "attribute") val attribute: AttributeFilter.Attribute? = null,
    @ColumnInfo(name = "attribute_state") val attributeState: Boolean? = null,
    @ColumnInfo(name = "last_updated") val lastUpdated: Date = Date.from(Instant.now()),
) {
    enum class ItemType {
        Text,
        Date,
        Category,
        Type,
        Place,
        Vehicle,
        Attribute
    }
}