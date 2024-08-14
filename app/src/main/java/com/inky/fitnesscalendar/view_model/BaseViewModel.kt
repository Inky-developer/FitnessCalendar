package com.inky.fitnesscalendar.view_model

import android.content.Context
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inky.fitnesscalendar.AppRepository
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilter
import com.inky.fitnesscalendar.db.entities.Day
import com.inky.fitnesscalendar.db.entities.RichActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class BaseViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val repository: AppRepository
) : ViewModel() {
    val snackbarHostState = SnackbarHostState()

    fun addToFilterHistory(filter: ActivityFilter) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.upsertFilterHistoryChips(filter.items())
        }
    }

    fun saveDay(day: Day) = viewModelScope.launch(Dispatchers.IO) {
        repository.saveDay(day)
    }

    fun deleteActivity(richActivity: RichActivity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteActivity(richActivity.activity)
            val result = snackbarHostState.showSnackbar(
                context.getString(R.string.deleted_activity),
                actionLabel = context.getString(R.string.undo),
                duration = SnackbarDuration.Short
            )
            when (result) {
                SnackbarResult.ActionPerformed -> repository.saveActivity(richActivity)
                SnackbarResult.Dismissed -> {}
            }
        }
    }
}