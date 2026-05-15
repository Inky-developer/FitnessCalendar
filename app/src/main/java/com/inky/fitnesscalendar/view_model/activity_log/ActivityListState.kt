package com.inky.fitnesscalendar.view_model.activity_log

import com.inky.fitnesscalendar.data.activity_filter.ActivityFilter

data class ActivityListState(
    // All items that should be displayed
    val data: Data?,
    val filter: ActivityFilter,
) {
    data class Data(
        val items: List<ActivityListItem>,
        val numActivities: Int,
    )
}