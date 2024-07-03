package com.inky.fitnesscalendar.view_model

import android.content.Context
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inky.fitnesscalendar.AppRepository
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilter
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilterChip.Companion.toActivityFilterChip
import com.inky.fitnesscalendar.db.entities.Activity
import com.inky.fitnesscalendar.db.entities.TypeActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityLogViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    val repository: AppRepository
) : ViewModel() {
    val snackbarHostState = SnackbarHostState()

    val filterHistory = repository.getFilterHistoryItems()
        .map { item -> item.mapNotNull { it.toActivityFilterChip() } }

    private val _activities = MutableStateFlow(emptyList<TypeActivity>())
    val activities = _activities.asStateFlow()

    var activityListState = mutableStateOf(LazyListState())

    private var activityUpdateJob: Job? = null

    init {
        activityUpdateJob = repository.getActivities(ActivityFilter()).onEach { activityList ->
            _activities.emit(activityList)
        }.launchIn(viewModelScope)
    }

    fun setFilter(filter: ActivityFilter) = viewModelScope.launch {
        activityUpdateJob?.cancelAndJoin()
        activityUpdateJob = repository.getActivities(filter).onEach { activityList ->
            _activities.emit(activityList)
        }.launchIn(viewModelScope)
    }

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
}