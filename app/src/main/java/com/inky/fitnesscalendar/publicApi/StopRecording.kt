package com.inky.fitnesscalendar.publicApi

import android.widget.Toast
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.di.appContext

class StopRecording : ApiActivity() {
    private val recordingRepository = appContext.recordingRepository
    private val databaseRepository = appContext.databaseRepo

    override suspend fun handleRequest() {
        val activityType = extractActivityTypeByName(intent, databaseRepository) ?: return

        val stoppedRecordings = recordingRepository.endAllRecordingsOfType(activityType)
        runOnUiThread {
            Toast.makeText(
                this,
                resources.getQuantityString(
                    R.plurals.success_stopped_n_recordings,
                    stoppedRecordings,
                    stoppedRecordings
                ),
                Toast.LENGTH_LONG
            ).show()
        }

        finishAndRemoveTask()
    }
}
