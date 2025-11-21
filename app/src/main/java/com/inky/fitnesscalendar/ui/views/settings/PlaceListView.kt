package com.inky.fitnesscalendar.ui.views.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.ContentColor
import com.inky.fitnesscalendar.db.entities.Place
import com.inky.fitnesscalendar.ui.components.ActivityImage
import com.inky.fitnesscalendar.ui.components.BottomSheetButton
import com.inky.fitnesscalendar.ui.components.ImageViewer
import com.inky.fitnesscalendar.ui.components.defaultTopAppBarColors
import com.inky.fitnesscalendar.ui.components.getAppBarContainerColor
import com.inky.fitnesscalendar.ui.util.Icons
import com.inky.fitnesscalendar.ui.util.localDatabaseValues
import com.inky.fitnesscalendar.view_model.PlaceListViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PlaceListView(
    viewModel: PlaceListViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onEditPlace: (Place?) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.places)) },
                colors = defaultTopAppBarColors(),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icons.ArrowBack(stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { onEditPlace(null) }) {
                        Icons.Add(stringResource(R.string.new_place))
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(hostState = viewModel.snackbarHostState) },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        val places by viewModel.places.collectAsState()
        val activityCounts by viewModel.placeActivityCounts.collectAsState()

        var filterColor by rememberSaveable { mutableStateOf<ContentColor?>(null) }
        val filteredPlaces = remember(
            places,
            filterColor
        ) { places.filter { filterColor == null || it.color == filterColor } }

        Column(modifier = Modifier.padding(innerPadding)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(getAppBarContainerColor(scrollBehavior))
            ) {
                ColorFilter(filterColor = filterColor, onFilterColor = { filterColor = it })
            }
            PlaceList(
                filteredPlaces,
                activityCounts,
                onDeletePlace = { viewModel.delete(it) },
                onEditPlace = { onEditPlace(it) },
            )
        }
    }
}

@Composable
private fun PlaceList(
    places: List<Place>,
    activityCounts: Map<Int, Int>,
    onDeletePlace: (Place) -> Unit,
    onEditPlace: (Place) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(contentPadding = PaddingValues(bottom = 128.dp), modifier = modifier) {
        items(places, key = { it.uid ?: -1 }) { place ->
            PlaceCard(
                place,
                activityCount = activityCounts[place.uid] ?: 0,
                onDelete = { onDeletePlace(place) },
                onEdit = { onEditPlace(place) },
                modifier = Modifier.animateItem()
            )
        }
    }
}

@Composable
private fun ColorFilter(filterColor: ContentColor?, onFilterColor: (ContentColor?) -> Unit) {
    val usedColors = localDatabaseValues.current.places.map { it.color }.toSet()
    val contentColors = ContentColor.entries.filter { usedColors.contains(it) }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 8.dp),
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .fillMaxWidth()
    ) {
        items(contentColors) { color ->
            FilterChip(
                selected = filterColor == color,
                onClick = { onFilterColor(if (filterColor == color) null else color) },
                label = { Text(color.text()) },
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(color.color())
                    )
                },
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun PlaceCard(
    place: Place,
    activityCount: Int,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showContextMenu by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var showImageViewer by rememberSaveable { mutableStateOf(false) }

    val imageUri = place.imageName?.getImageUri()

    Card(
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .fillMaxWidth()
            .combinedClickable(
                onClick = onEdit,
                onLongClick = { showContextMenu = true }
            )
    ) {
        Text(
            place.name,
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .padding(top = 8.dp)
        )
        HorizontalDivider(
            color = colorResource(place.color.colorId),
            thickness = 2.dp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        if (imageUri != null) {
            ActivityImage(
                imageUri,
                onClick = { showImageViewer = true },
                modifier = Modifier.padding(all = 8.dp)
            )
            HorizontalDivider()
        }
        Text(
            stringResource(R.string.number_of_activities_n, activityCount),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(all = 8.dp)
        )

        if (place.description.isNotBlank()) {
            HorizontalDivider()
            Text(
                place.description,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(all = 8.dp)
            )
        }

    }

    if (showContextMenu) {
        ModalBottomSheet(onDismissRequest = { showContextMenu = false }) {
            Text(
                stringResource(R.string.options),
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            HorizontalDivider(modifier = Modifier.padding(all = 8.dp))

            BottomSheetButton(
                onClick = {
                    showContextMenu = false
                    showDeleteDialog = true
                },
                leadingIcon = { Icons.Delete(stringResource(R.string.delete)) },
            ) {
                Text(stringResource(R.string.delete_place))
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            icon = {
                Icons.Delete(
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

    if (showImageViewer && imageUri != null) {
        ImageViewer(
            imageUri = imageUri,
            onDismiss = { showImageViewer = false },
        )
    }
}

