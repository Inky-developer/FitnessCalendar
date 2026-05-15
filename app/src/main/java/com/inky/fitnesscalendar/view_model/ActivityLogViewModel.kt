package com.inky.fitnesscalendar.view_model

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilter
import com.inky.fitnesscalendar.db.entities.Day
import com.inky.fitnesscalendar.db.entities.RichActivity
import com.inky.fitnesscalendar.repository.DatabaseRepository
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
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class ActivityLogViewModel @Inject constructor(
    @ApplicationContext context: Context,
    repository: DatabaseRepository
) : BaseViewModel(context, repository) {
    private val _activityListState = MutableStateFlow(
        ActivityListState(
            items = emptyList(),
            numActivities = 0,
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
                    items = calculateActivityListItems(activities, days),
                    numActivities = activities.size,
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
        days: List<Day>
    ): List<ActivityListItem> {
        val daysWithData = days.map { it.day }.toSet()

        val dayIter = days.iterator()
        var day = if (dayIter.hasNext()) dayIter.next() else null

        val zoneId = ZoneId.systemDefault()

        return activities.flatMap { activity ->
            sequence {
                while (day != null && day!!.day >= activity.activity.epochDay(zoneId)) {
                    yield(ActivityListItem.DateHeader(day!!))
                    day = if (dayIter.hasNext()) dayIter.next() else null
                }

                val activityDay = activity.activity.epochDay(zoneId)
                if (!daysWithData.contains(activityDay)) {
                    yield(ActivityListItem.DateHeader(Day(activityDay)))
                }

                yield(ActivityListItem.Activity(activity))
            }
        }
    }
}