package com.inky.fitnesscalendar.view_model

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.inky.fitnesscalendar.repository.AppRepository
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilter
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilterChip.Companion.toActivityFilterChip
import com.inky.fitnesscalendar.db.entities.RichActivity
import com.inky.fitnesscalendar.util.toLocalDate
import com.inky.fitnesscalendar.view_model.activity_log.ActivityListItem
import com.inky.fitnesscalendar.view_model.activity_log.ActivityListState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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

    private val _activityListState = MutableStateFlow(
        ActivityListState(
            items = emptyList(),
            activities = emptyList(),
            days = emptyMap(),
            filter = ActivityFilter(),
            isInitialized = false
        )
    )
    val activityListState: StateFlow<ActivityListState> = _activityListState.asStateFlow()

    private val dayFlow = repository.getDays()

    private var activityListStateUpdateJob: Job? = null

    init {
        updateJob(ActivityFilter())
    }

    fun setFilter(filter: ActivityFilter) {
        updateJob(filter)
    }

    private fun updateJob(filter: ActivityFilter) = viewModelScope.launch(Dispatchers.IO) {
        activityListStateUpdateJob?.cancelAndJoin()
        activityListStateUpdateJob = repository
            .getActivities(filter)
            .combine(dayFlow) { activities, days ->
                _activityListState.value.copy(
                    items = calculateActivityListItems(activities),
                    activities = activities,
                    days = days,
                    filter = filter,
                    isInitialized = true
                )
            }
            .onEach { newState ->
                _activityListState.emit(newState)
            }
            .launchIn(viewModelScope)
    }

    private fun calculateActivityListItems(
        activities: List<RichActivity>,
    ): List<ActivityListItem> {
        val result = mutableListOf<ActivityListItem>()

        val zoneId = ZoneId.systemDefault()
        val activitiesByDay = activities.groupBy { it.activity.startTime.toLocalDate(zoneId) }

        for ((day, items) in activitiesByDay) {
            result.add(ActivityListItem.DateHeader(day))
            result.addAll(items.map { ActivityListItem.Activity(it) })
        }

        return result
    }
}