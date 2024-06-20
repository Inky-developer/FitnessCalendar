package com.inky.fitnesscalendar.view_model.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inky.fitnesscalendar.AppRepository
import com.inky.fitnesscalendar.data.ActivityType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityTypeViewModel @Inject constructor(private val repository: AppRepository) : ViewModel() {
    val typeRows = repository.getActivityTypeRows()

    fun save(activityType: ActivityType) = viewModelScope.launch {
        repository.saveActivityType(activityType)
    }
}