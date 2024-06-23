package com.inky.fitnesscalendar.data.activity_filter

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.inky.fitnesscalendar.data.ActivityCategory
import com.inky.fitnesscalendar.data.ActivityType
import java.time.Instant
import java.util.Date

@Entity(
    foreignKeys = [ForeignKey(
        entity = ActivityType::class,
        parentColumns = arrayOf("uid"),
        childColumns = arrayOf("type_id"),
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("last_updated")]
)
data class FilterHistoryItem(
    @ColumnInfo(name = "uid") @PrimaryKey val uid: Int? = null,
    @ColumnInfo(name = "type") val type: ItemType,
    @ColumnInfo(name = "text") val text: String? = null,
    @ColumnInfo(name = "date_range_option") val dateRangeOption: DateRangeOption? = null,
    @ColumnInfo(name = "category") val category: ActivityCategory? = null,
    @ColumnInfo(name = "type_id") val typeId: Int? = null,
    @ColumnInfo(name = "attribute") val attribute: AttributeFilter.Attribute? = null,
    @ColumnInfo(name = "attribute_state") val attributeState: Boolean? = null,
    @ColumnInfo(name = "last_updated") val lastUpdated: Date = Date.from(Instant.now()),
) {
    enum class ItemType {
        Text,
        Date,
        Category,
        Type,
        Attribute
    }
}