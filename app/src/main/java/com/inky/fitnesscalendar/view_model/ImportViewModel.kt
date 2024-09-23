package com.inky.fitnesscalendar.view_model

import android.content.Context
import android.content.Intent
import android.os.ParcelFileDescriptor
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inky.fitnesscalendar.MainActivity
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.repository.DatabaseRepository
import com.inky.fitnesscalendar.repository.ImportRepository
import com.inky.fitnesscalendar.util.EXTRA_TOAST
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImportViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val dbRepository: DatabaseRepository,
    private val importRepository: ImportRepository
) : ViewModel() {
    var closeActivity: () -> Unit = {}

    private val _tracks = MutableStateFlow(emptyList<ImportRepository.ImportTrack>())
    val tracks get() = _tracks.asStateFlow()

    private val _done = mutableStateOf(false)
    val done: State<Boolean> get() = _done

    private val _error = mutableStateOf(false)
    val error: State<Boolean> get() = _error

    fun import(importTracks: List<Pair<ImportRepository.ImportTrack, ActivityType>>) =
        viewModelScope.launch(Dispatchers.Default) {
            var numImportedActivities = 0

            for ((importTrack, activityType) in importTracks) {
                val result = importRepository.importTrack(importTrack, activityType)
                if (result != null) {
                    numImportedActivities += 1
                }
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_TASK_ON_HOME or Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra(
                    EXTRA_TOAST,
                    context.resources.getQuantityString(
                        R.plurals.imported_activities,
                        numImportedActivities,
                        numImportedActivities
                    )
                )
            }
            context.startActivity(intent)
            closeActivity()
        }

    fun updateTypeMapping(key: String, value: ActivityType) =
        viewModelScope.launch(Dispatchers.IO) {
            dbRepository.setActivityTypeName(key, value)
        }

    fun loadFiles(files: List<ParcelFileDescriptor>) = viewModelScope.launch(Dispatchers.IO) {
        _done.value = false

        val loadedTracks = importRepository.loadFiles(files)
        if (loadedTracks.isEmpty()) {
            _error.value = true
        }

        _tracks.emit(_tracks.value.toMutableList().apply { addAll(loadedTracks) })

        _done.value = true
    }
}