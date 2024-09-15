package com.inky.fitnesscalendar.ui.views.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import androidx.hilt.navigation.compose.hiltViewModel
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.repository.BackupRepository
import com.inky.fitnesscalendar.view_model.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupView(viewModel: BaseViewModel = hiltViewModel(), onBack: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val topAppBarColors = TopAppBarDefaults.topAppBarColors(
        scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer,
        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
    )
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.backup)) },
                colors = topAppBarColors,
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
            Column(
                modifier = Modifier
                    .clickable { /* TODO */ }
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(stringResource(R.string.backup_directory))
                Text("TODO", style = MaterialTheme.typography.bodyMedium)
            }

            BackupButton(viewModel.repository.backupRepository, viewModel.snackbarHostState)
        }
    }
}

@Composable
private fun BackupButton(backupRepository: BackupRepository, snackbarHostState: SnackbarHostState) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var backupInProgress by remember { mutableStateOf(false) }

    val launcher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocumentTree()) { uri ->
            val file = uri?.let { DocumentFile.fromTreeUri(context, it) }
                ?.createFile("application/zip", "backup.zip")?.uri
            if (file != null) {
                scope.launch(Dispatchers.IO) {
                    backupInProgress = true
                    val error = backupRepository.backup(file)
                    backupInProgress = false

                    val message = when (error) {
                        null -> context.getString(R.string.backup_successful)

                        BackupRepository.BackupError.OLD_ANDROID_VERSION ->
                            context.getString(R.string.your_android_version_is_too_old_for_backup)
                    }
                    snackbarHostState.showSnackbar(
                        message,
                        duration = SnackbarDuration.Long
                    )

                }
            } else if (uri != null) {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        context.getString(R.string.could_not_read_path),
                        duration = SnackbarDuration.Long
                    )
                }
            }
        }

    Button(
        onClick = { launcher.launch(null) },
        enabled = !backupInProgress && BackupRepository.isBackupSupported(),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        if (backupInProgress) {
            CircularProgressIndicator()
        } else {
            Text(stringResource(R.string.create_backup))
        }
    }
}