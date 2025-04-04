package com.inky.fitnesscalendar.publicApi

import android.widget.Toast
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.repository.DatabaseRepository
import com.inky.fitnesscalendar.repository.RecordingRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StopRecording : ApiActivity() {
    @Inject
    lateinit var recordingRepository: RecordingRepository

    @Inject
    lateinit var databaseRepository: DatabaseRepository

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

        finish()
    }
}