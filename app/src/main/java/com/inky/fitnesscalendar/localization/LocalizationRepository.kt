package com.inky.fitnesscalendar.localization

import android.content.Context
import android.text.format.DateFormat
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalizationRepository @Inject constructor(@ApplicationContext context: Context) {
    @OptIn(ExperimentalMaterial3Api::class)
    val dateFormatter = DatePickerDefaults.dateFormatter()
    val timeFormatter: java.text.DateFormat = DateFormat.getTimeFormat(context)
}