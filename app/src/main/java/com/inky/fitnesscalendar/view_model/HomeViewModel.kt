package com.inky.fitnesscalendar.view_model

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.inky.fitnesscalendar.repository.DatabaseRepository
import com.inky.fitnesscalendar.data.ActivityStatistics
import com.inky.fitnesscalendar.data.EpochDay
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilter
import com.inky.fitnesscalendar.data.activity_filter.DateRangeOption
import com.inky.fitnesscalendar.data.measure.Duration.Companion.until
import com.inky.fitnesscalendar.db.entities.Day
import com.inky.fitnesscalendar.db.entities.Recording
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext context: Context,
    repository: DatabaseRepository
) : BaseViewModel(context, repository) {
    val weekStats = loadWeekStats()
    val monthStats = loadMonthStats()
    val activitiesToday = repository.getDayActivities(EpochDay.today())

    private val _today = MutableStateFlow(Day(day = EpochDay.today()))
    val today: StateFlow<Day> = _today.asStateFlow()

    val mostRecentActivity = repository.getMostRecentActivity().map { typeActivity ->
        if (typeActivity?.activity?.let { it.endTime.until(Date.from(Instant.now())).elapsedHours < 2.0 } == true) {
            typeActivity
        } else {
            null
        }
    }
    val recordings = repository.getRecordings()

    init {
        repository.getDay(EpochDay.today()).onEach { _today.emit(it) }.launchIn(viewModelScope)
    }

    fun updateDay(day: Day) = viewModelScope.launch(Dispatchers.IO) {
        repository.saveDay(day)
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
        val filter = ActivityFilter(range = DateRangeOption.sevenDays())
        return repository.getActivities(filter).map { activities ->
            ActivityStatistics(activities)
        }
    }

    private fun loadMonthStats(): Flow<ActivityStatistics> {
        val filter = ActivityFilter(range = DateRangeOption.fourWeeks())
        return repository.getActivities(filter).map { activities ->
            ActivityStatistics(activities)
        }
    }
}