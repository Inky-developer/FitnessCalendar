package com.inky.fitnesscalendar.view_model

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inky.fitnesscalendar.AppRepository
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.util.importCsv
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

    fun import(importData: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val types = repository.loadActivityTypes()

            val typeActivities = importCsv(importData, types)

            _importing.emit(true)
            for (typeActivity in typeActivities) {
                repository.saveActivity(typeActivity)
            }
            _toastMessage.emit(
                context.resources.getQuantityString(
                    R.plurals.imported_activities,
                    typeActivities.size,
                    typeActivities.size
                )
            )
            _importing.emit(false)
        }
    }
}