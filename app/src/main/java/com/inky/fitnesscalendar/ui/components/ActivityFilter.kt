package com.inky.fitnesscalendar.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilter
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilterChip
import com.inky.fitnesscalendar.ui.util.Icons
import com.inky.fitnesscalendar.ui.util.SharedContentKey
import com.inky.fitnesscalendar.ui.util.localDatabaseValues
import com.inky.fitnesscalendar.ui.util.sharedElement

@Composable
fun FilterInformation(
    filter: ActivityFilter,
    onChange: (ActivityFilter) -> Unit
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val filterItems = remember(filter) { filter.items() }
    val historyItems = localDatabaseValues.current.activityFilterChips
    val filteredHistoryItems = remember(filter, historyItems) {
        historyItems.filter {
            !filterItems.contains(
                it
            )
        }
    }

    LaunchedEffect(filterItems) {
        listState.animateScrollToItem(0)
    }

    LazyRow(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .sharedElement(SharedContentKey.FilterInformation)
    ) {
        items(filterItems, key = { it }) { chip ->
            FilterChip(
                onClick = { onChange(chip.removeFrom(filter)) },
                label = { Text(chip.displayText(context)) },
                leadingIcon = { FilterChipIcon(chip) }
            )
        }

        items(filteredHistoryItems, key = { it }) { chip ->
            SuggestionFilterChip(
                onClick = { onChange(chip.addTo(filter)) },
                label = { Text(chip.displayText(context)) },
            )
        }
    }
}

@Composable
private fun FilterChipIcon(chip: ActivityFilterChip) {
    when (chip) {
        is ActivityFilterChip.AttributeFilterChip -> Icon(
            painterResource(R.drawable.outline_label_24),
            stringResource(R.string.attribute)
        )

        is ActivityFilterChip.CategoryFilterChip -> Text(
            chip.category.emoji,
            style = MaterialTheme.typography.titleLarge
        )

        is ActivityFilterChip.DateFilterChip -> Icons.DateRange(stringResource(R.string.date_range))

        is ActivityFilterChip.TextFilterChip -> Icons.Edit(stringResource(R.string.text))

        is ActivityFilterChip.TypeFilterChip -> Text(
            chip.type.emoji,
            style = MaterialTheme.typography.titleLarge
        )

        is ActivityFilterChip.PlaceFilterChip -> PlaceIcon(chip.place)

        is ActivityFilterChip.VehicleFilterChip -> Text(
            chip.vehicle.emoji,
            style = MaterialTheme.typography.titleLarge
        )

        is ActivityFilterChip.FeelFilterChip -> Text(
            chip.feel.emoji,
            style = MaterialTheme.typography.titleLarge
        )

        is ActivityFilterChip.FavoriteFilterChip -> FavoriteIcon(chip.favorite)
    }
}

@Composable
private fun LazyItemScope.FilterChip(
    leadingIcon: @Composable () -> Unit,
    label: @Composable () -> Unit,
    onClick: () -> Unit
) {
    InputChip(
        selected = false,
        onClick = onClick,
        label = label,
        leadingIcon = leadingIcon,
        trailingIcon = { Icons.Close(stringResource(R.string.clear)) },
        colors = InputChipDefaults.inputChipColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .animateItem()
    )
}

@Composable
private fun LazyItemScope.SuggestionFilterChip(
    label: @Composable () -> Unit,
    onClick: () -> Unit
) {
    SuggestionChip(
        onClick = onClick,
        label = label,
        icon = {
            Icon(
                painterResource(
                    R.drawable.outline_history_24,
                ),
                stringResource(R.string.recent)
            )
        },
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .animateItem()
    )
}