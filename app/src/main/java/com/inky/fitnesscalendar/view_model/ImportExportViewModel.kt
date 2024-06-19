package com.inky.fitnesscalendar.view_model

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inky.fitnesscalendar.AppRepository
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.Activity
import com.inky.fitnesscalendar.data.TypeActivity
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
            val types = repository.loadActivityTypes().associateBy { it.uid!! }

            _importing.emit(true)
            for (activity in activities) {
                val typeActivity = TypeActivity(activity, types[activity.typeId]!!)
                repository.saveActivity(typeActivity)
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