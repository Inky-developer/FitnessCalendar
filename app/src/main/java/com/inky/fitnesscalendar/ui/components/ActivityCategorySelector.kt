package com.inky.fitnesscalendar.ui.components

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
import com.inky.fitnesscalendar.data.ActivityCategory

@Composable
fun ActivityCategorySelector(
    isSelected: (ActivityCategory) -> Boolean,
    onSelect: (ActivityCategory) -> Unit
) {
    LazyRow {
        items(ActivityCategory.entries) { category ->
            FilterChip(
                selected = isSelected(category),
                onClick = { onSelect(category) },
                label = {
                    Text(
                        category.emoji,
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                modifier = Modifier.padding(all = 4.dp),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected(category),
                    borderColor = colorResource(category.colorId)
                ),
            )
        }
    }
}