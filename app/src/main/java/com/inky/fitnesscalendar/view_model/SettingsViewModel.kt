package com.inky.fitnesscalendar.view_model

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.di.AppRepository
import com.inky.fitnesscalendar.repository.ImportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext context: Context,
    app: AppRepository,
    private val importRepository: ImportRepository
) : BaseViewModel(context, app) {
    fun recalculateTrackData() = viewModelScope.launch(Dispatchers.Default) {
        val numUpdatedActivities = importRepository.updateTrackActivities()

        app.snackbarHostState.showSnackbar(
            context.resources.getQuantityString(
                R.plurals.updated_activities,
                numUpdatedActivities,
                numUpdatedActivities
            ),
        )
    }
}