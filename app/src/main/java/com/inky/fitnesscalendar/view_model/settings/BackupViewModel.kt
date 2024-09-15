package com.inky.fitnesscalendar.view_model.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.material3.SnackbarDuration
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.viewModelScope
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.preferences.Preference.Companion.PREF_BACKUP_URI
import com.inky.fitnesscalendar.repository.AppRepository
import com.inky.fitnesscalendar.repository.BackupRepository
import com.inky.fitnesscalendar.view_model.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    @ApplicationContext context: Context,
    repository: AppRepository,
    private val backupRepository: BackupRepository
) : BaseViewModel(context, repository) {
    private var _backupInProgress = MutableStateFlow(false)
    val backupInProgress get() = _backupInProgress.asStateFlow()

    fun updateBackupUri(uri: Uri?) = viewModelScope.launch(Dispatchers.IO) {
        if (uri != null) {
            val contentResolver = context.contentResolver

            val takeFlags: Int =
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            // Check for the freshest data.
            contentResolver.takePersistableUriPermission(uri, takeFlags)
            PREF_BACKUP_URI.set(context, uri.toString())
        } else {
            PREF_BACKUP_URI.set(context, "")
        }
    }

    fun doBackup() = viewModelScope.launch(Dispatchers.IO) {
        val uri = Uri.parse(PREF_BACKUP_URI.get(context))
        val file = DocumentFile.fromTreeUri(context, uri)
            ?.createFile("application/zip", "backup.zip")
            ?.uri

        if (file != null) {
            _backupInProgress.value = true
            val error = backupRepository.backup(file)
            _backupInProgress.value = false

            val message = when (error) {
                null -> context.getString(R.string.backup_successful)

                BackupRepository.BackupError.OLD_ANDROID_VERSION ->
                    context.getString(R.string.your_android_version_is_too_old_for_backup)
            }
            snackbarHostState.showSnackbar(
                message,
                duration = SnackbarDuration.Long
            )
        } else {
            snackbarHostState.showSnackbar(
                context.getString(R.string.could_not_read_path),
                duration = SnackbarDuration.Long
            )
        }
    }
}