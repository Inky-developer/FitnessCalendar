package com.inky.fitnesscalendar.ui.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * A value that can be converted to string using context
 */
interface ContextFormat {
    fun formatWithContext(context: Context): String

    @Composable
    fun text() = formatWithContext(LocalContext.current)
}