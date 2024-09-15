package com.inky.fitnesscalendar.view_model.settings

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityTypeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: AppRepository
) : ViewModel() {
    val snackbarHostState = SnackbarHostState()

    fun save(activityType: ActivityType) = viewModelScope.launch(Dispatchers.IO) {
        repository.saveActivityType(activityType)
    }

    fun delete(activityType: ActivityType) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteActivityType(activityType)
                val result = snackbarHostState.showSnackbar(
                    message = context.getString(R.string.deleted_activity_type),
                    actionLabel = context.getString(R.string.undo),
                    duration = SnackbarDuration.Short
                )
                when (result) {
                    SnackbarResult.ActionPerformed -> repository.saveActivityType(activityType)
                    SnackbarResult.Dismissed -> {}
                }
            } catch (e: SQLiteConstraintException) {
                snackbarHostState.showSnackbar(message = context.getString(R.string.cannot_delete_type_because_there_are_still_activities))
            }
        }
    }
}