package com.inky.fitnesscalendar.view_model

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.inky.fitnesscalendar.data.EpochDay
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
            data = null,
            filter = ActivityFilter(),
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
                    data = ActivityListState.Data(
                        items = calculateActivityListItems(
                            activities,
                            days,
                            isUnfiltered = filter.isEmpty()
                        ),
                        numActivities = activities.size
                    ),
                    filter = filter,
                )
            }
            .onEach { newState ->
                _activityListState.emit(newState)
            }
            .launchIn(viewModelScope)
    }

    /**
     * Zips the activities and days into a single list of items.
     * Assumes that activities and days are sorted by descending dates.
     * Excludes days without activities if a filter is active.
     * TODO: Consider allowing filtering for days, e.g by text
     */
    private fun calculateActivityListItems(
        activities: List<RichActivity>,
        days: List<Day>,
        isUnfiltered: Boolean,
    ): List<ActivityListItem> {
        val dayIter = days.iterator()
        var day = if (dayIter.hasNext()) dayIter.next() else null
        var lastDay: EpochDay? = null

        val zoneId = ZoneId.systemDefault()

        val result = activities.flatMap { activity ->
            sequence {
                val activityDay = activity.activity.epochDay(zoneId)

                while (day != null && day!!.day >= activity.activity.epochDay(zoneId)) {
                    if (isUnfiltered || day?.day == activityDay) {
                        yield(ActivityListItem.DateHeader(day!!))
                    }
                    lastDay = day?.day
                    day = if (dayIter.hasNext()) dayIter.next() else null
                }

                if (lastDay != activityDay) {
                    yield(ActivityListItem.DateHeader(Day(activityDay)))
                    lastDay = activityDay
                }

                yield(ActivityListItem.Activity(activity))
            }
        }.toMutableList()
        if (isUnfiltered) {
            day?.let { result.add(ActivityListItem.DateHeader(it)) }
            result += dayIter.asSequence().map { ActivityListItem.DateHeader(it) }
        }
        return result
    }
}