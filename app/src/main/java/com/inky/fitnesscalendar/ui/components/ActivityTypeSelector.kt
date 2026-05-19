package com.inky.fitnesscalendar.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.ui.util.localDatabaseValues

@Composable
fun ActivityTypeSelector(
    modifier: Modifier = Modifier,
    typeRows: List<List<ActivityType>> = localDatabaseValues.current.activityTypeRows,
    isSelected: (ActivityType) -> Boolean,
    onSelect: (ActivityType) -> Unit,
) {
    Column(modifier = modifier) {
        for (activities in typeRows) {
            LazyRow {
                items(activities) { activityType ->
                    ActivityTypeFilterChip(
                        activityType = activityType,
                        selected = isSelected(activityType),
                        onClick = { onSelect(activityType) },
                    )
                }
            }
        }
    }
}

@Composable
fun ActivityTypeFilterChip(activityType: ActivityType, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                activityType.emoji,
                style = MaterialTheme.typography.headlineMedium
            )
        },
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = colorResource(activityType.color.colorId)
        ),
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}