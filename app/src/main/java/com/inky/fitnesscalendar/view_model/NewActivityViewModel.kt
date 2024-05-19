package com.inky.fitnesscalendar.view_model

import androidx.lifecycle.ViewModel
import com.inky.fitnesscalendar.AppRepository
import com.inky.fitnesscalendar.localization.LocalizationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NewActivityViewModel @Inject constructor(val repository: AppRepository): ViewModel() {
    val localizationRepository = repository.localizationRepository
}