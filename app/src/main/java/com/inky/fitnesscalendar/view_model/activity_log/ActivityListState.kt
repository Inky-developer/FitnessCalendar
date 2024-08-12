package com.inky.fitnesscalendar.view_model.activity_log

import androidx.compose.foundation.lazy.LazyListState
import com.inky.fitnesscalendar.data.EpochDay
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilter
import com.inky.fitnesscalendar.db.entities.Day
import com.inky.fitnesscalendar.db.entities.RichActivity

data class ActivityListState(
    // All items that should be displayed
    val items: List<ActivityListItem>,
    // All (potentially filtered) activities
    val activities: List<RichActivity>,
    // All relevant days
    val days: Map<EpochDay, Day>,
    val filter: ActivityFilter,
    val isInitialized: Boolean,
    val listState: LazyListState = LazyListState()
) {
    suspend fun scrollToActivity(activityId: Int?): Boolean {
        return getActivityIndex(activityId)?.let { listState.scrollToItem(it) } != null
    }

    private fun getActivityIndex(activityId: Int?) = items.withIndex()
        .firstOrNull { (_, item) -> item is ActivityListItem.Activity && item.richActivity.activity.uid == activityId }?.index
}