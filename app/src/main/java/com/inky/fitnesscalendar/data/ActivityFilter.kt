package com.inky.fitnesscalendar.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class ActivityFilter(
    val types: List<ActivityType> = emptyList(),
    val categories: List<ActivityCategory> = emptyList(),
    val text: String? = null,
    val range: DateRangeOption? = null,
) : Parcelable {
    fun isEmpty() = this == ActivityFilter()
}