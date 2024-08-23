package com.inky.fitnesscalendar.view_model

import android.content.Context
import android.content.Intent
import android.os.ParcelFileDescriptor
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inky.fitnesscalendar.AppRepository
import com.inky.fitnesscalendar.MainActivity
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.gpx.GpxTrack
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.db.entities.RichActivity
import com.inky.fitnesscalendar.db.entities.Track
import com.inky.fitnesscalendar.util.EXTRA_TOAST
import com.inky.fitnesscalendar.util.gpx.GpxReader
import com.inky.fitnesscalendar.view_model.import.ImportTrack
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.InputStream
import javax.inject.Inject

@HiltViewModel
class ImportViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val repository: AppRepository
) : ViewModel() {
    var closeActivity: () -> Unit = {}

    private val _tracks = MutableStateFlow(emptyList<ImportTrack>())
    val tracks get() = _tracks.asStateFlow()

    private val _done = mutableStateOf(false)
    val done: State<Boolean> get() = _done

    private val _error = mutableStateOf(false)
    val error: State<Boolean> get() = _error

    fun import(activities: List<Pair<RichActivity, GpxTrack>>) =
        viewModelScope.launch(Dispatchers.Default) {
            for ((richActivity, gpxTrack) in activities) {
                val activityId = repository.saveActivity(richActivity)

                val track = Track(activityId = activityId, points = gpxTrack.points)
                repository.saveTrack(track)
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_TASK_ON_HOME or Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra(
                    EXTRA_TOAST,
                    context.resources.getQuantityString(
                        R.plurals.imported_activities,
                        activities.size,
                        activities.size
                    )
                )
            }
            context.startActivity(intent)
            closeActivity()
        }

    fun updateTypeMapping(key: String, value: ActivityType) =
        viewModelScope.launch(Dispatchers.IO) {
            repository.setActivityTypeName(key, value)
        }

    fun loadFiles(files: List<ParcelFileDescriptor>) = viewModelScope.launch(Dispatchers.IO) {
        _done.value = false
        for (descriptor in files) {
            descriptor.use {
                val actualDescriptor = descriptor.fileDescriptor
                val stream = FileInputStream(actualDescriptor)
                loadFile(stream)
            }
        }

        if (tracks.value.isEmpty()) {
            _error.value = true
        }

        _done.value = true
    }

    private suspend fun loadFile(stream: InputStream) {
        val gpx = GpxReader.read(stream)
        if (gpx != null) {
            _tracks.emit(
                _tracks.value.toMutableList().apply { addAll(gpx.tracks.map { ImportTrack(it) }) }
            )
        }
    }
}