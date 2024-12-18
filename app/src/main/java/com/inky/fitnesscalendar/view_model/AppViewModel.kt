package com.inky.fitnesscalendar.view_model

import android.content.Context
import com.inky.fitnesscalendar.repository.DatabaseRepository
import com.inky.fitnesscalendar.repository.RecordingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    @ApplicationContext context: Context,
    repository: DatabaseRepository,
    val recordingRepository: RecordingRepository
) : BaseViewModel(context, repository)