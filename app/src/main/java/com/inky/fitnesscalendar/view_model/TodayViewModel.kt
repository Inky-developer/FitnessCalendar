package com.inky.fitnesscalendar.view_model

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.inky.fitnesscalendar.AppRepository
import com.inky.fitnesscalendar.data.ActivityFilter
import com.inky.fitnesscalendar.data.ActivityStatistics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val handle: SavedStateHandle,
    val repository: AppRepository
) : ViewModel() {
    val weekStats = loadWeekStats()
    val monthStats = loadMonthStats()
    val activitiesToday = repository.getActivities(ActivityFilter.atDay(Instant.now()))

    private fun loadWeekStats(): Flow<ActivityStatistics> {
        val filter = ActivityFilter(
            startRangeDate = Date.from(
                Instant.ofEpochMilli(
                    Instant.now().toEpochMilli() - ChronoUnit.WEEKS.duration.toMillis() * 7
                )
            )
        )
        return repository.getActivities(filter).map { activities ->
            ActivityStatistics(activities)
        }
    }

    private fun loadMonthStats(): Flow<ActivityStatistics> {
        val filter = ActivityFilter(
            startRangeDate = Date.from(
                Instant.ofEpochMilli(
                    Instant.now().toEpochMilli() - ChronoUnit.MONTHS.duration.toMillis()
                )
            )
        )
        return repository.getActivities(filter).map { activities ->
            ActivityStatistics(activities)
        }
    }
}