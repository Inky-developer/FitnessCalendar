package com.inky.fitnesscalendar.view_model

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inky.fitnesscalendar.AppRepository
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.Activity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImportExportViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val repository: AppRepository
) : ViewModel() {
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    private val _importing = MutableSharedFlow<Boolean>()
    val importing = _importing.asSharedFlow()

    fun import(activities: List<Activity>) {
        viewModelScope.launch(Dispatchers.IO) {
            _importing.emit(true)
            for (activity in activities) {
                repository.saveActivity(activity)
            }
            _toastMessage.emit(
                context.resources.getQuantityString(
                    R.plurals.imported_activities,
                    activities.size,
                    activities.size
                )
            )
            _importing.emit(false)
        }
    }
}