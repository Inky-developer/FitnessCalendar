package com.inky.fitnesscalendar.ui.views

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.Feel
import com.inky.fitnesscalendar.data.Vehicle
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilter
import com.inky.fitnesscalendar.data.activity_filter.AttributeFilter
import com.inky.fitnesscalendar.data.activity_filter.DateRangeOption
import com.inky.fitnesscalendar.db.entities.Place
import com.inky.fitnesscalendar.ui.components.ActivityCategorySelector
import com.inky.fitnesscalendar.ui.components.ActivityTypeSelector
import com.inky.fitnesscalendar.ui.components.OptionGroup
import com.inky.fitnesscalendar.ui.components.PlaceIcon
import com.inky.fitnesscalendar.ui.util.SharedContentKey
import com.inky.fitnesscalendar.ui.util.localDatabaseValues
import com.inky.fitnesscalendar.ui.util.sharedBounds
import com.inky.fitnesscalendar.util.toggled


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterView(
    initialFilter: ActivityFilter,
    onFilterChange: (ActivityFilter) -> Unit,
    onNavigateBack: () -> Unit
) {
    var filter by rememberSaveable(initialFilter) { mutableStateOf(initialFilter) }

    val onBack = {
        onFilterChange(filter)
        onNavigateBack()
    }
    BackHandler {
        onBack()
    }

    val scrollState = rememberScrollState()

    val appBar = @Composable {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            title = {
                TextField(
                    filter.text ?: "",
                    onValueChange = {
                        filter = filter.copy(text = it)
                    },
                    placeholder = { Text(stringResource(R.string.search_for_activity)) },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        focusedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onBack() })
                )
            },
            navigationIcon = {
                IconButton(onClick = { onBack() }) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowBack,
                        stringResource(R.string.back),
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = { filter = ActivityFilter() }, enabled = !filter.isEmpty()
                ) {
                    Icon(Icons.Outlined.Clear, stringResource(R.string.reset_filters))
                }
            },
            modifier = Modifier.sharedBounds(SharedContentKey.AppBar)
        )
    }
    Scaffold(
        topBar = appBar, containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            val context = LocalContext.current
            val selectionLabel = remember(filter) {
                if (filter.types.isEmpty()) null else filter.types.joinToString(", ") { it.name }
            }
            val categorySelectionLabel = remember(filter) {
                if (filter.categories.isEmpty()) null else filter.categories.joinToString(", ") {
                    context.getString(it.nameId)
                }
            }
            val placeSelectionLabel = remember(filter) {
                if (filter.places.isEmpty()) null else filter.places.joinToString(", ") { it.name }
            }
            val attributeSelectionLabel = remember(filter) {
                val entries = filter.attributes.entries()
                    .filter { (_, state) -> state != AttributeFilter.TriState.Undefined }
                if (entries.isEmpty()) null else entries.joinToString(", ") { (attribute, state) ->
                    attribute.getString(context, state.toBooleanOrNull() == true)
                }
            }

            OptionGroup(
                label = stringResource(R.string.filter_by_categories),
                selectionLabel = categorySelectionLabel,
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .padding(top = 4.dp)
            ) {
                ActivityCategorySelector(
                    isSelected = { filter.categories.contains(it) },
                    onSelect = { category ->
                        filter = filter.copy(categories = filter.categories.toggled(category))
                    }
                )
            }

            OptionGroup(
                label = stringResource(R.string.filter_by_activities),
                selectionLabel = selectionLabel,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                ActivityTypeSelector(
                    isSelected = { filter.types.contains(it) },
                    onSelect = { activityType ->
                        filter = filter.copy(types = filter.types.toggled(activityType))
                    },
                )
            }

            if (localDatabaseValues.current.places.isNotEmpty()) {
                OptionGroup(
                    label = stringResource(R.string.filter_by_places),
                    selectionLabel = placeSelectionLabel,
                    modifier = Modifier.padding(all = 8.dp)
                ) {
                    PlacesSelector(
                        isSelected = { filter.places.contains(it) },
                        onSelect = { place ->
                            filter = filter.copy(places = filter.places.toggled(place))
                        }
                    )
                }
            }

            OptionGroup(
                label = stringResource(R.string.filter_by_date),
                selectionLabel = filter.range?.getText(context),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                LazyRow {
                    items(DateRangeKind.entries) { range ->
                        FilterChip(
                            selected = filter.range?.name == range.rangeName,
                            onClick = {
                                val newRange = if (filter.range?.name == range.rangeName) {
                                    null
                                } else {
                                    range.toOption()
                                }
                                filter = filter.copy(range = newRange)
                            },
                            label = {
                                Text(
                                    range.toOption().getText(context),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            },
                            modifier = Modifier.padding(all = 4.dp)
                        )
                    }
                }
            }

            FilterSelectionGroup(
                label = stringResource(R.string.filter_by_vehicles),
                text = { context.getString(it.nameId) },
                selection = filter.vehicles,
                options = Vehicle.entries,
                onSelection = { filter = filter.copy(vehicles = it) }
            ) {
                Text(it.emoji, style = MaterialTheme.typography.headlineMedium)
            }

            FilterSelectionGroup(
                label = stringResource(R.string.filter_by_feels),
                text = { context.getString(it.nameId) },
                selection = filter.feels,
                options = Feel.entries,
                onSelection = { filter = filter.copy(feels = it) }
            ) {
                Text(it.emoji, style = MaterialTheme.typography.headlineMedium)
            }

            OptionGroup(
                label = stringResource(R.string.filter_by_attributes),
                selectionLabel = attributeSelectionLabel,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                LazyRow {
                    items(AttributeFilter.Attribute.entries) { attribute ->
                        FilterChip(
                            selected = filter.attributes.get(attribute).toBooleanOrNull() != null,
                            onClick = {
                                val newState = when (filter.attributes.get(attribute)) {
                                    AttributeFilter.TriState.Undefined -> AttributeFilter.TriState.Yes
                                    AttributeFilter.TriState.Yes -> AttributeFilter.TriState.No
                                    AttributeFilter.TriState.No -> AttributeFilter.TriState.Undefined
                                }
                                filter = filter.copy(
                                    attributes = filter.attributes.with(
                                        attribute,
                                        newState
                                    )
                                )

                            },
                            label = {
                                Text(
                                    stringResource(attribute.nameId),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            },
                            modifier = Modifier.padding(all = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlacesSelector(isSelected: (Place) -> Boolean, onSelect: (Place) -> Unit) {
    val places = localDatabaseValues.current.places
    LazyRow {
        items(places) { place ->
            FilterChip(
                selected = isSelected(place),
                onClick = { onSelect(place) },
                label = {
                    Text(
                        place.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                leadingIcon = { PlaceIcon(place) },
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected(place),
                    borderColor = colorResource(place.color.colorId)
                ),
                modifier = Modifier.padding(all = 4.dp)
            )
        }
    }
}

@Composable
private fun <T> FilterSelectionGroup(
    label: String,
    text: (T) -> String,
    selection: List<T>,
    options: List<T>,
    onSelection: (List<T>) -> Unit,
    icon: @Composable (T) -> Unit,
) {
    val selectionLabel = remember(selection) {
        if (selection.isEmpty()) null else selection.joinToString(", ") { text(it) }
    }
    OptionGroup(
        label = label,
        selectionLabel = selectionLabel,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        LazyRow {
            items(options) { option ->
                FilterChip(
                    selected = selection.contains(option),
                    onClick = {
                        onSelection(selection.toggled(option))
                    },
                    label = { icon(option) },
                    modifier = Modifier.padding(all = 4.dp)
                )
            }
        }
    }
}

private enum class DateRangeKind(val rangeName: DateRangeOption.DateRangeName) {
    SevenDays(DateRangeOption.DateRangeName.SevenDays),
    LastWeek(DateRangeOption.DateRangeName.LastWeek),
    ThirtyDays(DateRangeOption.DateRangeName.ThirtyDays),
    LastMonth(DateRangeOption.DateRangeName.LastMonth),
    Year(DateRangeOption.DateRangeName.Year),
    LastYear(DateRangeOption.DateRangeName.LastYear);

    fun toOption() = when (this) {
        SevenDays -> DateRangeOption.sevenDays()
        LastWeek -> DateRangeOption.lastWeek()
        ThirtyDays -> DateRangeOption.thirtyDays()
        LastMonth -> DateRangeOption.lastMonth()
        Year -> DateRangeOption.year()
        LastYear -> DateRangeOption.lastYear()
    }
}