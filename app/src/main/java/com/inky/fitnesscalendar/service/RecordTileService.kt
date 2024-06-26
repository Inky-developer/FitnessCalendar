package com.inky.fitnesscalendar.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.TileService
import com.inky.fitnesscalendar.AppRepository
import com.inky.fitnesscalendar.MainActivity
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.db.entities.Recording
import com.inky.fitnesscalendar.db.entities.TypeRecording
import com.inky.fitnesscalendar.di.ActivityTypeDecisionTree
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class RecordTileService : TileService() {
    @Inject
    lateinit var repository: AppRepository

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private var currentActivityType: ActivityType? = null

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        maybeUnlock {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startActivityAndCollapse(
                    PendingIntent.getActivity(
                        this,
                        0,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                )
            } else {
                startActivity(intent)
            }
        }
    }

    private fun maybeUnlock(func: () -> Unit) {
        if (isSecure) {
            func()
        } else {
            unlockAndRun(func)
        }
    }

    private fun startRecording(type: ActivityType) = scope.launch {
        repository.startRecording(
            TypeRecording(
                recording = Recording(
                    typeId = type.uid!!,
                    startTime = Date.from(Instant.now())
                ),
                type = type
            ),
            this@RecordTileService
        )
    }

    override fun onClick() {
        super.onClick()

        // TODO: Show Dialog when not all data can be determined automatically
        when (val type = currentActivityType) {
            null -> startMainActivity()
            else -> {
                if (type.hasVehicle) {
                    startMainActivity()
                } else {
                    startRecording(type)
                }

            }
        }
    }

    override fun onStartListening() {
        super.onStartListening()

        currentActivityType = ActivityTypeDecisionTree.decisionTree?.classifyNow()
        val title = when (val type = currentActivityType) {
            null -> getString(R.string.record_activity)
            else -> getString(R.string.record_activity_type, type.name)
        }
        qsTile?.apply {
            label = title
            updateTile()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}