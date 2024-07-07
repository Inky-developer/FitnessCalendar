package com.inky.fitnesscalendar.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inky.fitnesscalendar.AppRepository
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilter
import com.inky.fitnesscalendar.db.entities.Day
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GenericViewModel @Inject constructor(val repository: AppRepository) : ViewModel() {
    fun addToFilterHistory(filter: ActivityFilter) {
        viewModelScope.launch {
            repository.upsertFilterHistoryChips(filter.items())
        }
    }

    fun saveDay(day: Day) = viewModelScope.launch {
        repository.saveDay(day)
    }
}