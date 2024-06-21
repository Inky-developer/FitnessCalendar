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
import com.inky.fitnesscalendar.data.ActivityType

@Composable
fun ActivityTypeSelector(
    modifier: Modifier = Modifier,
    typeRows: List<List<ActivityType>>,
    isSelected: (ActivityType) -> Boolean,
    onSelect: (ActivityType) -> Unit,
) {
    Column(modifier = modifier) {
        for (activities in typeRows) {
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
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected(activityType),
                            borderColor = colorResource(activityType.color.colorId)
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
        }
    }
}