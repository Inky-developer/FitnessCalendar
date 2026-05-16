package com.inky.fitnesscalendar.view_model

import androidx.lifecycle.ViewModel
import com.inky.fitnesscalendar.di.AppContext
import com.inky.fitnesscalendar.repository.DatabaseRepository

class NewActivityViewModel(app: AppContext) : ViewModel() {
    val repository: DatabaseRepository = app.databaseRepo
    val localizationRepository = repository.localizationRepository
}
