package com.inky.fitnesscalendar.broadcast_receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.inky.fitnesscalendar.repository.DatabaseRepository
import com.inky.fitnesscalendar.util.ACTION_CANCEL
import com.inky.fitnesscalendar.util.ACTION_SAVE
import com.inky.fitnesscalendar.util.EXTRA_RECORDING_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "NotificationPodcastReceiver"

@AndroidEntryPoint
class NotificationBroadcastReceiver : BroadcastReceiver() {
    @Inject
    lateinit var repository: DatabaseRepository

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, intent?.toUri(Intent.URI_INTENT_SCHEME) ?: "Intent was null")

        if (intent == null) return

        val id = intent.getIntExtra(EXTRA_RECORDING_ID, -1)

        // TODO: Looks like the recommended solution is to use workers
        val pendingResult = goAsync()
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch {
            try {
                Log.d(TAG, "Received ${intent.action} with id $id")
                val recording = repository.getRecording(id)!!
                Log.d(TAG, "id resolved to $recording")
                when (intent.action) {
                    ACTION_CANCEL -> repository.deleteRecording(recording)
                    ACTION_SAVE -> repository.endRecording(recording)
                    else -> {}
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}