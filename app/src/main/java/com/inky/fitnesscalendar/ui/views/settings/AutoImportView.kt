package com.inky.fitnesscalendar.ui.views.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.preferences.Preference.Companion.PREF_WATCHED_FOLDERS
import com.inky.fitnesscalendar.ui.components.defaultTopAppBarColors
import com.inky.fitnesscalendar.ui.util.Icons
import com.inky.fitnesscalendar.view_model.settings.AutoImportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoImportView(viewModel: AutoImportViewModel = hiltViewModel(), onBack: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.auto_import)) },
                colors = defaultTopAppBarColors(),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icons.ArrowBack(stringResource(R.string.back))
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
                stringResource(R.string.auto_import_help_text),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                    .padding(all = 8.dp)
            )

            AutoImportList(
                onNewDir = viewModel::addAutoImportDir,
                onRemoveDir = viewModel::removeAutoImportDir
            )
        }
    }
}

@Composable
fun AutoImportList(onNewDir: (Uri) -> Unit, onRemoveDir: (Uri) -> Unit) {
    val dirs by PREF_WATCHED_FOLDERS.flow(LocalContext.current).collectAsState(emptySet())

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(all = 8.dp)) {
            AddFolderButton(onUri = { it?.let(onNewDir) })

            AnimatedContent(dirs.isEmpty(), label = "isWatchedFoldersEmpty") { isEmpty ->
                Column {
                    if (isEmpty) {
                        Text(stringResource(R.string.no_folders_configured_yet))
                    } else {
                        Text(stringResource(R.string.configured_folders))
                    }

                    for (folder in dirs) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                folder.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { onRemoveDir(folder) }) {
                                Icons.Delete(
                                    stringResource(R.string.delete),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun AddFolderButton(onUri: (Uri?) -> Unit) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = onUri
    )

    FilledTonalButton(onClick = { launcher.launch(null) }, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.add_folder_to_auto_import))
    }
}