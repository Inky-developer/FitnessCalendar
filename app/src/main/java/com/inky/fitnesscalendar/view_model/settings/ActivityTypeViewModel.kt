package com.inky.fitnesscalendar.view_model.settings

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inky.fitnesscalendar.AppRepository
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.ActivityType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityTypeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: AppRepository
) :
    ViewModel() {
    val typeRows = repository.getActivityTypeRows()
    val snackbarHostState = SnackbarHostState()

    fun save(activityType: ActivityType) = viewModelScope.launch {
        repository.saveActivityType(activityType)
    }

    fun delete(activityType: ActivityType) {
        viewModelScope.launch {
            try {
                val result = snackbarHostState.showSnackbar(
                    message = context.getString(R.string.deleting_activity_type),
                    actionLabel = context.getString(R.string.cancel),
                    duration = SnackbarDuration.Short
                )
                when (result) {
                    SnackbarResult.ActionPerformed -> {}
                    SnackbarResult.Dismissed -> repository.deleteActivityType(activityType)
                }
            } catch (e: SQLiteConstraintException) {
                snackbarHostState.showSnackbar(message = context.getString(R.string.cannot_delete_type_because_there_are_still_activities))
            }
        }
    }
}