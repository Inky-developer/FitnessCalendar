package com.inky.fitnesscalendar.ui.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.ActivityCategory
import com.inky.fitnesscalendar.data.ActivityFilter
import com.inky.fitnesscalendar.data.DateRangeOption
import com.inky.fitnesscalendar.ui.components.ActivityTypeSelector
import com.inky.fitnesscalendar.ui.components.OptionGroup
import com.inky.fitnesscalendar.ui.util.SharedContentKey
import com.inky.fitnesscalendar.ui.util.sharedBounds


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterView(
    filter: ActivityFilter,
    onFilterChange: (ActivityFilter) -> Unit,
    onBack: () -> Unit
) {
    val appBar = @Composable {
        TopAppBar(colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ), title = {
            TextField(
                filter.text ?: "",
                onValueChange = {
                    onFilterChange(filter.copy(text = it))
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
        }, navigationIcon = {
            IconButton(onClick = { onBack() }) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    stringResource(R.string.back),
                )
            }
        }, actions = {
            IconButton(
                onClick = { onFilterChange(ActivityFilter()) }, enabled = !filter.isEmpty()
            ) {
                Icon(Icons.Outlined.Clear, stringResource(R.string.reset_filters))
            }
        }, modifier = Modifier.sharedBounds(SharedContentKey.AppBar)
        )
    }
    Scaffold(
        topBar = appBar, containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            val context = LocalContext.current
            val selectionLabel =
                if (filter.types.isEmpty()) null else filter.types.joinToString(", ") {
                    context.getString(it.nameId)
                }
            val categorySelectionLabel =
                if (filter.categories.isEmpty()) null else filter.categories.joinToString(", ") {
                    context.getString(it.nameId)
                }
            OptionGroup(
                label = stringResource(R.string.select_activity),
                selectionLabel = selectionLabel,
                modifier = Modifier.padding(all = 8.dp)
            ) {
                ActivityTypeSelector(isSelected = { filter.types.contains(it) },
                    onSelect = { activityType ->
                        val oldSelection = filter.types
                        val newSelection =
                            oldSelection.filter { it != activityType }.toMutableList()
                        if (newSelection.size == oldSelection.size) {
                            newSelection.add(activityType)
                        }
                        onFilterChange(filter.copy(types = newSelection))
                    })
            }

            OptionGroup(
                label = stringResource(R.string.select_category),
                selectionLabel = categorySelectionLabel,
                modifier = Modifier.padding(all = 8.dp)
            ) {
                LazyRow {
                    items(ActivityCategory.entries) { category ->
                        FilterChip(
                            selected = filter.categories.contains(category),
                            onClick = {
                                val oldSelection = filter.categories
                                val newSelection =
                                    oldSelection.filter { it != category }.toMutableList()
                                if (newSelection.size == oldSelection.size) {
                                    newSelection.add(category)
                                }
                                onFilterChange(filter.copy(categories = newSelection))
                            },
                            label = {
                                Text(
                                    category.emoji,
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            },
                            modifier = Modifier.padding(all = 4.dp)
                        )
                    }
                }
            }

            OptionGroup(
                label = stringResource(R.string.select_date),
                selectionLabel = filter.range?.nameId?.let { stringResource(it) },
                modifier = Modifier.padding(all = 8.dp)
            ) {
                LazyRow {
                    items(DateRangeOption.entries) { range ->
                        FilterChip(
                            selected = filter.range == range,
                            onClick = {
                                val newRange = if (filter.range == range) {
                                    null
                                } else {
                                    range
                                }
                                onFilterChange(filter.copy(range = newRange))
                            },
                            label = {
                                Text(
                                    stringResource(range.nameId),
                                    style = MaterialTheme.typography.headlineSmall
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