package com.inky.fitnesscalendar

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.inky.fitnesscalendar.repository.DatabaseRepository
import com.inky.fitnesscalendar.repository.HealthConnectRepository
import com.inky.fitnesscalendar.ui.App
import com.inky.fitnesscalendar.ui.components.AppFrame
import com.inky.fitnesscalendar.util.EXTRA_TOAST
import com.inky.fitnesscalendar.util.result.TypedResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var repository: DatabaseRepository

    @Inject
    lateinit var healthConnectRepository: HealthConnectRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)

        requestNotificationPermission()

        if (savedInstanceState == null) {
            val toastMsg = intent.getStringExtra(EXTRA_TOAST)
            if (toastMsg != null) {
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show()
            }
        }

        lifecycleScope.launch {
            val result = healthConnectRepository.synchronize()
            if (result is TypedResult.Err) {
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.could_not_read_step_data, result.error),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        setContent {
            AppFrame {
                App()
            }
        }
    }

    private fun requestNotificationPermission() {
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val locationPermissionRequest =
                registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

            locationPermissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
