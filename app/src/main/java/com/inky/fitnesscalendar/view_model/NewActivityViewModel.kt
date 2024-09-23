package com.inky.fitnesscalendar.view_model

import androidx.lifecycle.ViewModel
import com.inky.fitnesscalendar.repository.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NewActivityViewModel @Inject constructor(val repository: DatabaseRepository) : ViewModel() {
    val localizationRepository = repository.localizationRepository
}