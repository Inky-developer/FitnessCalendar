package com.inky.fitnesscalendar.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.inky.fitnesscalendar.data.ActivityType
import com.inky.fitnesscalendar.di.ActivityTypeOrder

@Composable
fun ActivityTypeSelector(
    modifier: Modifier = Modifier,
    activityRows: List<List<ActivityType>> = ActivityTypeOrder.getRows(),
    isSelected: (ActivityType) -> Boolean,
    onSelect: (ActivityType) -> Unit,
) {
    Column(modifier = modifier) {
        for (activities in activityRows) {
            LazyRow {
                items(activities) { activityType ->
                    FilterChip(
                        selected = isSelected(activityType),
                        onClick = { onSelect(activityType) },
                        label = {
                            Text(
                                activityType.emoji,
                                style = MaterialTheme.typography.headlineMedium
                            )
                        },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}