package com.inky.fitnesscalendar.ui.views.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.db.entities.Place
import com.inky.fitnesscalendar.ui.components.BaseEditDialog
import com.inky.fitnesscalendar.ui.components.ColorSelector
import com.inky.fitnesscalendar.ui.components.OptionGroup
import com.inky.fitnesscalendar.view_model.PlaceListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceListView(viewModel: PlaceListViewModel = hiltViewModel(), onOpenDrawer: () -> Unit) {
    var showEditDialog by rememberSaveable { mutableStateOf(false) }
    var placeToEdit by rememberSaveable { mutableStateOf<Place?>(null) }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val topAppBarColors = TopAppBarDefaults.topAppBarColors(
        scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer,
        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
    )
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.places)) },
                colors = topAppBarColors,
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(
                            imageVector = Icons.Outlined.Menu,
                            contentDescription = stringResource(R.string.Menu),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Outlined.Add, stringResource(R.string.new_place))
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(hostState = viewModel.snackbarHostState) },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        val places by viewModel.places.collectAsState()

        PlaceList(
            places,
            onDeletePlace = { viewModel.delete(it) },
            onEditPlace = {
                showEditDialog = true
                placeToEdit = it
            },
            modifier = Modifier.padding(innerPadding)
        )
    }

    if (showEditDialog) {
        EditPlaceDialog(
            initialPlace = placeToEdit,
            onDismiss = { showEditDialog = false },
            onSave = {
                showEditDialog = false
                viewModel.save(it)
            })
    }
}

@Composable
private fun PlaceList(
    places: List<Place>,
    onDeletePlace: (Place) -> Unit,
    onEditPlace: (Place) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(places, key = { it.uid ?: -1 }) { place ->
            PlaceCard(
                place,
                onDelete = { onDeletePlace(place) },
                onEdit = { onEditPlace(place) },
                modifier = Modifier.animateItem()
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun PlaceCard(
    place: Place,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showContextMenu by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    Card(
        border = BorderStroke(2.dp, colorResource(place.color.colorId)),
        modifier = modifier
            .padding(all = 8.dp)
            .fillMaxWidth()
            .combinedClickable(
                onClick = onEdit,
                onLongClick = { showContextMenu = true }
            )
    ) {
        Text(
            place.name,
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }

    if (showContextMenu) {
        ModalBottomSheet(onDismissRequest = { showContextMenu = false }) {
            Text(
                stringResource(R.string.options),
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            HorizontalDivider(modifier = Modifier.padding(all = 8.dp))

            ListItem(
                headlineContent = { Text(stringResource(R.string.delete_place)) },
                leadingContent = { Icon(Icons.Outlined.Delete, stringResource(R.string.delete)) },
                modifier = Modifier.clickable {
                    showContextMenu = false
                    showDeleteDialog = true
                }
            )

            Spacer(Modifier.height(32.dp))
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            icon = {
                Icon(
                    Icons.Outlined.Delete,
                    stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text(stringResource(R.string.ask_delete_place)) },
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete()
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun EditPlaceDialog(initialPlace: Place?, onDismiss: () -> Unit, onSave: (Place) -> Unit) {
    val title = if (initialPlace != null) {
        stringResource(R.string.edit_object, initialPlace.name)
    } else {
        stringResource(R.string.new_place)
    }

    var name by rememberSaveable(initialPlace) { mutableStateOf(initialPlace?.name ?: "") }
    var color by rememberSaveable(initialPlace) { mutableStateOf(initialPlace?.color) }

    val saveEnabled by remember(name, color) {
        derivedStateOf { name.isNotBlank() && color != null }
    }

    BaseEditDialog(
        title = title,
        onNavigateBack = onDismiss,
        onSave = { onSave(Place(uid = initialPlace?.uid, name = name, color = color!!)) },
        saveEnabled = saveEnabled,
        actions = {}
    ) {
        Column(modifier = Modifier.padding(all = 8.dp)) {
            TextField(
                value = name,
                onValueChange = { name = it },
                leadingIcon = { Icon(Icons.Outlined.Edit, stringResource(R.string.type_name)) },
                placeholder = { Text(stringResource(R.string.name_of_place)) },
                singleLine = true,
                keyboardOptions = remember { KeyboardOptions(capitalization = KeyboardCapitalization.Words) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            OptionGroup(
                label = stringResource(R.string.select_color),
                selectionLabel = if (color != null) stringResource(color!!.nameId) else null,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                ColorSelector(
                    isSelected = { color == it },
                    onSelect = { color = it }
                )
            }
        }
    }
}