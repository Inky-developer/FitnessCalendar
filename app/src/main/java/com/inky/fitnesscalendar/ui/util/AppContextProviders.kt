package com.inky.fitnesscalendar.ui.util

import androidx.compose.runtime.Composable
import com.inky.fitnesscalendar.di.AppRepository

@Composable
context(app: AppRepository)
fun AppContextProviders(content: @Composable () -> Unit) {
    ProvideDatabaseValues(app.db) {
        ProvidePreferences {
            content()
        }
    }
}