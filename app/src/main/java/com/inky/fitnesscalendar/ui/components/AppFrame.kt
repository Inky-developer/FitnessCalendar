package com.inky.fitnesscalendar.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import com.inky.fitnesscalendar.ui.theme.FitnessCalendarTheme

@Composable
fun AppFrame(modifier: Modifier = Modifier, app: @Composable () -> Unit) {
    FitnessCalendarTheme {
        // In landscape mode, use the more conservative `safeDrawingPadding`, which looks
        // uglier but makes sure that no content is obstructed
        val orientation = LocalConfiguration.current.orientation
        val baseModifier = modifier.imePadding()
        val surfaceModifier = when (orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> baseModifier.safeDrawingPadding()
            else -> baseModifier
        }

        Surface(
            modifier = surfaceModifier,
            color = MaterialTheme.colorScheme.background
        ) {
            app()
        }
    }
}