package com.inky.fitnesscalendar.view_model

import androidx.lifecycle.ViewModel
import com.inky.fitnesscalendar.AppRepository
import com.inky.fitnesscalendar.data.ActivityFilter
import com.inky.fitnesscalendar.data.ActivityStatistics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(val appRepository: AppRepository) : ViewModel() {
    val activities = appRepository.getActivities(ActivityFilter())

    val activityStatistics = activities.map { ActivityStatistics(it) }
}