package com.inky.fitnesscalendar.view_model

import android.content.Context
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inky.fitnesscalendar.AppRepository
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.ActivityStatistics
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilter
import com.inky.fitnesscalendar.data.activity_filter.DateRangeOption
import com.inky.fitnesscalendar.db.entities.Activity
import com.inky.fitnesscalendar.db.entities.Recording
import com.inky.fitnesscalendar.util.Duration.Companion.until
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    val repository: AppRepository
) : ViewModel() {
    val weekStats = loadWeekStats()
    val monthStats = loadMonthStats()
    val activitiesToday = repository.getActivities(ActivityFilter(range = DateRangeOption.Today))
    val mostRecentActivity = repository.getMostRecentActivity().map { typeActivity ->
        if (typeActivity?.activity?.let { it.endTime.until(Date.from(Instant.now())).elapsedHours < 2.0 } == true) {
            typeActivity
        } else {
            null
        }
    }
    val recordings = repository.getRecordings()

    val snackbarHostState = SnackbarHostState()

    fun deleteActivity(activity: Activity) {
        viewModelScope.launch {
            val result = snackbarHostState.showSnackbar(
                context.getString(R.string.deleting_activity),
                actionLabel = context.getString(R.string.abort),
                duration = SnackbarDuration.Short
            )
            when (result) {
                SnackbarResult.ActionPerformed -> {}
                SnackbarResult.Dismissed -> repository.deleteActivity(activity)
            }
        }
    }

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
        val filter = ActivityFilter(range = DateRangeOption.FourWeeks)
        return repository.getActivities(filter).map { activities ->
            ActivityStatistics(activities)
        }
    }
}