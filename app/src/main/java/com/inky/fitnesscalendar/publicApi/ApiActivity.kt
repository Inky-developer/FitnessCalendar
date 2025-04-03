package com.inky.fitnesscalendar.publicApi

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.repository.DatabaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

abstract class ApiActivity : ComponentActivity() {
    abstract suspend fun handleRequest()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            lifecycleScope.launch(Dispatchers.IO) {
                handleRequest()
                finish()
            }
        } else {
            finish()
        }
    }

    suspend fun extractActivityTypeByName(
        intent: Intent,
        repository: DatabaseRepository
    ): ActivityType? {
        val activityTypes = repository.getActivityTypes().first()

        val activityTypeName = intent.getStringExtra(EXTRA_ACTIVITY_TYPE)
        if (activityTypeName == null) {
            runOnUiThread {
                Toast.makeText(this, "No activity type specified", Toast.LENGTH_LONG).show()
            }
            return null
        }

        val activityType = activityTypes.find { it.name == activityTypeName }
        if (activityType == null) {
            runOnUiThread {
                Toast.makeText(this, "Unknown activity type: $activityTypeName", Toast.LENGTH_LONG)
                    .show()
            }
            return null
        }

        return activityType
    }
}