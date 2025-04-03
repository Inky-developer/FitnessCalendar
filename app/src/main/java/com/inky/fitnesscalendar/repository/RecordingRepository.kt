package com.inky.fitnesscalendar.repository

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Immutable
import com.inky.fitnesscalendar.db.dao.ActivityDao
import com.inky.fitnesscalendar.db.dao.ActivityTypeDao
import com.inky.fitnesscalendar.db.dao.RecordingDao
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.db.entities.Recording
import com.inky.fitnesscalendar.db.entities.RichRecording
import com.inky.fitnesscalendar.preferences.Preference
import com.inky.fitnesscalendar.util.getCurrentBssid
import com.inky.fitnesscalendar.util.hideRecordingNotification
import com.inky.fitnesscalendar.util.showRecordingNotification
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "RecordingRepository"

@Immutable
@Singleton
class RecordingRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val recordingDao: RecordingDao,
    private val activityTypeDao: ActivityTypeDao,
    private val activityDao: ActivityDao
) {
    suspend fun startRecording(richRecording: RichRecording) {
        val includeWifi = Preference.COLLECT_BSSID.get(context)
        val recording = if (includeWifi) {
            richRecording.recording.copy(wifiBssid = context.getCurrentBssid())
        } else {
            richRecording.recording
        }
        val recordingId = recordingDao.insert(recording).toInt()
        context.showRecordingNotification(
            recordingId,
            richRecording.type,
            richRecording.recording.startTime.time
        )
    }

    fun getRecordings() = recordingDao.getRecordings()

    suspend fun getRecording(uid: Int) = recordingDao.getById(uid)

    suspend fun deleteRecording(recording: Recording) {
        Log.d(TAG, "Deleting $recording")
        recording.uid?.let { context.hideRecordingNotification(it) }
        recordingDao.delete(recording)
    }

    suspend fun endRecording(recording: Recording): Boolean {
        Log.d(TAG, "Ending recording $recording")
        recording.uid?.let { context.hideRecordingNotification(it) }
        val type = activityTypeDao.get(recording.typeId)
        if (type == null) {
            Log.e(TAG, "Could not retrieve type for activity")
            return false
        }
        val activity = recording.toActivity(type)
        activityDao.stopRecording(recording, activity)
        return true
    }

    suspend fun endAllRecordingsOfType(type: ActivityType): Int {
        if (type.uid == null) return 0

        var stoppedRecordings = 0
        for (recording in recordingDao.loadRecordingsOfType(type.uid)) {
            val success = endRecording(recording)
            if (success) {
                stoppedRecordings += 1
            }
        }

        return stoppedRecordings
    }
}