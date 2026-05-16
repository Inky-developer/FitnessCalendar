package com.inky.fitnesscalendar.publicApi

import androidx.activity.compose.setContent
import androidx.compose.runtime.rememberCoroutineScope
import com.inky.fitnesscalendar.di.appContext
import com.inky.fitnesscalendar.ui.components.AppFrame
import com.inky.fitnesscalendar.ui.util.AppContextProviders
import com.inky.fitnesscalendar.ui.views.QsTileRecordActivityDialog
import kotlinx.coroutines.launch

/**
 * Enables third party applications to show the recording dialog using intents
 */
class CreateRecording : ApiActivity() {
    private val databaseRepository = appContext.databaseRepo
    private val recordingRepository = appContext.recordingRepository

    override suspend fun handleRequest() = runOnUiThread {
        setContent {
            val scope = rememberCoroutineScope()
            AppFrame {
                AppContextProviders(repository = databaseRepository) {
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
