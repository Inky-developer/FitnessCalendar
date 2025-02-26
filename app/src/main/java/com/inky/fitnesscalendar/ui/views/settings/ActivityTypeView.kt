package com.inky.fitnesscalendar.ui.views.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.ui.components.ActivityCategorySelector
import com.inky.fitnesscalendar.ui.components.ActivityTypeSelector
import com.inky.fitnesscalendar.ui.components.BaseEditDialog
import com.inky.fitnesscalendar.ui.components.ColorSelector
import com.inky.fitnesscalendar.ui.components.EmojiPickerDialog
import com.inky.fitnesscalendar.ui.components.OptionGroup
import com.inky.fitnesscalendar.ui.components.defaultTopAppBarColors
import com.inky.fitnesscalendar.ui.components.optionGroupDefaultBackground
import com.inky.fitnesscalendar.view_model.settings.ActivityTypeEditState
import com.inky.fitnesscalendar.view_model.settings.ActivityTypeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityTypeView(viewModel: ActivityTypeViewModel = hiltViewModel(), onBack: () -> Unit) {
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
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            selectedType = null
                            showEditDialog = true
                        }
                    ) {
                        Icon(Icons.Outlined.Add, stringResource(R.string.add_activity_type))
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = viewModel.snackbarHostState)
        },
    ) { paddingValues ->
        Row(
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
        }
    }

    if (showEditDialog) {
        EditTypeDialog(
            initialState = initialEditState,
            onDismiss = { showEditDialog = false },
            onSave = {
                showEditDialog = false
                viewModel.save(it)
            },
            onDelete = {
                showEditDialog = false
                selectedType?.let { viewModel.delete(it) }
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
    val context = LocalContext.current

    var state by rememberSaveable { mutableStateOf(initialState) }
    var showEmojiPicker by rememberSaveable { mutableStateOf(false) }

    val type = remember(state) { state.toActivityType() }
    val title = remember(state) {
        if (state.isNewType) {
            context.getString(R.string.new_type)
        } else {
            context.getString(R.string.edit_type, state.name)
        }
    }

    BaseEditDialog(
        saveEnabled = type != null,
        onSave = { type?.let(onSave) },
        onNavigateBack = onDismiss,
        title = title,
        actions = {
            if (!state.isNewType) {
                EditTypeDialogMenu(onDeleteType = onDelete)
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
                            Icon(Icons.Outlined.Face, stringResource(R.string.emoji))
                        } else {
                            Text(state.emoji, style = MaterialTheme.typography.displaySmall)
                        }
                    }
                }

                TextField(
                    value = state.name,
                    onValueChange = { state = state.copy(name = it) },
                    leadingIcon = { Icon(Icons.Outlined.Edit, stringResource(R.string.type_name)) },
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
                selectionLabel = if (state.category != null) stringResource(state.category!!.nameId) else null,
            ) {
                ActivityCategorySelector(
                    isSelected = { state.category == it },
                    onSelect = { state = state.copy(category = it) }
                )
            }

            OptionGroup(
                label = stringResource(R.string.select_color),
                selectionLabel = if (state.color != null) stringResource(state.color!!.nameId) else null,
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
                    selectionLabel = state.limitPlacesByColor?.name(),
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
fun EditTypeDialogMenu(onDeleteType: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }

    IconButton(onClick = { showMenu = true }) {
        Icon(Icons.Outlined.Menu, stringResource(R.string.Menu))
    }

    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.delete_activity_type)) },
            leadingIcon = { Icon(Icons.Outlined.Delete, stringResource(R.string.delete)) },
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