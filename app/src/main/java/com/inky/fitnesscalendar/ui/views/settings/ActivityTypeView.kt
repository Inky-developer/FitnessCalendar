package com.inky.fitnesscalendar.ui.views.settings

import android.database.sqlite.SQLiteConstraintException
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.di.AppRepository
import com.inky.fitnesscalendar.ui.components.ActivityCategorySelector
import com.inky.fitnesscalendar.ui.components.ActivityTypeFilterChip
import com.inky.fitnesscalendar.ui.components.ActivityTypeSelector
import com.inky.fitnesscalendar.ui.components.BaseEditDialog
import com.inky.fitnesscalendar.ui.components.ColorSelector
import com.inky.fitnesscalendar.ui.components.EmojiPickerDialog
import com.inky.fitnesscalendar.ui.components.OptionGroup
import com.inky.fitnesscalendar.ui.components.defaultTopAppBarColors
import com.inky.fitnesscalendar.ui.components.optionGroupDefaultBackground
import com.inky.fitnesscalendar.ui.util.Icons
import com.inky.fitnesscalendar.ui.util.localDatabaseValues
import com.inky.fitnesscalendar.view_model.settings.ActivityTypeEditState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
context(app: AppRepository)
fun ActivityTypeView(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()

    var selectedType by rememberSaveable { mutableStateOf<ActivityType?>(null) }
    val initialEditState = remember(selectedType) {
        selectedType?.let { ActivityTypeEditState(it) } ?: ActivityTypeEditState()
    }

    var showEditDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.activity_types)) },
                colors = defaultTopAppBarColors(),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icons.ArrowBack(stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            selectedType = null
                            showEditDialog = true
                        }
                    ) {
                        Icons.Add(stringResource(R.string.add_activity_type))
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = app.snackbarHostState)
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(all = 8.dp)
                .fillMaxWidth()
        ) {
            ActivityTypeSelector(
                isSelected = { false },
                onSelect = {
                    selectedType = it
                    showEditDialog = true
                },
            )

            ArchivedActivityTypes(
                onUnarchive = { scope.launch { restore(it) } }
            )
        }
    }

    if (showEditDialog) {
        EditTypeDialog(
            initialState = initialEditState,
            onDismiss = { showEditDialog = false },
            onSave = {
                scope.launch {
                    app.db.saveActivityType(it)
                    showEditDialog = false
                }
            },
            onDelete = {
                scope.launch {
                    selectedType?.let { delete(it) }
                    showEditDialog = false
                }
            }
        )
    }
}


@Composable
fun EditTypeDialog(
    initialState: ActivityTypeEditState,
    onDismiss: () -> Unit,
    onSave: (ActivityType) -> Unit,
    onDelete: () -> Unit,
) {
    val resources = LocalResources.current

    var state by rememberSaveable { mutableStateOf(initialState) }
    var showEmojiPicker by rememberSaveable { mutableStateOf(false) }

    val type = remember(state) { state.toActivityType() }
    val title = remember(state) {
        if (state.isNewType) {
            resources.getString(R.string.new_type)
        } else {
            resources.getString(R.string.edit_type, state.name)
        }
    }

    BaseEditDialog(
        saveEnabled = type != null,
        onSave = { type?.let(onSave) },
        onNavigateBack = onDismiss,
        title = title,
        actions = {
            if (!state.isNewType) {
                EditTypeDialogMenu(
                    onDeleteType = onDelete,
                    onArchive = {
                        type?.let { onSave(it.copy(archived = true)) }
                    },
                    showArchiveOption = state == initialState
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .padding(all = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                IconButton(
                    onClick = { showEmojiPicker = true },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = optionGroupDefaultBackground()),
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(end = 4.dp)
                ) {
                    AnimatedContent(targetState = state.emoji, label = "emoji") { emoji ->
                        if (emoji.isBlank()) {
                            Icons.Face(stringResource(R.string.emoji))
                        } else {
                            Text(state.emoji, style = MaterialTheme.typography.displaySmall)
                        }
                    }
                }

                TextField(
                    value = state.name,
                    onValueChange = { state = state.copy(name = it) },
                    leadingIcon = { Icons.Edit(stringResource(R.string.type_name)) },
                    placeholder = { Text(stringResource(R.string.name_of_type)) },
                    singleLine = true,
                    keyboardOptions = remember { KeyboardOptions(capitalization = KeyboardCapitalization.Words) },
                    colors = TextFieldDefaults.colors(unfocusedContainerColor = optionGroupDefaultBackground()),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp)
                )
            }

            OptionGroup(
                label = stringResource(R.string.select_category),
                selectionLabel = state.category?.text(),
            ) {
                ActivityCategorySelector(
                    isSelected = { state.category == it },
                    onSelect = { state = state.copy(category = it) }
                )
            }

            OptionGroup(
                label = stringResource(R.string.select_color),
                selectionLabel = state.color?.text(),
            ) {
                ColorSelector(
                    isSelected = { state.color == it },
                    onSelect = { state = state.copy(color = it) }
                )
            }

            Toggle(
                name = stringResource(R.string.has_place),
                value = state.hasPlace,
                onValue = { state = state.copy(hasPlace = it) }
            )
            AnimatedVisibility(state.hasPlace) {
                OptionGroup(
                    label = stringResource(R.string.limit_places_by_color),
                    selectionLabel = state.limitPlacesByColor?.text(),
                ) {
                    ColorSelector(
                        isSelected = { state.limitPlacesByColor == it },
                        onSelect = {
                            state = state.copy(
                                limitPlacesByColor = if (state.limitPlacesByColor == it) null else it
                            )
                        }
                    )
                }
            }
            Toggle(
                name = stringResource(R.string.has_duration),
                value = state.hasDuration,
                onValue = { state = state.copy(hasDuration = it) }
            )
            Toggle(
                name = stringResource(R.string.has_distance),
                value = state.hasDistance,
                onValue = { state = state.copy(hasDistance = it) }
            )
            Toggle(
                name = stringResource(R.string.has_vehicle),
                value = state.hasVehicle,
                onValue = { state = state.copy(hasVehicle = it) }
            )
            Toggle(
                name = stringResource(R.string.has_intensity),
                value = state.hasIntensity,
                onValue = { state = state.copy(hasIntensity = it) }
            )
        }
    }

    if (showEmojiPicker) {
        EmojiPickerDialog(
            onDismiss = { showEmojiPicker = false },
            onEmoji = {
                showEmojiPicker = false
                state = state.copy(emoji = it)
            }
        )
    }
}

@Composable
fun EditTypeDialogMenu(
    onDeleteType: () -> Unit,
    onArchive: () -> Unit,
    showArchiveOption: Boolean
) {
    var showMenu by remember { mutableStateOf(false) }

    IconButton(onClick = { showMenu = true }) {
        Icons.MoreOptions(stringResource(R.string.open_context_menu))
    }

    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
        if (showArchiveOption) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.archive_activity_type)) },
                leadingIcon = { Icons.Archive(stringResource(R.string.archive_activity_type)) },
                onClick = onArchive
            )
        }
        DropdownMenuItem(
            text = { Text(stringResource(R.string.delete_activity_type)) },
            leadingIcon = { Icons.Delete(stringResource(R.string.delete)) },
            onClick = onDeleteType
        )
    }
}

@Composable
fun Toggle(
    name: String,
    value: Boolean,
    onValue: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(vertical = 4.dp)
            .clip(MaterialTheme.shapes.small)
            .background(optionGroupDefaultBackground())
    ) {
        Text(
            name,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        )
        Switch(
            checked = value,
            onCheckedChange = onValue,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@Composable
fun ArchivedActivityTypes(onUnarchive: (ActivityType) -> Unit) {
    val activityTypes = localDatabaseValues.current.activityTypes.filter { it.archived }

    AnimatedVisibility(activityTypes.isNotEmpty(), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(all = 8.dp)

        ) {
            Text(
                stringResource(R.string.archived_activity_types),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(all = 8.dp)
            )
            FlowRow {
                for (type in activityTypes) {
                    ActivityTypeFilterChip(
                        activityType = type,
                        selected = false,
                        onClick = { onUnarchive(type) },
                    )
                }
            }
        }
    }
}

context(app: AppRepository)
suspend fun delete(activityType: ActivityType) {
    try {
        app.db.deleteActivityType(activityType)
        val result = app.snackbarHostState.showSnackbar(
            message = app.context.getString(R.string.deleted_activity_type),
            actionLabel = app.context.getString(R.string.undo),
            duration = SnackbarDuration.Short
        )
        when (result) {
            SnackbarResult.ActionPerformed -> app.db.saveActivityType(activityType)
            SnackbarResult.Dismissed -> {}
        }
    } catch (e: SQLiteConstraintException) {
        app.snackbarHostState.showSnackbar(message = app.context.getString(R.string.cannot_delete_type_because_there_are_still_activities))
    }
}

context(app: AppRepository)
suspend fun restore(activityType: ActivityType) {
    app.db.saveActivityType(activityType.copy(archived = false))
    val result = app.snackbarHostState.showSnackbar(
        message = app.context.getString(R.string.restored_activity_type),
        actionLabel = app.context.getString(R.string.undo),
        duration = SnackbarDuration.Short
    )
    when (result) {
        SnackbarResult.ActionPerformed -> app.db.saveActivityType(activityType)
        SnackbarResult.Dismissed -> {}
    }
}