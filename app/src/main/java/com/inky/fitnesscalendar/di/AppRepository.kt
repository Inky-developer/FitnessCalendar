package com.inky.fitnesscalendar.di

import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Immutable
import com.inky.fitnesscalendar.repository.DatabaseRepository
import com.inky.fitnesscalendar.repository.LocalizationRepository
import com.inky.fitnesscalendar.repository.RecordingRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Immutable
@Singleton
class AppRepository @Inject constructor(
    @ApplicationContext val context: Context,
    val db: DatabaseRepository,
    val localizationRepository: LocalizationRepository,
    val recordingRepository: RecordingRepository
) : LocalizationRepository by localizationRepository {
    val snackbarHostState = SnackbarHostState()
}