package com.inky.fitnesscalendar.view_model.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.inky.fitnesscalendar.preferences.Preference.Companion.PREF_WATCHED_FOLDERS
import com.inky.fitnesscalendar.repository.AutoImportRepository
import com.inky.fitnesscalendar.repository.DatabaseRepository
import com.inky.fitnesscalendar.view_model.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AutoImportViewModel @Inject constructor(
    @ApplicationContext context: Context,
    databaseRepository: DatabaseRepository,
    private val autoImportRepository: AutoImportRepository
) : BaseViewModel(context, databaseRepository) {
    fun addAutoImportDir(uri: Uri) = viewModelScope.launch(Dispatchers.IO) {
        val contentResolver = context.contentResolver
        val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
        contentResolver.takePersistableUriPermission(uri, takeFlags)
        PREF_WATCHED_FOLDERS.add(context, uri)

        autoImportRepository.importFrom(setOf(uri), true)
    }

    fun removeAutoImportDir(uri: Uri) = viewModelScope.launch(Dispatchers.IO) {
        PREF_WATCHED_FOLDERS.update(context) {
            it - uri
        }
    }
}