package com.inky.fitnesscalendar.view_model

import android.content.Context
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.inky.fitnesscalendar.AppRepository
import com.inky.fitnesscalendar.data.EpochDay
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilter
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilterChip.Companion.toActivityFilterChip
import com.inky.fitnesscalendar.db.entities.Day
import com.inky.fitnesscalendar.db.entities.RichActivity
import com.inky.fitnesscalendar.util.toLocalDate
import com.inky.fitnesscalendar.view_model.activity_log.ActivityListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class ActivityLogViewModel @Inject constructor(
    @ApplicationContext context: Context,
    repository: AppRepository
) : BaseViewModel(context, repository) {
    val filterHistory = repository.getFilterHistoryItems()
        .map { item -> item.mapNotNull { it.toActivityFilterChip() } }

    private val _days = MutableStateFlow(emptyMap<EpochDay, Day>())
    val days = _days.asStateFlow()

    private val _activities = MutableStateFlow(emptyList<RichActivity>())
    val activities = _activities.asStateFlow()

    val activityListItems: Flow<List<ActivityListItem>> = activities.map { activityList ->
        val result = mutableListOf<ActivityListItem>()

        val zoneId = ZoneId.systemDefault()
        val days = activityList.groupBy { it.activity.startTime.toLocalDate(zoneId) }

        for ((day, items) in days) {
            result.add(ActivityListItem.DateHeader(day))
            result.addAll(items.map { ActivityListItem.Activity(it) })
        }

        result
    }

    var activityListState = mutableStateOf(LazyListState())

    private var activityUpdateJob: Job? = null

    init {
        activityUpdateJob = repository.getActivities(ActivityFilter()).onEach { activityList ->
            _activities.emit(activityList)
        }.launchIn(viewModelScope)

        // TODO: Only select the required days
        repository.getDays().onEach { _days.emit(it) }.launchIn(viewModelScope)
    }

    fun setFilter(filter: ActivityFilter) = viewModelScope.launch {
        activityUpdateJob?.cancelAndJoin()
        activityUpdateJob = repository.getActivities(filter).onEach { activityList ->
            _activities.emit(activityList)
        }.launchIn(viewModelScope)
    }
}