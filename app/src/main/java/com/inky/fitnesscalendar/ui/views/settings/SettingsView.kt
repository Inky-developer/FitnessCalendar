package com.inky.fitnesscalendar.ui.views.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.preferences.Preference
import com.inky.fitnesscalendar.ui.util.SharedContentKey
import com.inky.fitnesscalendar.ui.util.sharedBounds
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(
    onOpenDrawer: () -> Unit,
    onNavigateDebug: () -> Unit,
    onNavigateTypes: () -> Unit,
    onNavigatePlaces: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Outlined.Menu, stringResource(R.string.Menu))
                    }
                },
                scrollBehavior = scrollBehavior,
                modifier = Modifier.sharedBounds(SharedContentKey.AppBar)
            )
        },
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
                    title = stringResource(R.string.places),
                    onClick = onNavigatePlaces,
                )
                Setting(
                    title = stringResource(R.string.debug),
                    onClick = onNavigateDebug,
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                CollectBssidPreference()
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun CollectBssidPreference() {
    var showRequestExplanationDialog by rememberSaveable { mutableStateOf(false) }
    var tryingToEnablePreference by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ),
        onPermissionsResult = { result ->
            if (tryingToEnablePreference && result.values.all { it }) {
                tryingToEnablePreference = false
                scope.launch {
                    Preference.COLLECT_BSSID.set(context, true)
                }
            }
        }
    )

    PreferenceToggle(preference = Preference.COLLECT_BSSID) { enabled ->
        tryingToEnablePreference = false
        if (enabled && !permissionsState.allPermissionsGranted) {
            showRequestExplanationDialog = true
            false
        } else {
            true
        }
    }

    if (showRequestExplanationDialog) {
        AlertDialog(
            onDismissRequest = {
                showRequestExplanationDialog = false
            },
            confirmButton = {
                TextButton(onClick = {
                    showRequestExplanationDialog = false
                    tryingToEnablePreference = true
                    permissionsState.launchMultiplePermissionRequest()
                }) {
                    Text(stringResource(R.string.action_continue))
                }
            },
            title = { Text(stringResource(R.string.wifi_information_permission_help_title)) },
            text = { Text(stringResource(R.string.wifi_information_permission_help_description)) }
        )
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
        if (filter != null) {
            if (filter(it)) {
                scope.launch { preference.set(context, it) }
            }
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