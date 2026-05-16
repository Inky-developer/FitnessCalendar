package com.inky.fitnesscalendar.view_model

import android.content.Context
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilter
import com.inky.fitnesscalendar.db.entities.Day
import com.inky.fitnesscalendar.db.entities.RichActivity
import com.inky.fitnesscalendar.di.AppContext
import com.inky.fitnesscalendar.repository.DatabaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class BaseViewModel(val app: AppContext) : ViewModel() {
    val context: Context get() = app.context
    val repository: DatabaseRepository get() = app.databaseRepo
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
