package com.inky.fitnesscalendar.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.gpx.GpxTrack
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.localization.LocalizationRepository
import com.inky.fitnesscalendar.repository.ImportRepository
import com.inky.fitnesscalendar.ui.components.ActivityTypeSelector
import com.inky.fitnesscalendar.ui.components.BaseEditDialog
import com.inky.fitnesscalendar.ui.components.CompactActivityCard
import com.inky.fitnesscalendar.ui.components.OptionGroup
import com.inky.fitnesscalendar.ui.components.defaultTopAppBarColors
import com.inky.fitnesscalendar.ui.util.Icons
import com.inky.fitnesscalendar.ui.util.localDatabaseValues
import com.inky.fitnesscalendar.util.result.isOk
import com.inky.fitnesscalendar.view_model.ImportViewModel

@Composable
fun ImportView(viewModel: ImportViewModel) {
    val done by viewModel.done
    val tracks by viewModel.tracks.collectAsState()
    val error by viewModel.error

    if (!done) {
        CircularProgressIndicator(modifier = Modifier.fillMaxSize())
    } else if (error) {
        Scaffold {
            Text(
                stringResource(R.string.could_not_read_input_file),
                modifier = Modifier.padding(it)
            )
        }
    } else {
        ImportView(
            tracks = tracks,
            localizationRepository = viewModel.dbRepository.localizationRepository,
            onImport = viewModel::import,
            onTypeMapping = viewModel::updateTypeMapping
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportView(
    tracks: List<ImportRepository.ImportTrack>,
    localizationRepository: LocalizationRepository,
    onImport: (List<Pair<ImportRepository.ImportTrack, ActivityType>>) -> Unit,
    onTypeMapping: (String, ActivityType) -> Unit,
) {
    val typeMapping = localDatabaseValues.current.activityTypeNames
    val saveButtonEnabled = remember(tracks, typeMapping) {
        tracks.all { typeMapping[it.track.type] != null } && tracks.any {
            it.toRichActivity(typeMapping[it.track.type]!!).isOk()
        }
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.import_activities)) },
                colors = defaultTopAppBarColors(),
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            AnimatedVisibility(visible = saveButtonEnabled) {
                ExtendedFloatingActionButton(
                    onClick = {
                        val tracksAndActivityTypes = tracks.mapNotNull { track ->
                            typeMapping[track.track.type]?.let { track to it }
                        }
                        onImport(tracksAndActivityTypes)
                    },
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icons.Add(stringResource(R.string.confirm_import))
                        Text(
                            stringResource(R.string.confirm_import),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(bottom = 128.dp),
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .testTag("import_list")
        ) {
            items(tracks) { track ->
                val type = typeMapping[track.track.type]
                TrackView(
                    track = track,
                    selectedType = type,
                    localizationRepository = localizationRepository,
                    onChangeType = { key, value -> onTypeMapping(key, value) }
                )
            }
        }
    }
}

@Composable
fun TrackView(
    track: ImportRepository.ImportTrack,
    selectedType: ActivityType?,
    localizationRepository: LocalizationRepository,
    onChangeType: (String, ActivityType) -> Unit,
) {
    var dialogOpen by rememberSaveable { mutableStateOf(false) }
    var selectedActivityType by rememberSaveable { mutableStateOf<ActivityType?>(null) }
    val (richActivity, error) = remember(track, selectedType) {
        if (selectedType == null) {
            return@remember null to null
        }
        val result = track.toRichActivity(selectedType)
        return@remember result.ok() to result.err()
    }

    Box(
        modifier = Modifier
            .clickable { dialogOpen = true }
            .testTag("import_list_item")) {
        if (richActivity != null) {
            CompactActivityCard(
                richActivity = richActivity,
                localizationRepository = localizationRepository,
                expand = true
            )
        } else {
            Column {
                ActivityCardWithoutType(track.track, error, localizationRepository)
            }
        }
    }

    if (dialogOpen) {
        BaseEditDialog(
            title = stringResource(R.string.choose_activity_type),
            onNavigateBack = { dialogOpen = false },
            onSave = {
                selectedActivityType?.let { onChangeType(track.track.type, it) }
                dialogOpen = false
            },
            actions = {}
        ) {
            OptionGroup(
                label = stringResource(R.string.select_activity),
                selectionLabel = selectedActivityType?.name,
                modifier = Modifier.padding(all = 8.dp)
            ) {
                ActivityTypeSelector(
                    isSelected = { it == selectedActivityType },
                    onSelect = { selectedActivityType = it }
                )
            }
        }
    }
}

@Composable
private fun ActivityCardWithoutType(
    track: GpxTrack,
    error: ImportRepository.ImportError?,
    localizationRepository: LocalizationRepository
) {
    val time = remember(track) {
        track.startTime?.let { localizationRepository.formatRelativeDate(it) } ?: ""
    }
    val context = LocalContext.current
    val errorMessage = remember(error) { error?.let { context.getString(error.messageId) } }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            time,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .align(Alignment.End)
                .padding(horizontal = 4.dp)
        )
        Text(
            track.type,
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Text(
            track.name,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Text(
            stringResource(R.string.click_to_set_type),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(all = 8.dp)
        )

        if (errorMessage != null) {
            Text(
                stringResource(R.string.cannot_import_activity_due_to_error, errorMessage),
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )
        }
    }
}