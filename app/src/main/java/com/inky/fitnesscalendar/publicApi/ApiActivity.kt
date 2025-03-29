package com.inky.fitnesscalendar.publicApi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

abstract class ApiActivity : ComponentActivity() {
    abstract suspend fun handleRequest()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            lifecycleScope.launch {
                handleRequest()
                finish()
            }
        } else {
            finish()
        }
    }
}