package com.inky.fitnesscalendar.publicApi

import androidx.activity.compose.setContent
import androidx.compose.runtime.rememberCoroutineScope
import com.inky.fitnesscalendar.repository.DatabaseRepository
import com.inky.fitnesscalendar.repository.RecordingRepository
import com.inky.fitnesscalendar.ui.components.AppFrame
import com.inky.fitnesscalendar.ui.util.ProvideDatabaseValues
import com.inky.fitnesscalendar.ui.views.QsTileRecordActivityDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Enables third party applications to show the recording dialog using intents
 */
@AndroidEntryPoint
class CreateRecording : ApiActivity() {
    @Inject
    lateinit var databaseRepository: DatabaseRepository

    @Inject
    lateinit var recordingRepository: RecordingRepository

    override suspend fun handleRequest() = runOnUiThread {
        setContent {
            val scope = rememberCoroutineScope()
            AppFrame {
                ProvideDatabaseValues(repository = databaseRepository) {
                    QsTileRecordActivityDialog(
                        localizationRepository = databaseRepository.localizationRepository,
                        onSave = {
                            scope.launch {
                                recordingRepository.startRecording(it)
                                finishAndRemoveTask()
                            }
                        },
                        onDismiss = { finishAndRemoveTask() }
                    )
                }
            }
        }
    }
}