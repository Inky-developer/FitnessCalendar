package com.inky.fitnesscalendar

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import com.inky.fitnesscalendar.ui.App
import com.inky.fitnesscalendar.ui.theme.FitnessCalendarTheme
import com.inky.fitnesscalendar.util.EXTRA_TOAST
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var repository: AppRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)

        requestNotificationPermission()

        val toastMsg = intent.getStringExtra(EXTRA_TOAST)
        if (toastMsg != null) {
            Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show()
        }

        setContent {
            FitnessCalendarTheme {
                // In landscape mode, use the more conservative `safeDrawingPadding`, which looks
                // uglier but makes sure that no content is obstructed
                val orientation = LocalConfiguration.current.orientation
                val baseModifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                val modifier = when (orientation) {
                    Configuration.ORIENTATION_LANDSCAPE -> baseModifier.safeDrawingPadding()
                    else -> baseModifier
                }

                Surface(
                    modifier = modifier,
                    color = MaterialTheme.colorScheme.background
                ) {
                    App()
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val locationPermissionRequest =
                registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
                    if (!result) {
                        Toast.makeText(this, "You suck", Toast.LENGTH_LONG).show()
                    }
                }

            locationPermissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
