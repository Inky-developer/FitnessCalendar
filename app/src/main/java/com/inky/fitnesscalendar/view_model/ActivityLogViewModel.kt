package com.inky.fitnesscalendar.view_model

import android.content.Context
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inky.fitnesscalendar.AppRepository
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.Activity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityLogViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    val repository: AppRepository
) : ViewModel() {
    val snackbarHostState = SnackbarHostState()

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