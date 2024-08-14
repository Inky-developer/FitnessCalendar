package com.inky.fitnesscalendar.view_model

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inky.fitnesscalendar.AppRepository
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.db.entities.RichActivity
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

    private val _showImportDialog = MutableSharedFlow<Boolean>()
    val showImportDialog = _showImportDialog.asSharedFlow()

    private val _importData = MutableSharedFlow<List<RichActivity>>()
    val importData = _importData.asSharedFlow()

    fun import(importData: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _importing.emit(true)
            val types = repository.loadActivityTypes()
            val typeActivities = importCsv(importData, types)
            _importing.emit(false)

            _importData.emit(typeActivities)
            _showImportDialog.emit(typeActivities.isNotEmpty())
        }
    }

    fun dismissImportDialog() = viewModelScope.launch {
        _showImportDialog.emit(false)
        _importData.emit(emptyList())
    }

    fun confirmImport(data: List<RichActivity>) = viewModelScope.launch(Dispatchers.IO) {
        _showImportDialog.emit(false)
        for (typeActivity in data) {
            repository.saveActivity(typeActivity)
        }
        _importData.emit(emptyList())
        _toastMessage.emit(
            context.resources.getQuantityString(
                R.plurals.imported_activities,
                data.size,
                data.size
            )
        )
    }
}