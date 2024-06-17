package com.inky.fitnesscalendar.data.activity_filter

import android.os.Parcelable
import com.inky.fitnesscalendar.data.ActivityCategory
import com.inky.fitnesscalendar.data.ActivityType
import kotlinx.parcelize.Parcelize


@Parcelize
data class ActivityFilter(
    val types: List<ActivityType> = emptyList(),
    val categories: List<ActivityCategory> = emptyList(),
    val text: String? = null,
    val range: DateRangeOption? = null,
    val attributes: AttributeFilter = AttributeFilter(),
) : Parcelable {
    fun isEmpty() = this == ActivityFilter()
}