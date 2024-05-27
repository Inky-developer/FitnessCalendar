package com.inky.fitnesscalendar.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import kotlinx.coroutines.delay

@Composable
fun Timer(content: @Composable (Long) -> Unit) {
    var ticks by remember { mutableStateOf(0) }
    var isRunning by remember { mutableStateOf(true) }
    val time = remember(ticks) { System.currentTimeMillis() }

    LifecycleResumeEffect(null) {
        isRunning = true
        onPauseOrDispose { isRunning = false }
    }

    LaunchedEffect(isRunning) {
        while (isRunning) {
            ticks += 1
            delay(1000)
        }
    }

    content(time)
}