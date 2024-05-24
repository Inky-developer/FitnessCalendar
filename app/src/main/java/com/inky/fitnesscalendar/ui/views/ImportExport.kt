package com.inky.fitnesscalendar.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.ui.util.SharedContentKey
import com.inky.fitnesscalendar.ui.util.sharedBounds
import com.inky.fitnesscalendar.util.exportCsv
import com.inky.fitnesscalendar.util.importCsv
import com.inky.fitnesscalendar.view_model.ImportExportViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportExport(
    viewModel: ImportExportViewModel = hiltViewModel(),
    onOpenDrawer: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var exporting by remember { mutableStateOf(false) }
    val importing by viewModel.importing.collectAsState(initial = false)
    var importData by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(null) {
        viewModel.toastMessage.collect {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.import_export)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Outlined.Menu, stringResource(R.string.Menu))
                    }
                },
                modifier = Modifier.sharedBounds(SharedContentKey.AppBar)
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        val context = LocalContext.current
        Column(modifier = Modifier.padding(paddingValues)) {
            Button(
                enabled = !exporting,
                onClick = {
                    exporting = true
                    scope.launch {
                        val activities = viewModel.repository.loadAllActivities()
                        context.exportCsv(activities)
                        exporting = false
                    }
                }, modifier = Modifier
                    .padding(all = 8.dp)
                    .fillMaxWidth()
            ) {
                AnimatedVisibility(visible = exporting) {
                    CircularProgressIndicator()
                }
                AnimatedVisibility(visible = !exporting) {
                    Text(stringResource(R.string.export_activities))
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 32.dp))

            TextField(
                value = importData,
                onValueChange = { importData = it },
                label = { Text(stringResource(R.string.enter_import_data)) },
                modifier = Modifier
                    .padding(all = 8.dp)
                    .fillMaxWidth()
            )

            Button(
                enabled = !importing && importData.isNotBlank(),
                onClick = {
                    val activities = importCsv(importData)
                    viewModel.import(activities)
                },
                modifier = Modifier
                    .padding(all = 8.dp)
                    .fillMaxWidth()
            ) {
                AnimatedVisibility(visible = importing) {
                    CircularProgressIndicator()
                }

                AnimatedVisibility(visible = !importing) {
                    Text(stringResource(R.string.import_activities))
                }
            }
        }
    }
}