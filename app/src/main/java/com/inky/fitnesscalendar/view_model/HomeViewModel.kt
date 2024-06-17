package com.inky.fitnesscalendar.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inky.fitnesscalendar.AppRepository
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilter
import com.inky.fitnesscalendar.data.ActivityStatistics
import com.inky.fitnesscalendar.data.activity_filter.DateRangeOption
import com.inky.fitnesscalendar.data.Recording
import com.inky.fitnesscalendar.util.Duration.Companion.until
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(val repository: AppRepository) : ViewModel() {
    val weekStats = loadWeekStats()
    val monthStats = loadMonthStats()
    val activitiesToday = repository.getActivities(ActivityFilter(range = DateRangeOption.Today))
    val mostRecentActivity = repository.getMostRecentActivity().map { activity ->
        if (activity?.let { it.endTime.until(Date.from(Instant.now())).elapsedHours < 2.0 } == true) {
            activity
        } else {
            null
        }
    }
    val recordings = repository.getRecordings()

    fun abortRecording(recording: Recording) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteRecording(recording)
        }
    }

    fun saveRecording(recording: Recording) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.endRecording(recording)
        }
    }

    private fun loadWeekStats(): Flow<ActivityStatistics> {
        val filter = ActivityFilter(range = DateRangeOption.SevenDays)
        return repository.getActivities(filter).map { activities ->
            ActivityStatistics(activities)
        }
    }

    private fun loadMonthStats(): Flow<ActivityStatistics> {
        val filter = ActivityFilter(range = DateRangeOption.ThirtyDays)
        return repository.getActivities(filter).map { activities ->
            ActivityStatistics(activities)
        }
    }
}