package com.inky.fitnesscalendar.ui.views.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.localization.LocalizationRepository
import com.inky.fitnesscalendar.preferences.Preference.Companion.PREF_BACKUP_URI
import com.inky.fitnesscalendar.repository.backup.BackupRepository
import com.inky.fitnesscalendar.ui.components.defaultTopAppBarColors
import com.inky.fitnesscalendar.view_model.settings.BackupViewModel
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupView(viewModel: BackupViewModel = hiltViewModel(), onBack: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.backup)) },
                colors = defaultTopAppBarColors(),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.back))
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(hostState = viewModel.snackbarHostState) },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 8.dp)
        ) {
            Text(
                stringResource(R.string.warning_backups_not_encrypted),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                    .padding(all = 8.dp)
            )

            val lastBackup by viewModel.lastBackup.collectAsState()
            LastBackupInfo(lastBackup)

            BackupDirectoryButton(onUri = viewModel::updateBackupUri)

            val backupInProgress by viewModel.backupInProgress.collectAsState()
            BackupButton(
                backupInProgress = backupInProgress,
                onBackup = viewModel::doBackup
            )

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp, horizontal = 8.dp)
            )

            val restoreInProgress by viewModel.restoreInProgress.collectAsState()
            RestoreButton(restoreInProgress = restoreInProgress, onRestore = viewModel::doRestore)
        }
    }
}

@Composable
private fun RestoreButton(restoreInProgress: Boolean, onRestore: (Uri) -> Unit) {
    var restoreFileUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { restoreFileUri = it }
    )

    if (restoreFileUri != null) {
        AlertDialog(
            icon = {
                Icon(
                    Icons.Outlined.Warning,
                    stringResource(R.string.warning),
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text(stringResource(R.string.ask_restore_backup)) },
            text = { Text(stringResource(R.string.this_will_overwrite_all_app_data)) },
            onDismissRequest = { restoreFileUri = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        restoreFileUri?.let(onRestore)
                        restoreFileUri = null
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { restoreFileUri = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Button(
        onClick = { launcher.launch(arrayOf("application/zip")) },
        enabled = !restoreInProgress,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        if (restoreInProgress) {
            CircularProgressIndicator()
        } else {
            Text(stringResource(R.string.restore_backup))
        }
    }
}

@Composable
private fun BackupButton(
    backupInProgress: Boolean,
    onBackup: () -> Unit
) {
    val backupDirectory by PREF_BACKUP_URI.collectAsState()

    Button(
        onClick = onBackup,
        enabled = !backupInProgress && BackupRepository.isBackupSupported() && backupDirectory != null,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        if (backupInProgress) {
            CircularProgressIndicator()
        } else {
            Text(stringResource(R.string.create_backup))
        }
    }
}

@Composable
private fun BackupDirectoryButton(onUri: (Uri?) -> Unit) {
    val backupUri by PREF_BACKUP_URI.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = onUri
    )

    Column(
        modifier = Modifier
            .clickable { launcher.launch(backupUri) }
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(stringResource(R.string.backup_directory))
        AnimatedContent(backupUri, label = "backupUri") { uri ->
            val text = uri?.toString() ?: stringResource(R.string.tap_to_set_backup_dir)
            Text(text, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun LastBackupInfo(lastBackupDate: LocalDateTime? = null) {
    val context = LocalContext.current
    val lastBackupText = remember(lastBackupDate) {
        lastBackupDate
            ?.let { LocalizationRepository.localDateFormatter.format(lastBackupDate) }
            ?: context.getString(R.string.never)
    }

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(stringResource(R.string.last_backup))
        Text(lastBackupText, style = MaterialTheme.typography.bodySmall)
    }
}