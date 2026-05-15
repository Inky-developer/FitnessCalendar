package com.inky.fitnesscalendar.view_model.activity_log

import androidx.compose.foundation.lazy.LazyListState
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilter

data class ActivityListState(
    // All items that should be displayed
    val data: Data?,
    val filter: ActivityFilter,
    val listState: LazyListState = LazyListState()
) {
    data class Data(
        val items: List<ActivityListItem>,
        val numActivities: Int,
    )

    suspend fun scrollToActivity(activityId: Int?): Boolean {
        return getActivityIndex(activityId)?.let { listState.scrollToItem(it) } != null
    }

    private fun getActivityIndex(activityId: Int?) = data?.items?.withIndex()
        ?.firstOrNull { (_, item) -> item is ActivityListItem.Activity && item.richActivity.activity.uid == activityId }?.index
}