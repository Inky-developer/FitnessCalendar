package com.inky.fitnesscalendar.view_model

import androidx.lifecycle.viewModelScope
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.di.AppContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsViewModel(app: AppContext) : BaseViewModel(app) {
    private val importRepository = app.importRepository

    fun recalculateTrackData() = viewModelScope.launch(Dispatchers.Default) {
        val numUpdatedActivities = importRepository.updateTrackActivities()

        snackbarHostState.showSnackbar(
            context.resources.getQuantityString(
                R.plurals.updated_activities,
                numUpdatedActivities,
                numUpdatedActivities
            ),
        )
    }
}
