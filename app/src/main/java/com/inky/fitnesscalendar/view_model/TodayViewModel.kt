package com.inky.fitnesscalendar.view_model

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.inky.fitnesscalendar.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(private val handle: SavedStateHandle, val repository: AppRepository): ViewModel() {
}