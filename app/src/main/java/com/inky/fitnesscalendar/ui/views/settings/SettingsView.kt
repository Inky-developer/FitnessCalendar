package com.inky.fitnesscalendar.ui.views.settings

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.inky.fitnesscalendar.BuildConfig
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.preferences.Preference
import com.inky.fitnesscalendar.repository.backup.BackupRepository
import com.inky.fitnesscalendar.ui.components.defaultTopAppBarColors
import com.inky.fitnesscalendar.ui.util.SharedContentKey
import com.inky.fitnesscalendar.ui.util.sharedBounds
import com.inky.fitnesscalendar.view_model.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(
    viewModel: SettingsViewModel = hiltViewModel(),
    onOpenDrawer: () -> Unit,
    onNavigateAbout: () -> Unit,
    onNavigateDebug: () -> Unit,
    onNavigateTypes: () -> Unit,
    onNavigatePlaces: () -> Unit,
    onNavigateAutoImport: () -> Unit,
    onNavigateBackup: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                colors = defaultTopAppBarColors(),
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Outlined.Menu, stringResource(R.string.Menu))
                    }
                },
                scrollBehavior = scrollBehavior,
                modifier = Modifier.sharedBounds(SharedContentKey.AppBar)
            )
        },
        snackbarHost = { SnackbarHost(viewModel.snackbarHostState) },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(all = 8.dp)
        ) {
            item {
                Setting(
                    title = stringResource(R.string.configure_activity_types),
                    onClick = onNavigateTypes
                )
                Setting(
                    title = stringResource(R.string.configure_places),
                    onClick = onNavigatePlaces,
                )
                Setting(
                    title = stringResource(R.string.configure_auto_import),
                    onClick = onNavigateAutoImport
                )
                if (BackupRepository.isBackupSupported()) {
                    Setting(
                        title = stringResource(R.string.configure_backup),
                        onClick = onNavigateBackup
                    )
                }

                Setting(stringResource(R.string.configure_about), onClick = onNavigateAbout)

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    stringResource(R.string.advanced_settings),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                PreferenceToggle(Preference.PREF_ENABLE_PUBLIC_API)

                Setting(
                    title = stringResource(R.string.recalculate_track_data),
                    onClick = viewModel::recalculateTrackData
                )

                Setting(
                    title = stringResource(R.string.debug),
                    onClick = onNavigateDebug,
                )

                if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    Setting(
                        title = "Load sample activities",
                        onClick = {
                            viewModel.repository.generateSampleActivitiesForTesting()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun Setting(
    title: String,
    onClick: () -> Unit
) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(title)
    }
}

@Composable
private fun <T> PreferenceToggle(
    preference: Preference<Boolean, T>,
    filter: ((Boolean) -> Boolean)? = null
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val checked by preference.collectAsState()

    val title = remember(preference) { preference.titleId?.let { context.getString(it) } }
    val description =
        remember(preference) { preference.descriptionId?.let { context.getString(it) } }

    val onCheckedChange: (Boolean) -> Unit = {
        if (filter == null || filter(it)) {
            scope.launch { preference.set(context, it) }
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onCheckedChange(!checked) }
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title ?: "", style = MaterialTheme.typography.titleMedium, maxLines = 1)
            Text(description ?: "", style = MaterialTheme.typography.bodyMedium)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}