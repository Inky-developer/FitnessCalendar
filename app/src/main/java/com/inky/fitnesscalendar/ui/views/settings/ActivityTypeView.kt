package com.inky.fitnesscalendar.ui.views.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.ActivityType
import com.inky.fitnesscalendar.ui.components.ActivityCategorySelector
import com.inky.fitnesscalendar.ui.components.ActivityTypeSelector
import com.inky.fitnesscalendar.ui.components.BaseEditDialog
import com.inky.fitnesscalendar.ui.components.OptionGroup
import com.inky.fitnesscalendar.util.ACTIVITY_TYPE_COLOR_IDS
import com.inky.fitnesscalendar.view_model.settings.ActivityTypeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityTypeView(viewModel: ActivityTypeViewModel = hiltViewModel(), onBack: () -> Unit) {
    val typeRows by viewModel.typeRows.collectAsState(initial = emptyList())
    var selectedType by remember { mutableStateOf<ActivityType?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.actity_types)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.back))
                    }
                }
            )
        }
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
                typeRows = typeRows
            )
        }
    }

    if (showEditDialog) {
        when (val type = selectedType) {
            null -> {}
            else -> EditTypeDialog(
                initialType = type,
                onDismiss = { showEditDialog = false },
                onSave = {
                    showEditDialog = false
                    viewModel.save(it)
                })
        }
    }
}


@Composable
fun EditTypeDialog(
    initialType: ActivityType,
    onDismiss: () -> Unit,
    onSave: (ActivityType) -> Unit
) {
    var type by remember(initialType) { mutableStateOf(initialType) }

    val formValid by remember {
        derivedStateOf {
            type.name.isNotBlank() && type.name.lines().size == 1 && type.emoji.isNotBlank() && type.emoji.lines().size == 1
        }
    }

    BaseEditDialog(
        saveEnabled = formValid,
        onSave = { onSave(type) },
        onNavigateBack = onDismiss,
        title = stringResource(R.string.edit_type, type.name),
        actions = {}
    ) {
        Column(modifier = Modifier.padding(all = 8.dp)) {
            TextField(
                value = type.name,
                onValueChange = { type = type.copy(name = it) },
                leadingIcon = { Icon(Icons.Outlined.Edit, stringResource(R.string.type_name)) },
                placeholder = { Text(stringResource(R.string.name_of_type)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            // TODO: Use proper emoji picker
            TextField(
                value = type.emoji,
                onValueChange = { type = type.copy(emoji = it) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            OptionGroup(
                label = stringResource(R.string.select_category),
                selectionLabel = stringResource(type.activityCategory.nameId),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                ActivityCategorySelector(
                    isSelected = { type.activityCategory == it },
                    onSelect = { type = type.copy(activityCategory = it) }
                )
            }

            // TODO: Display selected color name
            OptionGroup(
                label = stringResource(R.string.select_color),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                ColorSelector(
                    isSelected = { type.colorId == it },
                    onSelect = { type = type.copy(colorId = it) }
                )
            }

            Toggle(
                name = stringResource(R.string.has_duration),
                value = type.hasDuration,
                onValue = { type = type.copy(hasDuration = it) }
            )
            Toggle(
                name = stringResource(R.string.has_vehicle),
                value = type.hasVehicle,
                onValue = { type = type.copy(hasVehicle = it) }
            )

        }
    }
}

@Composable
fun ColorSelector(
    isSelected: (Int) -> Boolean,
    onSelect: (Int) -> Unit
) {
    LazyRow {
        items(ACTIVITY_TYPE_COLOR_IDS) { colorId ->
            FilterChip(
                selected = isSelected(colorId),
                onClick = { onSelect(colorId) },
                label = {
                    Surface(
                        color = colorResource(colorId),
                        modifier = Modifier
                            .width(24.dp)
                            .height(24.dp)
                            .clip(CircleShape)
                    ) {}
                },
                modifier = Modifier.padding(all = 4.dp)
            )
        }
    }
}

@Composable
fun Toggle(
    name: String,
    value: Boolean,
    onValue: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            name,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = value, onCheckedChange = onValue)
    }
}