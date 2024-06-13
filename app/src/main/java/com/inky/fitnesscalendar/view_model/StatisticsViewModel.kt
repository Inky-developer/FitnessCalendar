package com.inky.fitnesscalendar.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inky.fitnesscalendar.AppRepository
import com.inky.fitnesscalendar.data.Activity
import com.inky.fitnesscalendar.data.ActivityFilter
import com.inky.fitnesscalendar.data.ActivityStatistics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(val appRepository: AppRepository) : ViewModel() {
    private var _filter = ActivityFilter()
    var filter
        get() = _filter
        set(newFilter) {
            _filter = newFilter
            refreshActivities()
        }

    private val _activities = MutableStateFlow<List<Activity>?>(null)
    val activityStatistics get() = _activities.filterNotNull().map { ActivityStatistics(it) }

    init {
        refreshActivities()
    }

    private fun refreshActivities() {
        viewModelScope.launch {
            _activities.value =
                appRepository.getActivities(filter).shareIn(viewModelScope, SharingStarted.Eagerly)
                    .first()
        }
    }
}