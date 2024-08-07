package com.inky.fitnesscalendar.ui.views.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.db.entities.Place
import com.inky.fitnesscalendar.view_model.PlaceListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceListView(
    viewModel: PlaceListViewModel = hiltViewModel(),
    onOpenDrawer: () -> Unit,
    onEditPlace: (Place?) -> Unit
) {
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
                    IconButton(onClick = { onEditPlace(null) }) {
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
            onEditPlace = { onEditPlace(it) },
            modifier = Modifier.padding(innerPadding)
        )
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

