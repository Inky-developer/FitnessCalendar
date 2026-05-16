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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class ActivityLogViewModel @Inject constructor(
    @ApplicationContext context: Context,
    repository: DatabaseRepository
) : BaseViewModel(context, repository) {
    private val filterFlow = MutableStateFlow(ActivityFilter())

    private val dayFlow = repository.getDays()

    @OptIn(ExperimentalCoroutinesApi::class)
    val activityListState: StateFlow<ActivityListState> = filterFlow
        .flatMapLatest { filter ->
            combine(
                repository.getActivities(filter),
                dayFlow,
                repository.getDaysFiltered(filter),
            ) { activities, allDays, filteredDays ->
                ActivityListState(
                    data = ActivityListState.Data(
                        items = calculateActivityListItems(activities, allDays, filteredDays),
                        numActivities = activities.size,
                    ),
                    filter = filter,
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ActivityListState(data = null, filter = ActivityFilter()),
        )

    fun setFilter(filter: ActivityFilter) {
        filterFlow.value = filter
    }

    /**
     * Zips the activities and days into a single list of items.
     * Assumes that activities and days are sorted by descending dates.
     * filteredDays is the list of days matching the filter.
     * allDays will be used to include days of matching activities that are not necessarily
     * in the filteredDays list.
     */
    private fun calculateActivityListItems(
        activities: List<RichActivity>,
        allDays: Map<EpochDay, Day>,
        filteredDays: Set<EpochDay>,
    ): List<ActivityListItem> {
        val zoneId = ZoneId.systemDefault()

        val activitiesByDay = activities.groupBy { it.activity.epochDay(zoneId) }
        val orderedEpochDays = (filteredDays + activitiesByDay.keys).sortedDescending()

        val result = mutableListOf<ActivityListItem>()
        for (epochDay in orderedEpochDays) {
            val day = allDays[epochDay] ?: Day(epochDay)
            result.add(ActivityListItem.DateHeader(day))
            for (activity in activitiesByDay[epochDay].orEmpty()) {
                result.add(ActivityListItem.Activity(activity))
            }
        }
        return result
    }
}
