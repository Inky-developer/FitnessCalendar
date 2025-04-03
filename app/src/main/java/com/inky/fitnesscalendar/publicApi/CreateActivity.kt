package com.inky.fitnesscalendar.publicApi

import android.app.PendingIntent
import android.content.Intent
import android.widget.Toast
import androidx.core.app.TaskStackBuilder
import androidx.core.net.toUri
import com.inky.fitnesscalendar.MainActivity
import com.inky.fitnesscalendar.repository.DatabaseRepository
import com.inky.fitnesscalendar.ui.views.Views
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import javax.inject.Inject

private const val EXTRA_ACTIVITY_TYPE = "ACTIVITY_TYPE"
private const val EXTRA_START_TIME = "START_TIME"
private const val EXTRA_END_TIME = "END_TIME"

/**
 * Enables third party applications to create a new fitness calendar activity using intents
 */
@AndroidEntryPoint
class CreateActivity : ApiActivity() {
    @Inject
    lateinit var repository: DatabaseRepository

    override suspend fun handleRequest() {
        val activityTypes = repository.getActivityTypes().first()

        val activityTypeName = intent.getStringExtra(EXTRA_ACTIVITY_TYPE)
        val activityTypeId = activityTypes.find { it.name == activityTypeName }?.uid
        if (activityTypeId == null && activityTypeName != null) {
            Toast.makeText(this, "Unknown activity type: $activityTypeName", Toast.LENGTH_LONG)
                .show()
            return
        }

        val startTime = intent.getLongExtra(EXTRA_START_TIME, -1).takeIf { it >= 0 }
        val endTime = intent.getLongExtra(EXTRA_END_TIME, -1).takeIf { it >= 0 }

        val route = Views.ApiNewActivity(activityTypeId, startTime = startTime, endTime = endTime)

        val newActivityIntent = Intent(
            Intent.ACTION_VIEW,
            route.toDeepUrl().toUri(),
            this,
            MainActivity::class.java
        )
        val pendingIntent = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(newActivityIntent)
            getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
        pendingIntent!!.send()
    }
}