package com.inky.fitnesscalendar.publicApi

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.TaskStackBuilder
import androidx.core.net.toUri
import com.inky.fitnesscalendar.MainActivity
import com.inky.fitnesscalendar.repository.DatabaseRepository
import com.inky.fitnesscalendar.ui.views.Views
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Enables third party applications to create a new fitness calendar activity using intents
 */
@AndroidEntryPoint
class CreateActivity : ApiActivity() {
    @Inject
    lateinit var repository: DatabaseRepository

    override suspend fun handleRequest() {
        val activityType = extractActivityTypeByName(intent, repository) ?: return
        val startTime = intent.getLongExtra(EXTRA_START_TIME, -1).takeIf { it >= 0 }
        val endTime = intent.getLongExtra(EXTRA_END_TIME, -1).takeIf { it >= 0 }

        val route = Views.ApiNewActivity(activityType.uid, startTime = startTime, endTime = endTime)

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