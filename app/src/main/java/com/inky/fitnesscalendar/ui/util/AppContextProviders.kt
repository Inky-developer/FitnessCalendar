package com.inky.fitnesscalendar.ui.util

import androidx.compose.runtime.Composable
import com.inky.fitnesscalendar.repository.DatabaseRepository

@Composable
fun AppContextProviders(repository: DatabaseRepository, content: @Composable () -> Unit) {
    ProvideDatabaseValues(repository) {
        ProvidePreferences {
            content()
        }
    }
}