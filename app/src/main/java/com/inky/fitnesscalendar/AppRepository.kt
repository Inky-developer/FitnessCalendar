package com.inky.fitnesscalendar

import android.content.Context
import androidx.compose.runtime.Stable
import com.inky.fitnesscalendar.data.Activity
import com.inky.fitnesscalendar.data.ActivityFilter
import com.inky.fitnesscalendar.data.ActivityType
import com.inky.fitnesscalendar.data.Recording
import com.inky.fitnesscalendar.data.Vehicle
import com.inky.fitnesscalendar.db.dao.ActivityDao
import com.inky.fitnesscalendar.db.dao.RecordingDao
import com.inky.fitnesscalendar.localization.LocalizationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Stable
class AppRepository @Inject constructor(
    @ApplicationContext val context: Context,
    private val activityDao: ActivityDao,
    private val recordingDao: RecordingDao,
    val localizationRepository: LocalizationRepository
) {
    suspend fun loadAllActivities() = activityDao.loadActivities()

    fun getActivities(filter: ActivityFilter): Flow<List<Activity>> {
        // Use simpler query if no filters are needed
        if (filter.isEmpty()) {
            return activityDao.getActivities()
        }

        val searchTypes = filter.text?.let { filterText ->
            ActivityType.entries.filter { type ->
                context.getString(type.nameId).contains(filterText, ignoreCase = true)
            }
        } ?: emptyList()

        val searchVehicles = filter.text?.let { filterText ->
            Vehicle.entries.filter { vehicle ->
                context.getString(vehicle.nameId).contains(filterText, ignoreCase = true)
            }
        } ?: emptyList()

        return activityDao.getFiltered(
            filter.types,
            filter.types.isEmpty(),
            filter.text?.let { "%$it%" },
            searchTypes,
            searchVehicles,
            filter.startRangeDate,
            filter.endRangeDate
        )
    }

    suspend fun saveActivity(activity: Activity) {
        activityDao.save(activity.clean())
    }

    suspend fun deleteActivity(activity: Activity) {
        activityDao.delete(activity)
    }

    fun getActivity(id: Int) = activityDao.get(id)

    suspend fun startRecording(recording: Recording) {
        recordingDao.insert(recording)
    }

    fun getRecordings() = recordingDao.getRecordings()

    suspend fun deleteRecording(recording: Recording) = recordingDao.delete(recording)

    suspend fun endRecording(recording: Recording) {
        val activity = recording.toActivity()
        activityDao.stopRecording(recording, activity)
    }
}