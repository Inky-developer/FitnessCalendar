package com.inky.fitnesscalendar.view_model.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.material3.SnackbarDuration
import androidx.lifecycle.viewModelScope
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.preferences.Preference.Companion.PREF_BACKUP_URI
import com.inky.fitnesscalendar.repository.DatabaseRepository
import com.inky.fitnesscalendar.repository.backup.BackupRepository
import com.inky.fitnesscalendar.view_model.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    @ApplicationContext context: Context,
    repository: DatabaseRepository,
    private val backupRepository: BackupRepository
) : BaseViewModel(context, repository) {

    private var _backupInProgress = MutableStateFlow(false)
    val backupInProgress get() = _backupInProgress.asStateFlow()

    private var _restoreInProgress = MutableStateFlow(false)
    val restoreInProgress get() = _restoreInProgress.asStateFlow()

    private var _lastBackup = MutableStateFlow<LocalDateTime?>(null)
    val lastBackup get() = _lastBackup.asStateFlow()

    init {
        PREF_BACKUP_URI.flow(context).map { backupDir ->
            updateLastBackup(backupDir)
        }.launchIn(viewModelScope)
    }

    fun updateBackupUri(uri: Uri?) = viewModelScope.launch(Dispatchers.IO) {
        if (uri != null) {
            val contentResolver = context.contentResolver
            val takeFlags: Int =
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            contentResolver.takePersistableUriPermission(uri, takeFlags)
        }
        PREF_BACKUP_URI.set(context, uri)
    }

    fun doBackup() = viewModelScope.launch(Dispatchers.IO) {
        if (_backupInProgress.value) return@launch

        val uri = PREF_BACKUP_URI.get(context)
        _backupInProgress.value = true
        val error = if (uri != null) {
            backupRepository.backup(uri)
        } else {
            BackupRepository.BackupError.CannotAccessFile
        }
        _backupInProgress.value = false
        updateLastBackup(uri)

        val message = when (error) {
            null -> context.getString(R.string.backup_successful)
            else -> context.getString(error.msgID)
        }
        snackbarHostState.showSnackbar(
            message,
            duration = SnackbarDuration.Long
        )
    }

    fun doRestore(file: Uri) = viewModelScope.launch(Dispatchers.IO) {
        if (_restoreInProgress.value) return@launch
        _restoreInProgress.value = true


        val error = backupRepository.restore(file)

        _restoreInProgress.value = false

        val message = context.getString(error.msgID)
        snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Long)
    }

    private fun updateLastBackup(dir: Uri?) {
        _lastBackup.value = dir?.let { backupRepository.getLastBackup(it) }
    }
}